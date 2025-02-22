package com.java.ticketbookingsystem.userservice.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.CognitoUserPoolDetails;
import com.java.ticketbookingsystem.userservice.dto.TokenHolder;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.TokenManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenManagementServiceImpl implements TokenManagementService {

    private final CognitoUserPoolDetails cognitoUserPoolDetails;
    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final Cache<String, Map<String, TokenHolder>> usersSessionsTokenCache;

    public TokenManagementServiceImpl(CognitoUserPoolDetails cognitoUserPoolDetails, CognitoIdentityProviderClient cognitoIdentityProviderClient, Cache<String, Map<String, TokenHolder>> usersSessionsTokenCache) {
        this.cognitoUserPoolDetails = cognitoUserPoolDetails;
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.usersSessionsTokenCache = usersSessionsTokenCache;
    }

    /**
     * Stores the tokens for the specified username.
     * @param username the Cognito username
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @param expiresInSeconds expiration (in seconds) provided by Cognito
     */
    @Override
    public void storeTokens(String sessionId, String username, String accessToken, String refreshToken, long expiresInSeconds) {
        log.debug("Storing token with key: {}", username);
        TokenHolder tokenHolder = new TokenHolder();
        tokenHolder.setAccessToken(accessToken);
        tokenHolder.setRefreshToken(refreshToken);
        tokenHolder.setExpiryTimestamp(Instant.now().getEpochSecond() + expiresInSeconds);
        tokenHolder.setSessionId(sessionId);

        usersSessionsTokenCache.asMap().compute(username, (key, existingSessionsMap) -> {
            Map<String, TokenHolder> sessions = existingSessionsMap != null ? existingSessionsMap : new ConcurrentHashMap<>();
            sessions.put(sessionId, tokenHolder);
            return sessions;
        });

        log.info("Stored tokens for user: {}, session: {}", username, sessionId);
    }

    /**
     * Refreshes tokens by calling Cognito’s REFRESH_TOKEN_AUTH flow.
     * @param username the username whose token is to be refreshed
     * @param refreshToken the refresh token (from request)
     * @return a new AuthenticationResponse containing fresh tokens
     */
    @Override
    public AuthenticationResponse refreshTokens(String sessionId, String username, String refreshToken) {
        try {
            AdminInitiateAuthResponse authResponse = cognitoIdentityProviderClient.adminInitiateAuth(
                    AdminInitiateAuthRequest.builder()
                            .userPoolId(cognitoUserPoolDetails.getUserPoolId())
                            .clientId(cognitoUserPoolDetails.getClientId())
                            .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                            .authParameters(Map.of("REFRESH_TOKEN", refreshToken))
                            .build()
            );
            String newAccessToken = authResponse.authenticationResult().accessToken();
            // Cognito may or may not return a new refresh token; if not, reuse the existing one.
            String newRefreshToken = authResponse.authenticationResult().refreshToken() != null ?
                    authResponse.authenticationResult().refreshToken() : refreshToken;
            long expiresIn = authResponse.authenticationResult().expiresIn();

            // Update cache with the new tokens.
            storeTokens(sessionId, username, newAccessToken, newRefreshToken, expiresIn);

            return new AuthenticationResponse(newAccessToken, newRefreshToken);
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new TBSUserServiceException("Failed to refresh token", e);
        }
    }

    /**
     * Retrieves the tokens for a given username.
     *
     * @param username the username associated with the tokens
     */
    @Override
    public TokenHolder getTokens(String username) {
        Map<String, TokenHolder> sessions = usersSessionsTokenCache.getIfPresent(username);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("No active sessions found for user: {}", username);
            return null;
        }
        log.info("Retrieved tokens for user: {}", username);
        return sessions.values().iterator().next(); // Return the first active session
    }

    /**
     * Invalidates the tokens for a given username.
     *
     * @param username the username associated with the tokens
     */
    @Override
    public void invalidateTokens(String username) {
        log.debug("Invalidating token with key: {}", username);
        usersSessionsTokenCache.invalidate(username);
        log.info("Token invalidated successfully with key: {}", username);
    }

    /**
     * Invalidates the session for a given username.
     *
     * @param username the username associated with the session
     * @param sessionId the session id
     */
    @Override
    public void invalidateSession(String username, String sessionId) {
        log.info("Invalidating session {} for user: {}", sessionId, username);
        usersSessionsTokenCache.asMap().computeIfPresent(username, (key, sessions) -> {
            sessions.remove(sessionId);
            return sessions.isEmpty() ? null : sessions; // Remove user if no active sessions remain
        });
    }
}
