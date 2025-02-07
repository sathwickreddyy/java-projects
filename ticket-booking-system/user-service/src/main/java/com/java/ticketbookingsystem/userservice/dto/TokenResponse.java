package com.java.ticketbookingsystem.userservice.dto;

/**
 * DTO representing the token response.
 * Contains the raw JWT token string.
 */
public class TokenResponse {

    private String token;

    public TokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
