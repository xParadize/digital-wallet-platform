package com.wallet.authservice.repository;

import com.wallet.authservice.entity.UnverifiedUser;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnverifiedUserRepository extends KeyValueRepository<UnverifiedUser, String> {
}