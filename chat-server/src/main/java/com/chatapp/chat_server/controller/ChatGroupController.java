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
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // UPDATED: Request record now includes the ownerId
    public record CreateGroupRequest(String groupName, Set<Long> memberIds, Long ownerId) {}
    public record DirectMessageRequest(Long currentUserId, Long targetUserId) {}
    public record AddMembersRequest(Set<Long> memberIds) {}


    @PostMapping("/groups")
    public ResponseEntity<GroupDto> createGroup(@RequestBody CreateGroupRequest request) {
        // UPDATED: Pass the ownerId to the service method
        ChatGroup newGroup = chatGroupService.createGroup(request.groupName(), request.memberIds(), request.ownerId());

        // UPDATED: The DTO now includes the owner's ID
        GroupDto groupDto = new GroupDto(
                newGroup.getId(),
                newGroup.getGroupName(),
                newGroup.getMembers().stream().map(User::getUsername).collect(Collectors.toSet()),
                newGroup.getOwner().getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(groupDto);
    }

    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<GroupDto>> getGroupsForUser(@PathVariable Long userId) {
        // This method is now correct because the service it calls was already updated
        return ResponseEntity.ok(chatGroupService.getAllGroupsForUser(userId));
    }

    @PostMapping("/groups/dm")
    public ResponseEntity<GroupDto> getOrCreateDirectMessageGroup(@RequestBody DirectMessageRequest request) {
        ChatGroup group = chatGroupService.findOrCreatePrivateChat(request.currentUserId(), request.targetUserId());

        // UPDATED: The DTO now includes the owner's ID
        GroupDto groupDto = new GroupDto(
                group.getId(),
                group.getGroupName(),
                group.getMembers().stream().map(User::getUsername).collect(Collectors.toSet()),
                group.getOwner().getId()
        );
        return ResponseEntity.ok(groupDto);
    }

    @DeleteMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            chatGroupService.removeUserFromGroup(groupId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<GroupDto> addMembersToGroup(@PathVariable Long groupId, @RequestBody AddMembersRequest request) {
        try {
            ChatGroup updatedGroup = chatGroupService.addMembersToGroup(groupId, request.memberIds());

            // UPDATED: The DTO now includes the owner's ID
            GroupDto groupDto = new GroupDto(
                    updatedGroup.getId(),
                    updatedGroup.getGroupName(),
                    updatedGroup.getMembers().stream().map(User::getUsername).collect(Collectors.toSet()),
                    updatedGroup.getOwner().getId()
            );
            return ResponseEntity.ok(groupDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // Add this new record inside your ChatGroupController, with the other records
    public record DeleteGroupRequest(Long userId) {}


    // Add this new endpoint method to your ChatGroupController class
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId, @RequestBody DeleteGroupRequest request) {
        try {
            chatGroupService.deleteGroup(groupId, request.userId());
            return ResponseEntity.ok().build(); // Return 200 OK on success
        } catch (IllegalStateException e) {
            // Return 403 Forbidden if the user is not the owner
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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