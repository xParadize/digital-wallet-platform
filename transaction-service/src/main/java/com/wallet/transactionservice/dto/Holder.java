package com.wallet.transactionservice.dto;

import java.util.UUID;

public record Holder(
        String name,
        String lastname,
        UUID id
) {
}
