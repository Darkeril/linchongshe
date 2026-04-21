package com.hongshu.idle.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hongshu.idle.domain.entity.IdleCategory;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NavbarTreeSelect树结构实体类
 *

 */
public class IdleNavbarTreeSelect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<IdleNavbarTreeSelect> children;

    public IdleNavbarTreeSelect() {

    }

    public IdleNavbarTreeSelect(IdleCategory category) {
        this.id = category.getId();
        this.label = category.getTitle();
        this.children = category.getChildren().stream().map(IdleNavbarTreeSelect::new).collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<IdleNavbarTreeSelect> getChildren() {
        return children;
    }

    public void setChildren(List<IdleNavbarTreeSelect> children) {
        this.children = children;
    }
}
