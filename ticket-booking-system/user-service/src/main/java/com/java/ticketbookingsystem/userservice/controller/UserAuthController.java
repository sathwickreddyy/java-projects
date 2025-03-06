package com.java.ticketbookingsystem.userservice.controller;

import com.java.ticketbookingsystem.userservice.dto.*;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.TokenManagementService;
import com.java.ticketbookingsystem.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * REST controller responsible for managing user-related operations in the Ticket Booking System.
 * <p>
 * This controller provides endpoints for authentication (sign in, sign out, token refresh),
 * retrieving user details, and managing user roles. All endpoints are versioned under '/v1/users' and
 * require valid authentication tokens.
 * <p>
 * Security:
 * - All endpoints require a valid authentication token.
 * - Role management endpoints require an ADMIN role.
 *
 * @see UserService
 * @see UserDetails
 */
@RestController
@RequestMapping("/v1/users")
@Slf4j
@Validated
public class UserAuthController {

    private final UserService userService;
    private final TokenManagementService tokenManagementService;

    /**
     * Constructs a new AuthController with required dependencies.
     *
     * @param userService            the service layer component handling user operations.
     * @param tokenManagementService the service handling token management operations.
     * @throws IllegalArgumentException if userService is null.
     */
    public UserAuthController(@Qualifier("firebaseUserServiceImpl") UserService userService, TokenManagementService tokenManagementService) {
        this.userService = Objects.requireNonNull(userService, "UserService cannot be null");
        this.tokenManagementService = tokenManagementService;
        log.info("AuthController initialized with UserService implementation: {}",
                userService.getClass().getSimpleName());
    }

    /**
     * Fetches the raw JWT token from the Authorization header.
     * <p>
     * This endpoint is primarily for debugging or inspection purposes and does not reissue a new token.
     *
     * @param request HttpServletRequest used to retrieve the Authorization header.
     * @return A JSON response containing the token.
     * @throws TBSUserServiceException if the token is missing or improperly formatted.
     */
    @Operation(
            summary = "Fetch JWT token",
            description = "Fetches the raw JWT token provided in the Authorization header",
            tags = {"Tokens"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing Authorization header"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/token")
    public ResponseEntity<TokenResponse> fetchToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            return ResponseEntity.ok(new TokenResponse(token));
        } else {
            throw new TBSUserServiceException("Authorization token is missing");
        }
    }

    /**
     * Signs in a user and returns JWT tokens.
     * <p>
     * This endpoint delegates to Cognito (or your authentication provider) and returns both the access and refresh tokens.
     *
     * @param signInRequest contains sign-in credentials.
     * @return a JSON response containing access and refresh tokens.
     * @throws TBSUserServiceException if sign-in fails.
     */
    @Operation(
            summary = "Sign in user",
            description = "Signs in a user and returns a JWT token",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User signed in successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid sign-in request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signIn(
            @Parameter(description = "Sign-in credentials", required = true)
            @RequestBody AuthenticationRequest signInRequest, HttpServletRequest request) {
        try {
            AuthenticationResponse response = userService.signIn(signInRequest, request.getSession().getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to sign in user: {}", e.getMessage());
            throw new TBSUserServiceException("Sign in failed", e);
        }
    }

    /**
     * Signs out the current user.
     * <p>
     * This endpoint invalidates the current session/token.
     *
     * @return HTTP 204 (No Content) on successful sign out.
     */
    @Operation(
            summary = "Sign out user",
            description = "Signs out the currently logged in user",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User signed out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid sign-out request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/signout")
    public ResponseEntity<Void> signOut() {
        String currentUserName = userService.getCurrentUser();
        UserDetails currentUserDetails = userService.getUserDetails(currentUserName);
        userService.signOut(currentUserDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Refreshes the access token using the provided refresh token.
     * <p>
     * This endpoint returns fresh tokens after verifying the existing refresh token.
     *
     * @param request the refresh token request containing username and refresh token.
     * @return a JSON response containing the new access and refresh tokens.
     * @throws TBSUserServiceException if either the username or refresh token is missing.
     */
    @Operation(
            summary = "Refresh JWT token",
            description = "Refreshes the access token using the provided refresh token",
            tags = {"Tokens"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Username or refresh token is missing"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        if (refreshTokenRequest.getUsername() == null || refreshTokenRequest.getRefreshToken() == null) {
            throw new TBSUserServiceException("Username or refresh token is missing");
        }
        AuthenticationResponse response = tokenManagementService.refreshTokens(request.getSession().getId(), refreshTokenRequest.getUsername(), refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of the currently authenticated user.
     * <p>
     * This endpoint extracts the user information from the security context and returns a response DTO
     * containing username, name, email, gender, phone number, and role.
     *
     * @return a JSON response containing the current user's details.
     * @throws TBSUserServiceException if no current user is found.
     */
    @Operation(
            summary = "Get current user details",
            description = "Retrieves details of the currently authenticated user",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getCurrentUser")
    public ResponseEntity<UserDetailsResponse> getCurrentUser() {
        String currentUserName = userService.getCurrentUser();
        UserDetails currentUserDetails = userService.getUserDetails(currentUserName);
        if (currentUserDetails == null) {
            throw new TBSUserServiceException("No current user found");
        }
        // Map the domain UserDetails to the response DTO.
        UserDetailsResponse response = new UserDetailsResponse();
        response.setUsername(currentUserDetails.getUsername());
        response.setName(currentUserDetails.getName());
        response.setEmail(currentUserDetails.getEmail());
        response.setGender(currentUserDetails.getGender());
        response.setPhoneNumber(currentUserDetails.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Registers a new user in the system
     * @param registrationRequest user registration details
     * @return authentication tokens
     */
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with default USER role",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration request"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signUp(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody RegistrationRequest registrationRequest) {

        try {
            AuthenticationResponse response = userService.signUp(registrationRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            throw new TBSUserServiceException("User registration failed", e);
        }
    }
}
