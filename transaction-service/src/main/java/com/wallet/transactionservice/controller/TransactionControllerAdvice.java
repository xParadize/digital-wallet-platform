package com.wallet.transactionservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.transactionservice.dto.ApiStatusResponse;
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
    public ResponseEntity<ApiStatusResponse> handleIncorrectSearchPath() {
        ApiStatusResponse response = new ApiStatusResponse(false, "There's noting here," +
                "try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handleCardNotFoundException(CardNotFoundException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentOfferEntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handlePaymentOfferEntityNotFoundException(PaymentOfferEntityNotFoundException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectTimePeriodException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiStatusResponse> handleIncorrectTimePeriodException(IncorrectTimePeriodException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CardAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiStatusResponse> handleCardAccessDeniedException(CardAccessDeniedException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiStatusResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CardLimitExceededException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiStatusResponse> handleCardLimitExceededException(CardLimitExceededException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PaymentOfferNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handlePaymentOfferNotFoundException(PaymentOfferNotFoundException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiStatusResponse> handleTransactionNotFoundException(TransactionNotFoundException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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

    @ExceptionHandler(FeeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiStatusResponse> handleFeeException(FeeException e) {
        ApiStatusResponse response = new ApiStatusResponse(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<ApiStatusResponse> handleFeignException(FeignException e) throws JsonProcessingException {
        JsonNode errorJson = objectMapper.readTree(e.contentUTF8());
        String originalMessage = errorJson.path(ERROR_MESSAGE_FIELD).asText("Bad request");
        ApiStatusResponse response = new ApiStatusResponse(false, originalMessage);
        return new ResponseEntity<>(response, HttpStatus.valueOf(e.status()));
    }
}
