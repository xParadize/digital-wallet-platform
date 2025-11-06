package com.wallet.cardservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
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
