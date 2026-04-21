package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.domain.dto.FeedbackDTO;
import com.hongshu.web.domain.entity.WebFeedback;
import com.hongshu.web.domain.entity.WebFeedbackLike;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.FeedbackVO;
import com.hongshu.web.mapper.web.WebFeedbackLikeMapper;
import com.hongshu.web.mapper.web.WebFeedbackMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebFeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 反馈服务实现类
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebFeedbackServiceImpl extends ServiceImpl<WebFeedbackMapper, WebFeedback> implements IWebFeedbackService {

    @Autowired
    private WebFeedbackMapper feedbackMapper;
    @Autowired
    private WebFeedbackLikeMapper feedbackLikeMapper;
    @Autowired
    private WebUserMapper userMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedbackVO submitFeedback(FeedbackDTO feedbackDTO, String userId) {
        WebFeedback feedback = ConvertUtils.sourceToTarget(feedbackDTO, WebFeedback.class);
        feedback.setUid(userId);
        feedback.setLikeCount(0L);
        feedback.setStatus(0); // 待处理
        feedback.setCreateTime(new Date());
        feedback.setUpdateTime(new Date());

        if (StringUtils.isEmpty(feedback.getType())) {
            feedback.setType("feature"); // 默认类型
        }

        feedbackMapper.insert(feedback);

        // 转换为VO并返回
        FeedbackVO feedbackVO = ConvertUtils.sourceToTarget(feedback, FeedbackVO.class);
        feedbackVO.setIsLiked(false);

        // 查询用户信息
        WebUser user = userMapper.selectById(userId);
        if (user != null) {
            feedbackVO.setNickname(user.getUsername());
            feedbackVO.setAvatar(user.getAvatar());
        }

        return feedbackVO;
    }

    @Override
    public IPage<FeedbackVO> getFeedbackList(long currentPage, long pageSize, String type, String userId) {
        Page<WebFeedback> page = new Page<>(currentPage, pageSize);
        QueryWrapper<WebFeedback> queryWrapper = new QueryWrapper<>();

        // 按类型筛选
        if (StringUtils.isNotEmpty(type)) {
            queryWrapper.eq("type", type);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("create_time");

        IPage<WebFeedback> feedbackPage = feedbackMapper.selectPage(page, queryWrapper);

        // 转换为VO
        IPage<FeedbackVO> voPage = feedbackPage.convert(feedback -> {
            FeedbackVO vo = ConvertUtils.sourceToTarget(feedback, FeedbackVO.class);

            // 查询用户信息
            WebUser user = userMapper.selectById(feedback.getUid());
            if (user != null) {
                vo.setNickname(user.getUsername());
                vo.setAvatar(user.getAvatar());
            }

            return vo;
        });

        // 查询当前用户点赞的反馈ID集合
        Set<Long> likedFeedbackIds = getLikedFeedbackIds(userId);

        // 设置是否点赞
        voPage.getRecords().forEach(vo -> {
            vo.setIsLiked(likedFeedbackIds.contains(vo.getId()));
        });

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeFeedback(Long feedbackId, String userId) {
        // 查询是否已点赞
        QueryWrapper<WebFeedbackLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", userId);
        queryWrapper.eq("feedback_id", feedbackId);
        WebFeedbackLike existingLike = feedbackLikeMapper.selectOne(queryWrapper);

        WebFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        if (existingLike != null) {
            // 取消点赞
            feedbackLikeMapper.deleteById(existingLike.getId());
            feedback.setLikeCount(Math.max(0, feedback.getLikeCount() - 1));
            feedbackMapper.updateById(feedback);
            return false;
        } else {
            // 点赞
            WebFeedbackLike like = new WebFeedbackLike();
            like.setUid(userId);
            like.setFeedbackId(feedbackId);
            like.setCreateTime(new Date());
            like.setUpdateTime(new Date());
            feedbackLikeMapper.insert(like);
            feedback.setLikeCount((feedback.getLikeCount() == null ? 0 : feedback.getLikeCount()) + 1);
            feedbackMapper.updateById(feedback);
            return true;
        }
    }

    /**
     * 获取用户点赞的反馈ID集合
     */
    private Set<Long> getLikedFeedbackIds(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return Collections.emptySet();
        }

        QueryWrapper<WebFeedbackLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", userId);
        List<WebFeedbackLike> likes = feedbackLikeMapper.selectList(queryWrapper);

        return likes.stream()
                .map(WebFeedbackLike::getFeedbackId)
                .collect(Collectors.toSet());
    }
}

