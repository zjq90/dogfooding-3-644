package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Borrower;
import com.library.entity.BorrowOrder;
import com.library.entity.PlatformNotification;
import com.library.mapper.BorrowOrderMapper;
import com.library.mapper.BorrowerMapper;
import com.library.mapper.PlatformNotificationMapper;
import com.library.service.PlatformNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlatformNotificationServiceImpl extends ServiceImpl<PlatformNotificationMapper, PlatformNotification> implements PlatformNotificationService {

    @Autowired
    private BorrowerMapper borrowerMapper;
    
    @Autowired
    private BorrowOrderMapper borrowOrderMapper;

    @Override
    public PageResult<PlatformNotification> getNotificationPage(Integer page, Integer size, Integer isRead, Integer notificationType) {
        Page<PlatformNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PlatformNotification> wrapper = new LambdaQueryWrapper<>();
        
        if (isRead != null) {
            wrapper.eq(PlatformNotification::getIsRead, isRead);
        }
        
        if (notificationType != null) {
            wrapper.eq(PlatformNotification::getNotificationType, notificationType);
        }
        
        wrapper.orderByDesc(PlatformNotification::getCreateTime);
        Page<PlatformNotification> notificationPage = this.page(pageParam, wrapper);
        
        List<PlatformNotification> records = notificationPage.getRecords();
        fillRelatedInfo(records);
        
        return new PageResult<>(notificationPage.getTotal(), records, 
                                notificationPage.getCurrent(), notificationPage.getSize());
    }

    @Override
    public List<PlatformNotification> getUnreadNotifications() {
        LambdaQueryWrapper<PlatformNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformNotification::getIsRead, 0);
        wrapper.eq(PlatformNotification::getStatus, 1);
        wrapper.orderByDesc(PlatformNotification::getCreateTime);
        List<PlatformNotification> notifications = this.list(wrapper);
        fillRelatedInfo(notifications);
        return notifications;
    }

    @Override
    public int getUnreadCount() {
        LambdaQueryWrapper<PlatformNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformNotification::getIsRead, 0);
        wrapper.eq(PlatformNotification::getStatus, 1);
        return (int) this.count(wrapper);
    }

    @Override
    public boolean createNotification(PlatformNotification notification) {
        return this.save(notification);
    }

    @Override
    public boolean markAsRead(Long id, String readBy) {
        PlatformNotification notification = this.getById(id);
        if (notification != null) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notification.setReadBy(readBy);
            return this.updateById(notification);
        }
        return false;
    }

    @Override
    public boolean markAllAsRead(String readBy) {
        LambdaQueryWrapper<PlatformNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformNotification::getIsRead, 0);
        
        List<PlatformNotification> notifications = this.list(wrapper);
        LocalDateTime now = LocalDateTime.now();
        
        for (PlatformNotification notification : notifications) {
            notification.setIsRead(1);
            notification.setReadTime(now);
            notification.setReadBy(readBy);
            this.updateById(notification);
        }
        return true;
    }

    @Override
    public boolean deleteNotification(Long id) {
        return this.removeById(id);
    }

    @Override
    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<PlatformNotification> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(PlatformNotification::getStatus, 1);
        stats.put("total", this.count(totalWrapper));
        
        LambdaQueryWrapper<PlatformNotification> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(PlatformNotification::getIsRead, 0);
        unreadWrapper.eq(PlatformNotification::getStatus, 1);
        stats.put("unread", this.count(unreadWrapper));
        
        LambdaQueryWrapper<PlatformNotification> reminderWrapper = new LambdaQueryWrapper<>();
        reminderWrapper.eq(PlatformNotification::getNotificationType, 2);
        reminderWrapper.eq(PlatformNotification::getIsRead, 0);
        reminderWrapper.eq(PlatformNotification::getStatus, 1);
        stats.put("reminderCount", this.count(reminderWrapper));
        
        LambdaQueryWrapper<PlatformNotification> deductWrapper = new LambdaQueryWrapper<>();
        deductWrapper.eq(PlatformNotification::getNotificationType, 3);
        deductWrapper.eq(PlatformNotification::getIsRead, 0);
        deductWrapper.eq(PlatformNotification::getStatus, 1);
        stats.put("deductCount", this.count(deductWrapper));
        
        return stats;
    }
    
    private void fillRelatedInfo(List<PlatformNotification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        
        List<Long> borrowerIds = notifications.stream()
                .filter(n -> n.getBorrowerId() != null)
                .map(PlatformNotification::getBorrowerId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> orderIds = notifications.stream()
                .filter(n -> n.getOrderId() != null)
                .map(PlatformNotification::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        
        if (!borrowerIds.isEmpty()) {
            List<Borrower> borrowers = borrowerMapper.selectBatchIds(borrowerIds);
            Map<Long, Borrower> borrowerMap = borrowers.stream()
                    .collect(Collectors.toMap(Borrower::getId, b -> b));
            
            for (PlatformNotification notification : notifications) {
                if (notification.getBorrowerId() != null) {
                    Borrower borrower = borrowerMap.get(notification.getBorrowerId());
                    if (borrower != null) {
                        notification.setBorrowerName(borrower.getName());
                    }
                }
            }
        }
        
        if (!orderIds.isEmpty()) {
            List<BorrowOrder> orders = borrowOrderMapper.selectBatchIds(orderIds);
            Map<Long, BorrowOrder> orderMap = orders.stream()
                    .collect(Collectors.toMap(BorrowOrder::getId, b -> b));
            
            for (PlatformNotification notification : notifications) {
                if (notification.getOrderId() != null) {
                    BorrowOrder order = orderMap.get(notification.getOrderId());
                    if (order != null) {
                        notification.setOrderNo(order.getOrderNo());
                    }
                }
            }
        }
    }
}
