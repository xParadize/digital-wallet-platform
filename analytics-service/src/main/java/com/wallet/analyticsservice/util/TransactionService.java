package com.wallet.analyticsservice.util;

import com.wallet.analyticsservice.entity.TransactionEvent;
import com.wallet.analyticsservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public void save(TransactionEvent transactionEvent) {
        log.info("transaction completed");
        transactionRepository.save(transactionEvent);
    }
}
