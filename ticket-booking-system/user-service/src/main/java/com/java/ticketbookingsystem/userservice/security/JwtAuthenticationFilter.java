package com.java.ticketbookingsystem.userservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.utils.CognitoJWTValidator;
import com.java.ticketbookingsystem.userservice.utils.CognitoJwtDecoder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * JWT Authentication Filter that handles Cognito token validation and authentication context setup.
 * <p>
 * This filter intercepts incoming requests, extracts JWT tokens from the Authorization header,
 * validates them using AWS Cognito's public keys, and sets up the Spring Security authentication context.
 * Implements a caching mechanism for decoded tokens to improve performance.
 *
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see com.java.ticketbookingsystem.userservice.utils.CognitoJwtDecoder
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String COGNITO_GROUPS_CLAIM = "cognito:groups";

    private final JwtDecoder jwtDecoder;
    private final Cache<String, DecodedJWT> jwtCache;

    /**
     * Constructs JWT authentication filter with Cognito validator
     * @param jwtValidator Custom validator for Cognito-specific JWT claims
     */
    public JwtAuthenticationFilter(CognitoJWTValidator jwtValidator) {
        this.jwtDecoder = new CognitoJwtDecoder(jwtValidator);
        this.jwtCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
        log.info("Initialized JwtAuthenticationFilter with token caching");
    }

    /**
     * Main filter method that processes each request
     * @param request Incoming HTTP request
     * @param response HTTP response
     * @param filterChain Spring filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);
            if (token != null) {
                processJwtToken(token);
            }
        } catch (JwtException e) {
            handleJwtError(e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Processes and validates JWT token, setting security context
     * @param token Raw JWT token from header
     */
    private void processJwtToken(String token) {
        try {
            DecodedJWT decodedJWT = jwtCache.get(token, t -> {
                log.debug("Cache miss - decoding fresh token");
                Jwt jwt = jwtDecoder.decode(t);
                // Use JWT's token value with proper decoding
                return JWT.decode(jwt.getTokenValue());
            });

            setSecurityContext(decodedJWT);
            log.info("Successfully authenticated user: {}", decodedJWT.getSubject());

        } catch (Exception e) {
            log.error("JWT processing failed: {}", e.getMessage());
            throw new TBSUserServiceException("JWT validation error", e);
        }
    }

    /**
     * Sets up Spring Security context with authenticated user details
     * @param decodedJWT Validated JWT claims
     */
    private void setSecurityContext(DecodedJWT decodedJWT) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        decodedJWT.getSubject(),
                        null,
                        extractAuthorities(decodedJWT)
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Set security context for user: {}", decodedJWT.getSubject());
    }

    /**
     * Extracts JWT token from Authorization header
     * @return Raw token or null if not present
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            log.debug("Extracted JWT token from Authorization header");
            return token;
        }
        log.warn("No valid Authorization header found");
        return null;
    }

    /**
     * Extracts granted authorities from Cognito group claims
     * @return Collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> extractAuthorities(DecodedJWT jwt) {
        // Get the claim as a Claim object first
        Claim groupsClaim = jwt.getClaim(COGNITO_GROUPS_CLAIM);

        // Handle different claim types safely
        List<String> groups = groupsClaim.isNull() ?
                Collections.emptyList() :
                groupsClaim.asList(String.class);

        log.debug("Extracted Cognito groups: {}", groups);
        return groups.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }


    /**
     * Handles JWT validation errors and clears security context
     */
    private void handleJwtError(JwtException e) {
        SecurityContextHolder.clearContext();
        log.error("JWT validation error: {}", e.getMessage());
        throw new TBSUserServiceException("Authentication failed", e);
    }
}
