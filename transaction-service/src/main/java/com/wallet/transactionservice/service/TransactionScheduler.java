package com.wallet.transactionservice.service;

import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionScheduler {
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Scheduled(fixedDelayString = "${transaction.scheduler.pending-cleanup.delay-ms}")
    @Transactional
    public void removePendingTransactions() {
        List<Transaction> removedTransactions = transactionRepository.cleanUpPendingTransactions();
        removedTransactions.forEach(
                t -> transactionService.cancelTransaction(t.getId())
        );
    }
}
