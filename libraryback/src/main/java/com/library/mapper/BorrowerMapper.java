package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Borrower;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowerMapper extends BaseMapper<Borrower> {
    
    @Select("SELECT * FROM borrower WHERE borrower_no = #{borrowerNo} AND deleted = 0")
    Borrower selectByBorrowerNo(@Param("borrowerNo") String borrowerNo);
    
    @Select("SELECT * FROM borrower WHERE phone = #{phone} AND deleted = 0")
    Borrower selectByPhone(@Param("phone") String phone);
}
