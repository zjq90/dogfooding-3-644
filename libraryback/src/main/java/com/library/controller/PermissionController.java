package com.library.controller;

import com.library.common.Result;
import com.library.entity.Permission;
import com.library.service.DepartmentPermissionService;
import com.library.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DepartmentPermissionService departmentPermissionService;

    @GetMapping("/list")
    public Result<List<Permission>> getAllPermissions(@RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看权限列表");
        }
        
        List<Permission> permissions = permissionService.getAllPermissions();
        return Result.success(permissions);
    }

    @GetMapping("/tree")
    public Result<List<Permission>> getPermissionTree(@RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看权限树");
        }
        
        List<Permission> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    @GetMapping("/type/{type}")
    public Result<List<Permission>> getPermissionsByType(@PathVariable Integer type, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看权限列表");
        }
        
        List<Permission> permissions = permissionService.getPermissionsByType(type);
        return Result.success(permissions);
    }

    @GetMapping("/department/{departmentId}")
    public Result<List<Permission>> getPermissionsByDepartment(@PathVariable Long departmentId, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看部门权限");
        }
        
        List<Permission> permissions = permissionService.getPermissionsByDepartmentId(departmentId);
        return Result.success(permissions);
    }

    @GetMapping("/{id}")
    public Result<Permission> getPermissionById(@PathVariable Long id, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看权限信息");
        }
        
        Permission permission = permissionService.getById(id);
        if (permission != null) {
            return Result.success(permission);
        }
        return Result.error("权限不存在");
    }

    @PostMapping
    public Result<Void> addPermission(@RequestBody Permission permission, @RequestAttribute Integer role) {
        log.info("新增权限: {}", permission.getName());
        
        if (role != 1) {
            return Result.error("无权限添加权限");
        }
        
        try {
            boolean success = permissionService.addPermission(permission);
            if (success) {
                log.info("权限添加成功: {}", permission.getName());
                return Result.success("添加成功");
            }
            return Result.error("添加失败");
        } catch (Exception e) {
            log.error("添加权限失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updatePermission(@PathVariable Long id, @RequestBody Permission permission, @RequestAttribute Integer role) {
        log.info("更新权限信息: {}", id);
        
        if (role != 1) {
            return Result.error("无权限修改权限信息");
        }
        
        permission.setId(id);
        
        try {
            boolean success = permissionService.updatePermission(permission);
            if (success) {
                log.info("权限更新成功: {}", id);
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新权限失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@PathVariable Long id, @RequestAttribute Integer role) {
        log.info("删除权限: {}", id);
        
        if (role != 1) {
            return Result.error("无权限删除权限");
        }
        
        try {
            boolean success = permissionService.deletePermission(id);
            if (success) {
                log.info("权限删除成功: {}", id);
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除权限失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updatePermissionStatus(@PathVariable Long id, @RequestParam Integer status, 
                                                @RequestAttribute Integer role) {
        log.info("更新权限状态: {}, 状态: {}", id, status);
        
        if (role != 1) {
            return Result.error("无权限修改权限状态");
        }
        
        Permission permission = new Permission();
        permission.setId(id);
        permission.setStatus(status);
        boolean success = permissionService.updateById(permission);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }

    @GetMapping("/department/{departmentId}/ids")
    public Result<List<Long>> getDepartmentPermissionIds(@PathVariable Long departmentId, @RequestAttribute Integer role) {
        if (role != 1) {
            return Result.error("无权限查看部门权限");
        }
        
        List<Long> permissionIds = departmentPermissionService.getPermissionIdsByDepartmentId(departmentId);
        return Result.success(permissionIds);
    }

    @PostMapping("/department/{departmentId}")
    public Result<Void> assignPermissions(@PathVariable Long departmentId, @RequestBody List<Long> permissionIds, 
                                          @RequestAttribute Integer role) {
        log.info("分配部门权限: 部门ID={}, 权限数量={}", departmentId, permissionIds != null ? permissionIds.size() : 0);
        
        if (role != 1) {
            return Result.error("无权限分配部门权限");
        }
        
        try {
            boolean success = departmentPermissionService.assignPermissions(departmentId, permissionIds);
            if (success) {
                log.info("部门权限分配成功: {}", departmentId);
                return Result.success("分配成功");
            }
            return Result.error("分配失败");
        } catch (Exception e) {
            log.error("分配部门权限失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
