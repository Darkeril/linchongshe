package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.ModelCommand;
import com.hongshu.ai.domain.vo.ModelVO;
import com.hongshu.ai.service.IModelService;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 大模型信息接口
 *
 * @author: myj
 * @date: 2023-12-01
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/model")
public class ModelController extends BaseController {

    @Autowired
    private IModelService modelService;


    /**
     * 查询大模型信息分页列表
     *
     * @author: myj
     * @date: 2023-12-01
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:model:list')" )
    public ResponseInfo<IPageInfo<ModelVO>> pageModel(@RequestParam Map map) {
        return modelService.pageModel(new Query(map, true));
    }

    /**
     * 查询大模型信息列表
     *
     * @author: myj
     * @date: 2023-12-01
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:model:list')" )
    public ResponseInfo<List<ModelVO>> listModel(@RequestParam Map map) {
        return modelService.listModel(new Query(map));
    }

    /**
     * 获取大模型信息详细信息
     *
     * @author: myj
     * @date: 2023-12-01
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:model:query')" )
    public ResponseInfo<ModelVO> getModelById(@PathVariable("id") Long id) {
        return modelService.getModelById(id);
    }

    /**
     * 新增大模型信息
     *
     * @author: myj
     * @date: 2023-12-01
     * @version: 1.0.0
     */
    @PostMapping
//    @PreAuthorize("hasAuthority('gpt:model:save')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveModel(@Validated @RequestBody ModelCommand command) {
        return modelService.saveModel(command);
    }

    /**
     * 修改大模型信息
     *
     * @author: myj
     * @date: 2023-12-01
     * @version: 1.0.0
     */
    @PutMapping
//    @PreAuthorize("hasAuthority('gpt:model:update')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateModel(@Validated @RequestBody ModelCommand command) {
        return modelService.updateModel(command);
    }

    /**
     * 批量删除大模型信息
     *
     * @author: myj false
     * @date: 2023-12-01
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:model:remove')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeModelByIds(@PathVariable List<Long> ids) {
        return modelService.removeModelByIds(ids);
    }

}
