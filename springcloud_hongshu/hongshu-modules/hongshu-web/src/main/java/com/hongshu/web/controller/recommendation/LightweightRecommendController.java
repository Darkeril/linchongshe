package com.hongshu.web.controller.recommendation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.vo.NoteVo;
import com.hongshu.web.service.recommendation.lightweight.LightweightRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 轻量级推荐控制器（四维度推荐算法）
 * 提供简洁高效的推荐接口
 * <p>
 * 📊 标签召回（40%权重）
 * └─ 根据用户点赞/收藏的笔记标签推荐
 * <p>
 * 📂 分类召回（30%权重）
 * └─ 根据用户偏好的分类推荐
 * <p>
 * 👥 关注召回（20%权重）
 * └─ 推荐关注用户的最新内容
 * <p>
 * 🔥 热门召回（10%权重）
 * └─ 推荐近7天的热门内容
 *
 * <p>
 * 小红书的推荐特点 = 标签匹配 + 分类探索 + 社交关系 + 热门内容
 * 综合评分 = 互动质量(40%) + 新鲜度(30%) + 个性化(30%)
 * <p>
 * - 互动质量: 点赞×1 + 收藏×2 + 评论×3
 * - 新鲜度: 时间衰减（24小时内最高）
 * - 个性化: 来源权重（标签>关注>分类>热门）
 *

 * @date 2024/01/01
 */
@RestController
@RequestMapping("/api/recommend/lightweight")
@Slf4j
public class LightweightRecommendController {

    @Autowired
    private LightweightRecommendService lightweightRecommendService;


    /**
     * 获取推荐笔记（轻量级版本）
     * 支持未登录和已登录用户
     *
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @return 推荐笔记分页结果
     */
    @NoLoginIntercept
    @GetMapping("/notes/{currentPage}/{pageSize}")
    public Result<?> getRecommendNotes(@PathVariable long currentPage, @PathVariable long pageSize) {

        try {
            // 获取当前用户ID（未登录则为null）
            String userId = null;
            try {
                userId = AuthContextHolder.getUserId();
            } catch (Exception e) {
                log.debug("用户未登录");
            }

            log.info("轻量级推荐接口 - 用户: {}, 页码: {}, 大小: {}", userId, currentPage, pageSize);

            // 调用推荐服务
            Page<NoteVo> result = lightweightRecommendService.getRecommendNotes(userId, currentPage, pageSize);

            return Result.ok(result);

        } catch (Exception e) {
            log.error("获取推荐笔记失败", e);
            return Result.ok("获取推荐笔记失败: " + e.getMessage());
        }
    }


    /**
     * 获取推荐笔记（指定用户）
     * 用于测试或管理后台
     *
     * @param userId      用户ID
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @return 推荐笔记分页结果
     */
    @GetMapping("/notes/user/{userId}/{currentPage}/{pageSize}")
    public Result<?> getRecommendNotesForUser(@PathVariable String userId, @PathVariable long currentPage, @PathVariable long pageSize) {

        try {
            if (StringUtils.isBlank(userId)) {
                return Result.ok("用户ID不能为空");
            }

            log.info("获取指定用户推荐 - 用户: {}, 页码: {}, 大小: {}", userId, currentPage, pageSize);

            Page<NoteVo> result = lightweightRecommendService.getRecommendNotes(
                    userId, currentPage, pageSize
            );

            return Result.ok(result);

        } catch (Exception e) {
            log.error("获取指定用户推荐笔记失败", e);
            return Result.ok("获取推荐笔记失败: " + e.getMessage());
        }
    }


    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @NoLoginIntercept
    @GetMapping("/health")
    public Result<?> health() {
        return Result.ok("轻量级推荐服务运行正常");
    }

}

