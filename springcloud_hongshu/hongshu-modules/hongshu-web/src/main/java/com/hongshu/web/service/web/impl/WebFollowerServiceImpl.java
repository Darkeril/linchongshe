package com.hongshu.web.service.web.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.entity.WebFollower;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.FollowerVo;
import com.hongshu.web.domain.vo.TrendUserVO;
import com.hongshu.web.domain.vo.TrendVo;
import com.hongshu.web.im.ChatUtils;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebFollowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hongshu
 */
@Service
public class WebFollowerServiceImpl extends ServiceImpl<WebFollowerMapper, WebFollower> implements IWebFollowerService {

    /** 关注流 IN(uid) 上限，避免关注人数过多时单条 SQL 过大 */
    private static final int MAX_FOLLOW_IDS_FOR_TREND = 1000;

    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebFollowerMapper followerMapper;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;
    @Autowired
    private ChatUtils chatUtils;


    /**
     * 列表/关注流：按需列查询（非 SELECT *）。含 content 供前端展示笔记描述/摘要。
     */
    private static final String[] TREND_NOTE_LIST_COLUMNS = {
            "id", "uid", "title", "content", "note_cover", "note_type",
            "urls", "address", "like_count", "comment_count", "view_count",
            "update_time", "create_time"
    };

    /**
     * 获取关注用户的所有动态
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<TrendVo> getFollowTrend(long currentPage, long pageSize) {
        Page<TrendVo> page = new Page<>();
        String currentUid = AuthContextHolder.getUserId();
        if (currentUid == null || currentUid.isEmpty()) {
            page.setRecords(new ArrayList<>());
            return page;
        }
        List<WebFollower> followers = this.list(new QueryWrapper<WebFollower>()
                .select("fid")
                .eq("uid", currentUid));
        if (followers == null) {
            followers = new ArrayList<>();
        }
        List<String> fids = followers.stream()
                .filter(Objects::nonNull)
                .map(WebFollower::getFid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (fids.size() > MAX_FOLLOW_IDS_FOR_TREND) {
            fids = fids.subList(0, MAX_FOLLOW_IDS_FOR_TREND);
        }
        fids.add(currentUid);
        Page<WebNote> mpPage = new Page<>((int) currentPage, (int) pageSize);
        if (currentPage > 1) {
            mpPage.setSearchCount(false);
        }
        Page<WebNote> notePage = noteMapper.selectPage(mpPage,
                new QueryWrapper<WebNote>()
                        .select(TREND_NOTE_LIST_COLUMNS)
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .in("uid", fids)
                        .orderByDesc("update_time"));
        List<WebNote> notes = notePage.getRecords();
        List<TrendVo> trendVos = new ArrayList<>();
        if (!notes.isEmpty()) {
            List<String> ids = notes.stream().map(WebNote::getUid).distinct().collect(Collectors.toList());
            List<WebUser> users = userMapper.selectBatchIds(ids);
            HashMap<String, WebUser> userMap = new HashMap<>();
            users.forEach(item -> userMap.put(String.valueOf(item.getId()), item));

            List<String> pageNids = notes.stream().map(n -> String.valueOf(n.getId())).collect(Collectors.toList());
            Set<String> likedNidSet = new HashSet<>();
            if (!pageNids.isEmpty()) {
                List<WebLikeOrCollection> pageLikes = likeOrCollectionMapper.selectList(
                        new QueryWrapper<WebLikeOrCollection>()
                                .eq("uid", currentUid)
                                .eq("type", 1)
                                .in("like_or_collection_id", pageNids));
                likedNidSet = pageLikes.stream()
                        .map(WebLikeOrCollection::getLikeOrCollectionId)
                        .collect(Collectors.toSet());
            }

            for (WebNote note : notes) {
                TrendVo trendVo = new TrendVo();
                WebUser user = userMap.get(note.getUid());
                if (user == null) {
                    continue;
                }
                trendVo.setUid(String.valueOf(user.getId()))
                        .setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setNoteCover(note.getNoteCover())
                        .setNoteType(note.getNoteType())
                        .setNid(String.valueOf(note.getId()))
                        .setAddress(note.getAddress())
                        .setTime(note.getUpdateTime() != null ? note.getUpdateTime().getTime()
                                : (note.getCreateTime() != null ? note.getCreateTime().getTime() : 0L))
                        .setTitle(note.getTitle())
                        .setContent(note.getContent())
                        .setCommentCount(note.getCommentCount())
                        .setLikeCount(note.getLikeCount())
                        .setIsLike(likedNidSet.contains(String.valueOf(note.getId())))
                        .setIsLoading(false);
                String urls = note.getUrls();
                List<String> imgList = Collections.emptyList();
                if (StringUtils.isNotBlank(urls)) {
                    try {
                        imgList = JSONUtil.toList(urls, String.class);
                    } catch (Exception ignore) {
                        imgList = Collections.emptyList();
                    }
                }
                trendVo.setImgUrls(imgList);
                trendVos.add(trendVo);
            }
        }
        long total = notePage.getTotal();
        page.setTotal(total);
        page.setRecords(trendVos);
        return page;
    }

    /**
     * 获取关注列表
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<TrendVo> getFollowList(long currentPage, long pageSize, String userId) {
        Page<TrendVo> page = new Page<>();
        String currentUid = AuthContextHolder.getUserId();

        // 1. 查询我关注的所有用户
        List<WebFollower> followers = this.list(new QueryWrapper<WebFollower>().eq("uid", currentUid));
        List<String> fids = followers.stream()
                .filter(Objects::nonNull)
                .map(WebFollower::getFid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (fids.isEmpty()) {
            return page;
        }

        // 2. 关注人过多时做限制，避免 in 查询过大
        if (fids.size() > MAX_FOLLOW_IDS_FOR_TREND) {
            fids = fids.subList(0, MAX_FOLLOW_IDS_FOR_TREND);
        }

        // 3. 构建查询条件（列表不查 content，减轻负载）
        QueryWrapper<WebNote> queryWrapper = new QueryWrapper<WebNote>()
                .select(TREND_NOTE_LIST_COLUMNS)
                .eq("deleted", 0)
                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                .in("uid", fids)
                .orderByDesc("update_time");

        // 4. 如果指定了userId，则只查询该用户的内容
        if (StringUtils.isNotBlank(userId) && fids.contains(userId)) {
            queryWrapper.eq("uid", userId);
        }

        // 5. 分页查询关注用户的内容
        Page<WebNote> mpPage = new Page<>((int) currentPage, (int) pageSize);
        if (currentPage > 1) {
            mpPage.setSearchCount(false);
        }
        Page<WebNote> notePage = noteMapper.selectPage(mpPage, queryWrapper);

        List<WebNote> notes = notePage.getRecords();
        List<TrendVo> trendVos = new ArrayList<>();
        if (!notes.isEmpty()) {
            // 6. 批量查用户信息
            List<String> ids = notes.stream().map(WebNote::getUid).collect(Collectors.toList());
            List<WebUser> users = userMapper.selectBatchIds(ids);
            Map<String, WebUser> userMap = users.stream()
                    .collect(Collectors.toMap(u -> String.valueOf(u.getId()), u -> u));

            // 7. 仅当前页笔记的点赞状态（避免拉全量点赞）
            List<String> pageNids = notes.stream().map(n -> String.valueOf(n.getId())).collect(Collectors.toList());
            Set<String> likeOrCollectionIdSet = new HashSet<>();
            if (!pageNids.isEmpty()) {
                likeOrCollectionIdSet = likeOrCollectionMapper.selectList(
                        new QueryWrapper<WebLikeOrCollection>()
                                .eq("uid", currentUid)
                                .eq("type", 1)
                                .in("like_or_collection_id", pageNids)
                ).stream()
                        .map(WebLikeOrCollection::getLikeOrCollectionId)
                        .collect(Collectors.toSet());
            }

            // 8. 组装VO
            for (WebNote note : notes) {
                TrendVo trendVo = new TrendVo();
                WebUser user = userMap.get(note.getUid());
                if (user == null) continue; // 防止脏数据
                trendVo.setUid(String.valueOf(user.getId()))
                        .setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setNoteType(note.getNoteType())
                        .setNoteCover(note.getNoteCover())
                        .setTitle(note.getTitle())
                        .setAddress(note.getAddress())
                        .setNid(String.valueOf(note.getId()))
                        .setTime(note.getUpdateTime() != null ? note.getUpdateTime().getTime() :
                                (note.getCreateTime() != null ? note.getCreateTime().getTime() : 0L))
                        .setContent(note.getContent())
                        .setCommentCount(note.getCommentCount())
                        .setLikeCount(note.getLikeCount())
                        .setViewCount(note.getViewCount())
                        .setIsLike(likeOrCollectionIdSet.contains(String.valueOf(note.getId())))
                        .setIsLoading(false);

                // 返回所有图片
                String urls = note.getUrls();
                List<String> imgList = StringUtils.isNotBlank(urls) ? JSONUtil.toList(urls, String.class) : Collections.emptyList();
                trendVo.setImgUrls(imgList);
                trendVos.add(trendVo);
            }
        }
        page.setTotal(notePage.getTotal());
        page.setRecords(trendVos);
        return page;
    }

    @Override
    public List<TrendUserVO> getFollowUser() {
        String currentUid = AuthContextHolder.getUserId();
        // 1. 查询我关注的所有用户
        List<WebFollower> followers = this.list(new QueryWrapper<WebFollower>().eq("uid", currentUid));
        // 使用Set去重关注用户ID
        List<String> fids = followers.stream()
                .filter(Objects::nonNull)
                .map(WebFollower::getFid)
                .filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        if (fids.isEmpty()) {
            return new ArrayList<>();
        }
        if (fids.size() > MAX_FOLLOW_IDS_FOR_TREND) {
            fids = fids.subList(0, MAX_FOLLOW_IDS_FOR_TREND);
        }
        // 3. 批量查询用户信息
        List<WebUser> users = userMapper.selectBatchIds(fids);
        // 4. 一次性查询用户的内容状态（最近七天 + 所有时间）
        Map<String, ContentStatus> userContentStatusMap = this.checkUsersContentStatus(fids);
        // 5. 组装VO
        return users.stream()
                .map(user -> {
                    String userId = String.valueOf(user.getId());
                    ContentStatus status = userContentStatusMap.getOrDefault(userId, new ContentStatus());
                    return new TrendUserVO()
                            .setUid(userId)
                            .setUsername(user.getUsername())
                            .setAvatar(user.getAvatar())
                            .setTag(status.hasNewContent)
                            .setHasContent(status.hasContent);
                })
                .sorted(Comparator.comparing(TrendUserVO::getTag).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 内容状态类
     */
    private static class ContentStatus {
        boolean hasContent = false;    // 是否有发布过内容
        boolean hasNewContent = false; // 最近三天是否有新内容
    }

    /**
     * 检查用户的内容状态
     */
    private Map<String, ContentStatus> checkUsersContentStatus(List<String> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        // 计算七天前的时间
        Date sevenDaysAgo = DateUtil.offsetDay(new Date(), -7);
        // 查询所有发布过内容的用户
        List<Object> allContentUserIds = noteMapper.selectObjs(
                new QueryWrapper<WebNote>()
                        .select("DISTINCT uid")
                        .in("uid", userIds)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
        );
        // 查询最近七天发布过内容的用户
        List<Object> newContentUserIds = noteMapper.selectObjs(
                new QueryWrapper<WebNote>()
                        .select("DISTINCT uid")
                        .in("uid", userIds)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .ge("create_time", sevenDaysAgo)
        );
        // 构建用户内容状态映射
        Map<String, ContentStatus> userContentStatusMap = new HashMap<>();
        // 初始化所有用户
        for (String userId : userIds) {
            userContentStatusMap.put(userId, new ContentStatus());
        }
        // 设置发布过内容的用户
        for (Object userIdObj : allContentUserIds) {
            String uid = String.valueOf(userIdObj);
            ContentStatus status = userContentStatusMap.get(uid);
            if (status != null) {
                status.hasContent = true;
            }
        }
        // 设置最近三天有新内容的用户
        for (Object userIdObj : newContentUserIds) {
            String uid = String.valueOf(userIdObj);
            ContentStatus status = userContentStatusMap.get(uid);
            if (status != null) {
                status.hasNewContent = true;
            }
        }
        return userContentStatusMap;
    }

    /**
     * 获取当前用户所有的关注和粉丝
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param type        类型
     */
    @Override
    public Page<FollowerVo> getFriend(long currentPage, long pageSize, String uid, Integer type) {

        Page<FollowerVo> result = new Page<>();
        Page<WebFollower> followerPage = null;
        List<WebFollower> followerList = null;
        Set<String> uids = null;
        if (type == 0) {
            // 粉丝列表
            followerPage = this.page(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<WebFollower>().eq("fid", uid));
            followerList = followerPage.getRecords();
            uids = followerList.stream().filter(Objects::nonNull).map(WebFollower::getUid).filter(Objects::nonNull).collect(Collectors.toSet());
        } else if (type == 1) {
            // 关注列表
            followerPage = this.page(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<WebFollower>().eq("uid", uid));
            followerList = followerPage.getRecords();
            uids = followerList.stream().filter(Objects::nonNull).map(WebFollower::getFid).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        long total = followerPage != null ? followerPage.getTotal() : 0;
//        Map<String, WebUser> userMap = userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));

        Map<String, WebUser> userMap = new HashMap<>();
        if (uids != null && !uids.isEmpty()) {
            userMap = userMapper.selectBatchIds(uids).stream()
                    .collect(Collectors.toMap(WebUser::getId, user -> user));
        }

        // 得到当前用户的所有关注
//        List<WebFollower> followers = this.list(new QueryWrapper<WebFollower>().eq("uid", uid));
//        Set<String> followerSet = followers.stream().map(WebFollower::getFid).collect(Collectors.toSet());

        List<FollowerVo> followerVoList = new ArrayList<>();
        Map<String, WebUser> finalUserMap = userMap;
        followerList.forEach(item -> {
            WebUser user = null;
            FollowerVo followerVo = new FollowerVo();
            if (type == 0) {
                user = finalUserMap.get(item.getUid());
            } else if (type == 1) {
                user = finalUserMap.get(item.getFid());
            }
            // 如果用户不存在（可能已被删除），跳过该记录
            if (user == null) {
                return;
            }
            followerVo.setUid(user.getId())
                    .setUsername(user.getUsername())
                    .setAvatar(user.getAvatar())
                    .setHsId(user.getHsId())
                    .setFanCount(user.getFanCount())
                    .setTime(item.getCreateTime().getTime());
//                    .setIsFollow(followerSet.contains(item.getUid()));
            followerVoList.add(followerVo);
        });
        result.setRecords(followerVoList);
        result.setTotal(total);
        return result;
    }

    /**
     * 关注用户
     *
     * @param followerId 关注用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void followById(String followerId) {
        WebFollower follower = new WebFollower();
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();
        follower.setUid(userId);
        follower.setFid(followerId);
        follower.setCreateTime(new Date());
        follower.setUpdateTime(new Date());
        // 得到当前用户
        WebUser currentUser = userMapper.selectById(userId);
        WebUser followerUser = userMapper.selectById(followerId);
        if (isFollow(followerId)) {
            currentUser.setFollowerCount(currentUser.getFollowerCount() - 1);
            followerUser.setFanCount(followerUser.getFanCount() - 1);
            this.remove(new QueryWrapper<WebFollower>().eq("uid", userId).eq("fid", followerId));
        } else {
            currentUser.setFollowerCount(currentUser.getFollowerCount() + 1);
            followerUser.setFanCount(followerUser.getFanCount() + 1);
            this.save(follower);
            chatUtils.sendMessage(followerId, 2);
        }
        userMapper.updateById(currentUser);
        userMapper.updateById(followerUser);
    }

    /**
     * 当前用户是否关注
     *
     * @param followerId 关注的用户ID
     */
    @Override
    public boolean isFollow(String followerId) {
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();
        long count = this.count(new QueryWrapper<WebFollower>().eq("uid", userId).eq("fid", followerId));
        return count > 0;
    }

    /**
     * 获取当前用户的最新关注信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<FollowerVo> getNoticeFollower(long currentPage, long pageSize) {
        Page<FollowerVo> result = new Page<>();
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();

        Page<WebFollower> followerPage = this.page(
                new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebFollower>()
                        .eq("fid", userId)
                        .ne("uid", userId)
                        .orderByDesc("create_time")
        );
        List<WebFollower> followerList = followerPage.getRecords();
        long total = followerPage.getTotal();

        Set<String> uids = followerList.stream()
                .filter(Objects::nonNull)
                .map(WebFollower::getUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 关键：如果uids为空，直接返回空结果，避免SQL报错
        if (uids.isEmpty()) {
            result.setRecords(new ArrayList<>());
            result.setTotal(total);
            return result;
        }

        Map<String, WebUser> userMap = userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 得到当前用户的所有关注
        List<WebFollower> followers = this.list(new QueryWrapper<WebFollower>().eq("uid", userId));
        Set<String> followerSet = followers.stream().filter(Objects::nonNull).map(WebFollower::getFid).filter(Objects::nonNull).collect(Collectors.toSet());

        List<FollowerVo> followerVoList = new ArrayList<>();
        followerList.forEach(item -> {
            WebUser user = userMap.get(item.getUid());
            if (user != null) { // 防止user为null
                FollowerVo followerVo = new FollowerVo();
                followerVo.setUid(String.valueOf(user.getId()))
                        .setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(item.getCreateTime().getTime())
                        .setIsFollow(followerSet.contains(item.getUid()));
                followerVoList.add(followerVo);
            }
        });
        result.setRecords(followerVoList);
        result.setTotal(total);
        return result;
    }
}
