package com.hongshu.web.domain.dto;

import com.hongshu.common.core.validator.group.DefaultGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;


/**
  sunlightcs@gmail.com
 * @since 1.0.0 2023-03-16
 */
@Data
@Schema(name = "浏览记录")
public class BrowseRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "uid不能为空", groups = DefaultGroup.class)
    private String uid;

    @NotNull(message = "笔记不能为空", groups = DefaultGroup.class)
    private String nid;
}
