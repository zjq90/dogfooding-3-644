package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.SmsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SmsRecordMapper extends BaseMapper<SmsRecord> {
    
    @Select("SELECT * FROM sms_record WHERE phone = #{phone} AND deleted = 0 ORDER BY create_time DESC")
    List<SmsRecord> selectByPhone(@Param("phone") String phone);
    
    @Select("SELECT * FROM sms_record WHERE send_status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<SmsRecord> selectByStatus(@Param("status") Integer status);
    
    @Select("SELECT COUNT(*) FROM sms_record WHERE phone = #{phone} AND send_status = 2 AND deleted = 0")
    int countSuccessByPhone(@Param("phone") String phone);
}
