package com.wallet.walletservice.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${digital-wallet-platform.services.transaction-service.uri}")
public interface TransactionFeignClient {
}
