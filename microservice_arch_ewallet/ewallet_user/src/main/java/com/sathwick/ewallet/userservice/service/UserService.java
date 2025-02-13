package com.sathwick.ewallet.userservice.service;

import com.sathwick.ewallet.userservice.domain.User;
import com.sathwick.ewallet.userservice.service.resource.TransactionRequest;
import com.sathwick.ewallet.userservice.service.resource.UserRequest;
import com.sathwick.ewallet.userservice.service.resource.UserResponse;

public interface UserService {
    void createUser(User user);
    UserResponse getUser(String userId);
    UserResponse deleteUser(String userId);
    UserResponse updateUser(UserRequest userRequest, String id);
    boolean transfer(Long userId, TransactionRequest request);
}
