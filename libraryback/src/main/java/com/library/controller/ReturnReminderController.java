package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.ReturnReminder;
import com.library.service.ReturnReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reminder")
public class ReturnReminderController {

    @Autowired
    private ReturnReminderService returnReminderService;

    @GetMapping("/page")
    public Result<PageResult<ReturnReminder>> getReminderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long orderId) {
        
        PageResult<ReturnReminder> result = returnReminderService.getReminderPage(page, size, status, orderId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ReturnReminder> getReminderById(@PathVariable Long id) {
        ReturnReminder reminder = returnReminderService.getByIdWithInfo(id);
        if (reminder != null) {
            return Result.success(reminder);
        }
        return Result.error("提醒记录不存在");
    }

    @GetMapping("/order/{orderId}")
    public Result<ReturnReminder> getLatestByOrderId(@PathVariable Long orderId) {
        ReturnReminder reminder = returnReminderService.getLatestByOrderId(orderId);
        if (reminder != null) {
            return Result.success(reminder);
        }
        return Result.error("暂无提醒记录");
    }

    @GetMapping("/today")
    public Result<List<ReturnReminder>> getTodayReminders() {
        List<ReturnReminder> list = returnReminderService.getTodayReminders();
        return Result.success(list);
    }

    @GetMapping("/active")
    public Result<List<ReturnReminder>> getActiveReminders() {
        List<ReturnReminder> list = returnReminderService.getActiveReminders();
        return Result.success(list);
    }

    @PostMapping("/create/{orderId}")
    public Result<Void> createReminder(@PathVariable Long orderId) {
        log.info("创建归还提醒: orderId={}", orderId);
        
        try {
            boolean success = returnReminderService.createReminder(orderId);
            if (success) {
                log.info("归还提醒创建成功: orderId={}", orderId);
                return Result.success("提醒创建成功");
            }
            return Result.error("提醒创建失败");
        } catch (Exception e) {
            log.error("归还提醒创建失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/process/{id}")
    public Result<Void> processReminder(@PathVariable Long id) {
        log.info("处理提醒记录: id={}", id);
        
        try {
            boolean success = returnReminderService.processReminder(id);
            if (success) {
                log.info("提醒记录处理成功: id={}", id);
                return Result.success("处理成功");
            }
            return Result.error("处理失败");
        } catch (Exception e) {
            log.error("提醒记录处理失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/cancel/{orderId}")
    public Result<Void> cancelReminder(@PathVariable Long orderId) {
        log.info("取消提醒: orderId={}", orderId);
        
        try {
            boolean success = returnReminderService.cancelReminder(orderId);
            if (success) {
                log.info("提醒取消成功: orderId={}", orderId);
                return Result.success("取消成功");
            }
            return Result.error("取消失败");
        } catch (Exception e) {
            log.error("提醒取消失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        log.info("删除提醒记录: {}", id);
        
        boolean success = returnReminderService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
