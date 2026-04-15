package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Employee;
import com.library.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/page")
    public Result<PageResult<Employee>> getEmployeePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestAttribute Integer role) {
        
        if (role != 1) {
            return Result.error("无权限查看人员列表");
        }
        
        PageResult<Employee> result = employeeService.getEmployeePage(page, size, keyword, departmentId);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<Employee>> getAllEmployees(@RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看人员列表");
        }
        
        List<Employee> employees = employeeService.getAllEmployees();
        return Result.success(employees);
    }

    @GetMapping("/department/{departmentId}")
    public Result<List<Employee>> getEmployeesByDepartment(@PathVariable Long departmentId, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看人员列表");
        }
        
        List<Employee> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        return Result.success(employees);
    }

    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看人员信息");
        }
        
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error("人员不存在");
    }

    @PostMapping
    public Result<Void> addEmployee(@RequestBody Employee employee, @RequestAttribute Integer role) {
        log.info("新增人员: {}", employee.getName());
        
        if (role != 1) {
            return Result.error("无权限添加人员");
        }
        
        try {
            boolean success = employeeService.addEmployee(employee);
            if (success) {
                log.info("人员添加成功: {}", employee.getName());
                return Result.success("添加成功");
            }
            return Result.error("添加失败");
        } catch (Exception e) {
            log.error("添加人员失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateEmployee(@PathVariable Long id, @RequestBody Employee employee, @RequestAttribute Integer role) {
        log.info("更新人员信息: {}", id);
        
        if (role != 1) {
            return Result.error("无权限修改人员信息");
        }
        
        employee.setId(id);
        
        try {
            boolean success = employeeService.updateEmployee(employee);
            if (success) {
                log.info("人员更新成功: {}", id);
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新人员失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEmployee(@PathVariable Long id, @RequestAttribute Integer role) {
        log.info("删除人员: {}", id);
        
        if (role != 1) {
            return Result.error("无权限删除人员");
        }
        
        try {
            boolean success = employeeService.deleteEmployee(id);
            if (success) {
                log.info("人员删除成功: {}", id);
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除人员失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateEmployeeStatus(@PathVariable Long id, @RequestParam Integer status, 
                                              @RequestAttribute Integer role) {
        log.info("更新人员状态: {}, 状态: {}", id, status);
        
        if (role != 1) {
            return Result.error("无权限修改人员状态");
        }
        
        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(status);
        boolean success = employeeService.updateById(employee);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }
}
