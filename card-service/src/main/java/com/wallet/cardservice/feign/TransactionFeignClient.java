package com.wallet.cardservice.feign;

import com.wallet.cardservice.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.transaction-service.uri}")
public interface TransactionFeignClient {

    @GetMapping("/api/v1/transactions/cards/last-used")
    List<String> getLastUsedCardNumbers(@RequestParam("userId") UUID userId,
                                        @RequestParam("offset") int offset,
                                        @RequestParam("limit") int limit);

    @GetMapping("/api/v1/transactions/{cardNumber}/recent")
    List<TransactionDto> getRecentTransactions(@PathVariable("cardNumber") String cardNumber, @RequestParam("count") int count);
}