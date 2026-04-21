//package com.hongshu.idle.controller;
//
//import com.hongshu.common.core.enums.Result;
//import com.hongshu.idle.service.IOssService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
///**
// * OSS
// *
// * @author: hongshu
// */
//@RequestMapping("/oss")
//@RestController
//public class OssController {
//
//    @Autowired
//    private IOssService ossService;
//
//
//    /**
//     * 上传文件
//     *
//     * @param file 文件
//     */
//    @PostMapping("upload")
//    public Result<?> upload(MultipartFile file) {
//        String path = ossService.upload(file);
//        return Result.ok(path);
//    }
//
//    /**
//     * 批量上传文件
//     *
//     * @param files 文件集
//     */
//    @PostMapping(value = "uploadBatch")
//    public Result<List<String>> uploadBatch(@RequestParam("uploadFiles") MultipartFile[] files) {
//        if (files.length == 0) {
//            return Result.fail(null);
//        }
//        List<String> stringList = ossService.uploadBatch(files);
//        return Result.ok(stringList);
//    }
//
//    /**
//     * 删除文件
//     *
//     * @param path 路径
//     */
//    @GetMapping("delete")
//    public Result<?> delete(String path) {
//        ossService.delete(path);
//        return Result.ok();
//    }
//
//    /**
//     * 批量删除文件
//     *
//     * @param filePaths 文件路径集
//     */
//    @PostMapping(value = "deleteBatch")
//    public Result<?> deleteBatch(@RequestBody List<String> filePaths) {
//        if (filePaths.isEmpty()) {
//            return Result.fail(null);
//        }
//        ossService.batchDelete(filePaths);
//        return Result.ok();
//    }
//}
