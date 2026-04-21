package com.hongshu.web.service.web.impl;

import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.web.domain.dto.NoteBehaviorReportDTO;
import com.hongshu.web.domain.entity.WebNoteBehavior;
import com.hongshu.web.mapper.web.WebNoteBehaviorMapper;
import com.hongshu.web.service.web.IWebNoteBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记行为记录 Service 实现
 */
@Slf4j
@Service
public class WebNoteBehaviorServiceImpl implements IWebNoteBehaviorService {

    private static final List<String> VALID_EVENT_TYPES = List.of("exposure", "view", "watch", "share");

    @Autowired
    private WebNoteBehaviorMapper behaviorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(NoteBehaviorReportDTO dto) {
        if (dto == null || ValidatorUtil.isNull(dto.getNid()) || ValidatorUtil.isNull(dto.getEventType())) {
            return;
        }
        if (!VALID_EVENT_TYPES.contains(dto.getEventType())) {
            log.warn("无效的 eventType: {}", dto.getEventType());
            return;
        }
        String uid = ValidatorUtil.isNotNull(dto.getUid()) ? dto.getUid() : resolveUid();
        WebNoteBehavior entity = toEntity(dto, uid);
        if (entity != null) {
            behaviorMapper.insert(entity);
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void reportBatch(List<NoteBehaviorReportDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String uid = resolveUid();
        List<WebNoteBehavior> entities = new ArrayList<>();
        for (NoteBehaviorReportDTO dto : list) {
            if (ValidatorUtil.isNull(dto.getNid()) || ValidatorUtil.isNull(dto.getEventType())) {
                continue;
            }
            if (!VALID_EVENT_TYPES.contains(dto.getEventType())) {
                continue;
            }
            String dtoUid = ValidatorUtil.isNotNull(dto.getUid()) ? dto.getUid() : uid;
            WebNoteBehavior entity = toEntity(dto, dtoUid);
            if (entity != null) {
                entities.add(entity);
            }
        }
        for (WebNoteBehavior e : entities) {
            behaviorMapper.insert(e);
        }
    }

    private WebNoteBehavior toEntity(NoteBehaviorReportDTO dto, String uid) {
        WebNoteBehavior e = new WebNoteBehavior();
        e.setEventType(dto.getEventType());
        e.setNid(dto.getNid());
        e.setUid(uid);
        e.setScene(dto.getScene() != null ? dto.getScene() : "other");
        e.setDurationSec(dto.getDurationSec() != null ? dto.getDurationSec() : 0);
        e.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : 0);
        e.setCreateTime(new Date());
        e.setUpdateTime(new Date());
        return e;
    }

    private String resolveUid() {
        try {
            return AuthContextHolder.getUserId();
        } catch (Exception ex) {
            return null;
        }
    }
}
