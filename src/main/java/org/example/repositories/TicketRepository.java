package org.example.repositories;

import org.example.models.Ticket;
import org.example.models.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByAssignedAgentId(Long agentId);
}