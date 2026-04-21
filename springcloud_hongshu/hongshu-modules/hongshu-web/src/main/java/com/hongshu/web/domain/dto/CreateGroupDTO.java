package com.hongshu.web.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建群聊DTO
 *
 * @author: hongshu
 */
@Data
public class CreateGroupDTO implements Serializable {
    private String groupName;
    private String groupAvatar;
    private String groupDescription;
    private List<String> memberIds;
}


