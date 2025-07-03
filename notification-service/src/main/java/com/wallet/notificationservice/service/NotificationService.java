package com.wallet.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.notificationservice.event.EmailConfirmationEvent;
import com.wallet.notificationservice.event.EmailConfirmedEvent;
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
}
