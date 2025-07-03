package com.wallet.authservice.service;

import com.wallet.authservice.entity.UserPrototype;
import com.wallet.authservice.exception.IncorrectPasswordException;
import com.wallet.authservice.exception.UserPrototypeNotFoundException;
import com.wallet.authservice.kafka.AuthKafkaProducer;
import com.wallet.authservice.repository.UserPrototypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPrototypeService {
    private final UserPrototypeRepository userPrototypeRepository;
    private final PasswordService passwordService;
    private final AuthKafkaProducer authKafkaProducer;

    @Transactional
    public void saveUserPrototype(UserPrototype userPrototype) {
        userPrototypeRepository.save(userPrototype);
    }

    public boolean existsByEmail(String email) {
        return userPrototypeRepository.existsByEmail(email);
    }

    public String findPasswordByEmail(String email) {
        Optional<String> optPassword = userPrototypeRepository.getPasswordByEmail(email);
        return optPassword.orElseThrow(IncorrectPasswordException::new);
    }

    public UUID findIdByEmail(String email) {
        Optional<UUID> optUserId = userPrototypeRepository.findIdByEmail(email);
        return optUserId.orElseThrow(() -> new UserPrototypeNotFoundException("User not found."));
    }

    public UserPrototype findUserPrototypeById(UUID userId) {
        Optional<UserPrototype> optUserPrototype = userPrototypeRepository.findById(userId);
        return optUserPrototype.orElseThrow(() -> new UserPrototypeNotFoundException("User not found."));
    }

    @Transactional(readOnly = true)
    public UserDetailsService userDetailsService() {
        return username -> {
            UUID userId = UUID.fromString(username);
            return userPrototypeRepository.findById(userId)
                    .orElseThrow(() -> new UserPrototypeNotFoundException("User not found."));
        };
    }

    @Transactional
    public void changePassword(String newPassword, String email) {
        userPrototypeRepository.changePassword(passwordService.encode(newPassword), email);
        authKafkaProducer.sendPasswordChanged(email, findIdByEmail(email));
    }
}
