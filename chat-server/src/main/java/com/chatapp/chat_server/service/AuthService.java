package com.chatapp.chat_server.service;

import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Username already taken");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Invalid username or password"));

        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        } else {
            throw new IllegalStateException("Invalid username or password");
        }
    }
}