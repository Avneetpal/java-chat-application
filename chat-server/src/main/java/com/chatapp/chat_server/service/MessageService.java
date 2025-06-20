package com.chatapp.chat_server.service;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.Message;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.repository.ChatGroupRepository;
import com.chatapp.chat_server.repository.MessageRepository;
import com.chatapp.chat_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository, ChatGroupRepository chatGroupRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    @Transactional
    public Message saveMessage(ChatMessageDto.ChatMessageRequest request) {
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found with ID: " + request.senderId()));

        ChatGroup group = chatGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + request.groupId()));

        Message message = new Message();
        message.setSender(sender);
        message.setGroup(group);
        message.setContent(request.content());

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto.ChatMessageResponse> getMessageHistory(Long groupId) {
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId)
                .stream()
                .map(message -> new ChatMessageDto.ChatMessageResponse(
                        message.getGroup().getId(), // UPDATED: Pass the groupId
                        message.getContent(),
                        message.getSender().getUsername(),
                        message.getSentAt()
                ))
                .collect(Collectors.toList());
    }
}