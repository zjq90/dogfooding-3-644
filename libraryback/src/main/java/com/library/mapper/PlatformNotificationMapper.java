package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.PlatformNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlatformNotificationMapper extends BaseMapper<PlatformNotification> {
    
    @Select("SELECT n.*, bo.order_no, b.name as borrower_name " +
            "FROM platform_notification n " +
            "LEFT JOIN borrow_order bo ON n.related_order_id = bo.id " +
            "LEFT JOIN borrower b ON bo.borrower_id = b.id " +
            "WHERE n.id = #{id} AND n.deleted = 0")
    PlatformNotification selectByIdWithInfo(@Param("id") Long id);
    
    @Select("SELECT n.*, bo.order_no, b.name as borrower_name " +
            "FROM platform_notification n " +
            "LEFT JOIN borrow_order bo ON n.related_order_id = bo.id " +
            "LEFT JOIN borrower b ON bo.borrower_id = b.id " +
            "WHERE n.status = #{status} AND n.deleted = 0 " +
            "ORDER BY n.create_time DESC")
    List<PlatformNotification> selectListByStatus(@Param("status") Integer status);
    
    @Select("SELECT n.*, bo.order_no, b.name as borrower_name " +
            "FROM platform_notification n " +
            "LEFT JOIN borrow_order bo ON n.related_order_id = bo.id " +
            "LEFT JOIN borrower b ON bo.borrower_id = b.id " +
            "WHERE n.type = #{type} AND n.status = #{status} AND n.deleted = 0 " +
            "ORDER BY n.create_time DESC")
    List<PlatformNotification> selectByTypeAndStatus(@Param("type") Integer type, @Param("status") Integer status);
    
    @Select("SELECT COUNT(*) FROM platform_notification WHERE status = 0 AND deleted = 0")
    int countUnread();
    
    @Update("UPDATE platform_notification SET status = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);
    
    @Update("UPDATE platform_notification SET status = 1 WHERE status = 0")
    int markAllAsRead();
}
