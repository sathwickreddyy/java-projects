package com.java.ticketbookingsystem.userservice.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.java.ticketbookingsystem.userservice.dto.CognitoUserPoolDetails;
import com.java.ticketbookingsystem.userservice.dto.TokenHolder;
import com.java.ticketbookingsystem.userservice.utils.CognitoJWTValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for AWS Cognito Integration.
 */
@Configuration
public class CommonConfigurations {

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.region}")
    private String region;

    /**
     * Custom attribute name in Cognito that stores user roles.
     * This attribute is used for role-based access control.
     */
    @Value("${aws.cognito.userRoleAttribute}")
    private String userRoleAttribute;

    @Bean
    public CognitoJWTValidator cognitoJWTValidator() throws MalformedURLException {
        return new CognitoJWTValidator(clientId, region, userPoolId);
    }

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient()
    {
        return CognitoIdentityProviderClient.builder().region(Region.of(region)).build();
    }

    @Bean
    public Cache<String, Map<String, TokenHolder>> getTokenCache()
    {
        // Tokens are cached for 30 minutes; production systems may use Redis or a DB.
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    @Bean
    public CognitoUserPoolDetails getCognitoUserPoolDetails()
    {
        CognitoUserPoolDetails cognitoUserPoolDetails = new CognitoUserPoolDetails(userPoolId, clientId, region);
        cognitoUserPoolDetails.setUserRoleAttribute(userRoleAttribute);
        return cognitoUserPoolDetails;
    }
}
