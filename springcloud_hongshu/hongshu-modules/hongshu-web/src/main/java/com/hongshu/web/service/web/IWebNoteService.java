package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.web.domain.dto.NoteAppDTO;
import com.hongshu.web.domain.dto.NoteDTO;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.vo.NoteVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 笔记
 *
 * @author: hongshu
 */
public interface IWebNoteService extends IService<WebNote> {

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    NoteVo getNoteById(String noteId);

    /**
     * 新增笔记
     *
     * @param terminal  来源标识
     * @param requestId 唯一标识
     * @param noteData  笔记对象
     * @param coverFile 封面图
     * @param files     图片文件
     */
    void saveNoteByDTO(String terminal, String requestId, String noteData, MultipartFile coverFile, MultipartFile[] files);

    void saveNote(String terminal, String requestId, NoteAppDTO noteDTO);

    /**
     * 更新笔记
     *
     * @param noteData 笔记对象
     * @param files    图片文件
     */
    void updateNoteByDTO(String noteData, MultipartFile coverFile, MultipartFile[] files);

    /**
     * 删除笔记
     *
     * @param noteIds 笔记ID集合
     */
    void deleteNoteByIds(List<String> noteIds);

    /**
     * 获取热门笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteVo> getHotPage(long currentPage, long pageSize);

    /**
     * 置顶笔记
     *
     * @param noteId 笔记ID
     */
    boolean pinnedNote(String noteId);

    Object getNoteCount(String userId);

}
