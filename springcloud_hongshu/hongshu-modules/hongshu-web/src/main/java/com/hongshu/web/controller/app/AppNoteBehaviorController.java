package com.hongshu.web.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.dto.NoteBehaviorReportDTO;
import com.hongshu.web.service.web.IWebNoteBehaviorService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 笔记行为上报（曝光/观看来源/观看时段/时长完播）
 * 用户端埋点后调用此接口，数据用于管理端-数据中心-数据分析
 */
@RestController
@RequestMapping("/app/noteBehavior")
@Schema(name = "笔记行为上报")
public class AppNoteBehaviorController {

    @Autowired
    private IWebNoteBehaviorService noteBehaviorService;

    /**
     * 上报单条行为
     * body: { eventType, nid, uid?, scene?, durationSec?, completed? }
     */
    @PostMapping("report")
    public Result<?> report(@RequestBody NoteBehaviorReportDTO dto) {
        noteBehaviorService.report(dto);
        return Result.ok(null);
    }

    /**
     * 批量上报（如曝光列表）
     * body: { list: [ { eventType, nid, uid?, scene? }, ... ] }
     */
    @PostMapping("reportBatch")
    public Result<?> reportBatch(@RequestBody List<NoteBehaviorReportDTO> list) {
        noteBehaviorService.reportBatch(list);
        return Result.ok(null);
    }
}
