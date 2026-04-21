package com.hongshu.common.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * ES-笔记
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class NoteSearchVo implements Serializable {

    private String id;

    private String title;

    private String content;

    private String noteCover;

    private Integer noteCoverHeight;

    private String cid;

    private String categoryName;

    private String cpid;

    private String categoryParentName;

    private String uid;

    private String username;

    private String avatar;

    private String urls;

    private List<String> imgUrls;

    private String tags;

    private String pinned;

    private String auditStatus;

    private String noteType;

    private String status;

    private Boolean isLike;

    private Long likeCount;

    private Long viewCount;

    private Long time;

    private Boolean isLoading;

    private String address;
}
