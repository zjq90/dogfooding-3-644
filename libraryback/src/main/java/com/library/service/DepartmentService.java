package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Department;
import java.util.List;

public interface DepartmentService extends IService<Department> {
    
    PageResult<Department> getDepartmentPage(Integer page, Integer size, String keyword);
    
    List<Department> getDepartmentTree();
    
    List<Department> getAllDepartments();
    
    Department getByCode(String code);
    
    boolean addDepartment(Department department);
    
    boolean updateDepartment(Department department);
    
    boolean deleteDepartment(Long id);
    
    String generateDepartmentCode();
}
