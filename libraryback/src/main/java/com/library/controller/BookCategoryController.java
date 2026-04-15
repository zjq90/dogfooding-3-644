package com.library.controller;

import com.library.common.Result;
import com.library.entity.BookCategory;
import com.library.service.BookCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class BookCategoryController {

    @Autowired
    private BookCategoryService categoryService;

    @GetMapping("/list")
    public Result<List<BookCategory>> getAllCategories() {
        List<BookCategory> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/tree")
    public Result<List<BookCategory>> getCategoryTree() {
        List<BookCategory> categories = categoryService.getCategoryTree();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<BookCategory> getCategoryById(@PathVariable Long id) {
        BookCategory category = categoryService.getById(id);
        if (category != null) {
            return Result.success(category);
        }
        return Result.error("分类不存在");
    }

    @PostMapping
    public Result<Void> addCategory(@RequestBody BookCategory category, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限添加分类");
        }
        
        log.info("新增分类: {}", category.getName());
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.error("分类名称不能为空");
        }
        if (category.getCode() == null || category.getCode().trim().isEmpty()) {
            return Result.error("分类编码不能为空");
        }
        
        boolean success = categoryService.save(category);
        if (success) {
            log.info("分类添加成功: {}", category.getName());
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody BookCategory category, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限修改分类");
        }
        
        log.info("更新分类信息: {}", id);
        category.setId(id);
        boolean success = categoryService.updateById(category);
        if (success) {
            log.info("分类更新成功: {}", id);
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限删除分类");
        }
        
        log.info("删除分类: {}", id);
        boolean success = categoryService.removeById(id);
        if (success) {
            log.info("分类删除成功: {}", id);
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
