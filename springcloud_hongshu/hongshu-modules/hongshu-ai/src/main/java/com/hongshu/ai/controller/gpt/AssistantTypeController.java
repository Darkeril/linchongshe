package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.AssistantTypeCommand;
import com.hongshu.ai.domain.vo.AssistantTypeVO;
import com.hongshu.ai.service.IAssistantTypeService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 助手分类接口
 *
 * @author: myj
 * @date: 2023-11-22
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/assistant-type")
public class AssistantTypeController extends BaseController {

    @Autowired
    private IAssistantTypeService assistantTypeService;


    /**
     * 查询助手分类分页列表
     *
     * @author: myj
     * @date: 2023-11-22
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:assistant:type:list')" )
    public ResponseInfo<IPageInfo<AssistantTypeVO>> pageAssistantType(@RequestParam Map map) {
        return assistantTypeService.pageAssistantType(new Query(map, true));
    }

    /**
     * 查询助手分类列表
     *
     * @author: myj
     * @date: 2023-11-22
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:assistant:type:list')" )
    public ResponseInfo<List<AssistantTypeVO>> listAssistantType(@RequestParam Map map) {
        return assistantTypeService.listAssistantType(new Query(map));
    }

    /**
     * 获取助手分类详细信息
     *
     * @author: myj
     * @date: 2023-11-22
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:assistant:type:query')" )
    public ResponseInfo<AssistantTypeVO> getAssistantTypeById(@PathVariable("id") Long id) {
        return assistantTypeService.getAssistantTypeById(id);
    }

    /**
     * 新增助手分类
     *
     * @author: myj
     * @date: 2023-11-22
     * @version: 1.0.0
     */
    @PostMapping
//    @PreAuthorize("hasAuthority('gpt:assistant:type:save')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveAssistantType(@Validated @RequestBody AssistantTypeCommand command) {
        return assistantTypeService.saveAssistantType(command);
    }

    /**
     * 修改助手分类
     *
     * @author: myj
     * @date: 2023-11-22
     * @version: 1.0.0
     */
    @PutMapping
//    @PreAuthorize("hasAuthority('gpt:assistant:type:update')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateAssistantType(@Validated @RequestBody AssistantTypeCommand command) {
        return assistantTypeService.updateAssistantType(command);
    }

    /**
     * 批量删除助手分类
     *
     * @author: myj false
     * @date: 2023-11-22
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:assistant:type:remove')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeAssistantTypeByIds(@PathVariable List<Long> ids) {
        return assistantTypeService.removeAssistantTypeByIds(ids);
    }

}
