package com.hongshu.web.service.sys.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongshu.common.core.constant.Constantss;
import com.hongshu.common.core.domain.IpLoginFrequencyVO;
import com.hongshu.common.core.domain.LoginLocationStatVO;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.EStatus;
import com.hongshu.common.core.global.SysConf;
import com.hongshu.system.api.RemoteChatService;
import com.hongshu.system.api.RemoteIdleService;
import com.hongshu.web.domain.entity.DailyStatistics;
import com.hongshu.web.domain.vo.DailyNewDataVO;
import com.hongshu.web.domain.vo.UserLocationVO;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.idle.IdleProductOrderMapper;
import com.hongshu.web.mapper.sys.SysLoginInforMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.*;
import com.hongshu.web.service.web.IWebUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 首页RestApi 业务层处理
 *

 */
@Service
public class SysDashboardServiceImpl implements ISysDashboardService {

    @Autowired
    private IWebUserService userService;
    @Autowired
    private ISysNoteService noteService;
    @Autowired
    private RemoteIdleService remoteIdleService;
    @Autowired
    private RemoteChatService remoteChatService;
    @Autowired
    private ISysCommentService commentService;
    @Autowired
    private ISysVisitService visitService;
    @Autowired
    private ISysMemberService memberService;
    @Autowired
    private WebUserMapper userMapper;

    @Autowired
    private IdleProductOrderMapper idleProductOrderMapper;

    @Autowired
    private WebNoteMapper webNoteMapper;

    @Autowired
    private IdleProductMapper idleProductMapper;

    @Autowired
    private SysLoginInforMapper webLoginInforMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public Map<String, Object> getInitData() {
        Map<String, Object> map = new HashMap<>(Constantss.NUM_FOUR);
        map.put(SysConf.VISIT_COUNT, visitService.getWebVisitCount());
        map.put(SysConf.USER_COUNT, memberService.getMemberCount(0));
        map.put(SysConf.BLOG_COUNT, noteService.getNoteCount(EStatus.ENABLE));
        map.put(SysConf.COMMENT_COUNT, commentService.getCommentCount(EStatus.ENABLE));
        map.put(SysConf.PRODUCT_COUNT, remoteIdleService.getProductCount().getData());
        map.put(SysConf.GPT_MODEL_COUNT, remoteChatService.getGptModelCount().getData());
        return map;
    }

    @Override
    public Map<String, Object> getVisitByWeek() {
        return visitService.getVisitByWeek();
    }

    @Override
    public List<Map<String, Object>> getNoteCountByType() {
        return noteService.getNoteCountByType();
    }

    @Override
    public List<Map<String, Object>> getProductCountByType() {
        R<?> r = remoteIdleService.getProductCountByType();
        Object data = r.getData();
        if (data == null) {
            return Collections.emptyList();
        }
        // 用ObjectMapper安全转换
        return objectMapper.convertValue(
                data, new TypeReference<List<Map<String, Object>>>() {
                }
        );
    }

    @Override
    public Map<String, Object> getOrderData(int days) {
        if (days != 7 && days != 30) {
            days = 7;
        }
        R<?> r = remoteIdleService.getOrderDataByWeek(days);
        Object data = r.getData();
        if (data == null) {
            return Collections.emptyMap();
        }
        // 类型安全转换
        return objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    public DailyNewDataVO getDailyNewData() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        try {
            int todayNewUsers = userMapper.getTodayNewUserCount();
            int todayNewOrders = (int) idleProductOrderMapper.countCreatedToday();
            int todayNewNotes = (int) webNoteMapper.countCreatedToday();
            int todayNewProducts = (int) idleProductMapper.countCreatedToday();
            List<DailyStatistics> statistics = userMapper.getStatisticsByDateRange(
                    sevenDaysAgo, today);
            return this.buildDailyNewDataVO(todayNewUsers, todayNewOrders, todayNewNotes, todayNewProducts, statistics);
        } catch (Exception e) {
            throw new RuntimeException("获取统计数据失败", e);
        }
    }

    private DailyNewDataVO buildDailyNewDataVO(int todayNewUsers, int todayNewOrders,
                                               int todayNewNotes, int todayNewProducts,
                                               List<DailyStatistics> statistics) {
        List<String> dates = new ArrayList<>();
        List<Integer> userCounts = new ArrayList<>();
        List<Integer> noteCounts = new ArrayList<>();
        List<Integer> productCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (DailyStatistics stat : statistics) {
            dates.add(formatter.format(stat.getDate().toLocalDate()));
            userCounts.add(stat.getUserCount());
            noteCounts.add(stat.getNoteCount());
            productCounts.add(stat.getProductCount());
        }

        return DailyNewDataVO.builder()
                .todayNewUsers(todayNewUsers)
                .todayNewNotes(todayNewNotes)
                .todayNewProducts(todayNewProducts)
                .todayNewOrders(todayNewOrders)
                .dates(dates)
                .users(userCounts)
                .notes(noteCounts)
                .products(productCounts)
                .build();
    }


    @Override
    public List<Map<String, Object>> getNoteCountByCategory() {
        return noteService.getNoteCountByCategory();
    }

    @Override
    public List<Map<String, Object>> getProductCountByCategory() {
        R<?> r = remoteIdleService.getProductCountByCategory();
        Object data = r.getData();
        if (data == null) {
            return Collections.emptyList();
        }
        // 用ObjectMapper安全转换
        return objectMapper.convertValue(
                data, new TypeReference<List<Map<String, Object>>>() {
                }
        );
    }

    @Override
    public Map<String, Object> getNoteContributeCount() {
        Map<String, Object> resultMap = new HashMap<>();

        // 查询笔记贡献度
        Map<String, Object> noteMap = noteService.getNoteContributeCount();
        // 查询商品贡献度
        Object data = remoteIdleService.getProductContributeCount().getData();

        // 组装返回结果
        resultMap.put("contributeDate", noteMap.get("contributeDate"));
        resultMap.put("blogContributeCount", noteMap.get("blogContributeCount"));

        if (data == null) {
            resultMap.put("goodsContributeCount", Collections.emptyList());
        } else {
            // 用ObjectMapper安全转换
            Map<String, Object> productMap = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {
            });
            resultMap.put("goodsContributeCount", productMap.get("goodsContributeCount"));
        }
        return resultMap;
    }

    @Override
    public List<NoteSearchVo> getHotNote(String noteType) {
        return noteService.getHotNote(noteType);
    }

    @Override
    public List<UserLocationVO> getUserLocations() {
        return userService.getAllUserLocations();
    }

    @Override
    public List<LoginLocationStatVO> getWebLoginLocationStats(int days, int limit) {
        if (days < 1) {
            days = 7;
        }
        if (days > 90) {
            days = 90;
        }
        if (limit < 1) {
            limit = 8;
        }
        if (limit > 30) {
            limit = 30;
        }
        Date start = Date.from(LocalDate.now()
                .minusDays((long) days - 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        List<LoginLocationStatVO> list = webLoginInforMapper.selectWebLoginLocationStats(start, limit);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public List<LoginLocationStatVO> getMobileLoginLocationStats(int days, int limit) {
        if (days < 1) {
            days = 7;
        }
        if (days > 90) {
            days = 90;
        }
        if (limit < 1) {
            limit = 8;
        }
        if (limit > 30) {
            limit = 30;
        }
        Date start = Date.from(LocalDate.now()
                .minusDays((long) days - 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        List<LoginLocationStatVO> list = webLoginInforMapper.selectMobileLoginLocationStats(start, limit);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public List<IpLoginFrequencyVO> getIpLoginFrequencyStats(int days, int limit) {
        if (days < 1) {
            days = 1;
        }
        if (days > 90) {
            days = 90;
        }
        if (limit < 1) {
            limit = 3;
        }
        if (limit > 30) {
            limit = 30;
        }
        Date start = Date.from(LocalDate.now()
                .minusDays((long) days - 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        List<IpLoginFrequencyVO> list = webLoginInforMapper.selectIpLoginFrequencyStats(start, limit);
        return list != null ? list : Collections.emptyList();
    }
}
