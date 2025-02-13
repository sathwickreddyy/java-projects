package com.example.practisejdbl69.service.impl;

import com.example.practisejdbl69.exception.UserAlreadyExistsException;
import com.example.practisejdbl69.domain.User;
import com.example.practisejdbl69.repository.UserRepository;
import com.example.practisejdbl69.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Step 3: Spring Security.
@Service
public class UserDetailsServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findUserByName(username);
        return optionalUser.orElseThrow(()-> new UsernameNotFoundException("User "+username+" Not Found"));
    }

    @Override
    public void addUser(User user) {
        Optional<User> existingUser = userRepository.findUserByName(user.getName());
        if(existingUser.isEmpty()){
            user.setAuthority("USER");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        }
        else {
            throw new UserAlreadyExistsException("User " + user.getName() + " Already Exists");
        }
    }
}
