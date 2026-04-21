package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.UploadConfigCommand;
import com.hongshu.ai.domain.vo.UploadConfigVO;
import com.hongshu.ai.service.IUploadConfigService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 缩略图配置接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/upload-config")
public class UploadConfigController extends BaseController {

    @Autowired
    private IUploadConfigService uploadConfigService;


    /**
     * 查询缩略图配置分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('gpt:upload:config:list')")
    public ResponseInfo<IPageInfo<UploadConfigVO>> pageUploadConfig(@RequestParam Map map) {
        return uploadConfigService.pageUploadConfig(new Query(map, true));
    }

    /**
     * 查询缩略图配置列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('gpt:upload:config:list')")
    public ResponseInfo<List<UploadConfigVO>> listUploadConfig(@RequestParam Map map) {
        return uploadConfigService.listUploadConfig(new Query(map));
    }

    /**
     * 获取缩略图配置详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('gpt:upload:config:query')")
    public ResponseInfo<UploadConfigVO> getUploadConfigById(@PathVariable("id") Long id) {
        return uploadConfigService.getUploadConfigById(id);
    }

    /**
     * 新增缩略图配置
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PostMapping
    @PreAuthorize("hasAuthority('gpt:upload:config:save')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveUploadConfig(@Validated @RequestBody UploadConfigCommand command) {
        return uploadConfigService.saveUploadConfig(command);
    }

    /**
     * 修改缩略图配置
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PutMapping
    @PreAuthorize("hasAuthority('gpt:upload:config:update')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateUploadConfig(@Validated @RequestBody UploadConfigCommand command) {
        return uploadConfigService.updateUploadConfig(command);
    }

    /**
     * 批量删除缩略图配置
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAuthority('gpt:upload:config:remove')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeUploadConfigByIds(@PathVariable List<Long> ids) {
        return uploadConfigService.removeUploadConfigByIds(ids);
    }

}
