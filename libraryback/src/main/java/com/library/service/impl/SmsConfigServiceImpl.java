package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.SmsConfig;
import com.library.mapper.SmsConfigMapper;
import com.library.service.SmsConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsConfigServiceImpl extends ServiceImpl<SmsConfigMapper, SmsConfig> implements SmsConfigService {

    @Override
    public SmsConfig getActiveConfig() {
        LambdaQueryWrapper<SmsConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateConfig(SmsConfig config) {
        return this.updateById(config);
    }
}
