package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BorrowOrder;
import com.library.service.BorrowOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/borrowOrder")
public class BorrowOrderController {

    @Autowired
    private BorrowOrderService borrowOrderService;

    @GetMapping("/page")
    public Result<PageResult<BorrowOrder>> getBorrowOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer depositStatus,
            @RequestParam(required = false) Integer paymentStatus) {
        
        PageResult<BorrowOrder> result = borrowOrderService.getBorrowOrderPage(page, size, keyword, status, depositStatus, paymentStatus);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<BorrowOrder> getBorrowOrderById(@PathVariable Long id) {
        BorrowOrder order = borrowOrderService.getById(id);
        if (order != null) {
            return Result.success(order);
        }
        return Result.error("订单不存在");
    }

    @PostMapping
    public Result<Void> createBorrowOrder(@RequestBody BorrowOrder borrowOrder) {
        log.info("创建借阅订单: 借阅人员ID: {}, 图书ID: {}", borrowOrder.getBorrowerId(), borrowOrder.getBookId());
        
        try {
            boolean success = borrowOrderService.createBorrowOrder(borrowOrder);
            if (success) {
                log.info("借阅订单创建成功: {}", borrowOrder.getOrderNo());
                return Result.success("订单创建成功");
            }
            return Result.error("订单创建失败");
        } catch (Exception e) {
            log.error("借阅订单创建失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id) {
        log.info("归还图书: 订单ID: {}", id);
        
        try {
            boolean success = borrowOrderService.returnBook(id);
            if (success) {
                log.info("图书归还成功: 订单ID: {}", id);
                return Result.success("图书归还成功");
            }
            return Result.error("图书归还失败");
        } catch (Exception e) {
            log.error("图书归还失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/refundDeposit")
    public Result<Void> refundDeposit(@PathVariable Long id) {
        log.info("退还订单押金: 订单ID: {}", id);
        
        try {
            boolean success = borrowOrderService.refundDeposit(id);
            if (success) {
                log.info("订单押金退还成功: 订单ID: {}", id);
                return Result.success("押金退还成功");
            }
            return Result.error("押金退还失败");
        } catch (Exception e) {
            log.error("订单押金退还失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBorrowOrder(@PathVariable Long id) {
        log.info("删除借阅订单: {}", id);
        
        boolean success = borrowOrderService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
