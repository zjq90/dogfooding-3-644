package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.ReturnReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReturnReminderMapper extends BaseMapper<ReturnReminder> {
    
    @Select("SELECT r.*, b.name as borrower_name, b.phone as borrower_phone, " +
            "bk.title as book_title, bk.isbn as book_isbn, bo.order_no, bo.due_date " +
            "FROM return_reminder r " +
            "LEFT JOIN borrower b ON r.borrower_id = b.id " +
            "LEFT JOIN book_info bk ON r.book_id = bk.id " +
            "LEFT JOIN borrow_order bo ON r.order_id = bo.id " +
            "WHERE r.id = #{id} AND r.deleted = 0")
    ReturnReminder selectByIdWithInfo(@Param("id") Long id);
    
    @Select("SELECT r.*, b.name as borrower_name, b.phone as borrower_phone, " +
            "bk.title as book_title, bk.isbn as book_isbn, bo.order_no, bo.due_date " +
            "FROM return_reminder r " +
            "LEFT JOIN borrower b ON r.borrower_id = b.id " +
            "LEFT JOIN book_info bk ON r.book_id = bk.id " +
            "LEFT JOIN borrow_order bo ON r.order_id = bo.id " +
            "WHERE r.order_id = #{orderId} AND r.deleted = 0 " +
            "ORDER BY r.reminder_count DESC LIMIT 1")
    ReturnReminder selectLatestByOrderId(@Param("orderId") Long orderId);
    
    @Select("SELECT r.*, b.name as borrower_name, b.phone as borrower_phone, " +
            "bk.title as book_title, bk.isbn as book_isbn, bo.order_no, bo.due_date " +
            "FROM return_reminder r " +
            "LEFT JOIN borrower b ON r.borrower_id = b.id " +
            "LEFT JOIN book_info bk ON r.book_id = bk.id " +
            "LEFT JOIN borrow_order bo ON r.order_id = bo.id " +
            "WHERE r.status = #{status} AND r.deleted = 0")
    List<ReturnReminder> selectListByStatus(@Param("status") Integer status);
    
    @Select("SELECT r.*, b.name as borrower_name, b.phone as borrower_phone, " +
            "bk.title as book_title, bk.isbn as book_isbn, bo.order_no, bo.due_date " +
            "FROM return_reminder r " +
            "LEFT JOIN borrower b ON r.borrower_id = b.id " +
            "LEFT JOIN book_info bk ON r.book_id = bk.id " +
            "LEFT JOIN borrow_order bo ON r.order_id = bo.id " +
            "WHERE r.reminder_date = #{date} AND r.status = 0 AND r.deleted = 0")
    List<ReturnReminder> selectTodayReminders(@Param("date") LocalDate date);
    
    @Select("SELECT COUNT(*) FROM return_reminder WHERE order_id = #{orderId} AND status = 0 AND deleted = 0")
    int countActiveByOrderId(@Param("orderId") Long orderId);
}
