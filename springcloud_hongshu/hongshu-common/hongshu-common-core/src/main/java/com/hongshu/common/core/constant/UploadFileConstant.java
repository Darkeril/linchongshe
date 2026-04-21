package com.hongshu.common.core.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文件上传路径配置
 *
 * @author: hongshu
 */
@Schema(name = "文件上传路径配置")
public interface UploadFileConstant {

    @Schema(description = "文件上传地址")
    String ADDRESS = "XXX";

    @Schema(description = "文件访问前缀")
    String OSS = "/oss/";
}
