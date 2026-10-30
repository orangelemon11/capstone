package org.example.services;

import org.springframework.transaction.annotation.Transactional;
import org.example.dto.NotificationResponse;
import org.example.models.Notification;
import org.example.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public List<NotificationResponse> getUserNotificationsReadFalse(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(Notification not) {
        return new NotificationResponse(
                not.getId(),
                not.getUser().getId(),
                not.getTicket() != null ? not.getTicket().getId() : null,
                not.getType(),
                not.getTitle(),
                not.getMessage(),
                not.getRead(),
                not.getCreatedAt()
        );
    }
}
