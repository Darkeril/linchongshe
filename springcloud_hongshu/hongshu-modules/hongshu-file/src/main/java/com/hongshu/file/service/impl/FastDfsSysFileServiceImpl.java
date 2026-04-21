//package com.hongshu.file.service.impl;
//
//import com.alibaba.nacos.common.utils.IoUtils;
//import com.github.tobato.fastdfs.domain.fdfs.StorePath;
//import com.github.tobato.fastdfs.service.FastFileStorageClient;
//import com.hongshu.common.core.utils.file.FileTypeUtils;
//import com.hongshu.file.service.ISysFileService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.InputStream;
//
///**
// * FastDFS 文件存储
// *
//
// */
//@Service
//public class FastDfsSysFileServiceImpl implements ISysFileService {
//
//    /**
//     * 域名或本机访问地址
//     */
//    @Value("${fdfs.domain}")
//    public String domain;
//
//    @Autowired
//    private FastFileStorageClient storageClient;
//
//
//    /**
//     * FastDfs文件上传接口
//     *
//     * @param file 上传的文件
//     * @return 访问地址
//     */
//    @Override
//    public String uploadFile(MultipartFile file) {
//        InputStream inputStream = null;
//        try {
//            inputStream = file.getInputStream();
//            StorePath storePath = storageClient.uploadFile(inputStream, file.getSize(), FileTypeUtils.getExtension(file), null);
//            return domain + "/" + storePath.getFullPath();
//        } catch (Exception e) {
//            throw new RuntimeException("FastDfs Failed to upload file", e);
//        } finally {
//            IoUtils.closeQuietly(inputStream);
//        }
//    }
//
//    @Override
//    public String uploadFile(File file) {
//        return null;
//    }
//}
