package com.chatapp.chat_server.repository;

import com.chatapp.chat_server.model.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<ChatGroup, Long> {
}