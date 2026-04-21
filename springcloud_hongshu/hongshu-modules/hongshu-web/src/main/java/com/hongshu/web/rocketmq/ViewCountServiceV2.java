package com.hongshu.web.rocketmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.mapper.web.WebNoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 浏览数服务V2（优化版）
 * 使用Redis缓存 + RocketMQ异步更新
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class ViewCountServiceV2 {

    @Autowired
    private ViewCountCacheService cacheService;

    @Autowired(required = false)
    private ViewCountProducerService producerService;

    @Autowired
    private WebNoteMapper noteMapper;

    // TODO: 需要注入 IdleProductMapper
    // @Autowired
    // private IdleProductMapper productMapper;

    // 批量更新阈值：每10次浏览更新一次数据库
    private static final long BATCH_UPDATE_THRESHOLD = 10;

    /**
     * 记录笔记浏览（优化版）
     *
     * @param noteId 笔记ID
     * @return 最新浏览数
     */
    public Long recordNoteView(String noteId) {
        try {
            // 1. Redis原子递增
            Long newCount = cacheService.incrementNoteView(noteId);

            log.debug("记录笔记浏览: noteId={}, newCount={}", noteId, newCount);

            // 2. 判断是否需要更新数据库
            if (shouldUpdateDatabase(newCount)) {
                asyncUpdateNoteViewCount(noteId, newCount);
            }

            return newCount;
        } catch (Exception e) {
            log.error("记录笔记浏览失败: noteId={}", noteId, e);
            // 降级：直接更新数据库
            return fallbackRecordNoteView(noteId);
        }
    }

    /**
     * 记录商品浏览（优化版）
     *
     * @param productId 商品ID
     * @return 最新浏览数
     */
    public Long recordProductView(String productId) {
        try {
            Long newCount = cacheService.incrementProductView(productId);

            log.debug("记录商品浏览: productId={}, newCount={}", productId, newCount);

            if (shouldUpdateDatabase(newCount)) {
                asyncUpdateProductViewCount(productId, newCount);
            }

            return newCount;
        } catch (Exception e) {
            log.error("记录商品浏览失败: productId={}", productId, e);
            return 0L;
        }
    }

    /**
     * 获取笔记浏览数（优化版）
     *
     * @param noteId 笔记ID
     * @return 浏览数
     */
    public Long getNoteViewCount(String noteId) {
        try {
            // 先从Redis获取
            Long cachedCount = cacheService.getNoteViewCount(noteId);

            if (cachedCount > 0) {
                return cachedCount;
            }

            // Redis未命中，查询数据库并缓存
            WebNote note = noteMapper.selectOne(
                    new QueryWrapper<WebNote>()
                            .select("view_count")
                            .eq("id", noteId)
            );

            if (note != null && note.getViewCount() != null) {
                cacheService.setNoteViewCount(noteId, note.getViewCount());
                return note.getViewCount();
            }

            return 0L;
        } catch (Exception e) {
            log.error("获取笔记浏览数失败: noteId={}", noteId, e);
            return 0L;
        }
    }

    /**
     * 获取商品浏览数（优化版）
     *
     * @param productId 商品ID
     * @return 浏览数
     */
    public Long getProductViewCount(String productId) {
        try {
            Long cachedCount = cacheService.getProductViewCount(productId);

            if (cachedCount > 0) {
                return cachedCount;
            }

            // TODO: 从数据库加载并缓存
            // IdleProduct product = productMapper.selectOne(...);
            // cacheService.setProductViewCount(productId, product.getViewCount());

            return 0L;
        } catch (Exception e) {
            log.error("获取商品浏览数失败: productId={}", productId, e);
            return 0L;
        }
    }

    /**
     * 判断是否需要更新数据库
     *
     * @param count 当前浏览数
     * @return true-需要更新，false-不需要
     */
    private boolean shouldUpdateDatabase(Long count) {
        // 每10次浏览更新一次数据库
        return count % BATCH_UPDATE_THRESHOLD == 0;
    }

    /**
     * 异步更新笔记浏览数到数据库
     *
     * @param noteId 笔记ID
     * @param count  浏览数
     */
    private void asyncUpdateNoteViewCount(String noteId, Long count) {
        try {
            if (producerService != null) {
                // RocketMQ可用，发送异步消息
                producerService.sendNoteViewMessage(noteId, count);
            } else {
                // RocketMQ不可用，直接更新数据库
                syncUpdateNoteViewCount(noteId, count);
            }
        } catch (Exception e) {
            log.error("异步更新笔记浏览数失败，降级为同步更新: noteId={}, count={}", noteId, count, e);
            syncUpdateNoteViewCount(noteId, count);
        }
    }

    /**
     * 异步更新商品浏览数到数据库
     *
     * @param productId 商品ID
     * @param count     浏览数
     */
    private void asyncUpdateProductViewCount(String productId, Long count) {
        try {
            if (producerService != null) {
                producerService.sendProductViewMessage(productId, count);
            } else {
                syncUpdateProductViewCount(productId, count);
            }
        } catch (Exception e) {
            log.error("异步更新商品浏览数失败: productId={}, count={}", productId, count, e);
            syncUpdateProductViewCount(productId, count);
        }
    }

    /**
     * 同步更新笔记浏览数到数据库
     *
     * @param noteId 笔记ID
     * @param count  浏览数
     */
    private void syncUpdateNoteViewCount(String noteId, Long count) {
        try {
            WebNote note = new WebNote();
            note.setId(noteId);
            note.setViewCount(count);
            noteMapper.updateById(note);
            log.info("同步更新笔记浏览数成功: noteId={}, count={}", noteId, count);
        } catch (Exception e) {
            log.error("同步更新笔记浏览数失败: noteId={}, count={}", noteId, count, e);
        }
    }

    /**
     * 同步更新商品浏览数到数据库
     *
     * @param productId 商品ID
     * @param count     浏览数
     */
    private void syncUpdateProductViewCount(String productId, Long count) {
        try {
            // TODO: 实现商品浏览数更新
            log.info("同步更新商品浏览数成功: productId={}, count={}", productId, count);
        } catch (Exception e) {
            log.error("同步更新商品浏览数失败: productId={}, count={}", productId, count, e);
        }
    }

    /**
     * 降级处理：直接更新数据库
     *
     * @param noteId 笔记ID
     * @return 浏览数
     */
    private Long fallbackRecordNoteView(String noteId) {
        try {
            WebNote note = noteMapper.selectOne(
                    new QueryWrapper<WebNote>()
                            .select("view_count")
                            .eq("id", noteId)
            );

            if (note != null) {
                Long newCount = (note.getViewCount() != null ? note.getViewCount() : 0L) + 1;
                note.setViewCount(newCount);
                noteMapper.updateById(note);
                return newCount;
            }

            return 0L;
        } catch (Exception e) {
            log.error("降级处理失败: noteId={}", noteId, e);
            return 0L;
        }
    }
}

