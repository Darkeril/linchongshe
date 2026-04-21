package com.hongshu.ai.api.openai.sse;

import com.hongshu.ai.api.openai.OpenAiClient;
import com.hongshu.ai.api.openai.entity.chat.ChatCompletion;
import com.hongshu.ai.api.openai.plugin.PluginAbstract;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSourceListener;

/**
 * 描述： 插件开发返回信息收集sse监听器
 */
@Slf4j
public class DefaultPluginListener extends PluginListener {

    public DefaultPluginListener(OpenAiClient client, EventSourceListener eventSourceListener, PluginAbstract plugin, ChatCompletion chatCompletion) {
        super(client, eventSourceListener, plugin, chatCompletion);
    }
}
