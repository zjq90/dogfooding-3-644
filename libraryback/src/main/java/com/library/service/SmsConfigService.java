package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.SmsConfig;

public interface SmsConfigService extends IService<SmsConfig> {
    
    SmsConfig getActiveConfig();
    
    boolean updateConfig(SmsConfig config);
}
