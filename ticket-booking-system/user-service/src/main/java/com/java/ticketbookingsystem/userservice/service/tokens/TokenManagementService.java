package com.java.ticketbookingsystem.userservice.service.tokens;

import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.TokenHolder;

import java.util.Optional;

/**
 * Service interface for managing tokens.
 * <p>
 * This interface defines methods for storing and refreshing tokens.
 *
 * @author Sathwick
 * @since 1.0
 */
public interface TokenManagementService {
    /**
     * Stores the access token, refresh token, and expiry timestamp for a given userId.
     *
     * @param sessionId       The session ID associated with the tokens.
     * @param userId          The userId associated with the tokens.
     * @param accessToken     The access token to store.
     * @param refreshToken    The refresh token to store.
     * @param expireInSeconds The expiry timestamp in seconds since epoch.
     */
    void storeTokens(String sessionId, String userId, String accessToken, String refreshToken, long expireInSeconds);

    /**
     * Retrieves the tokens for a given userId.
     *
     * @param userId the userId associated with the tokens
     * @param sessionId the sessionId associated with the tokens
     */
    Optional<TokenHolder> getTokens(String userId, String sessionId);

    /**
     * Invalidates the tokens for a given userId.
     *
     * @param userId the userId associated with the tokens
     */
    void invalidateTokens(String userId);

    /**
     * Invalidates the session for a given sessionId.
     *
     * @param userId    the userId associated with the user
     * @param sessionId the sessionId associated with the tokens
     */
    void invalidateSession(String userId, String sessionId);
}
