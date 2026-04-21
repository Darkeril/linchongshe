package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.web.domain.entity.WebPlannedFeature;
import com.hongshu.web.domain.vo.PlannedFeatureVO;

import java.util.List;

/**
 * 待实现功能服务接口
 *
 * @author: hongshu
 */
public interface IWebPlannedFeatureService extends IService<WebPlannedFeature> {

    /**
     * 获取显示的待实现功能列表
     *
     * @return 功能列表
     */
    List<PlannedFeatureVO> getVisiblePlannedFeatures();
}

