package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record PeriodGroupedExpenseDto(
        BigDecimal totalSpending,
        List<CategorySpending> spendingByCategory,
        List<DailyTransactionDto> dailyTransactions
) {
}
