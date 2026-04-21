package com.hongshu.web.controller.sys;

import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.entity.WebPlannedFeature;
import com.hongshu.web.service.sys.ISysPlannedFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈控制器
 *
 * @author: hongshu
 */
@RestController
@RequestMapping("/feedback")
public class SysFeedbackController {

    @Autowired
    private ISysPlannedFeatureService plannedFeatureService;


    /**
     * 获取待实现功能列表（管理后台用，包含所有数据）
     */
    @GetMapping("/plannedFeatures/list")
    public Result<?> getPlannedFeatureList() {
        List<WebPlannedFeature> features = plannedFeatureService.list();
        return Result.ok(features);
    }

    /**
     * 添加待实现功能
     */
    @PostMapping("/plannedFeatures")
    public Result<?> addPlannedFeature(@RequestBody WebPlannedFeature feature) {
        boolean success = plannedFeatureService.save(feature);
        if (success) {
            return Result.ok(feature);
        } else {
            return Result.fail("添加失败");
        }
    }

    /**
     * 更新待实现功能
     */
    @PutMapping("/plannedFeatures")
    public Result<?> updatePlannedFeature(@RequestBody WebPlannedFeature feature) {
        boolean success = plannedFeatureService.updateById(feature);
        if (success) {
            return Result.ok(feature);
        } else {
            return Result.fail("更新失败");
        }
    }

    /**
     * 删除待实现功能
     */
    @DeleteMapping("/plannedFeatures/{id}")
    public Result<?> deletePlannedFeature(@PathVariable Long id) {
        boolean success = plannedFeatureService.removeById(id);
        if (success) {
            return Result.ok("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

}

