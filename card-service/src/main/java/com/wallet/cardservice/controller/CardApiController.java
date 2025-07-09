package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.CardPreviewDto;
import com.wallet.cardservice.dto.SaveCardDto;
import com.wallet.cardservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CardApiController {
    private final CardService cardService;

    @PostMapping("/card")
    public ResponseEntity<HttpStatus> saveCard(@RequestBody SaveCardDto saveCardDto) {
        cardService.saveCard(saveCardDto);
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
}
