package com.hongshu.web.service.web.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.UUID;
import com.google.gson.Gson;
import com.hongshu.common.core.utils.SpringContextUtils;
import com.hongshu.web.config.OssConfigService;
import com.hongshu.web.domain.dto.OssConfigDTO;
import com.hongshu.web.factory.OssFactory;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author: hongshu
 */
@Slf4j
public class QiNiuYunUploadFile implements OssFactory {

    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String domain;


    public QiNiuYunUploadFile() {
        // 从数据库加载配置
        this.loadConfigFromDatabase();
    }

    private void loadConfigFromDatabase() {
        try {
            // 获取OSS配置服务
            OssConfigService ossConfigService = SpringContextUtils.getBean(OssConfigService.class);
            // 从数据库获取七牛云配置
            OssConfigDTO qiniuConfig = ossConfigService.getOssConfig("qiniuyun");

            if (qiniuConfig != null) {
                this.domain = qiniuConfig.getDomain();
                this.accessKey = qiniuConfig.getAccessKey();
                this.secretKey = qiniuConfig.getSecretKey();
                this.bucketName = qiniuConfig.getBucketName();
            } else {
                // 如果数据库中没有配置，使用默认配置
                Environment bean = SpringContextUtils.getBean(Environment.class);
                this.domain = bean.getProperty("oss.qiniuyun.domain");
                this.accessKey = bean.getProperty("oss.qiniuyun.accessKey");
                this.secretKey = bean.getProperty("oss.qiniuyun.secretKey");
                this.bucketName = bean.getProperty("oss.qiniuyun.bucketName");
            }
        } catch (Exception e) {
            log.error("加载七牛云配置失败", e);
            // 使用默认配置
            Environment bean = SpringContextUtils.getBean(Environment.class);
            this.domain = bean.getProperty("oss.qiniuyun.domain");
            this.accessKey = bean.getProperty("oss.qiniuyun.accessKey");
            this.secretKey = bean.getProperty("oss.qiniuyun.secretKey");
            this.bucketName = bean.getProperty("oss.qiniuyun.bucketName");
        }
    }

    // 添加刷新配置的方法
    public void refreshConfig() {
        loadConfigFromDatabase();
    }


    @Override
    public String save(MultipartFile file) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huabei());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本

        UploadManager uploadManager = new UploadManager(cfg);

        String filePath = "";
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename.substring(originalFilename.lastIndexOf("."));
        ;

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
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucketName);
            Response response = uploadManager.put(input, fileName, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

//            filePath = "http://" + domain + "/" + putRet.key;
            filePath = domain + "/" + putRet.key;
            return filePath;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String save(File file) {
        // 构造配置
        Configuration cfg = new Configuration(Region.huabei());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);

        String originalFilename = file.getName();
        String fileName = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = uuid + fileName;
        String datePath = new DateTime().toString("yyyy/MM/dd");
        fileName = datePath + "/" + fileName;

        try {
            InputStream input = new FileInputStream(file);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucketName);
            Response response = uploadManager.put(input, fileName, upToken, null, null);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            String filePath = domain + "/" + putRet.key;
            input.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Boolean delete(String path) {
        String replaceFileName = path.replace("http://" + domain + "/", "");
        Configuration cfg = new Configuration(Region.beimei());

        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);

        try {
            Response response = bucketManager.delete(bucketName, replaceFileName);
            return response.isOK();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
