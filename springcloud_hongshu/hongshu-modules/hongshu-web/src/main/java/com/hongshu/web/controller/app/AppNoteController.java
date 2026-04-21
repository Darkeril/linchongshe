package com.hongshu.web.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.dto.NoteAppDTO;
import com.hongshu.web.domain.vo.NoteVo;
import com.hongshu.web.service.web.IWebNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 笔记
 *
 * @author: hongshu
 */
@RequestMapping("/app/note")
@RestController
public class AppNoteController {

    @Autowired
    IWebNoteService noteService;


    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    @GetMapping("getNoteById")
    @NoLoginIntercept
    public Result<?> getNoteById(String noteId) {
        NoteVo noteVo = noteService.getNoteById(noteId);
        return Result.ok(noteVo);
    }

    /**
     * 新增笔记
     *
     * @param requestId 唯一标识
     * @param noteData  笔记对象
     * @param coverFile 封面图
     * @param files     图片文件
     */
    @PostMapping("saveNoteByDTO")
    public Result<?> saveNoteByDTO(@RequestParam("requestId") String requestId,
                                   @RequestParam("noteData") String noteData,
                                   @RequestParam("coverFile") MultipartFile coverFile,
                                   @RequestParam("uploadFiles") MultipartFile[] files) {
        String terminal = "app";
        noteService.saveNoteByDTO(terminal, requestId, noteData, coverFile, files);
        return Result.ok();
    }

    /**
     * 新增笔记
     *
     * @param requestId 唯一标识
     * @param noteDTO   笔记对象
     */
    @PostMapping("saveNote")
    public Result<?> saveNote(@RequestParam("requestId") String requestId,
                              @RequestBody NoteAppDTO noteDTO) {
        String terminal = "app";
        noteService.saveNote(terminal, requestId, noteDTO);
        return Result.ok();
    }

    /**
     * 删除笔记
     *
     * @param noteIds 笔记ID集合
     */
    @PostMapping("deleteNoteByIds")
    public Result<?> deleteNoteByIds(@RequestBody List<String> noteIds) {
        noteService.deleteNoteByIds(noteIds);
        return Result.ok();
    }

    /**
     * 更新笔记
     *
     * @param noteData 笔记对象
     * @param files    图片文件
     */
    @PostMapping("updateNoteByDTO")
    public Result<?> updateNoteByDTO(@RequestParam("noteData") String noteData,
                                     @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                                     @RequestParam(value = "uploadFiles", required = false) MultipartFile[] files
    ) {
        noteService.updateNoteByDTO(noteData, coverFile, files);
        return Result.ok();
    }

    /**
     * 获取热门笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getHotPage/{currentPage}/{pageSize}")
    public Result<?> getHotPage(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<NoteVo> pageInfo = noteService.getHotPage(currentPage, pageSize);
        return Result.ok(pageInfo);
    }

    /**
     * 置顶笔记
     *
     * @param noteId 笔记ID
     */
    @GetMapping("pinnedNote")
    public Result<?> pinnedNote(String noteId) {
        boolean flag = noteService.pinnedNote(noteId);
        return Result.ok(flag);
    }
}
