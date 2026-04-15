package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.PlatformNotification;

import java.util.List;

public interface PlatformNotificationService extends IService<PlatformNotification> {
    
    PageResult<PlatformNotification> getNotificationPage(Integer page, Integer size, Integer status, Integer type);
    
    PlatformNotification getByIdWithInfo(Long id);
    
    List<PlatformNotification> getUnreadNotifications();
    
    List<PlatformNotification> getUnreadReturnReminders();
    
    boolean createNotification(String title, String content, Integer type, Long relatedOrderId);
    
    boolean markAsRead(Long id);
    
    boolean markAllAsRead();
    
    int countUnread();
    
    int countUnreadReturnReminders();
}
