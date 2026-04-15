package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sms_config")
public class SmsConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String provider;
    
    private String accessKey;
    
    private String secretKey;
    
    private String signName;
    
    private String templateCode;
    
    private String region;
    
    private String endpoint;
    
    private Integer status;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
