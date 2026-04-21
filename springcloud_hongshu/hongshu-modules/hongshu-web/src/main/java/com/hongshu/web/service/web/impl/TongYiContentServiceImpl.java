package com.hongshu.web.service.web.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.web.domain.dto.TongYiContentRequestDTO;
import com.hongshu.web.domain.dto.TongYiContentResponseDTO;
import com.hongshu.web.domain.entity.WebSystemConfig;
import com.hongshu.web.mapper.sys.SysSystemConfigMapper;
import com.hongshu.web.service.IOssUploadService;
import com.hongshu.web.service.web.ITongYiContentService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 通义千问内容生成服务实现
 * 支持图片和视频URL输入，生成小红书风格的标题和内容
 *
 * @author hongshu
 */
@Slf4j
@Service
public class TongYiContentServiceImpl implements ITongYiContentService {

    @Autowired
    private SysSystemConfigMapper systemConfigMapper;
    @Autowired
    private IOssUploadService ossUploadService;

    /**
     * 通义千问视觉模型（支持图片和视频理解）
     */
    private static final String QWEN_VL_MODEL = "qwen-vl-max";
    /**
     * DashScope API 基础URL（兼容OpenAI格式）
     */
    private static final String DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";


    /**
     * HTTP客户端
     */
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 获取通义千问API Key
     * 优先从系统配置表获取，如果没有则从 gpt_openkey 表获取
     */
    private String getTongYiApiKey() {
        try {
            // 方案1：从系统配置表查询
            WebSystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<WebSystemConfig>()
                            .eq(WebSystemConfig::getConfigKey, "tongyi.api.key")
            );

            if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                log.debug("从系统配置表获取到通义千问API Key");
                return config.getConfigValue();
            }

            // 方案2：从 gpt_openkey 表查询（备用方案）
            try {
                String apiKey = systemConfigMapper.selectTongYiApiKeyFromOpenkey();
                if (apiKey != null && !apiKey.isEmpty()) {
                    log.debug("从 gpt_openkey 表获取到通义千问API Key");
                    return apiKey;
                }
            } catch (Exception e) {
                log.debug("从 gpt_openkey 表获取API Key失败（可能表不存在或没有配置）: {}", e.getMessage());
            }

            log.warn("通义千问API Key未配置，请检查：");
            log.warn("1. 系统配置表（web_system_config）中是否配置了 tongyi.api.key");
            log.warn("2. 或者 gpt_openkey 表中是否有 model='QIANWEN' 且 status=1 的记录");
            return null;
        } catch (Exception e) {
            log.error("获取通义千问API Key失败", e);
            return null;
        }
    }

    @Override
    public TongYiContentResponseDTO generateContent(TongYiContentRequestDTO request) {
        try {
            String apiKey = getTongYiApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("通义千问服务未配置或未启用，请在系统配置中设置 tongyi.api.key")
                        .requestId(UUID.randomUUID().toString())
                        .build();
            }

            // 检查是否有图片或视频
            List<String> imageUrls = request.getImageUrls();
            String videoUrl = request.getVideoUrl();

            if ((imageUrls == null || imageUrls.isEmpty()) && (videoUrl == null || videoUrl.isEmpty())) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("请至少提供一张图片或一个视频")
                        .requestId(UUID.randomUUID().toString())
                        .build();
            }

            if (request.getTitleOnly() != null && request.getTitleOnly()) {
                return generateTitles(imageUrls, videoUrl, request, apiKey);
            } else {
                return generateContentWithMedia(imageUrls, videoUrl, request, apiKey);
            }

        } catch (Exception e) {
            log.error("调用通义千问内容生成接口异常", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("生成文案失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }

    @Override
    public TongYiContentResponseDTO generateContentFromFiles(
            MultipartFile[] imageFiles,
            String category,
            String productName,
            String productDescription,
            String style) {
        try {
            List<String> imageUrls = ossUploadService.uploadBatch(imageFiles);

            TongYiContentRequestDTO request = TongYiContentRequestDTO.builder()
                    .imageUrls(imageUrls)
                    .category(category)
                    .productName(productName)
                    .productDescription(productDescription)
                    .style(style != null ? style : "小红书笔记")
                    .build();

            return generateContent(request);
        } catch (Exception e) {
            log.error("上传图片并生成文案失败", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("上传图片失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }

    @Override
    public TongYiContentResponseDTO generateContentFromVideo(
            MultipartFile videoFile,
            String category,
            String productName,
            String productDescription,
            String style) {
        try {
            String videoUrl = ossUploadService.upload(videoFile);

            TongYiContentRequestDTO request = TongYiContentRequestDTO.builder()
                    .videoUrl(videoUrl)
                    .category(category)
                    .productName(productName)
                    .productDescription(productDescription)
                    .style(style != null ? style : "小红书笔记")
                    .build();

            return generateContent(request);
        } catch (Exception e) {
            log.error("上传视频并生成文案失败", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("上传视频失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }

    @Override
    public TongYiContentResponseDTO generateTitlesOnly(TongYiContentRequestDTO request) {
        request.setTitleOnly(true);
        return generateContent(request);
    }

    /**
     * 生成内容（包含标题和正文）
     * 支持图片和视频URL
     */
    /**
     * 生成内容（包含标题和正文）
     * 支持图片和视频URL，使用HTTP直接调用，使用正确的content数组格式
     */
    private TongYiContentResponseDTO generateContentWithMedia(
            List<String> imageUrls,
            String videoUrl,
            TongYiContentRequestDTO request,
            String apiKey) {
        try {
            // 构建提示词
            StringBuilder prompt = new StringBuilder();

            // 根据内容类型区分提示词
            String contentType = request.getContentType();
            boolean isIdle = "idle".equalsIgnoreCase(contentType);

            if (isIdle) {
                // 闲置商品的提示词
                prompt.append("请根据以下");
                if (imageUrls != null && !imageUrls.isEmpty() && videoUrl != null && !videoUrl.isEmpty()) {
                    prompt.append("图片和视频");
                } else if (imageUrls != null && !imageUrls.isEmpty()) {
                    prompt.append("图片");
                } else {
                    prompt.append("视频");
                }
                prompt.append("，生成一篇小红书风格的闲置商品描述。");

                if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                    prompt.append("商品类别：").append(request.getCategory()).append("。");
                }
                if (request.getProductName() != null && !request.getProductName().isEmpty()) {
                    prompt.append("商品名称：").append(request.getProductName()).append("。");
                }
                if (request.getProductDescription() != null && !request.getProductDescription().isEmpty()) {
                    prompt.append("商品描述：").append(request.getProductDescription()).append("。");
                }

                prompt.append("\n\n要求：");
                prompt.append("\n1. 生成一个吸引人的商品标题（10-20字，突出商品特点和优势，不要包含emoji）");
                prompt.append("\n2. 生成商品描述正文（100-200字，包含以下内容：");
                prompt.append("\n   - 商品的基本信息和特点");
                prompt.append("\n   - 商品的使用情况和新旧程度");
                prompt.append("\n   - 商品的优势和卖点");
                prompt.append("\n   - 适合的人群或使用场景");
                prompt.append("\n   使用小红书风格，可以适当使用emoji，语言要真实自然，突出商品价值）");
                prompt.append("\n3. 格式：标题和正文用换行分隔");
            } else {
                // 笔记的提示词（默认）
                prompt.append("请根据以下");
                if (imageUrls != null && !imageUrls.isEmpty() && videoUrl != null && !videoUrl.isEmpty()) {
                    prompt.append("图片和视频");
                } else if (imageUrls != null && !imageUrls.isEmpty()) {
                    prompt.append("图片");
                } else {
                    prompt.append("视频");
                }
                prompt.append("，生成一篇小红书风格的笔记。");

                if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                    prompt.append("类别：").append(request.getCategory()).append("。");
                }
                if (request.getProductName() != null && !request.getProductName().isEmpty()) {
                    prompt.append("产品名称：").append(request.getProductName()).append("。");
                }
                if (request.getProductDescription() != null && !request.getProductDescription().isEmpty()) {
                    prompt.append("产品描述：").append(request.getProductDescription()).append("。");
                }

                prompt.append("\n\n要求：");
                prompt.append("\n1. 生成一个吸引人的标题（10-20字，不要包含emoji）");
                prompt.append("\n2. 生成正文内容（100-200字，小红书风格，可以适当使用emoji，内容要生动有趣，有个人感受和体验）");
                prompt.append("\n3. 格式：标题和正文用换行分隔");
            }

            // 构建content数组（通义千问VL模型需要使用content数组格式）
            JSONArray contentArray = new JSONArray();

            // 添加文本内容
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", prompt.toString());
            contentArray.add(textContent);

            // 添加图片URL（使用image_url格式）
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (int i = 0; i < imageUrls.size() && i < 4; i++) { // 最多4张图片
                    String imageUrl = imageUrls.get(i);
                    if (imageUrl != null && !imageUrl.isEmpty() &&
                            (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                        JSONObject imageContent = new JSONObject();
                        imageContent.put("type", "image_url");
                        JSONObject imageUrlObj = new JSONObject();
                        imageUrlObj.put("url", imageUrl);
                        imageContent.put("image_url", imageUrlObj);
                        contentArray.add(imageContent);
                        log.debug("添加图片URL: {}", imageUrl);
                    }
                }
            }

            // 添加视频URL（使用video_url格式）
            if (videoUrl != null && !videoUrl.isEmpty() &&
                    (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                JSONObject videoContent = new JSONObject();
                videoContent.put("type", "video_url");
                JSONObject videoUrlObj = new JSONObject();
                videoUrlObj.put("url", videoUrl);
                videoContent.put("video_url", videoUrlObj);
                contentArray.add(videoContent);
                log.debug("添加视频URL: {}", videoUrl);
            }

            // 构建请求体
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", contentArray);

            JSONArray messages = new JSONArray();
            messages.add(message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", QWEN_VL_MODEL);
            requestBody.put("messages", messages);

            String requestBodyStr = requestBody.toJSONString();
            log.debug("请求体: {}", requestBodyStr);

            // 发送HTTP请求
            RequestBody body = RequestBody.create(requestBodyStr, MediaType.parse("application/json; charset=utf-8"));
            Request httpRequest = new Request.Builder()
                    .url(DASHSCOPE_BASE_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            log.info("调用通义千问视觉模型生成内容，模型: {}", QWEN_VL_MODEL);
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("HTTP请求失败: code={}, body={}", response.code(), errorBody);
                    return TongYiContentResponseDTO.builder()
                            .success(false)
                            .errorMessage("API调用失败: " + errorBody)
                            .requestId(UUID.randomUUID().toString())
                            .build();
                }

                String responseBody = response.body().string();
                log.debug("API响应: {}", responseBody);

                return parseHttpResponse(responseBody);
            }

        } catch (IOException e) {
            log.error("生成内容失败", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("生成内容失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }

    /**
     * 只生成标题
     * 支持图片和视频URL，使用HTTP直接调用
     */
    private TongYiContentResponseDTO generateTitles(
            List<String> imageUrls,
            String videoUrl,
            TongYiContentRequestDTO request,
            String apiKey) {
        try {
            // 构建提示词
            StringBuilder prompt = new StringBuilder();
            prompt.append("请根据以下");

            if (imageUrls != null && !imageUrls.isEmpty() && videoUrl != null && !videoUrl.isEmpty()) {
                prompt.append("图片和视频");
            } else if (imageUrls != null && !imageUrls.isEmpty()) {
                prompt.append("图片");
            } else {
                prompt.append("视频");
            }

            prompt.append("，生成3-5个小红书风格的标题。");

            if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                prompt.append("类别：").append(request.getCategory()).append("。");
            }
            if (request.getProductName() != null && !request.getProductName().isEmpty()) {
                prompt.append("产品名称：").append(request.getProductName()).append("。");
            }

            prompt.append("\n\n要求：");
            prompt.append("\n1. 每个标题10-20字");
            prompt.append("\n2. 标题要吸引人，符合小红书风格");
            prompt.append("\n3. 用换行分隔每个标题");

            // 构建content数组
            JSONArray contentArray = new JSONArray();

            // 添加文本内容
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", prompt.toString());
            contentArray.add(textContent);

            // 添加图片URL
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (int i = 0; i < imageUrls.size() && i < 4; i++) {
                    String imageUrl = imageUrls.get(i);
                    if (imageUrl != null && !imageUrl.isEmpty() &&
                            (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                        JSONObject imageContent = new JSONObject();
                        imageContent.put("type", "image_url");
                        JSONObject imageUrlObj = new JSONObject();
                        imageUrlObj.put("url", imageUrl);
                        imageContent.put("image_url", imageUrlObj);
                        contentArray.add(imageContent);
                        log.debug("添加图片URL: {}", imageUrl);
                    }
                }
            }

            // 添加视频URL
            if (videoUrl != null && !videoUrl.isEmpty() &&
                    (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                JSONObject videoContent = new JSONObject();
                videoContent.put("type", "video_url");
                JSONObject videoUrlObj = new JSONObject();
                videoUrlObj.put("url", videoUrl);
                videoContent.put("video_url", videoUrlObj);
                contentArray.add(videoContent);
                log.debug("添加视频URL: {}", videoUrl);
            }

            // 构建请求体
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", contentArray);

            JSONArray messages = new JSONArray();
            messages.add(message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", QWEN_VL_MODEL);
            requestBody.put("messages", messages);

            String requestBodyStr = requestBody.toJSONString();
            log.debug("请求体: {}", requestBodyStr);

            // 发送HTTP请求
            RequestBody body = RequestBody.create(requestBodyStr, MediaType.parse("application/json; charset=utf-8"));
            Request httpRequest = new Request.Builder()
                    .url(DASHSCOPE_BASE_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            log.info("调用通义千问视觉模型生成标题，模型: {}", QWEN_VL_MODEL);
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("HTTP请求失败: code={}, body={}", response.code(), errorBody);
                    return TongYiContentResponseDTO.builder()
                            .success(false)
                            .errorMessage("API调用失败: " + errorBody)
                            .requestId(UUID.randomUUID().toString())
                            .build();
                }

                String responseBody = response.body().string();
                log.debug("API响应: {}", responseBody);

                return parseTitlesHttpResponse(responseBody);
            }

        } catch (IOException e) {
            log.error("生成标题失败", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("生成标题失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }

    /**
     * 解析HTTP响应（生成内容）
     */
    private TongYiContentResponseDTO parseHttpResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);

            if (jsonResponse == null) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("API响应为空")
                        .requestId(UUID.randomUUID().toString())
                        .build();
            }

            // 检查是否有错误
            if (jsonResponse.containsKey("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                String errorMessage = error.getString("message");
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("API错误: " + errorMessage)
                        .requestId(jsonResponse.getString("id"))
                        .build();
            }

            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("API响应中没有生成内容")
                        .requestId(jsonResponse.getString("id"))
                        .build();
            }

            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            if (content == null || content.trim().isEmpty()) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("生成的内容为空")
                        .requestId(jsonResponse.getString("id"))
                        .build();
            }

            // 解析标题和正文
            String[] lines = content.split("\n");
            List<String> titles = new ArrayList<>();
            StringBuilder contentBuilder = new StringBuilder();
            boolean isTitle = true;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (isTitle && line.length() <= 30 && !line.contains("正文") && !line.contains("内容")) {
                    titles.add(line);
                    isTitle = false;
                } else {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append("\n");
                    }
                    contentBuilder.append(line);
                }
            }

            // 如果没有提取到标题，使用第一行作为标题
            if (titles.isEmpty() && contentBuilder.length() > 0) {
                String firstLine = contentBuilder.toString().split("\n")[0];
                if (firstLine.length() > 30) {
                    firstLine = firstLine.substring(0, 30) + "...";
                }
                titles.add(firstLine);
            }

            return TongYiContentResponseDTO.builder()
                    .success(true)
                    .titles(titles)
                    .content(contentBuilder.toString())
                    .requestId(jsonResponse.getString("id"))
                    .build();

        } catch (Exception e) {
            log.error("解析内容响应失败", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("解析响应失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }

    /**
     * 解析HTTP响应（只生成标题）
     */
    private TongYiContentResponseDTO parseTitlesHttpResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);

            if (jsonResponse == null) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("API响应为空")
                        .requestId(UUID.randomUUID().toString())
                        .build();
            }

            // 检查是否有错误
            if (jsonResponse.containsKey("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                String errorMessage = error.getString("message");
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("API错误: " + errorMessage)
                        .requestId(jsonResponse.getString("id"))
                        .build();
            }

            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("API响应中没有生成内容")
                        .requestId(jsonResponse.getString("id"))
                        .build();
            }

            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            if (content == null || content.trim().isEmpty()) {
                return TongYiContentResponseDTO.builder()
                        .success(false)
                        .errorMessage("生成的标题为空")
                        .requestId(jsonResponse.getString("id"))
                        .build();
            }

            List<String> titles = new ArrayList<>();
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                // 移除序号和符号
                line = line.replaceAll("^[0-9]+\\.\\s*", "")
                        .replaceAll("^[•·]\\s*", "")
                        .replaceAll("^-\\s*", "");
                if (!line.isEmpty() && line.length() <= 50) {
                    titles.add(line);
                }
            }

            return TongYiContentResponseDTO.builder()
                    .success(true)
                    .titles(titles)
                    .requestId(jsonResponse.getString("id"))
                    .build();

        } catch (Exception e) {
            log.error("解析标题响应失败", e);
            return TongYiContentResponseDTO.builder()
                    .success(false)
                    .errorMessage("解析响应失败: " + e.getMessage())
                    .requestId(UUID.randomUUID().toString())
                    .build();
        }
    }
}

