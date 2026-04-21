package com.hongshu.idle.domain.vo;

import com.hongshu.common.core.utils.TreeNode;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类
 *
 * @author: hongshu
 */
@Data
public class IdleCategoryVO extends TreeNode<IdleCategoryVO> implements Serializable {

    private String title;

    /** 分类封面图（用于市集/全部分类页展示图标） */
    private String normalCover;

    /** 热门封面图 */
    private String hotCover;
}
