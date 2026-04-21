package com.hongshu.ai.controller.app;

import com.hongshu.ai.api.base.service.LLMService;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.controller.base.BaseAppController;
import com.hongshu.ai.domain.command.ChatCommand;
import com.hongshu.ai.domain.vo.ChatVO;
import com.hongshu.ai.service.GptService;
import com.hongshu.ai.service.IChatMessageService;
import com.hongshu.ai.service.IChatService;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.common.core.validator.base.BaseAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 对话接口
 *
 * @author: Yang
 * @date: 2023/5/4
 * @version: 1.0.0
 */
@RestController(value = "appChatController")
@RequestMapping("/v1/chat")
public class ChatController extends BaseAppController {

    @Autowired
    private IChatService chatService;
    @Autowired
    private IChatMessageService chatMessageService;
    @Autowired
    private GptService gptService;
    @Autowired
    private LLMService llmService;


    /**
     * 获取聊天列表
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @GetMapping()
    public ResponseInfo listChat(@RequestParam Map map) {
        map.put("userId", getLoginUser().getId());
        return chatService.listChat(new Query(map));
    }

    /**
     * 删除聊天列表
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @DeleteMapping("/{chatNumber}")
    public ResponseInfo deleteChat(@PathVariable("chatNumber") String chatNumber) {
        return chatService.removeChatByChatNumber(chatNumber);
    }

    /**
     * 获取聊天内容列表
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @GetMapping("/message")
    public ResponseInfo listChatMessage(@RequestParam Map map) {
        BaseAssert.isBlankOrNull(map.get("chatNumber"), "缺少会话标识");
        Query query = new Query(map);
        query.put("status", ChatStatusEnum.SUCCESS.getValue());
        return chatMessageService.listChatMessage(query);
    }

    /**
     * 获取聊天内容
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @GetMapping("/message/{messageId}")
    public ResponseInfo listChatMessageById(@PathVariable String messageId) {
        return chatMessageService.getChatByMessageId(messageId);
    }

    /**
     * 删除聊天内容
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @DeleteMapping("/message/{messageId}")
    public ResponseInfo removeChatMessageById(@PathVariable String messageId) {
        return chatMessageService.removeChatMessageByMessageId(messageId);
    }

    /**
     * 创建对话
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @PostMapping()
    public ResponseInfo<ChatVO> saveChat(@RequestBody ChatCommand command) {
        command.setUserId(getUserId());
        return chatService.saveSSEChat(command);
    }

    /**
     * 发送消息
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @PostMapping("/message")
    public ResponseInfo sendMessage(@Validated @RequestBody ChatCommand command) {
        command.setUserId(getUserId());
        command = gptService.validateGptCommand(command);
        return ResponseInfo.success(gptService.chatMessage(command));
    }

    /**
     * 创建sse连接
     *
     * @return
     */
    @GetMapping("/sse/create")
    public SseEmitter createConnect(@RequestHeader(name = "uid") String uid) {
        return llmService.createSse(uid);
    }

    /**
     * 关闭连接
     */
    @GetMapping("/sse/close")
    public void closeConnect(@RequestHeader(name = "uid") String uid) {
        llmService.closeSse(uid);
    }

    /**
     * 对话响应
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @GetMapping("/completions")
    public void completions(HttpServletResponse response, 
                           @RequestParam(required = false) String ws, 
                           @RequestParam(required = false) String conversationId) {
        Boolean isWs = false;
        if (ValidatorUtil.isNotNull(ws)) {
            isWs = Boolean.valueOf(ws);
        }
        // 🔧 修复：使用 getUserId() 而不是 getLoginUser().getUid()，兼容 uniapp 用户
        String uid = String.valueOf(getUserId());
        llmService.sseChat(response, isWs, uid, conversationId);
    }

    /**
     * 同步响应
     *
     * @author: Yang
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @PostMapping("/completions/sync")
    public ResponseInfo syncCompletions(@RequestBody ChatCommand command) {
        command.setUserId(getUserId());
        return ResponseInfo.success(llmService.chat(command));
    }

}
