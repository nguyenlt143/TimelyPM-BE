package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.config.NotificationWebSocketHandler;
import com.CapstoneProject.capstone.dto.response.notification.GetNotificationResponse;
import com.CapstoneProject.capstone.model.Notification;
import com.CapstoneProject.capstone.repository.NotificationRepository;
import com.CapstoneProject.capstone.service.INotificationService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public List<GetNotificationResponse> getNotifications() {
        UUID id = AuthenUtil.getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByUserIdAndActive(id);

        return notifications.stream().map(
                notification -> {
                    GetNotificationResponse response = new GetNotificationResponse();
                    response.setId(notification.getId());
                    response.setUserId(notification.getUser().getId());
                    response.setMessage(notification.getMessage());
                    response.setRead(notification.isRead());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String createdAtString = notification.getCreatedAt().format(formatter);
                    response.setCreateAt(createdAtString);
                    return response;
                }
        ).collect(Collectors.toList());
    }

    @Override
    public void createNotification(Notification notification) {
        notificationRepository.save(notification);
        GetNotificationResponse response = new GetNotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUser().getId());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        response.setCreateAt(notification.getCreatedAt().format(formatter));
        broadcastNotification(response);
    }

    public void broadcastNotification(GetNotificationResponse notification) {
        try {
            webSocketHandler.broadcastNotification(notification);
        } catch (IOException e) {
            System.err.println("Error broadcasting notification: " + e.getMessage());
        }
    }
}
