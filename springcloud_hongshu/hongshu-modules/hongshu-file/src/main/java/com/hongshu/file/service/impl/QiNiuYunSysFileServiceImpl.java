package com.hongshu.file.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.file.domain.OssConfigDTO;
import com.hongshu.file.service.ISysFileService;
import com.hongshu.file.util.QiniuRegionResolver;
import com.hongshu.system.api.RemoteWebService;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author: hongshu
 */
@Service
public class QiNiuYunSysFileServiceImpl implements ISysFileService {

    @Autowired
    private RemoteWebService remoteWebService;


    @Override
    public String uploadFile(MultipartFile file) {
        R<?> r = remoteWebService.getOssConfigInternal();
        Object data = r.getData();
        Map<String, OssConfigDTO> ossMap =
                JSONUtil.toBean(JSONUtil.parseObj(data),
                        new TypeReference<Map<String, OssConfigDTO>>() {
                        }, false);
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.QINIUYUN.getCode());

        Configuration cfg = new Configuration(QiniuRegionResolver.resolve(ossConfig.getRegion()));
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本

        UploadManager uploadManager = new UploadManager(cfg);

        String filePath = "";
        String originalFilename = file.getOriginalFilename();
        // 安全地提取文件扩展名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }
        }
        // 如果没有扩展名，根据ContentType判断或使用默认扩展名
        if (fileExtension.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    fileExtension = contentType.contains("png") ? ".png" : 
                                   contentType.contains("gif") ? ".gif" : 
                                   contentType.contains("webp") ? ".webp" : ".jpg";
                } else if (contentType.startsWith("video/")) {
                    fileExtension = contentType.contains("mp4") ? ".mp4" : 
                                   contentType.contains("avi") ? ".avi" : ".mp4";
                } else {
                    fileExtension = ".jpg"; // 默认使用jpg
                }
            } else {
                fileExtension = ".jpg"; // 默认使用jpg
            }
        }
        String fileName = fileExtension;

        //1 在文件名称里面添加随机唯一的值
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // yuy76t5rew01.jpg
        fileName = uuid + fileName;

        //2 把文件按照日期进行分类
        //获取当前日期
        //   2019/11/12
        String datePath = new DateTime().toString("yyyy/MM/dd");
        //拼接
        //  2019/11/12/ewtqr313401.jpg
        fileName = datePath + "/" + fileName;

        try {
            byte[] uploadBytes = file.getBytes();
            // 获取文件流
            InputStream input = new ByteArrayInputStream(uploadBytes);
            Auth auth = Auth.create(ossConfig.getAccessKey(), ossConfig.getSecretKey());
            String upToken = auth.uploadToken(ossConfig.getBucketName());
            Response response = uploadManager.put(input, fileName, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            filePath = ossConfig.getDomain() + "/" + putRet.key;
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String uploadFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        R<?> r = remoteWebService.getOssConfigInternal();
        Object data = r.getData();
        Map<String, OssConfigDTO> ossMap =
                JSONUtil.toBean(JSONUtil.parseObj(data),
                        new TypeReference<Map<String, OssConfigDTO>>() {
                        }, false);
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.QINIUYUN.getCode());

        Configuration cfg = new Configuration(QiniuRegionResolver.resolve(ossConfig.getRegion()));
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本

        UploadManager uploadManager = new UploadManager(cfg);

        String filePath = "";
        String fileName = file.getName();
        String fileExtension = "";
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = fileName.substring(lastDotIndex);
        } else {
            // 如果没有扩展名，根据文件内容判断（简单处理，默认使用.mp4）
            fileExtension = ".mp4";
        }

        //1 在文件名称里面添加随机唯一的值
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // yuy76t5rew01.jpg
        fileName = uuid + fileExtension;

        //2 把文件按照日期进行分类
        //获取当前日期
        //   2019/11/12
        String datePath = new DateTime().toString("yyyy/MM/dd");
        //拼接
        //  2019/11/12/ewtqr313401.jpg
        fileName = datePath + "/" + fileName;

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            Auth auth = Auth.create(ossConfig.getAccessKey(), ossConfig.getSecretKey());
            String upToken = auth.uploadToken(ossConfig.getBucketName());
            Response response = uploadManager.put(fileInputStream, fileName, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            filePath = ossConfig.getDomain() + "/" + putRet.key;
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("七牛云上传文件失败: " + e.getMessage(), e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
