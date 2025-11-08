package com.wallet.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardInfoDto {
    private CardDto cardDto;
    private CardMetadataDto cardMetadataDto;
    private Holder holder;
    private CardDetailsDto secretDetails;
    private List<TransactionDto> recentTransactions;
    private LimitDto limit;
}
