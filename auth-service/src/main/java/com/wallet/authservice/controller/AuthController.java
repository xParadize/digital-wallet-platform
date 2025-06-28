package com.wallet.authservice.controller;

import com.wallet.authservice.dto.ApiResponse;
import com.wallet.authservice.dto.SignUpRequest;
import com.wallet.authservice.exception.IncorrectSearchPath;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getInputFieldErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        if (Objects.equals(signUpRequest.getPassword(), signUpRequest.getPasswordConfirmation())) {
            try {
                System.out.println(signUpRequest);
                return new ResponseEntity<>(new ApiResponse(true, "Please, confirm your email"), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(new ApiResponse(false, "Registration error: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Passwords don't match"), HttpStatus.BAD_REQUEST);
        }
    }

    private String getInputFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
    }
}
