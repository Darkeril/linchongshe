package com.hongshu.web.controller.web;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.dto.EsRecordDTO;
import com.hongshu.web.service.web.IWebEsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ES
 *
 * @author: hongshu
 */
@RequestMapping("/es/record")
@RestController
public class WebEsRecordController {

    @Autowired
    private IWebEsRecordService esRecordService;

    /**
     * 获取搜索记录
     */
    @GetMapping("getRecordByKeyWord")
    public Result<?> getRecordByKeyWord(EsRecordDTO esRecordDTO) {
        return Result.ok(esRecordService.getRecordByKeyWord(esRecordDTO));
    }

    /**
     * 热门关键词
     */
    @NoLoginIntercept
    @GetMapping("getHotRecord")
    public Result<?> getHotRecord() {
        return Result.ok(esRecordService.getHotRecord());
    }

    /**
     * 增加搜索记录
     */
    @PostMapping("addRecord")
    public Result<?> addRecord(@RequestBody EsRecordDTO esRecordDTO) {
        esRecordService.addRecord(esRecordDTO);
        return Result.ok();
    }

    /**
     * 清空搜索记录
     */
    @PostMapping("clearAllRecord")
    public Result<?> clearAllRecord() {
        esRecordService.clearAllRecord();
        return Result.ok();
    }
}
