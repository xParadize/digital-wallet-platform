package com.wallet.transactionservice.kafka;

import com.wallet.transactionservice.entity.OutboxEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${transaction.outbox.topic}")
    private String topic;

    public void sendTransactionEvent(OutboxEvent event) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
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
