package com.wallet.authservice.repository;

import com.wallet.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(UUID userId);
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query(value = "UPDATE refresh_token SET token = ?2 WHERE token = ?1", nativeQuery = true)
    void changeRefreshToken(String oldToken, String newToken);
}
