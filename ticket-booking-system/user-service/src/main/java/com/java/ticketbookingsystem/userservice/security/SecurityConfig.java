package com.java.ticketbookingsystem.userservice.security;

import com.java.ticketbookingsystem.userservice.security.filters.FirebaseAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

//    private final AWSJwtAuthenticationFilter AWSJwtAuthenticationFilter;
    private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    public SecurityConfig(FirebaseAuthenticationFilter firebaseAuthenticationFilter) {
        this.firebaseAuthenticationFilter = firebaseAuthenticationFilter;
    }

    /**
     * Configures the security filter chain.
     *
     * The config now permits /v1/users/signin, /v1/users/refresh, and /v1/users/token to match the public endpoints in AuthController.
     * <p>
     * The configuration performs the following:
     * <ul>
     *   <li>Disables CSRF protection (since JWT is used and our system is stateless).</li>
     *   <li>Permits unauthenticated access to Swagger UI resources and all endpoints under /v1/auth/**.</li>
     *   <li>Requires authentication for all other endpoints.</li>
     *   <li>Sets session management to stateless (no HTTP sessions are stored).</li>
     *   <li>Adds the JwtAuthenticationFilter before the standard UsernamePasswordAuthenticationFilter.</li>
     * </ul>
     * </p>
     *
     * @param http an instance of HttpSecurity used for configuring the web security.
     * @return a fully configured SecurityFilterChain bean.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT usage
                .csrf(AbstractHttpConfigurer::disable)

                // Set up URL authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow open access to Swagger and API docs resources
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Allow unauthenticated access to authentication endpoints (adjusted to actual paths)
                        .requestMatchers("/v1/users/signin", "/v1/users/refresh-token", "/v1/users/token", "/v1/users/signup").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Set session management to stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Add the Firebase JWT authentication filter before the standard filter
                .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
