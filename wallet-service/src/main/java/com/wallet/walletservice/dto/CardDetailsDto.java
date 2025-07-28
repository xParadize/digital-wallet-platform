package com.wallet.walletservice.dto;

import com.wallet.walletservice.enums.CardType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
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
    private Limit limit;
}
