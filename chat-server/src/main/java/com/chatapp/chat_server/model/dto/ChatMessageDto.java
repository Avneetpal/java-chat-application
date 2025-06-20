package com.chatapp.chat_server.model.dto;

import java.time.Instant;

public class ChatMessageDto {

    public record ChatMessageRequest(Long groupId, Long senderId, String content) {}

    /**
     * UPDATED: Added 'groupId' to the response record.
     */
    public record ChatMessageResponse(Long groupId, String content, String senderUsername, Instant sentAt) {}
}