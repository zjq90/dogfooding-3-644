package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.ReturnReminder;

import java.time.LocalDate;
import java.util.List;

public interface ReturnReminderService extends IService<ReturnReminder> {
    
    PageResult<ReturnReminder> getReminderPage(Integer page, Integer size, Integer status, Long orderId);
    
    ReturnReminder getByIdWithInfo(Long id);
    
    ReturnReminder getLatestByOrderId(Long orderId);
    
    List<ReturnReminder> getTodayReminders();
    
    List<ReturnReminder> getActiveReminders();
    
    boolean createReminder(Long orderId);
    
    boolean processReminder(Long reminderId);
    
    boolean cancelReminder(Long orderId);
    
    int countActiveByOrderId(Long orderId);
}
