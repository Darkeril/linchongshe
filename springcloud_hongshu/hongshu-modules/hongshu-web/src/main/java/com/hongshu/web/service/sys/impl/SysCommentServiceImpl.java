package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.IdleProduct;
import com.hongshu.web.domain.entity.WebComment;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.CommentVo;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.sys.SysCommentMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.ISysCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论信息 服务层处理
 *

 */
@Slf4j
@Service
public class SysCommentServiceImpl implements ISysCommentService {

    @Autowired
    private SysCommentMapper commentMapper;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private IdleProductMapper productMapper;


    /**
     * 查询评论信息集合
     *
     * @param query 评论信息
     */
    @Override
    public List<CommentVo> selectCommentList(Query query) {
        QueryWrapper<WebComment> qw = new QueryWrapper<>();

        // 处理用户名筛选：如果传入了username，先查询用户ID
        String username = (String) query.get("username");
        if (StringUtils.isNotBlank(username)) {
            QueryWrapper<WebUser> userQw = new QueryWrapper<>();
            userQw.lambda().like(WebUser::getUsername, username);
            List<WebUser> users = userMapper.selectList(userQw);
            if (users.isEmpty()) {
                // 如果没有找到匹配的用户，返回空列表
                return new ArrayList<>();
            }
            // 提取用户ID列表
            List<String> userIds = users.stream()
                    .map(WebUser::getId)
                    .collect(Collectors.toList());
            qw.lambda().in(WebComment::getUid, userIds);
        } else {
            // 如果没有username参数，使用uid参数
            qw.lambda().eq(ValidatorUtil.isNotNull(query.getUid()), WebComment::getUid, query.getUid());
        }

        // 处理评论内容筛选
        String content = (String) query.get("content");
        if (StringUtils.isNotBlank(content)) {
            qw.lambda().like(WebComment::getContent, content);
        }

        qw.lambda()
                // 根据 commentType 判断是查询商品评论还是笔记评论
                .isNotNull("product".equals(query.getCommentType()), WebComment::getProductId)
                .isNull("note".equals(query.getCommentType()), WebComment::getProductId)
                .eq(WebComment::getLevel, 1)
                .ge(ValidatorUtil.isNotNull(query.get("params[beginTime]")),
                        WebComment::getCreateTime, query.get("params[beginTime]"))
                .le(ValidatorUtil.isNotNull(query.get("params[endTime]")),
                        WebComment::getCreateTime, query.get("params[endTime]"));

        // 处理排序
        String orderByColumn = (String) query.get("orderByColumn");
        String isAsc = (String) query.get("isAsc");
        if (StringUtils.isNotBlank(orderByColumn)) {
            if ("createTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebComment::getCreateTime);
                } else {
                    qw.lambda().orderByDesc(WebComment::getCreateTime);
                }
            } else {
                // 默认按创建时间降序
                qw.lambda().orderByDesc(WebComment::getCreateTime);
            }
        } else {
            // 默认按创建时间降序
            qw.lambda().orderByDesc(WebComment::getCreateTime);
        }

        // 查询评论列表
        List<WebComment> commentList = commentMapper.selectList(qw);
        List<CommentVo> commentVOList = ConvertUtils.sourceToTarget(commentList, CommentVo.class);
        if (commentVOList.isEmpty()) {
            return commentVOList;
        }

        // 收集所有需要查询的用户ID
        Set<String> userIds = new HashSet<>();
        for (CommentVo commentVo : commentVOList) {
            // 添加评论人ID
            userIds.add(commentVo.getUid());

            // 添加被评论人ID（根据是否有productId区分）
            if (StringUtils.isNotBlank(commentVo.getProductId())) {
                // 商品评论时，replyUid就是被评论人ID
                if (StringUtils.isNotBlank(commentVo.getReplyUid())) {
                    userIds.add(commentVo.getReplyUid());
                }
            } else {
                // 笔记评论时，noteUid是被评论人ID
                if (StringUtils.isNotBlank(commentVo.getNoteUid())) {
                    userIds.add(commentVo.getNoteUid());
                }
            }
        }

        // 批量查询用户信息
        Map<String, WebUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WebUser::getId, Function.identity()));

        // 分别收集笔记ID和商品ID
        Set<String> noteIds = commentVOList.stream()
                .filter(comment -> StringUtils.isNotBlank(comment.getNid()))
                .map(CommentVo::getNid)
                .collect(Collectors.toSet());

        Set<String> productIds = commentVOList.stream()
                .filter(comment -> StringUtils.isNotBlank(comment.getProductId()))
                .map(CommentVo::getProductId)
                .collect(Collectors.toSet());

        // 批量查询笔记和商品信息
        Map<String, WebNote> noteMap = Collections.emptyMap();
        if (!noteIds.isEmpty()) {
            noteMap = noteMapper.selectBatchIds(noteIds).stream()
                    .collect(Collectors.toMap(WebNote::getId, Function.identity()));
        }

        Map<String, IdleProduct> productMap = Collections.emptyMap();
        if (!productIds.isEmpty()) {
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(IdleProduct::getId, Function.identity()));
        }

        // 设置关联数据
        for (CommentVo commentVo : commentVOList) {
            // 设置评论类型：根据是否有productId判断
            if (StringUtils.isNotBlank(commentVo.getProductId())) {
                commentVo.setCommentType("product");
            } else {
                commentVo.setCommentType("note");
            }

            // 设置评论人信息
            WebUser webUser = userMap.get(commentVo.getUid());
            if (webUser != null) {
                commentVo.setUsername(webUser.getUsername());
                commentVo.setAvatar(webUser.getAvatar());
            }

            // 设置被评论人信息
            if (StringUtils.isNotBlank(commentVo.getProductId())) {
                // 商品评论时，从replyUid获取被评论人信息
                WebUser replyUser = userMap.get(commentVo.getReplyUid());
                if (replyUser != null) {
                    commentVo.setPushUsername(replyUser.getUsername());
                    commentVo.setReplyUsername(replyUser.getUsername());
                    commentVo.setReplyAvatar(replyUser.getAvatar());
                }
            } else {
                // 笔记评论时，从noteUid获取被评论人信息
                WebUser noteUser = userMap.get(commentVo.getNoteUid());
                if (noteUser != null) {
                    commentVo.setReplyUsername(noteUser.getUsername());
                    commentVo.setReplyAvatar(noteUser.getAvatar());
                    commentVo.setPushUsername(noteUser.getUsername());
                }
            }

            // 设置标题和封面
            if (StringUtils.isNotBlank(commentVo.getProductId())) {
                IdleProduct product = productMap.get(commentVo.getProductId());
                if (product != null) {
                    commentVo.setTitle(product.getTitle());
                    commentVo.setNoteCover(product.getCover());
                }
            } else {
                WebNote note = noteMap.get(commentVo.getNid());
                if (note != null) {
                    commentVo.setTitle(note.getTitle());
                    commentVo.setNoteCover(note.getNoteCover());
                }
            }
        }
        return commentVOList;
    }

    /**
     * 查询一级以下评论
     *
     * @param query 评论信息
     */
    @Override
    public List<CommentVo> selectTreeList(Query query) {
        QueryWrapper<WebComment> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(ValidatorUtil.isNotNull(query.getUid()), WebComment::getUid, query.getUid())
                .eq(ValidatorUtil.isNotNull(query.get("targetId")),
                        "product".equals(query.get("type")) ? WebComment::getProductId : WebComment::getNid,
                        query.get("targetId"))
                .gt(WebComment::getLevel, 1)  // 查询一级以下的评论（level > 1）
                .ge(ValidatorUtil.isNotNull(query.get("params[beginTime]")),
                        WebComment::getCreateTime, query.get("params[beginTime]"))
                .le(ValidatorUtil.isNotNull(query.get("params[endTime]")),
                        WebComment::getCreateTime, query.get("params[endTime]"))
                .orderByAsc(WebComment::getLevel)  // 按评论等级升序
                .orderByDesc(WebComment::getCreateTime);  // 同级评论按时间倒序

        // 查询评论列表
        List<WebComment> commentList = commentMapper.selectList(qw);
        // 转换为 VO
        List<CommentVo> commentVOList = ConvertUtils.sourceToTarget(commentList, CommentVo.class);
        if (commentVOList.isEmpty()) {
            return commentVOList;
        }

        // 批量查询用户信息
        Set<String> userIds = new HashSet<>();
        for (CommentVo commentVo : commentVOList) {
            userIds.add(commentVo.getUid());
            if (StringUtils.isNotBlank(commentVo.getNoteUid())) {
                userIds.add(commentVo.getNoteUid());
            }
            if (StringUtils.isNotBlank(commentVo.getReplyUid())) {
                userIds.add(commentVo.getReplyUid());
            }
        }
        Map<String, WebUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WebUser::getId, Function.identity(), (k1, k2) -> k1));

        // 分别收集笔记ID和商品ID
        Set<String> noteIds = commentVOList.stream()
                .filter(comment -> StringUtils.isNotBlank(comment.getNid()))
                .map(CommentVo::getNid)
                .collect(Collectors.toSet());

        Set<String> productIds = commentVOList.stream()
                .filter(comment -> StringUtils.isNotBlank(comment.getProductId()))
                .map(CommentVo::getProductId)
                .collect(Collectors.toSet());

        // 批量查询笔记信息
        Map<String, WebNote> noteMap = Collections.emptyMap();
        if (!noteIds.isEmpty()) {
            noteMap = noteMapper.selectBatchIds(noteIds).stream()
                    .collect(Collectors.toMap(WebNote::getId, Function.identity(), (k1, k2) -> k1));
        }

        // 批量查询商品信息
        Map<String, IdleProduct> productMap = Collections.emptyMap();
        if (!productIds.isEmpty()) {
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(IdleProduct::getId, Function.identity(), (k1, k2) -> k1));
        }

        // 设置关联数据
        for (CommentVo commentVo : commentVOList) {
            // 设置评论类型：根据是否有productId判断
            if (StringUtils.isNotBlank(commentVo.getProductId())) {
                commentVo.setCommentType("product");
            } else {
                commentVo.setCommentType("note");
            }

            // 设置评论人信息
            WebUser webUser = userMap.get(commentVo.getUid());
            if (webUser != null) {
                commentVo.setUsername(webUser.getUsername());
                commentVo.setAvatar(webUser.getAvatar());
            }

            // 根据评论类型设置被评论人和相关信息
            if (StringUtils.isNotBlank(commentVo.getProductId())) {
                // 商品评论
                WebUser replyUser = userMap.get(commentVo.getReplyUid());
                if (replyUser != null) {
                    commentVo.setReplyUsername(replyUser.getUsername());
                    commentVo.setReplyAvatar(replyUser.getAvatar());
                }

                IdleProduct product = productMap.get(commentVo.getProductId());
                if (product != null) {
                    commentVo.setPushUsername(replyUser != null ? replyUser.getUsername() : null);
                    commentVo.setTitle(product.getTitle());
                    commentVo.setNoteCover(product.getCover());
                }
            } else {
                // 笔记评论
                WebUser noteUser = userMap.get(commentVo.getReplyUid());
                if (noteUser != null) {
                    commentVo.setReplyUsername(noteUser.getUsername());
                    commentVo.setReplyAvatar(noteUser.getAvatar());
                }

                WebNote note = noteMap.get(commentVo.getNid());
                if (note != null) {
                    commentVo.setTitle(note.getTitle());
                    commentVo.setNoteCover(note.getNoteCover());
                }
            }
        }
        return commentVOList;
    }

    /**
     * 通过评论ID查询评论信息
     *
     * @param id 评论ID
     */
    @Override
    public WebComment selectCommentById(Long id) {
        return commentMapper.selectById(id);
    }

    /**
     * 通过笔记ID查询评论信息
     *
     * @param nid 笔记ID
     */
    @Override
    public List<WebComment> selectCommentByNid(Long nid) {
        QueryWrapper<WebComment> qw = new QueryWrapper<>();
        qw.lambda().like(ValidatorUtil.isNotNull(nid), WebComment::getNid, nid);
        return commentMapper.selectList(qw);
    }

    /**
     * 批量删除评论信息
     *
     * @param ids 需要删除的评论ID
     */
    @Override
    public int deleteCommentByIds(Long[] ids) {
        List<Long> longs = Arrays.asList(ids);
        for (Long id : ids) {
            WebComment comment = selectCommentById(id);
            if (ValidatorUtil.isNull(comment)) {
                log.info("评论不存在:{}", id);
                longs.remove(id);
            }
        }
        return commentMapper.deleteBatchIds(longs);
    }

    @Override
    public Integer getCommentCount(int status) {
        QueryWrapper<WebComment> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(BaseSQLConf.STATUS, status);
        return Math.toIntExact(commentMapper.selectCount(queryWrapper));
    }
}
