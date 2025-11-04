package com.wallet.walletservice.dto;

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
    private String cardType;
    private Holder holder;
    private CardSecretDetails secretDetails;
    private boolean frozen;
    private boolean blocked;
    private List<TransactionDto> recentTransactions;
    private Limit limit;
}
