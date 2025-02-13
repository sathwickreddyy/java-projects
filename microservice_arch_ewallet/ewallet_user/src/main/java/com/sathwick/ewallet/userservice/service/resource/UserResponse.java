package com.sathwick.ewallet.userservice.service.resource;

import com.sathwick.ewallet.userservice.domain.User;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String userId;
    private String name;
    private String email;

    public UserResponse(User user){
        this.userId = user.getUserId().toString();
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
