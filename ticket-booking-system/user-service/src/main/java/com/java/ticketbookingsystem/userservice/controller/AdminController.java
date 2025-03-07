package com.java.ticketbookingsystem.userservice.controller;


import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.users.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    public AdminController(@Qualifier("firebaseUserService")
                           UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves detailed information about a specific user.
     *
     * @param userId the unique identifier of the user to retrieve.
     * @return ResponseEntity containing user details if found.
     * @throws TBSUserServiceException if user is not found or a service error occurs.
     */
    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user details",
            description = "Retrieves comprehensive information about a specific user",
            tags = {"Admin"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDetails> getUser(
            @Parameter(description = "UserId of the target user", required = true)
            @PathVariable @NotBlank String userId) {

        log.info("Received request to fetch details for user: {}", userId);
        UserDetails userDetails = userService.getUserDetails(userId);
        log.debug("Retrieved user details for {}: {}", userId, userDetails);
        return ResponseEntity.ok(userDetails);
    }

    /**
     * Updates the role of a specified user
     *
     * @param userId Target user ID
     * @param role   New role to assign
     * @return HTTP 204 on success
     */
    @Operation(
            summary = "Update user role",
            description = "Updates the role of a specified user (ADMIN only)",
            tags = {"Admin"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role specified"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable String userId,
            @Parameter(description = "New role for the user", required = true)
            @RequestBody UserDetails.UserRole role) {

        log.info("Received role update request for user {} to {}", userId, role);
        userService.updateUserRole(userId, role);
        log.debug("Successfully updated role for user {}", userId);
        return ResponseEntity.noContent().build();
    }
}
