package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.enums.CardType;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.exception.InsufficientBalanceException;
import com.wallet.cardservice.feign.CardClient;
import com.wallet.cardservice.kafka.CardKafkaProducer;
import com.wallet.cardservice.mapper.CardMapper;
import com.wallet.cardservice.mapper.HolderMapper;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.repository.LimitRepository;
import com.wallet.cardservice.util.CardDataValidator;
import com.wallet.cardservice.util.CardInfoCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardInfoCollector cardInfoCollector;
    private final CardKafkaProducer cardKafkaProducer;
    private final CardClient cardClient;
    private final HolderMapper holderMapper;
    private final CardDataValidator cardDataValidator;
    private final LimitRepository limitRepository;

    public List<CardPreviewDto> getLinkedCards(UUID userId) {
        return cardRepository.findAllByUserId(userId).stream()
                .map(card -> CardPreviewDto.builder()
                        .maskedCardNumber(maskCardNumber(card.getNumber()))
                        .issuer(card.getCardIssuer())
                        .scheme(card.getCardScheme())
                        .cardType(CardType.DEBIT)
                        .isFrozen(false)
                        .isBlocked(false)
                        .balance(card.getMoney())
                        .build())
                .toList();
    }

    public CardDetailsDto getLinkedCard(String number, UUID userId) {
        Card card = cardRepository.getCardByNumber(number).orElseThrow(() -> new CardNotFoundException("Card not found"));
        return CardDetailsDto.builder()
                .balance(card.getMoney())
                .issuer(card.getCardIssuer())
                .scheme(card.getCardScheme())
                .cardType(CardType.DEBIT)
                .holder(getHolder(userId))
                .secretDetails(getCardSecretDetails(card))
                .isFrozen(false)
                .isBlocked(false)
                .recentTransactions(List.of())
                .limit(new CardLimitDto(
                        card.getLimit().getPerTransactionLimit(),
                        card.getLimit().isLimitEnabled()))
                .build();
    }

    private Holder getHolder(UUID userId) {
        HolderDto dto = cardClient.getHolder(userId).getBody();
        return holderMapper.toEntity(dto);
    }

    private CardSecretDetails getCardSecretDetails(Card card) {
        return new CardSecretDetails(
                card.getNumber(),
                card.getExpirationDate(),
                card.getCvv()
        );
    }

    @Transactional
    public void saveCard(SaveCardDto saveCardDto) {
        Card card = cardMapper.toEntity(saveCardDto);
        enrichCardWithMeta(card);

        Limit defaultLimit = Limit.builder()
                .perTransactionLimit(new BigDecimal("1000000"))
                .limitEnabled(true)
                .build();

        Limit savedLimit = limitRepository.save(defaultLimit);
        card.setLimit(savedLimit);

        Card savedCard = cardRepository.save(card);
        savedLimit.setCard(savedCard);

        sendCardLinkedEvent(saveCardDto.getUserId(), saveCardDto.getEmail(), maskCardNumber(card.getNumber()), card.getCardIssuer(), card.getCardScheme(), LocalDateTime.now());
    }

    private void enrichCardWithMeta(Card card) {
        CardMeta meta = cardInfoCollector.getCardMeta(card.getNumber());
        card.setCardIssuer(meta.issuer());
        card.setCardScheme(meta.scheme());
    }

    private void sendCardLinkedEvent(UUID userId, String email, String maskedNumber, String issuer, String scheme, LocalDateTime linkedAt) {
        CardLinkedEvent event = new CardLinkedEvent(userId, email, maskedNumber, issuer, scheme, linkedAt);
        cardKafkaProducer.sendCardLinkedEvent(event);
    }

    private String maskCardNumber(String number) {
        return "*" + number.substring(number.length() - 4);
    }

    public boolean isCardLinkedToUser(String cardNumber, UUID userId) {
        return cardRepository.getCardByNumber(cardNumber)
                .map(card -> card.getUserId().equals(userId))
                .orElse(false);
    }

    public Card getCardByNumber(String number) {
        return cardRepository.getCardByNumber(number).orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    @Transactional
    public void freeze(String number, String email, UUID userId) {
        Card card = getCardByNumber(number);
        card.setFrozen(true);
        cardRepository.save(card);
        cardKafkaProducer.sendCardFrozenEvent(maskCardNumber(number), email, userId);
    }

    @Transactional
    public void unfreeze(String number, String email, UUID userId) {
        Card card = getCardByNumber(number);
        card.setFrozen(false);
        cardRepository.save(card);
        cardKafkaProducer.sendCardUnfrozenEvent(maskCardNumber(number), email, userId);
    }

    @Transactional
    public void block(String number, String email, UUID userId) {
        Card card = getCardByNumber(number);
        card.setBlocked(true);
        cardRepository.save(card);
        cardKafkaProducer.sendCardBlockedEvent(maskCardNumber(number), email, userId);
    }

    public CardStatusAction convertStringToCardStatusAction(String inputString) {
        try {
            return CardStatusAction.valueOf(inputString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CardStatusActionException("Invalid card action: " + inputString);
        }
    }

    @Transactional
    public void removeCard(String number, UUID userId) {
        if (!isCardLinkedToUser(number, userId)) {
            throw new CardAccessDeniedException("You can't remove someone's card");
        }
        cardRepository.deleteCardByNumber(number);
    }

    public CardStatusDto getCardStatus(String number) {
        Card card = getCardByNumber(number);
        return new CardStatusDto(
                card.isFrozen(),
                card.isBlocked(),
                cardDataValidator.isCardExpired(card.getExpirationDate())
        );
    }

    @Transactional
    public void subtractMoney(UUID userId, BigDecimal amount, String cardNumber) {
        Card card = getCardByNumber(cardNumber);

        if (!isCardLinkedToUser(cardNumber, userId)) {
            throw new CardAccessDeniedException("You can't subtract money from someone's card");
        }

        if (card.getMoney().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        card.setMoney(amount);
        cardRepository.save(card);
    }
}
