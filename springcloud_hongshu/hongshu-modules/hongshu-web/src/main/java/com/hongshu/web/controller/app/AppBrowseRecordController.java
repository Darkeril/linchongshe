package com.hongshu.web.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.dto.BrowseRecordDTO;
import com.hongshu.web.service.web.IWebBrowseRecordService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 浏览记录
 *

 */
@RestController
@RequestMapping("/app/browseRecord")
@Schema(name = "浏览记录")
public class AppBrowseRecordController {

    @Autowired
    private IWebBrowseRecordService browseRecordService;


    /**
     * 获取浏览记录
     */
    @RequestMapping("getAllBrowseRecordByUser/{page}/{limit}")
    public Result<?> getAllBrowseRecordByUser(@PathVariable("page") long page, @PathVariable("limit") long limit) {
        Page<NoteSearchVo> browseRecordVoList = browseRecordService.getAllBrowseRecordByUser(page, limit);
        return Result.ok(browseRecordVoList);
    }

    /**
     * 添加浏览记录
     */
    @RequestMapping("addBrowseRecord")
    public Result<?> addBrowseRecord(@RequestBody BrowseRecordDTO browseRecordDTO) {
        browseRecordService.addBrowseRecord(browseRecordDTO);
        return Result.ok(null);
    }

    /**
     * 删除浏览记录
     */
    @RequestMapping("delRecord/{uid}")
    public Result<?> delRecord(@RequestBody List<String> idList, @PathVariable String uid) {
        browseRecordService.delRecord(uid, idList);
        return Result.ok(null);
    }
}
