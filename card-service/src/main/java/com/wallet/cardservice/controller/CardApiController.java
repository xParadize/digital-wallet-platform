package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.IncorrectSearchPath;
import com.wallet.cardservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CardApiController {
    private final CardService cardService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @GetMapping("/card")
    public ResponseEntity<CardDetailsDto> getLinkedCard(@RequestParam("number") String number, @RequestParam("userId") UUID userId) {
        if (!cardService.isCardLinkedToUser(number, userId)) {
            throw new CardAccessDeniedException("Access to the card is forbidden");
        }
        return new ResponseEntity<>(cardService.getLinkedCard(number, userId), HttpStatus.OK);
    }

    @PostMapping("/card")
    public ResponseEntity<HttpStatus> saveCard(@RequestBody SaveCardDto saveCardDto) {
        cardService.saveCard(saveCardDto);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/card")
    public ResponseEntity<HttpStatus> removeCard(@RequestParam("number") String number, @RequestParam("userId") UUID userId) {
        cardService.removeCard(number, userId);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/card")
    public ResponseEntity<HttpStatus> subtractMoney(@RequestParam("userId") UUID userId,
                                                    @RequestParam("amount") BigDecimal amount,
                                                    @RequestParam("cardNumber") String cardNumber) {
        cardService.subtractMoney(userId, amount, cardNumber);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/card/linked")
    public boolean isCardLinkedToUser(@RequestParam("cardNumber") String cardNumber,
                                             @RequestParam("userId") UUID userId) {
        return cardService.isCardLinkedToUser(cardNumber, userId);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardPreviewDto>> getLinkedCards(@RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(cardService.getLinkedCards(userId), HttpStatus.OK);
    }

    @GetMapping("/card/{number}/status")
    public ResponseEntity<CardStatusDto> getCardStatus(@PathVariable("number") String number) {
        return new ResponseEntity<>(cardService.getCardStatus(number), HttpStatus.OK);
    }
}
