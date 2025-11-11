package com.wallet.walletservice.controller;

import com.wallet.walletservice.dto.*;
import com.wallet.walletservice.exception.FieldValidationException;
import com.wallet.walletservice.exception.IncorrectSearchPath;
import com.wallet.walletservice.exception.InvalidAuthorizationException;
import com.wallet.walletservice.service.JwtService;
import com.wallet.walletservice.service.WalletService;
import com.wallet.walletservice.util.WalletRequestsValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet")
public class WalletController {
    private final WalletService walletService;
    private final JwtService jwtService;
    private final WalletRequestsValidator walletRequestsValidator;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<CardInfoDto> getCard(@PathVariable("cardId") Long cardId,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(walletService.getCardById(cardId, userId), HttpStatus.OK);
    }

    @PostMapping("/cards")
    public ResponseEntity<ApiResponse> addCard(@RequestBody @Valid AddCardDto addCardDto,
                                     BindingResult bindingResult,
                                     @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        String email = jwtService.extractEmailFromJwt(jwt);

        walletRequestsValidator.validateAddCardRequest(addCardDto.getExpirationDate(), addCardDto.getNumber(), userId);
        walletService.saveCard(addCardDto, userId, email);
        return new ResponseEntity<>(new ApiResponse(true, "The request to add the card has been successfully sent. Expect an email notification after checking the data"), HttpStatus.OK);
    }

    @DeleteMapping("/cards/{number}")
    public ResponseEntity<ApiResponse> removeCard(@PathVariable("number") String number,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        walletRequestsValidator.validateRemoveCardRequest(number, userId);
        walletService.removeCard(number, userId);
        return new ResponseEntity<>(new ApiResponse(true, "The card was successfully unlinked from your wallet"), HttpStatus.NO_CONTENT);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardPreviewDto>> getLinkedCards(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestHeader("Authorization") String authorizationHeader) {

        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        CardSort cardSort = walletRequestsValidator.validateGetLinkedCardsRequest(sort, order);

        return new ResponseEntity<>(
                walletService.getLinkedCards(userId, cardSort.getType(), cardSort.getOrder()),
                HttpStatus.OK
        );
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
