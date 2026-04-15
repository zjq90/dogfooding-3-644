package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.DepositDetail;

import java.math.BigDecimal;

public interface DepositDetailService extends IService<DepositDetail> {
    
    PageResult<DepositDetail> getDepositDetailPage(Integer page, Integer size, String keyword, Integer type, Long borrowerId);
    
    boolean addDeposit(Long borrowerId, BigDecimal amount, Integer paymentMethod, String operator, String remark);
    
    boolean refundDeposit(Long borrowerId, BigDecimal amount, Integer paymentMethod, String operator, String remark);
    
    boolean deductDeposit(Long borrowerId, BigDecimal amount, String orderNo, String operator, String remark);
}
