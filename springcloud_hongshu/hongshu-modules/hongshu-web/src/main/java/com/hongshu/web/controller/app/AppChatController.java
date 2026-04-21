package com.hongshu.web.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.CountMessage;
import com.hongshu.common.core.domain.Message;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.entity.WebChat;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.ChatMessageVo;
import com.hongshu.web.domain.vo.ChatUserRelationVo;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聊天
 *
 * @author: hongshu
 */
@RequestMapping("/app/im/chat")
@RestController
public class AppChatController {

    @Autowired
    private IWebChatService chatService;
    
    @Autowired
    private WebUserMapper userMapper;


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
        
        // 如果是群聊，需要填充发送者信息
        if (groupChatId != null && !groupChatId.isEmpty() && page.getRecords() != null && !page.getRecords().isEmpty()) {
            // 获取所有发送者ID
            Set<String> sendUids = page.getRecords().stream()
                    .map(WebChat::getSendUid)
                    .filter(uid -> uid != null && !uid.isEmpty())
                    .collect(Collectors.toSet());
            
            // 批量查询用户信息
            Map<String, WebUser> userMap = userMapper.selectBatchIds(sendUids).stream()
                    .collect(Collectors.toMap(WebUser::getId, user -> user));
            
            // 转换为VO并填充发送者信息
            List<ChatMessageVo> voList = page.getRecords().stream().map(chat -> {
                ChatMessageVo vo = new ChatMessageVo();
                vo.setId(chat.getId());
                vo.setProductId(chat.getProductId());
                vo.setSendUid(chat.getSendUid());
                vo.setAcceptUid(chat.getAcceptUid());
                vo.setContent(chat.getContent());
                vo.setChatType(chat.getChatType());
                vo.setGroupChatId(chat.getGroupChatId());
                vo.setMsgType(chat.getMsgType());
                vo.setTimestamp(chat.getTimestamp());
                vo.setAudioTime(chat.getAudioTime());
                
                // 填充发送者信息
                if (chat.getSendUid() != null) {
                    WebUser sender = userMap.get(chat.getSendUid());
                    if (sender != null) {
                        vo.setSenderName(sender.getUsername());
                        vo.setSenderAvatar(sender.getAvatar());
                    }
                }
                
                return vo;
            }).collect(Collectors.toList());
            
            // 创建新的Page对象，包含VO数据
            Page<ChatMessageVo> voPage = new Page<>();
            voPage.setCurrent(page.getCurrent());
            voPage.setSize(page.getSize());
            voPage.setTotal(page.getTotal());
            voPage.setPages(page.getPages());
            voPage.setRecords(voList);
            
            return Result.ok(voPage);
        }
        
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
     * 清除聊天数量
     *
     * @param sendUid 发送方用户ID（私聊时使用）或群聊ID（群聊时使用）
     * @param type    类型
     * @param productId 商品ID（商品聊天时使用）
     * @param groupChatId 群聊ID（群聊时使用）
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
}
