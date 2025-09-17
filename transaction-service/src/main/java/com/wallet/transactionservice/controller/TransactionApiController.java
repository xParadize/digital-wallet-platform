package com.wallet.transactionservice.controller;

import com.wallet.transactionservice.dto.ApiResponse;
import com.wallet.transactionservice.dto.OtpConfirmRequest;
import com.wallet.transactionservice.dto.TransactionDto;
import com.wallet.transactionservice.exception.IncorrectSearchPath;
import com.wallet.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionApiController {
    private final TransactionService transactionService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @GetMapping("/")
    public List<TransactionDto> getTransactions(@RequestParam("cardNumber") String cardNumber, @RequestParam("limit") int limit) {
        return transactionService.getLastTransactions(cardNumber, limit);
    }

    @GetMapping("/cards/last-used")
    public Set<String> getLastUsedCardNumbers(@RequestParam("userId") UUID userId) {
        return transactionService.lastUsedCardNumbers(userId);
    }

    @PostMapping("/confirm")
    public ResponseEntity<HttpStatus> confirmOtpAndFinalizeTransaction(@RequestBody OtpConfirmRequest req) {
        transactionService.finishTransactionByUserAndOffer(req.getUserId(), req.getOfferId());
        return ResponseEntity.ok().build();
    }
}
