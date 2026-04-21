package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.dto.LikeOrCollectionDTO;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.service.web.IWebLikeOrCollectionService;
import com.hongshu.web.rocketmq.LikeCollectionCacheService;
import com.hongshu.web.rocketmq.LikeCollectionProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 点赞/收藏 V2版本（使用Redis+RocketMQ）
 *
 * @author: hongshu
 *
 * 注意：此版本是可选的优化版本
 * - 如果启用了 RocketMQ，将使用 Redis + RocketMQ 架构
 * - 如果未启用 RocketMQ，请使用原来的 WebLikeOrCollectionServiceImpl
 */
@Slf4j
@Service("webLikeOrCollectionServiceV2")
public class WebLikeOrCollectionServiceV2Impl {

    @Autowired
    private LikeCollectionCacheService cacheService;

    @Autowired(required = false)  // 不强制要求注入，允许为 null
    private LikeCollectionProducerService producerService;

    @Autowired
    private WebNoteMapper noteMapper;

    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;

    // V1版本Service - 用于降级
    @Autowired
    private IWebLikeOrCollectionService likeOrCollectionServiceV1;

    /**
     * 点赞或收藏（优化版）
     *
     * 流程：
     * 1. 先更新Redis缓存（快速响应）
     * 2. 发送MQ消息（异步持久化）
     * 3. 消费者处理：写MySQL、更新ES、发通知
     */
    public void likeOrCollectionByDTO(LikeOrCollectionDTO dto) {
        String currentUid = AuthContextHolder.getUserId();
        String itemId = dto.getLikeOrCollectionId();
        Integer type = dto.getType();

        // 判断是点赞还是收藏
        boolean isLike = (type == 1 || type == 2);
        boolean isCollect = (type == 3 || type == 4);

        // 1. 检查当前状态（优先从Redis读取）
        Boolean cachedStatus = null;
        if (isLike) {
            cachedStatus = cacheService.getLikeStatus(currentUid, itemId);
        } else if (isCollect) {
            cachedStatus = cacheService.getCollectStatus(currentUid, itemId);
        }

        // 如果Redis没有，从数据库查询并缓存
        if (cachedStatus == null) {
            cachedStatus = this.isLikeOrCollectionFromDb(dto);
            if (isLike) {
                cacheService.cacheLikeStatus(currentUid, itemId, cachedStatus);
            } else if (isCollect) {
                cacheService.cacheCollectStatus(currentUid, itemId, cachedStatus);
            }
        }

        // 2. 更新Redis缓存（快速响应用户）
        boolean newStatus = !cachedStatus;

        if (isLike) {
            // 更新点赞状态和计数
            cacheService.cacheLikeStatus(currentUid, itemId, newStatus);
            if (newStatus) {
                cacheService.incrLikeCount(itemId);
            } else {
                cacheService.decrLikeCount(itemId);
            }
        } else if (isCollect) {
            // 更新收藏状态和计数
            cacheService.cacheCollectStatus(currentUid, itemId, newStatus);
            if (newStatus) {
                cacheService.incrCollectCount(itemId);
            } else {
                cacheService.decrCollectCount(itemId);
            }
        }

        log.info("Redis更新成功: uid={}, itemId={}, type={}, newStatus={}",
            currentUid, itemId, type, newStatus);

        // 3. 发送MQ消息（异步持久化）+ 降级策略
        boolean mqSuccess = false;
        if (producerService != null) {
            try {
                // RocketMQ 已启用，尝试发送消息
                if (isLike) {
                    producerService.sendLikeMessage(currentUid, itemId, dto.getPublishUid(), type, newStatus);
                } else if (isCollect) {
                    producerService.sendCollectMessage(currentUid, itemId, dto.getPublishUid(), type, newStatus);
                }
                mqSuccess = true;
                log.info("✅ MQ消息发送成功，异步持久化中...");
            } catch (Exception e) {
                log.error("❌ MQ消息发送失败，降级到同步持久化", e);
                mqSuccess = false;
            }
        }

        // 4. 降级策略：MQ 失败时同步写入 MySQL
        if (!mqSuccess) {
            log.warn("⚠️ 降级策略：直接同步写入MySQL（绕过RocketMQ）");
            try {
                // 调用 V1 版本的同步方法
                likeOrCollectionServiceV1.likeOrCollectionByDTO(dto);
                log.info("✅ 降级成功：MySQL同步写入完成");
            } catch (Exception e) {
                log.error("❌ 降级失败：MySQL写入异常", e);
                // 至少 Redis 已经更新了，用户看到的状态是对的
            }
        }
    }

    /**
     * 检查是否点赞或收藏（优化版）
     * 优先从Redis读取，Redis没有则查数据库并缓存
     */
    public boolean isLikeOrCollection(LikeOrCollectionDTO dto) {
        String currentUid = AuthContextHolder.getUserId();
        String itemId = dto.getLikeOrCollectionId();
        Integer type = dto.getType();

        boolean isLike = (type == 1 || type == 2);

        // 1. 先从Redis获取
        Boolean cachedStatus = null;
        if (isLike) {
            cachedStatus = cacheService.getLikeStatus(currentUid, itemId);
        } else {
            cachedStatus = cacheService.getCollectStatus(currentUid, itemId);
        }

        // 2. Redis命中，直接返回
        if (cachedStatus != null) {
            log.debug("Redis缓存命中: uid={}, itemId={}, status={}", currentUid, itemId, cachedStatus);
            return cachedStatus;
        }

        // 3. Redis未命中，查询数据库
        boolean dbStatus = this.isLikeOrCollectionFromDb(dto);

        // 4. 写入Redis缓存
        if (isLike) {
            cacheService.cacheLikeStatus(currentUid, itemId, dbStatus);
        } else {
            cacheService.cacheCollectStatus(currentUid, itemId, dbStatus);
        }

        log.debug("从数据库查询并缓存: uid={}, itemId={}, status={}", currentUid, itemId, dbStatus);
        return dbStatus;
    }

    /**
     * 从数据库查询点赞/收藏状态
     */
    private boolean isLikeOrCollectionFromDb(LikeOrCollectionDTO dto) {
        String currentUid = AuthContextHolder.getUserId();
        long count = likeOrCollectionMapper.selectCount(new QueryWrapper<WebLikeOrCollection>()
                .eq("uid", currentUid)
                .eq("like_or_collection_id", dto.getLikeOrCollectionId())
                .eq("type", dto.getType()));
        return count > 0;
    }

    /**
     * 获取点赞数（优化版）
     */
    public Long getLikeCount(String itemId) {
        // 1. 先从Redis获取
        Long cachedCount = cacheService.getLikeCount(itemId);
        if (cachedCount != null && cachedCount > 0) {
            return cachedCount;
        }

        // 2. 从数据库获取并缓存
        WebNote note = noteMapper.selectById(itemId);
        if (note != null) {
            cacheService.setLikeCount(itemId, note.getLikeCount());
            return note.getLikeCount();
        }

        return 0L;
    }
}

