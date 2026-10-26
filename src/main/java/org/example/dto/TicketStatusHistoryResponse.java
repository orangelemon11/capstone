package org.example.dto;

import org.example.models.TicketStatus;
import java.time.LocalDateTime;

public record TicketStatusHistoryResponse(
        Long id,
        Long ticketId,
        TicketStatus oldStatus,
        TicketStatus newStatus,
        Long changedByUserId,
        String comment,
        LocalDateTime changedAt
) {
}