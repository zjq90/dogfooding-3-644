package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.SmsSendRecord;
import com.library.mapper.SmsSendRecordMapper;
import com.library.service.SmsSendRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SmsSendRecordServiceImpl extends ServiceImpl<SmsSendRecordMapper, SmsSendRecord> implements SmsSendRecordService {

    @Override
    public PageResult<SmsSendRecord> getRecordPage(Integer page, Integer size, String phone, Integer sendStatus) {
        Page<SmsSendRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SmsSendRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(phone)) {
            wrapper.like(SmsSendRecord::getPhone, phone);
        }
        
        if (sendStatus != null) {
            wrapper.eq(SmsSendRecord::getSendStatus, sendStatus);
        }
        
        wrapper.orderByDesc(SmsSendRecord::getCreateTime);
        Page<SmsSendRecord> recordPage = this.page(pageParam, wrapper);
        
        return new PageResult<>(recordPage.getTotal(), recordPage.getRecords(), 
                                recordPage.getCurrent(), recordPage.getSize());
    }

    @Override
    public List<SmsSendRecord> getRecordsByBizId(String bizId) {
        LambdaQueryWrapper<SmsSendRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsSendRecord::getBizId, bizId);
        wrapper.orderByDesc(SmsSendRecord::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean createRecord(SmsSendRecord record) {
        return this.save(record);
    }

    @Override
    public boolean updateSendStatus(Long id, Integer status, String failReason) {
        SmsSendRecord record = this.getById(id);
        if (record != null) {
            record.setSendStatus(status);
            record.setSendTime(LocalDateTime.now());
            if (StringUtils.hasText(failReason)) {
                record.setFailReason(failReason);
            }
            return this.updateById(record);
        }
        return false;
    }
}
