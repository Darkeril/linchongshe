package com.hongshu.web.mapper.idle;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.IdleProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author: hongshu
 */
@Mapper
public interface IdleProductOrderMapper extends BaseMapper<IdleProductOrder> {

    @Select("SELECT COUNT(DISTINCT o.id) " +
            "FROM idle_product_order o " +
            "JOIN idle_product p ON o.product_id = p.id " +
            "WHERE p.cpid = #{categoryId} " +
            "AND DATE(o.create_time) = #{date} " +
            "AND o.order_status IN ('3', '4')")
    int countOrdersByCategoryAndDate(@Param("date") String date,
                                     @Param("categoryId") String categoryId
    );

    @Select("SELECT COUNT(*) FROM idle_product_order WHERE DATE(create_time) = CURDATE()")
    long countCreatedToday();

    @Select("SELECT COUNT(*) FROM idle_product_order")
    long countTotal();

}
