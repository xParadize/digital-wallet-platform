package com.wallet.authservice.service;

import com.wallet.authservice.dto.JwtAuthenticationResponse;
import com.wallet.authservice.dto.SignInRequest;
import com.wallet.authservice.entity.RefreshToken;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.ConfirmationTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UnverifiedUserService unverifiedUserService;
    private final UserPrototypeService userPrototypeService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Transactional
    public UnverifiedUser confirmEmailToken(String code) {
        UnverifiedUser unverifiedUser = unverifiedUserService.getUnverifiedUserById(UUID.fromString(code))
                .orElseThrow(ConfirmationTokenException::new);
        return unverifiedUser;
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        UUID userId = userPrototypeService.findIdByEmail(request.getEmail());

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userId.toString(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.getJwtAccessToken(userDetails);

        RefreshToken userRefreshToken = refreshTokenService.findRefreshTokenByUserId(userId);

        return JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(userRefreshToken.getToken())
                .build();
    }
}
