package com.wallet.transactionservice.service;

import com.wallet.transactionservice.entity.OutboxEvent;
import com.wallet.transactionservice.kafka.TransactionKafkaProducer;
import com.wallet.transactionservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxRepository outboxRepository;
    private final TransactionKafkaProducer transactionKafkaProducer;

    @Scheduled(fixedDelayString = "${transaction.outbox.scheduler.delay-ms}")
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findAllByOrderByCreatedAt();
        for (OutboxEvent event : events) {
            transactionKafkaProducer.sendTransactionEvent(event);
            outboxRepository.deleteById(event.getId());
        }
    }
}
