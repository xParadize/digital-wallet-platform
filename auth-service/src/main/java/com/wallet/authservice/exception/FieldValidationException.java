package com.wallet.authservice.exception;

import com.wallet.authservice.dto.InputFieldError;
import lombok.Getter;

import java.util.List;

@Getter
public class FieldValidationException extends RuntimeException {
    private final List<InputFieldError> errors;

    public FieldValidationException(String message, List<InputFieldError> errors) {
        super(message);
        this.errors = errors;
    }
}