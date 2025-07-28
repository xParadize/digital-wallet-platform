package com.wallet.transactionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class OtpInitRequest {
    private UUID userId;
    private String offerId;
}
