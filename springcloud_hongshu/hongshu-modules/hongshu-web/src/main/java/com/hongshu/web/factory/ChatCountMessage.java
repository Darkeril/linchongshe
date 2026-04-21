package com.hongshu.web.factory;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.constant.ImConstant;
import com.hongshu.common.core.domain.CountMessage;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.utils.RedisUtils;

/**

 */
public class ChatCountMessage implements MessageFactory {

    RedisUtils redisUtils;

    public ChatCountMessage(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Override
    public void sendMessage(Message message) {
        String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + message.getAcceptUid();
        CountMessage countMessage = JSONUtil.toBean(JSONUtil.toJsonStr(message.getContent()), CountMessage.class);
        countMessage.setUid(message.getAcceptUid());
        redisUtils.set(messageCountKey, JSONUtil.toJsonStr(countMessage));
    }
}
