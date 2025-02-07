package com.java.ticketbookingsystem.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CognitoUserPoolDetails {
    private String userPoolId;
    private String clientId;
    private String region;
}
