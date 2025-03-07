package com.java.ticketbookingsystem.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationRequest {
    private String email;
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
