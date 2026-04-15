package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_department_permission")
public class DepartmentPermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long departmentId;
    
    private Long permissionId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
