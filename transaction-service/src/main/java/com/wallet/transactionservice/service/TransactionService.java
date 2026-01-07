package com.wallet.transactionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.dto.TransactionDto;
import com.wallet.transactionservice.dto.TransactionEvent;
import com.wallet.transactionservice.entity.OutboxEvent;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.enums.CardType;
import com.wallet.transactionservice.enums.TransactionEventType;
import com.wallet.transactionservice.enums.TransactionStatus;
import com.wallet.transactionservice.exception.TransactionNotFoundException;
import com.wallet.transactionservice.mapper.TransactionMapper;
import com.wallet.transactionservice.repository.OutboxRepository;
import com.wallet.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final PaymentOfferEntityService paymentOfferEntityService;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    private final FeeService feeService;

    @Transactional(readOnly = true)
    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    @Transactional
    public Transaction createTransaction(UUID userId, PaymentOfferEntity paymentOfferEntity, String cardNumber) {
        BigDecimal amount = paymentOfferEntity.getAmount();
        BigDecimal finalAmount = feeService.applyTransferFee(amount);
        BigDecimal fee = finalAmount.subtract(amount).abs();
        BigDecimal signedAmount = paymentOfferEntity.getCategory().applySign(finalAmount);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .offer(paymentOfferEntity)
                .status(TransactionStatus.PENDING)
                .cardType(CardType.DEBIT)
                .cardNumber(cardNumber)
                .fee(fee)
                .amount(signedAmount)
                .createdAt(Instant.now())
                .build();
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void finishTransactionWithOtp(UUID userId, String offerId) {
        Transaction pendingTransaction = transactionRepository.findByUserIdAndOfferIdAndStatus(
                        userId, offerId, TransactionStatus.PENDING)
                .orElseThrow(() -> new TransactionNotFoundException("Pending transaction not found"));
        finishTransaction(pendingTransaction.getId());
    }

    @Transactional
    public Transaction finishTransaction(UUID transactionId) {
        Transaction transaction = getTransaction(transactionId);
        PaymentOfferEntity paymentOfferEntity = transaction.getOffer();

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setConfirmedAt(Instant.now());
        paymentOfferEntity.setCompletedAt(Instant.now());

        paymentOfferEntityService.save(paymentOfferEntity);

        Transaction successfulTransaction = transactionRepository.save(transaction);

        try {
            TransactionEvent transactionEvent = transactionMapper.toEvent(successfulTransaction);
            String eventPayload = objectMapper.writeValueAsString(transactionEvent);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(TransactionEventType.TRANSACTION_SUCCESSFUL.toString())
                    .payload(eventPayload)
                    .createdAt(Instant.now())
                    .build();
            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transaction successful event", e);
        }

        return successfulTransaction;
    }

    @Transactional
    public void cancelTransaction(UUID transactionId) {
        Transaction transaction = getTransaction(transactionId);
        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setCancelledAt(Instant.now());
        transactionRepository.save(transaction);

        paymentOfferEntityService.returnOffer(transaction.getOffer());

        try {
            TransactionEvent cancelledTransaction = transactionMapper.toEvent(transaction);
            String eventPayload = objectMapper.writeValueAsString(cancelledTransaction);
            OutboxEvent event = OutboxEvent.builder()
                    .eventType(TransactionEventType.TRANSACTION_CANCELLED.toString())
                    .payload(eventPayload)
                    .createdAt(Instant.now())
                    .build();
            outboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transaction cancelled event", e);
        }
    }

    @Transactional
    public void failTransaction(UUID transactionId) {
        Transaction transaction = getTransaction(transactionId);
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);

        paymentOfferEntityService.returnOffer(transaction.getOffer());

        try {
            TransactionEvent failedTransaction = transactionMapper.toEvent(transaction);
            String eventPayload = objectMapper.writeValueAsString(failedTransaction);
            OutboxEvent event = OutboxEvent.builder()
                    .eventType(TransactionEventType.TRANSACTION_FAILED.toString())
                    .payload(eventPayload)
                    .createdAt(Instant.now())
                    .build();
            outboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transaction failed event", e);
        }
    }

//    public void validateUserCardAccessWithDate(String cardNumber, UUID userId,  LocalDate from, LocalDate to) {
//        localDateValidator.validate(cardNumber, from, to);
//        CardDetailsDto cardDetailsDto = cardFeignClient.getCardByNumber(cardNumber).getBody();
//        paymentValidator.validateCardOwnership(cardDetailsDto, userId);
//    }
//
//    public PeriodGroupedTransactionsDto getTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
//        Instant start = dateConverter.toStartOfDayInstant(from);
//        Instant end = dateConverter.toEndOfDayInstant(to);
//
//        List<Transaction> allTransactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end);
//
//        if (allTransactions.isEmpty()) {
//            return new PeriodGroupedTransactionsDto(BigDecimal.ZERO, BigDecimal.ZERO, Collections.emptyList());
//        }
//
//        List<Transaction> paginatedTransactions = findTransactionsByCardInPeriod(cardNumber, start, end, page);
//
//        TransactionAggregator transactionAggregator = new TransactionAggregator(allTransactions, dateConverter);
//
//        return new PeriodGroupedTransactionsDto(
//                transactionAggregator.getTotalSpending(),
//                transactionAggregator.getTotalIncome(),
//                buildDailyTransactions(paginatedTransactions, transactionAggregator.getDailyTotals())
//        );
//    }
//
//    public PeriodGroupedExpenseDto getExpenseTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
//        Instant start = dateConverter.toStartOfDayInstant(from);
//        Instant end = dateConverter.toEndOfDayInstant(to);
//
//        List<Transaction> allTransactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end);
//
//        if (allTransactions.isEmpty()) {
//            return new PeriodGroupedExpenseDto(BigDecimal.ZERO, null, Collections.emptyList(), Collections.emptyList());
//        }
//
//        List<Transaction> paginatedExpenses = findExpenseTransactionsByCardInPeriod(cardNumber, start, end, page);
//
//        TransactionAggregator transactionAggregator = new TransactionAggregator(allTransactions, dateConverter);
//
//        String reportLink = analyticsFeignClient.analyzeExpenses(new CategorySpendingReportRequest(transactionAggregator.getCategorySpending(), cardNumber, from, to)).getBody();
//
//        return new PeriodGroupedExpenseDto(
//                transactionAggregator.getTotalSpending(),
//                reportLink,
//                transactionAggregator.getCategorySpending(),
//                buildDailyTransactions(paginatedExpenses, transactionAggregator.getDailySpendingTotals())
//        );
//    }
//
//    public PeriodGroupedIncomeDto getIncomeTransactionsByPeriod(String cardNumber, LocalDate from, LocalDate to, int page) {
//        Instant start = dateConverter.toStartOfDayInstant(from);
//        Instant end = dateConverter.toEndOfDayInstant(to);
//
//        List<Transaction> allTransactions = transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end);
//
//        if (allTransactions.isEmpty()) {
//            return new PeriodGroupedIncomeDto(BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
//        }
//
//        List<Transaction> paginatedIncome = findIncomeTransactionsByCardInPeriod(cardNumber, start, end, page);
//
//        TransactionAggregator transactionAggregator = new TransactionAggregator(allTransactions, dateConverter);
//
//        return new PeriodGroupedIncomeDto(
//                transactionAggregator.getTotalIncome(),
//                transactionAggregator.getCategoryIncome(),
//                buildDailyTransactions(paginatedIncome, transactionAggregator.getDailyIncomeTotals())
//        );
//    }
//
//    private List<DailyTransactionDto> buildDailyTransactions(List<Transaction> transactions, Map<LocalDate, BigDecimal> dailyTotals) {
//        return transactions.stream()
//                .collect(Collectors.groupingBy(
//                        t -> dateConverter.toLocalDate(t.getConfirmedAt())))
//                .entrySet().stream()
//                .map(entry -> new DailyTransactionDto(
//                        entry.getKey(),
//                        dailyTotals.getOrDefault(entry.getKey(), BigDecimal.ZERO),
//                        mapToTransactionDtos(entry.getValue())
//                ))
//                .sorted(Comparator.comparing(DailyTransactionDto::date))
//                .toList();
//    }
//
//    private List<TransactionDto> mapToTransactionDtos(List<Transaction> transactions) {
//        return transactions.stream()
//                .map(t -> new TransactionDto(
//                        t.getOffer().getVendor(),
//                        t.getOffer().getCategory(),
//                        t.getAmount(),
//                        t.getCardNumber(),
//                        t.getConfirmedAt()
//                ))
//                .toList();
//    }
//
//    private List<Transaction> findTransactionsByCardInPeriod(String cardNumber, Instant start, Instant end, int page) {
//        Pageable pageable = PageRequest.of(page, transactionsPerPage);
//        return transactionRepository.findAllByCardNumberAndConfirmedAtBetween(cardNumber, start, end, pageable);
//    }
//
//    private List<Transaction> findExpenseTransactionsByCardInPeriod(String cardNumber, Instant start, Instant end, int page) {
//        Pageable pageable = PageRequest.of(page, transactionsPerPage);
//        return transactionRepository.findAllByCardNumberAndConfirmedAtBetweenAndAmountLessThan(cardNumber, start, end, BigDecimal.ZERO, pageable);
//    }
//
//    private List<Transaction> findIncomeTransactionsByCardInPeriod(String cardNumber, Instant start, Instant end, int page) {
//        Pageable pageable = PageRequest.of(page, transactionsPerPage);
//        return transactionRepository.findAllByCardNumberAndConfirmedAtBetweenAndAmountGreaterThan(cardNumber, start, end, BigDecimal.ZERO, pageable);
//    }

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