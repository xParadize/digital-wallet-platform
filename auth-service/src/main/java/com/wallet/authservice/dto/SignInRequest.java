package com.wallet.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
    @Size(min = 5, max = 254, message = "Email should contain from 5 to 254 symbols")
    @NotEmpty(message = "Field 'email' shouldn't be empty")
    @Email(message = "Invalid email")
    private String email;

    @NotEmpty(message = "Field 'password' shouldn't be empty")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}<>])[A-Za-z\\d!@#$%^&*()\\-_=+\\[\\]{}<>]{8,}$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 digit, 1 special symbol, and be at least 8 characters long"
    )
    @Size(min = 8, message = "Password can't be less than 2 symbols")
    private String password;
}
