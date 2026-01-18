package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    void sendNotification(Long receiverId, String receiverName, String title, String content, 
                       String type, Long relatedId, String relatedType);

    List<Notification> getUnreadNotifications(Long receiverId);

    List<Notification> getAllNotifications(Long receiverId);

    boolean markAsRead(Long notificationId);

    void markAllAsRead(Long receiverId);

    void deleteNotification(Long notificationId);
}
