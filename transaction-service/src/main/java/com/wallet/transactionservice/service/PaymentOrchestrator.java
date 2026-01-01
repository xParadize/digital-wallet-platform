package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.*;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.exception.PaymentFailedException;
import com.wallet.transactionservice.feign.CardFeignClient;
import com.wallet.transactionservice.mapper.PaymentOfferMapper;
import com.wallet.transactionservice.util.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final OtpService otpService;

    public PaymentResult processPayment(UUID userId, String offerId, PaymentRequestDto paymentRequest) {
        PaymentOffer paymentOffer = cacheService.getPaymentOfferById(offerId);
        CardInfoDto cardInfo = cardFeignClient.getCardByNumber(paymentRequest.getCardNumber());
        paymentValidator.validatePayment(paymentRequest, cardInfo, userId, paymentOffer);

        PaymentOfferEntity paymentOfferEntity = paymentOfferMapper.toEntity(paymentOffer);
        PaymentOfferEntity savedOffer = paymentOfferEntityService.save(paymentOfferEntity);

        Transaction transaction = transactionService.createTransaction(userId, savedOffer, paymentRequest.getCardNumber());

        if (shouldRequireOtpVerification(cardInfo.getLimit(), paymentOffer)) {
            return handleOtpVerification(userId, paymentOffer);
        }

        executeFinancialTransaction(transaction, paymentOffer.amount().value());

        return PaymentResult.success();
    }

    public void executeFinancialTransaction(Transaction transaction, BigDecimal amount) {
        try {
            cardFeignClient.createPayment(
                    transaction.getCardNumber(),
                    transaction.getUserId(),
                    amount
            );
        } catch (Exception e) {
            transactionService.failTransaction(transaction.getId());
            throw new PaymentFailedException("Payment failed", e);
        }

        try {
            transactionService.finishTransaction(transaction.getId());
        } catch (Exception e) {
            // todo: возврат средств
            throw e;
        }
    }

    private boolean shouldRequireOtpVerification(LimitDto limitDto, PaymentOffer paymentOffer) {
        return limitDto.getLimitAmount() != null && paymentOffer.amount().value().compareTo(limitDto.getLimitAmount()) > 0;
    }

    private PaymentResult handleOtpVerification(UUID userId, PaymentOffer paymentOffer) {
        otpService.initiateOtp(userId, String.valueOf(paymentOffer.id()));
        String continuePaymentLink = String.format(
                "http://localhost:8100/api/v1/otp/verify?userId=%s&offerId=%s",
                userId, paymentOffer.id()
        );
        String message = "You have exceeded the allowed payment limit. Please complete the OTP verification by following this link: " + continuePaymentLink;
        return PaymentResult.requiresOtp(message);
    }

}
