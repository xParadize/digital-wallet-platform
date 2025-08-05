package com.wallet.transactionservice.service;

import com.wallet.transactionservice.dto.*;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.enums.CardType;
import com.wallet.transactionservice.enums.TransactionCategory;
import com.wallet.transactionservice.enums.TransactionStatus;
import com.wallet.transactionservice.exception.TransactionNotFoundException;
import com.wallet.transactionservice.feign.TransactionClient;
import com.wallet.transactionservice.mapper.PaymentOfferMapper;
import com.wallet.transactionservice.repository.TransactionRepository;
import com.wallet.transactionservice.util.DateConverter;
import com.wallet.transactionservice.util.LocalDateValidator;
import com.wallet.transactionservice.util.PaymentValidator;
import com.wallet.transactionservice.util.TransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    private final LocalDateValidator localDateValidator;
    private final DateConverter dateConverter;

    @Value("${transaction.per-page}")
    private int transactionsPerPage;

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
        transactionClient.subtractMoney(transaction.getUserId(), paymentOfferEntity.getAmount(), transaction.getCardNumber());
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

    public PeriodGroupedTransactionsDto getTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
        Instant start = dateConverter.toStartOfDayInstant(from);
        Instant end = dateConverter.toEndOfDayInstant(to);

        List<Transaction> transactionsByPeriod = findAllTransactionsByUserIdInPeriod(cardNumber, start, end, page);

        if (transactionsByPeriod.isEmpty()) {
            return new PeriodGroupedTransactionsDto(BigDecimal.ZERO, BigDecimal.ZERO, Collections.emptyList());
        }

        Map<LocalDate, BigDecimal> dailyTotalMap = getDailyTotal(cardNumber, start, end);

        var summary = transactionsByPeriod.stream()
                .collect(Collector.of(TransactionSummary::new,
                        (transactionSummary, transaction) -> {
                            BigDecimal amount = transaction.getAmount();
                            if (amount.signum() < 0) {
                                transactionSummary.totalSpending = transactionSummary.totalSpending.add(amount.abs());
                            } else if (amount.signum() > 0) {
                                transactionSummary.totalIncome = transactionSummary.totalIncome.add(amount);
                            }

                            LocalDate date = dateConverter.toLocalDate(transaction.getConfirmedAt());
                            transactionSummary.transactionsByDate.computeIfAbsent(date, instant -> new ArrayList<>()).add(transaction);
                        },
                        (s1, s2) -> {
                            s1.totalSpending = s1.totalSpending.add(s2.totalSpending);
                            s1.totalIncome = s1.totalIncome.add(s2.totalIncome);
                            s2.transactionsByDate.forEach((date, transactions) ->
                                    s1.transactionsByDate.merge(date, transactions, (existing, newList) -> {
                                        existing.addAll(newList);
                                        return existing;
                                    }));
                            return s1;
                        }
                ));

        List<DailyTransactionDto> dailyTransactions = summary.transactionsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> transactions = entry.getValue();

                    BigDecimal dailyTotal = dailyTotalMap.getOrDefault(date, BigDecimal.ZERO);

                    List<TransactionDto> transactionDtos = transactions.stream()
                            .map(t -> new TransactionDto(
                                    t.getOffer().getVendor(),
                                    t.getOffer().getCategory(),
                                    t.getAmount(),
                                    t.getCardNumber(),
                                    t.getConfirmedAt()
                            ))
                            .toList();

                    return new DailyTransactionDto(date, dailyTotal, transactionDtos);
                })
                .sorted(Comparator.comparing(DailyTransactionDto::date))
                .toList();

        return new PeriodGroupedTransactionsDto(
                getTotalSpending(cardNumber, start, end),
                getTotalIncome(cardNumber, start, end),
                dailyTransactions
        );
    }

    public PeriodGroupedExpenseDto getExpenseTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
        Instant start = dateConverter.toStartOfDayInstant(from);
        Instant end = dateConverter.toEndOfDayInstant(to);

        List<Transaction> transactionsByPeriod = findAllTransactionsByUserIdInPeriod(cardNumber, start, end, page);

        if (transactionsByPeriod.isEmpty()) {
            return new PeriodGroupedExpenseDto(BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
        }

        Map<LocalDate, BigDecimal> dailyTotalMap = getDailyTotalSpending(cardNumber, start, end);

        var summary = transactionsByPeriod.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .collect(Collector.of(TransactionSummary::new,
                        (transactionSummary, transaction) -> {
                            BigDecimal amount = transaction.getAmount();
                            transactionSummary.totalSpending = transactionSummary.totalSpending.add(amount.abs());

                            LocalDate date = dateConverter.toLocalDate(transaction.getConfirmedAt());
                            transactionSummary.transactionsByDate.computeIfAbsent(date, instant -> new ArrayList<>()).add(transaction);
                        },
                        (s1, s2) -> {
                            s1.totalSpending = s1.totalSpending.add(s2.totalSpending);
                            s1.totalIncome = s1.totalIncome.add(s2.totalIncome);
                            s2.transactionsByDate.forEach((date, transactions) ->
                                    s1.transactionsByDate.merge(date, transactions, (existing, newList) -> {
                                        existing.addAll(newList);
                                        return existing;
                                    }));
                            return s1;
                        }
                ));

        List<DailyTransactionDto> dailyTransactions = summary.transactionsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> transactions = entry.getValue();

                    BigDecimal dailyTotal = dailyTotalMap.getOrDefault(date, BigDecimal.ZERO);

                    List<TransactionDto> transactionDtos = transactions.stream()
                            .map(t -> new TransactionDto(
                                    t.getOffer().getVendor(),
                                    t.getOffer().getCategory(),
                                    t.getAmount(),
                                    t.getCardNumber(),
                                    t.getConfirmedAt()
                            ))
                            .toList();

                    return new DailyTransactionDto(date, dailyTotal, transactionDtos);
                })
                .sorted(Comparator.comparing(DailyTransactionDto::date))
                .toList();

        return new PeriodGroupedExpenseDto(
                getTotalSpending(cardNumber, start, end),
                calculateCategorySpending(cardNumber, start, end),
                dailyTransactions
        );
    }

    private BigDecimal getTotalSpending(String cardNumber, Instant from, Instant to) {
        List<Transaction> transactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, from, to);
        return transactions.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalIncome(String cardNumber, Instant from, Instant to) {
        List<Transaction> transactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, from, to);
        return transactions.stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<LocalDate, BigDecimal> getDailyTotal(String cardNumber, Instant from, Instant to) {
        List<Transaction> transactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, from, to);
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> dateConverter.toLocalDate(transaction.getConfirmedAt()),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    private Map<LocalDate, BigDecimal> getDailyTotalSpending(String cardNumber, Instant from, Instant to) {
        List<Transaction> transactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, from, to);
        return transactions.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .collect(Collectors.groupingBy(
                        transaction -> dateConverter.toLocalDate(transaction.getConfirmedAt()),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    private List<CategorySpending> calculateCategorySpending(String cardNumber, Instant from, Instant to) {
        List<Transaction> transactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, from, to);
        Map<TransactionCategory, BigDecimal> map = transactions.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .collect(Collectors.groupingBy(
                        t -> t.getOffer().getCategory(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
        return map.entrySet().stream()
                .map(entry -> {
                    TransactionCategory category = entry.getKey();
                    BigDecimal totalSpending = entry.getValue();
                    return new CategorySpending(category.toString(), totalSpending.abs());
                })
                .sorted(Comparator.comparing(CategorySpending::spending).reversed())
                .toList();
    }

    private List<Transaction> findAllTransactionsByUserIdInPeriod(String cardNumber, Instant start, Instant end, int page) {
        Pageable pageable = PageRequest.of(page, transactionsPerPage);
        return transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end, pageable);
    }

    public void validateUserCardAccessWithDate(String cardNumber, UUID userId,  LocalDate from, LocalDate to) {
        localDateValidator.validate(cardNumber, from, to);
        CardDetailsDto cardDetailsDto = transactionClient.getLinkedCard(cardNumber, userId).getBody();
        paymentValidator.validateCardOwnership(cardDetailsDto, userId);
    }
}