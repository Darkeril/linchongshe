package com.hongshu.web.domain.vo;

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
}
