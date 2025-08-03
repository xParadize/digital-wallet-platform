package com.wallet.transactionservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

@Getter
@Setter
public class CardTransactionsRequestDto {
    @CreditCardNumber(message = "Invalid card number")
    @NotEmpty(message = "Field 'cardNumber' shouldn't be empty")
    private String cardNumber;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", message = "field 'from' should be in yyyy-MM-dd format")
    @NotEmpty(message = "Field 'from' shouldn't be empty")
    private String from;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", message = "field 'to' should be in yyyy-MM-dd format")
    @NotEmpty(message = "Field 'to' shouldn't be empty")
    private String to;
}