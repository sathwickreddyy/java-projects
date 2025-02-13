package com.sathwick.ewallet.userservice.service.resource;

import com.sathwick.ewallet.userservice.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String password;
    @Email
    private String email;
    @NotBlank
    private String phone;

    public User toUser(){
        return User.builder().name(name).password(password).email(email).phone(phone).build();
    }
}
