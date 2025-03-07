package com.java.ticketbookingsystem.userservice.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationRequest;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.TokenResponse;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.AuthenticationService;
import com.java.ticketbookingsystem.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service("firebaseAuthenticationService")
public class FirebaseAuthenticationServiceImpl implements AuthenticationService {

    @Value("${firebase.api.key}")
    private String firebaseApiKey;
    private final UserService userService;
    private final FirebaseAuth firebaseAuth;
    private final RestTemplate restTemplate;

    public FirebaseAuthenticationServiceImpl(@Qualifier("firebaseUserService") UserService userService, FirebaseAuth firebaseAuth, RestTemplate restTemplate) {
        this.userService = userService;
        this.firebaseAuth = firebaseAuth;
        this.restTemplate = restTemplate;
    }

    /**
     * Note:- Custom Tokens ≠ ID Tokens
     * <br>
     * Firebase Security Architecture:
     * <p>
     * Custom Tokens: Generated server-side via Admin SDK for client-side authentication
     * <p>
     * ID Tokens: Generated client-side after Firebase authentication
     * <p>
     * Mixing these breaks Firebase's security model
     *
     * <pre>
     * sequenceDiagram
     * - Client->>Backend: Sign-in Request (email/password)
     * - Backend->>Firebase: Verify credentials
     * - Backend->>Client: Return Custom Token
     * - Client->>Firebase: Exchange Custom Token
     * - Firebase->>Client: ID Token + Refresh Token
     * - Client->>Backend: API Requests with ID Token
     * - Backend->>Firebase: Verify ID Token
     *</pre>
     * @param customToken Sign-in credentials Custom Token
     * @return a JSON response containing access and refresh tokens
     */
    private AuthenticationResponse exchangeCustomTokenForIdToken(String customToken) {
        try {
            log.info("Exchanging custom token for ID token");
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key="
                    + firebaseApiKey;

            Map<String, String> request = Map.of(
                    "token", customToken,
                    "returnSecureToken", "true"
            );
            var response = restTemplate.postForEntity(url, request, Map.class);
            log.debug("Token exchange response: {}", response.getBody());
            String idToken = (String) response.getBody().get("idToken");
            String refreshToken = (String) response.getBody().get("refreshToken");
            return AuthenticationResponse.builder()
                    .token(idToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            log.error("Failed to exchange custom token for ID token: {}", e.getMessage());
            throw new TBSUserServiceException("Token exchange failed");
        }
    }

    @Override
    public AuthenticationResponse signIn(AuthenticationRequest signInRequest, String sessionId) {
        return exchangeCustomTokenForIdToken(
                userService
                        .signIn(signInRequest, sessionId)
                        .getToken()
        );
    }

    @Override
    public TokenResponse refreshToken(String expiredToken) {
        try {
            // Verify token even if expired
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(expiredToken, true);

            // Check if user exists
            UserRecord user = firebaseAuth.getUser(decodedToken.getUid());

            // Generate new custom token
            String newToken = firebaseAuth.createCustomToken(user.getUid());
            log.info("Exchanging new custom token for Id Token Post Refresh");
            return new TokenResponse(exchangeCustomTokenForIdToken(newToken).getToken());
        } catch (
                FirebaseAuthException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new TBSUserServiceException("Token refresh failed, please signin again - " + e.getMessage());
        }
    }
}
