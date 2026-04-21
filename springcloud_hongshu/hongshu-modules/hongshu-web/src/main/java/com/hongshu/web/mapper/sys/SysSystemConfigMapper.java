package com.hongshu.web.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebSystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 *  数据层
 *

 */
@Mapper
public interface SysSystemConfigMapper extends BaseMapper<WebSystemConfig> {

    /**
     * 从 gpt_openkey 表查询通义千问的 API Key
     * @return API Key，如果没有找到则返回 null
     */
    @Select("SELECT app_key FROM gpt_openkey WHERE model = 'QIANWEN' AND status = 1 AND deleted = 0 LIMIT 1")
    String selectTongYiApiKeyFromOpenkey();
}
