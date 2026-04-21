package com.hongshu.idle.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

/**
 * 微信支付签名工具类
 * 
 * @author: hongshu
 */
@Slf4j
public class WeChatPaySignUtil {

    /**
     * 生成微信支付APIv3签名
     * 
     * @param method HTTP方法（GET、POST等）
     * @param url 请求URL（不包含域名，如：/v3/pay/transactions/native）
     * @param timestamp 时间戳（秒）
     * @param nonce 随机字符串
     * @param body 请求体（JSON字符串，GET请求为空字符串）
     * @param privateKey 商户私钥（PEM格式字符串）
     * @param certSerialNo 商户证书序列号
     * @param mchId 商户号
     * @return Authorization头内容
     */
    public static String generateAuthorization(String method, String url, long timestamp, 
                                               String nonce, String body, String privateKey, String certSerialNo, String mchId) {
        try {
            // 构建签名字符串
            String signStr = buildSignString(method, url, timestamp, nonce, body);
            
            // 使用私钥签名
            String signature = signWithPrivateKey(signStr, privateKey);
            
            // 构建Authorization头（微信支付APIv3格式）
            // 格式：WECHATPAY2-SHA256-RSA2048 mchid="xxx",nonce_str="xxx",signature="xxx",timestamp="xxx",serial_no="xxx"
            return String.format(
                "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",signature=\"%s\",timestamp=\"%d\",serial_no=\"%s\"",
                mchId,
                nonce,
                signature,
                timestamp,
                certSerialNo
            );
        } catch (Exception e) {
            log.error("生成微信支付签名失败", e);
            throw new RuntimeException("生成微信支付签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建签名字符串
     */
    private static String buildSignString(String method, String url, long timestamp, String nonce, String body) {
        return method + "\n" +
               url + "\n" +
               timestamp + "\n" +
               nonce + "\n" +
               (body == null ? "" : body) + "\n";
    }

    /**
     * 使用私钥签名（公开方法，供外部调用）
     */
    public static String signWithPrivateKey(String data, String privateKeyStr) throws Exception {
        try {
            String privateKeyContent = privateKeyStr;
            
            // 优先检测是否是PEM格式的私钥内容（包含BEGIN和END标记）
            boolean isPemFormat = privateKeyStr != null && 
                    (privateKeyStr.contains("-----BEGIN") || privateKeyStr.contains("-----END"));
            
            // 如果看起来像是文件路径（包含斜杠或反斜杠）且不是PEM格式，尝试从文件读取
            if (!isPemFormat && (privateKeyStr.contains("/") || privateKeyStr.contains("\\"))) {
                log.warn("检测到私钥字段可能是文件路径，尝试从文件读取: {}", privateKeyStr);
                try {
                    java.nio.file.Path keyPath = java.nio.file.Paths.get(privateKeyStr);
                    if (java.nio.file.Files.exists(keyPath)) {
                        privateKeyContent = new String(java.nio.file.Files.readAllBytes(keyPath), StandardCharsets.UTF_8);
                        log.info("成功从文件读取私钥: {}", privateKeyStr);
                    } else {
                        log.error("私钥文件不存在: {}", privateKeyStr);
                        throw new IllegalArgumentException("私钥文件不存在: " + privateKeyStr + "，请检查文件路径或直接将私钥内容存储到数据库");
                    }
                } catch (Exception e) {
                    log.error("从文件读取私钥失败: {}", e.getMessage());
                    throw new IllegalArgumentException("私钥配置错误：数据库中存储的是文件路径，但无法读取文件。请将私钥内容（PEM格式）直接存储到数据库中，而不是文件路径。错误: " + e.getMessage());
                }
            } else if (isPemFormat) {
                log.debug("检测到PEM格式私钥内容，直接使用");
            }
            
            // 移除PEM格式的头部和尾部，并清理所有空白字符（包括换行符、空格、制表符等）
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "") // 移除所有空白字符（包括换行、空格、制表符等）
                    .trim();
            
            // 验证Base64格式
            if (privateKeyContent.isEmpty()) {
                throw new IllegalArgumentException("私钥内容为空");
            }
            
            // 验证是否包含非Base64字符
            if (!privateKeyContent.matches("^[A-Za-z0-9+/=]+$")) {
                log.error("私钥包含非法字符，前100个字符: {}", 
                        privateKeyContent.length() > 100 ? privateKeyContent.substring(0, 100) : privateKeyContent);
                throw new IllegalArgumentException("私钥格式错误：包含非Base64字符。请确保私钥是PEM格式的Base64编码内容");
            }
            
            // Base64解码
            byte[] keyBytes;
            try {
                keyBytes = Base64.getDecoder().decode(privateKeyContent);
            } catch (IllegalArgumentException e) {
                log.error("Base64解码失败，私钥内容长度: {}, 前100个字符: {}", 
                        privateKeyContent.length(),
                        privateKeyContent.length() > 100 ? privateKeyContent.substring(0, 100) : privateKeyContent);
                throw new IllegalArgumentException("私钥Base64解码失败: " + e.getMessage(), e);
            }
            
            // 加载私钥
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            // 使用SHA256withRSA签名
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();
            
            // Base64编码返回
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            log.error("私钥签名失败", e);
            throw e;
        }
    }

    /**
     * 生成随机字符串
     */
    public static String generateNonce() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 获取当前时间戳（秒）
     */
    public static long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
}

