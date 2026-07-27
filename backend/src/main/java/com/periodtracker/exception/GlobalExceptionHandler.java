package com.periodtracker.exception;

import com.periodtracker.dto.ErrorResponse;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(new ErrorResponse.ErrorBody(
                        "NOT_FOUND", ex.getMessage(), null)));
    }

    @ExceptionHandler(OnboardingRequiredException.class)
    public ResponseEntity<ErrorResponse> handleOnboardingRequired(OnboardingRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(new ErrorResponse.ErrorBody(
                        "ONBOARDING_REQUIRED", ex.getMessage(),
                        Map.of("userId", ex.getUserId()))));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(new ErrorResponse.ErrorBody(
                        "VALIDATION_ERROR", ex.getMessage(), null)));
    }
}
