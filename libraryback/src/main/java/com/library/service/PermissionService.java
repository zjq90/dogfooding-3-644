package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Permission;
import java.util.List;

public interface PermissionService extends IService<Permission> {
    
    List<Permission> getAllPermissions();
    
    List<Permission> getPermissionTree();
    
    List<Permission> getPermissionsByType(Integer type);
    
    List<Permission> getPermissionsByDepartmentId(Long departmentId);
    
    Permission getByCode(String code);
    
    boolean addPermission(Permission permission);
    
    boolean updatePermission(Permission permission);
    
    boolean deletePermission(Long id);
}
