package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.*;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.enums.CardType;
import com.wallet.transactionservice.enums.TransactionCategory;
import com.wallet.transactionservice.enums.TransactionStatus;
import com.wallet.transactionservice.exception.TransactionNotFoundException;
import com.wallet.transactionservice.feign.AnalyticsFeignClient;
import com.wallet.transactionservice.feign.CardFeignClient;
import com.wallet.transactionservice.mapper.PaymentOfferMapper;
import com.wallet.transactionservice.mapper.TransactionMapper;
import com.wallet.transactionservice.repository.TransactionRepository;
import com.wallet.transactionservice.util.DateConverter;
import com.wallet.transactionservice.util.LocalDateValidator;
import com.wallet.transactionservice.util.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final CacheService cacheService;
    private final TransactionRepository transactionRepository;
    private final CardFeignClient cardFeignClient;
    private final PaymentValidator paymentValidator;
    private final OtpService otpService;
    private final PaymentOfferMapper paymentOfferMapper;
    private final PaymentOfferEntityService paymentOfferEntityService;
    private final LocalDateValidator localDateValidator;
    private final DateConverter dateConverter;
    private final AnalyticsFeignClient analyticsFeignClient;
    private final TransactionMapper transactionMapper;

    @Value("${transaction.per-page}")
    private int transactionsPerPage;

    @Transactional
    public PaymentResult processPayment(UUID userId, String offerId, PaymentRequestDto paymentRequest) {
        PaymentOffer paymentOffer = getPaymentById(offerId);
        CardDetailsDto cardDetailsDto = cardFeignClient.getLinkedCard(paymentRequest.getCardNumber(), userId).getBody();

        paymentValidator.validatePayment(paymentRequest, cardDetailsDto, userId, paymentOffer);

        PaymentOfferEntity paymentOfferEntity = paymentOfferMapper.toEntity(paymentOffer);
        PaymentOfferEntity savedPaymentOfferEntity = paymentOfferEntityService.save(paymentOfferEntity);

        UUID transactionId = initTransaction(
                userId,
                savedPaymentOfferEntity.getId(),
                cardDetailsDto.getSecretDetails().number(),
                cardDetailsDto.getCardType(),
                paymentOfferEntity.getAmount(),
                paymentOfferEntity.getCategory()
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
    public UUID initTransaction(UUID userId, String paymentOfferEntityId, String cardNumber, CardType cardType, BigDecimal amount, TransactionCategory category) {
        PaymentOfferEntity paymentOfferEntity = paymentOfferEntityService.findPaymentOfferEntityById(paymentOfferEntityId);

        BigDecimal signedAmount = category.applySign(amount);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .offer(paymentOfferEntity)
                .status(TransactionStatus.PENDING)
                .cardNumber(cardNumber)
                .cardType(cardType)
                .amount(signedAmount)
                .createdAt(Instant.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction.getId();
    }

    @Transactional
    public void finishTransactionById(UUID transactionId) {
        Transaction transaction = getTransactionById(transactionId);

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
        cardFeignClient.subtractMoney(transaction.getUserId(), paymentOfferEntity.getAmount(), transaction.getCardNumber());
        cacheService.removeOffer(transaction.getOffer().getId());

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setConfirmedAt(Instant.now());

        paymentOfferEntity.setCompletedAt(Instant.now());

        paymentOfferEntityService.save(paymentOfferEntity);
        transactionRepository.save(transaction);
    }

    public Transaction getTransactionById(UUID id) {
        return transactionRepository.getTransactionById(id).orElseThrow(() -> new TransactionNotFoundException("Transaction " + id + " not found"));
    }

    public void validateUserCardAccessWithDate(String cardNumber, UUID userId,  LocalDate from, LocalDate to) {
        localDateValidator.validate(cardNumber, from, to);
        CardDetailsDto cardDetailsDto = cardFeignClient.getLinkedCard(cardNumber, userId).getBody();
        paymentValidator.validateCardOwnership(cardDetailsDto, userId);
    }

    public PeriodGroupedTransactionsDto getTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
        Instant start = dateConverter.toStartOfDayInstant(from);
        Instant end = dateConverter.toEndOfDayInstant(to);

        List<Transaction> allTransactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end);

        if (allTransactions.isEmpty()) {
            return new PeriodGroupedTransactionsDto(BigDecimal.ZERO, BigDecimal.ZERO, Collections.emptyList());
        }

        List<Transaction> paginatedTransactions = findTransactionsByCardInPeriod(cardNumber, start, end, page);

        TransactionAggregator transactionAggregator = new TransactionAggregator(allTransactions, dateConverter);

        return new PeriodGroupedTransactionsDto(
                transactionAggregator.getTotalSpending(),
                transactionAggregator.getTotalIncome(),
                buildDailyTransactions(paginatedTransactions, transactionAggregator.getDailyTotals())
        );
    }

    public PeriodGroupedExpenseDto getExpenseTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
        Instant start = dateConverter.toStartOfDayInstant(from);
        Instant end = dateConverter.toEndOfDayInstant(to);

        List<Transaction> allTransactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end);

        if (allTransactions.isEmpty()) {
            return new PeriodGroupedExpenseDto(BigDecimal.ZERO, null, Collections.emptyList(), Collections.emptyList());
        }

        List<Transaction> paginatedExpenses = findExpenseTransactionsByCardInPeriod(cardNumber, start, end, page);

        TransactionAggregator transactionAggregator = new TransactionAggregator(allTransactions, dateConverter);

        String reportLink = analyticsFeignClient.analyzeExpenses(new CategorySpendingReportRequest(transactionAggregator.getCategorySpending(), cardNumber, from, to)).getBody();

        return new PeriodGroupedExpenseDto(
                transactionAggregator.getTotalSpending(),
                reportLink,
                transactionAggregator.getCategorySpending(),
                buildDailyTransactions(paginatedExpenses, transactionAggregator.getDailySpendingTotals())
        );
    }

    public PeriodGroupedIncomeDto getIncomeTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
        Instant start = dateConverter.toStartOfDayInstant(from);
        Instant end = dateConverter.toEndOfDayInstant(to);

        List<Transaction> allTransactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end);

        if (allTransactions.isEmpty()) {
            return new PeriodGroupedIncomeDto(BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
        }

        List<Transaction> paginatedIncome = findIncomeTransactionsByCardInPeriod(cardNumber, start, end, page);

        TransactionAggregator transactionAggregator = new TransactionAggregator(allTransactions, dateConverter);

        return new PeriodGroupedIncomeDto(
                transactionAggregator.getTotalIncome(),
                transactionAggregator.getCategoryIncome(),
                buildDailyTransactions(paginatedIncome, transactionAggregator.getDailyIncomeTotals())
        );
    }

    private List<DailyTransactionDto> buildDailyTransactions(List<Transaction> transactions, Map<LocalDate, BigDecimal> dailyTotals) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> dateConverter.toLocalDate(t.getConfirmedAt())))
                .entrySet().stream()
                .map(entry -> new DailyTransactionDto(
                        entry.getKey(),
                        dailyTotals.getOrDefault(entry.getKey(), BigDecimal.ZERO),
                        mapToTransactionDtos(entry.getValue())
                ))
                .sorted(Comparator.comparing(DailyTransactionDto::date))
                .toList();
    }

    private List<TransactionDto> mapToTransactionDtos(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> new TransactionDto(
                        t.getOffer().getVendor(),
                        t.getOffer().getCategory(),
                        t.getAmount(),
                        t.getCardNumber(),
                        t.getConfirmedAt()
                ))
                .toList();
    }

    private List<Transaction> findTransactionsByCardInPeriod(String cardNumber, Instant start, Instant end, int page) {
        Pageable pageable = PageRequest.of(page, transactionsPerPage);
        return transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end, pageable);
    }

    private List<Transaction> findExpenseTransactionsByCardInPeriod(String cardNumber, Instant start, Instant end, int page) {
        Pageable pageable = PageRequest.of(page, transactionsPerPage);
        return transactionRepository.findAllByCardNumberAndConfirmedAtBetweenAndAmountLessThan(cardNumber, start, end, BigDecimal.ZERO, pageable);
    }

    private List<Transaction> findIncomeTransactionsByCardInPeriod(String cardNumber, Instant start, Instant end, int page) {
        Pageable pageable = PageRequest.of(page, transactionsPerPage);
        return transactionRepository.findAllByCardNumberAndConfirmedAtBetweenAndAmountGreaterThan(cardNumber, start, end, BigDecimal.ZERO, pageable);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getRecentTransactions(String cardNumber, int count) {
        List<Transaction> transactions = transactionRepository.findByCardNumberOrderByConfirmedAtDesc(cardNumber, Limit.of(count));
        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> lastUsedCardNumbers(UUID userId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return transactionRepository.findAllByUserIdOrderByConfirmedAtDesc(userId, pageable).stream()
                .map(Transaction::getCardNumber)
                .distinct()
                .toList();
    }
}