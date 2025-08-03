package com.wallet.transactionservice.service;

import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.exception.PaymentOfferEntityNotFoundException;
import com.wallet.transactionservice.repository.PaymentOfferEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentOfferEntityService {
    private final PaymentOfferEntityRepository paymentOfferEntityRepository;

    @Transactional
    public PaymentOfferEntity save(PaymentOfferEntity entity) {
        return paymentOfferEntityRepository.save(entity);
    }

    public PaymentOfferEntity findPaymentOfferEntityById(String id) {
        return paymentOfferEntityRepository.findPaymentOfferEntityById(id).
                orElseThrow(() -> new PaymentOfferEntityNotFoundException("Payment offer " + id + " not found"));
    }
}
