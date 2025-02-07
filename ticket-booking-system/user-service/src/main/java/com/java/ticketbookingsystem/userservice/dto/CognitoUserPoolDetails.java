package com.java.ticketbookingsystem.userservice.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CognitoUserPoolDetails {
    private final String userPoolId;
    private final String clientId;
    private final String region;
    private String userRoleAttribute;
}
