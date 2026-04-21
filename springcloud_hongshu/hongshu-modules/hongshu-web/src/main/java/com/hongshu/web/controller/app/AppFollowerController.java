package com.hongshu.web.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.web.domain.vo.FollowerVo;
import com.hongshu.web.domain.vo.TrendUserVO;
import com.hongshu.web.domain.vo.TrendVo;
import com.hongshu.web.service.web.IWebFollowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注
 *
 * @author: hongshu
 */
@RequestMapping("/app/follower")
@RestController
public class AppFollowerController {

    @Autowired
    IWebFollowerService followerService;


    /**
     * 获取关注用户的所有动态
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getFollowTrend/{currentPage}/{pageSize}")
    public Result<?> getFollowTrend(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<TrendVo> pageInfo = followerService.getFollowTrend(currentPage, pageSize);
        return Result.ok(pageInfo);
    }

    /**
     * 获取关注列表
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getFollowList/{currentPage}/{pageSize}")
    public Result<?> getFollowList(@PathVariable long currentPage,
                                   @PathVariable long pageSize,
                                   @RequestParam(required = false) String userId) {
        Page<TrendVo> pageInfo = followerService.getFollowList(currentPage, pageSize, userId);
        return Result.ok(pageInfo);
    }

    @GetMapping("getFollowUser")
    public Result<?> getFollowUser() {
        List<TrendUserVO> trendVos = followerService.getFollowUser();
        return Result.ok(trendVos);
    }

    /**
     * 获取当前用户所有的关注和粉丝
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param type        类型
     */
    @GetMapping("getFriend/{currentPage}/{pageSize}")
    public Result<?> getFriend(@PathVariable long currentPage, @PathVariable long pageSize, String uid, Integer type) {
        Page<FollowerVo> pageInfo = followerService.getFriend(currentPage, pageSize, uid, type);
        return Result.ok(pageInfo);
    }

    /**
     * 关注用户
     *
     * @param followerId 关注用户ID
     */
    @GetMapping("followById")
    public Result<?> followById(String followerId) {
        followerService.followById(followerId);
        return Result.ok();
    }

    /**
     * 当前用户是否关注
     *
     * @param followerId 关注的用户ID
     */
    @GetMapping("isFollow")
    public Result<?> isFollow(String followerId) {
        boolean flag = followerService.isFollow(followerId);
        return Result.ok(flag);
    }

    /**
     * 获取当前用户的最新关注信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getNoticeFollower/{currentPage}/{pageSize}")
    public Result<?> getNoticeFollower(@PathVariable long currentPage, @PathVariable long pageSize) {
        Page<FollowerVo> pageInfo = followerService.getNoticeFollower(currentPage, pageSize);
        return Result.ok(pageInfo);
    }
}
