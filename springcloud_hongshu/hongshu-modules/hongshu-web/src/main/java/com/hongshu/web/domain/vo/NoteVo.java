package com.hongshu.web.domain.vo;

import com.hongshu.web.domain.entity.WebTag;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 笔记
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class NoteVo implements Serializable {

    private String id;

    private String title;

    private String content;

    private String noteCover;

    private String noteType;

    private String province;

    private String city;

    private String district;

    private String address;

    private String uid;

    private String username;

    private String avatar;

    private String urls;

    private String cid;

    private String cpid;

    /**
     * 笔记标签（JSON 字符串，形如 ["美食","旅行"]）
     * 主要用于 Web 端编辑时回显和提交
     */
    private String tags;

    /**
     * 所属专辑 ID（Web 端编辑专辑使用）
     */
    private String albumId;

    //图片数量
    private Integer count;

    //类型（图片或视频）
    private Integer type;

    private Long likeCount;

    private Long viewCount;

    private Long collectionCount;

    private Long commentCount;

    private List<WebTag> tagList;

    private Long time;

    private String pinned;

    //点赞关注收藏
    private Boolean isFollow;

    private Boolean isLike;

    private Boolean isCollection;

    private List<ProductVo> relatedProducts;

    private String auditStatus;

    /**
     * 位置相关字段（主要用于 Web 端编辑回显）
     */
    private Double longitude;

    private Double latitude;

    /**
     * 位置名称（如“某某商场”），可与 address 搭配使用
     */
    private String locationName;
}
