package com.hongshu.web.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 用户
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class TrendUserVO implements Serializable {

    private String uid;

    private String username;

    private String avatar;

    private Boolean tag; // 最近三天是否有新内容

    private Boolean hasContent; // 是否有内容

}
