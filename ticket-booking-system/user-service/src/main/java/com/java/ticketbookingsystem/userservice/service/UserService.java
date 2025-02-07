package com.java.ticketbookingsystem.userservice.service;

import com.java.ticketbookingsystem.userservice.dto.AuthenticationRequest;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;

/**
 * Interface defining the contract for user management operations.
 * Provides methods for user information retrieval and role management.
 */
public interface UserService {
    /**
     * Retrieves detailed user information from Cognito.
     *
     * @param username The unique identifier of the user
     * @return UserDetails object containing user information
     * @throws TBSUserServiceException if there's an error fetching user details
     */
    UserDetails getUserDetails(String username);

    /**
     * Updates the role of a specified user in Cognito.
     *
     * @param username The unique identifier of the user
     * @param role The new role to be assigned
     * @throws TBSUserServiceException if there's an error updating the user role
     */
    void updateUserRole(String username, UserDetails.UserRole role);

    /**
     * Authenticates a user by verifying their credentials.
     *
     * @param signInRequest The authentication request containing username and password
     * @return AuthenticationResponse containing token and refresh token
     * @throws TBSUserServiceException if authentication fails
     */
    AuthenticationResponse signIn(AuthenticationRequest signInRequest);

    /**
     * Signs out a user by revoking their refresh token.
     *
     * @param username username
     * @throws TBSUserServiceException if signing out fails
     */
    void signOut(String username);

    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDetails object representing the authenticated user
     */
    UserDetails getCurrentUser();
}

