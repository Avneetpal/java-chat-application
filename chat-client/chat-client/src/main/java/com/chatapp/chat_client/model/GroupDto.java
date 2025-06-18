package com.chatapp.chat_client.model;

// This record must match the structure of the DTO on the server
public record GroupDto(Long id, String groupName) {
    // We override toString() so the group name is nicely displayed in the ListView
    @Override
    public String toString() {
        return groupName;
    }
}