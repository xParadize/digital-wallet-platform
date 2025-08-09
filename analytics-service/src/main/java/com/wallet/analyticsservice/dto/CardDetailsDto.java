package com.wallet.analyticsservice.dto;

import com.wallet.analyticsservice.enums.CardType;
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
    private CardType cardType;
    private Holder holder;
    private CardSecretDetails secretDetails;
    private boolean isFrozen;
    private boolean isBlocked;
    private List<RecentTransactionsDto> recentTransactions;
    private CardLimitDto limit;
}