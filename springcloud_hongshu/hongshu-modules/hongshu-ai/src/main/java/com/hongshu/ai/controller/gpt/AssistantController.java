package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.AssistantCommand;
import com.hongshu.ai.domain.vo.AssistantVO;
import com.hongshu.ai.service.IAssistantService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI助理功能接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/assistant")
public class AssistantController extends BaseController {

    @Autowired
    private IAssistantService assistantService;


    /**
     * 查询AI助理功能分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:assistant:list')" )
    public ResponseInfo<IPageInfo<AssistantVO>> pageAssistant(@RequestParam Map map) {
        return assistantService.pageAssistant(new Query(map, true));
    }

    /**
     * 查询AI助理功能列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:assistant:list')" )
    public ResponseInfo<List<AssistantVO>> listAssistant(@RequestParam Map map) {
        return assistantService.listAssistant(new Query(map));
    }

    /**
     * 获取AI助理功能详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:assistant:query')" )
    public ResponseInfo<AssistantVO> getAssistantById(@PathVariable("id") Long id) {
        return assistantService.getAssistantById(id);
    }

    /**
     * 新增AI助理功能
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PostMapping
//    @PreAuthorize("hasAuthority('gpt:assistant:save')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveAssistant(@Validated @RequestBody AssistantCommand command) {
        return assistantService.saveAssistant(command);
    }

    /**
     * 修改AI助理功能
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PutMapping
//    @PreAuthorize("hasAuthority('gpt:assistant:update')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateAssistant(@Validated @RequestBody AssistantCommand command) {
        return assistantService.updateAssistant(command);
    }

    /**
     * 批量删除AI助理功能
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:assistant:remove')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeAssistantByIds(@PathVariable List<Long> ids) {
        return assistantService.removeAssistantByIds(ids);
    }

}
