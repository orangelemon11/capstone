package org.example.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long ticketId,
        Long senderUserId,
        String message,
        LocalDateTime sentAt,
        Boolean read
) {
}