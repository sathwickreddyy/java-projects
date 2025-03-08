package com.java.ticketbookingsystem.userservice.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class CookieUtil {

    private static final String COOKIE_KEY_NAME = "auth_token";

    /**
     * Stores the authentication token in a secure cookie.
     *
     * @param response The {@link HttpServletResponse} to which the cookie will be added.
     * @param token    The authentication token to store in the cookie.
     */
    public static void storeAuthTokenCookie(HttpServletResponse response, String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Attempted to store an empty or null authentication token in the cookie.");
            return;
        }

        try {
            Cookie cookie = new Cookie(COOKIE_KEY_NAME, token);

            cookie.setHttpOnly(true);         // Prevent XSS attacks
            cookie.setSecure(true);           // Only send over HTTPS
            cookie.setPath("/");              // Available for all endpoints
            cookie.setMaxAge(1800);           // Expires in 30 minutes
            cookie.setAttribute("SameSite", "Strict"); // Prevent CSRF attacks

            response.addCookie(cookie);
            log.info("Authentication token successfully stored in a secure cookie.");
        } catch (Exception e) {
            log.error("Failed to store authentication token in the cookie: {}", e.getMessage(), e);
        }
    }

    /**
     * Fetches the authentication token from cookies.
     *
     * @param request The {@link HttpServletRequest} containing the cookies.
     * @return An {@link Optional} containing the authentication token if found, otherwise an empty Optional.
     */
    public static Optional<String> fetchTokenFromCookie(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest is null. Unable to fetch authentication token from cookies.");
            return Optional.empty();
        }

        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null || cookies.length == 0) {
                log.debug("No cookies found in the request.");
                return Optional.empty();
            }

            for (Cookie cookie : cookies) {
                if (COOKIE_KEY_NAME.equals(cookie.getName())) {
                    log.info("Authentication token found in cookies.");
                    return Optional.ofNullable(cookie.getValue());
                }
            }

            log.debug("Authentication token not found in cookies.");
        } catch (Exception e) {
            log.error("Error occurred while fetching authentication token from cookies: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Deletes the authentication token cookie from the response.
     *
     * @param response The {@link HttpServletResponse} from which the cookie will be deleted.
     */
    public static void deleteAuthTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_KEY_NAME, ""); // Empty value
        cookie.setHttpOnly(true);         // Ensure consistency with stored cookie
        cookie.setSecure(true);           // Ensure deletion works on HTTPS
        cookie.setPath("/");              // Must match the original cookie's path
        cookie.setMaxAge(0);              // Expire immediately
        cookie.setAttribute("SameSite", "Strict"); // Prevent CSRF attacks

        response.addCookie(cookie);
        log.info("Authentication token cookie successfully deleted.");
    }
}

