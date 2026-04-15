package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowOrderMapper extends BaseMapper<BorrowOrder> {
    
    @Select("SELECT * FROM borrow_order WHERE order_no = #{orderNo} AND deleted = 0")
    BorrowOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
