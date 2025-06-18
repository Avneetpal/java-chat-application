package com.chatapp.chat_server.service;
import com.chatapp.chat_server.model.dto.GroupDto;
import com.chatapp.chat_server.model.entity.User;
import java.util.List;
import java.util.stream.Collectors;

import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.repository.GroupRepository;
import com.chatapp.chat_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public ChatGroup createGroup(String groupName, Set<Long> memberIds) {
        Set<User> members = new HashSet<>(userRepository.findAllById(memberIds));
        ChatGroup newGroup = new ChatGroup();
        newGroup.setGroupName(groupName);
        newGroup.setMembers(members);
        return groupRepository.save(newGroup);
    }
    // Add this new method to GroupService.java
    public List<GroupDto> getAllGroupsForUser(Long userId) {
        // This is a simplified version. A real app would have a more complex query.
        // For now, we get all groups and will filter them later.
        return groupRepository.findAll()
                .stream()
                .map(group -> new GroupDto(
                        group.getId(),
                        group.getGroupName(),
                        group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
    }
}