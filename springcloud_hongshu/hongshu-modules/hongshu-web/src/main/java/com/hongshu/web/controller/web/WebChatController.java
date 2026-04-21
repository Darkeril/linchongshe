package com.hongshu.web.controller.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.CountMessage;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.entity.WebChat;
import com.hongshu.web.domain.vo.ChatProductRelationVO;
import com.hongshu.web.domain.vo.ChatUserRelationVo;
import com.hongshu.web.domain.vo.SystemNotification;
import com.hongshu.web.service.web.IAICustomerServiceService;
import com.hongshu.web.service.web.ISystemNotificationService;
import com.hongshu.web.service.web.IWebChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天
 *
 * @author: hongshu
 */
@RequestMapping("/im/chat")
@RestController
public class WebChatController {

    @Autowired
    private IWebChatService chatService;
    @Autowired
    private ISystemNotificationService systemNotificationService;
    @Autowired(required = false)
    private IAICustomerServiceService aiCustomerServiceService;


    /**
     * 发送消息
     *
     * @param message 消息实体
     */
    @PostMapping("sendMsg")
    @NoLoginIntercept
    public Result<?> sendMsg(@RequestBody Message message) {
        chatService.sendMsg(message);
        return Result.ok();
    }

    /**
     * 获取所有的聊天记录
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param acceptUid   接收方用户ID（私聊时使用）
     * @param groupChatId 群聊ID（群聊时使用）
     */
    @GetMapping("getAllChatRecord/{currentPage}/{pageSize}")
    public Result<?> getAllChatRecord(@PathVariable long currentPage, @PathVariable long pageSize,
                                      String acceptUid, String groupChatId) {
        Page<WebChat> page = chatService.getAllChatRecord(currentPage, pageSize, acceptUid, groupChatId);
        return Result.ok(page);
    }

    /**
     * 获取商品聊天记录
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param acceptUid   接收方用户ID
     */
    @GetMapping("productChatRecord/{currentPage}/{pageSize}")
    public Result<?> getProductChatRecord(@PathVariable long currentPage,
                                          @PathVariable long pageSize,
                                          String acceptUid,
                                          String productId) {
        Page<WebChat> page = chatService.getProductChatRecord(currentPage, pageSize, acceptUid, productId);
        return Result.ok(page);
    }

    /**
     * 获取当前用户下所有聊天的用户信息
     */
    @GetMapping("getChatUserList")
    public Result<?> getChatUserList() {
        List<ChatUserRelationVo> list = chatService.getChatUserList();
        return Result.ok(list);
    }

    /**
     * 获取当前用户下所有聊天的用户信息
     */
    @GetMapping("productChatUserList")
    public Result<?> getProductChatUserList() {
        List<ChatProductRelationVO> list = chatService.getProductChatUserList();
        return Result.ok(list);
    }

    /**
     * 获取所有聊天记录数量
     */
    @GetMapping("getCountMessage")
    public Result<?> getCountMessage() {
        CountMessage countMessage = chatService.getCountMessage();
        return Result.ok(countMessage);
    }

    /**
     * 删除聊天
     */
    @GetMapping("deleteMsg")
    public String deleteMsg() {
        return null;
    }

    /**
     * 删除所有聊天记录
     */
    @GetMapping("deleteAllChatRecord")
    public String deleteAllChatRecord() {
        return null;
    }

    /**
     * 删除所有聊天用户
     */
    @GetMapping("deleteChatUser")
    public String deleteChatUser() {
        return null;
    }

    /**
     * 获取系统通知
     */
    @GetMapping("getSystemNotification")
    public Result<?> getSystemNotification() {
        SystemNotification systemNotification = chatService.getSystemNotification();
        return Result.ok(systemNotification);
    }

    /**
     * C端：系统通知配置列表（管理端「系统通知配置」Redis 中已启用的条目，与「通知公告/通知管理」无关）
     */
    @GetMapping("systemNotificationConfigList")
    public Result<?> getSystemNotificationConfigList() {
        List<SystemNotification> list = systemNotificationService.getSystemNotificationList();
        List<SystemNotification> filtered = list.stream()
                .filter(n -> Boolean.TRUE.equals(n.getEnabled()))
                .sorted((a, b) -> {
                    long ta = a.getTimestamp() != null ? a.getTimestamp() : 0L;
                    long tb = b.getTimestamp() != null ? b.getTimestamp() : 0L;
                    return Long.compare(tb, ta);
                })
                .collect(Collectors.toList());
        return Result.ok(filtered);
    }

    /**
     * 更新系统通知
     */
    @PostMapping("updateSystemNotification")
    public Result<?> updateSystemNotification(@RequestBody SystemNotification systemNotification) {
        chatService.updateSystemNotification(systemNotification);
        return Result.ok("系统通知更新成功");
    }

    /**
     * 清除聊天数量
     *
     * @param sendUid 发送方用户ID
     * @param type    类型
     */
    @GetMapping("clearMessageCount")
    public Result<?> clearMessageCount(String sendUid, Integer type, String productId, String groupChatId) {
        chatService.clearMessageCount(sendUid, type, productId, groupChatId);
        return Result.ok();
    }

    /**
     * 关闭聊天
     *
     * @param sendUid 发送方用户ID
     */
    @RequestMapping("closeChat/{sendUid}")
    public boolean closeChat(@PathVariable("sendUid") String sendUid) {
        return chatService.closeChat(sendUid);
    }

    /**
     * 获取所有与客服聊天的用户列表（用于admin管理端）
     */
    @GetMapping("getCustomerServiceUserList")
    public Result<?> getCustomerServiceUserList() {
        List<ChatUserRelationVo> list = chatService.getCustomerServiceUserList();
        return Result.ok(list);
    }

    /**
     * 获取客服聊天记录（用于admin管理端）
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param userId      用户ID
     */
    @GetMapping("getCustomerServiceChatRecord/{currentPage}/{pageSize}")
    public Result<?> getCustomerServiceChatRecord(@PathVariable long currentPage,
                                                  @PathVariable long pageSize,
                                                  String userId) {
        Page<WebChat> page = chatService.getCustomerServiceChatRecord(currentPage, pageSize, userId);
        return Result.ok(page);
    }

    /**
     * 客服发送消息（用于admin管理端）
     *
     * @param message 消息实体
     */
    @PostMapping("sendCustomerServiceMessage")
    public Result<?> sendCustomerServiceMessage(@RequestBody Message message) {
        chatService.sendCustomerServiceMessage(message);
        return Result.ok();
    }

    /**
     * 获取AI客服开关状态（用于admin管理端）
     */
    @GetMapping("getAICustomerServiceStatus")
    public Result<?> getAICustomerServiceStatus() {
        if (aiCustomerServiceService == null) {
            return Result.ok(false);
        }
        boolean enabled = aiCustomerServiceService.isAICustomerServiceEnabled();
        return Result.ok(enabled);
    }

    /**
     * 更新AI客服开关状态（用于admin管理端）
     *
     * @param enabled 是否启用
     */
    @PostMapping("updateAICustomerServiceStatus")
    public Result<?> updateAICustomerServiceStatus(@RequestBody Map<String, Boolean> params) {
        if (aiCustomerServiceService == null) {
            return Result.fail("AI客服服务未配置");
        }
        Boolean enabled = params.get("enabled");
        if (enabled == null) {
            return Result.fail("参数错误：enabled不能为空");
        }
        aiCustomerServiceService.updateAICustomerServiceStatus(enabled);
        return Result.ok("AI客服开关状态更新成功");
    }
}
