package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.utils.TreeUtils;
import com.hongshu.web.domain.entity.WebCategory;
import com.hongshu.web.domain.vo.CategoryVo;
import com.hongshu.web.mapper.web.WebCategoryMapper;
import com.hongshu.web.service.web.IWebCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类
 *
 * @author: hongshu
 */
@Service
public class WebCategoryServiceImpl extends ServiceImpl<WebCategoryMapper, WebCategory> implements IWebCategoryService {

    /**
     * 获取树形分类数据
     */
    @Override
    public List<CategoryVo> getCategoryTreeData() {
        // 只查询状态为正常（status = '0'）且未删除的分类
        List<WebCategory> list = this.list(new QueryWrapper<WebCategory>()
                .eq("status", "0")
                .eq("deleted", 0)
                .orderByAsc("sort"));
        List<CategoryVo> convertor = DozerUtil.convertor(list, CategoryVo.class);
        return TreeUtils.build(convertor);
    }
}
