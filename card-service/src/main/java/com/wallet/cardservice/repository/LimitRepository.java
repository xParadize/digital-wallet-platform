package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Long> {
}
