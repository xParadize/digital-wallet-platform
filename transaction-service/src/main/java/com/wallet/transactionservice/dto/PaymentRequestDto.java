package com.wallet.transactionservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

@Getter
@Setter
public class PaymentRequestDto {
    @NotEmpty(message = "Field 'number' shouldn't be empty")
    @CreditCardNumber(message = "Invalid card number")
    private String cardNumber;

    @NotEmpty(message = "Field 'cvv' shouldn't be empty")
    @Pattern(
            regexp = "^\\d{3}$",
            message = "CVV must be exactly 3 digits"
    )
    private String cvv;
}
