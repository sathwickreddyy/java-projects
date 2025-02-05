package com.java.ticketbookingsystem.userservice.configuration;

import com.java.ticketbookingsystem.userservice.utils.CognitoJWTValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.net.MalformedURLException;

/**
 * Configuration class for AWS Cognito Integration.
 */
@Configuration
public class CognitoConfig {

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.region}")
    private String region;

    @Bean
    public CognitoJWTValidator cognitoJWTValidator() throws MalformedURLException {
        return new CognitoJWTValidator(clientId, region, userPoolId);
    }

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient()
    {
        return CognitoIdentityProviderClient.builder().region(Region.of(region)).build();
    }
}
