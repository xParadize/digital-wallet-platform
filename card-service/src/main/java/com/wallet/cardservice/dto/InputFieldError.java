package com.wallet.cardservice.dto;

import java.util.List;

public record InputFieldError(
        String field,
        List<String> errors
) {
}