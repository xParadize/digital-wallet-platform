package com.wallet.transactionservice.controller;

import com.wallet.transactionservice.dto.ApiResponse;
import com.wallet.transactionservice.dto.InputFieldError;
import com.wallet.transactionservice.dto.PaymentRequestDto;
import com.wallet.transactionservice.dto.PaymentResult;
import com.wallet.transactionservice.exception.IncorrectSearchPath;
import com.wallet.transactionservice.exception.InvalidAuthorizationException;
import com.wallet.transactionservice.service.JwtService;
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
@RequestMapping("/transactions")
public class TransactionController {
    private final JwtService jwtService;
    private final TransactionService transactionService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/{offer_id}")
    public ResponseEntity<?> initiateTransaction(@PathVariable("offer_id") String offerId,
                                                           @RequestBody @Valid PaymentRequestDto paymentRequestDto,
                                                           BindingResult bindingResult,
                                                           @RequestHeader("Authorization") String authorizationHeader) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        PaymentResult paymentResult = transactionService.processPayment(userId, offerId, paymentRequestDto);

        if (paymentResult.requiresOtp()) {
            return ResponseEntity.ok(paymentResult.message());
        }
        return ResponseEntity.ok().build();
    }

    private String extractJwtFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
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
