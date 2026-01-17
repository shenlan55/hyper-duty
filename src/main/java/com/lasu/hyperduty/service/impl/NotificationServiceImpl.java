package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.Notification;
import com.lasu.hyperduty.mapper.NotificationMapper;
import com.lasu.hyperduty.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void sendNotification(Long receiverId, String receiverName, String title, String content, 
                             String type, Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setReceiverName(receiverName);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setStatus("unread");
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setCreateTime(LocalDateTime.now());
        save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long receiverId) {
        return lambdaQuery()
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getStatus, "unread")
                .orderByDesc(Notification::getCreateTime)
                .list();
    }

    @Override
    public List<Notification> getAllNotifications(Long receiverId) {
        return lambdaQuery()
                .eq(Notification::getReceiverId, receiverId)
                .orderByDesc(Notification::getCreateTime)
                .list();
    }

    @Override
    public boolean markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null) {
            notification.setStatus("read");
            notification.setReadTime(LocalDateTime.now());
            return updateById(notification);
        }
        return false;
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        lambdaUpdate()
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getStatus, "unread")
                .set(Notification::getStatus, "read")
                .set(Notification::getReadTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void deleteNotification(Long notificationId) {
        removeById(notificationId);
    }
}
