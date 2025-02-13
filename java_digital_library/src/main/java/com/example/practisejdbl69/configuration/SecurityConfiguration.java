package com.example.practisejdbl69.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    /*
     * Spring provides below classes for SecurityConfiguration
     * Secure our API s
     * 1. Onboard the users with credentials. -> UserObject -> UserDetails
     * 2. Accept username and password from user. -> AuthenticationProvider
     * 3. Fetch the user from the database. -> UserDetailsService Implementation
     * 4. Compare the password hash of the user. -> AuthenticationProvider -> PasswordEncoder
     * 5. Check if the user has authority on API. -> SecurityFilterChain
     * 6. Let the user use the API. -> AuthenticationProvider
     */
    @Bean
    public PasswordEncoder getPasswordEncoder(){
        // Step 4 helper create a password encoder
        return new BCryptPasswordEncoder();
    }

    // Step 5: Security Filter Chain
    // adding security filters for api's
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
//      // add a new key-pair with key as "X-CSRF-TOKEN" : value as the CSRF token received after hitting /csrf url
        httpSecurity.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/greet/**").hasAuthority("USER")
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/error").permitAll() // by default any runtime exception - all the requests will be redirected to error page.
                        .requestMatchers("/csrf").permitAll()
                        .anyRequest().authenticated()
        ).formLogin(Customizer.withDefaults()) // login with username and password from web login page
                .httpBasic(Customizer.withDefaults()) //  login with username and password from post man etc.
                .oauth2Login(Customizer.withDefaults()) // login with oauth2
                //  "{baseUrl}/{action}/oauth2/code/{registrationId}" pattern configuration in 3party
                // example: http://localhost:8080/login/oauth2/code/github for Authorization callback URL
                .cors(Customizer.withDefaults()) // Enabling CoRs
                .csrf(Customizer.withDefaults()); // Enabling CSRF;
        httpSecurity.csrf(csrf -> csrf.disable());
        return httpSecurity.build();
    }
}
