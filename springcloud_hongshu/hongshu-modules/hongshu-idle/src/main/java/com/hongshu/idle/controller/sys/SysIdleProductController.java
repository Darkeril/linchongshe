package com.hongshu.idle.controller.sys;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.utils.poi.ExcelUtil;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.idle.domain.Query;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.service.sys.ISysIdleProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 闲宝信息
 *

 */
@RestController
@RequestMapping("/product")
public class SysIdleProductController extends BaseController {

    @Autowired
    private ISysIdleProductService productService;


    /**
     * 获取笔记列表
     */
    @GetMapping("/allList")
    public TableDataInfo list() {
        this.startPage();
        List<IdleProduct> productList = productService.getAllProductList();
        return getDataTable(productList);
    }

    /**
     * 获取笔记列表
     */
    @PreAuthorize("@ss.hasPermi('web:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<IdleProduct> productList = productService.selectProductList(new Query(map));
        return getDataTable(productList);
    }

    /**
     * 获取未审核笔记列表
     */
    @PreAuthorize("@ss.hasPermi('web:product:list')")
    @GetMapping("/unAuditList")
    public TableDataInfo UnAuditList(@RequestParam Map map) {
        this.startPage();
        List<IdleProduct> productList = productService.selectUnAuditProductList(new Query(map));
        return getDataTable(productList);
    }

    /**
     * 根据笔记编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(productService.selectProductById(id));
    }

    /**
     * 新增笔记
     */
    @PreAuthorize("@ss.hasPermi('web:product:add')")
    @Log(title = "笔记管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam("product") String product, @RequestParam(value = "file", required = false) MultipartFile file) {
        IdleProduct idleProduct = JSONUtil.toBean(product, IdleProduct.class);
        return toAjax(productService.insertProduct(idleProduct, file));
    }

    /**
     * 修改笔记
     */
    @PreAuthorize("@ss.hasPermi('web:product:edit')")
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestParam("product") String product, 
                          @RequestParam(value = "file", required = false) MultipartFile file) {
        IdleProduct idleProduct = JSONUtil.toBean(product, IdleProduct.class);
        return toAjax(productService.updateProduct(idleProduct, file, null));
    }

    /**
     * 删除笔记
     */
    @PreAuthorize("@ss.hasPermi('web:product:remove')")
    @Log(title = "笔记管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productService.deleteProductByIds(ids));
    }

    /**
     * 导出笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('web:product:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Query query) {
        List<IdleProduct> productList = productService.selectProductList(query);
        ExcelUtil<IdleProduct> util = new ExcelUtil<>(IdleProduct.class);
        util.exportExcel(response, productList, "笔记数据");
    }

    /**
     * 审核管理(0:未审核 1：通过 2：拒绝)
     */
    @PutMapping("auditProduct")
    public Result<?> auditProduct(@RequestBody Map<String, String> map) {
        String productId = map.get("productId");
        String auditType = map.get("auditType");
        productService.auditProduct(productId, auditType);
        return Result.ok();
    }

    /**
     * 批量笔记审核(0:未审核 1：通过 2：拒绝)
     */
    @PutMapping("batchAuditProduct")
    public Result<?> batchAuditProduct(@RequestBody Map<String, String> map) {
        String productIds = map.get("productIds"); // 逗号分隔的ID字符串
        String auditType = map.get("auditType");

        // 分割ID字符串并逐个处理
        if (productIds != null && !productIds.isEmpty()) {
            String[] idArray = productIds.split(",");
            for (String productId : idArray) {
                productService.auditProduct(productId.trim(), auditType);
            }
        }
        return Result.ok();
    }

    /**
     * 批量增加商品
     */
    @PostMapping("addProductBulkData")
    @NoLoginIntercept
    public void addProductBulkData() {
        productService.addProductBulkData();
    }

    /**
     * 清空商品
     */
    @DeleteMapping("delProductBulkData")
    @NoLoginIntercept
    public void delNoteBulkData() {
        productService.delProductBulkData();
    }

    /**
     * 重置
     */
    @PostMapping("refreshProductData")
    @NoLoginIntercept
    public Result<?> refreshProductData() {
        productService.refreshProductData();
        return Result.ok();
    }


    @GetMapping("/productCountByCategory")
    @NoLoginIntercept
    public Result<?> getProductCountByCategory() {
        return Result.ok(productService.getProductCountByCategory());
    }

    @GetMapping("/productCount")
    @NoLoginIntercept
    public Result<?> getProductCount() {
        return Result.ok(productService.getProductCount());
    }

    @GetMapping("/productCountByType")
    @NoLoginIntercept
    public Result<?> getProductCountByType() {
        return Result.ok(productService.getProductCountByType());
    }

    @GetMapping("/productContributeCount")
    @NoLoginIntercept
    public Result<?> getProductContributeCount() {
        return Result.ok(productService.getProductContributeCount());
    }
}
