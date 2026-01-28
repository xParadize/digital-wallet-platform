package com.wallet.transactionservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.dto.ApiResponse;
import com.wallet.transactionservice.dto.InputFieldError;
import com.wallet.transactionservice.dto.ValidationErrorResponse;
import com.wallet.transactionservice.exception.*;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@AllArgsConstructor
public class TransactionControllerAdvice {
    private final String ERROR_MESSAGE_FIELD = "message";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(IncorrectSearchPath.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleIncorrectSearchPath() {
        ApiResponse response = new ApiResponse(false, "There's noting here," +
                "try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleCardNotFoundException(CardNotFoundException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentOfferEntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handlePaymentOfferEntityNotFoundException(PaymentOfferEntityNotFoundException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectTimePeriodException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleIncorrectTimePeriodException(IncorrectTimePeriodException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CardAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse> handleCardAccessDeniedException(CardAccessDeniedException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CardLimitExceededException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse> handleCardLimitExceededException(CardLimitExceededException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PaymentOfferNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handlePaymentOfferNotFoundException(PaymentOfferNotFoundException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleTransactionNotFoundException(TransactionNotFoundException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse> handleInvalidAuthorizationException(InvalidAuthorizationException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<InputFieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> new InputFieldError(entry.getKey(), entry.getValue()))
                .toList();

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                false,
                "Validation failed",
                errors,
                Instant.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse> handleFeeException(FeeException e) {
        ApiResponse response = new ApiResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleFeignBadRequest(FeignException.BadRequest e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad request");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse> handleFeignUnauthorized(FeignException.Unauthorized e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Unauthorized access");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FeignException.Forbidden.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse> handleFeignForbidden(FeignException.Forbidden e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Access forbidden");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FeignException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleFeignNotFound(FeignException.NotFound e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Not found");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.TooManyRequests.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<ApiResponse> handleFeignTooManyRequests(FeignException.TooManyRequests e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Too many requests");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(FeignException.InternalServerError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse> handleFeignInternalError(FeignException.InternalServerError e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Internal server error");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignException.BadGateway.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ResponseEntity<ApiResponse> handleFeignBadGateway(FeignException.BadGateway e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad gateway");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(FeignException.ServiceUnavailable.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ApiResponse> handleFeignServiceUnavailable(FeignException.ServiceUnavailable e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Service temporarily unavailable");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse> handleFeignException(FeignException e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad request");
        ApiResponse response = new ApiResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.valueOf(e.status()));
    }
}
