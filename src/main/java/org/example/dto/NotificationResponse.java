package org.example.dto;

import org.example.models.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        Long ticketId,
        NotificationType type,
        String title,
        String message,
        Boolean read,
        LocalDateTime createdAt
) {}