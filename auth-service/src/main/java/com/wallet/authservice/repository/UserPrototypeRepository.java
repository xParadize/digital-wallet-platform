package com.wallet.authservice.repository;

import com.wallet.authservice.entity.UserPrototype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserPrototypeRepository extends JpaRepository<UserPrototype, UUID> {
}
