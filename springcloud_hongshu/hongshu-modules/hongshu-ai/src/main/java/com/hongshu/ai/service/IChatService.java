package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.domain.command.ChatCommand;
import com.hongshu.ai.domain.entity.Chat;
import com.hongshu.ai.domain.vo.ChatVO;

import java.util.List;

/**
 * 聊天摘要 服务类
 *
 * @author: Yang
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface IChatService extends IService<Chat> {

    /**
     * 查询聊天摘要分页列表
     *
     * @param query 查询条件
     * @return 聊天摘要集合
     */
    ResponseInfo<IPageInfo<ChatVO>> pageChat(Query query);

    /**
     * 查询聊天摘要列表
     *
     * @param query 查询条件
     * @return 聊天摘要集合
     */
    ResponseInfo<List<ChatVO>> listChat(Query query);

    /**
     * 根据主键查询聊天摘要
     *
     * @param id 聊天摘要主键
     * @return 聊天摘要
     */
    ResponseInfo<ChatVO> getChatById(Long id);

    /**
     * 新增聊天摘要(同步)
     *
     * @param command 聊天摘要
     * @return 结果
     */
    ResponseInfo<Long> saveChat(ChatCommand command);

    /**
     * 新增聊天摘要(SSE)
     *
     * @param command 聊天摘要
     * @return 结果
     */
    ResponseInfo<ChatVO> saveSSEChat(ChatCommand command);

    /**
     * 批量删除聊天摘要
     *
     * @param ids 需要删除的聊天摘要主键集合
     * @return 结果
     */
    ResponseInfo removeChatByIds(List<Long> ids);

    /**
     * 删除聊天摘要信息
     *
     * @param id 聊天摘要主键
     * @return 结果
     */
    ResponseInfo removeChatByChatNumber(String chatNumber);

    /**
     * 删除聊天摘要信息
     *
     * @param id 聊天摘要主键
     * @return 结果
     */
    ResponseInfo removeChatById(Long id);

}
