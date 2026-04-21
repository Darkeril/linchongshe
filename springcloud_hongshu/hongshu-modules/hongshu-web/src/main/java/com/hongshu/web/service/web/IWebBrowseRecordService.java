package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.dto.BrowseRecordDTO;
import com.hongshu.web.domain.entity.WebNote;

import java.util.List;

/**
 * 浏览记录
 *
 * @author: hongshu
 */
public interface IWebBrowseRecordService extends IService<WebNote> {

    /**
     * 获取浏览记录
     */
    Page<NoteSearchVo> getAllBrowseRecordByUser(long page, long limit);

    /**
     * 添加浏览记录
     */
    void addBrowseRecord(BrowseRecordDTO browseRecordDTO);

    /**
     * 删除浏览记录
     */
    void delRecord(String uid, List<String> idList);
}
