package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("deposit_detail")
public class DepositDetail {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long borrowerId;
    
    @TableField(exist = false)
    private String borrowerName;
    
    @TableField(exist = false)
    private String borrowerPhone;
    
    private String orderNo;
    
    private Integer type;
    
    private BigDecimal amount;
    
    private BigDecimal beforeBalance;
    
    private BigDecimal afterBalance;
    
    private Integer paymentMethod;
    
    private String operator;
    
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
