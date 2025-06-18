package com.chatapp.chat_client.model;

import com.chatapp.chat_client.service.UserSession;
import java.util.Set;

// This record now needs to accept the memberUsernames field from the server's JSON
public record GroupDto(Long id, String groupName, Set<String> memberUsernames) {

    /**
     * This is the smart part. This method is automatically called by JavaFX
     * when it wants to display the object in a ListView.
     */
    @Override
    public String toString() {
        // Get the currently logged-in user's name from our session
        String loggedInUsername = UserSession.getInstance().getUsername();

        // Check if this is a private, 2-person chat
        if (memberUsernames != null && memberUsernames.size() == 2) {
            // Find the name that is NOT the logged-in user's name
            return memberUsernames.stream()
                    .filter(name -> !name.equals(loggedInUsername))
                    .findFirst() // Get the other person's name
                    .orElse(groupName); // Or fallback to the group name if something is wrong
        }

        // If it's a group chat with more than 2 people, just return the group name
        return groupName;
    }
}