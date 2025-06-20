package com.chatapp.chat_server.controller;

import com.chatapp.chat_server.model.dto.UserDto;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.service.AuthService;
import com.chatapp.chat_server.service.UserService; // <-- ADD this import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService; // <-- ADD the UserService dependency

    // UPDATED: The constructor now injects both services
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // DTO for the request body
    public record AuthRequest(String username, String password) {}

    /**
     * UPDATED: This now correctly calls the UserService to handle registration.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest registerRequest) {
        try {
            // FIXED: Calls userService.registerUser instead of authService.registerUser
            userService.registerUser(registerRequest.username(), registerRequest.password());
            return ResponseEntity.ok().body(Map.of("message", "User registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * This calls the AuthService to handle login.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest loginRequest) {
        try {
            User user = authService.loginUser(loginRequest.username(), loginRequest.password());
            // Return a DTO with the user ID for the client
            UserDto userDto = new UserDto(user.getId(), user.getUsername());
            return ResponseEntity.ok(userDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}