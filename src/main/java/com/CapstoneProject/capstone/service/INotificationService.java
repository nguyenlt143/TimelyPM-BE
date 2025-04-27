package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.notification.GetNotificationResponse;
import com.CapstoneProject.capstone.model.Notification;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    List<GetNotificationResponse> getNotifications();
    void createNotification(Notification notification);
    void broadcastNotification(GetNotificationResponse notification);
}
