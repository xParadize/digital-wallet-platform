package com.wallet.cardservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CachedCardInfo(
        UUID userId,
        CardDto cardDto,
        CardMetadataDto cardMetadataDto,
        CardDetailsDto secretDetails,
        LimitDto limit
) {
}
