package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BorrowReminder;
import com.library.service.BorrowReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reminder")
public class BorrowReminderController {

    @Autowired
    private BorrowReminderService borrowReminderService;

    @GetMapping("/page")
    public Result<PageResult<BorrowReminder>> getReminderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reminderType) {
        
        PageResult<BorrowReminder> result = borrowReminderService.getReminderPage(page, size, status, reminderType);
        return Result.success(result);
    }

    @GetMapping("/pending")
    public Result<List<BorrowReminder>> getPendingReminders() {
        List<BorrowReminder> reminders = borrowReminderService.getPendingReminders();
        return Result.success(reminders);
    }

    @GetMapping("/order/{orderId}")
    public Result<List<BorrowReminder>> getRemindersByOrderId(@PathVariable Long orderId) {
        List<BorrowReminder> reminders = borrowReminderService.getRemindersByOrderId(orderId);
        return Result.success(reminders);
    }

    @PutMapping("/{id}/complete")
    public Result<Void> completeReminder(@PathVariable Long id) {
        boolean success = borrowReminderService.completeReminder(id);
        if (success) {
            return Result.success("提醒已完成");
        }
        return Result.error("操作失败");
    }

    @PutMapping("/cancel/{orderId}")
    public Result<Void> cancelReminder(@PathVariable Long orderId) {
        boolean success = borrowReminderService.cancelReminder(orderId);
        if (success) {
            return Result.success("提醒已取消");
        }
        return Result.error("操作失败");
    }
}
