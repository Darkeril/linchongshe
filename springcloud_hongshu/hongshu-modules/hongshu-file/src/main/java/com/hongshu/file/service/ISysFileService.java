package com.hongshu.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件上传接口
 *

 */
public interface ISysFileService {

    /**
     * 文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    String uploadFile(MultipartFile file) throws Exception;

    /**
     * 文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    String uploadFile(File file) throws Exception;
}
