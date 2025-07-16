package com.wallet.transactionservice.controller;

import com.wallet.transactionservice.dto.ApiResponse;
import com.wallet.transactionservice.dto.PaymentRequestDto;
import com.wallet.transactionservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
    private final JwtService jwtService;

    @PostMapping("/{transaction_id}")
    public ResponseEntity<ApiResponse> initiateTransaction(@PathVariable("transaction_id") String transactionId,
                                                           @RequestBody PaymentRequestDto dto,
                                                           @RequestHeader("Authorization") String authorizationHeader) {
        // ищем айди транзакции - если нет - ошибка

        // 1. Существование карты
        // 2. Принадлежность карты пользователю
        // 3. Срок действия карты

        // 4. Наличие достаточного баланса

        // 5. Проверка лимита пользователя
        

    }
}
