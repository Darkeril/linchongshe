package com.hongshu.web.controller;

import com.hongshu.common.core.enums.Result;
import com.hongshu.web.service.IOssUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OSS
 *
 * @author: hongshu
 */
@RequestMapping("/oss")
@RestController
public class OssUploadController {

    @Autowired
    private IOssUploadService ossService;


    /**
     * 上传文件
     *
     * @param file 文件
     */
    @PostMapping("upload")
    public Result<?> upload(MultipartFile file) {
        String path = ossService.upload(file);
        return Result.ok(path);
    }

    /**
     * 批量上传文件
     *
     * @param files 文件集
     */
    @PostMapping(value = "uploadBatch")
    public Result<List<String>> uploadBatch(@RequestParam("uploadFiles") MultipartFile[] files) {
        if (files.length == 0) {
            return Result.fail(null);
        }
        List<String> stringList = ossService.uploadBatch(files);
        return Result.ok(stringList);
    }
}
