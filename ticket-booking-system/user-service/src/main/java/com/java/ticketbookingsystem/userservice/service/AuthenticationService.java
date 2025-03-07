package com.java.ticketbookingsystem.userservice.service;

import com.java.ticketbookingsystem.userservice.dto.AuthenticationRequest;
import com.java.ticketbookingsystem.userservice.dto.AuthenticationResponse;
import com.java.ticketbookingsystem.userservice.dto.TokenResponse;

public interface AuthenticationService {
    AuthenticationResponse signIn(AuthenticationRequest signInRequest, String sessionId);

    TokenResponse refreshToken(String token);
}
