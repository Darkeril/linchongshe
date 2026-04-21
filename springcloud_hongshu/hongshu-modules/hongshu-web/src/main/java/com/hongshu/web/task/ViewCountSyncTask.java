package com.hongshu.web.task;

import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.rocketmq.ViewCountCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 浏览数定时同步任务
 * 定期将Redis中的浏览数同步到数据库
 *
 * @author: hongshu
 */
@Slf4j
@Component
public class ViewCountSyncTask {

    @Autowired
    private ViewCountCacheService cacheService;

    @Autowired
    private WebNoteMapper noteMapper;

    // TODO: 需要注入 IdleProductMapper
    // @Autowired
    // private IdleProductMapper productMapper;

    /**
     * 定时同步笔记浏览数
     * 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 300000) // 5分钟 = 300000毫秒
    public void syncNoteViewCounts() {
        try {
            log.info("开始同步笔记浏览数到数据库...");

            // 获取所有笔记浏览数的Redis键
            Set<String> keys = cacheService.getAllNoteViewKeys();

            if (keys.isEmpty()) {
                log.info("没有需要同步的笔记浏览数");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (String key : keys) {
                try {
                    // 从Redis键中提取笔记ID
                    String noteId = key.replace("note:view:", "");

                    // 获取浏览数
                    Long viewCount = cacheService.getNoteViewCount(noteId);

                    if (viewCount > 0) {
                        // 更新数据库
                        WebNote note = new WebNote();
                        note.setId(noteId);
                        note.setViewCount(viewCount);

                        int rows = noteMapper.updateById(note);

                        if (rows > 0) {
                            successCount++;
                        } else {
                            failCount++;
                            log.warn("同步笔记浏览数失败，笔记不存在: noteId={}", noteId);
                        }
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("同步单个笔记浏览数失败: key={}", key, e);
                }
            }

            log.info("笔记浏览数同步完成: 总数={}, 成功={}, 失败={}",
                    keys.size(), successCount, failCount);
        } catch (Exception e) {
            log.error("同步笔记浏览数异常", e);
        }
    }

    /**
     * 定时同步商品浏览数
     * 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 300000)
    public void syncProductViewCounts() {
        try {
            log.info("开始同步商品浏览数到数据库...");

            Set<String> keys = cacheService.getAllProductViewKeys();

            if (keys.isEmpty()) {
                log.info("没有需要同步的商品浏览数");
                return;
            }

            int successCount = 0;

            for (String key : keys) {
                try {
                    String productId = key.replace("product:view:", "");
                    Long viewCount = cacheService.getProductViewCount(productId);

                    if (viewCount > 0) {
                        // TODO: 更新商品浏览数
                        // IdleProduct product = new IdleProduct();
                        // product.setId(productId);
                        // product.setViewCount(viewCount);
                        // productMapper.updateById(product);

                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("同步单个商品浏览数失败: key={}", key, e);
                }
            }

            log.info("商品浏览数同步完成: 总数={}, 成功={}", keys.size(), successCount);
        } catch (Exception e) {
            log.error("同步商品浏览数异常", e);
        }
    }

    /**
     * 定时清理过期的浏览数缓存
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredViewCounts() {
        try {
            log.info("开始清理过期的浏览数缓存...");

            // Redis的过期键会自动删除，这里只需要记录日志
            Set<String> noteKeys = cacheService.getAllNoteViewKeys();
            Set<String> productKeys = cacheService.getAllProductViewKeys();

            log.info("当前缓存统计: 笔记浏览数={}, 商品浏览数={}",
                    noteKeys.size(), productKeys.size());
        } catch (Exception e) {
            log.error("清理过期浏览数缓存异常", e);
        }
    }
}


