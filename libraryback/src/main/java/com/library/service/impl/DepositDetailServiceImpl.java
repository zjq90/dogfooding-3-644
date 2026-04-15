package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.Borrower;
import com.library.entity.DepositDetail;
import com.library.mapper.BorrowerMapper;
import com.library.mapper.DepositDetailMapper;
import com.library.service.DepositDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DepositDetailServiceImpl extends ServiceImpl<DepositDetailMapper, DepositDetail> implements DepositDetailService {

    @Autowired
    private BorrowerMapper borrowerMapper;

    @Override
    public PageResult<DepositDetail> getDepositDetailPage(Integer page, Integer size, String keyword, Integer type, Long borrowerId) {
        Page<DepositDetail> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DepositDetail> wrapper = new LambdaQueryWrapper<>();
        
        if (borrowerId != null && borrowerId > 0) {
            wrapper.eq(DepositDetail::getBorrowerId, borrowerId);
        }
        
        if (type != null) {
            wrapper.eq(DepositDetail::getType, type);
        }
        
        wrapper.orderByDesc(DepositDetail::getCreateTime);
        Page<DepositDetail> detailPage = this.page(pageParam, wrapper);
        
        List<DepositDetail> records = detailPage.getRecords();
        fillBorrowerInfo(records);
        
        return new PageResult<>(detailPage.getTotal(), records, 
                                detailPage.getCurrent(), detailPage.getSize());
    }

    @Override
    @Transactional
    public boolean addDeposit(Long borrowerId, BigDecimal amount, Integer paymentMethod, String operator, String remark) {
        Borrower borrower = borrowerMapper.selectById(borrowerId);
        if (borrower == null) {
            throw new BusinessException("借阅人员不存在");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        
        BigDecimal beforeBalance = borrower.getBalance();
        BigDecimal afterBalance = beforeBalance.add(amount);
        
        DepositDetail detail = new DepositDetail();
        detail.setBorrowerId(borrowerId);
        detail.setType(1);
        detail.setAmount(amount);
        detail.setBeforeBalance(beforeBalance);
        detail.setAfterBalance(afterBalance);
        detail.setPaymentMethod(paymentMethod);
        detail.setOperator(operator);
        detail.setStatus(1);
        detail.setRemark(remark);
        
        borrower.setBalance(afterBalance);
        borrower.setDepositAmount(afterBalance);
        
        borrowerMapper.updateById(borrower);
        return this.save(detail);
    }

    @Override
    @Transactional
    public boolean refundDeposit(Long borrowerId, BigDecimal amount, Integer paymentMethod, String operator, String remark) {
        Borrower borrower = borrowerMapper.selectById(borrowerId);
        if (borrower == null) {
            throw new BusinessException("借阅人员不存在");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        
        BigDecimal beforeBalance = borrower.getBalance();
        if (beforeBalance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }
        
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        
        DepositDetail detail = new DepositDetail();
        detail.setBorrowerId(borrowerId);
        detail.setType(2);
        detail.setAmount(amount);
        detail.setBeforeBalance(beforeBalance);
        detail.setAfterBalance(afterBalance);
        detail.setPaymentMethod(paymentMethod);
        detail.setOperator(operator);
        detail.setStatus(1);
        detail.setRemark(remark);
        
        borrower.setBalance(afterBalance);
        borrower.setDepositAmount(afterBalance);
        
        borrowerMapper.updateById(borrower);
        return this.save(detail);
    }

    @Override
    @Transactional
    public boolean deductDeposit(Long borrowerId, BigDecimal amount, String orderNo, String operator, String remark) {
        Borrower borrower = borrowerMapper.selectById(borrowerId);
        if (borrower == null) {
            throw new BusinessException("借阅人员不存在");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        
        BigDecimal beforeBalance = borrower.getBalance();
        if (beforeBalance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }
        
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        
        DepositDetail detail = new DepositDetail();
        detail.setBorrowerId(borrowerId);
        detail.setOrderNo(orderNo);
        detail.setType(3);
        detail.setAmount(amount);
        detail.setBeforeBalance(beforeBalance);
        detail.setAfterBalance(afterBalance);
        detail.setPaymentMethod(1);
        detail.setOperator(operator);
        detail.setStatus(1);
        detail.setRemark(remark);
        
        borrower.setBalance(afterBalance);
        borrower.setDepositAmount(afterBalance);
        
        borrowerMapper.updateById(borrower);
        return this.save(detail);
    }
    
    private void fillBorrowerInfo(List<DepositDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        
        List<Long> borrowerIds = details.stream()
                .map(DepositDetail::getBorrowerId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Borrower> borrowers = borrowerMapper.selectBatchIds(borrowerIds);
        Map<Long, Borrower> borrowerMap = borrowers.stream()
                .collect(Collectors.toMap(Borrower::getId, b -> b));
        
        for (DepositDetail detail : details) {
            Borrower borrower = borrowerMap.get(detail.getBorrowerId());
            if (borrower != null) {
                detail.setBorrowerName(borrower.getName());
                detail.setBorrowerPhone(borrower.getPhone());
            }
        }
    }
}
