package com.wallet.cardservice.feign;

import com.wallet.cardservice.dto.Holder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.user-service.uri}")
public interface UserFeignClient {

    @GetMapping("/api/v1/users/{id}/holder")
    ResponseEntity<Holder> getCardHolder(@PathVariable("id") UUID userId);
}