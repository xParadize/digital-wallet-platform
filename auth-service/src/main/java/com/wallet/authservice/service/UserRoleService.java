package com.wallet.authservice.service;

import com.wallet.authservice.entity.UserPrototype;
import com.wallet.authservice.entity.UserRole;
import com.wallet.authservice.enums.Role;
import com.wallet.authservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public void saveUserRole(UserPrototype userPrototype, Role role) {
        UserRole userRole = UserRole.builder()
                .userId(userPrototype.getId())
                .role(role.name())
                .build();
        userRoleRepository.save(userRole);
    }
}