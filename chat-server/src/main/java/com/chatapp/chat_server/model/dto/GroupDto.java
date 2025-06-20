package com.chatapp.chat_server.model.dto;

import java.util.Set;

/**
 * UPDATED: This Data Transfer Object now includes the ownerId,
 * so the client can know who the admin of the group is.
 */
public record GroupDto(
        Long id,
        String groupName,
        Set<String> memberUsernames,
        Long ownerId // ADDED: The ID of the user who owns the group
) {
}