package org.example.controllers;


import org.example.dto.NotificationResponse;
import org.example.services.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationService notificationService;

    public NotificationsController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}/false")
    public List<NotificationResponse> getUserNotificationsReadFalse(
            @PathVariable Long userId
    ) {
        return notificationService.getUserNotificationsReadFalse(userId);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> getUserNotification(
            @PathVariable Long userId
    ) {
        return notificationService.getUserNotifications(userId);
    }
}
