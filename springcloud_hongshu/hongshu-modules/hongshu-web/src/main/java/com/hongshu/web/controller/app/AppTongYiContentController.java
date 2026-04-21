package com.hongshu.web.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.dto.TongYiContentRequestDTO;
import com.hongshu.web.domain.dto.TongYiContentResponseDTO;
import com.hongshu.web.service.web.ITongYiContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通义千问内容生成控制器
 * 支持图片和视频URL输入，生成小红书风格的标题和内容
 *
 * @author hongshu
 */
@Slf4j
@RestController
@RequestMapping("/app/tongyi/content")
public class AppTongYiContentController {

    @Autowired
    private ITongYiContentService tongYiContentService;


    /**
     * 根据图片和视频URL生成文案
     */
    @PostMapping("/generate")
    public Result<TongYiContentResponseDTO> generateContent(@RequestBody TongYiContentRequestDTO request) {
        try {
            log.info("收到通义千问内容生成请求: imageUrls={}, videoUrl={}, style={}",
                    request.getImageUrls(), request.getVideoUrl(), request.getStyle());

            TongYiContentResponseDTO response = tongYiContentService.generateContent(request);
            if (response.getSuccess()) {
                return Result.ok(response);
            } else {
                return Result.build(500, response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成文案失败", e);
            return Result.build(500, "生成文案失败: " + e.getMessage());
        }
    }

    /**
     * 根据上传的图片文件生成文案
     */
    @PostMapping(value = "/generate-from-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<TongYiContentResponseDTO> generateContentFromImages(
            @RequestParam("imageFiles") MultipartFile[] imageFiles,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "productDescription", required = false) String productDescription,
            @RequestParam(value = "style", required = false, defaultValue = "小红书笔记") String style) {
        try {
            if (imageFiles == null || imageFiles.length == 0) {
                return Result.build(400, "请至少上传一张图片");
            }

            TongYiContentResponseDTO response = tongYiContentService.generateContentFromFiles(
                    imageFiles, category, productName, productDescription, style);

            if (response.getSuccess()) {
                return Result.ok(response);
            } else {
                return Result.build(500, response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("上传图片并生成文案失败", e);
            return Result.build(500, "生成文案失败: " + e.getMessage());
        }
    }

    /**
     * 根据上传的视频文件生成文案
     */
    @PostMapping(value = "/generate-from-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<TongYiContentResponseDTO> generateContentFromVideo(
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "productDescription", required = false) String productDescription,
            @RequestParam(value = "style", required = false, defaultValue = "小红书笔记") String style) {
        try {
            if (videoFile == null || videoFile.isEmpty()) {
                return Result.build(400, "请上传视频文件");
            }

            TongYiContentResponseDTO response = tongYiContentService.generateContentFromVideo(
                    videoFile, category, productName, productDescription, style);

            if (response.getSuccess()) {
                return Result.ok(response);
            } else {
                return Result.build(500, response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("上传视频并生成文案失败", e);
            return Result.build(500, "生成文案失败: " + e.getMessage());
        }
    }

    /**
     * 只生成标题
     */
    @PostMapping("/generate-titles")
    public Result<TongYiContentResponseDTO> generateTitles(@RequestBody TongYiContentRequestDTO request) {
        try {
            TongYiContentResponseDTO response = tongYiContentService.generateTitlesOnly(request);
            if (response.getSuccess()) {
                return Result.ok(response);
            } else {
                return Result.build(500, response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成标题失败", e);
            return Result.build(500, "生成标题失败: " + e.getMessage());
        }
    }
}

