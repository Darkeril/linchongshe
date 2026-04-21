package com.hongshu.web.service.web;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 小红书视频和图片代理服务
 * 用于转发视频和图片请求，添加必要的请求头以支持播放和显示
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class XiaohongshuVideoProxyService {

    private final OkHttpClient httpClient;

    public XiaohongshuVideoProxyService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    /**
     * 下载视频到临时文件
     *
     * @param videoUrl 视频URL
     * @param cookie Cookie字符串（可选，用于认证）
     * @return 临时文件
     */
    public File downloadVideoToFile(String videoUrl, String cookie) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("视频URL不能为空");
        }

        File tempFile = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        Response response = null;

        try {
            log.info("开始下载视频到文件: {}", videoUrl);

            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(videoUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.xiaohongshu.com/")
                    .header("Origin", "https://www.xiaohongshu.com")
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "identity");

            // 如果提供了Cookie，添加到请求头
            if (cookie != null && !cookie.trim().isEmpty()) {
                requestBuilder.header("Cookie", cookie.trim());
                log.info("添加Cookie到视频下载请求（长度: {}）", cookie.trim().length());
            }

            Request request = requestBuilder.get().build();
            response = httpClient.newCall(request).execute();

            int statusCode = response.code();
            log.info("视频下载响应 - 状态码: {}, Content-Type: {}, Content-Length: {}", 
                    statusCode, 
                    response.header("Content-Type"),
                    response.header("Content-Length"));

            if (statusCode != 200 && statusCode != 206) {
                String errorBody = "";
                try {
                    if (response.body() != null) {
                        errorBody = response.body().string();
                        if (errorBody.length() > 500) {
                            errorBody = errorBody.substring(0, 500) + "...";
                        }
                    }
                } catch (Exception e) {
                    log.warn("无法读取错误响应体", e);
                }
                response.close();
                throw new RuntimeException("视频下载失败，状态码: " + statusCode + ", 响应: " + errorBody);
            }

            // 检查Content-Type
            String contentType = response.header("Content-Type");
            if (contentType != null && (contentType.contains("application/json") || contentType.contains("text/"))) {
                String errorBody = "";
                try {
                    if (response.body() != null) {
                        errorBody = response.body().string();
                        if (errorBody.length() > 500) {
                            errorBody = errorBody.substring(0, 500) + "...";
                        }
                    }
                } catch (Exception e) {
                    log.warn("无法读取错误响应体", e);
                }
                response.close();
                throw new RuntimeException("视频下载返回错误响应: " + errorBody);
            }

            // 创建临时文件
            String fileExtension = ".mp4"; // 默认扩展名
            if (videoUrl.contains(".mp4")) {
                fileExtension = ".mp4";
            } else if (videoUrl.contains(".webm")) {
                fileExtension = ".webm";
            }
            
            tempFile = File.createTempFile("xhs_video_", fileExtension);
            tempFile.deleteOnExit(); // 程序退出时删除临时文件

            // 下载视频到文件
            inputStream = response.body() != null ? response.body().byteStream() : null;
            if (inputStream == null) {
                throw new RuntimeException("无法获取视频流");
            }

            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192 * 4];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();

            log.info("视频下载成功，保存到: {}, 大小: {} bytes", tempFile.getAbsolutePath(), totalBytes);

            return tempFile;

        } catch (Exception e) {
            log.error("下载视频到文件失败: {}", videoUrl, e);
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception ex) {
                    log.warn("删除临时文件失败", ex);
                }
            }
            throw new RuntimeException("下载视频失败: " + e.getMessage(), e);
        } finally {
            // 关闭资源
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.warn("关闭输入流失败", e);
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                log.warn("关闭输出流失败", e);
            }
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                log.warn("关闭响应失败", e);
            }
        }
    }

    /**
     * 视频代理结果
     */
    public static class VideoProxyResult {
        private final InputStream inputStream;
        private final String contentType;
        private final long contentLength;
        private final Response response;
        private final int statusCode;
        private final String contentRange;

        public VideoProxyResult(InputStream inputStream, String contentType, long contentLength, 
                              Response response, int statusCode, String contentRange) {
            this.inputStream = inputStream;
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.response = response;
            this.statusCode = statusCode;
            this.contentRange = contentRange;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getContentType() {
            return contentType;
        }

        public long getContentLength() {
            return contentLength;
        }

        public Response getResponse() {
            return response;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getContentRange() {
            return contentRange;
        }
    }

    /**
     * 下载图片到临时文件
     *
     * @param imageUrl 图片URL
     * @param cookie Cookie字符串（可选，用于认证）
     * @return 临时文件
     */
    public File downloadImageToFile(String imageUrl, String cookie) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }

        File tempFile = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        Response response = null;

        try {
            log.info("开始下载图片到文件: {}", imageUrl);

            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(imageUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.xiaohongshu.com/")
                    .header("Origin", "https://www.xiaohongshu.com")
                    .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "identity");

            // 如果提供了Cookie，添加到请求头
            if (cookie != null && !cookie.trim().isEmpty()) {
                requestBuilder.header("Cookie", cookie.trim());
                log.info("添加Cookie到图片下载请求（长度: {}）", cookie.trim().length());
            }

            Request request = requestBuilder.get().build();
            response = httpClient.newCall(request).execute();

            int statusCode = response.code();
            log.info("图片下载响应 - 状态码: {}, Content-Type: {}, Content-Length: {}", 
                    statusCode, 
                    response.header("Content-Type"),
                    response.header("Content-Length"));

            if (statusCode != 200) {
                String errorBody = "";
                try {
                    if (response.body() != null) {
                        errorBody = response.body().string();
                        if (errorBody.length() > 500) {
                            errorBody = errorBody.substring(0, 500) + "...";
                        }
                    }
                } catch (Exception e) {
                    log.warn("无法读取错误响应体", e);
                }
                response.close();
                throw new RuntimeException("图片下载失败，状态码: " + statusCode + ", 响应: " + errorBody);
            }

            // 检查Content-Type
            String contentType = response.header("Content-Type");
            if (contentType != null && (contentType.contains("application/json") || contentType.contains("text/"))) {
                String errorBody = "";
                try {
                    if (response.body() != null) {
                        errorBody = response.body().string();
                        if (errorBody.length() > 500) {
                            errorBody = errorBody.substring(0, 500) + "...";
                        }
                    }
                } catch (Exception e) {
                    log.warn("无法读取错误响应体", e);
                }
                response.close();
                throw new RuntimeException("图片下载返回错误响应: " + errorBody);
            }

            // 创建临时文件，根据URL或Content-Type确定扩展名
            String fileExtension = ".jpg"; // 默认扩展名
            if (imageUrl.contains(".webp")) {
                fileExtension = ".webp";
            } else if (imageUrl.contains(".png")) {
                fileExtension = ".png";
            } else if (imageUrl.contains(".gif")) {
                fileExtension = ".gif";
            } else if (contentType != null) {
                if (contentType.contains("webp")) {
                    fileExtension = ".webp";
                } else if (contentType.contains("png")) {
                    fileExtension = ".png";
                } else if (contentType.contains("gif")) {
                    fileExtension = ".gif";
                }
            }
            
            tempFile = File.createTempFile("xhs_image_", fileExtension);
            tempFile.deleteOnExit(); // 程序退出时删除临时文件

            // 下载图片到文件
            inputStream = response.body() != null ? response.body().byteStream() : null;
            if (inputStream == null) {
                throw new RuntimeException("无法获取图片流");
            }

            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192 * 4];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();

            log.info("图片下载成功，保存到: {}, 大小: {} bytes", tempFile.getAbsolutePath(), totalBytes);

            return tempFile;

        } catch (Exception e) {
            log.error("下载图片到文件失败: {}", imageUrl, e);
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception ex) {
                    log.warn("删除临时文件失败", ex);
                }
            }
            throw new RuntimeException("下载图片失败: " + e.getMessage(), e);
        } finally {
            // 关闭资源
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.warn("关闭输入流失败", e);
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                log.warn("关闭输出流失败", e);
            }
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                log.warn("关闭响应失败", e);
            }
        }
    }

    /**
     * 代理图片请求
     * 用于转发小红书图片请求，添加必要的请求头以支持图片显示
     *
     * @param imageUrl 图片URL
     * @param cookie Cookie字符串（可选，用于认证）
     * @return 图片流和Content-Type
     */
    public VideoProxyResult proxyImage(String imageUrl, String cookie) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }

        try {
            log.info("开始代理图片请求: {}", imageUrl);

            // 构建请求，添加必要的请求头
            Request.Builder requestBuilder = new Request.Builder()
                    .url(imageUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.xiaohongshu.com/")
                    .header("Origin", "https://www.xiaohongshu.com")
                    .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "identity");
            
            // 如果提供了Cookie，添加到请求头
            if (cookie != null && !cookie.trim().isEmpty()) {
                requestBuilder.header("Cookie", cookie.trim());
                log.info("添加Cookie到图片请求（长度: {}）", cookie.trim().length());
            }
            
            Request request = requestBuilder.get().build();

            Response response = httpClient.newCall(request).execute();

            int statusCode = response.code();
            String contentType = response.header("Content-Type");
            String contentLengthHeader = response.header("Content-Length");

            log.info("图片请求响应 - 状态码: {}, Content-Type: {}, Content-Length: {}", 
                    statusCode, contentType, contentLengthHeader);

            if (statusCode != 200) {
                String errorBody = "";
                try {
                    if (response.body() != null) {
                        errorBody = response.body().string();
                        if (errorBody.length() > 500) {
                            errorBody = errorBody.substring(0, 500) + "...";
                        }
                        log.error("图片请求失败响应体: {}", errorBody);
                    }
                } catch (Exception e) {
                    log.warn("无法读取错误响应体", e);
                }
                response.close();
                throw new RuntimeException("图片请求失败，状态码: " + statusCode + ", 响应: " + errorBody);
            }

            // 获取Content-Type
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg"; // 默认类型
            }

            // 获取Content-Length
            long contentLength = 0;
            if (contentLengthHeader != null && !contentLengthHeader.isEmpty()) {
                try {
                    contentLength = Long.parseLong(contentLengthHeader);
                } catch (NumberFormatException e) {
                    log.warn("无法解析Content-Length: {}", contentLengthHeader);
                }
            }

            // 获取响应体输入流
            InputStream inputStream = response.body() != null ? response.body().byteStream() : null;
            if (inputStream == null) {
                response.close();
                throw new RuntimeException("图片响应体为空");
            }

            log.info("图片代理成功，Content-Type: {}, Content-Length: {}", contentType, contentLength);

            return new VideoProxyResult(inputStream, contentType, contentLength, response, statusCode, null);
        } catch (Exception e) {
            log.error("图片代理失败: {}", imageUrl, e);
            throw new RuntimeException("图片代理失败: " + e.getMessage(), e);
        }
    }
}

