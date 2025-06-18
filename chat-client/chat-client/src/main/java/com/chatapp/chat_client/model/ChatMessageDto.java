package com.chatapp.chat_client.model;

// A record is a compact class for holding data.
public class ChatMessageDto {

    // This represents the message the client will SEND to the server.
    public record ChatMessageRequest(Long groupId, Long senderId, String content) {}

    // This represents the message the client will RECEIVE from the server.
    public record ChatMessageResponse(String content, String senderUsername, String timestamp) {}
}