package com.wallet.analyticsservice.feign;

import com.wallet.analyticsservice.dto.CardDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.card-service.uri}")
public interface CardFeignClient {

    @GetMapping("/api/v1/card")
    ResponseEntity<CardDetailsDto> getLinkedCard(@RequestParam("number") String number,
                                                 @RequestParam("userId") UUID userId);
}
