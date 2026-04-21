package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.entity.WebAlbumNoteRelation;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebAlbumNoteRelationMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebAlbumNoteRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专辑-笔记
 *
 * @author: hongshu
 */
@Service
public class WebAlbumNoteRelationServiceImpl extends ServiceImpl<WebAlbumNoteRelationMapper, WebAlbumNoteRelation> implements IWebAlbumNoteRelationService {

    @Autowired
    private WebNoteMapper noteMapper;

    @Autowired
    private WebUserMapper userMapper;


    /**
     * 得到当前专辑下的所有笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param albumId     专辑ID
     * @param userId      用户ID
     */
    @Override
    public Page<NoteSearchVo> getNoteByAlbumId(long currentPage, long pageSize, String albumId, String userId) {
        Page<NoteSearchVo> result = new Page<>();
        Page<WebAlbumNoteRelation> albumImgRelationPage = this.page(new Page<>(currentPage, pageSize), new QueryWrapper<WebAlbumNoteRelation>().eq("aid", albumId).orderByDesc("create_time"));
        long total = albumImgRelationPage.getTotal();
        List<WebAlbumNoteRelation> records = albumImgRelationPage.getRecords();
        List<String> nids = records.stream().map(WebAlbumNoteRelation::getNid).collect(Collectors.toList());
//        String currentUser = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUser = AuthContextHolder.getUserId();
        List<WebNote> noteList;
        if (currentUser.equals(userId)) {
            // 表示是当前用户(能够查看所有的专辑，包括上传中的笔记)
            noteList = noteMapper.selectBatchIds(nids);
        } else {
            noteList = noteMapper.selectList(new QueryWrapper<WebNote>().in("id", nids).eq("status", 1));
        }
        List<String> uids = noteList.stream().map(WebNote::getUid).collect(Collectors.toList());
        List<WebUser> userList = userMapper.selectBatchIds(uids);
        HashMap<String, WebUser> userMap = new HashMap<>(16);
        userList.forEach(item -> {
            userMap.put(String.valueOf(item.getId()), item);
        });
        HashMap<String, WebNote> noteMap = new HashMap<>(16);
        noteList.forEach(item -> {
            noteMap.put(String.valueOf(item.getId()), item);
        });
        List<NoteSearchVo> noteVoList = new ArrayList<>();
        WebNote note;
        WebUser user;
        NoteSearchVo noteSearchVo;
        for (WebAlbumNoteRelation model : records) {
            note = noteMap.get(model.getNid());
            user = userMap.get(note.getUid());
            noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
            noteSearchVo.setUsername(user.getUsername())
                    .setAvatar(user.getAvatar());
            noteVoList.add(noteSearchVo);
        }
        result.setTotal(total);
        result.setRecords(noteVoList);
        return result;
    }

    @Override
    public Page<NoteSearchVo> pageNotesByAlbumIdForAdmin(String albumId, long currentPage, long pageSize) {
        Page<NoteSearchVo> result = new Page<>();
        Page<WebAlbumNoteRelation> albumPage = this.page(
                new Page<>(currentPage, pageSize),
                new QueryWrapper<WebAlbumNoteRelation>()
                        .eq("aid", albumId)
                        .orderByDesc("create_time"));
        List<WebAlbumNoteRelation> records = albumPage.getRecords();
        if (records.isEmpty()) {
            result.setTotal(albumPage.getTotal());
            result.setRecords(new ArrayList<>());
            return result;
        }
        List<String> nids = records.stream().map(WebAlbumNoteRelation::getNid).collect(Collectors.toList());
        List<WebNote> noteList = nids.isEmpty() ? new ArrayList<>() : noteMapper.selectBatchIds(nids);
        Map<String, WebNote> noteMap = noteList.stream()
                .collect(Collectors.toMap(WebNote::getId, n -> n, (a, b) -> a));
        List<String> uids = noteList.stream().map(WebNote::getUid).filter(u -> u != null).distinct()
                .collect(Collectors.toList());
        List<WebUser> userList = uids.isEmpty() ? new ArrayList<>() : userMapper.selectBatchIds(uids);
        Map<String, WebUser> userMap = userList.stream()
                .collect(Collectors.toMap(u -> String.valueOf(u.getId()), u -> u, (a, b) -> a));

        List<NoteSearchVo> voList = new ArrayList<>();
        for (WebAlbumNoteRelation rel : records) {
            WebNote note = noteMap.get(rel.getNid());
            if (note == null) {
                continue;
            }
            WebUser user = userMap.get(note.getUid());
            NoteSearchVo vo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
            if (vo == null) {
                continue;
            }
            if (user != null) {
                vo.setUsername(user.getUsername()).setAvatar(user.getAvatar());
            }
            voList.add(vo);
        }
        result.setRecords(voList);
        result.setTotal(albumPage.getTotal());
        return result;
    }
}
