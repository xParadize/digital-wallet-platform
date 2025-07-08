package com.wallet.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class SignUpRequest {

    @NotEmpty(message = "Field 'name' shouldn't be empty")
    @Pattern(
            regexp = "^[A-Z][a-z]+(?:[ .'-][a-z]+)*$",
            message = "Only Latin letters are allowed and the first letter must be uppercase"
    )
    @Size(min = 2, message = "Name can't be less than 2 symbols")
    private String name;

    @NotEmpty(message = "Field 'lastname' shouldn't be empty")
    @Pattern(
            regexp = "^[A-Z][a-z]+(?:[ .'-][a-z]+)*$",
            message = "Only Latin letters are allowed and the first letter must be uppercase"
    )
    @Size(min = 2, message = "Lastname can't be less than 2 symbols")
    private String lastname;

    @NotEmpty(message = "Field 'patronymic' shouldn't be empty")
    @Pattern(
            regexp = "^[A-Z][a-z]+(?:[ .'-][a-z]+)*$",
            message = "Only Latin letters are allowed and the first letter must be uppercase"
    )
    @Size(min = 2, message = "Lastname can't be less than 2 symbols")
    private String patronymic;

    @JsonProperty("birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Size(min = 10, max = 10, message = "Birth Date can't be more or less than 10 symbols")
    @NotEmpty(message = "Field 'birth date' shouldn't be empty")
    private String birthDate;

    @NotEmpty(message = "Field 'phone' shouldn't be empty")
    @Pattern(
            regexp = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$",
            message = "Invalid phone number"
    )
    @Size(min = 7, message = "Phone number can't be less than 7 symbols")
    private String phone;

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

    @JsonProperty("password_confirmation")
    @NotEmpty(message = "Field 'password confirmation' shouldn't be empty")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}<>])[A-Za-z\\d!@#$%^&*()\\-_=+\\[\\]{}<>]{8,}$",
            message = "Password confirmation must contain at least 1 uppercase letter, 1 lowercase letter, 1 digit, 1 special symbol, and be at least 8 characters long"
    )
    @Size(min = 8, message = "Password confirmation can't be less than 2 symbols")
    private String passwordConfirmation;
}