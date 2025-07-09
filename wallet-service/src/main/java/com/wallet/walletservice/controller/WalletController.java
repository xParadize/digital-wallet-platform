package com.wallet.walletservice.controller;

import com.wallet.walletservice.dto.AddCardDto;
import com.wallet.walletservice.dto.ApiResponse;
import com.wallet.walletservice.dto.CardPreviewDto;
import com.wallet.walletservice.dto.InputFieldError;
import com.wallet.walletservice.exception.IncorrectSearchPath;
import com.wallet.walletservice.service.JwtService;
import com.wallet.walletservice.service.WalletService;
import com.wallet.walletservice.util.CardDataValidator;
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
@RequestMapping("/wallet")
public class WalletController {
    private final CardDataValidator cardDataValidator;
    private final WalletService walletService;
    private final JwtService jwtService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/cards")
    public ResponseEntity<?> addCard(@RequestBody @Valid AddCardDto addCardDto,
                                     BindingResult bindingResult,
                                     @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        String email = jwtService.extractEmailFromJwt(jwt);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));

        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        if (cardDataValidator.isCardExpired(addCardDto.getExpirationDate())) {
            return new ResponseEntity<>(new ApiResponse(false, "The card has expired"), HttpStatus.BAD_REQUEST);
        }

        if (cardDataValidator.isCardLinkedToUser(addCardDto.getNumber(), userId)) {
            return new ResponseEntity<>(new ApiResponse(false, "It is not possible to add a card: it is already registered in the system"), HttpStatus.BAD_REQUEST);
        }

        walletService.saveCard(addCardDto, userId, email);

        return new ResponseEntity<>(new ApiResponse(true, "The request to add the card has been successfully sent. Expect an email notification after checking the data"), HttpStatus.OK);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardPreviewDto>> getLinkedCards(@RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(walletService.getLinkedCards(userId), HttpStatus.OK);
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
