package com.wallet.walletservice.dto;

import com.wallet.walletservice.enums.TransactionCategory;

public record RecentTransactionsDto(
        String recipient,
        TransactionCategory transactionCategory,
        String date,
        String comment,
        String amount
) {
}
