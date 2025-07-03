package com.wallet.authservice.service;

import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.ConfirmationTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UnverifiedUserService unverifiedUserService;

    @Transactional
    public UnverifiedUser confirmEmailToken(String code) {
        UnverifiedUser unverifiedUser = unverifiedUserService.getUnverifiedUserById(UUID.fromString(code))
                .orElseThrow(ConfirmationTokenException::new);
        return unverifiedUser;
    }
}
