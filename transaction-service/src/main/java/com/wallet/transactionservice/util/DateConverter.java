package com.wallet.transactionservice.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class DateConverter {
    private final ZoneId TIMESTAMP_ZONE = ZoneOffset.UTC;

    public LocalDate toLocalDate(Instant timestamp) {
        return timestamp.atZone(TIMESTAMP_ZONE).toLocalDate();
    }

    public Instant toStartOfDayInstant(LocalDate date) {
        return date.atStartOfDay(TIMESTAMP_ZONE).toInstant();
    }

    public Instant toEndOfDayInstant(LocalDate date) {
        return date.plusDays(1).atStartOfDay(TIMESTAMP_ZONE).toInstant().minusNanos(1);
    }
}
