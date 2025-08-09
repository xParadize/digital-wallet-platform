package com.wallet.analyticsservice.dto;

import com.wallet.analyticsservice.enums.TransactionCategory;

public record RecentTransactionsDto(
        String recipient,
        TransactionCategory transactionCategory,
        String date,
        String comment,
        String amount
) {
}
