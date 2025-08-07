package com.wallet.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class ValidationErrorResponse {
    private boolean success;
    private String message;
    private List<InputFieldError> errors;
    private Instant timestamp;
}