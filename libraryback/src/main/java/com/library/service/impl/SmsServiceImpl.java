package com.library.service.impl;

import com.alibaba.fastjson.JSON;
import com.library.entity.SmsConfig;
import com.library.entity.SmsSendRecord;
import com.library.service.SmsConfigService;
import com.library.service.SmsSendRecordService;
import com.library.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SmsConfigService smsConfigService;
    
    @Autowired
    private SmsSendRecordService smsSendRecordService;

    @Override
    public boolean sendSms(String phone, String templateCode, Map<String, String> params, String bizId, String bizType) {
        log.info("发送短信: phone={}, templateCode={}, params={}", phone, templateCode, params);
        
        SmsConfig config = smsConfigService.getActiveConfig();
        if (config == null) {
            log.warn("未找到有效的短信配置");
            return simulateSendSms(phone, templateCode, params, bizId, bizType, "模拟短信服务商");
        }
        
        SmsSendRecord record = new SmsSendRecord();
        record.setPhone(phone);
        record.setTemplateCode(templateCode != null ? templateCode : config.getTemplateCode());
        record.setParams(params != null ? JSON.toJSONString(params) : null);
        record.setSendStatus(1);
        record.setProvider(config.getProvider());
        record.setBizId(bizId);
        record.setBizType(bizType);
        
        String content = buildSmsContent(config.getSignName(), params);
        record.setContent(content);
        
        try {
            boolean success = callSmsProvider(config, phone, templateCode, params);
            
            if (success) {
                record.setSendStatus(2);
                record.setSendTime(LocalDateTime.now());
                log.info("短信发送成功: phone={}", phone);
            } else {
                record.setSendStatus(3);
                record.setFailReason("短信服务商返回失败");
                log.warn("短信发送失败: phone={}", phone);
            }
        } catch (Exception e) {
            record.setSendStatus(3);
            record.setFailReason(e.getMessage());
            log.error("短信发送异常: phone={}, error={}", phone, e.getMessage());
        }
        
        smsSendRecordService.save(record);
        return record.getSendStatus() == 2;
    }

    @Override
    public boolean sendReminderSms(String phone, String borrowerName, String bookTitle, String orderNo) {
        Map<String, String> params = new HashMap<>();
        params.put("name", borrowerName);
        params.put("book", bookTitle);
        
        String content = String.format("【图书管理系统】%s您好，您借阅的《%s》已逾期，请尽快归还。如有疑问请联系管理员。", 
                borrowerName, bookTitle);
        
        SmsSendRecord record = new SmsSendRecord();
        record.setPhone(phone);
        record.setContent(content);
        record.setParams(JSON.toJSONString(params));
        record.setSendStatus(1);
        record.setProvider("simulation");
        record.setBizId(orderNo);
        record.setBizType("borrow_reminder");
        
        boolean success = simulateSend(phone, content);
        
        if (success) {
            record.setSendStatus(2);
            record.setSendTime(LocalDateTime.now());
        } else {
            record.setSendStatus(3);
            record.setFailReason("模拟发送失败");
        }
        
        smsSendRecordService.save(record);
        return success;
    }

    @Override
    public boolean sendDepositDeductSms(String phone, String borrowerName, String bookTitle, java.math.BigDecimal amount) {
        Map<String, String> params = new HashMap<>();
        params.put("name", borrowerName);
        params.put("book", bookTitle);
        params.put("amount", amount.toString());
        
        String content = String.format("【图书管理系统】%s您好，您借阅的《%s》因逾期未归还，系统已自动扣除押金%.2f元。如有疑问请联系管理员。", 
                borrowerName, bookTitle, amount);
        
        SmsSendRecord record = new SmsSendRecord();
        record.setPhone(phone);
        record.setContent(content);
        record.setParams(JSON.toJSONString(params));
        record.setSendStatus(1);
        record.setProvider("simulation");
        record.setBizType("deposit_deduct");
        
        boolean success = simulateSend(phone, content);
        
        if (success) {
            record.setSendStatus(2);
            record.setSendTime(LocalDateTime.now());
        } else {
            record.setSendStatus(3);
            record.setFailReason("模拟发送失败");
        }
        
        smsSendRecordService.save(record);
        return success;
    }
    
    private boolean simulateSendSms(String phone, String templateCode, Map<String, String> params, 
                                    String bizId, String bizType, String provider) {
        String content = buildSmsContent("图书管理系统", params);
        
        SmsSendRecord record = new SmsSendRecord();
        record.setPhone(phone);
        record.setTemplateCode(templateCode);
        record.setContent(content);
        record.setParams(params != null ? JSON.toJSONString(params) : null);
        record.setSendStatus(1);
        record.setProvider(provider);
        record.setBizId(bizId);
        record.setBizType(bizType);
        
        boolean success = simulateSend(phone, content);
        
        if (success) {
            record.setSendStatus(2);
            record.setSendTime(LocalDateTime.now());
        } else {
            record.setSendStatus(3);
            record.setFailReason("模拟发送失败");
        }
        
        smsSendRecordService.save(record);
        return success;
    }
    
    private boolean callSmsProvider(SmsConfig config, String phone, String templateCode, Map<String, String> params) {
        log.info("调用短信服务商API: provider={}, phone={}", config.getProvider(), phone);
        return simulateSend(phone, buildSmsContent(config.getSignName(), params));
    }
    
    private boolean simulateSend(String phone, String content) {
        log.info("=== 模拟短信发送 ===");
        log.info("接收号码: {}", phone);
        log.info("短信内容: {}", content);
        log.info("发送时间: {}", LocalDateTime.now());
        log.info("发送状态: 成功");
        log.info("==================");
        return true;
    }
    
    private String buildSmsContent(String signName, Map<String, String> params) {
        StringBuilder content = new StringBuilder();
        content.append("【").append(signName != null ? signName : "图书管理系统").append("】");
        
        if (params != null) {
            String name = params.get("name");
            String book = params.get("book");
            String amount = params.get("amount");
            
            if (name != null && book != null) {
                if (amount != null) {
                    content.append(name).append("您好，您借阅的《").append(book)
                           .append("》因逾期未归还，系统已自动扣除押金").append(amount).append("元。");
                } else {
                    content.append(name).append("您好，您借阅的《").append(book)
                           .append("》已逾期，请尽快归还。");
                }
            }
        }
        
        return content.toString();
    }
}
