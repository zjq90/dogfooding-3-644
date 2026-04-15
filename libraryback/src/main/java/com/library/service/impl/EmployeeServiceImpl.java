package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.Department;
import com.library.entity.Employee;
import com.library.mapper.DepartmentMapper;
import com.library.mapper.EmployeeMapper;
import com.library.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public PageResult<Employee> getEmployeePage(Integer page, Integer size, String keyword, Long departmentId) {
        Page<Employee> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getName, keyword)
                    .or()
                    .like(Employee::getEmployeeNo, keyword)
                    .or()
                    .like(Employee::getPhone, keyword)
                    .or()
                    .like(Employee::getEmail, keyword));
        }
        
        if (departmentId != null && departmentId > 0) {
            wrapper.eq(Employee::getDepartmentId, departmentId);
        }
        
        wrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> employeePage = this.page(pageParam, wrapper);
        
        List<Employee> records = employeePage.getRecords();
        fillDepartmentName(records);
        
        return new PageResult<>(employeePage.getTotal(), records, 
                                employeePage.getCurrent(), employeePage.getSize());
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = baseMapper.selectAllWithDepartmentName();
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByDepartmentId(Long departmentId) {
        List<Employee> employees = baseMapper.selectByDepartmentId(departmentId);
        return employees;
    }

    @Override
    public Employee getByEmployeeNo(String employeeNo) {
        return baseMapper.selectByEmployeeNo(employeeNo);
    }

    @Override
    public boolean addEmployee(Employee employee) {
        if (!StringUtils.hasText(employee.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        
        if (employee.getDepartmentId() == null) {
            throw new BusinessException("所属部门不能为空");
        }
        
        Department department = departmentMapper.selectById(employee.getDepartmentId());
        if (department == null) {
            throw new BusinessException("所属部门不存在");
        }
        
        if (StringUtils.hasText(employee.getEmployeeNo())) {
            Employee existEmployee = getByEmployeeNo(employee.getEmployeeNo());
            if (existEmployee != null) {
                throw new BusinessException("工号已存在");
            }
        }
        
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        
        return this.save(employee);
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        if (employee.getId() == null) {
            throw new BusinessException("人员ID不能为空");
        }
        
        Employee existEmployee = this.getById(employee.getId());
        if (existEmployee == null) {
            throw new BusinessException("人员不存在");
        }
        
        if (employee.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(employee.getDepartmentId());
            if (department == null) {
                throw new BusinessException("所属部门不存在");
            }
        }
        
        if (StringUtils.hasText(employee.getEmployeeNo()) && !employee.getEmployeeNo().equals(existEmployee.getEmployeeNo())) {
            Employee codeEmployee = getByEmployeeNo(employee.getEmployeeNo());
            if (codeEmployee != null) {
                throw new BusinessException("工号已存在");
            }
        }
        
        return this.updateById(employee);
    }

    @Override
    public boolean deleteEmployee(Long id) {
        return this.removeById(id);
    }
    
    private void fillDepartmentName(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return;
        }
        
        List<Department> allDepartments = departmentMapper.selectAll();
        Map<Long, String> deptMap = allDepartments.stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
        
        for (Employee employee : employees) {
            if (employee.getDepartmentId() != null) {
                employee.setDepartmentName(deptMap.get(employee.getDepartmentId()));
            }
        }
    }
}
