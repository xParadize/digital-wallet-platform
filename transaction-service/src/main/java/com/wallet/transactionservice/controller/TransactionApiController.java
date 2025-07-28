package com.wallet.transactionservice.controller;

import com.wallet.transactionservice.dto.ApiResponse;
import com.wallet.transactionservice.dto.OtpConfirmRequest;
import com.wallet.transactionservice.exception.IncorrectSearchPath;
import com.wallet.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionApiController {
    private final TransactionService transactionService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmOtpAndFinalizeTransaction(@RequestBody OtpConfirmRequest req) {
        transactionService.finishTransactionByUserAndOffer(req.getUserId(), req.getOfferId());
        return ResponseEntity.ok().build();
    }
}
