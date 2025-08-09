package com.wallet.analyticsservice.dto;

import java.util.UUID;

public record Holder(
        String name,
        String lastname,
        UUID id
) {
}
