package com.wallet.authservice.kafka;

import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.event.EmailConfirmationEvent;
import com.wallet.authservice.event.EmailConfirmedEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class AuthKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendEmailConfirmation(UnverifiedUser unverifiedUser) {
        EmailConfirmationEvent event = new EmailConfirmationEvent(
                unverifiedUser.getId(),
                unverifiedUser.getEmail(),
                unverifiedUser.getName()
        );
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "auth.user.email-confirmation",
                unverifiedUser.getId().toString(),
                event
        );
        kafkaTemplate.send(record);
        logEvent(record);
    }

    public void sendEmailConfirmed(String email, UUID userId) {
        EmailConfirmedEvent event = new EmailConfirmedEvent(email);
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "auth.user.email-confirmed",
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
