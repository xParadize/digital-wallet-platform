package com.wallet.walletservice.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.math.BigDecimal;

@Getter
@Setter
public class AddCardDto {
    @NotEmpty(message = "Field 'number' shouldn't be empty")
    @CreditCardNumber(message = "Invalid card number")
    private String number;

    @NotEmpty(message = "Field 'expirationDate' shouldn't be empty")
    @Pattern(
            regexp = "^(0[1-9]|1[0-2])/\\d{2}$",
            message = "Expiration date must be in format MM/YY"
    )
    @Size(min = 5, max = 5, message = "Expiration date must be exactly 5 characters long")
    private String expirationDate;


    @NotEmpty(message = "Field 'cvv' shouldn't be empty")
    @Pattern(
            regexp = "^\\d{3}$",
            message = "CVV must be exactly 3 digits"
    )
    private String cvv;

    @NotNull(message = "Field 'money' shouldn't be null")
    @PositiveOrZero(message = "Balance must be 0 or more")
    private BigDecimal money;
}
