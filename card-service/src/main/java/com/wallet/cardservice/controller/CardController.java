package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.enums.CardStatus;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.exception.FieldValidationException;
import com.wallet.cardservice.exception.IncorrectSearchPath;
import com.wallet.cardservice.exception.InvalidAuthorizationException;
import com.wallet.cardservice.service.CardLimitService;
import com.wallet.cardservice.service.CardService;
import com.wallet.cardservice.service.JwtService;
import com.wallet.cardservice.util.CardRequestsValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/card")
public class CardController {
    private final CardService cardService;
    private final JwtService jwtService;
    private final CardLimitService cardLimitService;
    private final CardRequestsValidator cardRequestsValidator;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

//    @PatchMapping("/{number}/status")
//    public ResponseEntity<ApiResponse> changeCardStatus(@PathVariable("number") String number,
//                                                        @RequestBody CardStatusActionDto dto,
//                                                        @RequestHeader("Authorization") String authorizationHeader) {
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//        String email = jwtService.extractEmailFromJwt(jwt);
//
//        // cardRequestsValidator.validateCardStatusRequest(number, userId);
//
//        CardStatus action = cardService.convertStringToCardStatusAction(dto.statusAction());
//        Card card = cardService.getCardByNumber(number);

//        switch (action) {
//            case FREEZE -> {
//                if (card.isFrozen()) throw new CardStatusActionException("The card is already frozen");
//                cardService.freeze(number, email, userId);
//            }
//            case UNFREEZE -> {
//                if (!card.isFrozen()) throw new CardStatusActionException("The card isn't frozen");
//                cardService.unfreeze(number, email, userId);
//            }
//            case BLOCK -> {
//                if (card.isBlocked()) throw new CardStatusActionException("The card is already blocked");
//                cardService.block(number, email, userId);
//            }
//            default -> throw new CardStatusActionException("Unsupported action: " + dto.statusAction());
//        }
//
//        String actionResponseMessage = switch (action) {
//            case FREEZE -> "The card was successfully frozen";
//            case UNFREEZE -> "The card was successfully unfrozen";
//            case BLOCK -> "The card was successfully blocked";
//        };
//
//        return ResponseEntity.ok(new ApiResponse(true, actionResponseMessage));
//    }

//    @PostMapping("/{number}/limit")
//    public ResponseEntity<ApiResponse> setCardLimit(@PathVariable("number") String number,
//                                            @RequestBody @Valid SetCardLimitRequest request,
//                                            BindingResult bindingResult,
//                                            @RequestHeader("Authorization") String authorizationHeader) {
//        validateInput(bindingResult);
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//
//        Card card = cardService.getCardByNumber(number);
//
//        // cardRequestsValidator.validateSetCardLimitRequest(number, userId, card, request.getPerTransactionLimit());
//
//        cardLimitService.saveLimit(request.getPerTransactionLimit(), card);
//        return new ResponseEntity<>(new ApiResponse(true, "Limit set successfully"), HttpStatus.CREATED);
//    }
//
//    @PatchMapping("/{number}/limit")
//    public ResponseEntity<ApiResponse> updateCardLimit(@PathVariable("number") String number,
//                                             @RequestBody @Valid UpdateCardLimitRequest request,
//                                             BindingResult bindingResult,
//                                             @RequestHeader("Authorization") String authorizationHeader) {
//        validateInput(bindingResult);
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//
//        // cardRequestsValidator.validateUpdateCardLimitRequest(number, userId, request.getPerTransactionLimit());
//
//        Card card = cardService.getCardByNumber(number);
//        cardLimitService.updateLimit(card, request.getPerTransactionLimit());
//        return new ResponseEntity<>(new ApiResponse(true, "Limit updated successfully"), HttpStatus.OK);
//    }
//
//    @DeleteMapping("/{number}/limit")
//    public ResponseEntity<ApiResponse> removeCardLimit(@PathVariable("number") String number,
//                                               @RequestHeader("Authorization") String authorizationHeader) {
//        String jwt = extractJwtFromHeader(authorizationHeader);
//        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
//
//        // cardRequestsValidator.validateRemoveCardLimitRequest(number, userId);
//
//        Card card = cardService.getCardByNumber(number);
//        cardLimitService.removeLimit(card);
//        return new ResponseEntity<>(new ApiResponse(true, "Limit removed successfully"), HttpStatus.NO_CONTENT);
//    }

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
