package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.BookInfo;
import com.library.entity.BorrowOrder;
import com.library.entity.Borrower;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BorrowOrderMapper;
import com.library.mapper.BorrowerMapper;
import com.library.service.BorrowOrderService;
import com.library.service.BorrowReminderService;
import com.library.service.DepositDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BorrowOrderServiceImpl extends ServiceImpl<BorrowOrderMapper, BorrowOrder> implements BorrowOrderService {

    @Autowired
    private BorrowerMapper borrowerMapper;
    
    @Autowired
    private BookInfoMapper bookInfoMapper;
    
    @Autowired
    private DepositDetailService depositDetailService;
    
    @Autowired
    private BorrowReminderService borrowReminderService;

    @Override
    public PageResult<BorrowOrder> getBorrowOrderPage(Integer page, Integer size, String keyword, Integer status, Integer depositStatus, Integer paymentStatus) {
        Page<BorrowOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BorrowOrder::getOrderNo, keyword));
        }
        
        if (status != null) {
            wrapper.eq(BorrowOrder::getStatus, status);
        }
        
        if (depositStatus != null) {
            wrapper.eq(BorrowOrder::getDepositStatus, depositStatus);
        }
        
        if (paymentStatus != null) {
            wrapper.eq(BorrowOrder::getPaymentStatus, paymentStatus);
        }
        
        wrapper.orderByDesc(BorrowOrder::getCreateTime);
        Page<BorrowOrder> orderPage = this.page(pageParam, wrapper);
        
        List<BorrowOrder> records = orderPage.getRecords();
        fillRelatedInfo(records);
        
        return new PageResult<>(orderPage.getTotal(), records, 
                                orderPage.getCurrent(), orderPage.getSize());
    }

    @Override
    public BorrowOrder getByOrderNo(String orderNo) {
        return baseMapper.selectByOrderNo(orderNo);
    }

    @Override
    @Transactional
    public boolean createBorrowOrder(BorrowOrder borrowOrder) {
        Borrower borrower = borrowerMapper.selectById(borrowOrder.getBorrowerId());
        if (borrower == null) {
            throw new BusinessException("借阅人员不存在");
        }
        
        BookInfo book = bookInfoMapper.selectById(borrowOrder.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        
        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessException("图书库存不足");
        }
        
        BigDecimal depositAmount = borrowOrder.getDepositAmount() != null ? borrowOrder.getDepositAmount() : BigDecimal.ZERO;
        if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (borrower.getBalance() == null || borrower.getBalance().compareTo(depositAmount) < 0) {
                throw new BusinessException("借阅人押金余额不足，请先充值");
            }
        }
        
        String orderNo = "ORD" + System.currentTimeMillis();
        borrowOrder.setOrderNo(orderNo);
        borrowOrder.setBorrowDate(LocalDate.now());
        borrowOrder.setDueDate(LocalDate.now().plusDays(borrowOrder.getBorrowDays() != null ? borrowOrder.getBorrowDays() : 30));
        borrowOrder.setStatus(0);
        borrowOrder.setDepositStatus(0);
        borrowOrder.setPaymentStatus(1);
        borrowOrder.setOverdueDays(0);
        
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookInfoMapper.updateById(book);
        
        boolean success = this.save(borrowOrder);
        
        if (success && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            String operator = borrowOrder.getOperator() != null ? borrowOrder.getOperator() : "admin";
            String remark = "借阅图书扣除押金: " + book.getTitle();
            depositDetailService.deductDeposit(borrower.getId(), depositAmount, orderNo, operator, remark);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean returnBook(Long orderId) {
        BorrowOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() == 1) {
            throw new BusinessException("该订单已归还");
        }
        
        BookInfo book = bookInfoMapper.selectById(order.getBookId());
        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            bookInfoMapper.updateById(book);
        }
        
        LocalDate returnDate = LocalDate.now();
        order.setReturnDate(returnDate);
        order.setStatus(1);
        
        long overdueDays = ChronoUnit.DAYS.between(order.getDueDate(), returnDate);
        if (overdueDays > 0) {
            order.setOverdueDays((int) overdueDays);
            order.setOverdueFine(new java.math.BigDecimal(overdueDays).multiply(new java.math.BigDecimal("0.5")));
        }
        
        borrowReminderService.cancelReminder(orderId);
        
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean refundDeposit(Long orderId) {
        BorrowOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getDepositStatus() == 1) {
            throw new BusinessException("该订单押金已退还");
        }
        
        if (order.getDepositStatus() == 2) {
            throw new BusinessException("该订单押金已扣除");
        }
        
        BigDecimal depositAmount = order.getDepositAmount() != null ? order.getDepositAmount() : BigDecimal.ZERO;
        
        order.setDepositStatus(1);
        boolean success = this.updateById(order);
        
        if (success && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            String operator = order.getOperator() != null ? order.getOperator() : "admin";
            String remark = "图书归还退还押金，订单号: " + order.getOrderNo();
            depositDetailService.refundDeposit(order.getBorrowerId(), depositAmount, 1, operator, remark);
        }
        
        return success;
    }
    
    private void fillRelatedInfo(List<BorrowOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        
        List<Long> borrowerIds = orders.stream()
                .map(BorrowOrder::getBorrowerId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> bookIds = orders.stream()
                .map(BorrowOrder::getBookId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Borrower> borrowers = borrowerMapper.selectBatchIds(borrowerIds);
        Map<Long, Borrower> borrowerMap = borrowers.stream()
                .collect(Collectors.toMap(Borrower::getId, b -> b));
        
        List<BookInfo> books = bookInfoMapper.selectBatchIds(bookIds);
        Map<Long, BookInfo> bookMap = books.stream()
                .collect(Collectors.toMap(BookInfo::getId, b -> b));
        
        for (BorrowOrder order : orders) {
            Borrower borrower = borrowerMap.get(order.getBorrowerId());
            if (borrower != null) {
                order.setBorrowerName(borrower.getName());
                order.setBorrowerPhone(borrower.getPhone());
            }
            
            BookInfo book = bookMap.get(order.getBookId());
            if (book != null) {
                order.setBookTitle(book.getTitle());
                order.setBookIsbn(book.getIsbn());
            }
        }
    }
}
