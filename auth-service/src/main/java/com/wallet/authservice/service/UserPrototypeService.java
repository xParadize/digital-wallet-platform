package com.wallet.authservice.service;

import com.wallet.authservice.entity.UserPrototype;
import com.wallet.authservice.repository.UserPrototypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPrototypeService {
    private final UserPrototypeRepository userPrototypeRepository;

    @Transactional
    public void saveUserPrototype(UserPrototype userPrototype) {
        userPrototypeRepository.save(userPrototype);
    }
}
