package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BorrowReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BorrowReminderServiceImpl extends ServiceImpl<BorrowReminderMapper, BorrowReminder> implements BorrowReminderService {

    @Autowired
    private BorrowerMapper borrowerMapper;
    
    @Autowired
    private BookInfoMapper bookInfoMapper;
    
    @Autowired
    private BorrowOrderMapper borrowOrderMapper;

    @Override
    public PageResult<BorrowReminder> getReminderPage(Integer page, Integer size, Integer status, Integer reminderType) {
        Page<BorrowReminder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowReminder> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(BorrowReminder::getStatus, status);
        }
        
        if (reminderType != null) {
            wrapper.eq(BorrowReminder::getReminderType, reminderType);
        }
        
        wrapper.orderByDesc(BorrowReminder::getCreateTime);
        Page<BorrowReminder> reminderPage = this.page(pageParam, wrapper);
        
        List<BorrowReminder> records = reminderPage.getRecords();
        fillRelatedInfo(records);
        
        return new PageResult<>(reminderPage.getTotal(), records, 
                                reminderPage.getCurrent(), reminderPage.getSize());
    }

    @Override
    public List<BorrowReminder> getPendingReminders() {
        LambdaQueryWrapper<BorrowReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowReminder::getStatus, 0);
        wrapper.orderByAsc(BorrowReminder::getCreateTime);
        List<BorrowReminder> reminders = this.list(wrapper);
        fillRelatedInfo(reminders);
        return reminders;
    }

    @Override
    public List<BorrowReminder> getRemindersByOrderId(Long orderId) {
        LambdaQueryWrapper<BorrowReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowReminder::getOrderId, orderId);
        wrapper.orderByAsc(BorrowReminder::getReminderDay);
        return this.list(wrapper);
    }

    @Override
    public boolean createReminder(BorrowReminder reminder) {
        return this.save(reminder);
    }

    @Override
    public boolean updateSmsSent(Long id) {
        BorrowReminder reminder = this.getById(id);
        if (reminder != null) {
            reminder.setSmsSent(1);
            reminder.setSmsSendTime(java.time.LocalDateTime.now());
            return this.updateById(reminder);
        }
        return false;
    }

    @Override
    public boolean updatePlatformNotified(Long id) {
        BorrowReminder reminder = this.getById(id);
        if (reminder != null) {
            reminder.setPlatformNotified(1);
            return this.updateById(reminder);
        }
        return false;
    }

    @Override
    public boolean updateDepositDeducted(Long id, java.math.BigDecimal deductAmount) {
        BorrowReminder reminder = this.getById(id);
        if (reminder != null) {
            reminder.setDepositDeducted(1);
            reminder.setDepositDeductTime(java.time.LocalDateTime.now());
            reminder.setDeductAmount(deductAmount);
            return this.updateById(reminder);
        }
        return false;
    }

    @Override
    public boolean completeReminder(Long id) {
        BorrowReminder reminder = this.getById(id);
        if (reminder != null) {
            reminder.setStatus(1);
            return this.updateById(reminder);
        }
        return false;
    }

    @Override
    public boolean cancelReminder(Long orderId) {
        LambdaQueryWrapper<BorrowReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowReminder::getOrderId, orderId);
        wrapper.eq(BorrowReminder::getStatus, 0);
        
        List<BorrowReminder> reminders = this.list(wrapper);
        for (BorrowReminder reminder : reminders) {
            reminder.setStatus(2);
            this.updateById(reminder);
        }
        return true;
    }
    
    private void fillRelatedInfo(List<BorrowReminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return;
        }
        
        List<Long> borrowerIds = reminders.stream()
                .map(BorrowReminder::getBorrowerId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> bookIds = reminders.stream()
                .map(BorrowReminder::getBookId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> orderIds = reminders.stream()
                .map(BorrowReminder::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Borrower> borrowers = borrowerMapper.selectBatchIds(borrowerIds);
        Map<Long, Borrower> borrowerMap = borrowers.stream()
                .collect(Collectors.toMap(Borrower::getId, b -> b));
        
        List<BookInfo> books = bookInfoMapper.selectBatchIds(bookIds);
        Map<Long, BookInfo> bookMap = books.stream()
                .collect(Collectors.toMap(BookInfo::getId, b -> b));
        
        List<BorrowOrder> orders = borrowOrderMapper.selectBatchIds(orderIds);
        Map<Long, BorrowOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(BorrowOrder::getId, b -> b));
        
        for (BorrowReminder reminder : reminders) {
            Borrower borrower = borrowerMap.get(reminder.getBorrowerId());
            if (borrower != null) {
                reminder.setBorrowerName(borrower.getName());
                reminder.setBorrowerPhone(borrower.getPhone());
            }
            
            BookInfo book = bookMap.get(reminder.getBookId());
            if (book != null) {
                reminder.setBookTitle(book.getTitle());
            }
            
            BorrowOrder order = orderMap.get(reminder.getOrderId());
            if (order != null) {
                reminder.setOrderNo(order.getOrderNo());
            }
        }
    }
}
