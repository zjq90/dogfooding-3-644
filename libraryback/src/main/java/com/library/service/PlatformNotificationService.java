package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.PlatformNotification;
import java.util.List;
import java.util.Map;

public interface PlatformNotificationService extends IService<PlatformNotification> {
    
    PageResult<PlatformNotification> getNotificationPage(Integer page, Integer size, Integer isRead, Integer notificationType);
    
    List<PlatformNotification> getUnreadNotifications();
    
    int getUnreadCount();
    
    boolean createNotification(PlatformNotification notification);
    
    boolean markAsRead(Long id, String readBy);
    
    boolean markAllAsRead(String readBy);
    
    boolean deleteNotification(Long id);
    
    Map<String, Object> getNotificationStats();
}
