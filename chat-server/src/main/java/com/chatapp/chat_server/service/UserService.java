package com.chatapp.chat_server.service;

import com.chatapp.chat_server.model.dto.UserDto;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        // Convert the User entities to safe UserDto objects before sending
        return users.stream()
                .map(user -> new UserDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
    }
}