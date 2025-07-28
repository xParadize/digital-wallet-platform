package com.wallet.cardservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SetCardLimitRequest {
    @DecimalMin(value = "100.00", inclusive = true, message = "The limit can't be less than 100")
    @Digits(integer = 15, fraction = 2, message = "Incorrect number format")
    @NotNull(message = "Field 'perTransactionLimit' shouldn't be empty")
    private BigDecimal perTransactionLimit;
}
