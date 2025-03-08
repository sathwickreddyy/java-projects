package com.java.ticketbookingsystem.userservice.controller;

import com.java.ticketbookingsystem.userservice.dto.*;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.auth.AuthenticationService;
import com.java.ticketbookingsystem.userservice.service.users.UserService;
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
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    /**
     * Constructs a new AuthController with required dependencies.
     *
     * @param userService the service layer component handling user operations.
     * @throws IllegalArgumentException if userService is null.
     */
    public AuthController(@Qualifier("firebaseUserService") UserService userService,
                          @Qualifier("firebaseAuthenticationService") AuthenticationService authenticationService) {
        this.userService = Objects.requireNonNull(userService, "UserService cannot be null");
        this.authenticationService = authenticationService;
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
            return ResponseEntity.ok(authenticationService.signIn(signInRequest, request.getSession().getId()));
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
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        String currentUserId = userService.getCurrentUser();
        authenticationService.signOut(currentUserId, request.getSession().getId());
        return ResponseEntity.noContent().build();
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
    @GetMapping("/me")
    public ResponseEntity<UserDetailsResponse> currentUser(HttpServletRequest request) {
        log.info("Retrieving current user details");
        String currentUserId = userService.getCurrentUser();
        log.info("Current user ID: {}", currentUserId);
        UserDetailsResponse currentUserDetails = userService.getUserDetails(currentUserId);
        if (currentUserDetails == null) {
            throw new TBSUserServiceException("No current user found");
        }
        return ResponseEntity.ok(currentUserDetails);
    }

    /**
     * Registers a new user in the system
     *
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
            log.info("Registering new user: {}", registrationRequest.getUsername());
            AuthenticationResponse response = authenticationService.signUp(registrationRequest);
            log.info("User registered successfully: {}", registrationRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            throw new TBSUserServiceException("User registration failed", e);
        }
    }
}
