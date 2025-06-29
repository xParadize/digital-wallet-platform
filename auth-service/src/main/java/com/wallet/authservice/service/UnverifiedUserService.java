package com.wallet.authservice.service;

import com.wallet.authservice.dto.SignUpRequest;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.kafka.AuthKafkaProducer;
import com.wallet.authservice.mapper.UnverifiedUserMapper;
import com.wallet.authservice.repository.UnverifiedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnverifiedUserService {
    private final UnverifiedUserRepository unverifiedUserRepository;
    private final UnverifiedUserMapper unverifiedUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthKafkaProducer authKafkaProducer;

    @Transactional
    public void saveUnverifiedUser(SignUpRequest signUpRequest) {
        UnverifiedUser unverifiedUser = unverifiedUserMapper.toEntity(signUpRequest);
        unverifiedUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        unverifiedUserRepository.save(unverifiedUser);
        authKafkaProducer.sendEmailConfirmation(unverifiedUser);
    }
}
