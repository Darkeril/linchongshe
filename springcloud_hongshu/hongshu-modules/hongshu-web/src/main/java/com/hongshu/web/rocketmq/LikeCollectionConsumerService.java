package com.hongshu.web.rocketmq;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.dto.LikeCollectionMessage;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.im.ChatUtils;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 点赞/收藏消息消费者
 *
 * @author: hongshu
 *
 * 注意：需要先启动 RocketMQ 才能启用此 Consumer
 * 启动 RocketMQ 后，取消下面的注释即可
 */
@Slf4j
@Service
// 添加条件注解，当配置为 false 时不加载
@ConditionalOnProperty(name = "rocketmq.consumer.listen", havingValue = "true")
@RocketMQMessageListener(
    topic = "LIKE_COLLECTION_TOPIC",
    consumerGroup = "like-collection-consumer-group",
    maxReconsumeTimes = 3  // 最多重试3次
)
public class LikeCollectionConsumerService implements RocketMQListener<String> {

    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;

    @Autowired
    private WebNoteMapper noteMapper;

    @Autowired
    private ChatUtils chatUtils;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String messageBody) {
        try {
            // 解析消息
            LikeCollectionMessage message = JSON.parseObject(messageBody, LikeCollectionMessage.class);

            log.info("收到点赞/收藏消息: action={}, uid={}, itemId={}",
                    message.getAction(), message.getUid(), message.getLikeOrCollectionId());

            // 根据操作类型处理
            switch (message.getAction()) {
                case "LIKE":
                    handleLike(message);
                    break;
                case "UNLIKE":
                    handleUnlike(message);
                    break;
                case "COLLECT":
                    handleCollect(message);
                    break;
                case "UNCOLLECT":
                    handleUncollect(message);
                    break;
                default:
                    log.warn("未知的操作类型: {}", message.getAction());
            }

        } catch (Exception e) {
            log.error("处理点赞/收藏消息失败", e);
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 处理点赞
     */
    private void handleLike(LikeCollectionMessage message) {
        // 1. 检查是否已存在（防止重复消费）
        long count = likeOrCollectionMapper.selectCount(new QueryWrapper<WebLikeOrCollection>()
                .eq("uid", message.getUid())
                .eq("like_or_collection_id", message.getLikeOrCollectionId())
                .eq("type", message.getType()));

        if (count > 0) {
            log.info("点赞记录已存在，跳过: uid={}, itemId={}", message.getUid(), message.getLikeOrCollectionId());
            return;
        }

        // 2. 插入点赞记录
        WebLikeOrCollection record = new WebLikeOrCollection();
        record.setUid(message.getUid());
        record.setLikeOrCollectionId(message.getLikeOrCollectionId());
        record.setPublishUid(message.getPublishUid());
        record.setType(message.getType());
        record.setTimestamp(message.getTimestamp());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        likeOrCollectionMapper.insert(record);

        // 3. 更新笔记点赞数（MySQL）
        updateNoteCountInDb(message.getLikeOrCollectionId(), 1, message.getType());

        // 4. 更新ES索引
        updateEsNote(message.getLikeOrCollectionId(), true);

        // 5. 发送WebSocket通知
        if (!message.getPublishUid().equals(message.getUid())) {
            chatUtils.sendMessage(message.getPublishUid(), 0);
        }
    }

    /**
     * 处理取消点赞
     */
    private void handleUnlike(LikeCollectionMessage message) {
        // 1. 删除点赞记录
        likeOrCollectionMapper.delete(new QueryWrapper<WebLikeOrCollection>()
                .eq("uid", message.getUid())
                .eq("like_or_collection_id", message.getLikeOrCollectionId())
                .eq("type", message.getType()));

        // 2. 更新笔记点赞数（MySQL）
        updateNoteCountInDb(message.getLikeOrCollectionId(), -1, message.getType());

        // 3. 更新ES索引
        updateEsNote(message.getLikeOrCollectionId(), false);
    }

    /**
     * 处理收藏
     */
    private void handleCollect(LikeCollectionMessage message) {
        // 检查是否已存在
        long count = likeOrCollectionMapper.selectCount(new QueryWrapper<WebLikeOrCollection>()
                .eq("uid", message.getUid())
                .eq("like_or_collection_id", message.getLikeOrCollectionId())
                .eq("type", message.getType()));

        if (count > 0) {
            log.info("收藏记录已存在，跳过: uid={}, itemId={}", message.getUid(), message.getLikeOrCollectionId());
            return;
        }

        // 插入收藏记录
        WebLikeOrCollection record = new WebLikeOrCollection();
        record.setUid(message.getUid());
        record.setLikeOrCollectionId(message.getLikeOrCollectionId());
        record.setPublishUid(message.getPublishUid());
        record.setType(message.getType());
        record.setTimestamp(message.getTimestamp());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        likeOrCollectionMapper.insert(record);

        // 更新笔记收藏数
        updateNoteCountInDb(message.getLikeOrCollectionId(), 1, message.getType());

        // 发送通知
        if (!message.getPublishUid().equals(message.getUid())) {
            chatUtils.sendMessage(message.getPublishUid(), 0);
        }
    }

    /**
     * 处理取消收藏
     */
    private void handleUncollect(LikeCollectionMessage message) {
        // 删除收藏记录
        likeOrCollectionMapper.delete(new QueryWrapper<WebLikeOrCollection>()
                .eq("uid", message.getUid())
                .eq("like_or_collection_id", message.getLikeOrCollectionId())
                .eq("type", message.getType()));

        // 更新笔记收藏数
        updateNoteCountInDb(message.getLikeOrCollectionId(), -1, message.getType());
    }

    /**
     * 更新数据库中的计数（原子操作）
     */
    private void updateNoteCountInDb(String noteId, int increment, Integer type) {
        try {
            if (type == 1) {
                // 点赞笔记
                WebNote note = noteMapper.selectById(noteId);
                if (note != null) {
                    note.setLikeCount(note.getLikeCount() + increment);
                    noteMapper.updateById(note);
                }
            } else if (type == 3) {
                // 收藏笔记
                WebNote note = noteMapper.selectById(noteId);
                if (note != null) {
                    note.setCollectionCount(note.getCollectionCount() + increment);
                    noteMapper.updateById(note);
                }
            }
            // type == 2 (评论) 和 type == 4 (专辑) 的处理逻辑类似
        } catch (Exception e) {
            log.error("更新数据库计数失败: noteId={}, type={}", noteId, type, e);
        }
    }

    /**
     * 更新ES笔记索引
     */
    private void updateEsNote(String noteId, boolean isLike) {
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
                noteSearchVo.setIsLike(isLike);
                if (isLike) {
                    noteSearchVo.setLikeCount(noteSearchVo.getLikeCount() + 1);
                } else {
                    noteSearchVo.setLikeCount(noteSearchVo.getLikeCount() - 1);
                }

                // Step 3: 将更新后的数据保存回 Elasticsearch
                elasticsearchClient.update(u ->
                                u.index(NoteConstant.NOTE_INDEX)
                                        .id(noteId)
                                        .doc(noteSearchVo),
                        NoteSearchVo.class);

                log.debug("ES索引更新成功: noteId={}, isLike={}", noteId, isLike);
            }
        } catch (Exception e) {
            log.error("更新ES索引失败: noteId={}", noteId, e);
        }
    }
}

