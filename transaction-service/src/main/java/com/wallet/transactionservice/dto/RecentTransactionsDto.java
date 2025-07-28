package com.wallet.transactionservice.dto;

import com.wallet.transactionservice.enums.TransactionCategory;

public record RecentTransactionsDto(
        String recipient,
        TransactionCategory transactionCategory,
        String date,
        String comment,
        String amount
) {
}
