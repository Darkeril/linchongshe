package com.hongshu.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * OSS
 *
 * @author: hongshu
 */
public interface IOssUploadService {

    /**
     * 上传文件
     *
     * @param file 文件
     */
    String upload(MultipartFile file);

    /**
     * 上传文件
     *
     * @param file 文件
     */
    String upload(File file);

    /**
     * 批量上传文件
     *
     * @param files 文件集
     */
    List<String> uploadBatch(MultipartFile[] files);

    /**
     * 批量上传文件
     *
     * @param files 文件集
     */
    List<String> uploadBatch(List<File> files);
}
