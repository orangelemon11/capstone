package org.example.repositories;

import org.example.models.TicketAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAssignmentHistoryRepository
    extends JpaRepository<TicketAssignmentHistory, Long> {

    List<TicketAssignmentHistory> findByTicket_Id(Long ticketId);
}
