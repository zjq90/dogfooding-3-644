package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    @Select("SELECT * FROM sys_permission WHERE code = #{code} AND deleted = 0")
    Permission selectByCode(@Param("code") String code);
    
    @Select("SELECT * FROM sys_permission WHERE type = #{type} AND deleted = 0 ORDER BY sort_order")
    List<Permission> selectByType(@Param("type") Integer type);
    
    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order")
    List<Permission> selectByParentId(@Param("parentId") Long parentId);
    
    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY type, sort_order")
    List<Permission> selectAll();
    
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_department_permission dp ON p.id = dp.permission_id " +
            "WHERE dp.department_id = #{departmentId} AND p.deleted = 0 " +
            "ORDER BY p.type, p.sort_order")
    List<Permission> selectByDepartmentId(@Param("departmentId") Long departmentId);
}
