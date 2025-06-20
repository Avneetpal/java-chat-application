package com.chatapp.chat_server.controller;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.dto.GroupDto;
import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.service.ChatGroupService;
import com.chatapp.chat_server.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatGroupController {

    private final ChatGroupService chatGroupService;
    private final MessageService messageService;

    public ChatGroupController(ChatGroupService chatGroupService, MessageService messageService) {
        this.chatGroupService = chatGroupService;
        this.messageService = messageService;
    }

    public record CreateGroupRequest(String groupName, Set<Long> memberIds) {}
    public record DirectMessageRequest(Long currentUserId, Long targetUserId) {}


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

    /**
     * THIS IS THE FINAL FIX.
     * The method now correctly uses the 'currentUserId' from the client's request
     * instead of the hardcoded value '1L'.
     */
    @PostMapping("/groups/dm")
    public ResponseEntity<GroupDto> getOrCreateDirectMessageGroup(@RequestBody DirectMessageRequest request) {
        // The current user's ID now correctly comes from the request body.
        ChatGroup group = chatGroupService.findOrCreatePrivateChat(request.currentUserId(), request.targetUserId());

        GroupDto groupDto = new GroupDto(
                group.getId(),
                group.getGroupName(),
                group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
        );
        return ResponseEntity.ok(groupDto);
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<ChatMessageDto.ChatMessageResponse>> getMessageHistory(@PathVariable Long groupId) {
        List<ChatMessageDto.ChatMessageResponse> history = messageService.getMessageHistory(groupId);
        return ResponseEntity.ok(history);
    }
}