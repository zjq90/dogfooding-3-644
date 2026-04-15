package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.Borrower;
import com.library.mapper.BorrowerMapper;
import com.library.service.BorrowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Override
    public PageResult<Borrower> getBorrowerPage(Integer page, Integer size, String keyword, Integer status) {
        Page<Borrower> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Borrower> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Borrower::getName, keyword)
                    .or()
                    .like(Borrower::getBorrowerNo, keyword)
                    .or()
                    .like(Borrower::getPhone, keyword));
        }
        
        if (status != null) {
            wrapper.eq(Borrower::getStatus, status);
        }
        
        wrapper.orderByDesc(Borrower::getCreateTime);
        Page<Borrower> borrowerPage = this.page(pageParam, wrapper);
        
        return new PageResult<>(borrowerPage.getTotal(), borrowerPage.getRecords(), 
                                borrowerPage.getCurrent(), borrowerPage.getSize());
    }

    @Override
    public Borrower getByBorrowerNo(String borrowerNo) {
        return baseMapper.selectByBorrowerNo(borrowerNo);
    }

    @Override
    public Borrower getByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }

    @Override
    public boolean addBorrower(Borrower borrower) {
        if (!StringUtils.hasText(borrower.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        
        if (!StringUtils.hasText(borrower.getPhone())) {
            throw new BusinessException("手机号不能为空");
        }
        
        if (StringUtils.hasText(borrower.getBorrowerNo())) {
            Borrower existBorrower = getByBorrowerNo(borrower.getBorrowerNo());
            if (existBorrower != null) {
                throw new BusinessException("借阅证号已存在");
            }
        }
        
        Borrower phoneBorrower = getByPhone(borrower.getPhone());
        if (phoneBorrower != null) {
            throw new BusinessException("手机号已存在");
        }
        
        if (borrower.getStatus() == null) {
            borrower.setStatus(1);
        }
        
        if (borrower.getBalance() == null) {
            borrower.setBalance(BigDecimal.ZERO);
        }
        
        if (borrower.getDepositAmount() == null) {
            borrower.setDepositAmount(BigDecimal.ZERO);
        }
        
        return this.save(borrower);
    }

    @Override
    public boolean updateBorrower(Borrower borrower) {
        if (borrower.getId() == null) {
            throw new BusinessException("借阅人员ID不能为空");
        }
        
        Borrower existBorrower = this.getById(borrower.getId());
        if (existBorrower == null) {
            throw new BusinessException("借阅人员不存在");
        }
        
        if (StringUtils.hasText(borrower.getPhone()) && !borrower.getPhone().equals(existBorrower.getPhone())) {
            Borrower phoneBorrower = getByPhone(borrower.getPhone());
            if (phoneBorrower != null) {
                throw new BusinessException("手机号已存在");
            }
        }
        
        if (StringUtils.hasText(borrower.getBorrowerNo()) && !borrower.getBorrowerNo().equals(existBorrower.getBorrowerNo())) {
            Borrower noBorrower = getByBorrowerNo(borrower.getBorrowerNo());
            if (noBorrower != null) {
                throw new BusinessException("借阅证号已存在");
            }
        }
        
        return this.updateById(borrower);
    }

    @Override
    public boolean deleteBorrower(Long id) {
        return this.removeById(id);
    }
}
