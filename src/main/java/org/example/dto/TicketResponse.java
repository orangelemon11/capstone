package org.example.dto;

import org.example.models.TicketPriority;
import org.example.models.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        Long customerId,
        Long assignedAgentId,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {}