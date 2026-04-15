package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BorrowOrderMapper extends BaseMapper<BorrowOrder> {
    
    @Select("SELECT * FROM borrow_order WHERE order_no = #{orderNo} AND deleted = 0")
    BorrowOrder selectByOrderNo(@Param("orderNo") String orderNo);
    
    @Select("SELECT * FROM borrow_order WHERE status = 0 AND due_date < #{date} AND deleted = 0")
    List<BorrowOrder> selectOverdueOrders(@Param("date") LocalDate date);
    
    @Select("SELECT * FROM borrow_order WHERE status = 0 AND due_date = #{date} AND deleted = 0")
    List<BorrowOrder> selectOrdersByDueDate(@Param("date") LocalDate date);
}
