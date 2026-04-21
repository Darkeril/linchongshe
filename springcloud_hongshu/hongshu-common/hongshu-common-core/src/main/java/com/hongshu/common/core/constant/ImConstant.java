package com.hongshu.common.core.constant;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消息通知数量设置
 *
 * @author: hongshu
 */
@Schema(name = "消息通知数量")
public interface ImConstant {

    @Schema(description = "消息通知数量")
    String MESSAGE_COUNT_KEY = "messageCountKey:";

    /**
     *  IM数据源
     */
    public final static String DS_IM_PLATFORM = "platform";
}
