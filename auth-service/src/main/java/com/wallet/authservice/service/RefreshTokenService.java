package com.wallet.authservice.service;

import com.wallet.authservice.dto.JwtAuthenticationResponse;
import com.wallet.authservice.dto.RefreshTokenRequest;
import com.wallet.authservice.entity.RefreshToken;
import com.wallet.authservice.entity.UserPrototype;
import com.wallet.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserPrototypeService userPrototypeService;
    private final JwtService jwtService;

    @Transactional
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findRefreshTokenByUserId(UUID userId) {
        Optional<RefreshToken> token = refreshTokenRepository.findByUserId(userId);
        return token.orElse(null);
    }

    @Transactional
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken oldRefreshToken = findRefreshTokenByToken(refreshTokenRequest.getRefreshToken());
        UserPrototype userPrototype = userPrototypeService.findById(oldRefreshToken.getUserId());

        var accessToken = jwtService.getJwtAccessToken(userPrototype);
        var newRefreshToken = jwtService.getJwtRefreshToken(accessToken);

        changeRefreshToken(refreshTokenRequest.getRefreshToken(), newRefreshToken);

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
        return response;
    }

    @Transactional(readOnly = true)
    public RefreshToken findRefreshTokenByToken(String inputToken) {
        Optional<RefreshToken> token = refreshTokenRepository.findByToken(inputToken);
        return token.orElse(null);
    }

    @Transactional
    public void changeRefreshToken(String oldToken, String newToken) {
        refreshTokenRepository.changeRefreshToken(oldToken, newToken);
    }
}
