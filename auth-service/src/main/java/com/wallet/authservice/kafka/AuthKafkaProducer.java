package com.wallet.authservice.kafka;

import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.event.EmailConfirmationEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
