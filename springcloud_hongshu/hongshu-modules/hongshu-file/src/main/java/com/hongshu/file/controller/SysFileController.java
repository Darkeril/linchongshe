package com.hongshu.file.controller;

import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.utils.file.FileUtils;
import com.hongshu.file.service.ISysFileService;
import com.hongshu.file.service.XiaohongshuDownloadService;
import com.hongshu.system.api.domain.SysFile;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文件请求处理
 *

 */
@RestController
@Tag(name = "文件服务", description = "文件上传、批量上传等")
public class SysFileController {

    private static final Logger log = LoggerFactory.getLogger(SysFileController.class);

    @Autowired
    private ISysFileService sysFileService;

    @Autowired
    private XiaohongshuDownloadService xiaohongshuDownloadService;

    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 总大小限制50MB
    private static final int MAX_FILES = 10; // 最大文件数量


    /**
     * 文件上传请求
     */
    @PostMapping("upload")
    public R<SysFile> upload(MultipartFile file) {
        // 参数校验
        if (file == null || file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }
        try {
            // 上传并返回访问地址
            String url = sysFileService.uploadFile(file);
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("batch/upload")
    public R<List<SysFile>> batchUpload(MultipartFile[] files) {
        // 参数校验
        if (ArrayUtils.isEmpty(files)) {
            return R.fail("上传文件不能为空");
        }
        // 文件数量校验
        if (files.length > MAX_FILES) {
            return R.fail("单次最多上传" + MAX_FILES + "个文件");
        }
        try {
            // 总大小校验
//            long totalSize = Arrays.stream(files)
//                    .mapToLong(MultipartFile::getSize)
//                    .sum();
//            if (totalSize > MAX_TOTAL_SIZE) {
//                return R.fail("文件总大小不能超过50MB");
//            }
            List<SysFile> resultList = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            // 并行上传文件
            CompletableFuture<?>[] futures = Arrays.stream(files)
                    .map(file -> CompletableFuture.runAsync(() -> {
                        try {
                            // 单个文件上传
                            String url = sysFileService.uploadFile(file);
                            SysFile sysFile = SysFile.builder()
                                    .name(FileUtils.getName(url))
                                    .url(url)
//                                    .size(file.getSize())
//                                    .contentType(file.getContentType())
//                                    .originalFilename(file.getOriginalFilename())
//                                    .createTime(LocalDateTime.now())
                                    .build();
                            synchronized (resultList) {
                                resultList.add(sysFile);
                            }
                        } catch (Exception e) {
                            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                            synchronized (failedFiles) {
                                failedFiles.add(file.getOriginalFilename());
                            }
                        }
                    }))
                    .toArray(CompletableFuture[]::new);
            // 等待所有上传完成
            CompletableFuture.allOf(futures).join();
            // 处理上传结果
            if (!failedFiles.isEmpty()) {
                return R.fail("部分文件上传失败: " + String.join(", ", failedFiles));
            }
            return R.ok(resultList);
        } catch (Exception e) {
            log.error("批量上传文件失败", e);
            return R.fail("批量上传失败：" + e.getMessage());
        }
    }


    /**
     * 文件上传请求
     */
    @PostMapping("upload/file")
    public R<SysFile> upload(File file) {
        // 参数校验
        if (file == null) {
            return R.fail("上传文件不能为空");
        }
        try {
            // 上传并返回访问地址
            String url = sysFileService.uploadFile(file);
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("batch/upload/file")
    public R<List<SysFile>> batchUpload(File[] files) {
        // 参数校验
        if (ArrayUtils.isEmpty(files)) {
            return R.fail("上传文件不能为空");
        }
        // 文件数量校验
        if (files.length > MAX_FILES) {
            return R.fail("单次最多上传" + MAX_FILES + "个文件");
        }
        try {
            // 总大小校验
//            long totalSize = Arrays.stream(files)
//                    .mapToLong(MultipartFile::getSize)
//                    .sum();
//            if (totalSize > MAX_TOTAL_SIZE) {
//                return R.fail("文件总大小不能超过50MB");
//            }
            List<SysFile> resultList = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            // 并行上传文件
            CompletableFuture<?>[] futures = Arrays.stream(files)
                    .map(file -> CompletableFuture.runAsync(() -> {
                        try {
                            // 单个文件上传
                            String url = sysFileService.uploadFile(file);
                            SysFile sysFile = SysFile.builder()
                                    .name(FileUtils.getName(url))
                                    .url(url)
//                                    .size(file.getSize())
//                                    .contentType(file.getContentType())
//                                    .originalFilename(file.getOriginalFilename())
//                                    .createTime(LocalDateTime.now())
                                    .build();
                            synchronized (resultList) {
                                resultList.add(sysFile);
                            }
                        } catch (Exception e) {
//                            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                            synchronized (failedFiles) {
//                                failedFiles.add(file.getOriginalFilename());
                            }
                        }
                    }))
                    .toArray(CompletableFuture[]::new);
            // 等待所有上传完成
            CompletableFuture.allOf(futures).join();
            // 处理上传结果
            if (!failedFiles.isEmpty()) {
                return R.fail("部分文件上传失败: " + String.join(", ", failedFiles));
            }
            return R.ok(resultList);
        } catch (Exception e) {
            log.error("批量上传文件失败", e);
            return R.fail("批量上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载小红书视频到服务器并上传到OSS
     *
     * @param url 视频URL（需要URL编码）
     * @param cookie Cookie字符串（可选，用于认证）
     * @return OSS访问链接
     */
    @PostMapping("xiaohongshu/video/download")
    public R<SysFile> downloadXiaohongshuVideo(
            @RequestParam String url,
            @RequestParam(value = "cookie", required = false) String cookie) {
        try {
            if (StringUtils.isBlank(url)) {
                return R.fail("视频URL不能为空");
            }

            // URL解码
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            log.info("收到视频下载请求: {}", decodedUrl);

            // 验证URL是否为小红书视频URL
            if (!decodedUrl.contains("xhscdn.com") && !decodedUrl.contains("xiaohongshu.com")) {
                log.warn("非小红书视频URL，拒绝下载: {}", decodedUrl);
                return R.fail("仅支持小红书视频URL");
            }

            // 下载视频到临时文件
            log.info("开始下载视频到临时文件...");
            File videoFile = xiaohongshuDownloadService.downloadVideoToFile(decodedUrl, cookie);
            log.info("视频下载完成，临时文件: {}, 大小: {} bytes", videoFile.getAbsolutePath(), videoFile.length());

            try {
                // 上传到OSS
                log.info("开始上传视频到OSS...");
                String ossUrl = sysFileService.uploadFile(videoFile);
                log.info("视频上传到OSS成功，URL: {}", ossUrl);

                SysFile sysFile = new SysFile();
                sysFile.setName(FileUtils.getName(ossUrl));
                sysFile.setUrl(ossUrl);
                return R.ok(sysFile);
            } finally {
                // 删除临时文件
                try {
                    if (videoFile != null && videoFile.exists()) {
                        boolean deleted = videoFile.delete();
                        if (deleted) {
                            log.info("临时文件已删除: {}", videoFile.getAbsolutePath());
                        } else {
                            log.warn("临时文件删除失败: {}", videoFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    log.warn("删除临时文件时出错", e);
                }
            }

        } catch (Exception e) {
            log.error("下载视频失败", e);
            return R.fail("下载视频失败: " + e.getMessage());
        }
    }

    /**
     * 下载小红书图片到服务器并上传到OSS
     *
     * @param url 图片URL（需要URL编码）
     * @param cookie Cookie字符串（可选，用于认证）
     * @return OSS访问链接
     */
    @PostMapping("xiaohongshu/image/download")
    public R<SysFile> downloadXiaohongshuImage(
            @RequestParam String url,
            @RequestParam(value = "cookie", required = false) String cookie) {
        try {
            if (StringUtils.isBlank(url)) {
                return R.fail("图片URL不能为空");
            }

            // URL解码
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            log.info("收到图片下载请求: {}", decodedUrl);

            // 验证URL是否为小红书图片URL
            if (!decodedUrl.contains("xhscdn.com") && !decodedUrl.contains("xiaohongshu.com")) {
                log.warn("非小红书图片URL，拒绝下载: {}", decodedUrl);
                return R.fail("仅支持小红书图片URL");
            }

            // 下载图片到临时文件
            log.info("开始下载图片到临时文件...");
            File imageFile = xiaohongshuDownloadService.downloadImageToFile(decodedUrl, cookie);
            log.info("图片下载完成，临时文件: {}, 大小: {} bytes", imageFile.getAbsolutePath(), imageFile.length());

            try {
                // 上传到OSS
                log.info("开始上传图片到OSS...");
                String ossUrl = sysFileService.uploadFile(imageFile);
                log.info("图片上传到OSS成功，URL: {}", ossUrl);

                SysFile sysFile = new SysFile();
                sysFile.setName(FileUtils.getName(ossUrl));
                sysFile.setUrl(ossUrl);
                return R.ok(sysFile);
            } finally {
                // 删除临时文件
                try {
                    if (imageFile != null && imageFile.exists()) {
                        boolean deleted = imageFile.delete();
                        if (deleted) {
                            log.info("临时文件已删除: {}", imageFile.getAbsolutePath());
                        } else {
                            log.warn("临时文件删除失败: {}", imageFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    log.warn("删除临时文件时出错", e);
                }
            }

        } catch (Exception e) {
            log.error("下载图片失败", e);
            return R.fail("下载图片失败: " + e.getMessage());
        }
    }
}
