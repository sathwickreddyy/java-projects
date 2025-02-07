package com.java.ticketbookingsystem.userservice.dto;

import lombok.Data;


/**
 * Data Transfer Object (DTO) representing user information retrieved from AWS Cognito.
 * This class encapsulates essential user attributes.
 */
@Data
public class UserDetailsResponse {
    /**
     * The unique identifier for the user.
     */
    private String username;

    /**
     * The full name of the user.
     */
    private String name;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * The gender of the user.
     */
    private String gender;

    /**
     * The phone number of the user.
     */
    private String phoneNumber;
}