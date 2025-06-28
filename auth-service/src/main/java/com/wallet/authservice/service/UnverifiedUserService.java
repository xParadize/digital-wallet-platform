package com.wallet.authservice.service;

import com.wallet.authservice.dto.SignUpRequest;
import com.wallet.authservice.mapper.UnverifiedUserMapper;
import com.wallet.authservice.repository.UnverifiedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnverifiedUserService {
    private final UnverifiedUserRepository unverifiedUserRepository;
    private final UnverifiedUserMapper unverifiedUserMapper;

    @Transactional
    public void saveUnverifiedUser(SignUpRequest signUpRequest) {
        unverifiedUserRepository.save(unverifiedUserMapper.toEntity(signUpRequest));
    }
}
