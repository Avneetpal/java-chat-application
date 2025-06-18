package com.chatapp.chat_server.repository;

import com.chatapp.chat_server.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- THIS IS THE REQUIRED IMPORT
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // This method now has its 'List' type correctly imported
    List<User> findByUsernameContainingIgnoreCase(String username);
}