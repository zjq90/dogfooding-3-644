package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.DepartmentPermission;
import java.util.List;

public interface DepartmentPermissionService extends IService<DepartmentPermission> {
    
    List<Long> getPermissionIdsByDepartmentId(Long departmentId);
    
    boolean assignPermissions(Long departmentId, List<Long> permissionIds);
    
    boolean removePermissions(Long departmentId);
}
