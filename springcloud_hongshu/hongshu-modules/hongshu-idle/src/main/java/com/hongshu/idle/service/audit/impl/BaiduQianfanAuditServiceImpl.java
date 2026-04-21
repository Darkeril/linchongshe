package com.hongshu.idle.service.audit.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import com.hongshu.common.audit.service.ContentAuditService;
import com.hongshu.idle.domain.dto.SystemConfigDTO;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 百度千帆内容审核服务实现
 *
 * @author hongshu
 */
@Service("baiduQianfanAuditService")
@Slf4j
public class BaiduQianfanAuditServiceImpl implements ContentAuditService {
    
    @Autowired
    private ISysSystemConfigService systemConfigService;
    
    @Override
    public AutoAuditResult auditText(String content) {
        try {
            SystemConfigDTO config = systemConfigService.getSystemConfig();
            log.info("百度千帆文本审核配置 - enabled: {}, accessKey: {}", config.getBaiduQianfanEnabled(), config.getBaiduQianfanAccessKey());
            if (!config.getBaiduQianfanEnabled()) {
                return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.MANUAL)
                    .reason("百度千帆审核未开启")
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();
            }
            
            // 调用百度千帆文本审核API
            TextAuditResponse response = callBaiduTextAudit(content, config);
            
            return parseTextAuditResponse(response, config.getAutoAuditThreshold());
            
        } catch (Exception e) {
            log.error("百度千帆文本审核异常", e);
            return AutoAuditResult.builder()
                .result(AutoAuditResultEnum.ERROR)
                .reason("审核服务异常: " + e.getMessage())
                .auditTime(new Date())
                .provider("baidu_qianfan")
                .build();
        }
    }
    
    /**
     * 审核单张图片（内部方法）
     */
    private AutoAuditResult auditImage(String imageUrl) {
        try {
            SystemConfigDTO config = systemConfigService.getSystemConfig();
            log.info("百度千帆图片审核配置 - enabled: {}, imageUrl: {}", config.getBaiduQianfanEnabled(), imageUrl);
            if (!config.getBaiduQianfanEnabled()) {
                return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.MANUAL)
                    .reason("百度千帆审核未开启")
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();
            }
            
            // 调用百度千帆图片审核API
            ImageAuditResponse response = callBaiduImageAudit(imageUrl, config);
            
            return parseImageAuditResponse(response, config.getAutoAuditThreshold());
            
        } catch (Exception e) {
            log.error("百度千帆图片审核异常", e);
            return AutoAuditResult.builder()
                .result(AutoAuditResultEnum.ERROR)
                .reason("审核服务异常: " + e.getMessage())
                .auditTime(new Date())
                .provider("baidu_qianfan")
                .build();
        }
    }
    
    @Override
    public AutoAuditResult auditImages(List<String> imageUrls) {
        try {
            SystemConfigDTO config = systemConfigService.getSystemConfig();
            if (!config.getBaiduQianfanEnabled()) {
                return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.MANUAL)
                    .reason("百度千帆审核未开启")
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();
            }
            
            if (imageUrls == null || imageUrls.isEmpty()) {
                return AutoAuditResult.pass("无图片资源", "baidu_qianfan");
            }
            
            // 审核所有图片
            for (String imageUrl : imageUrls) {
                AutoAuditResult result = auditImage(imageUrl);
                if (result.getResult() == AutoAuditResultEnum.REJECT) {
                    return result; // 任一图片违规则整体拒绝
                }
                if (result.getResult() == AutoAuditResultEnum.MANUAL) {
                    return result; // 需要人工审核
                }
            }
            
            return AutoAuditResult.pass("图片审核通过", "baidu_qianfan");
            
        } catch (Exception e) {
            log.error("百度千帆图片批量审核异常", e);
            return AutoAuditResult.error("图片审核异常: " + e.getMessage(), "baidu_qianfan");
        }
    }
    
    @Override
    public AutoAuditResult auditContent(String text, List<String> imageUrls) {
        try {
            log.info("开始图文审核 - 文本长度: {}, 图片数量: {}", text != null ? text.length() : 0, imageUrls != null ? imageUrls.size() : 0);
            
            // 文本审核
            AutoAuditResult textResult = auditText(text);
            if (textResult == null) {
                log.error("文本审核返回null");
                return AutoAuditResult.error("文本审核返回null", "baidu_qianfan");
            }
            
            if (textResult.getResult() == AutoAuditResultEnum.REJECT) {
                return textResult; // 文本违规直接返回
            }
            
            // 图片审核
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    AutoAuditResult imageResult = auditImage(imageUrl);
                    if (imageResult == null) {
                        log.error("图片审核返回null - imageUrl: {}", imageUrl);
                        continue;
                    }
                    
                    if (imageResult.getResult() == AutoAuditResultEnum.REJECT) {
                        return imageResult; // 任一图片违规则整体拒绝
                    }
                    if (imageResult.getResult() == AutoAuditResultEnum.MANUAL) {
                        textResult = imageResult; // 需要人工审核
                    }
                }
            }
            
            return textResult;
        } catch (Exception e) {
            log.error("图文审核异常", e);
            return AutoAuditResult.error("图文审核异常: " + e.getMessage(), "baidu_qianfan");
        }
    }

    @Override
    public AutoAuditResult auditVideo(String textContext, List<String> videoUrls) {
        try {
            SystemConfigDTO config = systemConfigService.getSystemConfig();
            if (!config.getBaiduQianfanEnabled()) {
                return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.MANUAL)
                    .reason("百度千帆审核未开启")
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();
            }

            if (videoUrls == null || videoUrls.isEmpty()) {
                // 没有视频资源，直接返回通过
                return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.PASS)
                    .reason("无视频资源")
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();
            }

            AutoAuditResult finalResult = AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.PASS)
                    .reason("文本和视频内容合规")
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();

            // 1. 先对文本内容进行审核（如果有文本）
            if (textContext != null && !textContext.trim().isEmpty()) {
                TextAuditResponse textResponse = callBaiduTextAudit(textContext, config);
                AutoAuditResult textResult = parseTextAuditResponse(textResponse, config.getAutoAuditThreshold());

                if (textResult.getResult() == AutoAuditResultEnum.REJECT) {
                    return textResult; // 文本直接不合规，整体拒绝
                }
                if (textResult.getResult() == AutoAuditResultEnum.MANUAL) {
                    finalResult = textResult; // 文本需要人工，则整体至少人工审核
                }
            }

            // 2. 再对视频资源逐个进行审核
            for (String videoUrl : videoUrls) {
                VideoAuditResponse response = callBaiduVideoAudit(videoUrl, textContext, config);
                AutoAuditResult videoResult = parseVideoAuditResponse(response, config.getAutoAuditThreshold());

                if (videoResult.getResult() == AutoAuditResultEnum.REJECT) {
                    return videoResult; // 任一视频违规则整体拒绝
                }
                if (videoResult.getResult() == AutoAuditResultEnum.MANUAL) {
                    finalResult = videoResult; // 有视频需要人工，则整体至少人工审核
                }
            }

            return finalResult;
        } catch (Exception e) {
            log.error("百度千帆视频审核异常", e);
            return AutoAuditResult.builder()
                    .result(AutoAuditResultEnum.ERROR)
                    .reason("审核服务异常: " + e.getMessage())
                    .auditTime(new Date())
                    .provider("baidu_qianfan")
                    .build();
        }
    }
    
    /**
     * 调用百度千帆文本审核API
     */
    private TextAuditResponse callBaiduTextAudit(String content, SystemConfigDTO config) {
        TextAuditResponse response = new TextAuditResponse();
        try {
            if (config.getBaiduQianfanTextEndpoint() == null || config.getBaiduQianfanTextEndpoint().isEmpty()) {
                response.setConclusion("疑似");
                response.setConfidenceScore(0.0);
                response.setLogId("no_text_endpoint");
                return response;
            }

            String accessToken = getAccessToken(config.getBaiduQianfanAccessKey(), config.getBaiduQianfanSecretKey());
            if (accessToken == null) {
                response.setConclusion("疑似");
                response.setConfidenceScore(0.0);
                response.setLogId("get_token_failed");
                return response;
            }

            String requestUrl = config.getBaiduQianfanTextEndpoint() + "?access_token=" + accessToken;

            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append("text=")
                    .append(URLEncoder.encode(content == null ? "" : content, StandardCharsets.UTF_8.name()));

            byte[] postData = paramBuilder.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(requestUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                try (OutputStream out = conn.getOutputStream()) {
                    out.write(postData);
                    out.flush();
                }

                int httpCode = conn.getResponseCode();
                InputStream inputStream = (httpCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                String resultJson = readStream(inputStream);
                log.info("Baidu text audit response: {}", resultJson);

                if (resultJson == null || resultJson.isEmpty()) {
                    response.setConclusion("疑似");
                    response.setConfidenceScore(0.0);
                    response.setLogId("empty_response");
                    return response;
                }

                JSONObject root = JSON.parseObject(resultJson);
                response.setLogId(root.getString("log_id"));

                String conclusion = root.getString("conclusion");
                if (conclusion == null) {
                    conclusion = "疑似";
                }
                response.setConclusion(conclusion);

                Double confidence = 0.0;
                JSONArray dataArr = root.getJSONArray("data");
                if (dataArr != null && !dataArr.isEmpty()) {
                    JSONObject first = dataArr.getJSONObject(0);
                    if (first != null && first.get("confidence") != null) {
                        confidence = first.getDouble("confidence");
                    }
                }
                response.setConfidenceScore(confidence);

                return response;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } catch (Exception e) {
            log.error("调用百度文本审核接口异常", e);
            response.setConclusion("疑似");
            response.setConfidenceScore(0.0);
            response.setLogId("exception_" + System.currentTimeMillis());
            return response;
        }
    }
    
    /**
     * 调用百度千帆图片审核API
     */
    private ImageAuditResponse callBaiduImageAudit(String imageUrl, SystemConfigDTO config) {
        ImageAuditResponse response = new ImageAuditResponse();
        try {
            if (config.getBaiduQianfanImageEndpoint() == null || config.getBaiduQianfanImageEndpoint().isEmpty()) {
                response.setConclusion("疑似");
                response.setConfidenceScore(0.0);
                response.setLogId("no_image_endpoint");
                return response;
            }

            String accessToken = getAccessToken(config.getBaiduQianfanAccessKey(), config.getBaiduQianfanSecretKey());
            if (accessToken == null) {
                response.setConclusion("疑似");
                response.setConfidenceScore(0.0);
                response.setLogId("get_token_failed");
                return response;
            }

            String requestUrl = config.getBaiduQianfanImageEndpoint() + "?access_token=" + accessToken;

            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append("imgUrl=")
                    .append(URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.name()));

            byte[] postData = paramBuilder.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(requestUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                try (OutputStream out = conn.getOutputStream()) {
                    out.write(postData);
                    out.flush();
                }

                int httpCode = conn.getResponseCode();
                InputStream inputStream = (httpCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                String resultJson = readStream(inputStream);
                log.info("Baidu image audit response: {}", resultJson);

                if (resultJson == null || resultJson.isEmpty()) {
                    response.setConclusion("疑似");
                    response.setConfidenceScore(0.0);
                    response.setLogId("empty_response");
                    return response;
                }

                JSONObject root = JSON.parseObject(resultJson);
                response.setLogId(root.getString("log_id"));

                String conclusion = root.getString("conclusion");
                if (conclusion == null) {
                    conclusion = "疑似";
                }
                response.setConclusion(conclusion);

                Double confidence = 0.0;
                JSONArray dataArr = root.getJSONArray("data");
                if (dataArr != null && !dataArr.isEmpty()) {
                    JSONObject first = dataArr.getJSONObject(0);
                    if (first != null && first.get("confidence") != null) {
                        confidence = first.getDouble("confidence");
                    }
                }
                response.setConfidenceScore(confidence);

                return response;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } catch (Exception e) {
            log.error("调用百度图片审核接口异常", e);
            response.setConclusion("疑似");
            response.setConfidenceScore(0.0);
            response.setLogId("exception_" + System.currentTimeMillis());
            return response;
        }
    }
    
    /**
     * 解析文本审核响应
     */
    private AutoAuditResult parseTextAuditResponse(TextAuditResponse response, Double threshold) {
        AutoAuditResultEnum result;
        List<String> violations = new ArrayList<>();
        
        if ("合规".equals(response.getConclusion())) {
            result = AutoAuditResultEnum.PASS;
        } else if ("不合规".equals(response.getConclusion())) {
            result = AutoAuditResultEnum.REJECT;
            violations.add("文本内容违规");
        } else {
            // 疑似违规，根据阈值判断
            if (response.getConfidenceScore() >= threshold) {
                result = AutoAuditResultEnum.REJECT;
                violations.add("疑似违规内容");
            } else {
                result = AutoAuditResultEnum.MANUAL;
            }
        }
        
        return AutoAuditResult.builder()
            .result(result)
            .score(response.getConfidenceScore())
            .reason(response.getConclusion())
            .violations(violations)
            .auditId(response.getLogId())
            .auditTime(new Date())
            .provider("baidu_qianfan")
            .build();
    }

    /**
     * 调用百度千帆视频审核API
     */
    private VideoAuditResponse callBaiduVideoAudit(String videoUrl, String textContext, SystemConfigDTO config) {
        VideoAuditResponse response = new VideoAuditResponse();
        try {
            if (config.getBaiduQianfanVideoEndpoint() == null || config.getBaiduQianfanVideoEndpoint().isEmpty()) {
                // 如果未配置视频审核地址，则直接认为需要人工审核
                response.setConclusion("疑似");
                response.setConfidenceScore(0.0);
                response.setLogId("no_video_endpoint");
                return response;
            }

            // 1. 获取access_token
            String accessToken = getAccessToken(config.getBaiduQianfanAccessKey(), config.getBaiduQianfanSecretKey());
            if (accessToken == null) {
                response.setConclusion("疑似");
                response.setConfidenceScore(0.0);
                response.setLogId("get_token_failed");
                return response;
            }

            // 2. 组装请求URL
            String requestUrl = config.getBaiduQianfanVideoEndpoint() + "?access_token=" + accessToken;

            // 3. 组装请求体：与官方示例保持一致，仅使用 extId + name + videoUrl
            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append("name=")
                    .append(URLEncoder.encode("视频审核", StandardCharsets.UTF_8.name()));
            paramBuilder.append("&extId=")
                    .append(URLEncoder.encode(String.valueOf(System.currentTimeMillis()), StandardCharsets.UTF_8.name()));
            paramBuilder.append("&videoUrl=")
                    .append(URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.name()));

            byte[] postData = paramBuilder.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(requestUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);  // 连接超时：10秒
                conn.setReadTimeout(30000);     // 读取超时：30秒（视频审核需要更长时间）
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                try (OutputStream out = conn.getOutputStream()) {
                    out.write(postData);
                    out.flush();
                }

                int httpCode = conn.getResponseCode();
                InputStream inputStream = (httpCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                String resultJson = readStream(inputStream);
                log.info("Baidu video audit response: {}", resultJson);

                if (resultJson == null || resultJson.isEmpty()) {
                    response.setConclusion("疑似");
                    response.setConfidenceScore(0.0);
                    response.setLogId("empty_response");
                    return response;
                }

                JSONObject root = JSON.parseObject(resultJson);
                response.setLogId(root.getString("log_id"));

                String conclusion = root.getString("conclusion");
                if (conclusion == null) {
                    conclusion = "疑似";
                }
                response.setConclusion(conclusion);

                // 取第一个结果的置信度作为参考分数
                Double confidence = 0.0;
                JSONArray dataArr = root.getJSONArray("data");
                if (dataArr != null && !dataArr.isEmpty()) {
                    JSONObject first = dataArr.getJSONObject(0);
                    if (first != null && first.get("confidence") != null) {
                        confidence = first.getDouble("confidence");
                    }
                }
                response.setConfidenceScore(confidence);

                return response;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } catch (Exception e) {
            log.error("调用百度短视频审核接口异常", e);
            response.setConclusion("疑似");
            response.setConfidenceScore(0.0);
            response.setLogId("exception_" + System.currentTimeMillis());
            return response;
        }
    }

    /**
     * 获取百度开放平台access_token
     */
    private String getAccessToken(String ak, String sk) throws IOException {
        if (ak == null || ak.isEmpty() || sk == null || sk.isEmpty()) {
            log.warn("Baidu Qianfan ak/sk not configured");
            return null;
        }
        String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token" +
                "?grant_type=client_credentials" +
                "&client_id=" + URLEncoder.encode(ak, StandardCharsets.UTF_8.name()) +
                "&client_secret=" + URLEncoder.encode(sk, StandardCharsets.UTF_8.name());

        HttpURLConnection conn = null;
        try {
            URL url = new URL(tokenUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            int httpCode = conn.getResponseCode();
            InputStream inputStream = (httpCode == HttpURLConnection.HTTP_OK)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String resultJson = readStream(inputStream);
            log.info("Baidu getAccessToken response: {}", resultJson);

            if (resultJson == null || resultJson.isEmpty()) {
                return null;
            }
            JSONObject obj = JSON.parseObject(resultJson);
            return obj.getString("access_token");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 读取HTTP响应流为字符串
     */
    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * 解析视频审核响应
     */
    private AutoAuditResult parseVideoAuditResponse(VideoAuditResponse response, Double threshold) {
        AutoAuditResultEnum result;
        List<String> violations = new ArrayList<>();

        if ("合规".equals(response.getConclusion())) {
            result = AutoAuditResultEnum.PASS;
        } else if ("不合规".equals(response.getConclusion())) {
            result = AutoAuditResultEnum.REJECT;
            violations.add("视频内容违规");
        } else {
            // 疑似违规，根据阈值判断
            if (response.getConfidenceScore() >= threshold) {
                result = AutoAuditResultEnum.REJECT;
                violations.add("疑似违规视频");
            } else {
                result = AutoAuditResultEnum.MANUAL;
            }
        }

        return AutoAuditResult.builder()
            .result(result)
            .score(response.getConfidenceScore())
            .reason(response.getConclusion())
            .violations(violations)
            .auditId(response.getLogId())
            .auditTime(new Date())
            .provider("baidu_qianfan")
            .build();
    }
    
    /**
     * 解析图片审核响应
     */
    private AutoAuditResult parseImageAuditResponse(ImageAuditResponse response, Double threshold) {
        AutoAuditResultEnum result;
        List<String> violations = new ArrayList<>();
        
        if ("合规".equals(response.getConclusion())) {
            result = AutoAuditResultEnum.PASS;
        } else if ("不合规".equals(response.getConclusion())) {
            result = AutoAuditResultEnum.REJECT;
            violations.add("图片内容违规");
        } else {
            // 疑似违规，根据阈值判断
            if (response.getConfidenceScore() >= threshold) {
                result = AutoAuditResultEnum.REJECT;
                violations.add("疑似违规图片");
            } else {
                result = AutoAuditResultEnum.MANUAL;
            }
        }
        
        return AutoAuditResult.builder()
            .result(result)
            .score(response.getConfidenceScore())
            .reason(response.getConclusion())
            .violations(violations)
            .auditId(response.getLogId())
            .auditTime(new Date())
            .provider("baidu_qianfan")
            .build();
    }
    
    /**
     * 文本审核响应实体（内部类）
     */
    private static class TextAuditResponse {
        private String conclusion;
        private Double confidenceScore;
        private String logId;
        
        // getters and setters
        public String getConclusion() { return conclusion; }
        public void setConclusion(String conclusion) { this.conclusion = conclusion; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }
    }
    
    /**
     * 图片审核响应实体（内部类）
     */
    private static class ImageAuditResponse {
        private String conclusion;
        private Double confidenceScore;
        private String logId;
        
        // getters and setters
        public String getConclusion() { return conclusion; }
        public void setConclusion(String conclusion) { this.conclusion = conclusion; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }
    }

    /**
     * 视频审核响应实体（内部类）
     */
    private static class VideoAuditResponse {
        private String conclusion;
        private Double confidenceScore;
        private String logId;

        public String getConclusion() { return conclusion; }
        public void setConclusion(String conclusion) { this.conclusion = conclusion; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }
    }
}
