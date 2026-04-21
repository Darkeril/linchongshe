package com.hongshu.idle.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.ValidatorUtils;
import com.hongshu.common.core.validator.group.AddGroup;
import com.hongshu.idle.domain.dto.CollectionDTO;
import com.hongshu.idle.domain.vo.CollectionVO;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.service.idle.IIdleCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏
 *
 * @author: hongshu
 */
@RequestMapping("/app/collection")
@RestController
public class AppCollectionController {

    @Autowired
    IIdleCollectionService collectionService;


    /**
     * 收藏
     *
     * @param collectionDTO 点赞收藏实体
     */
    @PostMapping("collectionByDTO")
    public Result<?> collectionByDTO(@RequestBody CollectionDTO collectionDTO) {
        ValidatorUtils.validateEntity(collectionDTO, AddGroup.class);
        collectionService.collectionByDTO(collectionDTO);
        return Result.ok();
    }

    /**
     * 是否收藏
     *
     * @param collectionDTO 点赞收藏实体
     */
    @PostMapping("isCollection")
    public Result<?> isCollection(@RequestBody CollectionDTO collectionDTO) {
        boolean flag = collectionService.isCollection(collectionDTO);
        return Result.ok(flag);
    }

    /**
     * 获取当前用户最新的收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getNoticeCollection/{currentPage}/{pageSize}")
    public Result<?> getNoticeCollection(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<IdleProductVO> pageInfo = collectionService.getNoticeCollection(currentPage, pageSize);
        return Result.ok(pageInfo);
    }
}
