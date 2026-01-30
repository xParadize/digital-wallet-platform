package com.wallet.userservice.controller;

import com.wallet.userservice.dto.Holder;
import com.wallet.userservice.entity.UnverifiedUser;
import com.wallet.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "User management and lookup endpoints (internal/service-to-service)")
public class UserApiController {
    private final UserService userService;

    @Operation(summary = "Save user", description = "Persists an unverified user. Used after email confirmation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<HttpStatus> saveUser(@RequestBody UnverifiedUser unverifiedUser) {
        userService.saveUser(unverifiedUser);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(summary = "Check if user exists", description = "Returns whether a user with the given email or phone already exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "True if user exists, false otherwise")
    })
    @GetMapping("/exists")
    public boolean userExists(
            @Parameter(description = "User email") @RequestParam("email") String email,
            @Parameter(description = "User phone") @RequestParam("phone") String phone) {
        return userService.existsByEmailOrPhone(email, phone);
    }

    @Operation(summary = "Get card holder", description = "Returns card holder information for the given user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card holder data", content = @Content(schema = @Schema(implementation = Holder.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}/holder")
    public Holder getCardHolder(
            @Parameter(description = "User UUID") @PathVariable("id") UUID userId) {
        return userService.getCardHolder(userId);
    }
}

