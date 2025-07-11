package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.enums.CardType;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.feign.CardClient;
import com.wallet.cardservice.kafka.CardKafkaProducer;
import com.wallet.cardservice.mapper.CardMapper;
import com.wallet.cardservice.mapper.HolderMapper;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.util.CardInfoCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // TODO: recentTransactions - сделать функционал
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
        cardRepository.save(card);
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

}
