package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.Notification;
import com.lasu.hyperduty.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/duty/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list/{receiverId}")
    public ResponseResult<List<Notification>> getNotifications(@PathVariable Long receiverId) {
        List<Notification> notifications = notificationService.getAllNotifications(receiverId);
        return ResponseResult.success(notifications);
    }

    @GetMapping("/unread/{receiverId}")
    public ResponseResult<List<Notification>> getUnreadNotifications(@PathVariable Long receiverId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(receiverId);
        return ResponseResult.success(notifications);
    }

    @PutMapping("/read/{notificationId}")
    public ResponseResult<Void> markAsRead(@PathVariable Long notificationId) {
        boolean success = notificationService.markAsRead(notificationId);
        return success ? ResponseResult.success() : ResponseResult.error("标记失败");
    }

    @PutMapping("/read-all/{receiverId}")
    public ResponseResult<Void> markAllAsRead(@PathVariable Long receiverId) {
        notificationService.markAllAsRead(receiverId);
        return ResponseResult.success();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseResult<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseResult.success();
    }
}
