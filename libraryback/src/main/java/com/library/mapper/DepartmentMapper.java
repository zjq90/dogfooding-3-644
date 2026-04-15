package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    
    @Select("SELECT * FROM sys_department WHERE code = #{code} AND deleted = 0")
    Department selectByCode(@Param("code") String code);
    
    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order")
    List<Department> selectByParentId(@Param("parentId") Long parentId);
    
    @Select("SELECT * FROM sys_department WHERE deleted = 0 ORDER BY sort_order")
    List<Department> selectAll();
    
    @Select("SELECT COUNT(*) FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0")
    int countByParentId(@Param("parentId") Long parentId);
    
    @Select("SELECT COUNT(*) FROM sys_employee WHERE department_id = #{departmentId} AND deleted = 0")
    int countEmployeesByDepartmentId(@Param("departmentId") Long departmentId);
}
