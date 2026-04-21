package com.hongshu.idle.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.service.idle.IIdleEsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ES
 *
 * @author: hongshu
 */
@RestController
@RequestMapping("/app/es/product")
public class AppEsProductController {

    @Autowired
    private IIdleEsProductService esProductService;


    /**
     * 搜索对应的笔记
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param esProductDTO 笔记
     */
    @NoLoginIntercept
    @PostMapping("getProductByDTO/{currentPage}/{pageSize}")
    public Result<?> getProductByDTO(@PathVariable long currentPage,
                                     @PathVariable long pageSize,
                                     @RequestBody EsProductDTO esProductDTO) {
        Page<IdleProductVO> page = esProductService.getProductByDTO(currentPage, pageSize, esProductDTO);
        return Result.ok(page);
    }

    /**
     * 获取推荐笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @NoLoginIntercept
    @GetMapping("recommendProduct/{currentPage}/{pageSize}")
    public Result<?> getRecommendProduct(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<IdleProductVO> page = esProductService.getRecommendProduct(currentPage, pageSize);
        return Result.ok(page);
    }

    /**
     * 获取视频商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @NoLoginIntercept
    @GetMapping("getVideoProduct/{currentPage}/{pageSize}")
    public Result<?> getVideoProduct(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<IdleProductVO> page = esProductService.getVideoProduct(currentPage, pageSize);
        return Result.ok(page);
    }
}
