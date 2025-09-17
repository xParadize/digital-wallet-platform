package com.wallet.cardservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class DateConverter {
    public long mmYyToLong(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        YearMonth ym = YearMonth.parse(date, formatter);
        return ym.getYear() * 100L + ym.getMonthValue();
    }
}