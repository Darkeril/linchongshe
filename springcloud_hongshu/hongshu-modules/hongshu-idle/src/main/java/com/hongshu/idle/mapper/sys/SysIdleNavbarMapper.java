package com.hongshu.idle.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.idle.domain.entity.IdleCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 导航栏表 数据层
 *

 */
@Mapper
public interface SysIdleNavbarMapper extends BaseMapper<IdleCategory> {


    @Select("select count(1) from idle_category where pid = #{id} AND deleted = 0")
    int hasChildById(Long id);

    @Select("select * from idle_category where id = #{id} AND deleted = 0")
    IdleCategory selectByCpid(String cpid);

    @Select("select * from idle_category where id = #{id} AND deleted = 0")
    IdleCategory selectByPid(String cid);
}
