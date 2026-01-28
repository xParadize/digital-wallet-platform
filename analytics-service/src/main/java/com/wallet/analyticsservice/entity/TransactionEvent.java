package com.wallet.analyticsservice.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private UUID id;
    private UUID userId;
    private Long offerId;
    private String status;
    private String cardNumber;
    private String cardType;
    private BigDecimal amount;
    private String transactionType;
    private Instant createdAt;
    private Instant confirmedAt;
    private Instant cancelledAt;
}