package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.AgreementCommand;
import com.hongshu.ai.domain.vo.AgreementVO;
import com.hongshu.ai.service.IAgreementService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容管理接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/content")
public class AgreementController extends BaseController {

    @Autowired
    private IAgreementService contentService;


    /**
     * 查询内容管理分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('gpt:content:list')")
    public ResponseInfo<IPageInfo<AgreementVO>> pageContent(@RequestParam Map map) {
        return contentService.pageContent(new Query(map, true));
    }

    /**
     * 查询内容管理列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('gpt:content:list')")
    public ResponseInfo<List<AgreementVO>> listContent(@RequestParam Map map) {
        return contentService.listContent(new Query(map));
    }

    /**
     * 获取内容管理详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('gpt:content:query')")
    public ResponseInfo<AgreementVO> getContentById(@PathVariable("id") Long id) {
        return contentService.getContentById(id);
    }

    /**
     * 新增内容管理
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PostMapping
    @PreAuthorize("hasAuthority('gpt:content:save')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveContent(@Validated @RequestBody AgreementCommand command) {
        return contentService.saveContent(command);
    }

    /**
     * 修改内容管理
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PutMapping
    @PreAuthorize("hasAuthority('gpt:content:update')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateContent(@Validated @RequestBody AgreementCommand command) {
        return contentService.updateContent(command);
    }

    /**
     * 批量删除内容管理
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAuthority('gpt:content:remove')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeContentByIds(@PathVariable List<Long> ids) {
        return contentService.removeContentByIds(ids);
    }

}
