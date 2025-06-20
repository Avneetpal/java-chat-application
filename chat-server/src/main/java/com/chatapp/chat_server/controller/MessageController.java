package com.chatapp.chat_server.controller;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.entity.Message;
import com.chatapp.chat_server.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public MessageController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto.ChatMessageRequest chatMessage) {
        Message savedMessage = messageService.saveMessage(chatMessage);

        // UPDATED: Create the response DTO with the groupId
        ChatMessageDto.ChatMessageResponse response = new ChatMessageDto.ChatMessageResponse(
                savedMessage.getGroup().getId(), // Pass the groupId
                savedMessage.getContent(),
                savedMessage.getSender().getUsername(),
                savedMessage.getSentAt()
        );

        messagingTemplate.convertAndSend("/topic/group/" + chatMessage.groupId(), response);
    }
}