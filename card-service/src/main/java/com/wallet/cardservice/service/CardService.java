package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import com.wallet.cardservice.entity.CardMetadata;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.enums.CardSortOrder;
import com.wallet.cardservice.enums.CardSortType;
import com.wallet.cardservice.enums.CardStatus;
import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.event.CardStatusChangedEvent;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.exception.InsufficientBalanceException;
import com.wallet.cardservice.feign.TransactionFeignClient;
import com.wallet.cardservice.kafka.CardKafkaProducer;
import com.wallet.cardservice.mapper.CardMapper;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.util.CardInfoCollector;
import com.wallet.cardservice.util.CardSecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardKafkaProducer cardKafkaProducer;
    private final TransactionFeignClient transactionFeignClient;
    private final LimitService limitService;
    private final CardCacheService cardCacheService;
    private final CardSortManager cardSortManager;
    private final CardInfoCollector cardInfoCollector;
    private final CardSecurityProvider cardSecurityProvider;
    private final CardInfoService cardInfoService;

    private final int RECENT_TRANSACTIONS_COUNT = 3;

    @Transactional(readOnly = true)
    public List<CardPreviewDto> getCards(UUID userId, CardSortType sort, CardSortOrder order, int offset, int limit) throws ExecutionException, InterruptedException {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<Card> cards = switch (sort) {
            case RECENT -> cardSortManager.findAllCardsByLastUse(userId, offset, limit);
            case NAME -> cardSortManager.findAllCardsByIssuerName(userId, order, pageable);
            case BALANCE -> cardSortManager.findAllCardsByBalance(userId, order, pageable);
            case EXPIRATION -> cardSortManager.findAllCardsByExpiration(userId, order, pageable);
            case LIMIT -> cardSortManager.findAllCardsByLimit(userId, order, pageable);
        };

        return cards.stream()
                .map(this::getPreviewCardInfo)
                .toList();
    }

    @Transactional(readOnly = true)
    public Card getCardById(Long cardId, UUID userId) {
        cardSecurityProvider.checkCardOwner(cardId, userId);
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoById(Long cardId, UUID userId) {
        CardInfoDto cardInfoDto = cardInfoService.getCardInfoById(cardId, userId);
        List<TransactionDto> recentTransactions = getRecentTransactions(cardInfoDto.getSecretDetails().number());
        cardInfoDto.setRecentTransactions(recentTransactions);
        return cardInfoDto;
    }

    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoByNumber(String cardNumber, UUID userId) {
        return cardInfoService.getCardInfoByNumber(cardNumber, userId);
    }

    private List<TransactionDto> getRecentTransactions(String cardNumber) {
        return transactionFeignClient.getRecentTransactions(cardNumber, RECENT_TRANSACTIONS_COUNT);
    }

    private CardPreviewDto getPreviewCardInfo(Card card) {
        CardDetails cardDetails = card.getCardDetails();
        CardMetadata cardMetadata = card.getCardMetadata();

        return CardPreviewDto.builder()
                .number(cardDetails.getNumber())
                .issuer(cardMetadata.getIssuer())
                .scheme(cardMetadata.getPaymentScheme())
                .balance(card.getBalance())
                .build();
    }

    @Transactional
    public void saveCard(SaveCardDto saveCardDto, String email, UUID userId) {
        cardSecurityProvider.checkUserCardAmount(userId);
        Card card = buildCardEntity(saveCardDto, userId);
        cardRepository.save(card);
        sendCardLinkedEvent(card, email);
    }

    private Card buildCardEntity(SaveCardDto dto, UUID userId) {
        Card card = cardMapper.toEntity(dto);
        card.setUserId(userId);
        card.setStatus(CardStatus.ACTIVE);

        linkCardDetails(card);
        linkCardMetadata(card);
        linkCardLimit(card);

        return card;
    }

    private void linkCardDetails(Card card) {
        if (card.getCardDetails() != null) {
            card.getCardDetails().setCard(card);
        }
    }

    private void linkCardMetadata(Card card) {
        CardMeta meta = cardInfoCollector.getCardMeta(card.getCardDetails().getNumber());
        CardMetadata metadata = CardMetadata.builder()
                .card(card)
                .issuer(meta.issuer())
                .paymentScheme(meta.scheme())
                .build();
        card.setCardMetadata(metadata);
    }

    private void linkCardLimit(Card card) {
        Limit limit = limitService.createDefaultLimit();
        limit.setCard(card);
        card.setLimit(limit);
    }

    private void sendCardLinkedEvent(Card card, String email) {
        CardLinkedEvent event = new CardLinkedEvent(
                card.getUserId(),
                email,
                cardSecurityProvider.maskCardNumber(card.getCardDetails().getNumber()),
                card.getCardMetadata().getIssuer(),
                card.getCardMetadata().getPaymentScheme(),
                Instant.now()
        );
        cardKafkaProducer.sendCardLinkedEvent(event);
    }

    @Transactional
    public void updateCardStatus(CardStatusAction action, Long cardId, UUID userId, String email) {
        cardSecurityProvider.checkCardOwner(cardId, userId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        switch (action) {
            case FREEZE -> {
                if (card.getStatus().equals(CardStatus.FROZEN)) throw new CardStatusActionException("The card is already frozen");
                card.setStatus(CardStatus.FROZEN);
            }
            case UNFREEZE -> {
                if (!card.getStatus().equals(CardStatus.FROZEN)) throw new CardStatusActionException("The card isn't frozen");
                card.setStatus(CardStatus.ACTIVE);
            }
            case BLOCK -> {
                if (card.getStatus().equals(CardStatus.BLOCKED)) throw new CardStatusActionException("The card is already blocked");
                card.setStatus(CardStatus.BLOCKED);
            }
            default -> throw new CardStatusActionException("Unsupported action: " + action);
        }

        cardRepository.save(card);
        cardKafkaProducer.sendCardStatusChangedEvent(new CardStatusChangedEvent(
                        email,
                        cardSecurityProvider.maskCardNumber(card.getCardDetails().getNumber()),
                        card.getStatus().toString(),
                        Instant.now()
                ),
                userId
        );
        cardCacheService.evictCardById(cardId);
    }

    @CacheEvict(value = "card", key = "#cardId + ':user:' + #userId")
    @Transactional
    public void deleteCard(Long cardId, UUID userId) {
        cardSecurityProvider.checkCardOwner(cardId, userId);
        cardRepository.deleteById(cardId);
    }

    @Transactional
    public void subtractMoney(String cardNumber, UUID userId, BigDecimal amount) {
        Card card = cardRepository.findByCardDetails_Number(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        cardSecurityProvider.checkCardOwner(card.getId(), userId);
        if (card.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);

        cardCacheService.evictCardById(card.getId());
        cardCacheService.evictAllCardsByNumber(cardNumber);
    }
}