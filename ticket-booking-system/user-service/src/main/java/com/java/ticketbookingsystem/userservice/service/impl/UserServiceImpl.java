package com.java.ticketbookingsystem.userservice.service.impl;

import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface that manages user operations using AWS Cognito.
 * This service handles user authentication, authorization, and profile management for the
 * ticket booking system.
 *
 * The service integrates with AWS Cognito User Pools to:
 * - Retrieve user profiles and attributes
 * - Manage user roles and permissions
 * - Handle user group memberships
 *
 * @see UserService
 * @see CognitoIdentityProviderClient
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final CognitoIdentityProviderClient cognitoClient;

    /**
     * AWS Cognito User Pool ID configured in application properties.
     * This identifies the user directory for our application.
     */
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    /**
     * Custom attribute name in Cognito that stores user roles.
     * This attribute is used for role-based access control.
     */
    @Value("${aws.cognito.userRoleAttribute}")
    private String userRoleAttribute;

    /**
     * Constructor for UserServiceImpl.
     * Initializes the service with required AWS Cognito configurations.
     *
     * @param cognitoClient AWS Cognito client for user operations
     * @throws IllegalArgumentException if cognitoClient is null
     */
    public UserServiceImpl(CognitoIdentityProviderClient cognitoClient) {
        if (cognitoClient == null) {
            log.error("CognitoIdentityProviderClient cannot be null");
            throw new IllegalArgumentException("CognitoIdentityProviderClient is required");
        }
        this.cognitoClient = cognitoClient;
        log.info("UserService initialized successfully with Cognito client");
    }

    /**
     * Retrieves comprehensive user details including profile information and roles.
     * This method aggregates data from multiple Cognito API calls to build a complete
     * user profile.
     *
     * @param username The unique identifier for the user
     * @return UserDetails object containing all user information
     * @throws TBSUserServiceException if there's an error communicating with Cognito
     */
    @Override
    public UserDetails getUserDetails(String username) {
        log.info("Retrieving user details for username: {}", username);

        try {
            // Step 1: Fetch basic user profile from Cognito
            log.debug("Requesting user profile from Cognito for username: {}", username);
            AdminGetUserResponse response = cognitoClient.adminGetUser(b -> b
                    .userPoolId(userPoolId)
                    .username(username));
            log.debug("Successfully retrieved user profile from Cognito");

            // Step 2: Build user details with all attributes
            UserDetails userDetails = UserDetails.builder()
                    .username(username)
                    .email(getAttribute(response, "email"))
                    .name(getAttribute(response, "name"))
                    .phoneNumber(getAttribute(response, "phone_number"))
                    .gender(getAttribute(response, "gender"))
                    .authorities(getUserRoles(username))
                    .build();

            log.info("Successfully compiled user details for username: {}", username);
            return userDetails;

        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to fetch user details. Username: {}, Error: {}, ErrorType: {}",
                    username, e.getMessage(), e.getClass().getSimpleName());
            throw new TBSUserServiceException("Error retrieving user details from Cognito", e);
        }
    }

    /**
     * Updates the role of a user in the system. This operation modifies the user's
     * attributes in Cognito to reflect their new role.
     *
     * @param username The username of the user whose role needs to be updated
     * @param role The new role to be assigned to the user
     * @throws TBSUserServiceException if the role update operation fails
     */
    @Override
    public void updateUserRole(String username, UserDetails.UserRole role) {
        log.info("Initiating role update for user: {} to role: {}", username, role);

        try {
            // Update the role attribute in Cognito
            AttributeType roleAttribute = AttributeType.builder()
                    .name(userRoleAttribute)
                    .value(role.name())
                    .build();

            log.debug("Sending role update request to Cognito for user: {}", username);
            cognitoClient.adminUpdateUserAttributes(b -> b
                    .userPoolId(userPoolId)
                    .username(username)
                    .userAttributes(roleAttribute));

            log.info("Successfully updated role to {} for user: {}", role, username);

        } catch (CognitoIdentityProviderException e) {
            log.error("Role update failed. Username: {}, Target Role: {}, Error: {}",
                    username, role, e.getMessage());
            throw new TBSUserServiceException("Failed to update user role in Cognito", e);
        }
    }

    /**
     * Helper method to safely extract attributes from Cognito user response.
     * This method handles the case where an attribute might not exist.
     *
     * @param response The Cognito admin get user response
     * @param attrName The name of the attribute to extract
     * @return The value of the requested attribute or null if not found
     */
    private String getAttribute(AdminGetUserResponse response, String attrName) {
        log.trace("Extracting attribute: {} from user response", attrName);
        return response.userAttributes().stream()
                .filter(attr -> attr.name().equals(attrName))
                .findFirst()
                .map(AttributeType::value)
                .orElse(null);
    }

    /**
     * Retrieves and maps user's Cognito group memberships to Spring Security authorities.
     * This method is crucial for role-based access control (RBAC) in the application.
     *
     * @param username The username to fetch roles for
     * @return Collection of GrantedAuthority objects representing user roles
     * @throws CognitoIdentityProviderException if there's an error fetching group information
     */
    private Collection<? extends GrantedAuthority> getUserRoles(String username) {
        log.debug("Fetching group memberships for user: {}", username);

        AdminListGroupsForUserResponse response = cognitoClient.adminListGroupsForUser(b -> b
                .userPoolId(userPoolId)
                .username(username));

        Collection<GrantedAuthority> authorities = response.groups().stream()
                .map(group -> new SimpleGrantedAuthority(group.groupName()))
                .collect(Collectors.toList());

        log.debug("Retrieved {} roles for user: {}", authorities.size(), username);
        return authorities;
    }
}