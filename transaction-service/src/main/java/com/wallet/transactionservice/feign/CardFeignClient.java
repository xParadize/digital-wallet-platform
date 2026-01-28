package com.wallet.transactionservice.feign;

import com.wallet.transactionservice.dto.CardInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.card-service.uri}")
public interface CardFeignClient {

    @GetMapping("/api/v1/cards/{card_number}/lookup")
    CardInfoDto getCardByNumber(@PathVariable("card_number") String number);

    @PostMapping("/api/v1/cards/{card_number}/payment")
    ResponseEntity<HttpStatus> createPayment(@PathVariable("card_number") String cardNumber,
                                             @RequestParam("userId") UUID userId,
                                             @RequestParam("amount") BigDecimal amount);
}
