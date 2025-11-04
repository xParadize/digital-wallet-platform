package com.wallet.userservice.service;

import com.wallet.userservice.dto.HolderDto;
import com.wallet.userservice.entity.UnverifiedUser;
import com.wallet.userservice.entity.User;
import com.wallet.userservice.exception.UserNotFoundException;
import com.wallet.userservice.mapper.UserMapper;
import com.wallet.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    public HolderDto getCardHolder(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Unable to load holder's info"));
        return new HolderDto(user.getName(), user.getLastname(), user.getId());
    }
}
