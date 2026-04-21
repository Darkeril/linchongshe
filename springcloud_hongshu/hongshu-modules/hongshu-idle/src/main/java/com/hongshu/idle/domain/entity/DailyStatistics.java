package com.hongshu.idle.domain.entity;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;


/**
 * @author: hongshu
 */
@Data
@Builder
public class DailyStatistics {

    private Date date;
    private Integer userCount;
    private Integer noteCount;
    private Integer productCount;

}
