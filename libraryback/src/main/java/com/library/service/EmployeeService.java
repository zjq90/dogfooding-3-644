package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Employee;
import java.util.List;

public interface EmployeeService extends IService<Employee> {
    
    PageResult<Employee> getEmployeePage(Integer page, Integer size, String keyword, Long departmentId);
    
    List<Employee> getAllEmployees();
    
    List<Employee> getEmployeesByDepartmentId(Long departmentId);
    
    Employee getByEmployeeNo(String employeeNo);
    
    boolean addEmployee(Employee employee);
    
    boolean updateEmployee(Employee employee);
    
    boolean deleteEmployee(Long id);
}
