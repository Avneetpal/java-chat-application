package com.chatapp.chat_server.service;

import com.chatapp.chat_server.model.dto.ChatMessageDto;
import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.Message;
import com.chatapp.chat_server.model.entity.User;
import com.chatapp.chat_server.repository.ChatGroupRepository; // Ensure this is imported
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
    private final ChatGroupRepository chatGroupRepository; // Dependency is added

    // Constructor is updated to inject ChatGroupRepository
    public MessageService(MessageRepository messageRepository, UserRepository userRepository, ChatGroupRepository chatGroupRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    /**
     * UPDATED: This is the critical fix. The method now fetches the real group
     * from the database before saving the message.
     */
    @Transactional
    public Message saveMessage(ChatMessageDto.ChatMessageRequest request) {
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found with ID: " + request.senderId()));

        // This is the most important change: We fetch the real group.
        ChatGroup group = chatGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + request.groupId()));

        Message message = new Message();
        message.setSender(sender);
        message.setGroup(group); // We use the real, managed group object
        message.setContent(request.content());

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto.ChatMessageResponse> getMessageHistory(Long groupId) {
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId)
                .stream()
                .map(message -> new ChatMessageDto.ChatMessageResponse(
                        message.getContent(),
                        message.getSender().getUsername(),
                        message.getSentAt()
                ))
                .collect(Collectors.toList());
    }
}