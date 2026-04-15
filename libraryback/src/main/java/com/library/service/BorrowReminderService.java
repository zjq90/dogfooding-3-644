package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.BorrowReminder;
import java.util.List;

public interface BorrowReminderService extends IService<BorrowReminder> {
    
    PageResult<BorrowReminder> getReminderPage(Integer page, Integer size, Integer status, Integer reminderType);
    
    List<BorrowReminder> getPendingReminders();
    
    List<BorrowReminder> getRemindersByOrderId(Long orderId);
    
    boolean createReminder(BorrowReminder reminder);
    
    boolean updateSmsSent(Long id);
    
    boolean updatePlatformNotified(Long id);
    
    boolean updateDepositDeducted(Long id, java.math.BigDecimal deductAmount);
    
    boolean completeReminder(Long id);
    
    boolean cancelReminder(Long orderId);
}
