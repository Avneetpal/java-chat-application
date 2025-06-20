package com.chatapp.chat_client.model;
import com.chatapp.chat_client.model.UserDto;

// This record must match the structure of the UserDto on the server
public record UserDto(Long id, String username) {
    // We override toString() so the username is nicely displayed in a ListView
    @Override
    public String toString() {
        return username;
    }
}