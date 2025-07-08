package com.wallet.cardservice.dto;

public record CardInfoResponse(
    String Status,
    String Scheme,
    String Type,
    String Issuer,
    String CardTier,
    Country Country,
    boolean Luhn
) {

}