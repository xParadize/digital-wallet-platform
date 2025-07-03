package com.wallet.authservice.service;

import com.wallet.authservice.entity.RefreshToken;
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

    @Transactional
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findRefreshTokenByUserId(UUID userId) {
        Optional<RefreshToken> token = refreshTokenRepository.findByUserId(userId);
        return token.orElse(null);
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
