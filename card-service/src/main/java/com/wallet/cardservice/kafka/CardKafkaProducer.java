package com.wallet.cardservice.kafka;

import com.wallet.cardservice.event.CardBlockedEvent;
import com.wallet.cardservice.event.CardFrozenEvent;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.event.CardUnfrozenEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@AllArgsConstructor
public class CardKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendCardLinkedEvent(CardLinkedEvent event) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "card.linked",
                event.userId().toString(),
                event
        );
        kafkaTemplate.send(record);
        logEvent(record);
    }

    public void sendCardFrozenEvent(String number, String email, UUID userId) {
        CardFrozenEvent event = new CardFrozenEvent(email, number, Instant.now());
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "card.frozen",
                userId.toString(),
                event
        );
        kafkaTemplate.send(record);
        logEvent(record);
    }

    public void sendCardUnfrozenEvent(String number, String email, UUID userId) {
        CardUnfrozenEvent event = new CardUnfrozenEvent(email, number, Instant.now());
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "card.unfrozen",
                userId.toString(),
                event
        );
        kafkaTemplate.send(record);
        logEvent(record);
    }

    public void sendCardBlockedEvent(String number, String email, UUID userId) {
        CardBlockedEvent event = new CardBlockedEvent(email, number, Instant.now());
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "card.blocked",
                userId.toString(),
                event
        );
        kafkaTemplate.send(record);
        logEvent(record);
    }

    private void logEvent(ProducerRecord<String, Object> record) {
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
