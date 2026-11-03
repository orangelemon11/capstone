package org.example.dto;

public record TicketStatsResponse(
        long total,
        long open,
        long inProgress,
        long waitingForCustomer,
        long resolved,
        long closed
) {}