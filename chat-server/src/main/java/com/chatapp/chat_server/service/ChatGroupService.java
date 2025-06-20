package com.chatapp.chat_server.service;

import com.chatapp.chat_server.model.dto.GroupDto;
import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.repository.ChatGroupRepository;
import com.chatapp.chat_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final UserRepository userRepository;

    public ChatGroupService(ChatGroupRepository chatGroupRepository, UserRepository userRepository) {
        this.chatGroupRepository = chatGroupRepository;
        this.userRepository = userRepository;
    }

    public ChatGroup createGroup(String groupName, Set<Long> memberIds) {
        Set<User> members = new HashSet<>(userRepository.findAllById(memberIds));
        ChatGroup newGroup = new ChatGroup();
        newGroup.setGroupName(groupName);
        newGroup.setMembers(members);
        return chatGroupRepository.save(newGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroupsForUser(Long userId) {
        return chatGroupRepository.findByMembers_Id(userId)
                .stream()
                .map(group -> new GroupDto(
                        group.getId(),
                        group.getGroupName(),
                        group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatGroup findOrCreatePrivateChat(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + user1Id + " not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + user2Id + " not found"));

        return chatGroupRepository.findPrivateChatGroupBetweenUsers(user1, user2)
                .orElseGet(() -> {
                    ChatGroup newPrivateChat = new ChatGroup();
                    newPrivateChat.setGroupName(user1.getUsername() + " & " + user2.getUsername());
                    newPrivateChat.getMembers().add(user1);
                    newPrivateChat.getMembers().add(user2);
                    return chatGroupRepository.save(newPrivateChat);
                });
    }
    // Add this new method to your ChatGroupService class
    // Add this new method to your ChatGroupService class

    @Transactional
    public ChatGroup addMembersToGroup(Long groupId, Set<Long> memberIdsToAdd) {
        // Find the existing group
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        // Find all the User entities for the IDs we want to add
        Set<User> newMembers = new HashSet<>(userRepository.findAllById(memberIdsToAdd));

        // Add the new members to the group's existing member list.
        // The Set data structure automatically handles duplicates, so we don't need to
        // worry if a user is already in the group.
        group.getMembers().addAll(newMembers);

        // Because the 'group' entity is managed by JPA, any changes to it
        // within a @Transactional method are automatically saved. We don't even need to call 'save()'.
        return group;
    }

    @Transactional
    public void removeUserFromGroup(Long groupId, Long userId) {
        // Find the group and the user from the database
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Remove the user from the group's set of members
        if (group.getMembers().contains(user)) {
            group.getMembers().remove(user);

            // If the group has no members left after removal, delete the group itself
            if (group.getMembers().isEmpty()) {
                chatGroupRepository.delete(group);
            }
            // If members still exist, the change to the members list will be saved automatically
            // by JPA at the end of the transaction.
        } else {
            throw new IllegalStateException("User is not a member of this group.");
        }
    }
}