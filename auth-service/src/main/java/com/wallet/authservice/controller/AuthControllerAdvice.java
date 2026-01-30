package com.wallet.authservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.authservice.dto.ApiStatusResponse;
import com.wallet.authservice.dto.ValidationErrorResponse;
import com.wallet.authservice.exception.*;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
@AllArgsConstructor
public class AuthControllerAdvice {
    private final String ERROR_MESSAGE_FIELD = "message";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(IncorrectSearchPath.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handleIncorrectSearchPath() {
        ApiStatusResponse response = new ApiStatusResponse(false, "There's noting here," +
                "try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConfirmationTokenException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ResponseEntity<ApiStatusResponse> handleConfirmationTokenException() {
        ApiStatusResponse response = new ApiStatusResponse(false, "The code has expired. Request a new registration code.");
        return new ResponseEntity<>(response, HttpStatus.GONE);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handleIncorrectPasswordException(IncorrectPasswordException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiStatusResponse> handleInvalidAuthorizationException(InvalidAuthorizationException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FieldValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleFieldValidationException(FieldValidationException e) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                false,
                e.getMessage(),
                e.getErrors(),
                Instant.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handlePasswordMismatchException(PasswordMismatchException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handleUserNotFoundException(UserNotFoundException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handleFeignBadRequest(FeignException.BadRequest e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad request");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiStatusResponse> handleFeignUnauthorized(FeignException.Unauthorized e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Unauthorized access");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FeignException.Forbidden.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiStatusResponse> handleFeignForbidden(FeignException.Forbidden e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Access forbidden");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FeignException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handleFeignNotFound(FeignException.NotFound e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Not found");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.TooManyRequests.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<ApiStatusResponse> handleFeignTooManyRequests(FeignException.TooManyRequests e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Too many requests");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(FeignException.InternalServerError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiStatusResponse> handleFeignInternalError(FeignException.InternalServerError e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Internal server error");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignException.BadGateway.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ResponseEntity<ApiStatusResponse> handleFeignBadGateway(FeignException.BadGateway e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad gateway");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(FeignException.ServiceUnavailable.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ApiStatusResponse> handleFeignServiceUnavailable(FeignException.ServiceUnavailable e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Service temporarily unavailable");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiStatusResponse> handleFeignException(FeignException e) {
        String originalMessage = "Bad request";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (e.status() == -1) {
            originalMessage = "Service temporarily unavailable";
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            try {
                if (e.contentUTF8() != null && !e.contentUTF8().isEmpty()) {
                    JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
                    originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad request");
                }
                httpStatus = HttpStatus.valueOf(e.status());
            } catch (Exception ex) {
                originalMessage = "Service error occurred";
            }
        }

        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, httpStatus);
    }
}
