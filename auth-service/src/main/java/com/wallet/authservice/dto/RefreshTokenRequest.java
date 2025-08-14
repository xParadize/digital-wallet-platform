package com.wallet.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Size(min = 32, max = 32, message = "Refresh token must be exactly 32 characters long")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Refresh token contains invalid characters"
    )
    private String refreshToken;
}