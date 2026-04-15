package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_notification")
public class PlatformNotification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private Integer notificationType;
    
    private Long orderId;
    
    private Long borrowerId;
    
    private Integer isRead;
    
    private LocalDateTime readTime;
    
    private String readBy;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
    
    @TableField(exist = false)
    private String borrowerName;
    
    @TableField(exist = false)
    private String orderNo;
}
