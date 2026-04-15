package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
    
    @Select("SELECT * FROM sys_employee WHERE employee_no = #{employeeNo} AND deleted = 0")
    Employee selectByEmployeeNo(@Param("employeeNo") String employeeNo);
    
    @Select("SELECT e.*, d.name as department_name FROM sys_employee e " +
            "LEFT JOIN sys_department d ON e.department_id = d.id " +
            "WHERE e.deleted = 0 ORDER BY e.create_time DESC")
    List<Employee> selectAllWithDepartmentName();
    
    @Select("SELECT e.*, d.name as department_name FROM sys_employee e " +
            "LEFT JOIN sys_department d ON e.department_id = d.id " +
            "WHERE e.department_id = #{departmentId} AND e.deleted = 0")
    List<Employee> selectByDepartmentId(@Param("departmentId") Long departmentId);
}
