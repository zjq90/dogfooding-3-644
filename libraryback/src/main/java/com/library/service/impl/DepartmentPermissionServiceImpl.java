package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.DepartmentPermission;
import com.library.mapper.DepartmentPermissionMapper;
import com.library.service.DepartmentPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DepartmentPermissionServiceImpl extends ServiceImpl<DepartmentPermissionMapper, DepartmentPermission> implements DepartmentPermissionService {

    @Override
    public List<Long> getPermissionIdsByDepartmentId(Long departmentId) {
        List<DepartmentPermission> list = baseMapper.selectByDepartmentId(departmentId);
        return list.stream()
                .map(DepartmentPermission::getPermissionId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignPermissions(Long departmentId, List<Long> permissionIds) {
        baseMapper.deleteByDepartmentId(departmentId);
        
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                DepartmentPermission dp = new DepartmentPermission();
                dp.setDepartmentId(departmentId);
                dp.setPermissionId(permissionId);
                this.save(dp);
            }
        }
        
        return true;
    }

    @Override
    public boolean removePermissions(Long departmentId) {
        return baseMapper.deleteByDepartmentId(departmentId) >= 0;
    }
}
