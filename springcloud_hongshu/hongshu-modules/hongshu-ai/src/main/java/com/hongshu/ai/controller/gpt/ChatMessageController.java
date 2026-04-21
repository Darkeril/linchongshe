package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.domain.vo.ChatMessageVO;
import com.hongshu.ai.service.IChatMessageService;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 对话消息接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/chat-message")
public class ChatMessageController extends BaseController {

    @Autowired
    private IChatMessageService chatMessageService;


    /**
     * 查询对话消息分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:chat:message:list')" )
    public ResponseInfo<IPageInfo<ChatMessageVO>> pageChatMessage(@RequestParam Map map) {
        return chatMessageService.pageChatMessage(new Query(map, true));
    }

    /**
     * 查询对话消息列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:chat:message:list')" )
    public ResponseInfo<List<ChatMessageVO>> listChatMessage(@RequestParam Map map) {
        return chatMessageService.listChatMessage(new Query(map));
    }

    /**
     * 获取对话消息详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:chat:message:query')" )
    public ResponseInfo<ChatMessageVO> getChatMessageById(@PathVariable("id") Long id) {
        return chatMessageService.getChatMessageById(id);
    }

    /**
     * 批量删除对话消息
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:chat:message:remove')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeChatMessageByIds(@PathVariable List<Long> ids) {
        return chatMessageService.removeChatMessageByIds(ids);
    }

}
