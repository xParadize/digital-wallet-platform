package com.wallet.transactionservice.controller;

import com.wallet.transactionservice.dto.*;
import com.wallet.transactionservice.exception.FieldValidationException;
import com.wallet.transactionservice.exception.IncorrectSearchPath;
import com.wallet.transactionservice.exception.InvalidAuthorizationException;
import com.wallet.transactionservice.service.JwtService;
import com.wallet.transactionservice.service.PaymentOrchestrator;
import com.wallet.transactionservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction API", description = "Payment and transaction management endpoints")
public class TransactionApiController {
    private final TransactionService transactionService;
    private final JwtService jwtService;
    private final PaymentOrchestrator paymentOrchestrator;

    @Operation(summary = "Catch-all for unknown paths", description = "Returns 404 for unsupported transaction API paths.", hidden = true)
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiStatusResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Get transaction by ID", description = "Returns transaction details for the given transaction ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found", content = @Content(schema = @Schema(implementation = TransactionInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{transaction_id}")
    public TransactionInfoDto getTransaction(
            @Parameter(description = "Transaction UUID") @PathVariable("transaction_id") String transactionId) {
        return transactionService.getTransactionInfo(UUID.fromString(transactionId));
    }

    @Operation(summary = "Initiate payment", description = "Initiates a payment for the given offer. May require OTP confirmation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment initiated or completed", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{offer_id}")
    public ResponseEntity<ApiStatusResponse> initiateTransaction(
            @Parameter(description = "Payment offer ID") @PathVariable("offer_id") String offerId,
            @RequestBody @Valid PaymentRequestDto paymentRequestDto,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);

        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        PaymentResult paymentResult = paymentOrchestrator.processPayment(userId, offerId, paymentRequestDto);

        if (paymentResult.requiresOtp()) {
            return new ResponseEntity<>(new ApiStatusResponse(true, paymentResult.message()), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ApiStatusResponse(true,"Payment completed"), HttpStatus.OK);
    }

    @Operation(summary = "Cancel transaction", description = "Cancels an existing transaction by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PostMapping("/{transaction_id}/cancel")
    public ResponseEntity<Void> cancelTransaction(
            @Parameter(description = "Transaction UUID") @PathVariable("transaction_id") UUID transactionId) {
        transactionService.cancelTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Confirm OTP and finalize transaction", description = "Confirms OTP and completes a pending transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction finalized successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or request")
    })
    @PostMapping("/confirm")
    public ResponseEntity<HttpStatus> confirmOtpAndFinalizeTransaction(@RequestBody OtpConfirmRequest req) {
        transactionService.finishTransactionWithOtp(req.getUserId(), req.getOfferId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get recent transactions by card", description = "Returns the most recent transactions for the given card number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of recent transactions", content = @Content(schema = @Schema(implementation = TransactionDto.class)))
    })
    @GetMapping("/{cardId}/recent")
    public List<TransactionDto> getRecentTransactions(
            @Parameter(description = "Card number") @PathVariable("cardId") String cardNumber,
            @Parameter(description = "Maximum number of transactions to return") @RequestParam("count") int count) {
        return transactionService.getRecentTransactions(cardNumber, count);
    }

    @Operation(summary = "Get last used card numbers", description = "Returns card numbers recently used by the user, with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of card numbers")
    })
    @GetMapping("/cards/last-used")
    public List<String> getLastUsedCardNumbers(
            @Parameter(description = "User UUID") @RequestParam("userId") UUID userId,
            @Parameter(description = "Pagination offset") @RequestParam("offset") int offset,
            @Parameter(description = "Maximum number of items") @RequestParam("limit") int limit) {
        return transactionService.lastUsedCardNumbers(userId, offset, limit);
    }

    @Operation(summary = "Get transactions by period", description = "Returns transactions for a card within the given date range, grouped by period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouped transactions", content = @Content(schema = @Schema(implementation = PeriodGroupedTransactionsDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/period")
    public PeriodGroupedTransactionsDto getTransactions(
            @RequestBody @Valid CardTransactionsRequestDto request,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        transactionService.validateUserCardAccessWithDate(
                request.getCardNumber(),
                userId,
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo())
        );

        return transactionService.getTransactionsByPeriod(
                request.getCardNumber(),
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo()),
                page
        );
    }

    @Operation(summary = "Get expense by period", description = "Returns expense transactions for a card within the given date range, grouped by period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouped expense data", content = @Content(schema = @Schema(implementation = PeriodGroupedExpenseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/expense/period")
    public PeriodGroupedExpenseDto getExpense(
            @RequestBody @Valid CardTransactionsRequestDto request,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        transactionService.validateUserCardAccessWithDate(
                request.getCardNumber(),
                userId,
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo())
        );

        return transactionService.getExpenseTransactionsByPeriod(
                request.getCardNumber(),
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo()),
                page
        );
    }

    @Operation(summary = "Get income by period", description = "Returns income transactions for a card within the given date range, grouped by period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouped income data", content = @Content(schema = @Schema(implementation = PeriodGroupedIncomeDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/income/period")
    public PeriodGroupedIncomeDto getIncome(
            @RequestBody @Valid CardTransactionsRequestDto request,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        transactionService.validateUserCardAccessWithDate(
                request.getCardNumber(),
                userId,
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo())
        );

        return transactionService.getIncomeTransactionsByPeriod(
                request.getCardNumber(),
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo()),
                page
        );
    }

    @Operation(summary = "Get expense analytics report link", description = "Generates or retrieves an expense analytics report for the card and period, returns report URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report link in response body", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/expense/period/analytics")
    public ResponseEntity<ApiStatusResponse> getExpenseAnalytics(
            @RequestBody @Valid CardTransactionsRequestDto request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        transactionService.validateUserCardAccessWithDate(
                request.getCardNumber(),
                userId,
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo())
        );

        String reportLink = transactionService.getExpenseReportLink(
                request.getCardNumber(),
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo())
        );

        return new ResponseEntity<>(new ApiStatusResponse(true, reportLink), HttpStatus.OK);
    }

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
