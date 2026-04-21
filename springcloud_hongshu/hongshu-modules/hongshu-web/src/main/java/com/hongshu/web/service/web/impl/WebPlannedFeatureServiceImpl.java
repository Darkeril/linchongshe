package com.hongshu.web.service.web.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.web.domain.entity.WebPlannedFeature;
import com.hongshu.web.domain.vo.PlannedFeatureVO;
import com.hongshu.web.mapper.web.WebPlannedFeatureMapper;
import com.hongshu.web.service.web.IWebPlannedFeatureService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 待实现功能服务实现类
 *
 * @author: hongshu
 */
@Service
public class WebPlannedFeatureServiceImpl extends ServiceImpl<WebPlannedFeatureMapper, WebPlannedFeature> implements IWebPlannedFeatureService {

    @Override
    public List<PlannedFeatureVO> getVisiblePlannedFeatures() {
        QueryWrapper<WebPlannedFeature> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_show", 1); // 只查询显示的功能
        queryWrapper.orderByAsc("sort"); // 按排序字段升序
        queryWrapper.orderByDesc("create_time"); // 相同排序时按创建时间倒序

        List<WebPlannedFeature> features = this.list(queryWrapper);

        return features.stream()
                .map(feature -> ConvertUtils.sourceToTarget(feature, PlannedFeatureVO.class))
                .collect(Collectors.toList());
    }
}

