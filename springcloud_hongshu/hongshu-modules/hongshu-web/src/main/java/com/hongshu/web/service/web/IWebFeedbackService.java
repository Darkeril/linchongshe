package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.web.domain.dto.FeedbackDTO;
import com.hongshu.web.domain.entity.WebFeedback;
import com.hongshu.web.domain.vo.FeedbackVO;

/**
 * 反馈服务接口
 *
 * @author: hongshu
 */
public interface IWebFeedbackService extends IService<WebFeedback> {

    /**
     * 提交反馈
     *
     * @param feedbackDTO 反馈DTO
     * @param userId      用户ID
     * @return 反馈VO
     */
    FeedbackVO submitFeedback(FeedbackDTO feedbackDTO, String userId);

    /**
     * 获取反馈列表（分页）
     *
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @param type        反馈类型
     * @param userId      当前用户ID（用于判断是否点赞）
     * @return 分页结果
     */
    IPage<FeedbackVO> getFeedbackList(long currentPage, long pageSize, String type, String userId);

    /**
     * 点赞/取消点赞反馈
     *
     * @param feedbackId 反馈ID
     * @param userId     用户ID
     * @return 是否点赞成功
     */
    boolean likeFeedback(Long feedbackId, String userId);

}

