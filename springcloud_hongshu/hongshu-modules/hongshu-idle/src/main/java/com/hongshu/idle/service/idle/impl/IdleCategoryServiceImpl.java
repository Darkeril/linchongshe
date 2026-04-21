package com.hongshu.idle.service.idle.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.utils.TreeUtils;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.domain.vo.IdleCategoryVO;
import com.hongshu.idle.mapper.idle.IdleCategoryMapper;
import com.hongshu.idle.service.idle.IIdleCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类
 *
 * @author: hongshu
 */
@Service
public class IdleCategoryServiceImpl extends ServiceImpl<IdleCategoryMapper, IdleCategory> implements IIdleCategoryService {

    @Autowired
    private IdleCategoryMapper categoryMapper;

    /**
     * 获取树形分类数据
     */
    @Override
    public List<IdleCategoryVO> getCategoryTreeData() {
        // 只查询状态为正常（status = '0'）且未删除的分类
        List<IdleCategory> list = this.list(new QueryWrapper<IdleCategory>()
                .eq("status", "0")
                .eq("deleted", 0)
                .orderByAsc("sort"));
        List<IdleCategoryVO> convertor = DozerUtil.convertor(list, IdleCategoryVO.class);
        return TreeUtils.build(convertor);
    }

    @Override
    public List<IdleCategory> getCategoryAgg(EsProductDTO esProductDTO) {
        // 只查询未删除的分类
        return categoryMapper.selectList(new QueryWrapper<IdleCategory>()
                .eq("deleted", 0)
                .like("title", esProductDTO.getKeyword()));
    }
}
