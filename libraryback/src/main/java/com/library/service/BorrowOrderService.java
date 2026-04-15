package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.BorrowOrder;

public interface BorrowOrderService extends IService<BorrowOrder> {
    
    PageResult<BorrowOrder> getBorrowOrderPage(Integer page, Integer size, String keyword, Integer status, Integer depositStatus, Integer paymentStatus);
    
    BorrowOrder getByOrderNo(String orderNo);
    
    boolean createBorrowOrder(BorrowOrder borrowOrder);
    
    boolean returnBook(Long orderId);
    
    boolean refundDeposit(Long orderId);
}
