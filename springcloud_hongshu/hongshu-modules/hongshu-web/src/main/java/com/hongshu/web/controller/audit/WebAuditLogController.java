package com.hongshu.web.controller.audit;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.web.domain.dto.AuditLogQueryDTO;
import com.hongshu.web.domain.entity.WebAuditLog;
import com.hongshu.web.service.audit.IWebAuditLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核日志管理
 *
 * @author hongshu
 */
@Slf4j
@RestController
@RequestMapping("/auditLog")
@Tag(name = "审核日志管理")
public class WebAuditLogController extends BaseController {

    @Autowired
    private IWebAuditLogQueryService auditLogQueryService;

    /**
     * 分页查询审核日志
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询审核日志")
    public TableDataInfo list(@RequestBody AuditLogQueryDTO queryDTO) {
        startPage();
        List<WebAuditLog> list = auditLogQueryService.queryAuditLogs(queryDTO);
        return getDataTable(list);
    }

    /**
     * 查询审核日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询审核日志详情")
    public AjaxResult getInfo(@PathVariable String id) {
        return success(auditLogQueryService.getAuditLogById(id));
    }
}
