package com.hongshu.system.api.factory;

import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 文件服务降级处理
 */
@Component
public class RemoteFileFallbackFactory implements FallbackFactory<RemoteFileService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteFileFallbackFactory.class);

    @Override
    public RemoteFileService create(Throwable throwable) {
        log.error("文件服务调用失败:{}", throwable.getMessage());

        return new RemoteFileService() {
            @Override
            public R<SysFile> upload(MultipartFile file) {
                return R.fail("单文件上传失败:" + throwable.getMessage());
            }

            @Override
            public R<List<SysFile>> batchUpload(MultipartFile[] files) {
                return R.fail("批量上传文件失败:" + throwable.getMessage());
            }

            @Override
            public R<SysFile> upload(File file) {
                return R.fail("单文件上传失败:" + throwable.getMessage());
            }

            @Override
            public R<List<SysFile>> batchUpload(List<File> files) {
                return R.fail("批量上传文件失败:" + throwable.getMessage());
            }
        };
    }
}
