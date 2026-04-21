package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.domain.vo.IdleCategoryVO;

import java.util.List;

/**
 * 分类
 *
 * @author: hongshu
 */
public interface IIdleCategoryService extends IService<IdleCategory> {

    /**
     * 获取树形分类数据
     */
    List<IdleCategoryVO> getCategoryTreeData();

    List<IdleCategory> getCategoryAgg(EsProductDTO esProductDTO);
}
