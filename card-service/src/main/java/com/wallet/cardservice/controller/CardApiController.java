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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Card API", description = "Card management, limits and payment endpoints")
public class CardApiController {
    private final CardService cardService;
    private final CardSortValidator cardSortValidator;
    private final JwtService jwtService;
    private final PageParamsValidator pageParamsValidator;
    private final CardRequestsValidator cardRequestsValidator;
    private final LimitService limitService;

    @Operation(summary = "Catch-all for unknown paths", description = "Returns 404 for unsupported card API paths.", hidden = true)
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiStatusResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Get card by ID", description = "Returns full card information for the given card ID. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found", content = @Content(schema = @Schema(implementation = CardInfoDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{card_id}")
    public ResponseEntity<CardInfoDto> getCardById(
            @Parameter(description = "Card ID") @PathVariable("card_id") Long cardId,
            @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(cardService.getCardInfoById(cardId, userId), HttpStatus.OK);
    }

    @Operation(summary = "Lookup card by number", description = "Returns card information by card number. For internal service-to-service calls only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found", content = @Content(schema = @Schema(implementation = CardInfoDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{card_number}/lookup")
    public CardInfoDto getCardByNumber(
            @Parameter(description = "Card number") @PathVariable("card_number") String number,
            @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return cardService.getCardInfoByNumber(number, userId);
    }

    @Operation(summary = "Get user cards", description = "Returns a paginated list of cards for the authenticated user, with optional sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of card previews", content = @Content(schema = @Schema(implementation = CardPreviewDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("")
    public ResponseEntity<List<CardPreviewDto>> getCards(
            @Parameter(description = "Sort field (e.g. balance, createdAt)") @RequestParam(required = false) String sort,
            @Parameter(description = "Sort order (asc, desc)") @RequestParam(required = false) String order,
            @Parameter(description = "Pagination offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Page size limit") @RequestParam(required = false) Integer limit,
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

    @Operation(summary = "Add new card", description = "Creates and links a new card for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("")
    public ResponseEntity<HttpStatus> saveCard(@RequestBody SaveCardDto saveCardDto, @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        String email = jwtService.extractEmailFromJwt(jwt);

        cardService.saveCard(saveCardDto, email, userId);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @Operation(summary = "Update card status", description = "Updates card status (e.g. block/unblock) for the given card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid action or request"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{card_id}")
    public ResponseEntity<HttpStatus> updateCard(
            @Parameter(description = "Card ID") @PathVariable("card_id") Long cardId,
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

    @Operation(summary = "Delete card", description = "Soft-deletes or removes the card for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{card_id}")
    public ResponseEntity<HttpStatus> deleteCard(
            @Parameter(description = "Card ID") @PathVariable("card_id") Long cardId,
            @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        cardService.deleteCard(cardId, userId);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get card limit", description = "Returns the spending limit for the given card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Limit data", content = @Content(schema = @Schema(implementation = LimitDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{card_id}/limit")
    public ResponseEntity<LimitDto> getLimit(
            @Parameter(description = "Card ID") @PathVariable("card_id") Long cardId,
            @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        UUID userId = UUID.fromString(jwtService.extractUserIdFromJwt(jwt));
        return new ResponseEntity<>(limitService.getLimitDto(cardId, userId), HttpStatus.OK);
    }

    @Operation(summary = "Update card limit", description = "Updates the spending limit for the given card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Limit updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization"),
            @ApiResponse(responseCode = "403", description = "User has no access to the card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{card_id}/limit")
    public ResponseEntity<HttpStatus> updateLimit(
            @Parameter(description = "Card ID") @PathVariable("card_id") Long cardId,
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

    @Operation(summary = "Create payment (internal)", description = "Deducts amount from the card. For internal service-to-service use.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment applied successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PostMapping("/{card_number}/payment")
    public ResponseEntity<HttpStatus> createPayment(
            @Parameter(description = "Card number") @PathVariable("card_number") String cardNumber,
            @Parameter(description = "User UUID") @RequestParam("userId") UUID userId,
            @Parameter(description = "Payment amount") @RequestParam("amount") BigDecimal amount) {
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
