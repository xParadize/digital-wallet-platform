package com.wallet.authservice.controller;

import com.wallet.authservice.dto.ApiResponse;
import com.wallet.authservice.exception.IncorrectSearchPath;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class AuthControllerAdvice {

    @ExceptionHandler(IncorrectSearchPath.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleIncorrectSearchPath() {
        ApiResponse response = new ApiResponse(false, "There's noting here." +
                "Try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
