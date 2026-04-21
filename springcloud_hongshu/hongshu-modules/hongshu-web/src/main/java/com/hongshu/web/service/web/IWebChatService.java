package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.domain.CountMessage;
import com.hongshu.common.core.domain.Message;
import com.hongshu.web.domain.entity.WebChat;
import com.hongshu.web.domain.vo.ChatProductRelationVO;
import com.hongshu.web.domain.vo.ChatUserRelationVo;
import com.hongshu.web.domain.vo.SystemNotification;

import java.util.List;

/**
 * 聊天
 *
 * @author: hongshu
 */
public interface IWebChatService extends IService<WebChat> {

    /**
     * 发送消息
     *
     * @param message 消息
     */
    void sendMsg(Message message);

    /**
     * 获取所有聊天记录
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param acceptUid   接收方用户ID（私聊时使用）
     * @param groupChatId 群聊ID（群聊时使用）
     */
    Page<WebChat> getAllChatRecord(long currentPage, long pageSize, String acceptUid, String groupChatId);

    Page<WebChat> getProductChatRecord(long currentPage, long pageSize, String acceptUid, String productId);


    /**
     * 获取当前用户下所有聊天的用户信息
     */
    List<ChatUserRelationVo> getChatUserList();

    List<ChatProductRelationVO> getProductChatUserList();

    /**
     * 获取所有聊天记录数量
     */
    CountMessage getCountMessage();

    /**
     * 清除聊天数量
     *
     * @param sendUid 发送方用户ID（私聊时使用）或群聊ID（群聊时使用）
     * @param type    类型
     * @param productId 商品ID（商品聊天时使用）
     * @param groupChatId 群聊ID（群聊时使用）
     */
    void clearMessageCount(String sendUid, Integer type, String productId, String groupChatId);

    /**
     * 关闭聊天
     *
     * @param sendUid 发送方用户ID
     */
    boolean closeChat(String sendUid);

    /**
     * 获取系统通知
     */
    SystemNotification getSystemNotification();

    /**
     * 更新系统通知
     */
    void updateSystemNotification(SystemNotification systemNotification);

    /**
     * 获取所有与客服聊天的用户列表（用于admin管理端）
     */
    List<ChatUserRelationVo> getCustomerServiceUserList();

    /**
     * 获取客服聊天记录（用于admin管理端）
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param userId      用户ID
     */
    Page<WebChat> getCustomerServiceChatRecord(long currentPage, long pageSize, String userId);

    /**
     * 客服发送消息（用于admin管理端）
     *
     * @param message 消息实体
     */
    void sendCustomerServiceMessage(Message message);

}
