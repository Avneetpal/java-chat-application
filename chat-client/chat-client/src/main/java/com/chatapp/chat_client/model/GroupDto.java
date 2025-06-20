package com.chatapp.chat_client.model;

import com.chatapp.chat_client.service.UserSession;
import java.util.Objects;
import java.util.Set;

/**
 * UPDATED: This record now includes the ownerId to identify the group's admin.
 */
public record GroupDto(Long id, String groupName, Set<String> memberUsernames, Long ownerId) {

    @Override
    public String toString() {
        String loggedInUsername = UserSession.getInstance().getUsername();
        if (loggedInUsername == null) {
            return groupName;
        }

        // Handle 2-person DMs
        if (memberUsernames != null && memberUsernames.size() == 2) {
            return memberUsernames.stream()
                    .filter(name -> !name.equals(loggedInUsername))
                    .findFirst()
                    .orElse(groupName);
        }

        // Handle multi-person groups
        return groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDto groupDto = (GroupDto) o;
        return Objects.equals(id, groupDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}