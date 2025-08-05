package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record PeriodGroupedIncomeDto(
        BigDecimal totalIncome,
        List<CategoryIncome> incomeByCategory,
        List<DailyTransactionDto> dailyTransactions
) {
}
