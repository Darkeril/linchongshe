package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.web.domain.dto.AlbumDTO;
import com.hongshu.web.domain.entity.WebAlbum;
import com.hongshu.web.domain.vo.AlbumVo;

import java.util.List;

/**
 * 专辑
 *
 * @author: hongshu
 */
public interface IWebAlbumService extends IService<WebAlbum> {

    /**
     * 获取专辑列表
     *
     * @param userId 用户ID
     */
    List<WebAlbum> getAlbumList(String userId);

    /**
     * 根据用户ID获取专辑
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     */
    Page<WebAlbum> getAlbumByUserId(long currentPage, long pageSize, String userId);

    /**
     * 保存专辑
     *
     * @param albumDTO 专辑
     */
    void saveAlbum(AlbumDTO albumDTO);

    /**
     * 根据专辑ID获取专辑
     *
     * @param albumId 专辑ID
     */
    AlbumVo getAlbumById(String albumId);

    /**
     * 删除专辑
     *
     * @param albumId 专辑ID
     */
    void deleteAlbum(String albumId);

    /**
     * 更新专辑
     *
     * @param albumDTO 专辑
     */
    void updateAlbum(AlbumDTO albumDTO);

}
