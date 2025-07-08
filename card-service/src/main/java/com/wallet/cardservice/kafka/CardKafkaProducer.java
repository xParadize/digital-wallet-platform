package com.wallet.cardservice.kafka;

import com.wallet.cardservice.event.CardLinkedEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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

    private void logEvent(ProducerRecord<String, Object> record) {
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
