package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Borrower;

public interface BorrowerService extends IService<Borrower> {
    
    PageResult<Borrower> getBorrowerPage(Integer page, Integer size, String keyword, Integer status);
    
    Borrower getByBorrowerNo(String borrowerNo);
    
    Borrower getByPhone(String phone);
    
    boolean addBorrower(Borrower borrower);
    
    boolean updateBorrower(Borrower borrower);
    
    boolean deleteBorrower(Long id);
}
