package com.hongshu.web.service.web.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.dto.BrowseRecordDTO;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.entity.WebUserNoteRelation;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.mapper.web.WebUserNoteRelationMapper;
import com.hongshu.web.service.web.IWebBrowseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebBrowseRecordServiceImpl extends ServiceImpl<WebNoteMapper, WebNote> implements IWebBrowseRecordService {

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebUserNoteRelationMapper userNoteRelationMapper;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;


    /**
     * 获取浏览记录
     */
    @Override
    public Page<NoteSearchVo> getAllBrowseRecordByUser(long page, long limit) {
        String userId = AuthContextHolder.getUserId();
        
        // 创建分页对象
        Page<NoteSearchVo> pageInfo = new Page<>(page, limit);
        
        // 查询浏览记录总数
        long total = userNoteRelationMapper.selectCount(new QueryWrapper<WebUserNoteRelation>()
                .eq("uid", userId));
        
        // 分页查询浏览记录
        Page<WebUserNoteRelation> relationPage = new Page<>(page, limit);
        List<WebUserNoteRelation> noteRelationList = userNoteRelationMapper.selectPage(relationPage, 
                new QueryWrapper<WebUserNoteRelation>()
                        .eq("uid", userId)
                        .orderByDesc("update_time")).getRecords();

        // 是否点赞
        List<WebLikeOrCollection> likeOrCollections = likeOrCollectionMapper.selectList(new QueryWrapper<WebLikeOrCollection>().eq("uid", userId).eq("type", 1));
        List<String> likeOrCollectionIds = likeOrCollections.stream().map(WebLikeOrCollection::getLikeOrCollectionId).collect(Collectors.toList());

        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(noteRelationList)) {

            Set<String> nids = noteRelationList.stream().map(WebUserNoteRelation::getNid).collect(Collectors.toSet());
            Map<String, WebNote> noteMap = noteMapper.selectBatchIds(nids).stream().collect(Collectors.toMap(WebNote::getId, note -> note));

            noteRelationList.forEach(item -> {
                WebNote note = noteMap.get(item.getNid());
                if (note != null) {
                    WebUser user = userMapper.selectById(note.getUid());
                    NoteSearchVo noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
                    noteSearchVo.setAvatar(user.getAvatar());
                    noteSearchVo.setIsLike(likeOrCollectionIds.contains(note.getId()));
                    noteSearchVo.setUsername(user.getUsername());
                    // 浏览记录不应该显示置顶状态，清除pinned字段
                    noteSearchVo.setPinned(null);
                    noteSearchVoList.add(noteSearchVo);
                }
            });
        }
        
        // 设置分页信息
        pageInfo.setRecords(noteSearchVoList);
        pageInfo.setTotal(total);
        
        return pageInfo;
    }

    /**
     * 添加浏览记录
     */
    @Override
    @Transactional
    public void addBrowseRecord(BrowseRecordDTO browseRecordDTO) {
        // 查询是否存在浏览记录（使用 selectList 避免多条记录异常）
        List<WebUserNoteRelation> existingRecords = userNoteRelationMapper.selectList(
                new QueryWrapper<WebUserNoteRelation>()
                        .eq("uid", browseRecordDTO.getUid())
                        .eq("nid", browseRecordDTO.getNid())
                        .orderByDesc("update_time"));
        
        if (CollectionUtil.isEmpty(existingRecords)) {
            // 新增浏览记录
            WebUserNoteRelation newRecord = new WebUserNoteRelation();
            newRecord.setUid(browseRecordDTO.getUid());
            newRecord.setNid(browseRecordDTO.getNid());
            newRecord.setCreateTime(new Date());
            newRecord.setUpdateTime(new Date());
            userNoteRelationMapper.insert(newRecord);
            log.info("新增浏览记录: uid={}, nid={}", browseRecordDTO.getUid(), browseRecordDTO.getNid());
        } else {
            // 如果存在多条记录，保留最新的一条，删除其他重复记录
            if (existingRecords.size() > 1) {
                log.warn("发现重复浏览记录: uid={}, nid={}, 数量={}", 
                        browseRecordDTO.getUid(), browseRecordDTO.getNid(), existingRecords.size());
                // 保留第一条（最新的），删除其他
                List<String> duplicateIds = existingRecords.stream()
                        .skip(1)
                        .map(WebUserNoteRelation::getId)
                        .collect(Collectors.toList());
                if (!duplicateIds.isEmpty()) {
                    userNoteRelationMapper.deleteBatchIds(duplicateIds);
                    log.info("已删除重复浏览记录: {}", duplicateIds);
                }
            }
            // 更新浏览记录时间
            WebUserNoteRelation recordToUpdate = existingRecords.get(0);
            recordToUpdate.setUpdateTime(new Date());
            userNoteRelationMapper.updateById(recordToUpdate);
            log.debug("更新浏览记录: uid={}, nid={}", browseRecordDTO.getUid(), browseRecordDTO.getNid());
        }
        // 方案A：观看数仅由 getNoteById 内的 recordNoteView 统一累加，此处不再更新 viewCount，避免重复统计
    }

    /**
     * 删除浏览记录
     */
    @Override
    public void delRecord(String uid, List<String> idList) {
        // 根据 uid 查询出所有的数据
        List<WebUserNoteRelation> userNoteRelations = userNoteRelationMapper.selectList(
                new QueryWrapper<WebUserNoteRelation>().eq("uid", uid)
        );
        // 筛选出需要删除的数据
        List<String> idsToDelete = userNoteRelations.stream()
                .filter(relation -> idList.contains(relation.getNid()))  // 筛选出 nid 在 idList 中的数据
                .map(WebUserNoteRelation::getId)  // 获取对应的 id        // 只保留 id 在 idList 中的数据
                .collect(Collectors.toList());
        if (!idsToDelete.isEmpty()) {
            // 删除符合条件的数据
            userNoteRelationMapper.deleteBatchIds(idsToDelete);
        }
    }
}
