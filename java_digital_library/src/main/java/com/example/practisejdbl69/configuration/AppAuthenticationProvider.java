package com.example.practisejdbl69.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

// Step 2: Spring Security
@Configuration
public class AppAuthenticationProvider implements AuthenticationProvider {

    @Autowired //created as part of Step3 in Security
    UserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder encoder;

    // Authentication is the bean provided by Spring with details of the username and password which has been provided by the user/
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Step 3 done
        if(Objects.nonNull(userDetails)){
//            if(userDetails.getPassword().equals(authentication.getCredentials().toString())){
//                Wrong step *****
//                return authentication;
//            }
            // Spring Security Step 4: PasswordEncoder from SecurityConfiguration
            if(encoder.matches(authentication.getCredentials().toString(), userDetails.getPassword())){
                return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
            }
        }
        throw new BadCredentialsException("Invalid Credentials");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // Different types of authentications we support
        if(UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)){
            return true;
        }
        else if(JaasAuthenticationToken.class.isAssignableFrom(authentication)){
            return true;
        }
        return false;
    }
}
