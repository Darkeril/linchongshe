package com.hongshu.web.factory;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author: hongshu
 */
public interface OssFactory {

    String save(MultipartFile file);

    Boolean delete(String path);
}
