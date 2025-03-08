package com.java.ticketbookingsystem.userservice.service.auth.impl;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.java.ticketbookingsystem.userservice.dto.*;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.auth.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service("firebaseAuthenticationService")
public class FirebaseAuthenticationServiceImpl implements AuthenticationService {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final RestTemplate restTemplate;
    @Value("${firebase.api.key}")
    private String firebaseApiKey;


    public FirebaseAuthenticationServiceImpl(Firestore firestore, FirebaseAuth firebaseAuth, RestTemplate restTemplate) {
        this.firestore = firestore;
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
     * </pre>
     *
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

    /**
     * Registers a new user in Cloud.
     *
     * @param request RegistrationRequest
     * @return AuthenticationResponse
     */
    @Override
    public AuthenticationResponse signUp(RegistrationRequest request) {
        try {
            // Create Firebase user
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getName());

            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            // Determine default role or use provided role
            String defaultRole = assignDefaultRole(request);

            // Store additional details in Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", request.getEmail());
            userData.put("name", request.getName());
            userData.put("phoneNumber", request.getPhoneNumber());
            userData.put("gender", request.getGender());
            userData.put("roles", Collections.singletonList(defaultRole));
            userData.put("username", request.getUsername());

            firestore.collection("users").document(userRecord.getUid()).set(userData).get();

            // Set custom claims for roles
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", Collections.singletonList(defaultRole));
            firebaseAuth.setCustomUserClaims(userRecord.getUid(), claims);

            // Generate custom token
            String customToken = firebaseAuth.createCustomToken(userRecord.getUid());

            return exchangeCustomTokenForIdToken(customToken);
        } catch (Exception e) {
            log.error("Signup failed: {}", e.getMessage());
            throw new TBSUserServiceException("User registration failed " + e.getMessage());
        }
    }

    private String assignDefaultRole(RegistrationRequest request) {
        return Objects.isNull(request.getRole()) ? "USER" : request.getRole().name();
    }

    /**
     * Authenticates a user by verifying their credentials.
     *
     * @param signInRequest The authentication request containing username and password
     * @param sessionId     The session ID
     * @return AuthenticationResponse containing token and refresh token
     * @throws TBSUserServiceException if authentication fails
     */
    @Override
    public AuthenticationResponse signIn(AuthenticationRequest signInRequest, String sessionId) {
        try {
            log.info("Sign-in attempt for username/email: {}", signInRequest.getUsername() + " " + signInRequest.getEmail());

            UserRecord userRecord = firebaseAuth.getUserByEmail(signInRequest.getEmail());
            // In production, implement proper password verification flow
            // This would typically be handled client-side with Firebase SDK

            String customToken = firebaseAuth.createCustomToken(userRecord.getUid());

            log.info("Successful sign-in for user: {}", userRecord.getUid());
            return exchangeCustomTokenForIdToken(customToken);
        } catch (FirebaseAuthException e) {
            log.error("Authentication failed for username: {} - {}",
                    signInRequest.getUsername(), e.getMessage());
            throw new TBSUserServiceException("Authentication failed", e);
        }
    }

    /**
     * Refreshes a user's authentication token.
     *
     * @param expiredToken refresh token
     * @param sessionId    The session ID
     * @return TokenResponse
     */
    @Override
    public TokenResponse refreshToken(String expiredToken, String sessionId) {
        try {
            // Verify token even if expired
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(expiredToken, true);

            // Check if user exists
            UserRecord user = firebaseAuth.getUser(decodedToken.getUid());

            // Generate new custom token
            String newToken = firebaseAuth.createCustomToken(user.getUid());
            log.info("Exchanging new custom token for Id Token Post Refresh");

            // Update tokens in the cache
            AuthenticationResponse response = exchangeCustomTokenForIdToken(newToken);
            return new TokenResponse(response.getToken());
        } catch (FirebaseAuthException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new TBSUserServiceException("Token refresh failed, please signin again - " + e.getMessage());
        }
    }

    /**
     * Signs out a user by revoking their refresh token.
     *
     * @param userId username
     * @throws TBSUserServiceException if signing out fails
     */
    @Override
    public void signOut(String userId, String sessionId) {
        try {
            log.info("Signing out user: {}", userId);
            firebaseAuth.revokeRefreshTokens(userId);
            log.info("Successfully revoked tokens for user: {}", userId);
        } catch (FirebaseAuthException e) {
            log.error("Failed to sign out user: {} - {}", userId, e.getMessage());
            throw new TBSUserServiceException("Sign out failed", e);
        }
    }
}
