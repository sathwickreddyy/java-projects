package com.example.practisejdbl69.service;

import com.example.practisejdbl69.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void addUser(User user);
}
