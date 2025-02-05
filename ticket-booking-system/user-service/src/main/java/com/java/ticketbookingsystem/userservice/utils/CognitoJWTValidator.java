package com.java.ticketbookingsystem.userservice.utils;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

/**
 * Validates JWT tokens issued by AWS Cognito User Pools.
 * <p>
 * Handles the complete JWT validation workflow including:
 * - Retrieving JSON Web Key Set (JWKS) from Cognito endpoint
 * - Verifying token signature using RSA public keys
 * - Validating standard claims (issuer, audience, expiration)
 * - Checking Cognito-specific claims (token_use)
 * <p>
 * Security Implementation Details:
 * 1. Uses Auth0 JWT library for cryptographic verification
 * 2. Implements key rotation through JWKS endpoint
 * 3. Validates against Cognito's regional issuer URL
 *
 * @see <a href="https://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-user-pools-using-tokens-verifying-a-jwt.html">Cognito JWT Verification Docs</a>
 */
@Slf4j
public class CognitoJWTValidator {
    private static final String JWKS_PATH = "/.well-known/jwks.json";
    private final JwkProvider jwkProvider;
    private final String clientId;
    private final String issuerUrl;

    /**
     * Constructs validator using Cognito metadata
     * @param clientId Application client ID from Cognito
     * @param region AWS region (e.g., us-east-1)
     * @param userPoolId Cognito user pool ID
     * @throws MalformedURLException if JWKS URL construction fails
     */
    public CognitoJWTValidator(String clientId, String region, String userPoolId)
            throws MalformedURLException {

        this.clientId = clientId;
        this.issuerUrl = constructIssuerUrl(region, userPoolId);
        String jwksUrl = this.issuerUrl + JWKS_PATH;
        this.jwkProvider = new UrlJwkProvider(new URL(jwksUrl));

        log.info("Initialized Cognito validator:\n- Issuer: {}\n- JWKS: {}", issuerUrl, jwksUrl);
    }

    /**
     * Validates and decodes JWT token with comprehensive checks
     * @param token Raw JWT token from Authorization header
     * @return Decoded JWT with verified claims
     * @throws JWTVerificationException for any validation failure
     */
    public DecodedJWT validate(String token) throws JWTVerificationException {
        log.debug("Initiating JWT validation for token: {}...", abbreviateToken(token));

        try {
            Algorithm algorithm = getAlgorithm(token);
            JWTVerifier verifier = buildVerifier(algorithm);
            DecodedJWT jwt = verifier.verify(token);
            log.info("Successful validation for subject: {}", jwt.getSubject());
            return jwt;
        } catch (JwkException e) {
            log.error("JWKS retrieval failure: {}", e.getMessage());
            throw new JWTVerificationException("Key configuration error", e);
        } catch (JWTVerificationException e) {
            log.warn("Rejected invalid token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Constructs JWT verifier with security requirements
     * Why These Validations Matter:
     * Issuer Check: Prevents tokens from other user pools
     * Audience Check: Ensures token was issued for this client
     * Token Use: Restricts to access/id tokens (excludes refresh tokens)
     * JWKS Verification: Validates token signature against current keys
     */
    private JWTVerifier buildVerifier(Algorithm algorithm) {
        return JWT.require(algorithm)
                .withIssuer(issuerUrl)
                .withAudience(clientId)
                .withClaim("token_use", this::validateTokenUse)
                .build();
    }

    /**
     * Validates Cognito-specific token_use claim
     */
    private boolean validateTokenUse(Claim claim, DecodedJWT jwt) {
        String tokenUse = claim.asString();
        boolean isValid = "access".equals(tokenUse) || "id".equals(tokenUse);

        log.debug("Token use validation: {} => {}", tokenUse, isValid ? "VALID" : "INVALID");
        return isValid;
    }

    /**
     * Retrieves cryptographic algorithm for token verification
     */
    private Algorithm getAlgorithm(String token) throws JwkException {
        DecodedJWT unverifiedJwt = JWT.decode(token);
        log.debug("Decoding token header: {}", unverifiedJwt.getHeader());

        Jwk jwk = jwkProvider.get(unverifiedJwt.getKeyId());
        log.debug("Retrieved JWK for kid {}: {}", unverifiedJwt.getKeyId(), jwk);

        return Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
    }

    /**
     * Constructs Cognito issuer URL from region and user pool ID
     */
    private String constructIssuerUrl(String region, String userPoolId) {
        return String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId);
    }

    private String abbreviateToken(String token) {
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}
