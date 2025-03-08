package com.java.ticketbookingsystem.userservice.service.auth;

import com.java.ticketbookingsystem.userservice.dto.AuthenticationRequest;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.RegistrationRequest;
import com.java.ticketbookingsystem.userservice.dto.TokenResponse;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;

public interface AuthenticationService {
    /**
     * Registers a new user in Cloud.
     *
     * @param request RegistrationRequest
     * @return Boolean
     */
    Boolean signUp(RegistrationRequest request);

    /**
     * Authenticates a user by verifying their credentials.
     *
     * @param signInRequest The authentication request containing username and password
     * @return AuthenticationResponse containing token and refresh token
     * @throws TBSUserServiceException if authentication fails
     */
    AuthenticationResponse signIn(AuthenticationRequest signInRequest);

    /**
     * Refreshes a user's authentication token.
     *
     * @param token refresh token
     * @return TokenResponse
     */
    TokenResponse refreshToken(String token);

    /**
     * Signs out a user by revoking their refresh token.
     *
     * @param userId userId
     * @throws TBSUserServiceException if signing out fails
     */
    void signOut(String userId);
}
