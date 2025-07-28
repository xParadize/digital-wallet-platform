package com.wallet.transactionservice.dto;

import java.util.List;

public record InputFieldError(
        String field,
        List<String> errors
) {
}