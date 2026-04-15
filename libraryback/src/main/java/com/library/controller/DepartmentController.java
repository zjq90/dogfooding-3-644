package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Department;
import com.library.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/page")
    public Result<PageResult<Department>> getDepartmentPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestAttribute Integer role) {
        
        if (role != 1) {
            return Result.error("无权限查看部门列表");
        }
        
        PageResult<Department> result = departmentService.getDepartmentPage(page, size, keyword);
        return Result.success(result);
    }

    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree(@RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看部门树");
        }
        
        List<Department> tree = departmentService.getDepartmentTree();
        return Result.success(tree);
    }

    @GetMapping("/list")
    public Result<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return Result.success(departments);
    }

    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看部门信息");
        }
        
        Department department = departmentService.getById(id);
        if (department != null) {
            return Result.success(department);
        }
        return Result.error("部门不存在");
    }

    @PostMapping
    public Result<Void> addDepartment(@RequestBody Department department, @RequestAttribute Integer role) {
        log.info("新增部门: {}", department.getName());
        
        if (role != 1) {
            return Result.error("无权限添加部门");
        }
        
        try {
            boolean success = departmentService.addDepartment(department);
            if (success) {
                log.info("部门添加成功: {}", department.getName());
                return Result.success("添加成功");
            }
            return Result.error("添加失败");
        } catch (Exception e) {
            log.error("添加部门失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @RequestBody Department department, @RequestAttribute Integer role) {
        log.info("更新部门信息: {}", id);
        
        if (role != 1) {
            return Result.error("无权限修改部门信息");
        }
        
        department.setId(id);
        
        try {
            boolean success = departmentService.updateDepartment(department);
            if (success) {
                log.info("部门更新成功: {}", id);
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新部门失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id, @RequestAttribute Integer role) {
        log.info("删除部门: {}", id);
        
        if (role != 1) {
            return Result.error("无权限删除部门");
        }
        
        try {
            boolean success = departmentService.deleteDepartment(id);
            if (success) {
                log.info("部门删除成功: {}", id);
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除部门失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateDepartmentStatus(@PathVariable Long id, @RequestParam Integer status, 
                                                @RequestAttribute Integer role) {
        log.info("更新部门状态: {}, 状态: {}", id, status);
        
        if (role != 1) {
            return Result.error("无权限修改部门状态");
        }
        
        Department department = new Department();
        department.setId(id);
        department.setStatus(status);
        boolean success = departmentService.updateById(department);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }

    @GetMapping("/generate-code")
    public Result<String> generateDepartmentCode(@RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限生成部门编号");
        }
        
        String code = departmentService.generateDepartmentCode();
        return Result.success(code);
    }
}
