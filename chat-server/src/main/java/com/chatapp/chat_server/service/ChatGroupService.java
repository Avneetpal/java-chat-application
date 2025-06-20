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

    @Transactional
    public ChatGroup createGroup(String groupName, Set<Long> memberIds, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner with ID " + ownerId + " not found"));

        Set<User> members = new HashSet<>(userRepository.findAllById(memberIds));
        members.add(owner);

        ChatGroup newGroup = new ChatGroup();
        newGroup.setGroupName(groupName);
        newGroup.setMembers(members);
        newGroup.setOwner(owner);

        return chatGroupRepository.save(newGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroupsForUser(Long userId) {
        return chatGroupRepository.findByMembers_Id(userId)
                .stream()
                .map(group -> new GroupDto(
                        group.getId(),
                        group.getGroupName(),
                        group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet()),
                        group.getOwner().getId() // UPDATED: Now includes the owner's ID in the DTO
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
                    newPrivateChat.setMembers(Set.of(user1, user2));
                    newPrivateChat.setOwner(user1); // UPDATED: Sets the initiator as the owner of the new DM
                    return chatGroupRepository.save(newPrivateChat);
                });
    }
    // Add this new method to your ChatGroupService class

    @Transactional
    public void deleteGroup(Long groupId, Long requestingUserId) {
        // Find the group to be deleted
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        // IMPORTANT: Verify that the user requesting the deletion is the actual owner
        if (!group.getOwner().getId().equals(requestingUserId)) {
            throw new IllegalStateException("Only the group owner can delete this group.");
        }

        // If the check passes, delete the group.
        // Due to cascading settings, this should also remove related memberships and messages.
        chatGroupRepository.delete(group);
    }

    @Transactional
    public ChatGroup addMembersToGroup(Long groupId, Set<Long> memberIdsToAdd) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        Set<User> newMembers = new HashSet<>(userRepository.findAllById(memberIdsToAdd));
        group.getMembers().addAll(newMembers);

        return group;
    }

    @Transactional
    public void removeUserFromGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (group.getMembers().contains(user)) {
            group.getMembers().remove(user);

            if (group.getMembers().isEmpty()) {
                chatGroupRepository.delete(group);
            }
        } else {
            throw new IllegalStateException("User is not a member of this group.");
        }
    }
}