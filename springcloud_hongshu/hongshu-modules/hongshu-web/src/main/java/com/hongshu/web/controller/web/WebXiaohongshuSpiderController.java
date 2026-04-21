package com.hongshu.web.controller.web;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.domain.dto.XiaohongshuSpiderDTO;
import com.hongshu.web.domain.vo.XiaohongshuItemVO;
import com.hongshu.web.service.IOssUploadService;
import com.hongshu.web.service.web.XiaohongshuSpiderService;
import com.hongshu.web.service.web.XiaohongshuVideoProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

/**
 * 小红书爬虫控制器
 *
 * @author: hongshu
 */
@Slf4j
@RestController
@RequestMapping("/spider")
public class WebXiaohongshuSpiderController {

    @Autowired
    private XiaohongshuSpiderService spiderService;

    @Autowired
    private XiaohongshuVideoProxyService videoProxyService;

    @Autowired
    private IOssUploadService ossUploadService;

    /**
     * 爬取小红书内容
     *
     * @param dto 爬取参数
     * @return 爬取结果
     */
    @PostMapping("/xiaohongshu")
    public Result<?> spiderXiaohongshu(@RequestBody XiaohongshuSpiderDTO dto) {
        try {
            // 参数验证
            if (StringUtils.isBlank(dto.getKeyword())) {
                return Result.fail("关键词不能为空");
            }

            // 设置默认值
            if (dto.getType() == null || dto.getType().isEmpty()) {
                dto.setType("note"); // 默认爬取笔记
            }
            if (dto.getSortType() == null) {
                dto.setSortType(0); // 默认综合排序
            }
            if (dto.getNoteTime() == null) {
                dto.setNoteTime(0); // 默认不限时间
            }
            if (dto.getPage() == null || dto.getPage() < 1) {
                dto.setPage(1);
            }
            if (dto.getPageSize() == null || dto.getPageSize() < 1) {
                dto.setPageSize(10); // 默认每页10条
            }

            // 验证类型
            if (!"image".equals(dto.getType()) &&
                    !"video".equals(dto.getType()) &&
                    !"note".equals(dto.getType())) {
                return Result.fail("类型参数错误，只能是：image、video、note");
            }

            log.info("收到小红书爬取请求 - 关键词: {}, 类型: {}, 排序方式: {}, 笔记时间: {}, 页码: {}, 每页数量: {}, 是否使用Cookie: {}",
                    dto.getKeyword(), dto.getType(), dto.getSortType(), dto.getNoteTime(), 
                    dto.getPage(), dto.getPageSize(), 
                    dto.getCookie() != null && !dto.getCookie().isEmpty());

            // 调用爬虫服务
            List<XiaohongshuItemVO> items = spiderService.spiderByKeyword(
                    dto.getKeyword(),
                    dto.getType(),
                    dto.getSortType(),
                    dto.getNoteTime(),
                    dto.getPage(),
                    dto.getPageSize(),
                    dto.getCookie()
            );

            log.info("爬取完成，返回 {} 条结果", items.size());

            return Result.ok(items);

        } catch (Exception e) {
            log.error("爬取小红书失败", e);
            return Result.fail("爬取失败: " + e.getMessage());
        }
    }

    /**
     * 下载视频到服务器并上传到OSS
     *
     * @param url 视频URL（需要URL编码）
     * @param cookie Cookie字符串（可选，用于认证）
     * @return OSS访问链接
     */
    @PostMapping("/xiaohongshu/video/download")
    public Result<?> downloadVideo(
            @RequestParam String url,
            @RequestParam(value = "cookie", required = false) String cookie) {
        try {
            if (StringUtils.isBlank(url)) {
                return Result.fail("视频URL不能为空");
            }

            // URL解码
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            log.info("收到视频下载请求: {}", decodedUrl);

            // 验证URL是否为小红书视频URL
            if (!decodedUrl.contains("xhscdn.com") && !decodedUrl.contains("xiaohongshu.com")) {
                log.warn("非小红书视频URL，拒绝下载: {}", decodedUrl);
                return Result.fail("仅支持小红书视频URL");
            }

            // 下载视频到临时文件
            log.info("开始下载视频到临时文件...");
            File videoFile = videoProxyService.downloadVideoToFile(decodedUrl, cookie);
            log.info("视频下载完成，临时文件: {}, 大小: {} bytes", videoFile.getAbsolutePath(), videoFile.length());

            try {
                // 上传到OSS
                log.info("开始上传视频到OSS...");
                String ossUrl = ossUploadService.upload(videoFile);
                log.info("视频上传到OSS成功，URL: {}", ossUrl);

                return Result.ok(ossUrl);
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
            return Result.fail("下载视频失败: " + e.getMessage());
        }
    }

    /**
     * 下载图片到服务器并上传到OSS
     *
     * @param url 图片URL（需要URL编码）
     * @param cookie Cookie字符串（可选，用于认证）
     * @return OSS访问链接
     */
    @PostMapping("/xiaohongshu/image/download")
    public Result<?> downloadImage(
            @RequestParam String url,
            @RequestParam(value = "cookie", required = false) String cookie) {
        try {
            if (StringUtils.isBlank(url)) {
                return Result.fail("图片URL不能为空");
            }

            // URL解码
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            log.info("收到图片下载请求: {}", decodedUrl);

            // 验证URL是否为小红书图片URL
            if (!decodedUrl.contains("xhscdn.com") && !decodedUrl.contains("xiaohongshu.com")) {
                log.warn("非小红书图片URL，拒绝下载: {}", decodedUrl);
                return Result.fail("仅支持小红书图片URL");
            }

            // 下载图片到临时文件
            log.info("开始下载图片到临时文件...");
            File imageFile = videoProxyService.downloadImageToFile(decodedUrl, cookie);
            log.info("图片下载完成，临时文件: {}, 大小: {} bytes", imageFile.getAbsolutePath(), imageFile.length());

            try {
                // 上传到OSS
                log.info("开始上传图片到OSS...");
                String ossUrl = ossUploadService.upload(imageFile);
                log.info("图片上传到OSS成功，URL: {}", ossUrl);

                return Result.ok(ossUrl);
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
            return Result.fail("下载图片失败: " + e.getMessage());
        }
    }
}
