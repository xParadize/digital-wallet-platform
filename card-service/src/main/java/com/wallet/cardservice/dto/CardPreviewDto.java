package com.wallet.cardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardPreviewDto {
    private String number;
    private String issuer;
    private String scheme;
    private BigDecimal balance;
}

