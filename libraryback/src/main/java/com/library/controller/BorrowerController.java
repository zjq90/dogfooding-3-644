package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Borrower;
import com.library.service.BorrowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/borrower")
public class BorrowerController {

    @Autowired
    private BorrowerService borrowerService;

    @GetMapping("/page")
    public Result<PageResult<Borrower>> getBorrowerPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestAttribute Integer role) {
        
        PageResult<Borrower> result = borrowerService.getBorrowerPage(page, size, keyword, status);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<Borrower>> getAllBorrowers() {
        List<Borrower> borrowers = borrowerService.list();
        return Result.success(borrowers);
    }

    @GetMapping("/{id}")
    public Result<Borrower> getBorrowerById(@PathVariable Long id) {
        Borrower borrower = borrowerService.getById(id);
        if (borrower != null) {
            return Result.success(borrower);
        }
        return Result.error("借阅人员不存在");
    }

    @PostMapping
    public Result<Void> addBorrower(@RequestBody Borrower borrower) {
        log.info("新增借阅人员: {}", borrower.getName());
        
        try {
            boolean success = borrowerService.addBorrower(borrower);
            if (success) {
                log.info("借阅人员添加成功: {}", borrower.getName());
                return Result.success("添加成功");
            }
            return Result.error("添加失败");
        } catch (Exception e) {
            log.error("添加借阅人员失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateBorrower(@PathVariable Long id, @RequestBody Borrower borrower) {
        log.info("更新借阅人员信息: {}", id);
        
        borrower.setId(id);
        
        try {
            boolean success = borrowerService.updateBorrower(borrower);
            if (success) {
                log.info("借阅人员更新成功: {}", id);
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新借阅人员失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBorrower(@PathVariable Long id) {
        log.info("删除借阅人员: {}", id);
        
        try {
            boolean success = borrowerService.deleteBorrower(id);
            if (success) {
                log.info("借阅人员删除成功: {}", id);
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除借阅人员失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateBorrowerStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新借阅人员状态: {}, 状态: {}", id, status);
        
        Borrower borrower = new Borrower();
        borrower.setId(id);
        borrower.setStatus(status);
        boolean success = borrowerService.updateById(borrower);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }
}
