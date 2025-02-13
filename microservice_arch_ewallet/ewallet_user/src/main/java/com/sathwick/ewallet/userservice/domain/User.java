package com.sathwick.ewallet.userservice.domain;

import com.sathwick.ewallet.userservice.service.resource.UserResponse;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  userId;
    private String name;
    private String password;
    private String email;
    private String phone;

    public UserResponse toUserResponse(){
        return UserResponse.builder().name(name).email(email).userId(String.valueOf(userId)).build();
    }
}
