package com.chatapp.chat_server.repository;

import com.chatapp.chat_server.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * UPDATED: The method name now correctly refers to the 'group' field in the Message entity.
     * Spring Data JPA will find the 'group' field and then look for its 'id' property.
     */
    List<Message> findByGroupIdOrderBySentAtAsc(Long groupId);

}