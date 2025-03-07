package com.java.ticketbookingsystem.userservice.service.tokens.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.java.ticketbookingsystem.userservice.dto.TokenHolder;
import com.java.ticketbookingsystem.userservice.service.tokens.TokenManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("firebaseTokenManagementService")
public class FirebaseTokenManagementServiceImpl implements TokenManagementService {

    private final Cache<String, Map<String, TokenHolder>> usersSessionsTokenCache;

    public FirebaseTokenManagementServiceImpl(Cache<String, Map<String, TokenHolder>> usersSessionsTokenCache) {
        this.usersSessionsTokenCache = usersSessionsTokenCache;
    }

    /**
     * Stores the access token, refresh token, and expiry timestamp for a given userId.
     *
     * @param sessionId       The session ID associated with the tokens.
     * @param userId          The userId associated with the tokens.
     * @param accessToken     The access token to store.
     * @param refreshToken    The refresh token to store.
     * @param expireInSeconds The expiry timestamp in seconds since epoch.
     */
    @Override
    public void storeTokens(String sessionId, String userId, String accessToken, String refreshToken, long expireInSeconds) {
        Map<String, TokenHolder> userSessions = usersSessionsTokenCache.getIfPresent(userId);
        if (userSessions == null) {
            userSessions = new ConcurrentHashMap<>();
            usersSessionsTokenCache.put(userId, userSessions);
        }

        userSessions.put(sessionId, TokenHolder.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiryTimestamp(expireInSeconds)
                .build());

        log.info("Stored tokens for user: {}, session: {}", userId, sessionId);
    }

    /**
     * Retrieves the tokens for a given userId.
     *
     * @param userId the userId associated with the tokens
     * @param sessionId the sessionId associated with the tokens
     *
     * @return TokenHolder
     */
    @Override
    public Optional<TokenHolder> getTokens(String userId, String sessionId) {
        Map<String, TokenHolder> sessionsMap = usersSessionsTokenCache.getIfPresent(userId);
        return sessionsMap != null ? Optional.ofNullable(sessionsMap.get(sessionId)) : Optional.empty();
    }

    /**
     * Invalidates the tokens for a given userId.
     *
     * @param userId the userId associated with the tokens
     */
    @Override
    public void invalidateTokens(String userId) {
        usersSessionsTokenCache.invalidate(userId);
        log.info("Invalidated tokens for user: {}", userId);
    }

    /**
     * Invalidates the session for a given sessionId.
     *
     * @param userId    the userId associated with the user
     * @param sessionId the sessionId associated with the tokens
     */
    @Override
    public void invalidateSession(String userId, String sessionId) {
        // TODO
    }
}
