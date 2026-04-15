package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("return_reminder")
public class ReturnReminder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long borrowerId;
    
    @TableField(exist = false)
    private String borrowerName;
    
    @TableField(exist = false)
    private String borrowerPhone;
    
    private Long bookId;
    
    @TableField(exist = false)
    private String bookTitle;
    
    @TableField(exist = false)
    private String bookIsbn;
    
    @TableField(exist = false)
    private String orderNo;
    
    @TableField(exist = false)
    private LocalDate dueDate;
    
    private LocalDate reminderDate;
    
    private Integer reminderCount;
    
    private Integer smsSent;
    
    private String smsResponse;
    
    private Integer platformNotified;
    
    private Integer depositDeducted;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
