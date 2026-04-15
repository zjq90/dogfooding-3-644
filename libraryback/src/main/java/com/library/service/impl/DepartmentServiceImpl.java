package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.Department;
import com.library.mapper.DepartmentMapper;
import com.library.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public PageResult<Department> getDepartmentPage(Integer page, Integer size, String keyword) {
        Page<Department> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Department::getName, keyword)
                    .or()
                    .like(Department::getCode, keyword)
                    .or()
                    .like(Department::getDescription, keyword));
        }
        
        wrapper.orderByAsc(Department::getSortOrder);
        wrapper.orderByDesc(Department::getCreateTime);
        Page<Department> departmentPage = this.page(pageParam, wrapper);
        
        List<Department> records = departmentPage.getRecords();
        fillParentName(records);
        
        return new PageResult<>(departmentPage.getTotal(), records, 
                                departmentPage.getCurrent(), departmentPage.getSize());
    }

    @Override
    public List<Department> getDepartmentTree() {
        List<Department> allDepartments = baseMapper.selectAll();
        return buildTree(allDepartments, 0L);
    }

    @Override
    public List<Department> getAllDepartments() {
        List<Department> departments = baseMapper.selectAll();
        fillParentName(departments);
        return departments;
    }

    @Override
    public Department getByCode(String code) {
        return baseMapper.selectByCode(code);
    }

    @Override
    public boolean addDepartment(Department department) {
        if (!StringUtils.hasText(department.getName())) {
            throw new BusinessException("部门名称不能为空");
        }
        
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        
        if (!StringUtils.hasText(department.getCode())) {
            department.setCode(generateDepartmentCode());
        } else {
            Department existDept = getByCode(department.getCode());
            if (existDept != null) {
                throw new BusinessException("部门编号已存在");
            }
        }
        
        if (department.getSortOrder() == null) {
            department.setSortOrder(0);
        }
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        
        return this.save(department);
    }

    @Override
    public boolean updateDepartment(Department department) {
        if (department.getId() == null) {
            throw new BusinessException("部门ID不能为空");
        }
        
        Department existDept = this.getById(department.getId());
        if (existDept == null) {
            throw new BusinessException("部门不存在");
        }
        
        if (StringUtils.hasText(department.getCode()) && !department.getCode().equals(existDept.getCode())) {
            Department codeDept = getByCode(department.getCode());
            if (codeDept != null) {
                throw new BusinessException("部门编号已存在");
            }
        }
        
        if (department.getParentId() != null && department.getParentId().equals(department.getId())) {
            throw new BusinessException("上级部门不能是自己");
        }
        
        return this.updateById(department);
    }

    @Override
    public boolean deleteDepartment(Long id) {
        int childCount = baseMapper.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException("该部门下存在子部门，无法删除");
        }
        
        int employeeCount = baseMapper.countEmployeesByDepartmentId(id);
        if (employeeCount > 0) {
            throw new BusinessException("该部门下存在人员，无法删除");
        }
        
        return this.removeById(id);
    }

    @Override
    public String generateDepartmentCode() {
        List<Department> allDepartments = baseMapper.selectAll();
        int maxNum = 0;
        for (Department dept : allDepartments) {
            String code = dept.getCode();
            if (code != null && code.startsWith("B-")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return String.format("B-%03d", maxNum + 1);
    }
    
    private List<Department> buildTree(List<Department> departments, Long parentId) {
        List<Department> tree = new ArrayList<>();
        for (Department dept : departments) {
            if (parentId.equals(dept.getParentId())) {
                dept.setChildren(buildTree(departments, dept.getId()));
                tree.add(dept);
            }
        }
        return tree;
    }
    
    private void fillParentName(List<Department> departments) {
        if (departments == null || departments.isEmpty()) {
            return;
        }
        
        List<Department> allDepartments = baseMapper.selectAll();
        Map<Long, String> deptMap = allDepartments.stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
        
        for (Department dept : departments) {
            if (dept.getParentId() != null && dept.getParentId() > 0) {
                dept.setParentName(deptMap.get(dept.getParentId()));
            }
        }
    }
}
