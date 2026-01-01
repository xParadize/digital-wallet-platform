package com.wallet.analyticsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.analyticsservice.entity.TransactionEvent;
import com.wallet.analyticsservice.util.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction.events", groupId = "digital-wallet-transaction")
    public void consumeTransactionEvent(String payload) {
        try {
            TransactionEvent event = objectMapper.readValue(payload, TransactionEvent.class);
            transactionService.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize transaction event", e);
        }
    }
}
