package com.wallet.userservice.service;

import com.wallet.userservice.entity.UnverifiedUser;
import com.wallet.userservice.mapper.UserMapper;
import com.wallet.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional
    public void saveUser(UnverifiedUser unverifiedUser) {
        userRepository.save(userMapper.toEntity(unverifiedUser));
    }

    public boolean existsByEmailOrPhone(String email, String phone) {
        return userRepository.existsByEmailOrPhone(email, phone);
    }
}
