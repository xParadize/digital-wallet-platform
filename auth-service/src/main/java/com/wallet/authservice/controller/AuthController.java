package com.wallet.authservice.controller;

import com.wallet.authservice.dto.*;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.IncorrectSearchPath;
import com.wallet.authservice.exception.InvalidAuthorizationException;
import com.wallet.authservice.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
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
    private final PasswordService passwordService;

    @Value("${unverified-user.ttl.sec}")
    long unverifiedUserTtl;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        if (!Objects.equals(request.getPassword(), request.getPasswordConfirmation())) {
            return new ResponseEntity<>(new ApiResponse(false, "Passwords don't match"), HttpStatus.BAD_REQUEST);
        }

        if (unverifiedUserService.existsByEmailOrPhone(request.getEmail(), request.getPhone())) {
            return new ResponseEntity<>(new ApiResponse(false, "User with this email address or phone number already exists"), HttpStatus.BAD_REQUEST);
        }

        unverifiedUserService.saveUnverifiedUser(request);
        String message = String.format("Please, confirm your email within %s minutes", unverifiedUserTtl / 60);
        return new ResponseEntity<>(new ApiResponse(true, message), HttpStatus.OK);
    }

    @GetMapping("/confirm-email/{code}")
    public ResponseEntity<?> confirmEmail(@PathVariable("code") String code) {
        UnverifiedUser unverifiedUser = authService.confirmEmailToken(code);
        String authResponse = unverifiedUserService.enableUser(unverifiedUser);
        return new ResponseEntity<>(new ApiResponse(true, authResponse), HttpStatus.OK);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        if (!userPrototypeService.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "User with this email doesn't exist"), HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = userPrototypeService.findPasswordByEmail(request.getEmail());
        if (!passwordService.matchesPassword(request.getPassword(), encodedPassword)) {
            return new ResponseEntity<>(new ApiResponse(false, "Incorrect password"), HttpStatus.BAD_REQUEST);
        }

        var response = authService.signIn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        JwtAuthenticationResponse response = refreshTokenService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                            BindingResult bindingResult,
                                            @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        String email = jwtService.extractEmailFromJwt(jwt);

        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = userPrototypeService.findPasswordByEmail(email);
        if (!passwordService.matchesPassword(request.getOldPassword(), encodedPassword)) {
            return new ResponseEntity<>(new ApiResponse(false, "Incorrect old password"), HttpStatus.BAD_REQUEST);
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            return new ResponseEntity<>(new ApiResponse(false, "Old and new passwords are similar"), HttpStatus.BAD_REQUEST);
        }

        if (!(request.getNewPassword().equals(request.getNewPasswordConfirmation()))) {
            return new ResponseEntity<>(new ApiResponse(false, "Passwords don't match"), HttpStatus.BAD_REQUEST);
        }

        userPrototypeService.changePassword(request.getNewPassword(), email);
        return new ResponseEntity<>(new ApiResponse(true, "The password has been successfully changed"), HttpStatus.OK);
    }

    private String extractJwtFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
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
