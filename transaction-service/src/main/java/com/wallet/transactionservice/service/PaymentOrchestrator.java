package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.CardInfoDto;
import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.dto.PaymentRequestDto;
import com.wallet.transactionservice.dto.PaymentResult;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.feign.CardFeignClient;
import com.wallet.transactionservice.mapper.PaymentOfferMapper;
import com.wallet.transactionservice.util.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentOrchestrator {
    private final CardFeignClient cardFeignClient;
    private final PaymentValidator paymentValidator;
    private final TransactionService transactionService;
    private final CacheService cacheService;
    private final PaymentOfferEntityService paymentOfferEntityService;
    private final PaymentOfferMapper paymentOfferMapper;

    public PaymentResult processPayment(UUID userId, String offerId, PaymentRequestDto paymentRequest) {
        PaymentOffer paymentOffer = cacheService.getPaymentOfferById(offerId);

        CardInfoDto cardInfo = cardFeignClient.getCardByNumber(paymentRequest.getCardNumber());
        paymentValidator.validatePayment(paymentRequest, cardInfo, userId, paymentOffer);

        PaymentOfferEntity paymentOfferEntity = paymentOfferMapper.toEntity(paymentOffer);
        PaymentOfferEntity savedOffer = paymentOfferEntityService.save(paymentOfferEntity);

        UUID transactionId = transactionService.createTransaction(userId, savedOffer, paymentRequest.getCardNumber());

        transactionService.finishTransactionById(transactionId);

        return PaymentResult.success();
    }
}
