package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.controller.base.BaseController;
import com.hongshu.ai.domain.vo.ChatVO;
import com.hongshu.ai.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天摘要接口
 *
 * @author: Yang
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/chat")
public class ChatController extends BaseController {

    @Autowired
    private IChatService chatService;


    /**
     * 查询聊天摘要分页列表
     *
     * @author: Yang
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:chat:list')")
    public ResponseInfo<IPageInfo<ChatVO>> pageChat(@RequestParam Map map) {
        return chatService.pageChat(new Query(map, true));
    }

    /**
     * 查询聊天摘要列表
     *
     * @author: Yang
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:chat:list')")
    public ResponseInfo<List<ChatVO>> listChat(@RequestParam Map map) {
        return chatService.listChat(new Query(map));
    }

    /**
     * 获取聊天摘要详细信息
     *
     * @author: Yang
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:chat:query')")
    public ResponseInfo<ChatVO> getChatById(@PathVariable("id") Long id) {
        return chatService.getChatById(id);
    }

    /**
     * 批量删除聊天摘要
     *
     * @author: Yang false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:chat:remove')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeChatByIds(@PathVariable List<Long> ids) {
        return chatService.removeChatByIds(ids);
    }

}
