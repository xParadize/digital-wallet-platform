package com.wallet.cardservice.dto;

import com.wallet.cardservice.enums.TransactionCategory;

public record RecentTransactionsDto(
        String recipient,
        TransactionCategory transactionCategory,
        String date,
        String comment,
        String amount
) {
}
