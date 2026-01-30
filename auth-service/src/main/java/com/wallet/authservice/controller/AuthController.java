package com.wallet.authservice.controller;

import com.wallet.authservice.dto.*;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.FieldValidationException;
import com.wallet.authservice.exception.IncorrectSearchPath;
import com.wallet.authservice.exception.InvalidAuthorizationException;
import com.wallet.authservice.service.*;
import com.wallet.authservice.util.AuthRequestsValidator;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Authentication, registration and token management")
public class AuthController {
    private final UnverifiedUserService unverifiedUserService;
    private final AuthService authService;
    private final UserPrototypeService userPrototypeService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthRequestsValidator authRequestsValidator;

    @Value("${unverified-user.ttl.sec}")
    private long unverifiedUserTtl;

    @Operation(summary = "Catch-all for unknown paths", description = "Returns 404 for unsupported auth API paths.", hidden = true)
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiStatusResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Sign up", description = "Registers a new user. Sends email confirmation link. User must confirm email within TTL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful, confirmation email sent", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed (e.g. password mismatch, duplicate email/phone)")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<ApiStatusResponse> signUp(@RequestBody @Valid SignUpRequest request, BindingResult bindingResult) {
        validateInput(bindingResult);

        authRequestsValidator.validateSignUpRequest(
                request.getPassword(),
                request.getPasswordConfirmation(),
                request.getEmail(),
                request.getPhone()
        );

        unverifiedUserService.saveUnverifiedUser(request);
        String message = String.format("Please, confirm your email within %s minutes", unverifiedUserTtl / 60);
        return new ResponseEntity<>(new ApiStatusResponse(true, message), HttpStatus.OK);
    }

    @Operation(summary = "Confirm email", description = "Confirms user email using the token from the confirmation link. Enables the user and returns auth response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email confirmed, user enabled", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired confirmation code")
    })
    @GetMapping("/confirm-email/{code}")
    public ResponseEntity<ApiStatusResponse> confirmEmail(@Parameter(description = "Email confirmation token from link") @PathVariable("code") String code) {
        UnverifiedUser unverifiedUser = authService.confirmEmailToken(code);
        String authResponse = unverifiedUserService.enableUser(unverifiedUser);
        return new ResponseEntity<>(new ApiStatusResponse(true, authResponse), HttpStatus.OK);
    }

    @Operation(summary = "Sign in", description = "Authenticates user with email and password. Returns JWT access and refresh tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request, BindingResult bindingResult) {
        validateInput(bindingResult);
        authRequestsValidator.validateSignInRequest(request.getEmail(), request.getPassword());
        return authService.signIn(request);
    }

    @Operation(summary = "Refresh token", description = "Issues new access and refresh tokens using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New tokens issued", content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh-token")
    public JwtAuthenticationResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request, BindingResult bindingResult) {
        validateInput(bindingResult);
        return refreshTokenService.refreshToken(request);
    }

    @Operation(summary = "Change password", description = "Changes the password for the authenticated user. Requires valid JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation failed (e.g. wrong old password)"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing authorization")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/change-password")
    public ResponseEntity<ApiStatusResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authorizationHeader) {
        validateInput(bindingResult);
        String jwt = extractJwtFromHeader(authorizationHeader);
        String email = jwtService.extractEmailFromJwt(jwt);
        authRequestsValidator.validateChangePasswordRequest(
                email,
                request.getOldPassword(),
                request.getNewPassword(),
                request.getNewPassword()
        );
        userPrototypeService.changePassword(request.getNewPassword(), email);
        return new ResponseEntity<>(new ApiStatusResponse(true, "The password has been successfully changed"), HttpStatus.OK);
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
