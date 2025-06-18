package com.chatapp.chat_server.repository;

import com.chatapp.chat_server.model.entity.ChatGroup;
import com.chatapp.chat_server.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // <-- ADDED required import
import java.util.Optional;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {

    /**
     * This custom query finds a group that contains exactly two specific members.
     * This is used to find existing 1-on-1 chats.
     */
    @Query("SELECT g FROM ChatGroup g WHERE SIZE(g.members) = 2 AND :user1 MEMBER OF g.members AND :user2 MEMBER OF g.members")
    Optional<ChatGroup> findPrivateChatGroupBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * ADDED: This method finds all groups that a specific user is a member of.
     * Spring Data JPA creates the query automatically from the method name.
     */
    List<ChatGroup> findByMembers_Id(Long userId);
}