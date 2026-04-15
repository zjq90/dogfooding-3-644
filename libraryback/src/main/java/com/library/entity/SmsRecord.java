package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sms_record")
public class SmsRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String phone;
    
    private String templateCode;
    
    private String templateParam;
    
    private String content;
    
    private Integer sendStatus;
    
    private String responseCode;
    
    private String responseMessage;
    
    private String requestId;
    
    private LocalDateTime sendTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
