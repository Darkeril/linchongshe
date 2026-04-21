package com.hongshu.web.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员信息 数据层
 *

 */
@Mapper
public interface SysMemberMapper extends BaseMapper<WebUser> {

}
