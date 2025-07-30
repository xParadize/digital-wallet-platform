package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record PeriodGroupedTransactionsDto(
        BigDecimal totalSpending,
        BigDecimal totalIncome,
        List<DailyTransactionDto> dailyTransactions
) {
}
