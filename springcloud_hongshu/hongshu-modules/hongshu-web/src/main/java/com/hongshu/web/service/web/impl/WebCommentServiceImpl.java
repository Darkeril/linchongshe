package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.dto.CommentDTO;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.CommentVo;
import com.hongshu.web.im.ChatUtils;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.web.*;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import com.hongshu.common.core.exception.ServiceException;
import com.hongshu.web.rocketmq.CommentServiceV2;
import com.hongshu.web.service.audit.CommentAuditService;
import com.hongshu.web.service.web.IWebCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebCommentServiceImpl extends ServiceImpl<WebCommentMapper, WebComment> implements IWebCommentService {

    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;
    @Autowired
    private CommentServiceV2 commentServiceV2;
    @Autowired
    private CommentAuditService commentAuditService;


    /**
     * 获取所有一级分类
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param noteId      笔记ID
     */
    @Override
    public Page<CommentVo> getOneCommentByNoteId(long currentPage, long pageSize, String noteId) {
        return null;
    }

    /**
     * 根据评论ID获取当前评论
     *
     * @param commentId 评论ID
     */
    @Override
    public Object getCommentById(String commentId) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommentVo saveCommentByDTO(CommentDTO commentDTO) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        
        // 1. 审核评论内容
        AutoAuditResult auditResult = commentAuditService.auditComment(
                currentUid,
                null,  // commentId 在插入后生成
                commentDTO.getContent()
        );
        
        // 2. 根据审核结果决定是否发布
        if (auditResult.getResult() == AutoAuditResultEnum.REJECT) {
            log.warn("评论审核未通过: userId={}, reason={}", currentUid, auditResult.getReason());
            // 使用 400 错误码表示客户端错误（内容不合规）
            throw new ServiceException(auditResult.getReason(), 400);
        }
        
        // 3. 创建评论（直接使用 WebComment，不再使用 sync 表）
        WebComment comment = ConvertUtils.sourceToTarget(commentDTO, WebComment.class);
        comment.setUid(currentUid);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        
        // 4. 设置审核状态
        if (auditResult.getResult() == AutoAuditResultEnum.PASS) {
            comment.setAuditStatus(1); // 审核通过
        } else if (auditResult.getResult() == AutoAuditResultEnum.MANUAL) {
            comment.setAuditStatus(0); // 待审核
        } else if (auditResult.getResult() == AutoAuditResultEnum.REJECT) {
            comment.setAuditStatus(2); // 拒绝
        }
        comment.setAuditTime(auditResult.getAuditTime());
        comment.setAuditReason(auditResult.getReason());
        
        // 5. 插入评论
        this.save(comment);
        
        // 6. 保存审核日志（评论插入后，commentId 已生成）
        commentAuditService.saveAuditLog(comment.getId(), "comment", auditResult);

        // 7. 使用Redis缓存 + RocketMQ异步更新评论数
        commentServiceV2.recordCommentCreate(
                comment.getId(),
                commentDTO.getNid(),
                commentDTO.getProductId(),
                commentDTO.getPid()
        );

        // 8. 构建返回对象
        CommentVo commentVo = ConvertUtils.sourceToTarget(comment, CommentVo.class);
        WebUser user = userMapper.selectById(currentUid);

        commentVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(comment.getCreateTime().getTime());

        return commentVo;
    }

    /**
     * 根据评论ID同步评论集
     * 注意：已废弃，改为单表架构后不再需要同步
     *
     * @param commentIds 评论ID数据集
     */
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncCommentByIds(List<String> commentIds) {
        // 单表架构下不再需要同步操作
        log.info("syncCommentByIds 方法已废弃，单表架构下无需同步: commentIds=" + commentIds);
    }

    /**
     * 根据一级评论ID获取所有的二级评论
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param oneCommentId 一级评论ID
     */
    @Override
    public Page<CommentVo> getTwoCommentByOneCommentId(long currentPage, long pageSize, String oneCommentId) {
        Page<CommentVo> result = new Page<>();
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        Page<WebComment> twoCommentPage = this.page(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<WebComment>().eq("pid", oneCommentId).orderByDesc("like_count").orderByDesc("create_time"));
        List<WebComment> twoCommentList = twoCommentPage.getRecords();
        long total = twoCommentPage.getTotal();

        if (!twoCommentList.isEmpty()) {
            Set<String> uids = twoCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            List<WebUser> users = userMapper.selectBatchIds(uids);
            Map<String, WebUser> userMap = users.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            Set<String> replyUids = twoCommentList.stream().map(WebComment::getReplyUid).collect(Collectors.toSet());
            Map<String, WebUser> replyUserMap = new HashMap<>(16);
            if (!replyUids.isEmpty()) {
                List<WebUser> replyUsers = userMapper.selectBatchIds(replyUids);
                replyUserMap = replyUsers.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            }

            List<CommentVo> commentVos = new ArrayList<>();
            List<WebLikeOrCollection> likeOrCollections = likeOrCollectionMapper.selectList(new QueryWrapper<WebLikeOrCollection>().eq("uid", currentUid).eq("type", 2));
            List<String> likeComments = likeOrCollections.stream().map(WebLikeOrCollection::getLikeOrCollectionId).collect(Collectors.toList());
            for (WebComment comment : twoCommentList) {
                CommentVo commentVo = ConvertUtils.sourceToTarget(comment, CommentVo.class);
                WebUser user = userMap.get(comment.getUid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(comment.getCreateTime().getTime())
                        .setIsLike(likeComments.contains(comment.getId()));
                WebUser replyUser = replyUserMap.get(comment.getReplyUid());
                if (replyUser != null) {
                    commentVo.setReplyUsername(replyUser.getUsername());
                }
                commentVos.add(commentVo);
            }
            result.setRecords(commentVos);
        }
        result.setTotal(total);
        return result;
    }

    /**
     * 获取当前用户通知的评论集
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public IPage<CommentVo> getNoticeComment(long currentPage, long pageSize) {
        Page<CommentVo> result = new Page<>();
        String currentUid = AuthContextHolder.getUserId();

        // 添加用户ID检查
        if (currentUid == null) {
            throw new RuntimeException("用户未登录");
        }

        try {
            // 修改查询条件，添加商品评论的查询
            Page<WebComment> commentPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                    new QueryWrapper<WebComment>()
                            .and(wrapper -> wrapper
                                    // 原有的笔记评论条件
                                    .or(e -> e.eq("note_uid", currentUid)
                                            .or().eq("reply_uid", currentUid))
                                    // 添加商品评论条件
                                    .or(e -> e.eq("product_uid", currentUid))
                            )
                            .ne("uid", currentUid)
                            .orderByDesc("create_time"));

            List<WebComment> commentList = commentPage.getRecords();
            long total = commentPage.getTotal();

            List<CommentVo> commentVoList = new ArrayList<>();
            if (!commentList.isEmpty()) {
                // 获取用户信息
                Set<String> uids = commentList.stream()
                        .filter(comment -> comment.getUid() != null)
                        .map(WebComment::getUid)
                        .collect(Collectors.toSet());

                Map<String, WebUser> userMap = new HashMap<>();
                if (!uids.isEmpty()) {
                    userMap = userMapper.selectBatchIds(uids).stream()
                            .filter(user -> user != null && user.getId() != null)
                            .collect(Collectors.toMap(WebUser::getId, user -> user, (k1, k2) -> k1));
                }

                // 获取笔记信息
                Set<String> nids = commentList.stream()
                        .filter(c -> c.getNid() != null && !c.getNid().isEmpty())
                        .map(WebComment::getNid)
                        .collect(Collectors.toSet());

                Map<String, WebNote> noteMap = new HashMap<>();
                if (!nids.isEmpty()) {
                    noteMap = noteMapper.selectBatchIds(nids).stream()
                            .filter(note -> note != null && note.getId() != null)
                            .collect(Collectors.toMap(WebNote::getId, note -> note, (k1, k2) -> k1));
                }

                // 获取商品信息
                Set<String> productIds = commentList.stream()
                        .filter(c -> c.getProductId() != null && !c.getProductId().isEmpty())
                        .map(WebComment::getProductId)
                        .collect(Collectors.toSet());

                Map<String, IdleProduct> productMap = new HashMap<>();
                if (!productIds.isEmpty()) {
                    productMap = productMapper.selectBatchIds(productIds).stream()
                            .filter(product -> product != null && product.getId() != null)
                            .collect(Collectors.toMap(IdleProduct::getId, product -> product, (k1, k2) -> k1));
                }

                // 获取回复的评论内容
                Set<String> cids = commentList.stream()
                        .filter(item -> item.getPid() != null && !"0".equals(item.getPid()))
                        .filter(item -> item.getReplyId() != null)
                        .map(WebComment::getReplyId)
                        .collect(Collectors.toSet());

                Map<String, WebComment> replyCommentMap = new HashMap<>();
                if (!cids.isEmpty()) {
                    replyCommentMap = this.listByIds(cids).stream()
                            .filter(comment -> comment != null && comment.getId() != null)
                            .collect(Collectors.toMap(WebComment::getId, comment -> comment, (k1, k2) -> k1));
                }

                for (WebComment comment : commentList) {
                    try {
                        CommentVo commentVo = ConvertUtils.sourceToTarget(comment, CommentVo.class);
                        if (commentVo == null) {
                            continue;
                        }

                        // 设置用户信息
                        WebUser user = userMap.get(comment.getUid());
                        if (user != null) {
                            commentVo.setUsername(user.getUsername())
                                    .setAvatar(user.getAvatar())
                                    .setTime(comment.getCreateTime() != null ?
                                            comment.getCreateTime().getTime() : System.currentTimeMillis());
                        }

                        // 设置笔记或商品信息
                        if (comment.getNid() != null && !comment.getNid().isEmpty()) {
                            WebNote note = noteMap.get(comment.getNid());
                            if (note != null) {
                                commentVo.setNoteCover(note.getNoteCover());
                                commentVo.setCommentType("note");
                            }
                        } else if (comment.getProductId() != null && !comment.getProductId().isEmpty()) {
                            IdleProduct product = productMap.get(comment.getProductId());
                            if (product != null) {
                                commentVo.setProductId(product.getId());
                                commentVo.setProductCover(product.getCover());
                                commentVo.setProductName(product.getTitle());
                                commentVo.setCommentType("product");
                            }
                        }

                        // 处理回复信息
                        if (comment.getPid() != null && !"0".equals(comment.getPid()) &&
                                comment.getReplyId() != null) {
                            WebComment replyComment = replyCommentMap.get(comment.getReplyId());
                            if (replyComment != null) {
                                commentVo.setReplyContent(replyComment.getContent());
                                if (comment.getReplyUid() != null &&
                                        !comment.getReplyUid().equals(currentUid)) {
                                    WebUser replyUser = userMap.get(comment.getReplyUid());
                                    if (replyUser != null) {
                                        commentVo.setReplyUsername(replyUser.getUsername());
                                    }
                                }
                            }
                        }
                        commentVoList.add(commentVo);
                    } catch (Exception e) {
                        // 记录单个评论处理异常，继续处理其他评论
                        log.error("处理评论数据异常", e);
                    }
                }
            }

            result.setRecords(commentVoList);
            result.setTotal(total);
            return result;
        } catch (Exception e) {
            log.error("获取评论列表异常", e);
            throw new RuntimeException("获取评论列表失败", e);
        }
    }

    /**
     * 获取所有的一级评论并携带二级评论
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param noteId      笔记ID
     */
    @Override
    public Page<CommentVo> getCommentWithCommentByNoteId(long currentPage, long pageSize, String noteId) {
        //先得到所有的一级评论
        Page<CommentVo> result = new Page<>();
        Page<WebComment> oneCommentPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebComment>().eq("nid", noteId)
                        .eq("pid", "0")
                        .orderByDesc("like_count"));
        List<WebComment> oneCommentList = oneCommentPage.getRecords();
        if (!oneCommentList.isEmpty()) {
            Set<String> oneUids = oneCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            long onetotal = oneCommentPage.getTotal();
//            String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
            String currentUid = AuthContextHolder.getUserId();
            //得到对应的二级评论
            List<String> oneIds = oneCommentList.stream().map(WebComment::getId).collect(Collectors.toList());
            List<WebComment> twoCommentList = this.list(new QueryWrapper<WebComment>().in("pid", oneIds).orderByDesc("like_count").orderByDesc("create_time"));
            Set<String> twoUids = twoCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            oneUids.addAll(twoUids);

            List<WebUser> users = userMapper.selectBatchIds(oneUids);
            Map<String, WebUser> userMap = (users == null ? new java.util.ArrayList<WebUser>() : users)
                    .stream()
                    .collect(Collectors.toMap(WebUser::getId, user -> user));

            //得到当前用户点赞的评论（添加null检查）
            List<WebLikeOrCollection> likeOrCollections = likeOrCollectionMapper.selectList(new QueryWrapper<WebLikeOrCollection>().eq("uid", currentUid).eq("type", 2));
            List<String> likeComments = (likeOrCollections != null) ?
                    likeOrCollections.stream()
                            .filter(item -> item != null && item.getLikeOrCollectionId() != null)
                            .map(WebLikeOrCollection::getLikeOrCollectionId)
                            .collect(Collectors.toList()) :
                    new ArrayList<>();

            Set<String> replyUids = twoCommentList.stream().map(WebComment::getReplyUid).collect(Collectors.toSet());
            Map<String, WebUser> replyUserMap = new HashMap<>(16);
            if (!replyUids.isEmpty()) {
                List<WebUser> replyUsers = userMapper.selectBatchIds(replyUids);
                replyUserMap = replyUsers.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            }
            List<CommentVo> twoCommentVos = new ArrayList<>();
            for (WebComment twoComment : twoCommentList) {
                if (twoComment == null) continue; // 跳过null的评论
                
                CommentVo commentVo = ConvertUtils.sourceToTarget(twoComment, CommentVo.class);
                if (commentVo == null) continue; // 跳过转换失败的评论
                
                // 安全检查用户信息
                String twoCommentUid = twoComment.getUid();
                WebUser user = (twoCommentUid != null) ? userMap.get(twoCommentUid) : null;
                if (user != null) {
                    commentVo.setUsername(user.getUsername())
                            .setAvatar(user.getAvatar());
                }
                
                // 处理createTime可能为null的情况
                if (twoComment.getCreateTime() != null) {
                    commentVo.setTime(twoComment.getCreateTime().getTime());
                } else {
                    commentVo.setTime(System.currentTimeMillis());
                }
                
                // 安全检查点赞状态
                String twoCommentId = twoComment.getId();
                if (twoCommentId != null && likeComments != null) {
                    commentVo.setIsLike(likeComments.contains(twoCommentId));
                } else {
                    commentVo.setIsLike(false);
                }
                
                WebUser replyUser = replyUserMap.get(twoComment.getReplyUid());
                if (replyUser != null) {
                    commentVo.setReplyUsername(replyUser.getUsername());
                }
                twoCommentVos.add(commentVo);
            }

            Map<String, List<CommentVo>> twoCommentVoMap = twoCommentVos.stream()
                    .filter(vo -> vo != null && vo.getPid() != null)
                    .collect(Collectors.groupingBy(CommentVo::getPid));
            List<CommentVo> commentVoList = new ArrayList<>();
            for (WebComment oneComment : oneCommentList) {
                if (oneComment == null) continue; // 跳过null的评论
                
                CommentVo commentVo = ConvertUtils.sourceToTarget(oneComment, CommentVo.class);
                if (commentVo == null) continue; // 跳过转换失败的评论
                
                // 安全检查用户信息
                String oneCommentUid = oneComment.getUid();
                WebUser user = (oneCommentUid != null) ? userMap.get(oneCommentUid) : null;
                if (user != null) {
                    commentVo.setUsername(user.getUsername())
                            .setAvatar(user.getAvatar());
                }
                
                // 处理createTime可能为null的情况
                if (oneComment.getCreateTime() != null) {
                    commentVo.setTime(oneComment.getCreateTime().getTime());
                } else {
                    commentVo.setTime(System.currentTimeMillis());
                }
                
                // 安全检查点赞状态
                String oneCommentId = oneComment.getId();
                if (oneCommentId != null && likeComments != null) {
                    commentVo.setIsLike(likeComments.contains(oneCommentId));
                } else {
                    commentVo.setIsLike(false);
                }
                
                List<CommentVo> children = twoCommentVoMap.get(oneComment.getId());

                if (children != null && children.size() > 3) {
                    children = children.subList(0, 3);
                }
                commentVo.setChildren(children);
                commentVoList.add(commentVo);
            }
            result.setRecords(commentVoList);
            result.setTotal(onetotal);
        }
        return result;
    }

    @Override
    public Page<CommentVo> getCommentWithCommentByProductId(long currentPage, long pageSize, String productId) {
        //先得到所有的一级评论
        Page<CommentVo> result = new Page<>();
        Page<WebComment> oneCommentPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebComment>().eq("product_id", productId)
                        .eq("pid", "0")
                        .orderByDesc("like_count"));
        List<WebComment> oneCommentList = oneCommentPage.getRecords();
        if (!oneCommentList.isEmpty()) {
            Set<String> oneUids = oneCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            long oneTotal = oneCommentPage.getTotal();
//            String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
            String currentUid = AuthContextHolder.getUserId();
            //得到对应的二级评论
            List<String> oneIds = oneCommentList.stream().map(WebComment::getId).collect(Collectors.toList());
            List<WebComment> twoCommentList = this.list(new QueryWrapper<WebComment>().in("pid", oneIds).orderByDesc("like_count").orderByDesc("create_time"));
            Set<String> twoUids = twoCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            oneUids.addAll(twoUids);

            List<WebUser> users = userMapper.selectBatchIds(oneUids);
            Map<String, WebUser> userMap = users.stream().collect(Collectors.toMap(WebUser::getId, user -> user));

            //得到当前用户点赞的评论
            List<WebLikeOrCollection> likeOrCollections = likeOrCollectionMapper
                    .selectList(new QueryWrapper<WebLikeOrCollection>()
                            .eq("uid", currentUid)
                            .eq("type", 2));
            List<String> likeComments = likeOrCollections.stream().map(WebLikeOrCollection::getLikeOrCollectionId).collect(Collectors.toList());

            Set<String> replyUids = twoCommentList.stream().map(WebComment::getReplyUid).collect(Collectors.toSet());
            Map<String, WebUser> replyUserMap = new HashMap<>(16);
            if (!replyUids.isEmpty()) {
                List<WebUser> replyUsers = userMapper.selectBatchIds(replyUids);
                replyUserMap = replyUsers.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            }
            List<CommentVo> twoCommentVos = new ArrayList<>();
            for (WebComment twoComment : twoCommentList) {
                CommentVo commentVo = ConvertUtils.sourceToTarget(twoComment, CommentVo.class);
                WebUser user = userMap.get(twoComment.getUid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(twoComment.getCreateTime().getTime())
                        .setIsLike(likeComments.contains(twoComment.getId()));
                WebUser replyUser = replyUserMap.get(twoComment.getReplyUid());
                if (replyUser != null) {
                    commentVo.setReplyUsername(replyUser.getUsername());
                }
                twoCommentVos.add(commentVo);
            }

            Map<String, List<CommentVo>> twoCommentVoMap = twoCommentVos.stream().collect(Collectors.groupingBy(CommentVo::getPid));
            List<CommentVo> commentVoList = new ArrayList<>();
            for (WebComment oneComment : oneCommentList) {
                CommentVo commentVo = ConvertUtils.sourceToTarget(oneComment, CommentVo.class);
                WebUser user = userMap.get(oneComment.getUid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(oneComment.getCreateTime().getTime())
                        .setIsLike(likeComments.contains(oneComment.getId()));
                List<CommentVo> children = twoCommentVoMap.get(oneComment.getId());

                if (children != null && children.size() > 3) {
                    children = children.subList(0, 3);
                }
                commentVo.setChildren(children);
                commentVoList.add(commentVo);
            }
            result.setRecords(commentVoList);
            result.setTotal(oneTotal);
        }
        return result;
    }

    /**
     * 自动滚动到当前评论
     *
     * @param commentId 评论ID
     */
    @Override
    public Map<String, Object> scrollComment(String commentId) {
        Map<String, Object> resMap = new HashMap<>(16);
        WebComment comment = this.getById(commentId);
        String pid = comment.getPid();
        int page1 = 1;
        int page2 = 1;
        int limit1 = 7;
        int limit2 = 10;
        long total = 0;
        boolean flag = false;
        List<CommentVo> comments = new ArrayList<>();
        if ("0".equals(pid)) {
            while (!flag) {
                Page<CommentVo> allOneCommentPage = this.getCommentWithCommentByNoteId(page1, limit1, comment.getNid());
                List<CommentVo> commentVoList = allOneCommentPage.getRecords();
                List<String> pids = commentVoList.stream().map(CommentVo::getId).collect(Collectors.toList());
                if (pids.contains(commentId)) {
                    flag = true;
                    total = allOneCommentPage.getTotal();
                } else {
                    page1++;
                }
                comments.addAll(commentVoList);
            }
        } else {
            boolean flag2 = false;

            while (!flag) {
                IPage<CommentVo> allOneCommentPage = this.getCommentWithCommentByNoteId(page1, limit1, comment.getNid());
                List<CommentVo> commentVoList = allOneCommentPage.getRecords();
                List<String> pids = commentVoList.stream().map(CommentVo::getId).collect(Collectors.toList());
                if (pids.contains(pid)) {
                    for (CommentVo commentVo : commentVoList) {
                        if (Objects.equals(commentVo.getId(), pid)) {
                            List<CommentVo> comments2 = new ArrayList<>();
                            flag = true;
                            total = allOneCommentPage.getTotal();
                            while (!flag2) {
                                IPage<CommentVo> allTwoCommentPage = this.getTwoCommentByOneCommentId(page2, limit2, pid);
                                List<CommentVo> commentVoList2 = allTwoCommentPage.getRecords();
                                List<String> ids = commentVoList2.stream().map(CommentVo::getId).collect(Collectors.toList());
                                if (ids.contains(commentId)) {
                                    flag2 = true;
                                } else {
                                    page2++;
                                }
                                comments2.addAll(commentVoList2);
                            }
                            commentVo.setChildren(comments2);
                        }
                    }
                } else {
                    page1++;
                }
                comments.addAll(commentVoList);
            }
        }
        resMap.put("records", comments);
        resMap.put("total", total);
        resMap.put("page1", page1);
        resMap.put("page2", page2);
        return resMap;
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    @Override
    public void deleteCommentById(String commentId) {

    }
}
