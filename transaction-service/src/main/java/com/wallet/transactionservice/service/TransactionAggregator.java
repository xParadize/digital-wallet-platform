package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.CategoryIncome;
import com.wallet.transactionservice.dto.CategorySpending;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.util.DateConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionAggregator {
    private final List<Transaction> transactions;
    private final DateConverter dateConverter;
    private BigDecimal totalSpending;
    private BigDecimal totalIncome;
    private Map<LocalDate, BigDecimal> dailyTotals;
    private Map<LocalDate, BigDecimal> dailySpendingTotals;
    private Map<LocalDate, BigDecimal> dailyIncomeTotals;
    private List<CategorySpending> categorySpending;
    private List<CategoryIncome> categoryIncome;

    public BigDecimal getTotalSpending() {
        if (totalSpending == null) {
            totalSpending = transactions.stream()
                    .filter(t -> t.getAmount().signum() < 0)
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return totalSpending;
    }

    public BigDecimal getTotalIncome() {
        if (totalIncome == null) {
            totalIncome = transactions.stream()
                    .filter(t -> t.getAmount().signum() > 0)
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return totalIncome;
    }

    public Map<LocalDate, BigDecimal> getDailyTotals() {
        if (dailyTotals == null) {
            dailyTotals = calculateDailyTotals(transactions);
        }
        return dailyTotals;
    }

    public Map<LocalDate, BigDecimal> getDailySpendingTotals() {
        if (dailySpendingTotals == null) {
            dailySpendingTotals = calculateDailyTotals(
                    transactions.stream()
                            .filter(t -> t.getAmount().signum() < 0)
                            .toList()
            );
        }
        return dailySpendingTotals;
    }

    public Map<LocalDate, BigDecimal> getDailyIncomeTotals() {
        if (dailyIncomeTotals == null) {
            dailyIncomeTotals = calculateDailyTotals(
                    transactions.stream()
                            .filter(t -> t.getAmount().signum() > 0)
                            .toList()
            );
        }
        return dailyIncomeTotals;
    }

    public List<CategorySpending> getCategorySpending() {
        if (categorySpending == null) {
            categorySpending = calculateCategorySpending();
        }
        return categorySpending;
    }

    public List<CategoryIncome> getCategoryIncome() {
        if (categoryIncome == null) {
            categoryIncome = calculateCategoryIncome();
        }
        return categoryIncome;
    }

    private Map<LocalDate, BigDecimal> calculateDailyTotals(List<Transaction> transactionList) {
        return transactionList.stream()
                .collect(Collectors.groupingBy(
                        t -> dateConverter.toLocalDate(t.getConfirmedAt()),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    private List<CategorySpending> calculateCategorySpending() {
        return transactions.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .collect(Collectors.groupingBy(
                        t -> t.getOffer().getCategory(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .map(entry ->
                        new CategorySpending(entry.getKey().toString(), entry.getValue().abs()))
                .sorted(Comparator.comparing(CategorySpending::spending).reversed())
                .toList();
    }

    private List<CategoryIncome> calculateCategoryIncome() {
        return transactions.stream()
                .filter(t -> t.getAmount().signum() > 0)
                .collect(Collectors.groupingBy(
                        t -> t.getOffer().getCategory(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .map(entry ->
                        new CategoryIncome(entry.getKey().toString(), entry.getValue()))
                .sorted(Comparator.comparing(CategoryIncome::income).reversed())
                .toList();
    }
}
