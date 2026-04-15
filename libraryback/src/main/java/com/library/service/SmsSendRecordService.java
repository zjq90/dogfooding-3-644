package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.SmsSendRecord;
import java.util.List;

public interface SmsSendRecordService extends IService<SmsSendRecord> {
    
    PageResult<SmsSendRecord> getRecordPage(Integer page, Integer size, String phone, Integer sendStatus);
    
    List<SmsSendRecord> getRecordsByBizId(String bizId);
    
    boolean createRecord(SmsSendRecord record);
    
    boolean updateSendStatus(Long id, Integer status, String failReason);
}
