package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.vo.CommentVo;
import com.hongshu.web.service.sys.ISysCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论操作处理
 *

 */
@RestController
@RequestMapping("/comment")
public class SysCommentController extends BaseController {

    @Autowired
    private ISysCommentService commentService;


    /**
     * 查询评论列表
     */
    @PreAuthorize("@ss.hasPermi('web:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<CommentVo> commentList = commentService.selectCommentList(new Query(map));
        return getDataTable(commentList);
    }

    /**
     * 查询一级以下评论
     */
    @PreAuthorize("@ss.hasPermi('web:comment:list')")
    @GetMapping("/treeList")
    public TableDataInfo treeList(@RequestParam Map map) {
        this.startPage();
        List<CommentVo> commentList = commentService.selectTreeList(new Query(map));
        return getDataTable(commentList);
    }

    /**
     * 根据评论编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:comment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(commentService.selectCommentById(id));
    }

    /**
     * 根据笔记编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:comment:query')")
    @GetMapping(value = "/list/{nid}")
    public AjaxResult getComment(@PathVariable Long nid) {
        return success(commentService.selectCommentByNid(nid));
    }

    /**
     * 删除评论
     */
    @PreAuthorize("@ss.hasPermi('web:comment:remove')")
    @Log(title = "评论管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(commentService.deleteCommentByIds(ids));
    }
}
