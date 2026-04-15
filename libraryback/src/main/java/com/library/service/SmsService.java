package com.library.service;

import com.library.entity.SmsSendRecord;
import java.util.Map;

public interface SmsService {
    
    boolean sendSms(String phone, String templateCode, Map<String, String> params, String bizId, String bizType);
    
    boolean sendReminderSms(String phone, String borrowerName, String bookTitle, String orderNo);
    
    boolean sendDepositDeductSms(String phone, String borrowerName, String bookTitle, java.math.BigDecimal amount);
}
