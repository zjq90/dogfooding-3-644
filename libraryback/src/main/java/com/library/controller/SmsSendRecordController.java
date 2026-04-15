package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.SmsSendRecord;
import com.library.service.SmsSendRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsSendRecordController {

    @Autowired
    private SmsSendRecordService smsSendRecordService;

    @GetMapping("/page")
    public Result<PageResult<SmsSendRecord>> getRecordPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer sendStatus) {
        
        PageResult<SmsSendRecord> result = smsSendRecordService.getRecordPage(page, size, phone, sendStatus);
        return Result.success(result);
    }

    @GetMapping("/biz/{bizId}")
    public Result<List<SmsSendRecord>> getRecordsByBizId(@PathVariable String bizId) {
        List<SmsSendRecord> records = smsSendRecordService.getRecordsByBizId(bizId);
        return Result.success(records);
    }
}
