package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.PlatformNotification;
import com.library.service.PlatformNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notification")
public class PlatformNotificationController {

    @Autowired
    private PlatformNotificationService platformNotificationService;

    @GetMapping("/page")
    public Result<PageResult<PlatformNotification>> getNotificationPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) Integer notificationType) {
        
        PageResult<PlatformNotification> result = platformNotificationService.getNotificationPage(page, size, isRead, notificationType);
        return Result.success(result);
    }

    @GetMapping("/unread")
    public Result<List<PlatformNotification>> getUnreadNotifications() {
        List<PlatformNotification> notifications = platformNotificationService.getUnreadNotifications();
        return Result.success(notifications);
    }

    @GetMapping("/unreadCount")
    public Result<Integer> getUnreadCount() {
        int count = platformNotificationService.getUnreadCount();
        return Result.success(count);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getNotificationStats() {
        Map<String, Object> stats = platformNotificationService.getNotificationStats();
        return Result.success(stats);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, @RequestParam(required = false) String readBy) {
        boolean success = platformNotificationService.markAsRead(id, readBy != null ? readBy : "admin");
        if (success) {
            return Result.success("标记已读成功");
        }
        return Result.error("标记已读失败");
    }

    @PutMapping("/readAll")
    public Result<Void> markAllAsRead(@RequestParam(required = false) String readBy) {
        boolean success = platformNotificationService.markAllAsRead(readBy != null ? readBy : "admin");
        if (success) {
            return Result.success("全部标记已读成功");
        }
        return Result.error("全部标记已读失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        boolean success = platformNotificationService.deleteNotification(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
