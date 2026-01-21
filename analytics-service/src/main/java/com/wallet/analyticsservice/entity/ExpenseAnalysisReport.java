package com.wallet.analyticsservice.entity;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseAnalysisReport {
    private UUID id;
    private String cardNumber;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String report;
    private Instant createdAt;
    private Instant requestedAt;
}
