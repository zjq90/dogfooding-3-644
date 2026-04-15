package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.PlatformNotification;
import com.library.service.PlatformNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private PlatformNotificationService notificationService;

    @GetMapping("/page")
    public Result<PageResult<PlatformNotification>> getNotificationPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        
        PageResult<PlatformNotification> result = notificationService.getNotificationPage(page, size, status, type);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<PlatformNotification> getNotificationById(@PathVariable Long id) {
        PlatformNotification notification = notificationService.getByIdWithInfo(id);
        if (notification != null) {
            return Result.success(notification);
        }
        return Result.error("通知不存在");
    }

    @GetMapping("/unread")
    public Result<List<PlatformNotification>> getUnreadNotifications() {
        List<PlatformNotification> list = notificationService.getUnreadNotifications();
        return Result.success(list);
    }

    @GetMapping("/unread/return-reminders")
    public Result<List<PlatformNotification>> getUnreadReturnReminders() {
        List<PlatformNotification> list = notificationService.getUnreadReturnReminders();
        return Result.success(list);
    }

    @GetMapping("/count/unread")
    public Result<Map<String, Integer>> countUnread() {
        Map<String, Integer> result = new HashMap<>();
        result.put("totalUnread", notificationService.countUnread());
        result.put("returnReminderUnread", notificationService.countUnreadReturnReminders());
        return Result.success(result);
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        log.info("标记通知已读: id={}", id);
        
        boolean success = notificationService.markAsRead(id);
        if (success) {
            return Result.success("标记成功");
        }
        return Result.error("标记失败");
    }

    @PutMapping("/read/all")
    public Result<Void> markAllAsRead() {
        log.info("标记所有通知已读");
        
        boolean success = notificationService.markAllAsRead();
        if (success) {
            return Result.success("全部标记已读成功");
        }
        return Result.error("标记失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        log.info("删除通知: {}", id);
        
        boolean success = notificationService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
