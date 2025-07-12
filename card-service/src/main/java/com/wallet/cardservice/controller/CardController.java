package com.wallet.cardservice.controller;

import com.wallet.cardservice.dto.ApiResponse;
import com.wallet.cardservice.dto.CardStatusActionDto;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.service.CardService;
import com.wallet.cardservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/card")
public class CardController {
    private final CardService cardService;
    private final JwtService jwtService;

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

}
