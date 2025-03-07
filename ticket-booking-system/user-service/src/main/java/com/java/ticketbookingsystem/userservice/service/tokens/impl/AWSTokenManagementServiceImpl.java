package com.java.ticketbookingsystem.userservice.service.tokens.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.CognitoUserPoolDetails;
import com.java.ticketbookingsystem.userservice.dto.TokenHolder;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.tokens.TokenManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AWSTokenManagementServiceImpl implements TokenManagementService {

    private final CognitoUserPoolDetails cognitoUserPoolDetails;
    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final Cache<String, Map<String, TokenHolder>> usersSessionsTokenCache;

    public AWSTokenManagementServiceImpl(CognitoUserPoolDetails cognitoUserPoolDetails, CognitoIdentityProviderClient cognitoIdentityProviderClient, Cache<String, Map<String, TokenHolder>> usersSessionsTokenCache) {
        this.cognitoUserPoolDetails = cognitoUserPoolDetails;
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.usersSessionsTokenCache = usersSessionsTokenCache;
    }

    /**
     * Stores the tokens for the specified username.
     *
     * @param username         the Cognito username
     * @param accessToken      the access token
     * @param refreshToken     the refresh token
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
     * Retrieves the tokens for a given username.
     *
     * @param username the username associated with the tokens
     */
    @Override
    public Optional<TokenHolder> getTokens(String username, String sessionId) {
        Map<String, TokenHolder> sessions = usersSessionsTokenCache.getIfPresent(username);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("No active sessions found for user: {}", username);
            return null;
        }
        log.info("Retrieved tokens for user: {}", username);
//        return   sessions.values().iterator().next(); // Return the first active session
        return Optional.ofNullable(sessions.get(sessionId));
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
     * @param username  the username associated with the session
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
