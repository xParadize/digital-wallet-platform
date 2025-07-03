package com.wallet.authservice.service;

import com.wallet.authservice.dto.SignUpRequest;
import com.wallet.authservice.entity.RefreshToken;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.entity.UserPrototype;
import com.wallet.authservice.enums.Role;
import com.wallet.authservice.feign.UnverifiedUserClient;
import com.wallet.authservice.kafka.AuthKafkaProducer;
import com.wallet.authservice.mapper.UnverifiedUserMapper;
import com.wallet.authservice.repository.UnverifiedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnverifiedUserService {
    private final UnverifiedUserRepository unverifiedUserRepository;
    private final UnverifiedUserMapper unverifiedUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthKafkaProducer authKafkaProducer;
    private final UnverifiedUserClient unverifiedUserClient;
    private final JwtService jwtService;
    private final UserRoleService userRoleService;
    private final UserPrototypeService userPrototypeService;
    private final RefreshTokenService refreshTokenService;

    public boolean existsByEmailOrPhone(String email, String phone) {
        return unverifiedUserClient.existsByEmailOrPhone(email, phone);
    }

    @Transactional
    public void saveUnverifiedUser(SignUpRequest signUpRequest) {
        UnverifiedUser unverifiedUser = unverifiedUserMapper.toEntity(signUpRequest);
        unverifiedUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        unverifiedUserRepository.save(unverifiedUser);
        authKafkaProducer.sendEmailConfirmation(unverifiedUser);
    }

    @Transactional
    public Optional<UnverifiedUser> getUnverifiedUserById(UUID id) {
        return unverifiedUserRepository.findById(String.valueOf(id));
    }

    @Transactional
    public void deleteUnverifiedUserById(UUID id) {
        unverifiedUserRepository.deleteById(String.valueOf(id));
    }

    @Transactional
    public String enableUser(UnverifiedUser unverifiedUser) {
        saveUser(unverifiedUser);
        deleteUnverifiedUserById(unverifiedUser.getId());
        UserPrototype userPrototype = createUserPrototype(unverifiedUser);
        generateTokensAndSave(userPrototype);
        notifyEmailConfirmed(unverifiedUser);
        return jwtService.getJwtAccessToken(userPrototype);
    }

    private UserPrototype createUserPrototype(UnverifiedUser unverifiedUser) {
        UserPrototype userPrototype = unverifiedUserMapper.toEntity(unverifiedUser);
        userPrototype.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_VERIFIED_EMAIL));
        userPrototypeService.saveUserPrototype(userPrototype);

        userRoleService.saveUserRole(userPrototype, Role.ROLE_USER);
        userRoleService.saveUserRole(userPrototype, Role.ROLE_VERIFIED_EMAIL);

        return userPrototype;
    }

    @Transactional
    public void generateTokensAndSave(UserPrototype userPrototype) {
        String accessToken = jwtService.getJwtAccessToken(userPrototype);
        String refreshToken = jwtService.getJwtRefreshToken(accessToken);

        RefreshToken refreshTokenToSave = RefreshToken.builder()
                .userId(userPrototype.getId())
                .token(refreshToken)
                .build();

        refreshTokenService.saveRefreshToken(refreshTokenToSave);
    }

    private void notifyEmailConfirmed(UnverifiedUser unverifiedUser) {
        authKafkaProducer.sendEmailConfirmed(unverifiedUser.getEmail(), unverifiedUser.getId());
    }

    private void saveUser(UnverifiedUser unverifiedUser) {
        unverifiedUserClient.saveUser(unverifiedUser);
    }
}
