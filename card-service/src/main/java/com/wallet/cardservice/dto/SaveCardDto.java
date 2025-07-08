package com.wallet.cardservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class SaveCardDto {
    private String number;
    private UUID userId;
    private String expirationDate;
    private String cvv;
    private BigDecimal money;
    private String email;
}