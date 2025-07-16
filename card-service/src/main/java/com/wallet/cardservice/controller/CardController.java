package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardLimitException;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.service.CardLimitService;
import com.wallet.cardservice.service.CardService;
import com.wallet.cardservice.service.JwtService;
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

    @PatchMapping("/{number}/status")
    public ResponseEntity<ApiResponse> changeCardStatus(@PathVariable("number") String number,
                                                        @RequestBody CardStatusActionDto dto,
                                                        @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        String email = jwtService.extractEmailFromJwt(jwt);

        if (!cardService.isCardLinkedToUser(number, userId)) {
            throw new CardAccessDeniedException("You can't freeze, unfreeze or block someone's card.");
        }

        CardStatusAction action = cardService.convertStringToCardStatusAction(dto.statusAction());
        Card card = cardService.getCardByNumber(number);

        switch (action) {
            case FREEZE -> {
                if (card.isFrozen()) throw new CardStatusActionException("The card is already frozen");
                cardService.freeze(number, email, userId);
            }
            case UNFREEZE -> {
                if (!card.isFrozen()) throw new CardStatusActionException("The card isn't frozen");
                cardService.unfreeze(number, email, userId);
            }
            case BLOCK -> {
                if (card.isBlocked()) throw new CardStatusActionException("The card is already blocked");
                cardService.block(number, email, userId);
            }
            default -> throw new CardStatusActionException("Unsupported action: " + dto.statusAction());
        }

        String actionMessage = switch (action) {
            case FREEZE -> "The card was successfully frozen";
            case UNFREEZE -> "The card was successfully unfrozen";
            case BLOCK -> "The card was successfully blocked";
        };

        return ResponseEntity.ok(new ApiResponse(true, actionMessage));
    }

    @PostMapping("/{number}/limit")
    public ResponseEntity<?> setCardLimit(@PathVariable("number") String number,
                                                    @RequestBody @Valid SetCardLimitRequest request,
                                                    BindingResult bindingResult,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        if (!cardService.isCardLinkedToUser(number, userId)) {
            throw new CardAccessDeniedException("You can't set limit on someone's card.");
        }

        Card card = cardService.getCardByNumber(number);

        if (card.getLimit() != null) {
            throw new CardLimitException("Card already has a limit set.");
        }

        cardLimitService.saveLimit(request.getPerTransactionLimit(), card);
        return new ResponseEntity<>(new ApiResponse(true, "Limit set successfully"), HttpStatus.CREATED);
    }

    @PatchMapping("/{number}/limit")
    public ResponseEntity<?> updateCardLimit(@PathVariable("number") String number,
                                             @RequestBody @Valid UpdateCardLimitRequest request,
                                             BindingResult bindingResult,
                                             @RequestHeader("Authorization") String authorizationHeader) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        if (!cardService.isCardLinkedToUser(number, userId)) {
            throw new CardAccessDeniedException("You can't edit limit on someone's card.");
        }

        Card card = cardService.getCardByNumber(number);
        cardLimitService.updateLimit(card, request.getPerTransactionLimit());
        return new ResponseEntity<>(new ApiResponse(true, "Limit updated successfully"), HttpStatus.OK);
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
