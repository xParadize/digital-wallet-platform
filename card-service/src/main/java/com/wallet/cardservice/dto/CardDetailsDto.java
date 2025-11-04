package com.wallet.cardservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDetailsDto {
    private BigDecimal balance;
    private String issuer;
    private String scheme;
    private String cardType;
    private Holder holder;
    private CardSecretDetails secretDetails;
    private boolean frozen;
    private boolean blocked;
    private List<TransactionDto> recentTransactions;
    private CardLimitDto limit;
}
