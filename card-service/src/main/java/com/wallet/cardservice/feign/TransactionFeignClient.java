package com.wallet.cardservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.transaction-service.uri}")
public interface TransactionFeignClient {

    @GetMapping("/api/v1/transactions/cards/last-used")
    Set<String> getLastUsedCardNumbers(@RequestParam("userId") UUID userId);
}