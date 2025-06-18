package com.chatapp.chat_server.controller;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.dto.GroupDto;
import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.service.ChatGroupService;
import com.chatapp.chat_server.service.MessageService;
import com.chatapp.chat_server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // Changed base path for clarity
public class ChatGroupController {

    private final ChatGroupService chatGroupService;
    private final MessageService messageService;

    public ChatGroupController(ChatGroupService chatGroupService, MessageService messageService) {
        this.chatGroupService = chatGroupService;
        this.messageService = messageService;
    }

    // --- DTOs for Requests ---
    public record CreateGroupRequest(String groupName, Set<Long> memberIds) {}
    public record DirectMessageRequest(Long targetUserId) {}


    // --- Group Endpoints ---
    @PostMapping("/groups")
    public ResponseEntity<GroupDto> createGroup(@RequestBody CreateGroupRequest request) {
        ChatGroup newGroup = chatGroupService.createGroup(request.groupName(), request.memberIds());
        GroupDto groupDto = new GroupDto(
                newGroup.getId(),
                newGroup.getGroupName(),
                newGroup.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(groupDto);
    }

    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<GroupDto>> getGroupsForUser(@PathVariable Long userId) {
        List<GroupDto> groups = chatGroupService.getAllGroupsForUser(userId);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/groups/dm")
    public ResponseEntity<GroupDto> getOrCreateDirectMessageGroup(@RequestBody DirectMessageRequest request) {
        // Placeholder for the currently logged-in user's ID.
        Long currentUserId = 1L;
        ChatGroup group = chatGroupService.findOrCreatePrivateChat(currentUserId, request.targetUserId());
        GroupDto groupDto = new GroupDto(
                group.getId(),
                group.getGroupName(),
                group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
        );
        return ResponseEntity.ok(groupDto);
    }

    // --- Message History Endpoint ---
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<ChatMessageDto.ChatMessageResponse>> getMessageHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(messageService.getMessageHistory(groupId));
    }
}