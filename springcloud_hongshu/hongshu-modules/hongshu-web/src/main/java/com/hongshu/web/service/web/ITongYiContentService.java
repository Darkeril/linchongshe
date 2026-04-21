package com.hongshu.web.service.web;

import com.hongshu.web.domain.dto.TongYiContentRequestDTO;
import com.hongshu.web.domain.dto.TongYiContentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 通义千问内容生成服务接口
 *
 * @author hongshu
 */
public interface ITongYiContentService {

    /**
     * 根据图片和视频URL生成内容
     *
     * @param request 请求参数
     * @return 生成的内容
     */
    TongYiContentResponseDTO generateContent(TongYiContentRequestDTO request);

    /**
     * 根据上传的图片文件生成内容
     *
     * @param imageFiles 图片文件数组
     * @param category 类别
     * @param productName 产品名称
     * @param productDescription 产品描述
     * @param style 风格
     * @return 生成的内容
     */
    TongYiContentResponseDTO generateContentFromFiles(
            MultipartFile[] imageFiles,
            String category,
            String productName,
            String productDescription,
            String style);

    /**
     * 根据上传的视频文件生成内容
     *
     * @param videoFile 视频文件
     * @param category 类别
     * @param productName 产品名称
     * @param productDescription 产品描述
     * @param style 风格
     * @return 生成的内容
     */
    TongYiContentResponseDTO generateContentFromVideo(
            MultipartFile videoFile,
            String category,
            String productName,
            String productDescription,
            String style);

    /**
     * 只生成标题
     *
     * @param request 请求参数
     * @return 生成的标题列表
     */
    TongYiContentResponseDTO generateTitlesOnly(TongYiContentRequestDTO request);
}

