package com.wallet.authservice.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken;
}