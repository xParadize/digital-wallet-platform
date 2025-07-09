package com.wallet.cardservice.dto;

import com.wallet.cardservice.enums.CardType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardPreviewDto {
    private String maskedCardNumber;
    private String issuer;
    private String scheme;
    private CardType cardType;
    private boolean isFrozen;
    private boolean isBlocked;
    private BigDecimal balance;
}

