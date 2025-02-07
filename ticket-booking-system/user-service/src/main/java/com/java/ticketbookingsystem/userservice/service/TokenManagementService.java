package com.java.ticketbookingsystem.userservice.service;

import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.TokenHolder;

/**
 * Service interface for managing tokens.
 *
 * This interface defines methods for storing and refreshing tokens.
 *
 * @author Sathwick
 * @since 1.0
 */
public interface TokenManagementService {
    /**
     * Stores the access token, refresh token, and expiry timestamp for a given username.
     *
     * @param username The username associated with the tokens.
     * @param accessToken The access token to store.
     * @param refreshToken The refresh token to store.
     * @param expireInSeconds The expiry timestamp in seconds since epoch.
     */
    void storeTokens(String username, String accessToken, String refreshToken, long expireInSeconds);

    /**
     * Refreshes the tokens for a given username using the provided refresh token.
     *
     * @param username The username associated with the tokens.
     * @param refreshToken The refresh token to use for refreshing.
     */
    AuthenticationResponse refreshTokens(String username, String refreshToken);

    /**
     * Retrieves the tokens for a given username.
     * @param username the username associated with the tokens
     */
    TokenHolder getTokens(String username);
}
