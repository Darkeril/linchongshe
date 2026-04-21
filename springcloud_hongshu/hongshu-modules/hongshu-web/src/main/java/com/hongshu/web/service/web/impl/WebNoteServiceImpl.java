package com.hongshu.web.service.web.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.enums.TerminalTypeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.redis.service.RedisService;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.RemoteSystemService;
import com.hongshu.web.async.NoteAsyncService;
import com.hongshu.web.domain.dto.NoteAppDTO;
import com.hongshu.web.domain.dto.NoteDTO;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.NoteVo;
import com.hongshu.web.domain.vo.ProductVo;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.web.*;
import com.hongshu.web.domain.entity.WebAlbum;
import com.hongshu.web.domain.entity.WebAlbumNoteRelation;
import com.hongshu.web.rocketmq.ViewCountServiceV2;
import com.hongshu.web.rocketmq.StatisticsServiceV2;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.service.web.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: hongshu
 */
@Slf4j
@Service
public class WebNoteServiceImpl extends ServiceImpl<WebNoteMapper, WebNote> implements IWebNoteService {

    @Autowired
    private IWebUserService userService;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private IWebTagNoteRelationService tagNoteRelationService;
    @Autowired
    private IWebTagService tagService;
    @Autowired
    private IWebCategoryService categoryService;
    @Autowired
    private IWebEsNoteService esNoteService;
    @Autowired
    private IWebFollowerService followerService;
    @Autowired
    private IWebLikeOrCollectionService likeOrCollectionService;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;
    @Autowired
    private IWebCommentService commentService;
    @Autowired
    private IWebCommentSyncService commentSyncService;
    @Autowired
    private ViewCountServiceV2 viewCountServiceV2;
    @Autowired
    private IWebAlbumNoteRelationService albumNoteRelationService;
    @Autowired
    private WebAlbumMapper albumMapper;
    @Autowired
    private WebAlbumNoteRelationMapper albumNoteRelationMapper;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private RemoteSystemService remoteSystemService;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private WebProductNoteRelationMapper noteRelationMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ISysSystemConfigService systemConfigService;
    @Autowired
    private NoteAsyncService noteAsyncService;
    @Autowired
    private WebAuditLogMapper auditLogMapper;
    @Autowired
    private CommonAuditService commonAuditService;

    @Autowired
    private StatisticsServiceV2 statisticsServiceV2;

    @NotNull
    private StringBuilder getTags(WebNote note, NoteDTO noteDTO) {
        List<String> tagList = noteDTO.getTags();
        List<WebTagNoteRelation> tagNoteRelationList = new ArrayList<>();
        List<WebTag> tagList1 = tagService.list();
        Map<String, WebTag> tagMap = tagList1.stream().collect(Collectors.toMap(WebTag::getTitle, tag -> tag));
        StringBuilder tags = new StringBuilder();
        if (!tagList.isEmpty()) {
            for (String tag : tagList) {
                WebTagNoteRelation tagNoteRelation = new WebTagNoteRelation();
                if (tagMap.containsKey(tag)) {
                    WebTag tagModel = tagMap.get(tag);
                    tagNoteRelation.setTid(String.valueOf(tagModel.getId()));
                } else {
                    WebTag model = new WebTag();
                    model.setTitle(tag);
                    model.setLikeCount(1L);
                    tagService.save(model);
                    tagNoteRelation.setTid(String.valueOf(model.getId()));
                }
                tagNoteRelation.setNid(String.valueOf(note.getId()));
                tagNoteRelationList.add(tagNoteRelation);
                tags.append(tag);
            }
            tagNoteRelationService.saveBatch(tagNoteRelationList);
        }
        return tags;
    }

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    @Override
    public NoteVo getNoteById(String noteId) {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        qw.lambda().eq(WebNote::getId, noteId).eq(WebNote::getDeleted, 0);
        WebNote note = this.getOne(qw);
        if (note == null) {
            log.warn("查询笔记不存在或已删除，笔记ID: {}", noteId);
            throw new HongshuException(ResultCodeEnum.FAIL);
        }

        // 优化：使用Redis缓存 + RocketMQ异步更新浏览数
        Long viewCount = viewCountServiceV2.recordNoteView(noteId);
        note.setViewCount(viewCount);

        WebUser user = userService.getById(note.getUid());
        NoteVo noteVo = ConvertUtils.sourceToTarget(note, NoteVo.class);

        if (user != null) {
            noteVo.setUsername(user.getUsername())
                    .setAvatar(user.getAvatar());
        }
        // 设置时间，兼容null
        if (note.getUpdateTime() != null) {
            noteVo.setTime(note.getUpdateTime().getTime());
        } else if (note.getCreateTime() != null) {
            noteVo.setTime(note.getCreateTime().getTime());
        } else {
            noteVo.setTime(System.currentTimeMillis());
        }
        // 处理关注状态
        boolean follow = user != null && followerService.isFollow(String.valueOf(user.getId()));
        noteVo.setIsFollow(follow);
        // 处理点赞收藏状态
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        List<WebLikeOrCollection> likeOrCollectionList = likeOrCollectionService.list(
                new QueryWrapper<WebLikeOrCollection>()
                        .eq("like_or_collection_id", noteId)
                        .eq("uid", currentUid)
        );
        Set<Integer> types = likeOrCollectionList.stream()
                .map(WebLikeOrCollection::getType)
                .collect(Collectors.toSet());
        noteVo.setIsLike(types.contains(1));
        noteVo.setIsCollection(types.contains(3));
        // 处理标签
        List<WebTagNoteRelation> tagNoteRelationList = tagNoteRelationService.list(
                new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId)
        );
        List<String> tids = tagNoteRelationList.stream()
                .map(WebTagNoteRelation::getTid)
                .collect(Collectors.toList());
        if (!tids.isEmpty()) {
            List<WebTag> tagList = tagService.listByIds(tids);
            noteVo.setTagList(tagList);
        }
        // 处理关联商品信息
        if (StringUtils.isNotBlank(note.getProductId())) {
            List<String> productIds = Arrays.asList(note.getProductId().split(","));
            List<IdleProduct> products = productMapper.selectBatchIds(productIds);
            List<ProductVo> productVos = products.stream()
                    .map(product -> {
                        ProductVo vo = new ProductVo();
                        vo.setId(product.getId());
                        vo.setTitle(product.getTitle());
                        vo.setCover(product.getCover());
                        vo.setPrice(product.getPrice());
                        vo.setOriginalPrice(product.getOriginalPrice());
                        // 设置销量信息
//                        vo.setSoldCount(product.getSoldCount());
                        return vo;
                    })
                    .collect(Collectors.toList());
            noteVo.setRelatedProducts(productVos);
        }
//        this.updateById(note);
        return noteVo;
    }

    /**
     * 新增笔记
     *
     * @param terminal  来源标识
     * @param requestId 唯一标识
     * @param noteData  笔记对象
     * @param coverFile 封面图
     * @param files     图片文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNoteByDTO(String terminal, String requestId, String noteData, MultipartFile coverFile, MultipartFile[] files) {
        // 幂等性校验，防止重复提交
        String redisKey = "note:save:" + requestId;
        if (Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            throw new RuntimeException("请勿重复提交");
        }
        // 设置标记，5分钟后自动过期
        redisService.setCacheObject(redisKey, "1", 5L, TimeUnit.MINUTES);

        // 保存封面到本地临时目录
        String coverLocalPath = this.saveToLocalTemp(coverFile);
        // 保存内容文件到本地临时目录
        List<String> fileLocalPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            fileLocalPaths.add(this.saveToLocalTemp(file));
        }

        // 调用异步处理
        String currentUid = AuthContextHolder.getUserId();
        noteAsyncService.addNote(terminal, currentUid, noteData, coverLocalPath, fileLocalPaths);
    }

    @Override
    @Transactional
    public void saveNote(String terminal, String requestId, NoteAppDTO noteDTO) {
        if ("1".equals(noteDTO.getNoteType())) {
            // 图片类型
            if (noteDTO.getUrls() == null) {
                throw new RuntimeException("未上传任何图片");
            }
        } else if ("2".equals(noteDTO.getNoteType())) {
            // 视频类型
            if (noteDTO.getUrls() == null) {
                throw new RuntimeException("请上传一个视频文件");
            }
        } else if ("3".equals(noteDTO.getNoteType())) {
            // Live图类型
            if (noteDTO.getUrls() == null || noteDTO.getUrls().isEmpty()) {
                throw new RuntimeException("请上传 Live 动图文件");
            }
            log.info("发布 Live 图笔记: urls={}", noteDTO.getUrls());
        }
        // 幂等性校验，防止重复提交
        String redisKey = "note:save:" + requestId;
        if (Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            throw new RuntimeException("请勿重复提交");
        }
        // 设置标记，5分钟后自动过期
        redisService.setCacheObject(redisKey, "1", 5L, TimeUnit.MINUTES);

        String currentUid = AuthContextHolder.getUserId();
        try {
            // 更新用户笔记数量
            WebUser user = userMapper.selectById(currentUid);
            user.setNoteCount(user.getNoteCount() + 1);
            user.setUpdateTime(new Date());
            userMapper.updateById(user);

            // 保存笔记
            WebNote note = ConvertUtils.sourceToTarget(noteDTO, WebNote.class);
            note.setUid(currentUid);
            note.setAuthor(user.getUsername());
            String tags = JSONUtil.toJsonStr(noteDTO.getTags().toArray(new String[0]));
            note.setTags(tags);
            String urls = JSONUtil.toJsonStr(noteDTO.getUrls().toArray());
            note.setUrls(urls);
            note.setFromType(TerminalTypeEnum.fromValue(terminal).getCode());

            // 获取审核配置
            BaiduQianfanConfigDTO auditConfig = systemConfigService.getBaiduQianfanConfig();
            String auditMode = auditConfig.getAuditMode(); // manual/auto/hybrid

            // 设置通用字段
            note.setCreator(user.getUsername());
            note.setTime(System.currentTimeMillis());
            note.setCreateTime(new Date());
            note.setUpdateTime(new Date());

            // 处理关联商品
            if (noteDTO.getRelatedProducts() != null && !noteDTO.getRelatedProducts().isEmpty()) {
                // 从关联商品中提取ID并拼接
                String productIds = noteDTO.getRelatedProducts().stream()
                        .map(product -> product.getId())
                        .collect(Collectors.joining(","));
                note.setProductId(productIds);
            }

            if ("manual".equals(auditMode)) {
                // 纯人工审核模式
                Boolean auditEnabled = systemConfigService.getSystemConfig().getContentAuditEnabled();
                if (auditEnabled) {
                    note.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
                } else {
                    note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                }
                // 保存笔记
                noteMapper.insert(note);
            } else if ("auto".equals(auditMode) || "hybrid".equals(auditMode)) {
                // 自动审核或混合模式 - 先保存笔记为"审核中"状态，然后异步审核
                Boolean auditEnabled = systemConfigService.getSystemConfig().getContentAuditEnabled();
                if (auditEnabled) {
                    note.setAuditStatus(AuditStatusEnum.REVIEW.getCode()); // 先设为审核中
                } else {
                    note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                }
                
                // 保存笔记
                noteMapper.insert(note);
                
                // 异步执行审核（不阻塞发布流程）
                String noteId = note.getId();
                if (noteId != null && auditEnabled) {
                    List<String> dataList = new ArrayList<>();
                    if (noteDTO.getUrls() != null && !noteDTO.getUrls().isEmpty()) {
                        dataList.addAll(noteDTO.getUrls());
                    }
                    // 异步审核
                    asyncAuditNote(noteId, noteDTO, dataList, auditMode);
                }
            } else {
                // 默认为人工审核
                note.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
                // 保存笔记
                noteMapper.insert(note);
            }

            // 统计：发布笔记后，及时刷新工作台/首页统计缓存
            try {
                statisticsServiceV2.notifyStatisticsUpdate("note", "create", note.getId());
            } catch (Exception e) {
                log.warn("刷新统计缓存失败（不影响发布流程）: {}", e.getMessage());
            }

            // 如果有关联商品，保存笔记-商品关联关系
            if (noteDTO.getRelatedProducts() != null && !noteDTO.getRelatedProducts().isEmpty()) {
                List<WebProductNoteRelation> noteProducts = noteDTO.getRelatedProducts().stream().map(product -> {
                    WebProductNoteRelation noteProduct = new WebProductNoteRelation();
                    noteProduct.setNid(note.getId());
                    noteProduct.setPid(product.getId());
                    noteProduct.setCreateTime(new Date());
                    noteProduct.setUpdateTime(new Date());
                    return noteProduct;
                }).collect(Collectors.toList());
                noteRelationMapper.insertBatchSql(noteProducts);
            }

            // 如果选择了专辑，创建专辑-笔记关联并更新专辑笔记数量
            if (noteDTO.getAlbumId() != null && !noteDTO.getAlbumId().isEmpty()) {
                // 创建专辑-笔记关联
                WebAlbumNoteRelation albumNoteRelation = new WebAlbumNoteRelation();
                albumNoteRelation.setAid(noteDTO.getAlbumId());
                albumNoteRelation.setNid(note.getId());
                albumNoteRelation.setCreateTime(new Date());
                albumNoteRelation.setUpdateTime(new Date());
                albumNoteRelationMapper.insert(albumNoteRelation);

                // 更新专辑的笔记数量
                WebAlbum album = albumMapper.selectById(noteDTO.getAlbumId());
                if (album != null) {
                    album.setImgCount((album.getImgCount() == null ? 0L : album.getImgCount()) + 1);
                    album.setUpdateTime(new Date());
                    albumMapper.updateById(album);
                }
            }
        } catch (Exception e) {
            log.error("笔记保存失败", e);
            throw new RuntimeException("笔记保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存文件到本地临时目录
     * 允许传入 null 或空文件，返回 null 表示无需处理
     */
    private String saveToLocalTemp(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String tempDir = System.getProperty("java.io.tmpdir");
        String originalName = file.getOriginalFilename();
        String safeName = originalName != null ? originalName : "unknown";
        String fileName = UUID.randomUUID() + "_" + safeName;
        File localFile = new File(tempDir, fileName);
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
        return localFile.getAbsolutePath();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNoteByDTO(String noteData, MultipartFile coverFile, MultipartFile[] files) {
        String currentUid = AuthContextHolder.getUserId();
        // 解析前端传来的数据
        NoteDTO noteDTO = JSONUtil.toBean(noteData, NoteDTO.class);
        // 查询原有笔记
        WebNote note = noteMapper.selectById(noteDTO.getId());
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!note.getUid().equals(currentUid)) {
            throw new RuntimeException("无权编辑该笔记");
        }

        // 保存封面到本地临时目录（允许不上传新封面）
        String coverLocalPath = this.saveToLocalTemp(coverFile);
        // 保存内容文件到本地临时目录（允许不上传新图片/视频）
        List<String> fileLocalPaths = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String path = this.saveToLocalTemp(file);
                if (path != null) {
                    fileLocalPaths.add(path);
                }
            }
        }
        // 调用异步处理
        noteAsyncService.updateNote(noteDTO, note, coverLocalPath, fileLocalPaths);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoteByIds(List<String> noteIds) {
        // 只查询未删除的笔记
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        qw.in("id", noteIds).eq("deleted", 0);
        List<WebNote> noteList = this.list(qw);
        // TODO 这里需要优化，数据一致性问题
        noteList.forEach(item -> {
            // 逻辑删除：更新 deleted 字段为 1
            item.setDeleted(1);
            item.setUpdateTime(new Date());
            this.updateById(item);

            String noteId = item.getId();
            // 同步删除ES中的数据
            try {
                esNoteService.deleteNote(noteId);
            } catch (Exception e) {
                log.error("删除ES数据失败，笔记ID: {}", noteId, e);
                // ES删除失败不影响数据库删除，只记录日志
            }

            String urls = item.getUrls();
            JSONArray objects = JSONUtil.parseArray(urls);
            Object[] array = objects.toArray();
            List<String> pathArr = new ArrayList<>();
            for (Object o : array) {
                pathArr.add((String) o);
            }
//            ossService.batchDelete(pathArr);
            // TODO 可以使用多线程优化，
            // 删除点赞图片，评论，标签关系，收藏关系
            likeOrCollectionService.remove(new QueryWrapper<WebLikeOrCollection>().eq("like_or_collection_id", noteId));
            List<WebComment> commentList = commentService.list(new QueryWrapper<WebComment>().eq("nid", noteId));
            List<WebCommentSync> commentSyncList = commentSyncService.list(new QueryWrapper<WebCommentSync>().eq("nid", noteId));
            List<String> cids = commentList.stream().map(WebComment::getId).collect(Collectors.toList());
            List<String> cids2 = commentSyncList.stream().map(WebCommentSync::getId).collect(Collectors.toList());
            if (!cids.isEmpty()) {
                likeOrCollectionService.remove(new QueryWrapper<WebLikeOrCollection>().in("like_or_collection_id", cids).eq("type", 2));
            }
            commentService.removeBatchByIds(cids);
            commentSyncService.removeBatchByIds(cids2);
            tagNoteRelationService.remove(new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId));
            albumNoteRelationService.remove(new QueryWrapper<WebAlbumNoteRelation>().eq("nid", noteId));
        });
        // 逻辑删除：批量更新 deleted 字段为 1
        for (WebNote note : noteList) {
            note.setDeleted(1);
            note.setUpdateTime(new Date());
            this.updateById(note);
        }

        // 统计：删除笔记后，及时刷新工作台/首页统计缓存（批量删除按一次处理即可）
        try {
            statisticsServiceV2.notifyStatisticsUpdate("note", "delete");
        } catch (Exception e) {
            log.warn("刷新统计缓存失败（不影响删除流程）: {}", e.getMessage());
        }
    }

    @Override
    public Page<NoteVo> getHotPage(long currentPage, long pageSize) {
        return null;
    }

    @Override
    public boolean pinnedNote(String noteId) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        qw.lambda().eq(WebNote::getId, noteId).eq(WebNote::getDeleted, 0);
        WebNote note = this.getOne(qw);
        if (note == null) {
            throw new HongshuException("笔记不存在或已删除");
        }
        if ("1".equals(note.getPinned())) {
            note.setPinned("0");
        } else {
            QueryWrapper<WebNote> listQw = new QueryWrapper<>();
            listQw.eq("uid", currentUid).eq("deleted", 0);
            List<WebNote> noteList = this.list(listQw);
            long count = noteList.stream().filter(item -> "1".equals(item.getPinned())).count();
            if (count >= 3) {
                throw new HongshuException("最多只能置顶3个笔记");
            }
            note.setPinned("1");
            note.setUpdateTime(new Date());
        }
        return this.updateById(note);
    }

    @Override
    public Object getNoteCount(String userId) {
        // 只统计未删除的笔记
        int allCount = Math.toIntExact(noteMapper.selectCount(
                new QueryWrapper<WebNote>()
                        .eq("uid", userId)
                        .eq("deleted", 0)));
        int pendingCount = Math.toIntExact(noteMapper.selectCount(
                new QueryWrapper<WebNote>()
                        .eq("uid", userId)
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.REVIEW.getCode())));
        int approvedCount = Math.toIntExact(noteMapper.selectCount(
                new QueryWrapper<WebNote>()
                        .eq("uid", userId)
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())));
        int rejectedCount = Math.toIntExact(noteMapper.selectCount(
                new QueryWrapper<WebNote>()
                        .eq("uid", userId)
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.REJECT.getCode())));
        // 统计「该用户笔记」被点赞次数（type=1），用子查询避免拉取全部 note id 到内存
        int likeCount = Math.toIntExact(likeOrCollectionMapper.selectCount(
                new QueryWrapper<WebLikeOrCollection>()
                        .eq("type", 1)
                        .apply("like_or_collection_id IN (SELECT id FROM web_note WHERE uid = {0} AND deleted = 0)", userId)));

        Map<String, Object> result = new HashMap<>(3);
        result.put("allCount", allCount);
        result.put("pendingCount", pendingCount);
        result.put("approvedCount", approvedCount);
        result.put("rejectedCount", rejectedCount);
        result.put("likeCount", likeCount);
        return result;
    }

    /**
     * 异步执行审核（不阻塞发布流程）
     * @param noteId 笔记ID
     * @param noteDTO 笔记DTO（用于获取笔记类型和内容）
     * @param dataList 图片/视频URL列表
     * @param auditMode 审核模式字符串（manual/auto/hybrid）
     */
    private void asyncAuditNote(String noteId, NoteAppDTO noteDTO, List<String> dataList, String auditMode) {
        noteAsyncService.auditNoteAsync(noteId, noteDTO, dataList, auditMode);
    }

    /**
     * 执行审核并设置状态（已废弃，改为异步审核）
     * 参考 NoteAsyncService.performAuditAndSetStatus 方法
     * @param note 笔记对象
     * @param noteDTO 笔记DTO（用于获取笔记类型和内容）
     * @param dataList 图片/视频URL列表
     * @param auditMode 审核模式字符串（manual/auto/hybrid）
     * @return 审核结果
     */
    @Deprecated
    private AutoAuditResult performAuditAndSetStatus(WebNote note, NoteAppDTO noteDTO, List<String> dataList, String auditMode) {
        try {
            // 检查内容审核是否开启
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (!Boolean.TRUE.equals(auditEnabled)) {
                log.info("内容审核未开启，笔记直接通过，笔记ID: {}", note.getId());
                note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                return null;
            }
            
            // 获取审核模式枚举
            AuditModeEnum auditModeEnum = commonAuditService.getAuditMode();
            log.info("开始执行笔记审核 - 模式: {}, 笔记ID: {}", auditModeEnum.getDescription(), note.getId());
            
            // 使用通用审核服务执行审核
            String contentType = (noteDTO.getNoteType() != null && "2".equals(noteDTO.getNoteType())) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                noteDTO.getContent(), dataList, contentType, auditModeEnum);
            
            // 设置审核状态
            commonAuditService.setAuditStatus(note, auditResult, auditModeEnum);

            log.info("笔记审核完成 - 模式: {}, 结果: {}, 原因: {}, 笔记ID: {}", 
                    auditModeEnum.getDescription(), auditResult.getResult(), auditResult.getReason(), note.getId());

            return auditResult;

        } catch (Exception e) {
            log.error("笔记审核异常，笔记ID: {}", note.getId(), e);
            // 审核异常时，设置为待人工审核
            note.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
            return null;
        }
    }

    /**
     * 保存审核日志
     */
    private void saveAuditLog(String contentId, AutoAuditResult auditResult, String contentType) {
        try {
            WebAuditLog auditLog = WebAuditLog.builder()
                    .contentId(contentId)
                    .contentType(contentType)
                    .auditResult(auditResult.getResult().name())
                    .auditScore(auditResult.getScore() != null ? BigDecimal.valueOf(auditResult.getScore()) : null)
                    .auditReason(auditResult.getReason())
                    .auditProvider(auditResult.getProvider())
                    .auditTime(auditResult.getAuditTime())
                    .createTime(new Date())
                    .build();

            auditLogMapper.insert(auditLog);
            log.info("审核日志保存成功 - 内容ID: {}, 内容类型: {}, 审核结果: {}, 审核分数: {}",
                    contentId, contentType, auditResult.getResult(), auditResult.getScore());
        } catch (Exception e) {
            log.error("审核日志保存失败 - 内容ID: {}, 内容类型: {}", contentId, contentType, e);
        }
    }
}
