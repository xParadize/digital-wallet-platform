package com.wallet.transactionservice.kafka;

import com.wallet.transactionservice.entity.OutboxEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TransactionKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendTransactionEvent(OutboxEvent event) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    "transaction.events",
                    event.getId().toString(),
                    event.getPayload()
            );
            kafkaTemplate.send(record);
            logEvent(record);
        } catch (Exception e) {
            System.err.println("Failed to send event with ID " + event.getId() + ": " + e.getMessage());
        }
    }

    private void logEvent(ProducerRecord<String, String> record) {
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
