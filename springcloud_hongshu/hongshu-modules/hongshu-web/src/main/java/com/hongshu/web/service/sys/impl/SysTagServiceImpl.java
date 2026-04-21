package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebTag;
import com.hongshu.web.mapper.sys.SysTagMapper;
import com.hongshu.web.service.sys.ISysTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会员信息 服务层处理
 *

 */
@Slf4j
@Service
public class SysTagServiceImpl implements ISysTagService {

    @Autowired
    private SysTagMapper tagMapper;


    /**
     * 查询标签信息集合
     *
     * @param query 标签查询条件
     */
    @Override
    public List<WebTag> selectTagList(Query query) {
        QueryWrapper<WebTag> qw = new QueryWrapper<>();
        qw.lambda().like(ValidatorUtil.isNotNull(query.getTitle()), WebTag::getTitle, query.getTitle());
        // 处理 beginTime 和 endTime
        String beginTimeStr = (String) query.get("beginTime");
        String endTimeStr = (String) query.get("endTime");
        // 兼容 params[beginTime] 格式
        if (beginTimeStr == null) {
            beginTimeStr = (String) query.get("params[beginTime]");
        }
        if (endTimeStr == null) {
            endTimeStr = (String) query.get("params[endTime]");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginTimeStr != null && !beginTimeStr.trim().isEmpty()) {
            try {
                // 将字符串转为 Date
                Date beginTime = sdf.parse(beginTimeStr);
                // 添加大于等于条件
                qw.lambda().ge(WebTag::getCreateTime, beginTime);
            } catch (Exception e) {
                log.warn("解析开始时间失败: {}", beginTimeStr, e);
            }
        }
        if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
            try {
                Date endTime = sdf.parse(endTimeStr);
                // 确保 endTime 是当天的结束时间（23:59:59）
                endTime = new Date(endTime.getTime() + 1000 * 60 * 60 * 24 - 1);
                // 添加小于等于条件
                qw.lambda().le(WebTag::getCreateTime, endTime);
            } catch (Exception e) {
                log.warn("解析结束时间失败: {}", endTimeStr, e);
            }
        }
        // 排序：支持按创建时间和更新时间排序
        String orderByColumn = (String) query.get("orderByColumn");
        String isAsc = (String) query.get("isAsc");
        if (orderByColumn != null && !orderByColumn.trim().isEmpty()) {
            if ("createTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebTag::getCreateTime);
                } else {
                    // 默认降序
                    qw.lambda().orderByDesc(WebTag::getCreateTime);
                }
            } else if ("updateTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebTag::getUpdateTime);
                } else {
                    // 默认降序
                    qw.lambda().orderByDesc(WebTag::getUpdateTime);
                }
            } else {
                // 默认按创建时间降序
                qw.lambda().orderByDesc(WebTag::getCreateTime);
            }
        } else {
            // 默认按创建时间降序
            qw.lambda().orderByDesc(WebTag::getCreateTime);
        }
        return tagMapper.selectList(qw);
    }

    /**
     * 查询所有标签
     */
    @Override
    public List<WebTag> selectTagAll() {
        return tagMapper.selectList(new QueryWrapper<>());
    }

    /**
     * 通过标签ID查询标签信息
     *
     * @param id 标签ID
     */
    @Override
    public WebTag selectTagById(Long id) {
        return tagMapper.selectById(id);
    }

    /**
     * 新增标签信息
     *
     * @param tag 标签信息
     */
    @Override
    public int insertTag(WebTag tag) {
        if (tag.getSort() == null) {
            tag.setSort(0);
        }
        if (tag.getLikeCount() == null) {
            tag.setLikeCount(0L);
        }
        tag.setCreator("System");
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());
        return tagMapper.insert(tag);
    }

    /**
     * 修改保存标签信息
     *
     * @param tag 标签信息
     */
    @Override
    public int updateTag(WebTag tag) {
        WebTag webTag = tagMapper.selectById(tag.getId());
        if (webTag == null) {
            log.warn("标签不存在: {}", tag.getId());
            return 0;
        }
        // 只更新非null的字段
        if (tag.getTitle() != null) {
            webTag.setTitle(tag.getTitle());
        }
        if (tag.getSort() != null) {
            webTag.setSort(tag.getSort());
        }
        webTag.setUpdater("System");
        webTag.setUpdateTime(new Date());
        return tagMapper.updateById(webTag);
    }

    /**
     * 删除标签信息
     *
     * @param id 标签ID
     */
    @Override
    public int deleteTagById(Long id) {
        return tagMapper.deleteById(id);
    }

    /**
     * 批量删除标签信息
     *
     * @param ids 需要删除的标签ID
     */
    @Override
    public int deleteTagByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        // 过滤掉不存在的标签ID
        List<Long> validIds = new ArrayList<>();
        for (Long id : ids) {
            WebTag tag = selectTagById(id);
            if (ValidatorUtil.isNotNull(tag)) {
                validIds.add(id);
            } else {
                log.warn("标签不存在:{}", id);
            }
        }
        if (validIds.isEmpty()) {
            return 0;
        }
        return tagMapper.deleteBatchIds(validIds);
    }
}

