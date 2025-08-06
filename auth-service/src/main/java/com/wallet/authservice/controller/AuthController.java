package com.wallet.authservice.controller;

import com.wallet.authservice.dto.*;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.FieldValidationException;
import com.wallet.authservice.exception.IncorrectSearchPath;
import com.wallet.authservice.exception.InvalidAuthorizationException;
import com.wallet.authservice.service.*;
import com.wallet.authservice.util.AuthRequestsValidator;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UnverifiedUserService unverifiedUserService;
    private final AuthService authService;
    private final UserPrototypeService userPrototypeService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthRequestsValidator authRequestsValidator;

    @Value("${unverified-user.ttl.sec}")
    long unverifiedUserTtl;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody @Valid SignUpRequest request, BindingResult bindingResult) {
        validateInput(bindingResult);

        authRequestsValidator.validateSignUpRequest(
                request.getPassword(),
                request.getPasswordConfirmation(),
                request.getEmail(),
                request.getPhone()
        );

        unverifiedUserService.saveUnverifiedUser(request);
        String message = String.format("Please, confirm your email within %s minutes", unverifiedUserTtl / 60);
        return new ResponseEntity<>(new ApiResponse(true, message), HttpStatus.OK);
    }

    @GetMapping("/confirm-email/{code}")
    public ResponseEntity<ApiResponse> confirmEmail(@PathVariable("code") String code) {
        UnverifiedUser unverifiedUser = authService.confirmEmailToken(code);
        String authResponse = unverifiedUserService.enableUser(unverifiedUser);
        return new ResponseEntity<>(new ApiResponse(true, authResponse), HttpStatus.OK);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request, BindingResult bindingResult) {
        validateInput(bindingResult);
        authRequestsValidator.validateSignInRequest(request.getEmail(), request.getPassword());
        return authService.signIn(request);
    }

    @PostMapping("/refresh-token")
    public JwtAuthenticationResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request,
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
        return new ResponseEntity<>(new ApiResponse(true, "The password has been successfully changed"), HttpStatus.OK);
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
