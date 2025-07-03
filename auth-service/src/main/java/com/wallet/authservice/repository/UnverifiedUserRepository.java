package com.wallet.authservice.repository;

import com.wallet.authservice.entity.UnverifiedUser;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnverifiedUserRepository extends CrudRepository<UnverifiedUser, String> {
}