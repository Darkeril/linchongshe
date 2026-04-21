package com.hongshu.web.service.web.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.web.domain.entity.WebCommentSync;
import com.hongshu.web.mapper.web.WebCommentSyncMapper;
import com.hongshu.web.service.web.IWebCommentSyncService;
import org.springframework.stereotype.Service;

/**
 * @author: hongshu
 */
@Service
public class WebCommentSyncServiceImpl extends ServiceImpl<WebCommentSyncMapper, WebCommentSync> implements IWebCommentSyncService {
}
