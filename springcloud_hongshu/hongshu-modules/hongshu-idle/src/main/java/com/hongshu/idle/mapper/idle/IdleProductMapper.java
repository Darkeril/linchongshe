package com.hongshu.idle.mapper.idle;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.idle.domain.entity.IdleProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: hongshu
 */
@Mapper
public interface IdleProductMapper extends BaseMapper<IdleProduct> {

    /**
     * 通过标签获取博客数量
     *
     * @return
     */
    @Select("SELECT cpid, COUNT(cpid) as count FROM  idle_product where status = 0 AND deleted = 0 GROUP BY cpid")
    List<Map<String, Object>> getProductByCategory();

    /**
     * 通过标签获取博客类型数量
     *
     * @return
     */
    @Select("SELECT product_type, COUNT(product_type) as count FROM idle_product where status = 0 AND deleted = 0 GROUP BY product_type")
    List<Map<String, Object>> getProductCountByType();

    // 查询所有分类下的商品数量（只统计未删除的商品）
    @Select("SELECT cpid, COUNT(*) AS count FROM idle_product WHERE deleted = 0 GROUP BY cpid")
    List<Map<String, Object>> countProductByCategory();

    /**
     * 获取一年内的商品贡献数
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("SELECT DISTINCT DATE_FORMAT(create_time, '%Y-%m-%d') DATE, COUNT(uid) COUNT FROM idle_product WHERE 1=1 && deleted = 0 && create_time >= #{startTime} && create_time < #{endTime} GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> getProductContributeCount(@Param("startTime") String startTime, @Param("endTime") String endTime);

}
