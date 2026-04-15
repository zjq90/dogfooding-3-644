package com.library.job;

import com.library.entity.BookInfo;
import com.library.entity.BorrowOrder;
import com.library.entity.Borrower;
import com.library.entity.ReturnReminder;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BorrowOrderMapper;
import com.library.mapper.BorrowerMapper;
import com.library.service.DepositDetailService;
import com.library.service.ReturnReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
public class ReturnReminderJob {

    @Autowired
    private BorrowOrderMapper borrowOrderMapper;
    
    @Autowired
    private BorrowerMapper borrowerMapper;
    
    @Autowired
    private BookInfoMapper bookInfoMapper;
    
    @Autowired
    private ReturnReminderService returnReminderService;
    
    @Autowired
    private DepositDetailService depositDetailService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyOverdueReminder() {
        log.info("开始执行每日逾期提醒任务...");
        
        try {
            List<BorrowOrder> overdueOrders = borrowOrderMapper.selectOverdueOrders(LocalDate.now());
            log.info("发现 {} 个逾期订单", overdueOrders.size());
            
            for (BorrowOrder order : overdueOrders) {
                try {
                    processOverdueOrder(order);
                } catch (Exception e) {
                    log.error("处理逾期订单失败: orderId={}, error={}", order.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("每日逾期提醒任务执行失败: {}", e.getMessage());
        }
        
        log.info("每日逾期提醒任务执行完成");
    }
    
    private void processOverdueOrder(BorrowOrder order) {
        ReturnReminder latestReminder = returnReminderService.getLatestByOrderId(order.getId());
        int reminderCount = 0;
        
        if (latestReminder != null) {
            reminderCount = latestReminder.getReminderCount();
        }
        
        if (reminderCount >= 3) {
            log.info("订单 {} 已完成3次提醒，执行押金扣除", order.getOrderNo());
            deductDeposit(order);
        } else {
            log.info("为订单 {} 创建第 {} 次提醒", order.getOrderNo(), reminderCount + 1);
            returnReminderService.createReminder(order.getId());
        }
    }
    
    @Transactional
    public void deductDeposit(BorrowOrder order) {
        try {
            Borrower borrower = borrowerMapper.selectById(order.getBorrowerId());
            BookInfo book = bookInfoMapper.selectById(order.getBookId());
            
            if (borrower == null || book == null) {
                log.error("扣除押金失败：借阅人或图书不存在");
                return;
            }
            
            BigDecimal depositAmount = order.getDepositAmount();
            if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("订单 {} 没有押金可扣除", order.getOrderNo());
                return;
            }
            
            order.setDepositStatus(2);
            order.setStatus(2);
            borrowOrderMapper.updateById(order);
            
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            bookInfoMapper.updateById(book);
            
            String operator = "system";
            String remark = "逾期3天未归还，系统自动扣除押金，图书：" + book.getTitle();
            depositDetailService.deductDeposit(borrower.getId(), depositAmount, order.getOrderNo(), operator, remark);
            
            ReturnReminder latestReminder = returnReminderService.getLatestByOrderId(order.getId());
            if (latestReminder != null) {
                latestReminder.setDepositDeducted(1);
                latestReminder.setStatus(1);
                returnReminderService.updateById(latestReminder);
            }
            
            log.info("订单 {} 押金扣除成功，金额：{}", order.getOrderNo(), depositAmount);
            
        } catch (Exception e) {
            log.error("扣除押金失败: {}", e.getMessage());
            throw e;
        }
    }
    
    @Scheduled(cron = "0 0 9 * * ?")
    public void dueDateReminder() {
        log.info("开始执行到期提醒任务...");
        
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<BorrowOrder> dueOrders = borrowOrderMapper.selectOrdersByDueDate(tomorrow);
            log.info("发现 {} 个明天到期的订单", dueOrders.size());
            
            for (BorrowOrder order : dueOrders) {
                try {
                    int activeReminders = returnReminderService.countActiveByOrderId(order.getId());
                    if (activeReminders == 0) {
                        returnReminderService.createReminder(order.getId());
                    }
                } catch (Exception e) {
                    log.error("处理到期订单失败: orderId={}, error={}", order.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("到期提醒任务执行失败: {}", e.getMessage());
        }
        
        log.info("到期提醒任务执行完成");
    }
}
