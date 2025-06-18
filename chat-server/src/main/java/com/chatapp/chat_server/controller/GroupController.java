package com.chatapp.chat_server.controller;
import com.chatapp.chat_server.model.dto.GroupDto;

import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    public record CreateGroupRequest(String groupName, Set<Long> memberIds) {}

    @PostMapping
    public ResponseEntity<ChatGroup> createGroup(@RequestBody CreateGroupRequest request) {
        ChatGroup newGroup = groupService.createGroup(request.groupName(), request.memberIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroup);
    }
    // Add this new method to GroupController.java
    @GetMapping
    public ResponseEntity<List<GroupDto>> getUserGroups() {
        // This is a placeholder for getting the logged-in user's ID.
        // We will replace this with real security logic later.
        Long currentUserId = 1L; // Assuming user with ID 1 for now
        List<GroupDto> groups = groupService.getAllGroupsForUser(currentUserId);
        return ResponseEntity.ok(groups);
    }
}