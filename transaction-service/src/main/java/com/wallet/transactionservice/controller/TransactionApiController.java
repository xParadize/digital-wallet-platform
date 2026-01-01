package com.wallet.transactionservice.controller;

import com.wallet.transactionservice.dto.*;
import com.wallet.transactionservice.exception.FieldValidationException;
import com.wallet.transactionservice.exception.IncorrectSearchPath;
import com.wallet.transactionservice.exception.InvalidAuthorizationException;
import com.wallet.transactionservice.service.JwtService;
import com.wallet.transactionservice.service.PaymentOrchestrator;
import com.wallet.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionApiController {
    private final TransactionService transactionService;
    private final JwtService jwtService;
    private final PaymentOrchestrator paymentOrchestrator;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/{offer_id}")
    public ResponseEntity<ApiResponse> initiateTransaction(@PathVariable("offer_id") String offerId,
                                                          @RequestBody @Valid PaymentRequestDto paymentRequestDto,
                                                          BindingResult bindingResult,
                                                          @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);

        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        PaymentResult paymentResult = paymentOrchestrator.processPayment(userId, offerId, paymentRequestDto);

        if (paymentResult.requiresOtp()) {
            return new ResponseEntity<>(new ApiResponse(true, paymentResult.message()), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ApiResponse(true,"Payment completed"), HttpStatus.OK);
    }

    @PostMapping("/{transaction_id}/cancel")
    public ResponseEntity<Void> cancelTransaction(@PathVariable("transaction_id") UUID transactionId) {
        transactionService.cancelTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<HttpStatus> confirmOtpAndFinalizeTransaction(@RequestBody OtpConfirmRequest req) {
        transactionService.finishTransactionWithOtp(req.getUserId(), req.getOfferId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{cardId}/recent")
    public List<TransactionDto> getRecentTransactions(@PathVariable("cardId") String cardNumber, @RequestParam("count") int count) {
        return transactionService.getRecentTransactions(cardNumber, count);
    }

    @GetMapping("/cards/last-used")
    public List<String> getLastUsedCardNumbers(@RequestParam("userId") UUID userId,
                                               @RequestParam("offset") int offset,
                                               @RequestParam("limit") int limit) {
        return transactionService.lastUsedCardNumbers(userId, offset, limit);
    }

    //    @PostMapping("/period")
//    public PeriodGroupedTransactionsDto getTransactions(@RequestBody @Valid CardTransactionsRequestDto request,
//                                                        @RequestParam(defaultValue = "0") int page,
//                                          BindingResult bindingResult,
//                                          @RequestHeader("Authorization") String authorizationHeader) {
//        validateInput(bindingResult);
//
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//
//        transactionService.validateUserCardAccessWithDate(
//                request.getCardNumber(),
//                userId,
//                LocalDate.parse(request.getFrom()),
//                LocalDate.parse(request.getTo())
//        );
//
//        return transactionService.getTransactionsByPeriod(
//                request.getCardNumber(),
//                LocalDate.parse(request.getFrom()),
//                LocalDate.parse(request.getTo()),
//                page
//        );
//    }
//
//    @PostMapping("/expense/period")
//    public PeriodGroupedExpenseDto getExpense(@RequestBody @Valid CardTransactionsRequestDto request,
//                                                        @RequestParam(defaultValue = "0") int page,
//                                                        BindingResult bindingResult,
//                                                        @RequestHeader("Authorization") String authorizationHeader) {
//        validateInput(bindingResult);
//
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//
//        transactionService.validateUserCardAccessWithDate(
//                request.getCardNumber(),
//                userId,
//                LocalDate.parse(request.getFrom()),
//                LocalDate.parse(request.getTo())
//        );
//
//        return transactionService.getExpenseTransactionsByPeriod(
//                request.getCardNumber(),
//                LocalDate.parse(request.getFrom()),
//                LocalDate.parse(request.getTo()),
//                page
//        );
//    }
//
//    @PostMapping("/income/period")
//    public PeriodGroupedIncomeDto getIncome(@RequestBody @Valid CardTransactionsRequestDto request,
//                                              @RequestParam(defaultValue = "0") int page,
//                                              BindingResult bindingResult,
//                                              @RequestHeader("Authorization") String authorizationHeader) {
//        validateInput(bindingResult);
//
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//
//        transactionService.validateUserCardAccessWithDate(
//                request.getCardNumber(),
//                userId,
//                LocalDate.parse(request.getFrom()),
//                LocalDate.parse(request.getTo())
//        );
//
//        return transactionService.getIncomeTransactionsByPeriod(
//                request.getCardNumber(),
//                LocalDate.parse(request.getFrom()),
//                LocalDate.parse(request.getTo()),
//                page
//        );
//    }

    private String extractJwtFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }

    private void validateInput(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            throw new FieldValidationException("Validation failed", fieldErrors);
        }
    }

    private List<InputFieldError> getInputFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> new InputFieldError(entry.getKey(), entry.getValue()))
                .toList();
    }
}
