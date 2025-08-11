package com.wallet.transactionservice.feign;

import com.wallet.transactionservice.dto.CardDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.card-service.uri}")
public interface CardFeignClient {

    @GetMapping("/api/v1/card")
    ResponseEntity<CardDetailsDto> getLinkedCard(@RequestParam("number") String number,
                                                 @RequestParam("userId") UUID userId);

    @PatchMapping("/api/v1/card")
    ResponseEntity<HttpStatus> subtractMoney(@RequestParam("userId") UUID userId,
                                             @RequestParam("amount") BigDecimal amount,
                                             @RequestParam("cardNumber") String cardNumber);
}
