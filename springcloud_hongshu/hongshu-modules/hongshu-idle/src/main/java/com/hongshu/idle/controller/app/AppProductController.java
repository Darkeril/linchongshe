package com.hongshu.idle.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.dto.ProductAppDTO;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.service.idle.IIdleProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商城产品
 *
 * @author: hongshu
 */
@RequestMapping("/app/product")
@RestController
public class AppProductController {

    @Autowired
    private IIdleProductService productService;


    /**
     * 获取商品
     *
     * @param productId 商品ID
     */
    @GetMapping("getProductById")
    @NoLoginIntercept
    public Result<?> getProductById(String productId) {
        IdleProductVO productVO = productService.getProductById(productId);
        return Result.ok(productVO);
    }

//    /**
//     * 获取推荐商品
//     *
//     * @param currentPage 当前页
//     * @param pageSize    分页数
//     */
//    @NoLoginIntercept
//    @GetMapping("recommendProduct/{currentPage}/{pageSize}")
//    public Result<?> getRecommendProduct(@PathVariable long currentPage, @PathVariable long pageSize) {
//        Page<IdleProductVO> page = productService.getRecommendProduct(currentPage, pageSize);
//        return Result.ok(page);
//    }

    /**
     * 搜索对应的商品
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param esProductDTO 商品
     */
    @NoLoginIntercept
    @PostMapping("getProduct/{currentPage}/{pageSize}")
    public Result<?> getProduct(@PathVariable long currentPage,
                                @PathVariable long pageSize,
                                @RequestBody EsProductDTO esProductDTO) {
        Page<IdleProductVO> page = productService.getProduct(currentPage, pageSize, esProductDTO);
        return Result.ok(page);
    }

    /**
     * 查找商品
     *
     * @param keyword 关键词
     */
    @GetMapping("getProductByKeyword/{currentPage}/{pageSize}")
    public Result<?> getProductByKeyword(@PathVariable long currentPage,
                                         @PathVariable long pageSize,
                                         String keyword) {
        Page<IdleProductVO> pageInfo = productService.getProductByKeyword(currentPage, pageSize, keyword);
        return Result.ok(pageInfo);
    }

    /**
     * 新增商品
     *
     * @param requestId 唯一标识
     * @param noteData  笔记对象
     * @param coverFile 封面图
     * @param files     图片文件
     */
    @PostMapping("saveProductByDTO")
    public Result<?> saveProductByDTO(@RequestParam("requestId") String requestId,
                                      @RequestParam("noteData") String noteData,
                                      @RequestParam("coverFile") MultipartFile coverFile,
                                      @RequestParam("uploadFiles") MultipartFile[] files) {
        String terminal = "app";
        productService.saveProductByDTO(terminal, requestId, noteData, coverFile, files);
        return Result.ok();
    }

    /**
     * 新增商品
     *
     * @param requestId  唯一标识
     * @param productDTO 商品对象
     */
    @PostMapping("saveProduct")
    public Result<?> saveProduct(@RequestParam("requestId") String requestId,
                              @RequestBody ProductAppDTO productDTO) {
        String terminal = "app";
        productService.saveProduct(terminal, requestId, productDTO);
        return Result.ok();
    }

    /**
     * 更新商品
     *
     * @param productData 笔记对象
     * @param files       图片文件
     */
    @PostMapping("updateProductByDTO")
    public Result<?> updateProductByDTO(@RequestParam("productData") String productData,
                                        @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                                        @RequestParam(value = "uploadFiles", required = false) MultipartFile[] files) {
        productService.updateProductByDTO(productData, coverFile, files);
        return Result.ok();
    }

    /**
     * 置顶商品
     *
     * @param productId 商品ID
     */
    @GetMapping("pinnedProduct")
    public Result<?> pinnedProduct(String productId) {
        boolean flag = productService.pinnedProduct(productId);
        return Result.ok(flag);
    }

    @GetMapping("idleCount")
    public Result<?> getIdleCount(String userId) {
        return Result.ok(productService.getIdleCount(userId));
    }
}
