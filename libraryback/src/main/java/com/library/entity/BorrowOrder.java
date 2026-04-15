package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_order")
public class BorrowOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
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
    
    private BigDecimal depositAmount;
    
    private Integer borrowDays;
    
    private LocalDate borrowDate;
    
    private LocalDate dueDate;
    
    private LocalDate returnDate;
    
    private Integer depositStatus;
    
    private Integer paymentStatus;
    
    private Integer overdueDays;
    
    private BigDecimal overdueFine;
    
    private Integer status;
    
    private String operator;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
