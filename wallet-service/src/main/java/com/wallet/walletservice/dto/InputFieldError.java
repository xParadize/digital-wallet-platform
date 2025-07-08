package com.wallet.walletservice.dto;

import java.util.List;

public record InputFieldError(
        String field,
        List<String> errors
) {
}