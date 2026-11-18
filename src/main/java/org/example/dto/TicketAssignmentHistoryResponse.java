package org.example.dto;

import java.time.LocalDateTime;

public record TicketAssignmentHistoryResponse (
        Long id,
        Long ticketId,
        Long previousAgentId,
        Long newAgentId,
        Long changedByUserId,
        LocalDateTime changedAt
){}
