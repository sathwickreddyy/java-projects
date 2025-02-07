package com.java.ticketbookingsystem.userservice.service.impl;

import com.java.ticketbookingsystem.userservice.dto.*;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.TokenManagementService;
import com.java.ticketbookingsystem.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface managing user operations via AWS Cognito.
 * <p>
 * This service handles:
 * <ul>
 *   <li>Retrieval of user details from Cognito</li>
 *   <li>Updating user roles</li>
 *   <li>User authentication (sign in/out) and token storage</li>
 *   <li>Fetching details for the currently authenticated user using the SecurityContextHolder</li>
 * </ul>
 * </p>
 *
 * @see UserService
 * @see CognitoIdentityProviderClient
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoUserPoolDetails userPoolDetails;
    private final TokenManagementService tokenManagementService;

    /**
     * Constructs a new UserServiceImpl with AWS Cognito configurations.
     *
     * @param cognitoClient         the AWS Cognito client for user operations.
     * @param userPoolDetails       the Cognito user pool configuration details.
     * @param tokenManagementService service responsible for token storage/refresh.
     * @throws IllegalArgumentException if the cognitoClient is null.
     */
    public UserServiceImpl(CognitoIdentityProviderClient cognitoClient,
                           CognitoUserPoolDetails userPoolDetails,
                           TokenManagementService tokenManagementService) {
        if (cognitoClient == null) {
            log.error("CognitoIdentityProviderClient cannot be null");
            throw new IllegalArgumentException("CognitoIdentityProviderClient is required");
        }
        this.cognitoClient = cognitoClient;
        this.userPoolDetails = userPoolDetails;
        this.tokenManagementService = tokenManagementService;
        log.info("UserServiceImpl initialized successfully with Cognito client");
    }

    /**
     * Retrieves the detailed profile information for a user from Cognito.
     *
     * @param username the unique identifier for the user.
     * @return a UserDetails object containing attributes like email, name, phone, gender, and authorities.
     * @throws TBSUserServiceException if an error occurs fetching data from Cognito.
     */
    @Override
    public UserDetails getUserDetails(String username) {
        log.info("Retrieving user details for username: {}", username);

        try {
            // Fetch user profile from Cognito.
            AdminGetUserResponse response = cognitoClient.adminGetUser(r -> r
                    .userPoolId(userPoolDetails.getUserPoolId())
                    .username(username));
            log.debug("Successfully retrieved profile for username: {}", username);

            // Build and return a complete UserDetails instance.
            UserDetails userDetails = UserDetails.builder()
                    .username(username)
                    .email(getAttribute(response, "email"))
                    .name(getAttribute(response, "name"))
                    .phoneNumber(getAttribute(response, "phone_number"))
                    .gender(getAttribute(response, "gender"))
                    .authorities(getUserRoles(username))
                    .build();

            log.info("Compiled user details for username: {}", username);
            return userDetails;
        } catch (CognitoIdentityProviderException e) {
            log.error("Error retrieving user details for {}: {}", username, e.getMessage());
            throw new TBSUserServiceException("Error retrieving user details from Cognito", e);
        }
    }

    /**
     * Retrieves details for the currently authenticated user.
     * <p>
     * This method leverages Spring Security's SecurityContextHolder to determine the current user
     * and then returns full profile details from Cognito.
     * </p>
     *
     * @return a UserDetails object for the current authenticated user.
     * @throws TBSUserServiceException if no authenticated user is found or an error occurs.
     */
    @Override
    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in SecurityContext");
            throw new TBSUserServiceException("No authenticated user found");
        }
        String username = authentication.getName();
        log.info("Current authenticated user: {}", username);
        return getUserDetails(username);
    }

    /**
     * Updates a user's role in Cognito by modifying the custom role attribute.
     *
     * @param username the username whose role is being updated.
     * @param role     the new role to assign.
     * @throws TBSUserServiceException if user role update fails.
     */
    @Override
    public void updateUserRole(String username, UserDetails.UserRole role) {
        log.info("Updating role for user: {} to role: {}", username, role);
        try {
            AttributeType roleAttribute = AttributeType.builder()
                    .name(userPoolDetails.getUserRoleAttribute())
                    .value(role.name())
                    .build();
            cognitoClient.adminUpdateUserAttributes(r -> r
                    .userPoolId(userPoolDetails.getUserPoolId())
                    .username(username)
                    .userAttributes(roleAttribute));
            log.info("Successfully updated role for user: {} to {}", username, role);
        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to update role for user: {}. Error: {}", username, e.getMessage());
            throw new TBSUserServiceException("Failed to update user role in Cognito", e);
        }
    }

    /**
     * Authenticates a user using Cognito's USER_PASSWORD_AUTH flow.
     * On successful authentication, tokens are stored for future expiry/refresh.
     *
     * @param signInRequest contains the username and password.
     * @return AuthenticationResponse containing valid access and refresh tokens.
     * @throws TBSUserServiceException if authentication fails.
     */
    @Override
    public AuthenticationResponse signIn(AuthenticationRequest signInRequest) {
        try {
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(r -> r
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(userPoolDetails.getClientId())
                    .authParameters(Map.of(
                            "USERNAME", signInRequest.getUsername(),
                            "PASSWORD", signInRequest.getPassword()
                    )));
            String accessToken = authResponse.authenticationResult().accessToken();
            String refreshToken = authResponse.authenticationResult().refreshToken();
            long expiresIn = authResponse.authenticationResult().expiresIn();

            tokenManagementService.storeTokens(signInRequest.getUsername(), accessToken, refreshToken, expiresIn);
            log.info("User {} signed in successfully.", signInRequest.getUsername());
            return AuthenticationResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (CognitoIdentityProviderException e) {
            log.error("Sign in failed for user {}: {}", signInRequest.getUsername(), e.getMessage());
            throw new TBSUserServiceException("Failed to sign in user", e);
        }
    }

    /**
     * Signs out a user using Cognito's globalSignOut API.
     * Tokens stored in the local cache are also invalidated.
     *
     * @param username the username of the user to sign out.
     * @throws TBSUserServiceException if sign-out fails.
     */
    @Override
    public void signOut(String username) {
        try {
            TokenHolder tokenHolder = tokenManagementService.getTokens(username);
            if (tokenHolder != null) {
                cognitoClient.globalSignOut(r -> r.accessToken(tokenHolder.getAccessToken()));
                tokenManagementService.invalidateTokens(username);
                log.info("User {} signed out successfully.", username);
            } else {
                log.warn("No tokens found for user {}. Sign out skipped.", username);
            }
        } catch (CognitoIdentityProviderException e) {
            log.error("Sign out failed for user {}: {}", username, e.getMessage());
            throw new TBSUserServiceException("Failed to sign out user", e);
        }
    }

    /**
     * Helper method to safely extract an attribute from the Cognito user profile.
     *
     * @param response the Cognito response containing user attributes.
     * @param attrName the name of the attribute.
     * @return the attribute value, or null if absent.
     */
    private String getAttribute(AdminGetUserResponse response, String attrName) {
        return response.userAttributes().stream()
                .filter(attr -> attr.name().equals(attrName))
                .findFirst()
                .map(AttributeType::value)
                .orElse(null);
    }

    /**
     * Retrieves the Cognito groups for a user and maps them to Spring Security authorities.
     *
     * @param username the username to fetch roles for.
     * @return a collection of GrantedAuthority representing user roles.
     */
    private Collection<? extends GrantedAuthority> getUserRoles(String username) {
        AdminListGroupsForUserResponse response = cognitoClient.adminListGroupsForUser(r -> r
                .userPoolId(userPoolDetails.getUserPoolId())
                .username(username));
        return response.groups().stream()
                .map(group -> new SimpleGrantedAuthority(group.groupName()))
                .collect(Collectors.toList());
    }
}
