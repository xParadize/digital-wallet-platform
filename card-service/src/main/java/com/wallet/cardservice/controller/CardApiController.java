package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.exception.FieldValidationException;
import com.wallet.cardservice.exception.IncorrectSearchPath;
import com.wallet.cardservice.exception.InvalidAuthorizationException;
import com.wallet.cardservice.service.CardService;
import com.wallet.cardservice.service.JwtService;
import com.wallet.cardservice.service.LimitService;
import com.wallet.cardservice.util.CardRequestsValidator;
import com.wallet.cardservice.util.CardSortValidator;
import com.wallet.cardservice.util.PageParamsValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardApiController {
    private final CardService cardService;
    private final CardSortValidator cardSortValidator;
    private final JwtService jwtService;
    private final PageParamsValidator pageParamsValidator;
    private final CardRequestsValidator cardRequestsValidator;
    private final LimitService limitService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @GetMapping("/{card_id}")
    public ResponseEntity<CardInfoDto> getCardById(@PathVariable("card_id") Long cardId,
                                                   @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(cardService.getCardInfoById(cardId, userId), HttpStatus.OK);
    }

    // только для внутренних вызовов между сервисами
    @GetMapping("/{card_number}/lookup")
    public CardInfoDto getCardByNumber(@PathVariable("card_number") String number,
                                                   @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return cardService.getCardInfoByNumber(number, userId);
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

    @GetMapping("/{card_id}/limit")
    public ResponseEntity<LimitDto> getLimit(@PathVariable("card_id") Long cardId,
                                             @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(limitService.getLimitDto(cardId, userId), HttpStatus.OK);
    }

    @PatchMapping("/{card_id}/limit")
    public ResponseEntity<HttpStatus> updateLimit(@PathVariable("card_id") Long cardId,
                                                  @RequestBody @Valid LimitDto limitDto,
                                                  BindingResult bindingResult,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        cardRequestsValidator.validateUpdateCardLimitRequest(cardId, userId, limitDto.getLimitAmount());
        limitService.updateLimit(cardId, userId, limitDto.getLimitAmount());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/{card_number}/payment")
    public ResponseEntity<HttpStatus> createPayment(@PathVariable("card_number") String cardNumber,
                                                    @RequestParam("userId") UUID userId,
                                                    @RequestParam("amount") BigDecimal amount) {
        cardService.subtractMoney(cardNumber, userId, amount);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private String extractJwtFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }

    private void validateInput(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            throw new FieldValidationException("Validation failed", fieldErrors);
        }
    }

    private List<InputFieldError> getInputFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> new InputFieldError(entry.getKey(), entry.getValue()))
                .toList();
    }
}
