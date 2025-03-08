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
     * @return AuthenticationResponse
     */
    AuthenticationResponse signUp(RegistrationRequest request);

    /**
     * Authenticates a user by verifying their credentials.
     *
     * @param signInRequest The authentication request containing username and password
     * @param sessionId     The session ID
     * @return AuthenticationResponse containing token and refresh token
     * @throws TBSUserServiceException if authentication fails
     */
    AuthenticationResponse signIn(AuthenticationRequest signInRequest, String sessionId);

    /**
     * Refreshes a user's authentication token.
     *
     * @param token refresh token
     * @param sessionId session id
     * @return TokenResponse
     */
    TokenResponse refreshToken(String token, String sessionId);

    /**
     * Signs out a user by revoking their refresh token.
     *
     * @param userId userId
     * @throws TBSUserServiceException if signing out fails
     */
    void signOut(String userId, String sessionId);
}
