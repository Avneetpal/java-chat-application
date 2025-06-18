package com.chatapp.chat_server.service;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.model.entity.Message;
import com.chatapp.chat_server.repository.MessageRepository;
import com.chatapp.chat_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Message saveMessage(ChatMessageDto.ChatMessageRequest request) {
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found with ID: " + request.senderId()));

        ChatGroup group = new ChatGroup();
        group.setId(request.groupId());

        Message message = new Message();
        message.setSender(sender);
        message.setGroup(group);
        message.setContent(request.content());

        return messageRepository.save(message);
    }
}