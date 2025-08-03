package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyTransactionDto(
        LocalDate date,
        BigDecimal dailyTotal,
        List<TransactionDto> transactions
) {
}
