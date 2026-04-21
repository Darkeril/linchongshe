package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.web.domain.entity.WebFollower;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebNoteBehavior;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.FanProfileDistributionVo;
import com.hongshu.web.domain.vo.FanProfileOverviewVo;
import com.hongshu.web.domain.vo.FanProfileDistributionVo.NameValueVo;
import com.hongshu.web.mapper.web.WebCategoryMapper;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebNoteBehaviorMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.IFanProfileService;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.domain.dto.DemoAccountConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据中心-粉丝画像 Service 实现
 */
@Slf4j
@Service
public class FanProfileServiceImpl implements IFanProfileService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final String[] AGE_GROUPS = {"<18", "18-24", "25-34", "35-44", ">44"};
    private static final String GENDER_MALE = "1";
    private static final String GENDER_FEMALE = "2";

    @Autowired
    private WebFollowerMapper followerMapper;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebNoteBehaviorMapper noteBehaviorMapper;
    @Autowired
    private WebCategoryMapper categoryMapper;
    @Autowired
    private ISysSystemConfigService systemConfigService;

    @Override
    public FanProfileOverviewVo getOverview(String uid, int days) {
        String creatorUid = this.resolveCreatorUid(uid);
        FanProfileOverviewVo vo = new FanProfileOverviewVo();
        vo.setTotalFans(0L);
        vo.setNewFans(0L);
        vo.setLostFans(null);
        vo.setDates(new ArrayList<>());
        vo.setTotal(new ArrayList<>());
        vo.setNewFansTrend(new ArrayList<>());
        vo.setLostFansTrend(new ArrayList<>());
        if (ValidatorUtil.isNull(creatorUid)) {
            return vo;
        }
        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startTime = cal.getTime();

        long totalFans = followerMapper.selectCount(
                new QueryWrapper<WebFollower>().eq("fid", creatorUid));
        long newFansInPeriod = followerMapper.selectCount(
                new QueryWrapper<WebFollower>()
                        .eq("fid", creatorUid)
                        .ge("create_time", startTime)
                        .le("create_time", endTime));
        vo.setTotalFans(totalFans);
        vo.setNewFans(newFansInPeriod);

        List<WebFollower> listInRange = followerMapper.selectList(
                new QueryWrapper<WebFollower>()
                        .eq("fid", creatorUid)
                        .ge("create_time", startTime)
                        .le("create_time", endTime)
                        .select("id", "create_time"));
        LocalDate startDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Map<LocalDate, Long> newCountByDate = listInRange.stream()
                .filter(f -> f.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()));
        List<String> dates = new ArrayList<>();
        List<Long> newFansTrend = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dates.add(d.format(DATE_FMT));
            newFansTrend.add(newCountByDate.getOrDefault(d, 0L));
        }
        vo.setDates(dates);
        vo.setNewFansTrend(newFansTrend);
        List<Long> totalTrend = new ArrayList<>(dates.size());
        long running = totalFans;
        for (int i = newFansTrend.size() - 1; i >= 0; i--) {
            totalTrend.add(0, running);
            running -= newFansTrend.get(i);
        }
        vo.setTotal(totalTrend);
        vo.setLostFansTrend(Collections.emptyList());
        return vo;
    }

    @Override
    public FanProfileDistributionVo getDistribution(String uid) {
        String creatorUid = resolveCreatorUid(uid);
        FanProfileDistributionVo vo = new FanProfileDistributionVo();
        vo.setGender(new ArrayList<>());
        vo.setAge(new ArrayList<>());
        vo.setRegion(new ArrayList<>());
        vo.setInterest(new ArrayList<>());
        if (ValidatorUtil.isNull(creatorUid)) {
            return vo;
        }
        List<WebFollower> followers = followerMapper.selectList(
                new QueryWrapper<WebFollower>().eq("fid", creatorUid).select("uid"));
        Set<String> fanIds = followers.stream().map(WebFollower::getUid).filter(Objects::nonNull).collect(Collectors.toSet());
        if (fanIds.isEmpty()) {
            return vo;
        }
        List<WebUser> users = userMapper.selectBatchIds(new ArrayList<>(fanIds));
        if (users == null || users.isEmpty()) {
            return vo;
        }

        Map<String, Long> genderCount = users.stream()
                .filter(u -> ValidatorUtil.isNotNull(u.getGender()))
                .collect(Collectors.groupingBy(WebUser::getGender, Collectors.counting()));
        long male = genderCount.getOrDefault(GENDER_MALE, 0L);
        long female = genderCount.getOrDefault(GENDER_FEMALE, 0L);
        long other = users.size() - male - female;
        if (other < 0) other = 0;
        List<NameValueVo> gender = new ArrayList<>();
        if (male > 0) {
            NameValueVo nv = new NameValueVo();
            nv.setName("男性");
            nv.setValue((int) male);
            gender.add(nv);
        }
        if (female > 0) {
            NameValueVo nv = new NameValueVo();
            nv.setName("女性");
            nv.setValue((int) female);
            gender.add(nv);
        }
        if (other > 0) {
            NameValueVo nv = new NameValueVo();
            nv.setName("未知");
            nv.setValue((int) other);
            gender.add(nv);
        }
        vo.setGender(gender);

        Map<String, Long> ageCount = new LinkedHashMap<>();
        for (String ag : AGE_GROUPS) {
            ageCount.put(ag, 0L);
        }
        int currentYear = LocalDate.now().getYear();
        for (WebUser u : users) {
            String birthday = u.getBirthday();
            if (ValidatorUtil.isNull(birthday) || birthday.length() < 4) {
                continue;
            }
            int birthYear;
            try {
                birthYear = Integer.parseInt(birthday.substring(0, 4));
            } catch (NumberFormatException e) {
                continue;
            }
            int age = currentYear - birthYear;
            String group;
            if (age < 18) group = "<18";
            else if (age <= 24) group = "18-24";
            else if (age <= 34) group = "25-34";
            else if (age <= 44) group = "35-44";
            else group = ">44";
            ageCount.put(group, ageCount.get(group) + 1);
        }
        vo.setAge(ageCount.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> {
                    NameValueVo nv = new NameValueVo();
                    nv.setName(e.getKey());
                    nv.setValue(e.getValue().intValue());
                    return nv;
                })
                .collect(Collectors.toList()));

        Map<String, Long> regionCount = users.stream()
                .filter(u -> ValidatorUtil.isNotNull(u.getProvince()))
                .collect(Collectors.groupingBy(WebUser::getProvince, Collectors.counting()));
        List<NameValueVo> region = regionCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    NameValueVo nv = new NameValueVo();
                    nv.setName(e.getKey());
                    nv.setValue(e.getValue().intValue());
                    return nv;
                })
                .collect(Collectors.toList());
        vo.setRegion(region);

        // 粉丝兴趣分布：基于粉丝最近浏览/观看过的笔记分类统计（内容分类）
        Map<String, Long> categoryCount = new HashMap<>();
        try {
            if (!fanIds.isEmpty()) {
                // 1. 查找这些粉丝在行为表中的观看记录（view/watch），限定为这些粉丝产生的行为
                List<WebNoteBehavior> behaviors = noteBehaviorMapper.selectList(
                        new QueryWrapper<WebNoteBehavior>()
                                .in("uid", fanIds)
                                .in("event_type", Arrays.asList("view", "watch"))
                                .select("nid", "uid"));
                if (behaviors != null && !behaviors.isEmpty()) {
                    Set<String> noteIds = behaviors.stream()
                            .map(WebNoteBehavior::getNid)
                            .filter(ValidatorUtil::isNotNull)
                            .collect(Collectors.toSet());
                    if (!noteIds.isEmpty()) {
                        // 2. 查找这些笔记，并按一级分类ID(cpid)统计
                        List<WebNote> notes = noteMapper.selectList(
                                new QueryWrapper<WebNote>()
                                        .in("id", noteIds)
                                        .eq("deleted", 0)
                                        .select("id", "cpid"));
                        Map<String, Long> cnt = notes.stream()
                                .filter(n -> ValidatorUtil.isNotNull(n.getCpid()))
                                .collect(Collectors.groupingBy(WebNote::getCpid, Collectors.counting()));
                        categoryCount.putAll(cnt);
                    }
                }
            }
        } catch (Exception e) {
            log.error("计算粉丝兴趣分布（按分类）失败", e);
        }
        if (!categoryCount.isEmpty()) {
            // 3. 将分类ID映射为分类名称
            List<String> categoryIds = categoryCount.keySet().stream()
                    .filter(ValidatorUtil::isNotNull)
                    .collect(Collectors.toList());
            if (!categoryIds.isEmpty()) {
                List<com.hongshu.web.domain.entity.WebCategory> categories =
                        categoryMapper.selectBatchIds(categoryIds);
                Map<String, String> idNameMap = categories.stream()
                        .collect(Collectors.toMap(
                                com.hongshu.web.domain.entity.WebCategory::getId,
                                com.hongshu.web.domain.entity.WebCategory::getTitle,
                                (a, b) -> a));
                List<NameValueVo> interest = categoryCount.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(12)
                        .map(e -> {
                            String name = idNameMap.getOrDefault(e.getKey(), e.getKey());
                            NameValueVo nv = new NameValueVo();
                            nv.setName(name);
                            nv.setValue(e.getValue().intValue());
                            return nv;
                        })
                        .collect(Collectors.toList());
                vo.setInterest(interest);
            }
        }
        return vo;
    }

    private String resolveCreatorUid(String uid) {
        // 1. 前端显式传入 uid 时，优先使用
        if (ValidatorUtil.isNotNull(uid) && !uid.isEmpty()) {
            return uid;
        }

        // 2. 如果演示账号开启，并配置了用户名，则优先使用演示账号
        try {
            DemoAccountConfigDTO demoConfig = systemConfigService.getDemoAccountConfig();
            if (demoConfig != null
                    && Boolean.TRUE.equals(demoConfig.getEnabled())
                    && demoConfig.getUsername() != null
                    && !demoConfig.getUsername().isEmpty()) {
                WebUser user = userMapper.selectOne(
                        new QueryWrapper<WebUser>()
                                .lambda()
                                .eq(WebUser::getPhone, demoConfig.getUsername())
                );
                if (user != null && ValidatorUtil.isNotNull(user.getId())) {
                    return String.valueOf(user.getId());
                }
            }
        } catch (Exception e) {
            log.warn("解析演示账号配置失败，回退到当前登录用户: {}", e.getMessage());
        }

        // 3. 回退：当前登录用户
        try {
            return AuthContextHolder.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
