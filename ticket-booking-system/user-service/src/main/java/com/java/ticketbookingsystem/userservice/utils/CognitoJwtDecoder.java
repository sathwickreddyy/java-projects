package com.java.ticketbookingsystem.userservice.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom JWT decoder implementing Spring Security's {@link JwtDecoder} interface
 * to bridge AWS Cognito JWT validation with Spring Security's authentication framework.
 * <p>
 * This class handles the conversion between Auth0's JWT representation and Spring Security's
 * JWT model, including type conversions and claim structure normalization. It performs:
 * <ul>
 *   <li>JWT header/payload type conversions</li>
 *   <li>Date to Instant conversions for timestamp handling</li>
 *   <li>Safe claim value resolution</li>
 *   <li>Validation error translation</li>
 * </ul>
 *
 * @see CognitoJWTValidator Core Cognito validation logic
 * @see JwtDecoder Spring Security decoder interface
 */
@Slf4j
public class CognitoJwtDecoder implements JwtDecoder {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final CognitoJWTValidator validator;

    /**
     * Constructs a CognitoJwtDecoder with required validator
     * @param validator Configured CognitoJWTValidator instance that handles
     *                  AWS-specific JWT validation logic
     */
    public CognitoJwtDecoder(CognitoJWTValidator validator) {
        this.validator = validator;
        log.info("Initialized CognitoJwtDecoder with validator: {}", validator.getClass().getSimpleName());
    }

    /**
     * Decodes and validates a JWT token while handling type conversions between
     * Auth0 and Spring Security representations.
     *
     * @param token Raw JWT token string
     * @return Spring Security-compatible Jwt object
     * @throws JwtException when validation or conversion fails
     */
    @Override
    public Jwt decode(String token) throws JwtException {
        log.debug("Starting JWT decoding process for token: {}...", token.substring(0, 15));

        try {
            DecodedJWT decoded = validator.validate(token);
            log.info("Successfully validated JWT for subject: {}", decoded.getSubject());

            // Convert Auth0 timestamps to Spring-compatible Instants
            Instant issuedAt = decoded.getIssuedAt().toInstant();
            Instant expiresAt = decoded.getExpiresAt().toInstant();
            log.debug("Converted JWT timestamps - Issued: {}, Expires: {}", issuedAt, expiresAt);

            // Process header and claims with safety checks
            Map<String, Object> headers = convertHeader(decoded);
            Map<String, Object> claims = convertClaims(decoded.getClaims());

            log.debug("Successfully converted {} headers and {} claims", headers.size(), claims.size());

            return new Jwt(token, issuedAt, expiresAt, headers, claims);

        } catch (JWTVerificationException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            OAuth2Error error = new OAuth2Error("invalid_token", ex.getMessage(), null);
            throw new JwtValidationException("Cognito validation failed", Collections.singletonList(error));
        } catch (Exception ex) {
            log.error("Unexpected error during JWT decoding: {}", ex.getMessage());
            throw new JwtException("JWT processing failed", ex);
        }
    }

    /**
     * Converts JWT header from Base64URL string to claim map
     *
     * @param decoded Validated JWT object
     * @return Map of header claims
     * @throws JwtException if header parsing fails
     * @implNote Manual decoding required because Auth0 doesn't provide direct
     *           header claim map access
     */
    private Map<String, Object> convertHeader(DecodedJWT decoded) {
        try {
            String headerJson = new String(
                    Base64.getUrlDecoder().decode(decoded.getHeader()),
                    StandardCharsets.UTF_8
            );
            log.trace("Decoding JWT header: {}", headerJson);

            Map<String, Object> headers = objectMapper.readValue(headerJson, Map.class);
            log.debug("Successfully parsed {} header claims", headers.size());

            return headers;
        } catch (IOException ex) {
            log.error("Header decoding failed: {}", ex.getMessage());
            throw new JwtException("Invalid JWT header", ex);
        }
    }

    /**
     * Converts Auth0-specific claims to Spring-compatible Map<String, Object>
     * <p>
     * Handles special cases:
     * <ul>
     *   <li>Filters null claims</li>
     *   <li>Converts nested claim structures</li>
     *   <li>Handles type conversion errors gracefully</li>
     * </ul>
     *
     * @param auth0Claims Raw claims from Auth0 library
     * @return Normalized claim map for Spring Security
     */
    private Map<String, Object> convertClaims(Map<String, Claim> auth0Claims) {
        return auth0Claims.entrySet().stream()
                .peek(entry -> log.trace("Processing claim: {}", entry.getKey()))
                .filter(entry -> {
                    if (entry.getValue() == null) {
                        log.warn("Filtering out null claim: {}", entry.getKey());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                Object value = entry.getValue().as(Object.class);
                                log.debug("Converted claim {} to type {}", entry.getKey(),
                                        value != null ? value.getClass().getSimpleName() : "null");
                                return value;
                            } catch (Exception ex) {
                                log.warn("Claim conversion failed for {}: {}", entry.getKey(), ex.getMessage());
                                return "CLAIM_CONVERSION_ERROR";
                            }
                        }
                ));
    }
}
