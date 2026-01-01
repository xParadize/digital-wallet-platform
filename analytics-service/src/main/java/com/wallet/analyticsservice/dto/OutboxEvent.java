package com.wallet.analyticsservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class OutboxEvent {
    private Long id;
    private String eventType;
    private String payload;
    private Instant createdAt;
}