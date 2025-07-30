package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.*;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.enums.CardType;
import com.wallet.transactionservice.enums.Currency;
import com.wallet.transactionservice.enums.TransactionCategory;
import com.wallet.transactionservice.enums.TransactionStatus;
import com.wallet.transactionservice.exception.TransactionNotFoundException;
import com.wallet.transactionservice.feign.TransactionClient;
import com.wallet.transactionservice.mapper.PaymentOfferMapper;
import com.wallet.transactionservice.repository.TransactionRepository;
import com.wallet.transactionservice.util.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final CacheService cacheService;
    private final TransactionRepository transactionRepository;
    private final TransactionClient transactionClient;
    private final PaymentValidator paymentValidator;
    private final OtpService otpService;
    private final PaymentOfferMapper paymentOfferMapper;
    private final PaymentOfferEntityService paymentOfferEntityService;

    @Transactional
    public PaymentResult processPayment(UUID userId, String offerId, PaymentRequestDto paymentRequest) {
        PaymentOffer paymentOffer = getPaymentById(offerId);
        CardDetailsDto cardDetailsDto = transactionClient.getLinkedCard(paymentRequest.getCardNumber(), userId).getBody();

        paymentValidator.validatePayment(paymentRequest, cardDetailsDto, userId, paymentOffer);

        PaymentOfferEntity paymentOfferEntity = paymentOfferMapper.toEntity(paymentOffer);
        PaymentOfferEntity savedPaymentOfferEntity = paymentOfferEntityService.save(paymentOfferEntity);

        UUID transactionId = initTransaction(
                userId,
                savedPaymentOfferEntity.getId(),
                paymentOffer.category(),
                paymentOffer.amount(),
                cardDetailsDto.getSecretDetails().number(),
                cardDetailsDto.getCardType()
        );

        if (shouldRequireOtpVerification(cardDetailsDto, paymentOffer)) {
            return handleOtpVerification(userId, paymentOffer);
        }

        finishTransactionById(transactionId);
        return PaymentResult.success();
    }

    private boolean shouldRequireOtpVerification(CardDetailsDto cardDetails, PaymentOffer paymentOffer) {
        return cardDetails.getLimit() != null
                && cardDetails.getLimit().limitEnabled()
                && paymentOffer.amount().value().compareTo(cardDetails.getLimit().perTransactionLimit()) > 0;
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

    public PaymentOffer getPaymentById(String key) {
        return cacheService.getPaymentOfferById(key);
    }

    @Transactional
    public UUID initTransaction(UUID userId, String paymentOfferEntityId, String category, Amount amount, String cardNumber, CardType cardType) {
        // Получаем managed экземпляр PaymentOfferEntity из базы
        PaymentOfferEntity paymentOfferEntity = paymentOfferEntityService.findPaymentOfferEntityById(paymentOfferEntityId);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .offer(paymentOfferEntity)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.valueOf(category.toUpperCase()))
                .amount(amount.value())
                .currency(Currency.valueOf(amount.currency().toUpperCase()))
                .cardNumber(cardNumber)
                .cardType(cardType)
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction.getId();
    }

    @Transactional
    public void finishTransactionById(UUID transactionId) {
        Transaction transaction = getTransactionById(transactionId);

        // Получаем свежий managed экземпляр PaymentOfferEntity
        String offerId = transaction.getOffer().getId();
        PaymentOfferEntity paymentOfferEntity = paymentOfferEntityService.findPaymentOfferEntityById(offerId);

        finishTransactionInternal(transaction, paymentOfferEntity);
    }

    @Transactional
    public void finishTransactionByUserAndOffer(UUID userId, String offerId) {
        Transaction transaction = transactionRepository.findByUserIdAndOfferIdAndStatus(
                        userId, offerId, TransactionStatus.PENDING)
                .orElseThrow(() -> new TransactionNotFoundException("Pending transaction not found"));
        PaymentOfferEntity paymentOfferEntity = paymentOfferEntityService.findPaymentOfferEntityById(offerId);
        finishTransactionInternal(transaction, paymentOfferEntity);
    }

    private void finishTransactionInternal(Transaction transaction, PaymentOfferEntity paymentOfferEntity) {
        transactionClient.subtractMoney(transaction.getUserId(), transaction.getAmount(), transaction.getCardNumber());
        cacheService.removeOffer(transaction.getOffer().getId());

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setConfirmedAt(LocalDateTime.now());

        paymentOfferEntity.setCompletedAt(LocalDateTime.now());

        paymentOfferEntityService.save(paymentOfferEntity);
        transactionRepository.save(transaction);
    }

    public Transaction getTransactionById(UUID id) {
        return transactionRepository.getTransactionById(id).orElseThrow(() -> new TransactionNotFoundException("Transaction " + id + " not found"));
    }
}
