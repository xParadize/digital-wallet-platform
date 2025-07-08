package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.CardMeta;
import com.wallet.cardservice.dto.SaveCardDto;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.kafka.CardKafkaProducer;
import com.wallet.cardservice.mapper.CardMapper;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.util.CardInfoCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardInfoCollector cardInfoCollector;
    private final CardKafkaProducer cardKafkaProducer;

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
        return cardRepository.getCardByNumber(cardNumber).isPresent();
    }
}
