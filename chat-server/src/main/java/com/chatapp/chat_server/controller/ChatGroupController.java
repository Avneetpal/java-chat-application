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
        return ResponseEntity.ok(chatGroupService.getAllGroupsForUser(userId));
    }

    @PostMapping("/groups/dm")
    public ResponseEntity<GroupDto> getOrCreateDirectMessageGroup(@RequestBody DirectMessageRequest request) {
        ChatGroup group = chatGroupService.findOrCreatePrivateChat(request.currentUserId(), request.targetUserId());
        GroupDto groupDto = new GroupDto(
                group.getId(),
                group.getGroupName(),
                group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
        );
        return ResponseEntity.ok(groupDto);
    }
    // Add this new endpoint method to your ChatGroupController class

    @DeleteMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            chatGroupService.removeUserFromGroup(groupId, userId);
            // Return 200 OK for successful operation
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // Return 404 Not Found if the group or user doesn't exist
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Return 400 Bad Request if the user isn't in the group
            return ResponseEntity.badRequest().build();
        }
    }
    // Add this new record inside your ChatGroupController, with the other records
    public record AddMembersRequest(Set<Long> memberIds) {}

    // Add this new endpoint method to your ChatGroupController class
    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<GroupDto> addMembersToGroup(@PathVariable Long groupId, @RequestBody AddMembersRequest request) {
        try {
            ChatGroup updatedGroup = chatGroupService.addMembersToGroup(groupId, request.memberIds());

            // Convert the updated group to a DTO to send back to the client
            GroupDto groupDto = new GroupDto(
                    updatedGroup.getId(),
                    updatedGroup.getGroupName(),
                    updatedGroup.getMembers().stream().map(User::getUsername).collect(Collectors.toSet())
            );
            return ResponseEntity.ok(groupDto);
        } catch (IllegalArgumentException e) {
            // Return 404 Not Found if the group doesn't exist
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<ChatMessageDto.ChatMessageResponse>> getMessageHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(messageService.getMessageHistory(groupId));
    }
}