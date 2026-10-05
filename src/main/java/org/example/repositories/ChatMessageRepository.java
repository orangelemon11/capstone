package org.example.repositories;

import org.example.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByTicketIdOrderBySentAtAsc(Long ticketId);
}