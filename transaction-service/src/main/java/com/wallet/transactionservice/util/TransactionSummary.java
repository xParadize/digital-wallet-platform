package com.wallet.transactionservice.util;

import com.wallet.transactionservice.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionSummary {
    public BigDecimal totalSpending = BigDecimal.ZERO;
    public BigDecimal totalIncome = BigDecimal.ZERO;
    public Map<LocalDate, List<Transaction>> transactionsByDate = new HashMap<>();
}