package com.java.ticketbookingsystem.userservice.controller;

import com.java.ticketbookingsystem.userservice.dto.TokenResponse;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.auth.AuthenticationService;
import com.java.ticketbookingsystem.userservice.utils.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1/")
public class TokenController {

    private final AuthenticationService authenticationService;

    public TokenController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/token")
    public ResponseEntity<TokenResponse> fetchToken(HttpServletRequest request) {
        // Retrive the token from Cookie or Authorization header
        Optional<String> tokenOptional = CookieUtil.fetchTokenFromCookie(request);
        if (tokenOptional.isPresent()) {
            log.info("Token fetched from cookie");
            return ResponseEntity.ok(new TokenResponse(tokenOptional.get()));
        }

        // Retrieve the token from the Authorization header
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            return ResponseEntity.ok(new TokenResponse(token));
        }
        /*
        	•	Issue:  TBSUserServiceException should be reserved for unexpected business logic failures, not just missing headers.
	        •	Fix:    Use ResponseStatusException from Spring to return 400 Bad Request.
         */
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization token is missing");
    }


    /**
     * Refreshes the access token using the provided refresh token.
     * <p>
     * This endpoint returns fresh tokens after verifying the existing refresh token.
     *
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
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestHeader("Authorization") String authHeader, HttpServletResponse response) {
        log.info("Refreshing token");
        String expiredToken = authHeader.substring(7);

        // Update the Cookie with new token
        TokenResponse tokenResponse = authenticationService.refreshToken(expiredToken);
        CookieUtil.storeAuthTokenCookie(response, tokenResponse.getToken());

        return ResponseEntity.ok(tokenResponse);
    }
}
