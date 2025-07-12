package com.wallet.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.notificationservice.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MailSenderService mailSenderService;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "auth.user.email-confirmation", groupId = "digital-wallet-auth")
    public void consumeEmailConfirmation(String message) throws JsonProcessingException {
        EmailConfirmationEvent event = objectMapper.readValue(message, EmailConfirmationEvent.class);
        mailSenderService.sendVerifyEmail(
                event.email(),
                event.name(),
                event.userId()
        );
    }

    @Transactional
    @KafkaListener(topics = "auth.user.email-confirmed", groupId = "digital-wallet-auth")
    public void consumeEmailConfirmed(String message) throws JsonProcessingException {
        EmailConfirmedEvent event = objectMapper.readValue(message, EmailConfirmedEvent.class);
        mailSenderService.sendEmailConfirmed(event.email());
    }

    @Transactional
    @KafkaListener(topics = "auth.user.password-changed", groupId = "digital-wallet-auth")
    public void consumePasswordChanged(String message) throws JsonProcessingException {
        PasswordChangedEvent event = objectMapper.readValue(message, PasswordChangedEvent.class);
        mailSenderService.sendPasswordChanged(event.email());
    }

    @Transactional
    @KafkaListener(topics = "card.linked", groupId = "digital-wallet-card")
    public void consumeCardLinked(String message) throws JsonProcessingException {
        CardLinkedEvent event = objectMapper.readValue(message, CardLinkedEvent.class);
        mailSenderService.sendCardLinked(event);
    }

    @Transactional
    @KafkaListener(topics = "card.frozen", groupId = "digital-wallet-card")
    public void consumeCardFrozen(String message) throws JsonProcessingException {
        CardFrozenEvent event = objectMapper.readValue(message, CardFrozenEvent.class);
        mailSenderService.sendCardFrozen(event);
    }

    @Transactional
    @KafkaListener(topics = "card.unfrozen", groupId = "digital-wallet-card")
    public void consumeCardUnfrozen(String message) throws JsonProcessingException {
        CardUnfrozenEvent event = objectMapper.readValue(message, CardUnfrozenEvent.class);
        mailSenderService.sendCardUnfrozen(event);
    }
}
