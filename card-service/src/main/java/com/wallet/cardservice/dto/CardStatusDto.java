package com.wallet.cardservice.dto;

public record CardStatusDto(
        boolean isFrozen,
        boolean isBlocked,
        boolean isExpired) {
}