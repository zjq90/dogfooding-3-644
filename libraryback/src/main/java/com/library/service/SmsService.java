package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.SmsRecord;

import java.util.List;
import java.util.Map;

public interface SmsService extends IService<SmsRecord> {
    
    boolean sendReturnReminderSms(String phone, String borrowerName, String bookTitle, int overdueDays);
    
    boolean sendSms(String phone, String templateCode, Map<String, String> templateParam, String content);
    
    List<SmsRecord> getRecordsByPhone(String phone);
    
    List<SmsRecord> getPendingRecords();
    
    boolean retrySendSms(Long recordId);
}
