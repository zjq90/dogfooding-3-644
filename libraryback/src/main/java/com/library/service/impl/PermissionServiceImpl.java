package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.entity.Permission;
import com.library.mapper.DepartmentPermissionMapper;
import com.library.mapper.PermissionMapper;
import com.library.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private DepartmentPermissionMapper departmentPermissionMapper;

    @Override
    public List<Permission> getAllPermissions() {
        return baseMapper.selectAll();
    }

    @Override
    public List<Permission> getPermissionTree() {
        List<Permission> allPermissions = baseMapper.selectAll();
        return buildTree(allPermissions, 0L);
    }

    @Override
    public List<Permission> getPermissionsByType(Integer type) {
        return baseMapper.selectByType(type);
    }

    @Override
    public List<Permission> getPermissionsByDepartmentId(Long departmentId) {
        return baseMapper.selectByDepartmentId(departmentId);
    }

    @Override
    public Permission getByCode(String code) {
        return baseMapper.selectByCode(code);
    }

    @Override
    public boolean addPermission(Permission permission) {
        if (!StringUtils.hasText(permission.getName())) {
            throw new BusinessException("权限名称不能为空");
        }
        if (!StringUtils.hasText(permission.getCode())) {
            throw new BusinessException("权限编码不能为空");
        }
        if (permission.getType() == null) {
            throw new BusinessException("权限类型不能为空");
        }
        
        Permission existPermission = getByCode(permission.getCode());
        if (existPermission != null) {
            throw new BusinessException("权限编码已存在");
        }
        
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        
        return this.save(permission);
    }

    @Override
    public boolean updatePermission(Permission permission) {
        if (permission.getId() == null) {
            throw new BusinessException("权限ID不能为空");
        }
        
        Permission existPermission = this.getById(permission.getId());
        if (existPermission == null) {
            throw new BusinessException("权限不存在");
        }
        
        if (StringUtils.hasText(permission.getCode()) && !permission.getCode().equals(existPermission.getCode())) {
            Permission codePermission = getByCode(permission.getCode());
            if (codePermission != null) {
                throw new BusinessException("权限编码已存在");
            }
        }
        
        return this.updateById(permission);
    }

    @Override
    public boolean deletePermission(Long id) {
        departmentPermissionMapper.deleteByPermissionId(id);
        return this.removeById(id);
    }
    
    private List<Permission> buildTree(List<Permission> permissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();
        for (Permission perm : permissions) {
            if (parentId.equals(perm.getParentId())) {
                perm.setChildren(buildTree(permissions, perm.getId()));
                tree.add(perm);
            }
        }
        return tree;
    }
}
