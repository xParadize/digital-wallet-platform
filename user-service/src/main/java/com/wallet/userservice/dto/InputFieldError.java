package com.wallet.userservice.dto;

import java.util.List;

public record InputFieldError(
        String field,
        List<String> errors
) {
}
