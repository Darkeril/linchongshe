package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.UploadFileCommand;
import com.hongshu.ai.domain.vo.UploadFileVO;
import com.hongshu.ai.service.IUploadFileService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文件管理接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/file")
public class FileController extends BaseController {

    @Autowired
    private IUploadFileService fileService;


    /**
     * 查询文件管理分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('gpt:file:list')")
    public ResponseInfo<IPageInfo<UploadFileVO>> pageFile(@RequestParam Map map) {
        return fileService.pageFile(new Query(map, true));
    }

    /**
     * 查询文件管理列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('gpt:file:list')")
    public ResponseInfo<List<UploadFileVO>> listFile(@RequestParam Map map) {
        return fileService.listFile(new Query(map));
    }

    /**
     * 获取文件管理详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('gpt:file:query')")
    public ResponseInfo<UploadFileVO> getFileById(@PathVariable("id") Long id) {
        return fileService.getFileById(id);
    }

    /**
     * 新增文件管理
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PostMapping
    @PreAuthorize("hasAuthority('gpt:file:save')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveFile(@Validated @RequestBody UploadFileCommand command) {
        return fileService.saveFile(command);
    }

    /**
     * 修改文件管理
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PutMapping
    @PreAuthorize("hasAuthority('gpt:file:update')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateFile(@Validated @RequestBody UploadFileCommand command) {
        return fileService.updateFile(command);
    }

    /**
     * 批量删除文件管理
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAuthority('gpt:file:remove')")
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeFileByIds(@PathVariable List<Long> ids) {
        return fileService.removeFileByIds(ids);
    }

}
