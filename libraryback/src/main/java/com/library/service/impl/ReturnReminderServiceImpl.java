package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.BookInfo;
import com.library.entity.BorrowOrder;
import com.library.entity.Borrower;
import com.library.entity.ReturnReminder;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BorrowOrderMapper;
import com.library.mapper.BorrowerMapper;
import com.library.mapper.ReturnReminderMapper;
import com.library.service.PlatformNotificationService;
import com.library.service.ReturnReminderService;
import com.library.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class ReturnReminderServiceImpl extends ServiceImpl<ReturnReminderMapper, ReturnReminder> implements ReturnReminderService {

    @Autowired
    private BorrowOrderMapper borrowOrderMapper;
    
    @Autowired
    private BorrowerMapper borrowerMapper;
    
    @Autowired
    private BookInfoMapper bookInfoMapper;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private PlatformNotificationService notificationService;

    @Override
    public PageResult<ReturnReminder> getReminderPage(Integer page, Integer size, Integer status, Long orderId) {
        Page<ReturnReminder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ReturnReminder> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(ReturnReminder::getStatus, status);
        }
        
        if (orderId != null) {
            wrapper.eq(ReturnReminder::getOrderId, orderId);
        }
        
        wrapper.orderByDesc(ReturnReminder::getCreateTime);
        Page<ReturnReminder> reminderPage = this.page(pageParam, wrapper);
        
        List<ReturnReminder> records = reminderPage.getRecords();
        fillRelatedInfo(records);
        
        return new PageResult<>(reminderPage.getTotal(), records, 
                                reminderPage.getCurrent(), reminderPage.getSize());
    }

    @Override
    public ReturnReminder getByIdWithInfo(Long id) {
        return baseMapper.selectByIdWithInfo(id);
    }

    @Override
    public ReturnReminder getLatestByOrderId(Long orderId) {
        return baseMapper.selectLatestByOrderId(orderId);
    }

    @Override
    public List<ReturnReminder> getTodayReminders() {
        return baseMapper.selectTodayReminders(LocalDate.now());
    }

    @Override
    public List<ReturnReminder> getActiveReminders() {
        return baseMapper.selectListByStatus(0);
    }

    @Override
    @Transactional
    public boolean createReminder(Long orderId) {
        BorrowOrder order = borrowOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() == 1) {
            throw new BusinessException("该订单已归还");
        }
        
        ReturnReminder latestReminder = getLatestByOrderId(orderId);
        int reminderCount = 0;
        
        if (latestReminder != null) {
            reminderCount = latestReminder.getReminderCount();
            if (reminderCount >= 3) {
                throw new BusinessException("该订单已完成3次提醒");
            }
        }
        
        Borrower borrower = borrowerMapper.selectById(order.getBorrowerId());
        BookInfo book = bookInfoMapper.selectById(order.getBookId());
        
        ReturnReminder reminder = new ReturnReminder();
        reminder.setOrderId(orderId);
        reminder.setBorrowerId(order.getBorrowerId());
        reminder.setBookId(order.getBookId());
        reminder.setReminderDate(LocalDate.now());
        reminder.setReminderCount(reminderCount + 1);
        reminder.setSmsSent(0);
        reminder.setPlatformNotified(0);
        reminder.setDepositDeducted(0);
        reminder.setStatus(0);
        
        boolean success = this.save(reminder);
        
        if (success) {
            long overdueDays = ChronoUnit.DAYS.between(order.getDueDate(), LocalDate.now());
            int days = (int) Math.max(0, overdueDays);
            
            sendReminderNotification(order, borrower, book, reminder, days);
        }
        
        return success;
    }
    
    private void sendReminderNotification(BorrowOrder order, Borrower borrower, BookInfo book, 
                                         ReturnReminder reminder, int overdueDays) {
        try {
            String title = "归还提醒：图书《" + book.getTitle() + "》";
            if (overdueDays > 0) {
                title += "已逾期" + overdueDays + "天";
            } else {
                title += "即将到期";
            }
            
            String content = "借阅人" + borrower.getName() + "借阅的图书《" + book.getTitle() + "》";
            if (overdueDays > 0) {
                content += "已逾期" + overdueDays + "天，请尽快联系归还。";
                if (reminder.getReminderCount() == 3) {
                    content += "今天是第3次提醒，如仍未归还将扣除押金。";
                } else {
                    content += "这是第" + reminder.getReminderCount() + "次提醒。";
                }
            } else {
                content += "即将到期，请提醒借阅人按时归还。";
            }
            content += " 订单号：" + order.getOrderNo();
            
            notificationService.createNotification(title, content, 1, order.getId());
            reminder.setPlatformNotified(1);
            
            if (borrower.getPhone() != null && !borrower.getPhone().isEmpty()) {
                boolean smsSent = smsService.sendReturnReminderSms(
                    borrower.getPhone(), 
                    borrower.getName(), 
                    book.getTitle(), 
                    overdueDays
                );
                reminder.setSmsSent(smsSent ? 1 : 0);
            }
            
            this.updateById(reminder);
            
        } catch (Exception e) {
            log.error("发送提醒通知失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean processReminder(Long reminderId) {
        ReturnReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException("提醒记录不存在");
        }
        
        reminder.setStatus(1);
        return this.updateById(reminder);
    }

    @Override
    @Transactional
    public boolean cancelReminder(Long orderId) {
        LambdaQueryWrapper<ReturnReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnReminder::getOrderId, orderId)
               .eq(ReturnReminder::getStatus, 0);
        
        ReturnReminder reminder = new ReturnReminder();
        reminder.setStatus(2);
        
        return this.update(reminder, wrapper);
    }

    @Override
    public int countActiveByOrderId(Long orderId) {
        return baseMapper.countActiveByOrderId(orderId);
    }
    
    private void fillRelatedInfo(List<ReturnReminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return;
        }
        
        for (ReturnReminder reminder : reminders) {
            BorrowOrder order = borrowOrderMapper.selectById(reminder.getOrderId());
            if (order != null) {
                reminder.setOrderNo(order.getOrderNo());
                reminder.setDueDate(order.getDueDate());
                
                Borrower borrower = borrowerMapper.selectById(reminder.getBorrowerId());
                if (borrower != null) {
                    reminder.setBorrowerName(borrower.getName());
                    reminder.setBorrowerPhone(borrower.getPhone());
                }
                
                BookInfo book = bookInfoMapper.selectById(reminder.getBookId());
                if (book != null) {
                    reminder.setBookTitle(book.getTitle());
                    reminder.setBookIsbn(book.getIsbn());
                }
            }
        }
    }
}
