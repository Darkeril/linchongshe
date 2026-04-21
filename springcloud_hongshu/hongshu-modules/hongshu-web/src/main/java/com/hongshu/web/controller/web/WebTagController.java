package com.hongshu.web.controller.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.entity.WebTag;
import com.hongshu.web.domain.vo.NoteVo;
import com.hongshu.web.service.web.IWebTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签
 *
 * @author: hongshu
 */
@RequestMapping("/tag")
@RestController
public class WebTagController {

    @Autowired
    IWebTagService tagService;


    /**
     * 获取热门标签
     */
    @NoLoginIntercept
    @GetMapping("getHotTagList/{currentPage}/{pageSize}")
    public Result<?> getHotTagList(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<WebTag> voList = tagService.getHotTagList(currentPage, pageSize);
        return Result.ok(voList);
    }

    /**
     * 根据关键词获取标签
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param keyword     关键词
     */
    @GetMapping("getTagByKeyword/{currentPage}/{pageSize}")
    public Result<?> getTagByKeyword(@PathVariable long currentPage, @PathVariable long pageSize, String keyword) {
        Page<WebTag> page = tagService.getTagByKeyword(currentPage, pageSize, keyword);
        return Result.ok(page);
    }

    /**
     * 获取当前标签信息
     *
     * @param tagId 标签ID
     */
    @GetMapping("getTagById")
    public Result<?> getTagById(String tagId) {
        WebTag tag = tagService.getById(tagId);
        return Result.ok(tag);
    }

    /**
     * 根据标签ID获取图片信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param tagId       标签id
     * @param type        类型
     */
    @GetMapping("getNoteByTagId/{currentPage}/{pageSize}")
    public Result<?> getNoteByTagId(@PathVariable long currentPage, @PathVariable long pageSize, String tagId, Integer type) {
        Page<NoteVo> imgDetailVoList = tagService.getNoteByTagId(currentPage, pageSize, tagId, type);
        return Result.ok(imgDetailVoList);
    }
}
