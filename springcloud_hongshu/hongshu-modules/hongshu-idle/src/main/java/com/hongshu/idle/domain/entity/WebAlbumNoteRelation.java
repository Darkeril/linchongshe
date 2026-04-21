package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.idle.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 专辑-笔记
 *
 * @author: hongshu
 */
@Data
@TableName("web_album_note_relation")
public class WebAlbumNoteRelation extends HongshuBaseEntity {

    /**
     * 专辑ID
     */
    private String aid;

    /**
     * 笔记ID
     */
    private String nid;

}
