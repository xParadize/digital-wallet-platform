package com.wallet.authservice.controller;

import com.wallet.authservice.dto.ApiResponse;
import com.wallet.authservice.dto.InputFieldError;
import com.wallet.authservice.dto.SignUpRequest;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.IncorrectSearchPath;
import com.wallet.authservice.service.AuthService;
import com.wallet.authservice.service.UnverifiedUserService;
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

    @Value("${unverified-user.ttl.sec}")
    long unverifiedUserTtl;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            List<InputFieldError> fieldErrors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }

        if (Objects.equals(signUpRequest.getPassword(), signUpRequest.getPasswordConfirmation())) {
            if (unverifiedUserService.existsByEmailOrPhone(signUpRequest.getEmail(), signUpRequest.getPhone())) {
                return new ResponseEntity<>(new ApiResponse(false, "User with this email address or phone number already exists"), HttpStatus.BAD_REQUEST);
            } else {
                unverifiedUserService.saveUnverifiedUser(signUpRequest);
                return new ResponseEntity<>(new ApiResponse(true, String.format("Please, confirm your email within %s minutes", unverifiedUserTtl / 60)), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Passwords don't match"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/confirm-email/{code}")
    public ResponseEntity<?> confirmEmail(@PathVariable("code") String code) {
        UnverifiedUser unverifiedUser = authService.confirmEmailToken(code);
        String authResponse = unverifiedUserService.enableUser(unverifiedUser);
        return ResponseEntity.ok(new ApiResponse(true, authResponse));
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
