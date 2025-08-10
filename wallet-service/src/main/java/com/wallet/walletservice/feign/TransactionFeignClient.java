package com.wallet.walletservice.feign;

import com.wallet.walletservice.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "transaction-client",
        url = "http://localhost:8006")
public interface TransactionFeignClient {

    @GetMapping("/api/v1/transactions/")
    List<TransactionDto> getRecentTransactions(@RequestParam("cardNumber") String cardNumber, @RequestParam("limit") int limit);
}
