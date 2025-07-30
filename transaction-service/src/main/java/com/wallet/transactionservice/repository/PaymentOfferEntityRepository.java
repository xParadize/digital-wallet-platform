package com.wallet.transactionservice.repository;

import com.wallet.transactionservice.entity.PaymentOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentOfferEntityRepository extends JpaRepository<PaymentOfferEntity, String> {
    Optional<PaymentOfferEntity> findPaymentOfferEntityById(String id);
}
