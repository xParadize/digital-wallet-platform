package com.wallet.analyticsservice.dto;

import java.time.LocalDate;
import java.util.List;

public record CategorySpendingReportRequest(
        List<CategorySpending> categorySpendingList,
        String cardNumber,
        LocalDate from,
        LocalDate to) {
}
