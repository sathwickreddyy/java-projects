package com.java.ticketbookingsystem.userservice.service.users.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.java.ticketbookingsystem.userservice.dto.UserDetails;
import com.java.ticketbookingsystem.userservice.dto.UserDetailsResponse;
import com.java.ticketbookingsystem.userservice.exception.TBSUserServiceException;
import com.java.ticketbookingsystem.userservice.service.users.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("firebaseUserService")
public class FirebaseUserServiceImpl implements UserService {

    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    public FirebaseUserServiceImpl(FirebaseAuth firebaseAuth, Firestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
        log.info("FirebaseUserServiceImpl initialized");
    }

    /**
     * Retrieves detailed user information from Cognito.
     *
     * @param uid The unique identifier of the user
     * @return UserDetails object containing user information
     * @throws TBSUserServiceException if there's an error fetching user details
     */
    @Override
    public UserDetailsResponse getUserDetails(String uid) {
        try {
            log.info("Fetching user details for {}", uid);
            DocumentSnapshot doc = firestore.collection("users").document(uid).get().get();
            return UserDetailsResponse.builder()
                    .username(doc.getString("username"))
                    .email(doc.getString("email"))
                    .name(doc.getString("name"))
                    .phoneNumber(doc.getString("phoneNumber"))
                    .gender(doc.getString("gender"))
                    .role(mapRolesToAuthorities(doc.get("roles")).toString())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching user details: {}", e.getMessage());
            throw new TBSUserServiceException("User details fetch failed");
        }
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Object roles) {
        if (roles instanceof List) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Updates the role of a specified user in Cognito.
     *
     * @param uid  The unique identifier of the user
     * @param role The new role to be assigned
     * @throws TBSUserServiceException if there's an error updating the user role
     */
    @Override
    public void updateUserRole(String uid, UserDetails.UserRole role) {
        try {
            log.info("Updating role for {} to {}", uid, role);

            // Update Firestore document
            Map<String, Object> updates = new HashMap<>();
            updates.put("roles", Collections.singletonList(role.name()));
            firestore.collection("users").document(uid).update(updates).get();

            // Update custom claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", Collections.singletonList(role.name()));
            firebaseAuth.setCustomUserClaims(uid, claims);
        } catch (Exception e) {
            log.error("Role update failed: {}", e.getMessage());
            throw new TBSUserServiceException("Role update failed", e);
        }
    }

    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDetails object representing the authenticated user
     */
    @Override
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof FirebaseToken) {
                return ((FirebaseToken) principal).getUid();
            }
            return principal.toString();
        }
        return "anonymousUser";
    }

}
