package com.java.ticketbookingsystem.userservice.configuration;

import com.java.ticketbookingsystem.userservice.dto.ApiErrorResponse;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * Global exception handler for the Ticket Booking System's user service.
 * This class provides centralized exception handling across all controllers
 * in the application, ensuring consistent error responses and logging.
 * <p>
 * The handler manages various types of exceptions:
 * - Service-specific exceptions (TBSUserServiceException)
 * - Security exceptions (Authentication and Authorization)
 * - Validation exceptions (Method argument validation)
 * <p>
 * Each exception is logged appropriately and transformed into a standardized
 * error response format for the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * Handles service-specific exceptions thrown by the user service.
     * These exceptions typically represent business logic violations or
     * external service integration failures.
     *
     * @param ex The service exception that was thrown
     * @return ResponseEntity containing error details with HTTP 400 status
     */
    @ExceptionHandler(TBSUserServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceException(TBSUserServiceException ex) {
        log.error("Service exception occurred: {}", ex.getMessage(), ex);

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .code("USER_SERVICE_ERROR")
                .message(ex.getMessage())
                .build();

        log.debug("Generated error response: {}", error);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles security-related exceptions including both authentication and authorization failures.
     * This provides a unified handling of security exceptions while maintaining security by
     * not exposing detailed error information to clients.
     *
     * @param ex The security exception that was thrown
     * @return ResponseEntity containing error details with HTTP 403 status
     */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthException(Exception ex) {
        log.warn("Security exception occurred: {}, Type: {}",
                ex.getMessage(), ex.getClass().getSimpleName());

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .code("AUTH_ERROR")
                .message("Authentication/Authorization failed")
                .build();

        // Log the detailed error for debugging but return a generic message to the client
        log.debug("Authentication error details: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles validation exceptions thrown when request parameters or body fail
     * validation constraints. This method overrides the default Spring validation
     * handling to provide custom error responses.
     *
     * @param ex      The validation exception containing binding errors
     * @param headers The headers to be written to the response
     * @param status  The selected response status
     * @param request The current request
     * @return ResponseEntity containing validation error details
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation failed: {}", errorMessage);

        // Log detailed validation errors for debugging
        ex.getBindingResult().getAllErrors().forEach(error ->
                log.debug("Validation error: {}", error)
        );

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_ERROR")
                .message(errorMessage)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}