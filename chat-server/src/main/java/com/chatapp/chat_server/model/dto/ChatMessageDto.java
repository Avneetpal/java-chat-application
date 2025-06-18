package com.chatapp.chat_server.model.dto;

import java.time.Instant;

public class ChatMessageDto {
    public record ChatMessageRequest(Long groupId, Long senderId, String content) {}
    public record ChatMessageResponse(String content, String senderUsername, Instant timestamp) {}
}