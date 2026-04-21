package com.hongshu.web.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OSS
 *
 * @author: hongshu
 */
@Service
public class OssUploadServiceImpl implements IOssUploadService {

    @Autowired
    private RemoteFileService remoteFileService;


    @SneakyThrows
    @Override
    public String upload(MultipartFile file) {
        if (file == null) {
            throw new RuntimeException("请上传一个文件");
        }
        String fileUrl = "";
        R<SysFile> r = remoteFileService.upload(file);
        if (r.getCode() == 200) {
            fileUrl = r.getData().getUrl();
        }
        return fileUrl;
    }

    /**
     * 上传文件
     *
     * @param file 文件
     */
    @SneakyThrows
    @Override
    public String upload(File file) {
        if (file == null) {
            throw new RuntimeException("请上传一个文件");
        }
        String fileUrl = "";
        R<SysFile> r = remoteFileService.upload(file);
        if (r.getCode() == 200) {
            fileUrl = r.getData().getUrl();
        }
        return fileUrl;
    }

    /**
     * 批量上传文件
     *
     * @param files 文件集
     */
    @Override
    public List<String> uploadBatch(MultipartFile[] files) {
        if (files == null) {
            throw new RuntimeException("请至少上传一个文件");
        }
        R<List<SysFile>> listR = remoteFileService.batchUpload(files);
        return Optional.ofNullable(listR)
                .filter(response -> response.getCode() == 200) // 检查成功状态
                .map(R::getData) // 获取数据
                .filter(CollectionUtils::isNotEmpty) // 检查数据不为空
                .orElseThrow(() -> {
                    String errorMsg = listR != null ? listR.getMsg() : "远程服务无响应";
                    return new RuntimeException("图片上传失败: " + errorMsg);
                })
                .stream()
                .map(SysFile::getUrl) // 提取URL
                .filter(Objects::nonNull) // 过滤掉null的URL
                .collect(Collectors.toList());
    }

    /**
     * 批量上传文件
     *
     * @param files 文件集
     */
    @Override
    public List<String> uploadBatch(List<File> files) {
        if (files == null) {
            throw new RuntimeException("请至少上传一个文件");
        }
        R<List<SysFile>> listR = remoteFileService.batchUpload(files);
        return Optional.ofNullable(listR)
                .filter(response -> response.getCode() == 200) // 检查成功状态
                .map(R::getData) // 获取数据
                .filter(CollectionUtils::isNotEmpty) // 检查数据不为空
                .orElseThrow(() -> {
                    String errorMsg = listR != null ? listR.getMsg() : "远程服务无响应";
                    return new RuntimeException("图片上传失败: " + errorMsg);
                })
                .stream()
                .map(SysFile::getUrl) // 提取URL
                .filter(Objects::nonNull) // 过滤掉null的URL
                .collect(Collectors.toList());
    }
}
