package com.hongshu.idle.controller.recommendation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.service.recommendation.LightweightProductRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 轻量级商品推荐控制器
 * 提供简洁高效的商品推荐接口
 *

 * @date 2024/01/01
 */
@RestController
@RequestMapping("/api/recommend/product/lightweight")
@Slf4j
public class LightweightProductRecommendController {

    @Autowired
    private LightweightProductRecommendService lightweightProductRecommendService;


    /**
     * 获取推荐商品（轻量级版本）
     * 支持未登录和已登录用户
     *
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @return 推荐商品分页结果
     */
    @NoLoginIntercept
    @GetMapping("/products/{currentPage}/{pageSize}")
    public Result<?> getRecommendProducts(@PathVariable long currentPage, @PathVariable long pageSize) {

        try {
            // 获取当前用户ID（未登录则为null）
            String userId = null;
            try {
                userId = AuthContextHolder.getUserId();
            } catch (Exception e) {
                log.debug("用户未登录");
            }

            log.info("轻量级商品推荐接口 - 用户: {}, 页码: {}, 大小: {}", userId, currentPage, pageSize);

            // 调用推荐服务
            Page<IdleProductVO> result = lightweightProductRecommendService.getRecommendProducts(userId, currentPage, pageSize);

            return Result.ok(result);

        } catch (Exception e) {
            log.error("获取推荐商品失败", e);
            return Result.ok("获取推荐商品失败: " + e.getMessage());
        }
    }


    /**
     * 获取推荐商品（指定用户）
     * 用于测试或管理后台
     *
     * @param userId      用户ID
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @return 推荐商品分页结果
     */
    @GetMapping("/products/user/{userId}/{currentPage}/{pageSize}")
    public Result<?> getRecommendProductsForUser(@PathVariable String userId, @PathVariable long currentPage, @PathVariable long pageSize) {

        try {
            if (StringUtils.isBlank(userId)) {
                return Result.ok("用户ID不能为空");
            }

            log.info("获取指定用户商品推荐 - 用户: {}, 页码: {}, 大小: {}", userId, currentPage, pageSize);

            Page<IdleProductVO> result = lightweightProductRecommendService.getRecommendProducts(
                    userId, currentPage, pageSize
            );

            return Result.ok(result);

        } catch (Exception e) {
            log.error("获取指定用户推荐商品失败", e);
            return Result.ok("获取推荐商品失败: " + e.getMessage());
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
        return Result.ok("轻量级商品推荐服务运行正常");
    }

}



