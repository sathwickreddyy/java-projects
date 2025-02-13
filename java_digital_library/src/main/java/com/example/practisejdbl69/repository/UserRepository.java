package com.example.practisejdbl69.repository;

import com.example.practisejdbl69.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserByEmail(String email);
    Optional<User> findUserById(Integer id);

    Optional<User> findUserByName(String name);
}
