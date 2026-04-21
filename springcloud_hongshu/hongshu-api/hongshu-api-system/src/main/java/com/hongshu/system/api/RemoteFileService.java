package com.hongshu.system.api;

import com.hongshu.common.core.constant.ServiceNameConstants;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.system.api.factory.RemoteFileFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 文件服务
 */
@FeignClient(contextId = "remoteFileService", value = ServiceNameConstants.FILE_SERVICE, fallbackFactory = RemoteFileFallbackFactory.class)
public interface RemoteFileService {

    /**
     * 单文件上传
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<SysFile> upload(@RequestPart(value = "file") MultipartFile file);

    /**
     * 批量上传文件
     *
     * @param files 文件信息
     * @return 结果
     */
    @PostMapping(value = "/batch/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<List<SysFile>> batchUpload(@RequestPart(value = "files") MultipartFile[] files);

    /**
     * 单文件上传
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<SysFile> upload(@RequestPart(value = "file") File file);

    /**
     * 批量上传文件
     *
     * @param files 文件信息
     * @return 结果
     */
    @PostMapping(value = "/batch/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<List<SysFile>> batchUpload(@RequestPart(value = "files") List<File> files);
}
