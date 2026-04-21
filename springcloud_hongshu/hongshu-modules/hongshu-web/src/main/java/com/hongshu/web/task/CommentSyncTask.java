package com.hongshu.web.task;

import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.rocketmq.CommentCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 评论数据定时同步任务
 * 定期同步评论数缓存到数据库
 *
 * @author: hongshu
 */
@Slf4j
@Component
public class CommentSyncTask {

    @Autowired
    private CommentCacheService cacheService;

    @Autowired
    private WebNoteMapper noteMapper;

    /**
     * 定时同步笔记评论数
     * 每15分钟执行一次
     */
    @Scheduled(fixedDelay = 900000) // 15分钟 = 900000毫秒
    public void syncNoteCommentCounts() {
        try {
            log.info("开始同步笔记评论数到数据库...");

            // 获取所有有评论的笔记
            List<WebNote> notes = noteMapper.selectList(null);

            int syncCount = 0;

            for (WebNote note : notes) {
                try {
                    String noteId = note.getId();

                    // 获取Redis中的评论数
                    Long cachedCount = cacheService.getNoteCommentCount(noteId);

                    // 如果Redis中有数据且与数据库不一致，则同步
                    if (cachedCount > 0 && !cachedCount.equals(note.getCommentCount())) {
                        WebNote updateNote = new WebNote();
                        updateNote.setId(noteId);
                        updateNote.setCommentCount(cachedCount);
//                        updateNote.setUpdateTime(new java.util.Date());

                        int rows = noteMapper.updateById(updateNote);

                        if (rows > 0) {
                            syncCount++;
                            log.debug("同步笔记评论数: noteId={}, cachedCount={}, dbCount={}",
                                    noteId, cachedCount, note.getCommentCount());
                        }
                    }
                } catch (Exception e) {
                    log.error("同步单个笔记评论数失败: noteId={}", note.getId(), e);
                }
            }

            log.info("笔记评论数同步完成: 同步数量={}", syncCount);
        } catch (Exception e) {
            log.error("同步笔记评论数异常", e);
        }
    }

    /**
     * 定时清理过期的评论缓存
     * 每天凌晨6点执行
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void cleanExpiredCommentCache() {
        try {
            log.info("开始清理过期的评论缓存...");

            // Redis的过期键会自动删除，这里只需要记录日志
            log.info("评论缓存清理完成");
        } catch (Exception e) {
            log.error("清理评论缓存失败", e);
        }
    }
}


