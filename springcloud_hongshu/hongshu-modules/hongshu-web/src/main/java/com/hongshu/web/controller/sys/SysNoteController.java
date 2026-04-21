package com.hongshu.web.controller.sys;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.utils.poi.ExcelUtil;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.service.sys.ISysNoteService;
import com.hongshu.web.domain.vo.NoteDataCenterVo;
import com.hongshu.web.domain.vo.NoteDataCenterDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 笔记操作处理
 *

 */
@RestController
@RequestMapping("/note")
public class SysNoteController extends BaseController {

    @Autowired
    private ISysNoteService noteService;


    /**
     * 获取笔记列表
     */
    @GetMapping("/allList")
    public TableDataInfo list() {
        this.startPage();
        List<WebNote> noteList = noteService.getAllNoteList();
        return getDataTable(noteList);
    }

    /**
     * 数据中心-热门内容-笔记数据列表（分页）
     * 参数：noteType(笔记题材 1=图文 2=视频 3=live图)、startTime、endTime、pageNum、pageSize
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/dataCenterList")
    public TableDataInfo dataCenterList(@RequestParam Map<String, Object> map) {
        this.startPage();
        List<NoteDataCenterVo> list = noteService.selectNoteDataCenterList(new Query(map));
        return getDataTable(list);
    }

    /**
     * 数据中心-笔记数据详情（单条笔记的曝光、观看、来源、时段、趋势等）
     * 参数：notesId 笔记ID、days 统计天数（7、14 或 30，默认 30）
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/dataCenterDetail")
    public AjaxResult getDataCenterDetail(@RequestParam String notesId,
                                          @RequestParam(required = false, defaultValue = "30") Integer days) {
        NoteDataCenterDetailVo vo = noteService.getNoteDataCenterDetail(notesId, days);
        return vo != null ? success(vo) : error("笔记不存在");
    }

    /**
     * 获取笔记列表
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<WebNote> noteList = noteService.selectNoteList(new Query(map));
        return getDataTable(noteList);
    }

    /**
     * 获取未审核笔记列表
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/unAuditList")
    public TableDataInfo UnAuditList(@RequestParam Map map) {
        this.startPage();
        List<WebNote> noteList = noteService.selectUnAuditNoteList(new Query(map));
        return getDataTable(noteList);
    }

    /**
     * 根据笔记编号获取详细信息（路径变量仅匹配数字，避免与 /dataCenterDetail 等字面路径冲突）
     */
    @PreAuthorize("@ss.hasPermi('web:note:query')")
    @GetMapping(value = "/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(noteService.selectNoteById(id));
    }

    /**
     * 新增笔记
     */
    @PreAuthorize("@ss.hasPermi('web:note:add')")
    @Log(title = "笔记管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam("note") String note, @RequestParam(value = "file", required = false) MultipartFile file) {
        WebNote webNote = JSONUtil.toBean(note, WebNote.class);
        return toAjax(noteService.insertNote(webNote, file));
    }

    /**
     * 修改笔记
     */
    @PreAuthorize("@ss.hasPermi('web:note:edit')")
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestParam("note") String note, @RequestParam(value = "file", required = false) MultipartFile file) {
        WebNote webNote = JSONUtil.toBean(note, WebNote.class);
        return toAjax(noteService.updateNote(webNote, file));
    }

    /**
     * 删除笔记
     */
    @PreAuthorize("@ss.hasPermi('web:note:remove')")
    @Log(title = "笔记管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(noteService.deleteNoteByIds(ids));
    }

    /**
     * 导出笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('web:note:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Query query) {
        List<WebNote> noteList = noteService.selectNoteList(query);
        ExcelUtil<WebNote> util = new ExcelUtil<>(WebNote.class);
        util.exportExcel(response, noteList, "笔记数据");
    }

    /**
     * 审核管理(0:未审核 1：通过 2：拒绝)
     */
    @PutMapping("auditNote")
    public Result<?> auditNote(@RequestBody Map<String, String> map) {
        String noteId = map.get("noteId");
        String auditType = map.get("auditType");
        noteService.auditNote(noteId, auditType);
        return Result.ok();
    }

    /**
     * 批量笔记审核(0:未审核 1：通过 2：拒绝)
     */
    @PutMapping("batchAuditNote")
    public Result<?> batchAuditNote(@RequestBody Map<String, String> map) {
        String noteIds = map.get("noteIds"); // 逗号分隔的ID字符串
        String auditType = map.get("auditType");

        // 分割ID字符串并逐个处理
        if (noteIds != null && !noteIds.isEmpty()) {
            String[] idArray = noteIds.split(",");
            for (String noteId : idArray) {
                noteService.auditNote(noteId.trim(), auditType);
            }
        }
        return Result.ok();
    }
}
