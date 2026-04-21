package com.hongshu.web.controller.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.ValidatorUtils;
import com.hongshu.common.core.validator.group.AddGroup;
import com.hongshu.web.domain.dto.FeedbackDTO;
import com.hongshu.web.domain.vo.FeedbackVO;
import com.hongshu.web.domain.vo.PlannedFeatureVO;
import com.hongshu.web.service.web.IWebFeedbackService;
import com.hongshu.web.service.web.IWebPlannedFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反馈控制器
 *
 * @author: hongshu
 */
@RestController
@RequestMapping("/feedback")
public class WebFeedbackController {

    @Autowired
    private IWebFeedbackService feedbackService;
    @Autowired
    private IWebPlannedFeatureService plannedFeatureService;


    /**
     * 提交反馈
     */
    @PostMapping("/submit")
    public Result<?> submitFeedback(@RequestBody FeedbackDTO feedbackDTO) {
        ValidatorUtils.validateEntity(feedbackDTO, AddGroup.class);
        String userId = AuthContextHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }

        FeedbackVO feedbackVO = feedbackService.submitFeedback(feedbackDTO, userId);
        return Result.ok(feedbackVO);
    }

    /**
     * 获取反馈列表（分页）
     */
    @GetMapping("/list/{currentPage}/{pageSize}")
    public Result<?> getFeedbackList(
            @PathVariable long currentPage,
            @PathVariable long pageSize,
            @RequestParam(required = false) String type) {
        String userId = AuthContextHolder.getUserId();
        IPage<FeedbackVO> page = feedbackService.getFeedbackList(currentPage, pageSize, type, userId);
        return Result.ok(page);
    }

    /**
     * 点赞/取消点赞反馈
     */
    @PostMapping("/like/{feedbackId}")
    public Result<?> likeFeedback(@PathVariable Long feedbackId) {
        String userId = AuthContextHolder.getUserId();
        if (userId == null) {
            return Result.fail("请先登录");
        }

        boolean isLiked = feedbackService.likeFeedback(feedbackId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        return Result.ok(result);
    }

    /**
     * 获取待实现功能列表（前端显示用）
     */
    @GetMapping("/plannedFeatures")
    public Result<?> getPlannedFeatures() {
        List<PlannedFeatureVO> features = plannedFeatureService.getVisiblePlannedFeatures();
        return Result.ok(features);
    }

}

