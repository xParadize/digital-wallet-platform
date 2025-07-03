package com.wallet.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public boolean matchesPassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }
}

