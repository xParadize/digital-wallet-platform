package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.exception.IncorrectSearchPath;
import com.wallet.cardservice.exception.InvalidAuthorizationException;
import com.wallet.cardservice.service.CardService;
import com.wallet.cardservice.service.JwtService;
import com.wallet.cardservice.util.CardRequestsValidator;
import com.wallet.cardservice.util.CardSortValidator;
import com.wallet.cardservice.util.PageParamsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardApiController {
    private final CardService cardService;
    private final CardSortValidator cardSortValidator;
    private final JwtService jwtService;
    private final PageParamsValidator pageParamsValidator;
    private final CardRequestsValidator cardRequestsValidator;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @GetMapping("/{card_id}")
    public ResponseEntity<CardInfoDto> getCardById(@PathVariable("card_id") Long cardId,
                                                   @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(cardService.getCardById(cardId, userId), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<CardPreviewDto>> getCards(@RequestParam(required = false) String sort,
                                                         @RequestParam(required = false) String order,
                                                         @RequestParam(required = false) Integer offset,
                                                         @RequestParam(required = false) Integer limit,
                                                         @RequestHeader("Authorization") String authorizationHeader) throws ExecutionException, InterruptedException {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        CardSort cardSort = cardSortValidator.validateSort(sort, order);
        PageParams pageParams = pageParamsValidator.validatePageOffsetAndLimit(offset, limit);
        return new ResponseEntity<>(cardService.getCards(
                userId,
                cardSort.getType(),
                cardSort.getOrder(),
                pageParams.offset(),
                pageParams.limit()), HttpStatus.OK
        );
    }

    @PostMapping("")
    public ResponseEntity<HttpStatus> saveCard(@RequestBody SaveCardDto saveCardDto,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        String email = jwtService.extractEmailFromJwt(jwt);

        cardService.saveCard(saveCardDto, email, userId);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PatchMapping("/{card_id}")
    public ResponseEntity<HttpStatus> updateCard(@PathVariable("card_id") Long cardId,
                                                 @RequestBody UpdateCardDto updateCardDto,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        String email = jwtService.extractEmailFromJwt(jwt);

        CardStatusAction cardStatusAction = cardRequestsValidator.validateCardStatusActionRequest(
                updateCardDto.getAction(),
                cardId,
                userId);

        cardService.updateCardStatus(cardStatusAction, cardId, userId, email);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/{card_id}")
    public ResponseEntity<HttpStatus> deleteCard(@PathVariable("card_id") Long cardId,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        cardService.deleteCard(cardId, userId);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    private String extractJwtFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }

//    @DeleteMapping("/card")
//    public ResponseEntity<HttpStatus> removeCard(@RequestParam("number") String number, @RequestParam("userId") UUID userId) {
//        cardService.removeCard(number, userId);
//        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
//    }
//
//    @PatchMapping("/card")
//    public ResponseEntity<HttpStatus> subtractMoney(@RequestParam("userId") UUID userId,
//                                                    @RequestParam("amount") BigDecimal amount,
//                                                    @RequestParam("cardNumber") String cardNumber) {
//        cardService.subtractMoney(userId, amount, cardNumber);
//        return ResponseEntity.ok(HttpStatus.OK);
//    }
//
//    @GetMapping("/card/linked")
//    public boolean isCardLinkedToUser(@RequestParam("cardNumber") String cardNumber,
//                                             @RequestParam("userId") UUID userId) {
//        return cardService.isCardLinkedToUser(cardNumber, userId);
//    }
//
//    @GetMapping("/card/{number}/status")
//    public ResponseEntity<CardStatusDto> getCardStatus(@PathVariable("number") String number) {
//        return new ResponseEntity<>(cardService.getCardStatus(number), HttpStatus.OK);
//    }
}
