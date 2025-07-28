package com.wallet.userservice.dto;

import java.util.UUID;

public record HolderDto(
        String name,
        String lastname,
        UUID id
) {
}
