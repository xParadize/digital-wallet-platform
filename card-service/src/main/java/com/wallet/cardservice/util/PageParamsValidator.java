package com.wallet.cardservice.util;

import com.wallet.cardservice.dto.PageParams;
import com.wallet.cardservice.exception.PageParamsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageParamsValidator {
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MIN_OFFSET = 0;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;

    public PageParams validatePageOffsetAndLimit(Integer offset, Integer limit) {
        int validatedOffset = validateOffset(offset);
        int validatedLimit = validateLimit(limit);

        return new PageParams(validatedOffset, validatedLimit);
    }

    private int validateOffset(Integer offset) {
        if (offset == null) {
            return DEFAULT_OFFSET;
        }

        if (offset < MIN_OFFSET) {
            throw new PageParamsException("Offset must be non-negative");
        }

        return offset;
    }

    private int validateLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        if (limit < MIN_LIMIT) {
            throw new PageParamsException("Limit must be at least " + MIN_LIMIT);
        }

        if (limit > MAX_LIMIT) {
            throw new PageParamsException("Limit must not exceed " + MAX_LIMIT);
        }

        return limit;
    }
}