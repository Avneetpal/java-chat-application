package com.chatapp.chat_server.repository;

import com.chatapp.chat_server.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}