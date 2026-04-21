package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 商品-笔记
 *
 * @author: hongshu
 */
@Data
@TableName("web_product_note_relation")
public class WebProductNoteRelation extends HongshuBaseEntity {

    /**
     * 专辑ID
     */
    private String pid;

    /**
     * 笔记ID
     */
    private String nid;

}
