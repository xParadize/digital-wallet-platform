package com.wallet.walletservice.feign;

import com.wallet.walletservice.dto.CardDetailsDto;
import com.wallet.walletservice.dto.CardPreviewDto;
import com.wallet.walletservice.dto.SaveCardDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "wallet-client",
        url = "http://localhost:8084")
public interface WalletClient {

    @PostMapping("/api/v1/card")
    ResponseEntity<HttpStatus> saveCard(@RequestBody SaveCardDto saveCardDto);

    @GetMapping("/api/v1/card/linked")
    boolean isCardLinkedToUser(@RequestParam("cardNumber") String cardNumber, @RequestParam("userId")UUID userId);

    @GetMapping("/api/v1/cards")
    ResponseEntity<List<CardPreviewDto>> getLinkedCards(@RequestParam("userId") UUID userId);

    @GetMapping("/api/v1/card")
    ResponseEntity<CardDetailsDto> getLinkedCard(@RequestParam("number") String number, @RequestParam("userId") UUID userId);
}
