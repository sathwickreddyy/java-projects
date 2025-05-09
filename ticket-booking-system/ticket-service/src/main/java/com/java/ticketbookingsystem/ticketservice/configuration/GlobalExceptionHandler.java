package com.java.ticketbookingsystem.ticketservice.configuration;

import com.java.ticketbookingsystem.ticketservice.exception.TicketServiceException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * This class captures and returns standardized error responses
 * for known and unknown exceptions that occur during request processing.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions thrown when validation on an argument annotated with @Valid fails.
     *
     * @param ex the exception thrown due to validation failure
     * @return a ResponseEntity containing a list of validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handles javax.validation.ValidationException.
     *
     * @param ex the validation exception
     * @return a ResponseEntity containing the exception message
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + ex.getMessage());
    }

    /**
     * Handles custom TicketServiceException.
     *
     * @param ex the custom application exception
     * @return a ResponseEntity with the error message and BAD_REQUEST status
     */
    @ExceptionHandler(TicketServiceException.class)
    public ResponseEntity<String> handleTicketServiceException(TicketServiceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles any unhandled runtime exceptions.
     *
     * @param ex the exception
     * @return a ResponseEntity with a generic message and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Optional: Log the stack trace with a logger instead of returning full exception
        log.error("An unexpected error occurred.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
    }
}