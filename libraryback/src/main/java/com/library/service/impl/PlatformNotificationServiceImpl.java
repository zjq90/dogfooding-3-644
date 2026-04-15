package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.PlatformNotification;
import com.library.mapper.BorrowOrderMapper;
import com.library.mapper.BorrowerMapper;
import com.library.mapper.PlatformNotificationMapper;
import com.library.service.PlatformNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PlatformNotificationServiceImpl extends ServiceImpl<PlatformNotificationMapper, PlatformNotification> implements PlatformNotificationService {

    @Autowired
    private BorrowOrderMapper borrowOrderMapper;
    
    @Autowired
    private BorrowerMapper borrowerMapper;

    @Override
    public PageResult<PlatformNotification> getNotificationPage(Integer page, Integer size, Integer status, Integer type) {
        Page<PlatformNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PlatformNotification> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(PlatformNotification::getStatus, status);
        }
        
        if (type != null) {
            wrapper.eq(PlatformNotification::getType, type);
        }
        
        wrapper.orderByDesc(PlatformNotification::getCreateTime);
        Page<PlatformNotification> notificationPage = this.page(pageParam, wrapper);
        
        List<PlatformNotification> records = notificationPage.getRecords();
        fillRelatedInfo(records);
        
        return new PageResult<>(notificationPage.getTotal(), records, 
                                notificationPage.getCurrent(), notificationPage.getSize());
    }

    @Override
    public PlatformNotification getByIdWithInfo(Long id) {
        PlatformNotification notification = baseMapper.selectByIdWithInfo(id);
        if (notification != null) {
            markAsRead(id);
        }
        return notification;
    }

    @Override
    public List<PlatformNotification> getUnreadNotifications() {
        List<PlatformNotification> list = baseMapper.selectListByStatus(0);
        fillRelatedInfo(list);
        return list;
    }

    @Override
    public List<PlatformNotification> getUnreadReturnReminders() {
        List<PlatformNotification> list = baseMapper.selectByTypeAndStatus(1, 0);
        fillRelatedInfo(list);
        return list;
    }

    @Override
    @Transactional
    public boolean createNotification(String title, String content, Integer type, Long relatedOrderId) {
        PlatformNotification notification = new PlatformNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedOrderId(relatedOrderId);
        notification.setStatus(0);
        
        return this.save(notification);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long id) {
        return baseMapper.markAsRead(id) > 0;
    }

    @Override
    @Transactional
    public boolean markAllAsRead() {
        return baseMapper.markAllAsRead() > 0;
    }

    @Override
    public int countUnread() {
        return baseMapper.countUnread();
    }

    @Override
    public int countUnreadReturnReminders() {
        LambdaQueryWrapper<PlatformNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformNotification::getType, 1)
               .eq(PlatformNotification::getStatus, 0)
               .eq(PlatformNotification::getDeleted, 0);
        return (int) this.count(wrapper);
    }
    
    private void fillRelatedInfo(List<PlatformNotification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        
        for (PlatformNotification notification : notifications) {
            if (notification.getRelatedOrderId() != null) {
                com.library.entity.BorrowOrder order = borrowOrderMapper.selectById(notification.getRelatedOrderId());
                if (order != null) {
                    notification.setOrderNo(order.getOrderNo());
                    
                    com.library.entity.Borrower borrower = borrowerMapper.selectById(order.getBorrowerId());
                    if (borrower != null) {
                        notification.setBorrowerName(borrower.getName());
                    }
                }
            }
        }
    }
}
