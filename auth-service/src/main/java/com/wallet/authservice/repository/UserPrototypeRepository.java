package com.wallet.authservice.repository;

import com.wallet.authservice.entity.UserPrototype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPrototypeRepository extends JpaRepository<UserPrototype, UUID> {
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM UserPrototype u WHERE u.email = :email")
    Optional<UUID> findIdByEmail(@Param("email") String email);

    @Query("SELECT u.password FROM UserPrototype u WHERE u.email = :email")
    Optional<String> getPasswordByEmail(@Param("email") String email);

    @Modifying
    @Query(value = "UPDATE user_prototype SET password = ?1 WHERE email = ?2", nativeQuery = true)
    void changePassword(String newPassword, String email);
}
