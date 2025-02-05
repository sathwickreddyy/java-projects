package com.java.ticketbookingsystem.userservice.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * Custom JWT decoder implementing Spring Security's {@link JwtDecoder} interface.
 * <p>
 * This decoder bridges AWS Cognito JWT validation with Spring Security’s JWT model.
 * It performs the following key steps:
 * <ul>
 *   <li><b>Validation:</b> Delegates token signature and claim validation to {@link CognitoJWTValidator}.</li>
 *   <li><b>Timestamp Conversion:</b> Converts the issued and expiration dates (java.util.Date)
 *       to {@link Instant} to comply with Spring Security's JWT API.</li>
 *   <li><b>Header Processing:</b> Decodes the Base64URL‑encoded header string into a type‑safe Map.
 *       (Using TypeReference minimizes unchecked conversion warnings.)</li>
 *   <li><b>Claim Normalization:</b> Converts and filters Auth0-specific claims, handling exceptions
 *       so as not to disrupt JWT processing.</li>
 * </ul>
 * <p>
 * <b>Why these steps?</b>
 * <ul>
 *   <li><i>Token trust:</i> Verifying the signature, issuer, audience, and token_use claim ensures that the token
 *       was correctly issued by Cognito and is meant for this client.</li>
 *   <li><i>Type safety:</i> Converting headers and claims into well-defined types at runtime helps catch potential
 *       mismatches early and prevents runtime errors.</li>
 *   <li><i>Logging:</i> Detailed logging provides insight during debugging and aids in monitoring token processing.
 *       Sensitive parts of the token are abbreviated to avoid exposing security-critical data.</li>
 *   <li><i>Configuration Simplification:</i> Instead of retrieving the JWKS URL from properties, we derive it from
 *       the issuer URL because in AWS Cognito the JWKS URL is always the issuer URL appended with "/.well-known/jwks.json".</li>
 * </ul>
 *
 * @see CognitoJWTValidator for core Cognito validation logic
 * @see JwtDecoder for the Spring Security decoder interface
 */
@Slf4j
public class CognitoJwtDecoder implements JwtDecoder {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final CognitoJWTValidator validator;

    /**
     * Constructs a CognitoJwtDecoder with the specified CognitoJWTValidator.
     *
     * @param validator a CognitoJWTValidator instance that encapsulates AWS-specific JWT validation logic.
     */
    public CognitoJwtDecoder(CognitoJWTValidator validator) {
        this.validator = validator;
        log.info("Initialized CognitoJwtDecoder using validator: {}", validator.getClass().getSimpleName());
    }

    /**
     * Decodes and validates a JWT while converting its Auth0 representation to a Spring Security-compatible {@link Jwt}.
     * <p>
     * The method performs the following:
     * <ul>
     *   <li>Validates the token with {@link CognitoJWTValidator}</li>
     *   <li>Converts issuedAt and expiresAt from Date to {@link Instant}</li>
     *   <li>Decodes the header from a Base64URL string into a type‑safe Map</li>
     *   <li>Processes claims to normalize data and resolve any conversion issues</li>
     * </ul>
     *
     * @param token the raw JWT token string.
     * @return a {@link Jwt} object that can be used by Spring Security.
     * @throws JwtException if token validation or conversion fails.
     */
    @Override
    public Jwt decode(String token) throws JwtException {
        log.debug("Starting JWT decoding process for token: {}...", abbreviateToken(token));
        try {
            DecodedJWT decoded = validator.validate(token);
            log.info("JWT successfully validated for subject: {}", decoded.getSubject());

            // Convert issued and expiry timestamps from java.util.Date to java.time.Instant
            Instant issuedAt = decoded.getIssuedAt().toInstant();
            Instant expiresAt = decoded.getExpiresAt().toInstant();
            log.debug("Converted JWT timestamps - IssuedAt: {}, ExpiresAt: {}", issuedAt, expiresAt);

            // Process header and claims using safe conversion methods
            Map<String, Object> headers = convertHeader(decoded);
            Map<String, Object> claims = convertClaims(decoded.getClaims());
            log.debug("Parsed {} header claims and {} payload claims", headers.size(), claims.size());

            return new Jwt(token, issuedAt, expiresAt, headers, claims);

        } catch (JWTVerificationException ex) {
            log.error("JWT verification failed: {}", ex.getMessage());
            OAuth2Error error = new OAuth2Error("invalid_token", ex.getMessage(), null);
            throw new JwtValidationException("Cognito JWT validation failed", Collections.singletonList(error));
        } catch (Exception ex) {
            log.error("Unexpected error during JWT decoding: {}", ex.getMessage());
            throw new JwtException("JWT processing failed", ex);
        }
    }

    /**
     * Converts the Base64URL-encoded JWT header into a type‑safe Map.
     * <p>
     * Since Auth0 does not directly expose a Map for header claims, we manually decode
     * the header string. Using Jackson’s {@link TypeReference} ensures type safety and
     * minimizes unchecked conversion warnings.
     *
     * @param decoded the decoded JWT.
     * @return a Map containing the header claims.
     * @throws JwtException if the header cannot be parsed.
     */
    private Map<String, Object> convertHeader(DecodedJWT decoded) {
        try {
            String headerJson = new String(
                    Base64.getUrlDecoder().decode(decoded.getHeader()),
                    StandardCharsets.UTF_8
            );
            log.trace("Decoded JWT header JSON: {}", headerJson);
            // Use a TypeReference to obtain a type-safe Map<String, Object>
            Map<String, Object> headers = objectMapper.readValue(headerJson, new TypeReference<Map<String, Object>>() {});
            log.debug("Successfully parsed {} header claims", headers.size());
            return headers;
        } catch (IOException ex) {
            log.error("Failed to parse JWT header: {}", ex.getMessage());
            throw new JwtException("Invalid JWT header", ex);
        }
    }

    /**
     * Converts Auth0-specific claims into a type‑safe, Spring Security-compatible Map.
     * <p>
     * This method processes each claim by filtering out null values and attempting to convert
     * the claim value to a generic Object. If conversion fails, the claim is replaced with a
     * default error placeholder.
     *
     * @param auth0Claims the raw claims from Auth0.
     * @return a normalized Map of claims.
     */
    private Map<String, Object> convertClaims(Map<String, Claim> auth0Claims) {
        return auth0Claims.entrySet().stream()
                .peek(entry -> log.trace("Processing claim: {}", entry.getKey()))
                .filter(entry -> {
                    if (entry.getValue() == null) {
                        log.warn("Skipping null claim: {}", entry.getKey());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                Object value = entry.getValue().as(Object.class);
                                log.debug("Converted claim '{}' to type: {}", entry.getKey(),
                                        value != null ? value.getClass().getSimpleName() : "null");
                                return value;
                            } catch (Exception ex) {
                                log.warn("Failed to convert claim '{}': {}", entry.getKey(), ex.getMessage());
                                return "CLAIM_CONVERSION_ERROR";
                            }
                        }
                ));
    }

    /**
     * Abbreviates the token string for safe logging.
     *
     * @param token the full JWT token.
     * @return an abbreviated representation (first 8 and last 4 characters).
     */
    private String abbreviateToken(String token) {
        if (token.length() <= 12) {
            return token;
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}
