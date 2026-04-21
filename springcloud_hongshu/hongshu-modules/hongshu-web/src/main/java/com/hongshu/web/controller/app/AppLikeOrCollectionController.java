package com.hongshu.web.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.ValidatorUtils;
import com.hongshu.common.core.validator.group.AddGroup;
import com.hongshu.web.domain.dto.LikeOrCollectionDTO;
import com.hongshu.web.domain.vo.LikeOrCollectionVo;
import com.hongshu.web.service.web.IWebLikeOrCollectionService;
import com.hongshu.web.service.web.impl.WebLikeOrCollectionServiceV2Impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞/收藏
 *
 * @author: hongshu
 * 
 * 优化版本：使用 Redis + RocketMQ
 */
@RequestMapping("/app/likeOrCollection")
@RestController
public class AppLikeOrCollectionController {

    // V1版本（原有实现）- 用于获取通知列表
    @Autowired
    IWebLikeOrCollectionService likeOrCollectionService;

    // V2版本（优化实现）- 用于点赞/收藏操作
    @Autowired
    @Qualifier("webLikeOrCollectionServiceV2")
    WebLikeOrCollectionServiceV2Impl likeOrCollectionServiceV2;


    /**
     * 点赞或收藏（优化版：使用 Redis + RocketMQ）
     *
     * @param likeOrCollectionDTO 点赞收藏实体
     */
    @PostMapping("likeOrCollectionByDTO")
    public Result<?> likeOrCollectionByDTO(@RequestBody LikeOrCollectionDTO likeOrCollectionDTO) {
        ValidatorUtils.validateEntity(likeOrCollectionDTO, AddGroup.class);
        // 使用 V2 优化版本（Redis + RocketMQ）
        likeOrCollectionServiceV2.likeOrCollectionByDTO(likeOrCollectionDTO);
        return Result.ok();
    }

    /**
     * 是否点赞或收藏（优化版：优先从 Redis 读取）
     *
     * @param likeOrCollectionDTO 点赞收藏实体
     */
    @PostMapping("isLikeOrCollection")
    public Result<?> isLikeOrCollection(@RequestBody LikeOrCollectionDTO likeOrCollectionDTO) {
        // 使用 V2 优化版本（Redis缓存）
        boolean flag = likeOrCollectionServiceV2.isLikeOrCollection(likeOrCollectionDTO);
        return Result.ok(flag);
    }

    /**
     * 获取当前用户最新的点赞和收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getNoticeLikeOrCollection/{currentPage}/{pageSize}")
    public Result<?> getNoticeLikeOrCollection(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<LikeOrCollectionVo> pageInfo = likeOrCollectionService.getNoticeLikeOrCollection(currentPage, pageSize);
        return Result.ok(pageInfo);
    }
}
