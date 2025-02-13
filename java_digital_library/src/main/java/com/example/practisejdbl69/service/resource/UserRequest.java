package com.example.practisejdbl69.service.resource;

import com.example.practisejdbl69.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
    @Email
    private String email;
    @NotBlank
    private String phoneNumber;

    public User toUser(){
        return User.builder().name(userName).password(password).email(email).phoneNumber(phoneNumber).authority("USER").build();
    }

    public User getAdminUser(){
        return User.builder().name(userName).password(password).email(email).phoneNumber(phoneNumber).authority("ADMIN").build();
    }
}
