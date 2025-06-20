package com.chatapp.chat_client.model;

import java.time.Instant;

public class ChatMessageDto {

    public record ChatMessageRequest(Long groupId, Long senderId, String content) {}

    /**
     * UPDATED: This record now matches the server's response, including the groupId.
     */
    public record ChatMessageResponse(Long groupId, String content, String senderUsername, Instant sentAt) {}
}