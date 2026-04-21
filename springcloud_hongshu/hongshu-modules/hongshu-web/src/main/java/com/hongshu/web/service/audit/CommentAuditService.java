package com.hongshu.web.service.audit;

import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import com.hongshu.common.audit.service.ContentAuditService;
import com.hongshu.web.domain.entity.WebAuditLog;
import com.hongshu.web.mapper.web.WebAuditLogMapper;
import com.hongshu.web.utils.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * 评论审核服务
 *
 * @author hongshu
 */
@Slf4j
@Service
public class CommentAuditService {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private UserReputationService userReputationService;

    @Autowired
    private ContentAuditService contentAuditService;

    @Autowired
    private WebAuditLogMapper auditLogMapper;

    /**
     * 审核评论内容（不保存日志，仅返回审核结果）
     *
     * @param userId    用户ID
     * @param commentId 评论ID（可为null，仅用于日志记录）
     * @param content   评论内容
     * @return 审核结果
     */
    public AutoAuditResult auditComment(String userId, String commentId, String content) {
        log.info("开始审核评论: userId={}, commentId={}, content={}", userId, commentId, content);

        try {
            // 1. 本地敏感词过滤（第一道防线）
            if (sensitiveWordFilter.containsSensitiveWord(content)) {
                Set<String> sensitiveWords = sensitiveWordFilter.getSensitiveWords(content);
                log.warn("评论包含敏感词: userId={}, commentId={}, words={}", userId, commentId, sensitiveWords);

                return AutoAuditResult.builder()
                        .result(AutoAuditResultEnum.REJECT)
                        .reason("评论内容包含不当信息，请修改后重试")
                        .score(0.0)
                        .provider("sensitive_word_filter")
                        .auditTime(new Date())
                        .build();
            }

            // 2. 获取用户信誉等级
            int reputation = userReputationService.getUserReputation(userId);
            String level = userReputationService.getUserReputationLevel(userId);
            log.info("用户信誉: userId={}, reputation={}, level={}", userId, reputation, level);

            // 3. 根据用户信誉决定审核策略
            if (reputation >= 80) {
                // 高信誉用户：异步审核（先发后审）
                log.info("高信誉用户，异步审核: userId={}, commentId={}", userId, commentId);
                // 注意：异步审核需要在评论插入后调用，传入真实的 commentId

                return AutoAuditResult.builder()
                        .result(AutoAuditResultEnum.PASS)
                        .reason("高信誉用户，先发后审")
                        .score(1.0)
                        .provider("user_reputation")
                        .auditTime(new Date())
                        .build();

            } else {
                // 普通/低信誉用户：同步审核
                log.info("普通/低信誉用户，同步审核: userId={}, commentId={}", userId, commentId);
                AutoAuditResult result = contentAuditService.auditText(content);

                // 低信誉用户且审核结果不确定，转人工审核
                if (reputation < 50 && result.getResult() == AutoAuditResultEnum.MANUAL) {
                    log.info("低信誉用户且审核不确定，转人工审核: userId={}, commentId={}", userId, commentId);
                    result = AutoAuditResult.builder()
                            .result(AutoAuditResultEnum.MANUAL)
                            .reason("低信誉用户，需人工复审")
                            .score(result.getScore())
                            .provider(result.getProvider())
                            .auditTime(new Date())
                            .build();
                }

                return result;
            }

        } catch (Exception e) {
            log.error("评论审核失败: userId={}, commentId={}", userId, commentId, e);

            return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.MANUAL)
                    .reason("审核服务异常，转人工审核")
                    .score(0.5)
                    .provider("error_handler")
                    .auditTime(new Date())
                    .build();
        }
    }

    /**
     * 异步审核（高信誉用户先发后审）
     */
    @Async
    public void asyncAudit(String commentId, String content) {
        try {
            log.info("开始异步审核评论: commentId={}", commentId);
            AutoAuditResult result = contentAuditService.auditText(content);

            // 保存审核日志
            saveAuditLog(commentId, "comment", result);

            // 如果审核不通过，需要通知管理员或自动隐藏
            if (result.getResult() == AutoAuditResultEnum.REJECT) {
                log.warn("异步审核发现违规评论: commentId={}, reason={}", commentId, result.getReason());
                // TODO: 通知管理员或自动隐藏评论
            }

        } catch (Exception e) {
            log.error("异步审核失败: commentId={}", commentId, e);
        }
    }

    /**
     * 保存审核日志（公共方法，供外部调用）
     * 
     * @param commentId 评论ID（必须非空）
     * @param contentType 内容类型
     * @param result 审核结果
     */
    public void saveAuditLog(String commentId, String contentType, AutoAuditResult result) {
        if (commentId == null || commentId.isEmpty()) {
            log.warn("commentId 为空，跳过保存审核日志");
            return;
        }
        
        try {
            WebAuditLog auditLog = new WebAuditLog();
            auditLog.setContentId(commentId);
            auditLog.setContentType(contentType);
            auditLog.setAuditResult(result.getResult().name());
            
            // 将 Double 转换为 BigDecimal
            if (result.getScore() != null) {
                auditLog.setAuditScore(BigDecimal.valueOf(result.getScore()));
            }
            
            auditLog.setAuditReason(result.getReason());
            auditLog.setAuditProvider(result.getProvider());
            auditLog.setAuditTime(result.getAuditTime() != null ? result.getAuditTime() : new Date());
            auditLog.setCreateTime(new Date());

            auditLogMapper.insert(auditLog);
            log.info("保存评论审核日志成功: commentId={}, result={}", commentId, result.getResult());

        } catch (Exception e) {
            log.error("保存评论审核日志失败: commentId={}", commentId, e);
        }
    }

    /**
     * 快速检查评论是否包含敏感词
     */
    public boolean containsSensitiveWord(String content) {
        return sensitiveWordFilter.containsSensitiveWord(content);
    }

    /**
     * 替换评论中的敏感词
     */
    public String replaceSensitiveWord(String content) {
        return sensitiveWordFilter.replaceSensitiveWord(content, '*');
    }
}
