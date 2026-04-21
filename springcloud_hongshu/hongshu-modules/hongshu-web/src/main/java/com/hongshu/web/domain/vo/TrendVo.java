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
public class TrendVo implements Serializable {

    private String nid;

    private String uid;

    private String username;

    private String avatar;

    private String noteCover;

    private String noteType;

    private String title;

    private String address;

    private Long time;

    private String content;

    private List<String> imgUrls;

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Boolean isLike;

    private Boolean isLoading;
}
