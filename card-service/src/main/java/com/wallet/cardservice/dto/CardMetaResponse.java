package com.wallet.cardservice.dto;

public record CardMetaResponse(
    String Status,
    String Scheme,
    String Type,
    String Issuer,
    String CardTier,
    Country Country,
    boolean Luhn
) {

}