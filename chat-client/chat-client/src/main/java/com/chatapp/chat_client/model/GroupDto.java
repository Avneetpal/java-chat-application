package com.chatapp.chat_client.model;

import com.chatapp.chat_client.service.UserSession;
import java.util.Objects;
import java.util.Set;

/**
 * This record represents a chat group's data as needed by the client.
 * It is immutable, which is good practice for data transfer objects.
 */
public record GroupDto(Long id, String groupName, Set<String> memberUsernames) {

    /**
     * This is the final, correct logic for displaying chat names in the UI list.
     * It intelligently decides what name to show based on the number of members.
     */
    @Override
    public String toString() {
        String loggedInUsername = UserSession.getInstance().getUsername();

        // If the user session isn't ready for some reason, just show the group name as a fallback.
        if (loggedInUsername == null) {
            return groupName;
        }

        // Case 1: This is a 2-person direct message.
        if (memberUsernames != null && memberUsernames.size() == 2) {
            // Find the username that is NOT the logged-in user and return it.
            return memberUsernames.stream()
                    .filter(name -> !name.equals(loggedInUsername))
                    .findFirst()
                    .orElse(groupName); // Fallback to the raw group name (e.g., "user1 & user2")
        }

        // Case 2: This is a multi-person (3+) group chat, or you are alone in a group.
        // In this case, always show the proper group name (e.g., "My Study Group").
        return groupName;
    }

    /**
     * This custom `equals` method is crucial for JavaFX.
     * It ensures that when we tell the ListView to select or remove a group,
     * it can correctly find the group in its list by comparing only the unique ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDto groupDto = (GroupDto) o;
        return Objects.equals(id, groupDto.id);
    }

    /**
     * When you override `equals`, you must also override `hashCode`.
     * This ensures that data structures like HashSets and HashMaps work correctly.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}