package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("borrower")
public class Borrower {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String borrowerNo;
    
    private String name;
    
    private Integer gender;
    
    private String phone;
    
    private String email;
    
    private String idCard;
    
    private String address;
    
    private BigDecimal depositAmount;
    
    private BigDecimal balance;
    
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
