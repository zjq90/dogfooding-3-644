package com.library.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.*;
import com.library.mapper.BorrowOrderMapper;
import com.library.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
public class BorrowReminderTask {

    @Autowired
    private BorrowOrderMapper borrowOrderMapper;
    
    @Autowired
    private BorrowerService borrowerService;
    
    @Autowired
    private BookInfoService bookInfoService;
    
    @Autowired
    private BorrowReminderService borrowReminderService;
    
    @Autowired
    private PlatformNotificationService platformNotificationService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private DepositDetailService depositDetailService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void processOverdueReminders() {
        log.info("========== 开始执行逾期提醒任务 ==========");
        
        List<BorrowOrder> overdueOrders = getOverdueOrders();
        log.info("发现逾期订单数量: {}", overdueOrders.size());
        
        for (BorrowOrder order : overdueOrders) {
            try {
                processOverdueOrder(order);
            } catch (Exception e) {
                log.error("处理逾期订单异常: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }
        
        log.info("========== 逾期提醒任务执行完成 ==========");
    }

    @Scheduled(cron = "0 30 9 * * ?")
    public void processDepositDeduction() {
        log.info("========== 开始执行押金扣除任务 ==========");
        
        List<BorrowReminder> reminders = getRemindersForDeduction();
        log.info("需要扣除押金的提醒数量: {}", reminders.size());
        
        for (BorrowReminder reminder : reminders) {
            try {
                processDepositDeduction(reminder);
            } catch (Exception e) {
                log.error("押金扣除异常: reminderId={}, error={}", reminder.getId(), e.getMessage());
            }
        }
        
        log.info("========== 押金扣除任务执行完成 ==========");
    }

    @Scheduled(fixedRate = 300000)
    public void checkNewOverdueOrders() {
        LambdaQueryWrapper<BorrowOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowOrder::getStatus, 0);
        wrapper.lt(BorrowOrder::getDueDate, LocalDate.now());
        
        List<BorrowOrder> newOverdueOrders = borrowOrderMapper.selectList(wrapper);
        
        for (BorrowOrder order : newOverdueOrders) {
            if (!hasReminderForToday(order.getId())) {
                createInitialReminders(order);
            }
        }
    }
    
    private List<BorrowOrder> getOverdueOrders() {
        LambdaQueryWrapper<BorrowOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowOrder::getStatus, 0);
        wrapper.lt(BorrowOrder::getDueDate, LocalDate.now());
        return borrowOrderMapper.selectList(wrapper);
    }
    
    private void processOverdueOrder(BorrowOrder order) {
        Borrower borrower = borrowerService.getById(order.getBorrowerId());
        BookInfo book = bookInfoService.getById(order.getBookId());
        
        if (borrower == null || book == null) {
            log.warn("借阅人或图书不存在: orderId={}", order.getId());
            return;
        }
        
        long overdueDays = ChronoUnit.DAYS.between(order.getDueDate(), LocalDate.now());
        int reminderDay = (int) Math.min(overdueDays, 3);
        
        BorrowReminder existingReminder = getReminderForDay(order.getId(), reminderDay);
        
        if (existingReminder != null && existingReminder.getSmsSent() == 1) {
            log.info("今日已发送提醒: orderId={}, day={}", order.getId(), reminderDay);
            return;
        }
        
        BorrowReminder reminder;
        if (existingReminder == null) {
            reminder = createReminder(order, borrower, book, reminderDay);
        } else {
            reminder = existingReminder;
        }
        
        boolean smsSent = smsService.sendReminderSms(
            borrower.getPhone(), 
            borrower.getName(), 
            book.getTitle(), 
            order.getOrderNo()
        );
        
        if (smsSent) {
            borrowReminderService.updateSmsSent(reminder.getId());
            log.info("短信发送成功: orderId={}, phone={}", order.getId(), borrower.getPhone());
        }
        
        createPlatformNotification(order, borrower, book, reminderDay);
        borrowReminderService.updatePlatformNotified(reminder.getId());
        
        order.setStatus(2);
        order.setOverdueDays((int) overdueDays);
        borrowOrderMapper.updateById(order);
    }
    
    private BorrowReminder createReminder(BorrowOrder order, Borrower borrower, BookInfo book, int reminderDay) {
        BorrowReminder reminder = new BorrowReminder();
        reminder.setOrderId(order.getId());
        reminder.setBorrowerId(borrower.getId());
        reminder.setBookId(book.getId());
        reminder.setReminderType(2);
        reminder.setReminderDay(reminderDay);
        reminder.setSmsSent(0);
        reminder.setPlatformNotified(0);
        reminder.setDepositDeducted(0);
        reminder.setStatus(0);
        reminder.setRemark("逾期提醒第" + reminderDay + "天");
        
        borrowReminderService.save(reminder);
        return reminder;
    }
    
    private void createPlatformNotification(BorrowOrder order, Borrower borrower, BookInfo book, int reminderDay) {
        PlatformNotification notification = new PlatformNotification();
        notification.setTitle("借阅逾期提醒");
        notification.setContent(String.format(
            "借阅人【%s】借阅的《%s》已逾期%d天，订单号：%s，请及时处理。",
            borrower.getName(), book.getTitle(), reminderDay, order.getOrderNo()
        ));
        notification.setNotificationType(2);
        notification.setOrderId(order.getId());
        notification.setBorrowerId(borrower.getId());
        notification.setIsRead(0);
        notification.setStatus(1);
        
        platformNotificationService.save(notification);
        log.info("创建平台提醒: orderId={}", order.getId());
    }
    
    private List<BorrowReminder> getRemindersForDeduction() {
        LambdaQueryWrapper<BorrowReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowReminder::getStatus, 0);
        wrapper.eq(BorrowReminder::getReminderDay, 3);
        wrapper.eq(BorrowReminder::getSmsSent, 1);
        wrapper.eq(BorrowReminder::getDepositDeducted, 0);
        
        return borrowReminderService.list(wrapper);
    }
    
    private void processDepositDeduction(BorrowReminder reminder) {
        BorrowOrder order = borrowOrderMapper.selectById(reminder.getOrderId());
        if (order == null || order.getStatus() == 1) {
            borrowReminderService.completeReminder(reminder.getId());
            return;
        }
        
        Borrower borrower = borrowerService.getById(reminder.getBorrowerId());
        BookInfo book = bookInfoService.getById(reminder.getBookId());
        
        if (borrower == null) {
            log.warn("借阅人不存在: borrowerId={}", reminder.getBorrowerId());
            return;
        }
        
        BigDecimal deductAmount = calculateDeductAmount(order);
        
        if (borrower.getBalance().compareTo(deductAmount) >= 0) {
            depositDetailService.deductDeposit(
                borrower.getId(), 
                deductAmount, 
                order.getOrderNo(), 
                "system", 
                "逾期未还书自动扣除押金"
            );
            
            borrowReminderService.updateDepositDeducted(reminder.getId(), deductAmount);
            
            smsService.sendDepositDeductSms(
                borrower.getPhone(), 
                borrower.getName(), 
                book != null ? book.getTitle() : "未知图书", 
                deductAmount
            );
            
            PlatformNotification notification = new PlatformNotification();
            notification.setTitle("押金扣除通知");
            notification.setContent(String.format(
                "借阅人【%s】因逾期未还书，系统已自动扣除押金%.2f元。订单号：%s",
                borrower.getName(), deductAmount, order.getOrderNo()
            ));
            notification.setNotificationType(3);
            notification.setOrderId(order.getId());
            notification.setBorrowerId(borrower.getId());
            notification.setIsRead(0);
            notification.setStatus(1);
            platformNotificationService.save(notification);
            
            borrowReminderService.completeReminder(reminder.getId());
            
            log.info("押金扣除成功: orderId={}, amount={}", order.getId(), deductAmount);
        } else {
            log.warn("押金余额不足，无法扣除: borrowerId={}, balance={}", 
                    borrower.getId(), borrower.getBalance());
        }
    }
    
    private BigDecimal calculateDeductAmount(BorrowOrder order) {
        BigDecimal depositAmount = order.getDepositAmount();
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("50.00");
        }
        return depositAmount;
    }
    
    private boolean hasReminderForToday(Long orderId) {
        long overdueDays = 0;
        BorrowOrder order = borrowOrderMapper.selectById(orderId);
        if (order != null && order.getDueDate() != null) {
            overdueDays = ChronoUnit.DAYS.between(order.getDueDate(), LocalDate.now());
        }
        int reminderDay = (int) Math.min(overdueDays, 3);
        
        return getReminderForDay(orderId, reminderDay) != null;
    }
    
    private BorrowReminder getReminderForDay(Long orderId, int reminderDay) {
        LambdaQueryWrapper<BorrowReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowReminder::getOrderId, orderId);
        wrapper.eq(BorrowReminder::getReminderDay, reminderDay);
        wrapper.eq(BorrowReminder::getStatus, 0);
        wrapper.last("LIMIT 1");
        
        return borrowReminderService.getOne(wrapper);
    }
    
    private void createInitialReminders(BorrowOrder order) {
        Borrower borrower = borrowerService.getById(order.getBorrowerId());
        BookInfo book = bookInfoService.getById(order.getBookId());
        
        if (borrower != null && book != null) {
            long overdueDays = ChronoUnit.DAYS.between(order.getDueDate(), LocalDate.now());
            int reminderDay = (int) Math.min(overdueDays, 3);
            
            createReminder(order, borrower, book, reminderDay);
            log.info("创建初始提醒记录: orderId={}, day={}", order.getId(), reminderDay);
        }
    }
}
