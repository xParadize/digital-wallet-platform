package com.wallet.transactionservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record DailyTransactionDto(
        String date,
        BigDecimal dailyTotal,
        List<TransactionDto> transactions
) {
}
