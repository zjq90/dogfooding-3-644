package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BookInfo;
import com.library.service.BookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    @GetMapping("/page")
    public Result<PageResult<BookInfo>> getBookPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        
        PageResult<BookInfo> result = bookInfoService.getBookPage(page, size, keyword, categoryId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<BookInfo> getBookById(@PathVariable Long id) {
        BookInfo book = bookInfoService.getBookDetail(id);
        if (book != null) {
            return Result.success(book);
        }
        return Result.error("图书不存在");
    }

    @PostMapping
    public Result<Void> addBook(@RequestBody BookInfo book, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限添加图书");
        }
        
        log.info("新增图书: {}", book.getTitle());
        
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return Result.error("书名不能为空");
        }
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            return Result.error("ISBN不能为空");
        }
        
        if (book.getAvailableQuantity() == null) {
            book.setAvailableQuantity(book.getTotalQuantity() != null ? book.getTotalQuantity() : 0);
        }
        if (book.getStatus() == null) {
            book.setStatus(1);
        }
        
        boolean success = bookInfoService.save(book);
        if (success) {
            log.info("图书添加成功: {}", book.getTitle());
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @RequestBody BookInfo book, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限修改图书");
        }
        
        log.info("更新图书信息: {}", id);
        book.setId(id);
        boolean success = bookInfoService.updateById(book);
        if (success) {
            log.info("图书更新成功: {}", id);
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限删除图书");
        }
        
        log.info("删除图书: {}", id);
        boolean success = bookInfoService.removeById(id);
        if (success) {
            log.info("图书删除成功: {}", id);
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @GetMapping("/stats/category")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = bookInfoService.getCategoryStats();
        return Result.success(stats);
    }

    @GetMapping("/count")
    public Result<Map<String, Long>> getBookCount() {
        Map<String, Long> result = new java.util.HashMap<>();
        result.put("bookCount", bookInfoService.getBookCount());
        result.put("totalQuantity", bookInfoService.getTotalBookQuantity());
        return Result.success(result);
    }
}
