package com.hongshu.idle.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.vo.IdleCategoryVO;
import com.hongshu.idle.service.idle.IIdleCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类
 *
 * @author: hongshu
 */
@RequestMapping("/app/category")
@RestController
public class AppCategoryController {

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
}
