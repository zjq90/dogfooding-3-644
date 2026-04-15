package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.DepartmentPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DepartmentPermissionMapper extends BaseMapper<DepartmentPermission> {
    
    @Select("SELECT * FROM sys_department_permission WHERE department_id = #{departmentId}")
    List<DepartmentPermission> selectByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Delete("DELETE FROM sys_department_permission WHERE department_id = #{departmentId}")
    int deleteByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Delete("DELETE FROM sys_department_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
