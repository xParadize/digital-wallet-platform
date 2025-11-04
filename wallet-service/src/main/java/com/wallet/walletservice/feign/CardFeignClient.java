package com.wallet.walletservice.feign;

import com.wallet.walletservice.dto.CardDetailsDto;
import com.wallet.walletservice.dto.CardPreviewDto;
import com.wallet.walletservice.dto.SaveCardDto;
import com.wallet.walletservice.enums.CardSortOrder;
import com.wallet.walletservice.enums.CardSortType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "${digital-wallet-platform.services.card-service.uri}")
public interface CardFeignClient {

    @GetMapping("/api/v1/cards/{cardId}")
    ResponseEntity<CardDetailsDto> getCardById(@PathVariable("cardId") Long cardId, @RequestParam("userId") UUID userId);

    @PostMapping("/api/v1/card")
    ResponseEntity<HttpStatus> saveCard(@RequestBody SaveCardDto saveCardDto);

    @GetMapping("/api/v1/card/linked")
    boolean isCardLinkedToUser(@RequestParam("cardNumber") String cardNumber, @RequestParam("userId") UUID userId);

    @GetMapping("/api/v1/cards")
    ResponseEntity<List<CardPreviewDto>> getLinkedCards(
            @RequestParam("userId") UUID userId,
            @RequestParam("sort") CardSortType sort,
            @RequestParam("order") CardSortOrder order
    );

    @DeleteMapping("/api/v1/card")
    ResponseEntity<HttpStatus> removeCard(@RequestParam("number") String number, @RequestParam("userId") UUID userId);
}
