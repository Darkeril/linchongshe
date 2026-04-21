package com.hongshu.ai.service.impl;

import com.hongshu.ai.common.Query;
import com.hongshu.ai.mapper.StatisticsMapper;
import com.hongshu.ai.service.IStatisticsService;
import com.hongshu.common.core.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计接口实现类
 *
 * @author: myj
 * @date: 2023/1/13
 * @version: 1.0.0
 */
@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;


    /**
     * 获取首页总数据
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public Map<String, Object> getTotalData(Query query) {
        return statisticsMapper.getTotalData(query);
    }

    @Override
    public Long getGptModelData() {
        return statisticsMapper.getGptModelData();
    }

    /**
     * 获取折线图数据
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public List<Map<String, Object>> getLineData(Query query) {
        return statisticsMapper.getLineData(query);
    }

    /**
     * 获取雷达图数据
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public List<List<Object>> getRaddarData(Query query) {
        List<Map<String, Object>> lists = statisticsMapper.getRaddarData(query);
        List<List<Object>> result = new ArrayList<>();
        lists.stream().forEach(v -> {
            List<Object> object = new ArrayList<>();
            String date = String.valueOf(v.get("date"));
            object.add(DateUtil.getCurrentWeek(DateUtil.parseLocalDate(date).getDayOfWeek().getValue()));
            object.add(v.get("chatCount"));
            object.add(v.get("drawCount"));
            result.add(object);
        });
        return result;
    }

    /**
     * 获取饼图数据
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public Map<String, Object> getPieData(Query query) {
        return statisticsMapper.getPieData(query);
    }

    /**
     * 获取柱状图总数据
     *
     * @param query 查询条件
     * @return
     */
    @Override
    public List<Map<String, Object>> getBarData(Query query) {
        return statisticsMapper.getBarData(query);
    }

}
