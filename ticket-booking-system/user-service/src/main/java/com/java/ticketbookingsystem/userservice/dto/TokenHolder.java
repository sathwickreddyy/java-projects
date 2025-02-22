package com.java.ticketbookingsystem.userservice.dto;

import lombok.Data;


/**
 * Data Transfer Object (DTO) representing a token holder.
 * This class encapsulates the access token, refresh token, and expiry timestamp
 * of a token holder.
 */
@Data
public class TokenHolder {
    /**
     * The access token.
     */
    private String accessToken;
    /**
     * The refresh token.
     */
    private String refreshToken;
    /**
     * The expiry timestamp in seconds since epoch.
     */
    private long expiryTimestamp;
    /**
     * Session ID of the user
     */
    private String sessionId;
}
