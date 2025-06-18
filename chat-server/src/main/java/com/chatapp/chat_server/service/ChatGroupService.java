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

    /**
     * UPDATED: This method now correctly fetches only the groups for the specified user.
     * I've also added @Transactional, which is a good practice for service methods that read from the database.
     */
    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroupsForUser(Long userId) {
        // This is the only line that changed: from findAll() to our new findByMembers_Id(userId)
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
                    System.out.println("Creating new private chat between " + user1.getUsername() + " and " + user2.getUsername());
                    ChatGroup newPrivateChat = new ChatGroup();
                    newPrivateChat.setGroupName(user1.getUsername() + " & " + user2.getUsername());
                    newPrivateChat.getMembers().add(user1);
                    newPrivateChat.getMembers().add(user2);
                    return chatGroupRepository.save(newPrivateChat);
                });
    }
}