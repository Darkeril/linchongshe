package com.hongshu.web.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 反馈VO
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class FeedbackVO implements Serializable {

    private Long id;

    private String uid;

    private String nickname;

    private String avatar;

    private String content;

    private String type;

    private Long likeCount;

    private Boolean isLiked;

    private Integer status;

    private Date createTime;
}

