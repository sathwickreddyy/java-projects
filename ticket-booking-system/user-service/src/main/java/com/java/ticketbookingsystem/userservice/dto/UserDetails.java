package com.java.ticketbookingsystem.userservice.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Data Transfer Object (DTO) representing user information retrieved from AWS Cognito.
 * This class encapsulates all essential user attributes and their corresponding permissions.
 */
@Data
@Builder
public class UserDetails {
    private String username;
    private String email;
    private String name;
    private String phoneNumber;
    private String gender;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Enum defining the possible user roles in the system.
     * Used for role-based access control and user management.
     */
    public enum UserRole {
        ADMIN,
        USER,
        OWNER,
    }
}

