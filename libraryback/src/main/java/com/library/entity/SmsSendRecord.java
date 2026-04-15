package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sms_send_record")
public class SmsSendRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String phone;
    
    private String templateCode;
    
    private String content;
    
    private String params;
    
    private Integer sendStatus;
    
    private LocalDateTime sendTime;
    
    private String failReason;
    
    private String provider;
    
    private String bizId;
    
    private String bizType;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
