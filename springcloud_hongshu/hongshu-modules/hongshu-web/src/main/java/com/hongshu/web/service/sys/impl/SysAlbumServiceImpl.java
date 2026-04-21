package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.exception.ServiceException;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebAlbum;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.sys.SysAlbumMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.ISysAlbumService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会员信息 服务层处理
 *

 */
@Slf4j
@Service
public class SysAlbumServiceImpl implements ISysAlbumService {

    @Autowired
    private SysAlbumMapper albumMapper;

    @Autowired
    private WebUserMapper webUserMapper;

    /**
     * 查询会员信息集合
     *
     * @param query 会员信息
     */
    @Override
    public List<WebAlbum> getAlbumList(Query query) {
        QueryWrapper<WebAlbum> qw = new QueryWrapper<>();
        qw.lambda().like(ValidatorUtil.isNotNull(query.getTitle()), WebAlbum::getTitle, query.getTitle());
        qw.lambda().ge(ValidatorUtil.isNotNull(query.get("params[beginTime]")), WebAlbum::getCreateTime, query.get("params[beginTime]"));
        qw.lambda().le(ValidatorUtil.isNotNull(query.get("params[endTime]")), WebAlbum::getCreateTime, query.get("params[endTime]"));
        qw.orderByDesc("create_time");
        List<WebAlbum> list = albumMapper.selectList(qw);
        fillOwnerUsernames(list);
        return list;
    }

    /**
     * 查询所有会员
     */
    @Override
    public List<WebAlbum> selectAlbumAll() {
        List<WebAlbum> list = albumMapper.selectList(new QueryWrapper<>());
        fillOwnerUsernames(list);
        return list;
    }

    /** 批量填充所属用户名，供管理端列表展示 */
    private void fillOwnerUsernames(List<WebAlbum> albums) {
        if (albums == null || albums.isEmpty()) {
            return;
        }
        Set<String> uids = new HashSet<>();
        for (WebAlbum a : albums) {
            if (ValidatorUtil.isNotNull(a.getUid()) && !a.getUid().trim().isEmpty()) {
                uids.add(a.getUid().trim());
            }
        }
        if (uids.isEmpty()) {
            return;
        }
        List<WebUser> users = webUserMapper.selectBatchIds(new ArrayList<>(uids));
        Map<String, String> uidToName = users.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(WebUser::getId,
                        u -> u.getUsername() != null ? u.getUsername() : "",
                        (x, y) -> x));
        for (WebAlbum a : albums) {
            if (ValidatorUtil.isNotNull(a.getUid()) && uidToName.containsKey(a.getUid().trim())) {
                a.setOwnerUsername(uidToName.get(a.getUid().trim()));
            }
        }
    }

    /**
     * 通过会员ID查询会员信息
     *
     * @param id 会员ID
     */
    @Override
    public WebAlbum getAlbumById(Long id) {
        return albumMapper.selectById(id);
    }

    /**
     * 新增会员信息
     *
     * @param album 会员信息
     */
    @Override
    public int insertAlbum(WebAlbum album) {
        if (ValidatorUtil.isNull(album.getUid()) || album.getUid().trim().isEmpty()) {
            throw new ServiceException("请选择所属用户");
        }
        Date now = new Date();
        album.setCreateTime(now);
        album.setUpdateTime(now);
        if (album.getType() == null) {
            album.setType(0);
        }
        if (album.getImgCount() == null) {
            album.setImgCount(0L);
        }
        return albumMapper.insert(album);
    }

    /**
     * 修改保存会员信息
     *
     * @param album 会员信息
     */
    @Override
    public int updateAlbum(WebAlbum album) {
        WebAlbum webAlbum = albumMapper.selectById(album.getId());
        if (ValidatorUtil.isNull(webAlbum)) {
            throw new ServiceException("专辑不存在");
        }
        webAlbum.setTitle(album.getTitle());
        webAlbum.setSort(album.getSort());
        if (ValidatorUtil.isNotNull(album.getAlbumCover())) {
            webAlbum.setAlbumCover(album.getAlbumCover());
        }
        if (ValidatorUtil.isNotNull(album.getUid()) && !album.getUid().trim().isEmpty()) {
            webAlbum.setUid(album.getUid().trim());
        }
        webAlbum.setUpdater("System");
        webAlbum.setUpdateTime(new Date());
        return albumMapper.updateById(webAlbum);
    }

    /**
     * 删除会员信息
     *
     * @param id 会员ID
     */
    @Override
    public int deleteAlbumById(Long id) {
        return albumMapper.deleteById(id);
    }

    /**
     * 批量删除会员信息
     *
     * @param ids 需要删除的会员ID
     */
    @Override
    public int deleteAlbumByIds(Long[] ids) {
        List<Long> longs = Arrays.asList(ids);
        for (Long id : ids) {
            WebAlbum album = getAlbumById(id);
            if (ValidatorUtil.isNull(album)) {
                log.info("用户不存在:{}", id);
                longs.remove(id);
            }
        }
        return albumMapper.deleteBatchIds(longs);
    }
}
