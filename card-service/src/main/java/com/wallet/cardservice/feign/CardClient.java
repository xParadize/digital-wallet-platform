package com.wallet.cardservice.feign;

import com.wallet.cardservice.dto.HolderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "card-client",
        url = "http://localhost:8082")
public interface CardClient {

    @GetMapping("/api/v1/user/{id}/holder")
    ResponseEntity<HolderDto> getHolder(@PathVariable("id") UUID userId);
}