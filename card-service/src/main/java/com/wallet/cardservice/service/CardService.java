package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import com.wallet.cardservice.entity.CardMetadata;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.enums.CardSortOrder;
import com.wallet.cardservice.enums.CardSortType;
import com.wallet.cardservice.enums.CardStatus;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.feign.TransactionFeignClient;
import com.wallet.cardservice.feign.UserFeignClient;
import com.wallet.cardservice.kafka.CardKafkaProducer;
import com.wallet.cardservice.mapper.*;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.util.CardInfoCollector;
import com.wallet.cardservice.util.CardSecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardKafkaProducer cardKafkaProducer;
    private final UserFeignClient userFeignClient;
    private final HolderMapper holderMapper;
    private final TransactionFeignClient transactionFeignClient;
    private final CardLimitService cardLimitService;
    private final CardMetadataMapper cardMetadataMapper;
    private final CardDetailsMapper cardDetailsMapper;
    private final CardCacheService cardCacheService;
    private final CardLimitMapper cardLimitMapper;
    private final CardSortManager cardSortManager;
    private final CardInfoCollector cardInfoCollector;

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
    public CardInfoDto getCardById(Long cardId, UUID userId) {
        if (!isCardLinkedToUser(cardId, userId)) {
            throw new CardAccessDeniedException("Access to the card is forbidden");
        }

        CardInfoDto cached = getCardInfoFromCache(cardId, userId);
        if (cached != null) {
            return cached;
        }
        return loadAndCacheCardInfo(cardId, userId);
    }

    private CardInfoDto getCardInfoFromCache(Long cardId, UUID userId) {
        CardInfoDto cachedCardInfo = cardCacheService.getCardById(cardId, userId);
        if (cachedCardInfo == null) {
            return null;
        }
        List<TransactionDto> recentTransactions = getRecentTransactions(cachedCardInfo.getSecretDetails().number());
        cachedCardInfo.setRecentTransactions(recentTransactions);
        return cachedCardInfo;
    }

    private CardInfoDto loadAndCacheCardInfo(Long cardId, UUID userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        CardInfoDto cardInfoDto = getDetailedCardInfo(userId, card);

        cardCacheService.saveCard(cardId, userId, cardInfoDto);

        cardInfoDto.setRecentTransactions(getRecentTransactions(cardInfoDto.getSecretDetails().number()));
        return cardInfoDto;
    }

    private CardInfoDto getDetailedCardInfo(UUID userId, Card card) {
        CardDetails cardDetails = card.getCardDetails();
        CardMetadata cardMetadata = card.getCardMetadata();
        Limit limit = cardLimitService.getLimitByCard(card);

        return CardInfoDto.builder()
                .cardDto(cardMapper.toDto(card))
                .cardMetadataDto(cardMetadataMapper.toDto(cardMetadata))
                .holder(getCardHolder(userId))
                .secretDetails(cardDetailsMapper.toDto(cardDetails))
                .recentTransactions(Collections.emptyList())
                .limit(cardLimitMapper.toDto(limit))
                .build();
    }

    private Holder getCardHolder(UUID userId) {
        HolderDto dto = userFeignClient.getCardHolder(userId).getBody();
        return holderMapper.toEntity(dto);
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
        Limit limit = cardLimitService.createDefaultLimit();
        limit.setCard(card);
        card.setLimit(limit);
    }

    private void sendCardLinkedEvent(Card card, String email) {
        CardLinkedEvent event = new CardLinkedEvent(
                card.getUserId(),
                email,
                CardSecurityProvider.maskCardNumber(card.getCardDetails().getNumber()),
                card.getCardMetadata().getIssuer(),
                card.getCardMetadata().getPaymentScheme(),
                Instant.now()
        );
        cardKafkaProducer.sendCardLinkedEvent(event);
    }

    @Transactional(readOnly = true)
    public boolean isCardLinkedToUser(Long cardId, UUID userId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getUserId().equals(userId))
                .orElse(false);
    }

//    @Transactional
//    public void freeze(String number, String email, UUID userId) {
//        Card card = getCardByNumber(number);
//        card.setFrozen(true);
//        cardRepository.save(card);
//        cardKafkaProducer.sendCardFrozenEvent(maskCardNumber(number), email, userId);
//    }
//
//    @Transactional
//    public void unfreeze(String number, String email, UUID userId) {
//        Card card = getCardByNumber(number);
//        card.setFrozen(false);
//        cardRepository.save(card);
//        cardKafkaProducer.sendCardUnfrozenEvent(maskCardNumber(number), email, userId);
//    }
//
//    @Transactional
//    public void block(String number, String email, UUID userId) {
//        Card card = getCardByNumber(number);
//        card.setBlocked(true);
//        cardRepository.save(card);
//        cardKafkaProducer.sendCardBlockedEvent(maskCardNumber(number), email, userId);
//    }

//    public CardStatus convertStringToCardStatusAction(String inputString) {
//        try {
//            return CardStatus.valueOf(inputString.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new CardStatusActionException("Invalid card action: " + inputString);
//        }
//    }

//    @Transactional
//    public void removeCard(String number, UUID userId) {
//        if (!isCardLinkedToUser(number, userId)) {
//            throw new CardAccessDeniedException("You can't remove someone's card");
//        }
//        cardRepository.deleteCardByNumber(number);
//    }

//    public CardStatusDto getCardStatus(String number) {
//        Card card = getCardByNumber(number);
//        return new CardStatusDto(
//                card.isFrozen(),
//                card.isBlocked(),
//                cardDataValidator.isCardExpired(card.getExpirationDate())
//        );
//    }

//    @Transactional
//    public void subtractMoney(UUID userId, BigDecimal amount, String cardNumber) {
//        Card card = getCardByNumber(cardNumber);
//
//        if (!isCardLinkedToUser(cardNumber, userId)) {
//            throw new CardAccessDeniedException("You can't subtract money from someone's card");
//        }
//
//        if (card.getMoney().compareTo(amount) < 0) {
//            throw new InsufficientBalanceException("Insufficient balance");
//        }
//        card.setMoney(card.getMoney().subtract(amount));
//        cardRepository.save(card);
//    }
}