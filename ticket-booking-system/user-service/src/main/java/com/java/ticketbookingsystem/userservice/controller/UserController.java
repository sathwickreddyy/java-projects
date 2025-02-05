package com.java.ticketbookingsystem.userservice.controller;

import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.dto.UserDetails.UserRole;
import com.java.ticketbookingsystem.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * REST controller responsible for managing user-related operations in the Ticket Booking System.
 * This controller provides endpoints for retrieving user details and managing user roles.
 * All endpoints are versioned under '/v1/users' and require authentication.
 *
 * Security:
 * - All endpoints require a valid authentication token
 * - Role management endpoints require ADMIN role
 * - User detail retrieval is subject to authorization rules
 *
 * @see UserService
 * @see UserDetails
 */
@RestController
@RequestMapping("/v1/users")
@Slf4j
@Validated
@Tag(name = "User Management", description = "APIs for managing user profiles and roles")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a new UserController with required dependencies.
     *
     * @param userService Service layer component handling user operations
     * @throws IllegalArgumentException if userService is null
     */
    public UserController(UserService userService) {
        this.userService = Objects.requireNonNull(userService,
                "UserService cannot be null");
        log.info("UserController initialized with UserService implementation: {}",
                userService.getClass().getSimpleName());
    }

    /**
     * Retrieves detailed information about a specific user.
     * This endpoint returns comprehensive user details including profile information
     * and associated roles/permissions.
     *
     * @param username The unique identifier of the user to retrieve
     * @return ResponseEntity containing user details if found
     * @throws com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException
     *         if user is not found or service error occurs
     */
    @GetMapping("/{username}")
    @Operation(summary = "Get user details",
            description = "Retrieves detailed information about a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDetails> getUser(
            @Parameter(description = "Username of the target user", required = true)
            @PathVariable @NotBlank String username) {

        log.info("Received request to fetch details for user: {}", username);

        UserDetails userDetails = userService.getUserDetails(username);
        log.debug("Retrieved user details for {}: {}", username, userDetails);

        return ResponseEntity.ok(userDetails);
    }

    /**
     * Updates the role of a specified user. This operation is restricted to
     * administrators only and is used for role-based access control management.
     *
     * @param username The username of the user whose role needs to be updated
     * @param role The new role to be assigned to the user
     * @return ResponseEntity with no content if update is successful
     * @throws com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException
     *         if update fails
     * @throws org.springframework.security.access.AccessDeniedException
     *         if requester lacks admin privileges
     */
    @PutMapping("/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role",
            description = "Updates the role of a specified user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid role specified"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> updateUserRole(
            @Parameter(description = "Username of the target user", required = true)
            @PathVariable @NotBlank String username,

            @Parameter(description = "New role to be assigned", required = true)
            @RequestParam UserRole role) {

        log.info("Received request to update role to {} for user: {}", role, username);

        userService.updateUserRole(username, role);
        log.info("Successfully updated role to {} for user: {}", role, username);

        return ResponseEntity.ok().build();
    }
}