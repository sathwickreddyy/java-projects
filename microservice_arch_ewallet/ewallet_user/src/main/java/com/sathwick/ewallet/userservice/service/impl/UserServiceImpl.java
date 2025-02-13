package com.sathwick.ewallet.userservice.service.impl;

import com.sathwick.ewallet.userservice.domain.User;
import com.sathwick.ewallet.userservice.exception.UserException;
import com.sathwick.ewallet.userservice.repository.UserRepository;
import com.sathwick.ewallet.userservice.service.UserService;
import com.sathwick.ewallet.userservice.service.resource.TransactionRequest;
import com.sathwick.ewallet.userservice.service.resource.UserRequest;
import com.sathwick.ewallet.userservice.service.resource.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${kafka.topic.user-created}")
    private String USER_CREATED_TOPIC;

    @Value("${kafka.topic.user-deleted}")
    private String USER_DELETED_TOPIC;

    @Autowired
    private RestTemplate restTemplate;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void createUser(User user) {
        Optional<User> optionalUser = userRepository.findByName(user.getName());
        // check if user is valid
        // check if username exists
        if (optionalUser.isPresent()) {
            throw new UserException("EWALLET_USER_EXISTS_EXCEPTION", "User " + optionalUser.get().getName() + " Already Exists");
        }
        // Encode the password before storing
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // save the user to db
        userRepository.save(user);
        try {
            // create a user created event and wallet service will consume this event and creates the wallet service asynchronously
            kafkaTemplate.send(USER_CREATED_TOPIC, String.valueOf(user.getUserId()));
        }
        catch (Exception e){
            // Rollback user creation
            userRepository.delete(user);
            log.error("Kafka Exception "+e.getMessage());
        }
    }

    @Override
    public UserResponse getUser(String userId) {
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userId));
        User user = optionalUser.orElseThrow(() -> new UserException("EWALLET_USER_NOT_FOUND_EXCEPTION", "User Not Found"));
        return new UserResponse(user);
    }

    @Override
    public UserResponse deleteUser(String userId) {
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new UserException("EWALLET_USER_NOT_FOUND_EXCEPTION", "User Not Found"));
        userRepository.deleteById(Long.valueOf(userId));
        // send event to wallet service to delete wallet
        kafkaTemplate.send(USER_DELETED_TOPIC, userId);
        return new UserResponse(user);
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest, String id) {
        // Fetch user, if not present throw exception
        User existingUser = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UserException("EWALLET_USER_NOT_FOUND_EXCEPTION", "User Not Found"));

        // Check if there are any changes between the existing user and the user request
        boolean hasChanges = !userRequest.getEmail().equals(existingUser.getEmail()) ||
                !userRequest.getName().equals(existingUser.getName()) ||
                !passwordEncoder.matches(userRequest.getPassword(), existingUser.getPassword()) ||
                !userRequest.getPhone().equals(existingUser.getPhone());

        if (!hasChanges) {
            throw new UserException("EWALLET_NO_CHANGES_FOUND_EXCEPTION", "No Changes Found");
        }

        // Update the user object with the new values from the request
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setName(userRequest.getName());
        existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        existingUser.setPhone(userRequest.getPhone());

        // Save the updated user to the repository
        User updatedUser = userRepository.save(existingUser);

        // Return the updated user response
        return new UserResponse(updatedUser);
    }

    @Override
    public boolean transfer(Long senderId, TransactionRequest request) {
        try {
            log.info("Initiaing transfer from user "+senderId+" with request "+request.toString());
            Optional<User> senderOptional = userRepository.findById(senderId);
            if (senderOptional.isEmpty()) {
                throw new UserException("EWALLET_USER_NOT_FOUND_EXCEPTION", "Sender with id +" + senderId + "+ Not Found");
            }
            Optional<User> receiverOptional = userRepository.findById(request.getReceiverId());
            if (receiverOptional.isEmpty()) {
                throw new UserException("EWALLET_USER_NOT_FOUND_EXCEPTION", "Receiver with id +" + request.getReceiverId() + "+ Not Found");
            }
            // balance validation will be done at wallet service
            // now perform a rest call to Transaction Service and call should be according to controller of transaction service.
            ResponseEntity<Boolean> response = restTemplate.postForEntity("http://transaction/transactions/" + senderId, request, Boolean.class);
            log.info("Transfer response "+response.getBody());
            return response.getBody();
        }
        catch (Exception e){
            log.error("Internal Server Error: Exception in transfer "+e.getMessage());
            return false;
        }
    }

}
