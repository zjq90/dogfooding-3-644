package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("borrow_reminder")
public class BorrowReminder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long borrowerId;
    
    private Long bookId;
    
    private Integer reminderType;
    
    private Integer reminderDay;
    
    private Integer smsSent;
    
    private LocalDateTime smsSendTime;
    
    private Integer platformNotified;
    
    private Integer depositDeducted;
    
    private LocalDateTime depositDeductTime;
    
    private BigDecimal deductAmount;
    
    private Integer status;
    
    private String remark;
    
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
    private String borrowerPhone;
    
    @TableField(exist = false)
    private String bookTitle;
    
    @TableField(exist = false)
    private String orderNo;
}
