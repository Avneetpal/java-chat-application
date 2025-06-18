package com.chatapp.chat_server.controller;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.entity.Message;
import com.chatapp.chat_server.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    private final MessageService messageService;
    private final SimpMessageSendingOperations messagingTemplate;

    public MessageController(MessageService messageService, SimpMessageSendingOperations messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto.ChatMessageRequest chatMessageRequest) {
        Message savedMessage = messageService.saveMessage(chatMessageRequest);

        ChatMessageDto.ChatMessageResponse chatMessageResponse = new ChatMessageDto.ChatMessageResponse(
                savedMessage.getContent(),
                savedMessage.getSender().getUsername(),
                savedMessage.getSentAt()
        );

        String destination = "/topic/group/" + savedMessage.getGroup().getId();
        messagingTemplate.convertAndSend(destination, chatMessageResponse);
    }
}