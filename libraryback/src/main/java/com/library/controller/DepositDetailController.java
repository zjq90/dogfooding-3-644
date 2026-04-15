package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.DepositDetail;
import com.library.service.DepositDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/deposit")
public class DepositDetailController {

    @Autowired
    private DepositDetailService depositDetailService;

    @GetMapping("/page")
    public Result<PageResult<DepositDetail>> getDepositDetailPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long borrowerId) {
        
        PageResult<DepositDetail> result = depositDetailService.getDepositDetailPage(page, size, keyword, type, borrowerId);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addDeposit(@RequestBody Map<String, Object> params) {
        Long borrowerId = Long.valueOf(params.get("borrowerId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        Integer paymentMethod = Integer.valueOf(params.get("paymentMethod").toString());
        String operator = params.get("operator") != null ? params.get("operator").toString() : "admin";
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        
        log.info("缴纳押金: 借阅人员ID: {}, 金额: {}", borrowerId, amount);
        
        try {
            boolean success = depositDetailService.addDeposit(borrowerId, amount, paymentMethod, operator, remark);
            if (success) {
                log.info("押金缴纳成功: 借阅人员ID: {}, 金额: {}", borrowerId, amount);
                return Result.success("押金缴纳成功");
            }
            return Result.error("押金缴纳失败");
        } catch (Exception e) {
            log.error("押金缴纳失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/refund")
    public Result<Void> refundDeposit(@RequestBody Map<String, Object> params) {
        Long borrowerId = Long.valueOf(params.get("borrowerId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        Integer paymentMethod = Integer.valueOf(params.get("paymentMethod").toString());
        String operator = params.get("operator") != null ? params.get("operator").toString() : "admin";
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        
        log.info("退还押金: 借阅人员ID: {}, 金额: {}", borrowerId, amount);
        
        try {
            boolean success = depositDetailService.refundDeposit(borrowerId, amount, paymentMethod, operator, remark);
            if (success) {
                log.info("押金退还成功: 借阅人员ID: {}, 金额: {}", borrowerId, amount);
                return Result.success("押金退还成功");
            }
            return Result.error("押金退还失败");
        } catch (Exception e) {
            log.error("押金退还失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/deduct")
    public Result<Void> deductDeposit(@RequestBody Map<String, Object> params) {
        Long borrowerId = Long.valueOf(params.get("borrowerId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : "";
        String operator = params.get("operator") != null ? params.get("operator").toString() : "admin";
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        
        log.info("扣除押金: 借阅人员ID: {}, 金额: {}", borrowerId, amount);
        
        try {
            boolean success = depositDetailService.deductDeposit(borrowerId, amount, orderNo, operator, remark);
            if (success) {
                log.info("押金扣除成功: 借阅人员ID: {}, 金额: {}", borrowerId, amount);
                return Result.success("押金扣除成功");
            }
            return Result.error("押金扣除失败");
        } catch (Exception e) {
            log.error("押金扣除失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
