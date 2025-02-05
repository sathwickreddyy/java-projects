package com.java.ticketbookingsystem.userservice.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a standardized error response for the Ticket Booking System.
 * This class provides a consistent structure for all error responses
 * returned by the application's REST endpoints.
 */
@Data
@Builder
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String message;
}