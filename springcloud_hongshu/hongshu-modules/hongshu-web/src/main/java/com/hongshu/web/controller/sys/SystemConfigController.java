//package com.hongshu.web.controller.sys;
//
//
//import com.hongshu.common.core.annotation.AuthorityVerify.AuthorityVerify;
//import com.hongshu.common.core.web.domain.AjaxResult;
//import com.hongshu.web.domain.vo.SystemConfigVO;
//import com.hongshu.web.service.sys.SystemConfigService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//import static com.hongshu.common.core.web.domain.AjaxResult.success;
//
///**
// * 系统配置表 RestApi
// *
// * @author: hongshu
// * @date 2020年1月21日09:24:37
// */
//@Tag(name = "系统配置相关接口", description = "系统配置相关接口")
//@RestController
//@RequestMapping("/systemConfig")
//@Slf4j
//public class SystemConfigController {
//
//    @Autowired
//    private SystemConfigService systemConfigService;
//
//
//    @AuthorityVerify
//    @Operation(summary = "获取系统配置", description = "获取系统配置")
//    @GetMapping("/getSystemConfig")
//    public AjaxResult getSystemConfig() {
//        return success(systemConfigService.getConfig());
//    }
//
//    @AuthorityVerify
//    @Operation(summary = "通过Key前缀清空Redis缓存", description = "通过Key前缀清空Redis缓存")
//    @PostMapping("/cleanRedisByKey")
//    public String cleanRedisByKey(@RequestBody List<String> key) {
//        return systemConfigService.cleanRedisByKey(key);
//    }
//
//    @AuthorityVerify
//    @Operation(summary = "修改系统配置", description = "修改系统配置")
//    @PostMapping("/editSystemConfig")
//    public String editSystemConfig(@RequestBody SystemConfigVO systemConfigVO) {
//        return systemConfigService.editSystemConfig(systemConfigVO);
//    }
//}
//
