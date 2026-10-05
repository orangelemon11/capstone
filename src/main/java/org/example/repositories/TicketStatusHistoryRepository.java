package org.example.repositories;

import org.example.models.TicketStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketStatusHistoryRepository
        extends JpaRepository<TicketStatusHistory, Long> {

    List<TicketStatusHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}