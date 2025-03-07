package com.java.ticketbookingsystem.userservice.service.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationRequest;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.RegistrationRequest;
import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("firebaseUserService")
public class FirebaseUserServiceImpl implements UserService {

    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    public FirebaseUserServiceImpl(FirebaseAuth firebaseAuth, Firestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
        log.info("FirebaseUserServiceImpl initialized");
    }

    /**
     * Retrieves detailed user information from Cognito.
     *
     * @param uid The unique identifier of the user
     * @return UserDetails object containing user information
     * @throws TBSUserServiceException if there's an error fetching user details
     */
    @Override
    public UserDetails getUserDetails(String uid) {
        try {
            log.info("Fetching user details for {}", uid);
            DocumentSnapshot doc = firestore.collection("users").document(uid).get().get();
            return UserDetails.builder()
                    .username(doc.getString("username"))
                    .email(doc.getString("email"))
                    .name(doc.getString("name"))
                    .phoneNumber(doc.getString("phoneNumber"))
                    .gender(doc.getString("gender"))
                    .authorities(mapRolesToAuthorities(doc.get("roles")))
                    .build();
        } catch (Exception e) {
            log.error("Error fetching user details: {}", e.getMessage());
            throw new TBSUserServiceException("User details fetch failed");
        }
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Object roles) {
        if (roles instanceof List) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Updates the role of a specified user in Cognito.
     *
     * @param uid The unique identifier of the user
     * @param role     The new role to be assigned
     * @throws TBSUserServiceException if there's an error updating the user role
     */
    @Override
    public void updateUserRole(String uid, UserDetails.UserRole role) {
        try {
            log.info("Updating role for {} to {}", uid, role);

            // Update Firestore document
            Map<String, Object> updates = new HashMap<>();
            updates.put("roles", Collections.singletonList(role.name()));
            firestore.collection("users").document(uid).update(updates).get();

            // Update custom claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", Collections.singletonList(role.name()));
            firebaseAuth.setCustomUserClaims(uid, claims);
        } catch (Exception e) {
            log.error("Role update failed: {}", e.getMessage());
            throw new TBSUserServiceException("Role update failed", e);
        }
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
            log.info("Sign-in attempt for username/email: {}", signInRequest.getUsername()+" "+signInRequest.getEmail());

            UserRecord userRecord = firebaseAuth.getUserByEmail(signInRequest.getEmail());
            // In production, implement proper password verification flow
            // This would typically be handled client-side with Firebase SDK

            String customToken = firebaseAuth.createCustomToken(userRecord.getUid());

            log.info("Successful sign-in for user: {}", userRecord.getUid());
            return AuthenticationResponse.builder()
                    .token(customToken)
                    .refreshToken("") // Implement refresh token logic as needed
                    .build();
        } catch (FirebaseAuthException e) {
            log.error("Authentication failed for username: {} - {}",
                    signInRequest.getUsername(), e.getMessage());
            throw new TBSUserServiceException("Authentication failed", e);
        }
    }

    /**
     * Signs out a user by revoking their refresh token.
     *
     * @param userId username
     * @throws TBSUserServiceException if signing out fails
     */
    @Override
    public void signOut(String userId) {
        try {
            log.info("Signing out user: {}", userId);
            firebaseAuth.revokeRefreshTokens(userId);
            log.info("Successfully revoked tokens for user: {}", userId);
        } catch (FirebaseAuthException e) {
            log.error("Failed to sign out user: {} - {}", userId, e.getMessage());
            throw new TBSUserServiceException("Sign out failed", e);
        }
    }

    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDetails object representing the authenticated user
     */
    @Override
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof FirebaseToken) {
                return ((FirebaseToken) principal).getUid();
            }
            return principal.toString();
        }
        return "anonymousUser";
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

            // Store additional details in Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", request.getEmail());
            userData.put("name", request.getName());
            userData.put("phoneNumber", request.getPhoneNumber());
            userData.put("gender", request.getGender());
            userData.put("roles", Collections.singletonList(assignDefaultRole(request)));
            userData.put("username", request.getUsername());

            firestore.collection("users").document(userRecord.getUid()).set(userData).get();

            // Generate custom token
            String customToken = firebaseAuth.createCustomToken(userRecord.getUid());

            return AuthenticationResponse.builder()
                    .token(customToken)
                    .refreshToken("") // Implement refresh token logic if needed
                    .build();

        } catch (Exception e) {
            log.error("Signup failed: {}", e.getMessage());
            throw new TBSUserServiceException("User registration failed "+e.getMessage());
        }
    }

    private String assignDefaultRole(RegistrationRequest request) {
        return request.getRole() != null &&
                request.getRole() == UserDetails.UserRole.OWNER ?
                "OWNER" : "USER";
    }

}
