package com.java.ticketbookingsystem.userservice.service;

import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

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
}

