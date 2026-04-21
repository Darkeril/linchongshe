package com.hongshu.idle.controller.idle;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.domain.vo.IdleCategoryVO;
import com.hongshu.idle.service.idle.IIdleCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闲宝分类
 *
 * @author: hongshu
 */
@RequestMapping("/category")
@RestController
public class IdleCategoryController {

    @Autowired
    private IIdleCategoryService categoryService;


    /**
     * 获取树形分类数据
     */
    @GetMapping("getCategoryTreeData")
    @NoLoginIntercept
    public Result<?> getCategoryTreeData() {
        List<IdleCategoryVO> categoryList = categoryService.getCategoryTreeData();
        return Result.ok(categoryList);
    }

    /**
     * 搜索对应的笔记
     *
     * @param esProductDTO 笔记
     */
    @NoLoginIntercept
    @PostMapping("getCategoryAgg")
    public Result<?> getCategoryAgg(@RequestBody EsProductDTO esProductDTO) {
        List<IdleCategory> categoryList = categoryService.getCategoryAgg(esProductDTO);
        return Result.ok(categoryList);
    }
}
