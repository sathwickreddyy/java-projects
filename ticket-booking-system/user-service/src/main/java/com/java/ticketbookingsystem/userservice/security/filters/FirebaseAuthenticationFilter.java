package com.java.ticketbookingsystem.userservice.security.filters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.java.ticketbookingsystem.userservice.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (!token.isEmpty()) {
            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                Authentication auth = new FirebaseAuthenticationToken(decodedToken, extractAuthorities(decodedToken));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (FirebaseAuthException e) {
                log.error("Firebase authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * If a token is found in the session, use it. Otherwise, check the Authorization header and update the cache if found.
     *
     * @param request
     * @return
     */
    private String extractToken(HttpServletRequest request) {
        // fetch the token from cookies or use the authorization header
        Optional<String> tokenOptional = CookieUtil.fetchTokenFromCookie(request);
        if(tokenOptional.isPresent()) {
            log.info("JWT Token fetched from cookie");
            return tokenOptional.get();
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            return token;
        }
        return "";
    }


    /**
     * Expected Behavior:
     * <pre>
     * Storing Roles: You store roles as simple strings (e.g., "USER").
     *
     * Constructing Authorities: You construct authorities by prefixing the stored role with "ROLE_" (e.g., "ROLE_USER").
     * This is the expected behavior because Spring Security uses the "ROLE_" prefix for role-based access control. By storing roles without the prefix and adding it during authority construction, you maintain consistency with Spring Security's conventions.
     * </pre>
     * <p>
     * Example Walkthrough:
     * <pre>
     * Registration Request: A user registers with no specified role.
     * Default Role Assignment: The assignDefaultRole method returns "USER".
     * Storing in Firestore and Custom Claims: The role "USER" is stored.
     * Extracting Authorities: The authentication filter extracts the role "USER" from custom claims and constructs an authority "ROLE_USER".
     * This approach ensures that your roles are properly stored and used for authorization checks in your application.
     * </pre>
     *
     * @param token FirebaseToken
     * @return Collection<Authorities>
     */
    private Collection<? extends GrantedAuthority> extractAuthorities(FirebaseToken token) {
        Map<String, Object> claims = token.getClaims();
        if (claims.containsKey("roles")) {
            return ((List<String>) claims.get("roles")).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static class FirebaseAuthenticationToken extends AbstractAuthenticationToken {
        private final FirebaseToken decodedToken;

        public FirebaseAuthenticationToken(FirebaseToken decodedToken,
                                           Collection<? extends GrantedAuthority> authorities) {
            super(authorities);
            this.decodedToken = decodedToken;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return ""; // Credentials not needed for Firebase token
        }

        @Override
        public Object getPrincipal() {
            return decodedToken; // Return the complete Firebase token
        }
    }
}

