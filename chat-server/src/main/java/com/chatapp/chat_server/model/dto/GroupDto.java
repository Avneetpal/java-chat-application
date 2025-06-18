package com.chatapp.chat_server.model.dto;

import java.util.Set;

public record GroupDto(Long id, String groupName, Set<String> memberUsernames) {
}