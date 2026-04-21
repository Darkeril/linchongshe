package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.web.domain.Query;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.entity.WebAlbum;
import com.hongshu.web.service.sys.ISysAlbumService;
import com.hongshu.web.service.web.IWebAlbumNoteRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签操作处理
 *

 */
@RestController
@RequestMapping("/album")
public class SysAlbumController extends BaseController {

    @Autowired
    private ISysAlbumService albumService;

    @Autowired
    private IWebAlbumNoteRelationService albumNoteRelationService;

    /**
     * 获取会员列表
     */
    @PreAuthorize("@ss.hasPermi('web:album:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<WebAlbum> albumList = albumService.getAlbumList(new Query(map));
        return getDataTable(albumList);
    }

    /**
     * 根据会员编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:album:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(albumService.getAlbumById(id));
    }

    /**
     * 管理端：分页查询专辑内笔记（含未上架等，供专辑管理查看内容）
     */
    @PreAuthorize("@ss.hasPermi('web:album:query')")
    @GetMapping("/{id}/notes")
    public AjaxResult listAlbumNotes(
            @PathVariable("id") String id,
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<NoteSearchVo> page = albumNoteRelationService.pageNotesByAlbumIdForAdmin(id, current, size);
        return success(page);
    }

    /**
     * 新增会员
     */
    @PreAuthorize("@ss.hasPermi('web:album:add')")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WebAlbum album) {
        return toAjax(albumService.insertAlbum(album));
    }

    /**
     * 修改会员
     */
    @PreAuthorize("@ss.hasPermi('web:album:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WebAlbum album) {
        return toAjax(albumService.updateAlbum(album));
    }

    /**
     * 删除会员
     */
    @PreAuthorize("@ss.hasPermi('web:album:remove')")
    @Log(title = "会员管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(albumService.deleteAlbumByIds(ids));
    }
}
