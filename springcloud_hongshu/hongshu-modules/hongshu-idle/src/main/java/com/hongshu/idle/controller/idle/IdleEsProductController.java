package com.hongshu.idle.controller.idle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/es/product")
public class IdleEsProductController {

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
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @NoLoginIntercept
    @GetMapping("getHotProduct/{currentPage}/{pageSize}")
    public Result<?> getHotProduct(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<IdleProductVO> page = esProductService.getHotProduct(currentPage, pageSize);
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

    /**
     * 增加笔记
     *
     * @param idleProductVO 笔记
     */
    @PostMapping("addProduct")
    public void addProduct(@RequestBody IdleProductVO idleProductVO) {
        esProductService.addProduct(idleProductVO);
    }

    /**
     * 修改笔记
     *
     * @param idleProductVO 笔记
     */
    @PostMapping("updateProduct")
    public void updateProduct(@RequestBody IdleProductVO idleProductVO) {
        esProductService.updateProduct(idleProductVO);
    }

    /**
     * 删除es中的笔记
     *
     * @param productId 笔记ID
     */
    @RequestMapping("deleteProduct/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        esProductService.deleteProduct(productId);
    }
}
