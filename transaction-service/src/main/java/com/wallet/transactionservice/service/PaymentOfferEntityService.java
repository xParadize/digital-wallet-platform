package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.mapper.PaymentOfferMapper;
import com.wallet.transactionservice.repository.PaymentOfferEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentOfferEntityService {
    private final PaymentOfferEntityRepository paymentOfferEntityRepository;
    private final CacheService cacheService;
    private final PaymentOfferMapper paymentOfferMapper;

    @Transactional
    public PaymentOfferEntity save(PaymentOfferEntity entity) {
        return paymentOfferEntityRepository.save(entity);
    }

    public void returnOffer(PaymentOfferEntity paymentOfferEntity) {
        PaymentOffer offer = paymentOfferMapper.toDto(paymentOfferEntity);
        cacheService.returnOffer(offer);
    }
}
