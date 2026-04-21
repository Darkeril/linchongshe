package com.hongshu.web.service.web;

import com.hongshu.web.domain.dto.NoteBehaviorReportDTO;
import com.hongshu.web.domain.entity.WebNoteBehavior;

import java.util.List;

/**
 * 笔记行为记录 Service（曝光/观看/观看时长）
 */
public interface IWebNoteBehaviorService {

    /**
     * 上报单条或批量行为（exposure / view / watch）
     */
    void report(NoteBehaviorReportDTO dto);

    /**
     * 批量上报（如曝光列表）
     */
    void reportBatch(List<NoteBehaviorReportDTO> list);
}
