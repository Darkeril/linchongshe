package com.hongshu.web.service.web.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.exception.exceptionType.BusinessException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.dto.LikeOrCollectionDTO;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.CommentVo;
import com.hongshu.web.domain.vo.LikeOrCollectionVo;
import com.hongshu.web.im.ChatUtils;
import com.hongshu.web.mapper.web.*;
import com.hongshu.web.service.web.IWebLikeOrCollectionService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 点赞/收藏
 *
 * @author: hongshu
 */
@Service
public class WebLikeOrCollectionServiceImpl extends ServiceImpl<WebLikeOrCollectionMapper, WebLikeOrCollection> implements IWebLikeOrCollectionService {

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebAlbumMapper albumMapper;
    @Autowired
    private WebCommentMapper commentMapper;
    @Autowired
    private WebCommentSyncMapper commentSyncMapper;
    @Autowired
    private WebAlbumNoteRelationMapper albumNoteRelationMapper;
    @Autowired
    private ChatUtils chatUtils;
    @Autowired
    private ElasticsearchClient elasticsearchClient;


    /**
     * 点赞或收藏
     *
     * @param likeOrCollectionDTO 点赞收藏
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void likeOrCollectionByDTO(LikeOrCollectionDTO likeOrCollectionDTO) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        // 点赞
        if (this.isLikeOrCollection(likeOrCollectionDTO)) {
            this.remove(new QueryWrapper<WebLikeOrCollection>()
                    .eq("uid", currentUid)
                    .eq("like_or_collection_id", likeOrCollectionDTO.getLikeOrCollectionId())
                    .eq("type", likeOrCollectionDTO.getType()));
            this.updateLikeCollectionCount(likeOrCollectionDTO, -1);
            // 更新ES数据
            this.updateEsNote(likeOrCollectionDTO.getLikeOrCollectionId(), false);
        } else {
            // 点赞评论或者笔记
            WebLikeOrCollection likeOrCollection = ConvertUtils.sourceToTarget(likeOrCollectionDTO, WebLikeOrCollection.class);
            likeOrCollection.setTimestamp(System.currentTimeMillis());
            likeOrCollection.setUid(currentUid);
            likeOrCollection.setCreateTime(new Date());
            likeOrCollection.setUpdateTime(new Date());
            this.save(likeOrCollection);
            this.updateLikeCollectionCount(likeOrCollectionDTO, 1);
            // 更新ES数据
            this.updateEsNote(likeOrCollectionDTO.getLikeOrCollectionId(), true);
            // 不是当前用户才进行通知
            if (!likeOrCollectionDTO.getPublishUid().equals(currentUid)) {
                chatUtils.sendMessage(likeOrCollectionDTO.getPublishUid(), 0);
            }
        }
    }

    /**
     * 更新ES浏览记录
     */
    private void updateEsNote(String noteId, boolean b) {
        try {
            // Step 1: 获取现有的数据
            GetResponse<NoteSearchVo> getResponse = elasticsearchClient.get(g ->
                            g.index(NoteConstant.NOTE_INDEX)
                                    .id(noteId),
                    NoteSearchVo.class);
            // 检查是否获取到了数据
            if (getResponse.found()) {
                NoteSearchVo noteSearchVo = getResponse.source();
                // Step 2: 更新 isLike 字段
                noteSearchVo.setIsLike(b);
                if (b) {
                    noteSearchVo.setLikeCount(noteSearchVo.getLikeCount() + 1);
                } else {
                    noteSearchVo.setLikeCount(noteSearchVo.getLikeCount() - 1);
                }
                // Step 3: 将更新后的数据保存回 Elasticsearch
                UpdateResponse<NoteSearchVo> updateResponse = elasticsearchClient.update(u ->
                                u.index(NoteConstant.NOTE_INDEX)
                                        .id(noteId)
                                        .doc(noteSearchVo),
                        NoteSearchVo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否点赞或收藏
     *
     * @param likeOrCollectionDTO 点赞收藏
     */
    @Override
    public boolean isLikeOrCollection(LikeOrCollectionDTO likeOrCollectionDTO) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        long count = this.count(new QueryWrapper<WebLikeOrCollection>()
                .eq("uid", currentUid)
                .eq("like_or_collection_id", likeOrCollectionDTO.getLikeOrCollectionId())
                .eq("type", likeOrCollectionDTO.getType()));
        return count > 0;
    }

    /**
     * 获取当前用户最新的点赞和收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<LikeOrCollectionVo> getNoticeLikeOrCollection(long currentPage, long pageSize) {
        try {
            // 获取当前用户ID
//            String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
            String currentUid = AuthContextHolder.getUserId();
            if (StringUtils.isEmpty(currentUid)) {
                throw new BusinessException("用户未登录");
            }
            // 分页查询点赞收藏记录
            Page<WebLikeOrCollection> likeOrCollectionPage = this.page(
                    new Page<>((int) currentPage, (int) pageSize),
                    new QueryWrapper<WebLikeOrCollection>()
                            .eq("publish_uid", currentUid)
                            .ne("uid", currentUid)
                            .orderByDesc("create_time")
            );
            List<WebLikeOrCollection> likeOrCollectionList = likeOrCollectionPage.getRecords();
            if (CollectionUtils.isEmpty(likeOrCollectionList)) {
                return new Page<LikeOrCollectionVo>().setRecords(Collections.emptyList());
            }
            // 并行查询相关数据
            CompletableFuture<Map<String, WebUser>> userMapFuture = CompletableFuture.supplyAsync(() -> {
                Set<String> uids = likeOrCollectionList.stream()
                        .map(WebLikeOrCollection::getUid)
                        .collect(Collectors.toSet());
                if (uids.isEmpty()) {
                    return new HashMap<>(0);
                }
                return userMapper.selectBatchIds(uids).stream()
                        .collect(Collectors.toMap(WebUser::getId, Function.identity(), (k1, k2) -> k1));
            });
            CompletableFuture<Map<String, WebNote>> noteMapFuture = CompletableFuture.supplyAsync(() -> {
                Set<String> nids = likeOrCollectionList.stream()
                        .filter(e -> e.getType() == 1 || e.getType() == 3)
                        .map(WebLikeOrCollection::getLikeOrCollectionId)
                        .collect(Collectors.toSet());
                if (nids.isEmpty()) {
                    return new HashMap<>(0);
                }
                return noteMapper.selectBatchIds(nids).stream()
                        .collect(Collectors.toMap(WebNote::getId, Function.identity(), (k1, k2) -> k1));
            });
            CompletableFuture<Map<String, CommentVo>> commentMapFuture = CompletableFuture.supplyAsync(() -> {
                Set<String> cids = likeOrCollectionList.stream()
                        .filter(e -> e.getType() == 2)
                        .map(WebLikeOrCollection::getLikeOrCollectionId)
                        .collect(Collectors.toSet());
                if (cids.isEmpty()) {
                    return new HashMap<>(0);
                }
                List<WebComment> commentList = commentMapper.selectBatchIds(cids);
                Set<String> noteIds = commentList.stream()
                        .map(WebComment::getNid)
                        .collect(Collectors.toSet());
                Map<String, WebNote> noteMap = noteMapper.selectBatchIds(noteIds).stream()
                        .collect(Collectors.toMap(WebNote::getId, Function.identity(), (k1, k2) -> k1));
                return commentList.stream().map(item -> {
                    CommentVo commentVo = ConvertUtils.sourceToTarget(item, CommentVo.class);
                    WebNote note = noteMap.get(item.getNid());
                    if (note != null) {
                        commentVo.setNoteCover(note.getNoteCover());
                    }
                    return commentVo;
                }).collect(Collectors.toMap(
                        commentVo -> String.valueOf(commentVo.getId()),
                        Function.identity(),
                        (k1, k2) -> k1
                ));
            });
            CompletableFuture<Map<String, WebAlbum>> albumMapFuture = CompletableFuture.supplyAsync(() -> {
                Set<String> aids = likeOrCollectionList.stream()
                        .filter(e -> e.getType() == 4)
                        .map(WebLikeOrCollection::getLikeOrCollectionId)
                        .collect(Collectors.toSet());
                if (aids.isEmpty()) {
                    return new HashMap<>(0);
                }
                return albumMapper.selectBatchIds(aids).stream()
                        .collect(Collectors.toMap(WebAlbum::getId, Function.identity(), (k1, k2) -> k1));
            });
            // 等待所有异步任务完成
            Map<String, WebUser> userMap = userMapFuture.get(5, TimeUnit.SECONDS);
            Map<String, WebNote> noteMap = noteMapFuture.get(5, TimeUnit.SECONDS);
            Map<String, CommentVo> commentVoMap = commentMapFuture.get(5, TimeUnit.SECONDS);
            Map<String, WebAlbum> albumMap = albumMapFuture.get(5, TimeUnit.SECONDS);
            // 组装数据
            List<LikeOrCollectionVo> resultList = likeOrCollectionList.stream()
                    .map(model -> convertToVo(model, userMap, noteMap, commentVoMap, albumMap))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return new Page<LikeOrCollectionVo>()
                    .setRecords(resultList)
                    .setTotal(likeOrCollectionPage.getTotal());
        } catch (TimeoutException e) {
            log.error("获取点赞收藏信息超时", e);
            throw new BusinessException("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("获取点赞收藏信息异常", e);
            throw new BusinessException("获取数据失败");
        }
    }

    /**
     * 转换为VO对象
     */
    private LikeOrCollectionVo convertToVo(WebLikeOrCollection model,
                                           Map<String, WebUser> userMap,
                                           Map<String, WebNote> noteMap,
                                           Map<String, CommentVo> commentVoMap,
                                           Map<String, WebAlbum> albumMap) {
        try {
            WebUser user = userMap.get(model.getUid());
            if (user == null) {
                return null;
            }

            LikeOrCollectionVo vo = new LikeOrCollectionVo();
            vo.setUid(String.valueOf(user.getId()))
                    .setUsername(user.getUsername())
                    .setAvatar(user.getAvatar())
                    .setTime(model.getTimestamp())
                    .setType(model.getType());

            // 根据类型设置不同的内容
            switch (model.getType()) {
                case 2: // 评论
                    CommentVo commentVo = commentVoMap.get(model.getLikeOrCollectionId());
                    if (commentVo != null) {
                        vo.setItemId(commentVo.getId())
                                .setItemCover(commentVo.getNoteCover())
                                .setContent(commentVo.getContent());
                    }
                    break;
                case 4: // 专辑
                    WebAlbum album = albumMap.get(model.getLikeOrCollectionId());
                    if (album != null) {
                        vo.setItemId(String.valueOf(album.getId()))
                                .setItemCover(album.getAlbumCover())
                                .setContent(album.getTitle());
                    }
                    break;
                default: // 笔记
                    WebNote note = noteMap.get(model.getLikeOrCollectionId());
                    if (note != null) {
                        vo.setItemId(String.valueOf(note.getId()))
                                .setItemCover(note.getNoteCover());
                    }
                    break;
            }
            return vo;
        } catch (Exception e) {
            log.error("转换点赞收藏数据异常", e);
            return null;
        }
    }

    /**
     * 点赞
     */
    private void updateLikeCollectionCount(LikeOrCollectionDTO likeOrCollectionDTO, int val) {
        switch (likeOrCollectionDTO.getType()) {
            case 1:
                WebNote likeNote = noteMapper.selectById(likeOrCollectionDTO.getLikeOrCollectionId());
                likeNote.setLikeCount(likeNote.getLikeCount() + val);
                noteMapper.updateById(likeNote);
                break;
            case 2:
                WebComment comment = commentMapper.selectById(likeOrCollectionDTO.getLikeOrCollectionId());
                if (comment == null) {
                    WebCommentSync commentSync = commentSyncMapper.selectById(likeOrCollectionDTO.getLikeOrCollectionId());
                    commentSync.setLikeCount(commentSync.getLikeCount() + val);
                    commentSyncMapper.updateById(commentSync);
                } else {
                    comment.setLikeCount(comment.getLikeCount() + val);
                    commentMapper.updateById(comment);
                }
                break;
            case 3:
//                String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
                String currentUid = AuthContextHolder.getUserId();
                WebNote collectionNote = noteMapper.selectById(likeOrCollectionDTO.getLikeOrCollectionId());
                //收藏图片
                collectionNote.setCollectionCount(collectionNote.getCollectionCount() + val);
                noteMapper.updateById(collectionNote);

                WebAlbumNoteRelation albumNoteRelation = new WebAlbumNoteRelation();
                albumNoteRelation.setNid(String.valueOf(collectionNote.getId()));
                if (val == 1) {
                    // 同一用户可能有多条 type=0 的专辑（历史数据），取一条避免 selectOne 报 TooManyResultsException
                    List<WebAlbum> defaultAlbums = albumMapper.selectList(
                            new QueryWrapper<WebAlbum>().eq("uid", currentUid).eq("type", 0).orderByAsc("id").last("limit 1"));
                    WebAlbum album = CollectionUtils.isEmpty(defaultAlbums) ? null : defaultAlbums.get(0);
                    Integer imgCount = collectionNote.getCount();
                    if (ObjectUtils.isEmpty(album)) {
                        album = new WebAlbum();
                        album.setTitle("默认专辑");
                        album.setUid(currentUid);
                        album.setAlbumCover(collectionNote.getNoteCover());
                        album.setImgCount(Long.valueOf(imgCount));
                        album.setCreateTime(new Date());
                        album.setUpdateTime(new Date());
                        albumMapper.insert(album);
                    } else {
                        album.setImgCount(album.getImgCount() + imgCount);
                        if (StringUtils.isBlank(album.getAlbumCover())) {
                            album.setAlbumCover(collectionNote.getNoteCover());
                        }
                        albumMapper.updateById(album);
                    }
                    albumNoteRelation.setAid(String.valueOf(album.getId()));
                    albumNoteRelation.setCreateTime(new Date());
                    albumNoteRelation.setUpdateTime(new Date());
                    albumNoteRelationMapper.insert(albumNoteRelation);
                } else {
                    List<WebAlbumNoteRelation> albumNoteRelationList = albumNoteRelationMapper.selectList(new QueryWrapper<WebAlbumNoteRelation>().eq("nid", collectionNote.getId()));
                    Set<String> aids = albumNoteRelationList.stream().map(WebAlbumNoteRelation::getAid).collect(Collectors.toSet());
                    List<WebAlbum> albumList = albumMapper.selectBatchIds(aids);
                    WebAlbum album = albumList.stream().filter(item -> item.getUid().equals(currentUid)).findFirst().orElse(null);
                    Integer imgCount = collectionNote.getCount();
                    long nums = album.getImgCount() - imgCount;
                    if (nums <= 0) {
                        album.setAlbumCover(null);
                    }
                    album.setImgCount(nums);
                    albumMapper.updateById(album);
                    albumNoteRelationMapper.delete(new QueryWrapper<WebAlbumNoteRelation>().eq("aid", album.getId()).eq("nid", collectionNote.getId()));
                }
                break;
            default:
                // 收藏专辑
                WebAlbum collectAlbum = albumMapper.selectById(likeOrCollectionDTO.getLikeOrCollectionId());
                collectAlbum.setCollectionCount(collectAlbum.getCollectionCount() + val);
                albumMapper.updateById(collectAlbum);
                break;
        }
    }
}
