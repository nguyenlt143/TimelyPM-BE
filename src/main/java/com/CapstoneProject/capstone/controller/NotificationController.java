package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.notification.GetNotificationResponse;
import com.CapstoneProject.capstone.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.NOTIFICATION.NOTIFICATION)
public class NotificationController {
    private final INotificationService notificationService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(UrlConstant.NOTIFICATION.GET_ALL)
    public ResponseEntity<BaseResponse<List<GetNotificationResponse>>> getNotifications() {
        List<GetNotificationResponse> response = notificationService.getNotifications();
        return ResponseEntity.ok(new BaseResponse<>("200", "List notification", response));
    }
}
