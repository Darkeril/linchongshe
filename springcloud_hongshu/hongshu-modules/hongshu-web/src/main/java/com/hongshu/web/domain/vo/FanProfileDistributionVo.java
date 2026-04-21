package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据中心-粉丝画像-分布 VO（性别/年龄/地域/兴趣）
 */
@Data
public class FanProfileDistributionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 性别分布：{ name: "男性", value: 65 } */
    private List<NameValueVo> gender;
    /** 年龄分布：{ name: "18-24", value: 29 } */
    private List<NameValueVo> age;
    /** 地域分布：{ name: "北京", value: 7 } */
    private List<NameValueVo> region;
    /** 兴趣分布（标签）：{ name: "生活记录", value: 1 } */
    private List<NameValueVo> interest;

    @Data
    public static class NameValueVo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private Integer value;
    }
}
