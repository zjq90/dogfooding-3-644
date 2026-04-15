package com.library.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.SmsRecord;
import com.library.mapper.SmsRecordMapper;
import com.library.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class SmsServiceImpl extends ServiceImpl<SmsRecordMapper, SmsRecord> implements SmsService {

    private static final String SMS_TEMPLATE_CODE = "SMS_RETURN_REMINDER";

    @Override
    public boolean sendReturnReminderSms(String phone, String borrowerName, String bookTitle, int overdueDays) {
        Map<String, String> templateParam = new HashMap<>();
        templateParam.put("name", borrowerName);
        templateParam.put("book", bookTitle);
        templateParam.put("days", String.valueOf(overdueDays));
        
        String content = buildSmsContent(borrowerName, bookTitle, overdueDays);
        
        return sendSms(phone, SMS_TEMPLATE_CODE, templateParam, content);
    }
    
    private String buildSmsContent(String borrowerName, String bookTitle, int overdueDays) {
        StringBuilder content = new StringBuilder();
        content.append("【图书馆】尊敬的").append(borrowerName).append("，");
        content.append("您借阅的《").append(bookTitle).append("》");
        
        if (overdueDays > 0) {
            content.append("已逾期").append(overdueDays).append("天，请尽快归还。");
            if (overdueDays >= 2) {
                content.append("逾期3天将自动扣除押金，请及时处理。");
            }
        } else {
            content.append("即将到期，请按时归还。");
        }
        
        return content.toString();
    }

    @Override
    public boolean sendSms(String phone, String templateCode, Map<String, String> templateParam, String content) {
        SmsRecord record = new SmsRecord();
        record.setPhone(phone);
        record.setTemplateCode(templateCode);
        record.setTemplateParam(JSON.toJSONString(templateParam));
        record.setContent(content);
        record.setSendStatus(1);
        record.setRequestId(UUID.randomUUID().toString());
        
        this.save(record);
        
        try {
            boolean success = doSendSms(phone, content);
            
            if (success) {
                record.setSendStatus(2);
                record.setResponseCode("OK");
                record.setResponseMessage("发送成功");
                record.setSendTime(LocalDateTime.now());
                log.info("短信发送成功: phone={}, content={}", phone, content);
            } else {
                record.setSendStatus(3);
                record.setResponseCode("FAIL");
                record.setResponseMessage("发送失败");
                log.error("短信发送失败: phone={}", phone);
            }
            
            this.updateById(record);
            return success;
            
        } catch (Exception e) {
            record.setSendStatus(3);
            record.setResponseCode("ERROR");
            record.setResponseMessage(e.getMessage());
            this.updateById(record);
            log.error("短信发送异常: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean doSendSms(String phone, String content) {
        log.info("调用短信服务商接口: phone={}, content={}", phone, content);
        
        try {
            Thread.sleep(100);
            
            return true;
        } catch (Exception e) {
            log.error("短信接口调用异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<SmsRecord> getRecordsByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }

    @Override
    public List<SmsRecord> getPendingRecords() {
        return baseMapper.selectByStatus(0);
    }

    @Override
    public boolean retrySendSms(Long recordId) {
        SmsRecord record = this.getById(recordId);
        if (record == null) {
            return false;
        }
        
        return sendSms(record.getPhone(), record.getTemplateCode(), 
                      JSON.parseObject(record.getTemplateParam(), Map.class), 
                      record.getContent());
    }
}
