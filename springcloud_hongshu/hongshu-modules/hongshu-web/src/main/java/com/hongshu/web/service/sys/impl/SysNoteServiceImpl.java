package com.hongshu.web.service.sys.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.core.constant.Constantss;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.NoteTypeEnum;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.global.SysConf;
import com.hongshu.common.core.utils.*;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.dto.DemoAccountConfigDTO;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.NoteDataCenterDetailVo;
import com.hongshu.web.domain.vo.NoteDataCenterVo;
import com.hongshu.web.mapper.sys.SysAlbumMapper;
import com.hongshu.web.mapper.sys.SysNavbarMapper;
import com.hongshu.web.mapper.sys.SysNoteMapper;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebNoteBehaviorMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.ISysNoteService;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.service.web.IWebEsNoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 笔记信息 服务层处理
 *
 */
@Slf4j
@Service
public class SysNoteServiceImpl implements ISysNoteService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IWebEsNoteService esNoteService;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private SysNoteMapper noteMapper;
    @Autowired
    private WebNoteBehaviorMapper webNoteBehaviorMapper;
    @Autowired
    private WebFollowerMapper webFollowerMapper;
    @Autowired
    private SysNavbarMapper navbarMapper;
    @Autowired
    private SysAlbumMapper albumMapper;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private CommonAuditService commonAuditService;
    @Autowired
    private ISysSystemConfigService systemConfigService;


//    @Value("${oss.type}")
//    Integer type;


    /**
     * 查询笔记信息集合
     */
    @Override
    public List<WebNote> getAllNoteList() {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        // 审核通过的笔记，且未删除
        qw.lambda().like(WebNote::getAuditStatus, 1).eq(WebNote::getDeleted, 0);
        return noteMapper.selectList(qw);
    }

    /**
     * 查询笔记信息集合
     *
     * @param query 笔记信息
     */
    @Override
    public List<WebNote> selectNoteList(Query query) {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        // 只查询未删除的笔记
        qw.lambda().eq(WebNote::getDeleted, 0);
        qw.lambda().like(ValidatorUtil.isNotNull(query.getTitle()), WebNote::getTitle, query.getTitle());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getUid()), WebNote::getUid, query.getUid());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getNoteType()), WebNote::getNoteType, query.getNoteType());
        qw.lambda().eq(ValidatorUtil.isNotNull(query.get("auditStatus")), WebNote::getAuditStatus, query.get("auditStatus"));
        qw.lambda().eq(ValidatorUtil.isNotNull(query.get("pid")), WebNote::getCpid, query.get("pid"));
        qw.lambda().eq(ValidatorUtil.isNotNull(query.get("uid")), WebNote::getUid, query.get("uid"));
        // 支持按作者名称筛选
        Optional.ofNullable(query.get("author")).ifPresent(author -> qw.lambda().like(WebNote::getAuthor, author));
        // 支持按笔记来源筛选
        Optional.ofNullable(query.get("fromType")).ifPresent(fromType -> qw.lambda().eq(WebNote::getFromType, fromType));
        // 处理 beginTime 和 endTime
        String beginTimeStr = (String) query.get("params[beginTime]");
        String endTimeStr = (String) query.get("params[endTime]");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginTimeStr != null) {
            try {
                // 将字符串转为 Date
                Date beginTime = sdf.parse(beginTimeStr);
                // 添加大于等于条件
                qw.lambda().ge(WebNote::getCreateTime, beginTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (endTimeStr != null) {
            try {
                Date endTime = sdf.parse(endTimeStr);
                // 确保 endTime 是当天的结束时间（23:59:59）
                endTime = new Date(endTime.getTime() + 1000 * 60 * 60 * 24 - 1);
                // 添加小于等于条件
                qw.lambda().le(WebNote::getCreateTime, endTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 排序：支持按创建时间和更新时间排序
        String orderByColumn = (String) query.get("orderByColumn");
        String isAsc = (String) query.get("isAsc");
        if (orderByColumn != null && !orderByColumn.isEmpty()) {
            if ("createTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebNote::getCreateTime);
                } else {
                    qw.lambda().orderByDesc(WebNote::getCreateTime);
                }
            } else if ("updateTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebNote::getUpdateTime);
                } else {
                    qw.lambda().orderByDesc(WebNote::getUpdateTime);
                }
            } else {
                // 默认按更新时间降序
                qw.lambda().orderByDesc(WebNote::getUpdateTime);
            }
        } else {
            // 默认按更新时间降序
            qw.lambda().orderByDesc(WebNote::getUpdateTime);
        }
        List<WebNote> noteList = noteMapper.selectList(qw);

        // 批量查询专辑信息并设置专辑名称
        fillAlbumTitles(noteList);

        return noteList;
    }

    /**
     * 获取未审核笔记列表
     */
    @Override
    public List<WebNote> selectUnAuditNoteList(Query query) {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        // 只查询未删除的笔记
        qw.lambda().eq(WebNote::getDeleted, 0);
        qw.lambda().like(ValidatorUtil.isNotNull(query.getTitle()), WebNote::getTitle, query.getTitle());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getUid()), WebNote::getUid, query.getUid());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getNoteType()), WebNote::getNoteType, query.getNoteType());
        // 支持按审核状态筛选，如果未指定则默认查询未审核的（auditStatus = 0）
        if (ValidatorUtil.isNotNull(query.get("auditStatus"))) {
            qw.lambda().eq(WebNote::getAuditStatus, query.get("auditStatus"));
        } else {
            qw.lambda().eq(WebNote::getAuditStatus, 0);
        }
        // 支持按分类筛选（pid 对应 cpid）
        qw.lambda().eq(ValidatorUtil.isNotNull(query.get("pid")), WebNote::getCpid, query.get("pid"));
        // 支持按作者名称筛选
        Optional.ofNullable(query.get("author")).ifPresent(author -> qw.lambda().like(WebNote::getAuthor, author));
        // 支持按笔记来源筛选
        Optional.ofNullable(query.get("fromType")).ifPresent(fromType -> qw.lambda().eq(WebNote::getFromType, fromType));
        // 处理 beginTime 和 endTime
        String beginTimeStr = (String) query.get("params[beginTime]");
        String endTimeStr = (String) query.get("params[endTime]");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginTimeStr != null) {
            try {
                // 将字符串转为 Date
                Date beginTime = sdf.parse(beginTimeStr);
                // 添加大于等于条件
                qw.lambda().ge(WebNote::getCreateTime, beginTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (endTimeStr != null) {
            try {
                Date endTime = sdf.parse(endTimeStr);
                // 确保 endTime 是当天的结束时间（23:59:59）
                endTime = new Date(endTime.getTime() + 1000 * 60 * 60 * 24 - 1);
                // 添加小于等于条件
                qw.lambda().le(WebNote::getCreateTime, endTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 排序：支持按创建时间和更新时间排序
        String orderByColumn = (String) query.get("orderByColumn");
        String isAsc = (String) query.get("isAsc");
        if (orderByColumn != null && !orderByColumn.isEmpty()) {
            if ("createTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebNote::getCreateTime);
                } else {
                    qw.lambda().orderByDesc(WebNote::getCreateTime);
                }
            } else if ("updateTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(WebNote::getUpdateTime);
                } else {
                    qw.lambda().orderByDesc(WebNote::getUpdateTime);
                }
            } else {
                // 默认按更新时间降序
                qw.lambda().orderByDesc(WebNote::getUpdateTime);
            }
        } else {
            // 默认按更新时间降序
            qw.lambda().orderByDesc(WebNote::getUpdateTime);
        }
        List<WebNote> noteList = noteMapper.selectList(qw);

        // 批量查询专辑信息并设置专辑名称
        fillAlbumTitles(noteList);

        return noteList;
    }

    /**
     * 批量填充笔记的专辑名称
     *
     * @param noteList 笔记列表
     */
    private void fillAlbumTitles(List<WebNote> noteList) {
        if (CollectionUtil.isEmpty(noteList)) {
            return;
        }

        // 收集所有非空的专辑ID
        Set<String> albumIds = new HashSet<>();
        for (WebNote note : noteList) {
            if (note.getAlbumId() != null && !note.getAlbumId().isEmpty()) {
                albumIds.add(note.getAlbumId());
            }
        }

        // 批量查询专辑
        if (!albumIds.isEmpty()) {
            List<WebAlbum> albums = albumMapper.selectBatchIds(new ArrayList<>(albumIds));
            Map<String, String> albumMap = new HashMap<>();
            for (WebAlbum album : albums) {
                if (album.getId() != null && album.getTitle() != null) {
                    albumMap.put(String.valueOf(album.getId()), album.getTitle());
                }
            }

            // 设置专辑名称
            for (WebNote note : noteList) {
                if (note.getAlbumId() != null && !note.getAlbumId().isEmpty()) {
                    String albumTitle = albumMap.get(note.getAlbumId());
                    if (albumTitle != null) {
                        note.setAlbumTitle(albumTitle);
                    }
                }
            }
        }
    }

    /**
     * 通过笔记ID查询笔记信息
     *
     * @param id 笔记ID
     */
    @Override
    public WebNote selectNoteById(Long id) {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        qw.lambda().eq(WebNote::getId, id).eq(WebNote::getDeleted, 0);
        return noteMapper.selectOne(qw);
    }

    /**
     * 新增笔记信息
     *
     * @param note 笔记信息
     */
    @Override
    public int insertNote(WebNote note, MultipartFile file) {
        // 上传头像
        if (ObjectUtils.isNotEmpty(file)) {
//            String noteCover = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String noteCover = upload.getData().getUrl();
            note.setNoteCover(noteCover);
        }
        note.setCreator("System");
        note.setCreateTime(new Date());
        note.setUpdateTime(new Date());
        // 设置删除标识为0（正常状态）
        if (note.getDeleted() == null) {
            note.setDeleted(0);
        }
        int result = noteMapper.insert(note);

        // 如果审核状态为"已通过"（'1'），同步到ES
        if (result > 0 && AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())) {
            try {
                log.info("开始同步笔记到ES（按库表最新数据），笔记ID: {}, 笔记类型: {}",
                        note.getId(), note.getNoteType());
                esNoteService.upsertNoteFromDbById(note.getId());
                log.info("笔记新增成功并同步到ES，笔记ID: {}", note.getId());
            } catch (Exception e) {
                log.error("笔记同步到ES失败，笔记ID: {}, 笔记类型: {}", note.getId(), note.getNoteType(), e);
                // ES同步失败不影响主流程，只记录日志
            }
        } else {
            log.debug("笔记未同步到ES，笔记ID: {}, 审核状态: {}, 插入结果: {}",
                    note.getId(), note.getAuditStatus(), result);
        }

        return result;
    }

    /**
     * 修改保存笔记信息
     *
     * @param note 笔记信息
     */
    @Override
    public int updateNote(WebNote note, MultipartFile file) {
        // 上传头像
        if (ObjectUtils.isNotEmpty(file)) {
//            String noteCover = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String noteCover = upload.getData().getUrl();
            note.setNoteCover(noteCover);
        }

        // 查询原笔记的审核状态
        WebNote oldNote = noteMapper.selectById(note.getId());
        boolean needReaudit = false;
        if (oldNote != null) {
            String oldAuditStatus = oldNote.getAuditStatus();
            // 如果原笔记是未审核（0）或已驳回（2）状态，修改后需要重新审核
            if ("0".equals(oldAuditStatus) || "2".equals(oldAuditStatus)) {
                needReaudit = true;
                note.setAuditStatus("0"); // 重新设置为未审核状态，等待审核
                log.info("笔记ID:{} 原状态:{} 修改后重新提交审核", note.getId(), oldAuditStatus);
            }
        }

        note.setUpdater("System");
        note.setUpdateTime(new Date());

        // 如果需要重新审核，执行自动审核流程（参考用户端笔记更新逻辑）
        AutoAuditResult auditResult = null;
        if (needReaudit) {
            try {
                // 解析图片/视频URL列表
                List<String> dataList = new ArrayList<>();
                if (ObjectUtils.isNotEmpty(note.getUrls())) {
                    try {
                        List<String> urlList = JSONUtil.toList(note.getUrls(), String.class);
                        if (urlList != null && !urlList.isEmpty()) {
                            dataList.addAll(urlList);
                        }
                    } catch (Exception e) {
                        log.warn("解析笔记图片 URL 失败: {}", e.getMessage());
                    }
                }

                // 执行自动审核
                auditResult = performAuditAndSetStatus(note, dataList);
            } catch (Exception e) {
                log.error("笔记自动审核异常，笔记ID: {}", note.getId(), e);
                // 审核异常时，保持为待审核状态
                note.setAuditStatus("0");
            }
        }

        int result = noteMapper.updateById(note);

        // 保存审核日志（更新后才有 ID）
        if (auditResult != null && note.getId() != null) {
            try {
                commonAuditService.saveAuditLog(note.getId(), "note", auditResult);
            } catch (Exception e) {
                log.error("保存审核日志失败，笔记ID: {}", note.getId(), e);
            }
        }

        // 审核通过：按库表最新行 upsert；若从「已通过」变为非通过，从索引移除
        if (result > 0 && AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())) {
            try {
                esNoteService.upsertNoteFromDbById(note.getId());
                log.info("笔记更新成功并同步到ES，笔记ID: {}", note.getId());
            } catch (Exception e) {
                log.error("笔记同步到ES失败，笔记ID: {}", note.getId(), e);
            }
        } else if (result > 0 && oldNote != null
                && AuditStatusEnum.PASS.getCode().equals(oldNote.getAuditStatus())
                && !AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())) {
            esNoteService.deleteNote(note.getId());
        }

        return result;
    }

    /**
     * 执行审核并设置状态（使用 CommonAuditService 统一审核）
     * 参考 NoteAsyncService.performAuditAndSetStatus 方法
     *
     * @param note     笔记对象
     * @param dataList 图片/视频URL列表
     * @return 审核结果
     */
    private AutoAuditResult performAuditAndSetStatus(WebNote note, List<String> dataList) {
        try {
            // 检查内容审核是否开启
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (!Boolean.TRUE.equals(auditEnabled)) {
                log.info("内容审核未开启，笔记直接通过，笔记ID: {}", note.getId());
                note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                return null;
            }

            // 获取审核模式
            AuditModeEnum auditMode = commonAuditService.getAuditMode();
            log.info("开始执行笔记审核 - 模式: {}, 笔记ID: {}", auditMode.getDescription(), note.getId());

            // 使用通用审核服务执行审核
            String contentType = (note.getNoteType() != null && "2".equals(note.getNoteType())) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                    note.getContent(), dataList, contentType, auditMode);

            // 设置审核状态
            commonAuditService.setAuditStatus(note, auditResult, auditMode);

            log.info("笔记审核完成 - 模式: {}, 结果: {}, 原因: {}, 笔记ID: {}",
                    auditMode.getDescription(), auditResult.getResult(), auditResult.getReason(), note.getId());

            return auditResult;

        } catch (Exception e) {
            log.error("笔记审核异常，笔记ID: {}", note.getId(), e);
            // 审核异常时，设置为待人工审核
            note.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
            return null;
        }
    }

    /**
     * 批量删除笔记信息（逻辑删除）
     *
     * @param ids 需要删除的笔记ID
     */
    @Override
    public int deleteNoteByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            WebNote note = selectNoteById(id);
            if (ValidatorUtil.isNull(note)) {
                log.info("笔记不存在:{}", id);
                continue;
            }
            // 逻辑删除：更新 deleted 字段为 1
            note.setDeleted(1);
            note.setUpdateTime(new Date());
            int result = noteMapper.updateById(note);
            if (result > 0) {
                count++;
                esNoteService.deleteNote(note.getId());
            }
        }
        return count;
    }

    @Override
    public Integer getNoteCount(int status) {
        QueryWrapper<WebNote> queryWrapper = new QueryWrapper<>();
        // 添加逻辑删除过滤条件：只统计未删除的笔记
        queryWrapper.eq("deleted", 0);
        // 如果需要根据状态过滤，可以添加：
        // queryWrapper.eq(BaseSQLConf.STATUS, status);
        return Math.toIntExact(noteMapper.selectCount(queryWrapper));
    }

    @Override
    public Map<String, Object> getNoteContributeCount() {
        // 从Redis中获取博客分类下包含的博客数量
//        String jsonMap = redisUtil.get(RedisConf.DASHBOARD + Constantss.SYMBOL_COLON + RedisConf.BLOG_CONTRIBUTE_COUNT);
//        if (StringUtilss.isNotEmpty(jsonMap)) {
//            Map<String, Object> resultMap = JsonUtils.jsonToMap(jsonMap);
//            return resultMap;
//        }

        // 获取今天结束时间
        String endTime = DateUtilss.getNowTime();
        // 获取365天前的日期
        Date temp = DateUtilss.getDate(endTime, -365);
        String startTime = DateUtilss.dateTimeToStr(temp);
        List<Map<String, Object>> noteContributeMap = noteMapper.getNoteContributeCount(startTime, endTime);
        List<String> dateList = DateUtilss.getDayBetweenDates(startTime, endTime);
        Map<String, Object> dateMap = new HashMap<>();
        for (Map<String, Object> itemMap : noteContributeMap) {
            dateMap.put(itemMap.get("DATE").toString(), itemMap.get("COUNT"));
        }

        List<List<Object>> resultList = new ArrayList<>();
        for (String item : dateList) {
            Integer count = 0;
            if (dateMap.get(item) != null) {
                count = Integer.valueOf(dateMap.get(item).toString());
            }
            List<Object> objectList = new ArrayList<>();
            objectList.add(item);
            objectList.add(count);
            resultList.add(objectList);
        }

        Map<String, Object> resultMap = new HashMap<>(Constantss.NUM_TWO);
        List<String> contributeDateList = new ArrayList<>();
        contributeDateList.add(startTime);
        contributeDateList.add(endTime);
        resultMap.put(SysConf.CONTRIBUTE_DATE, contributeDateList);
        resultMap.put(SysConf.BLOG_CONTRIBUTE_COUNT, resultList);
        // 将 全年博客贡献度 存入到Redis【过期时间2小时】
//        redisUtil.setEx(RedisConf.DASHBOARD + Constantss.SYMBOL_COLON + RedisConf.BLOG_CONTRIBUTE_COUNT, JsonUtils.objectToJson(resultMap), 2, TimeUnit.HOURS);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getNoteCountByType() {
        List<Map<String, Object>> noteCountByType = noteMapper.getNoteCountByType();
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 统计现有数据
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (Map<String, Object> item : noteCountByType) {
            String noteType = String.valueOf(item.get("note_type"));
            Number count = (Number) item.get(SysConf.COUNT);
            typeCountMap.put(noteType, count != null ? count.intValue() : 0);
        }

        // 遍历所有笔记类型，确保包含所有类型的统计
        for (NoteTypeEnum type : NoteTypeEnum.values()) {
            Map<String, Object> resultItem = new HashMap<>();
            resultItem.put(SysConf.BLOG_SORT_UID, type.getCode());
            resultItem.put(SysConf.NAME, type.getDesc());
            resultItem.put(SysConf.VALUE, typeCountMap.getOrDefault(type.getCode(), 0));
            resultList.add(resultItem);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getNoteCountByCategory() {
        // 从Redis中获取博客分类下包含的博客数量
//        String jsonArrayList = redisUtil.get(RedisConf.DASHBOARD + Constantss.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_SORT);
//        if (StringUtilss.isNotEmpty(jsonArrayList)) {
//            ArrayList jsonList = JsonUtils.jsonArrayToArrayList(jsonArrayList);
//            return jsonList;
//        }
        List<Map<String, Object>> noteCountByBlogSortMap = noteMapper.getNoteCountByCategory();
        Map<String, Integer> categoryMap = new HashMap<>();
        for (Map<String, Object> item : noteCountByBlogSortMap) {
            String cpid = String.valueOf(item.get("cpid"));
            // java.lang.Number是Integer,Long的父类
            Number num = (Number) item.get(SysConf.COUNT);
            Integer count = 0;
            if (num != null) {
                count = num.intValue();
            }
            categoryMap.put(cpid, count);
        }

        //把查询到的BlogSort放到Map中
        Set<String> blogSortUids = categoryMap.keySet();
        Collection<WebCategory> blogSortCollection = new ArrayList<>();

        if (blogSortUids.size() > 0) {
            blogSortCollection = navbarMapper.selectBatchIds(blogSortUids);
        }

        Map<String, String> blogSortEntityMap = new HashMap<>();
        for (WebCategory category : blogSortCollection) {
            if (StringUtilss.isNotEmpty(category.getTitle())) {
                blogSortEntityMap.put(String.valueOf(category.getId()), category.getTitle());
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {

            String blogSortUid = entry.getKey();

            if (blogSortEntityMap.get(blogSortUid) != null) {
                String blogSortName = blogSortEntityMap.get(blogSortUid);
                Integer count = entry.getValue();
                Map<String, Object> itemResultMap = new HashMap<>();
                itemResultMap.put(SysConf.BLOG_SORT_UID, blogSortUid);
                itemResultMap.put(SysConf.NAME, blogSortName);
                itemResultMap.put(SysConf.VALUE, count);
                resultList.add(itemResultMap);
            }
        }
        // 将 每个分类下文章数目 存入到Redis【过期时间2小时】
//        if (resultList.size() > 0) {
//            redisUtil.setEx(RedisConf.DASHBOARD + Constantss.SYMBOL_COLON + RedisConf.BLOG_COUNT_BY_SORT, JsonUtils.objectToJson(resultList), 2, TimeUnit.HOURS);
//        }
        return resultList;
    }

    /**
     * 审核管理
     *
     * @param noteId    笔记ID
     * @param auditType 审核状态
     */
    @Override
    public boolean auditNote(String noteId, String auditType) {
        WebNote note = noteMapper.selectOne(new QueryWrapper<WebNote>().eq("id", noteId));
        if (ObjectUtil.isEmpty(note)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 通过：1
        if ("pass".equals(auditType)) {
            note.setAuditStatus(AuditStatusEnum.PASS.getCode());
        }
        // 拒绝：2
        if ("reject".equals(auditType)) {
            note.setAuditStatus(AuditStatusEnum.REJECT.getCode());
        }
        note.setUpdateTime(new Date());
        noteMapper.updateById(note);

        if ("pass".equals(auditType)) {
            try {
                esNoteService.upsertNoteFromDbById(noteId);
            } catch (Exception e) {
                log.error("审核通过后同步ES失败，noteId={}", noteId, e);
            }
        } else if ("reject".equals(auditType)) {
            esNoteService.deleteNote(noteId);
        }
        return true;
    }

    @Override
    public List<NoteSearchVo> getHotNote(String noteType) {
        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();
        try {
            // 构建搜索请求
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.NOTE_INDEX).sort(s -> s.field(f -> f.field("likeCount").order(SortOrder.Desc))); // 按 likeCount 降序排序

            // 如果传入了 noteType，添加一个过滤条件
            if (noteType != null && !noteType.isEmpty()) {
                builder.query(q -> q.term(t -> t.field("noteType").value(v -> v.stringValue(noteType))));
            }

            SearchRequest searchRequest = builder.build();

            // 执行搜索请求
            SearchResponse<NoteSearchVo> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVo.class);

            // 获取搜索结果
            List<Hit<NoteSearchVo>> hits = searchResponse.hits().hits();
            if (CollectionUtil.isNotEmpty(hits)) {
                for (Hit<NoteSearchVo> hit : hits) {
                    NoteSearchVo noteSearchVo = hit.source();
                    noteSearchVoList.add(noteSearchVo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 这里可以进一步处理异常，比如记录日志或者抛出自定义异常
        }
        return noteSearchVoList;
    }

    /**
     * 数据中心-热门内容-笔记数据列表（分页，由 Controller 调用 startPage 后执行）
     */
    @Override
    public List<NoteDataCenterVo> selectNoteDataCenterList(Query query) {
        QueryWrapper<WebNote> qw = new QueryWrapper<>();
        qw.lambda().eq(WebNote::getDeleted, 0);
        // 笔记题材：1=图文 2=视频 3=live图（对应 NoteTypeEnum）
        if (ValidatorUtil.isNotNull(query.getNoteType())) {
            qw.lambda().eq(WebNote::getNoteType, String.valueOf(query.getNoteType()));
        }
        // 当未显式传入 uid 时，尝试使用演示账号或当前登录用户作为创作者ID
        String rawUid = query.getUid() != null ? String.valueOf(query.getUid()) : null;
        String creatorUid = this.resolveCreatorUid(rawUid);
        if (ValidatorUtil.isNotNull(creatorUid)) {
            qw.lambda().eq(WebNote::getUid, creatorUid);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (ValidatorUtil.isNotNull(query.getStartTime())) {
            try {
                String startStr = query.getStartTime().trim();
                if (startStr.length() == 10) startStr += " 00:00:00";
                Date start = startStr.length() > 16 ? sdfFull.parse(startStr) : sdf.parse(startStr);
                qw.lambda().ge(WebNote::getCreateTime, start);
            } catch (Exception e) {
                log.warn("parse startTime failed: {}", query.getStartTime(), e);
            }
        }
        if (ValidatorUtil.isNotNull(query.getEndTime())) {
            try {
                String endStr = query.getEndTime().trim();
                if (endStr.length() == 10) endStr += " 23:59:59";
                else if (endStr.length() == 16) endStr += ":00";
                Date end = endStr.length() > 16 ? sdfFull.parse(endStr) : sdf.parse(endStr);
                qw.lambda().le(WebNote::getCreateTime, end);
            } catch (Exception e) {
                log.warn("parse endTime failed: {}", query.getEndTime(), e);
            }
        }
        qw.lambda().orderByDesc(WebNote::getCreateTime);
        List<WebNote> list = noteMapper.selectList(qw);
        // PageHelper 拦截后返回的是 Page，优先从 Page 里读取 total；
        // 如果无法读取到（或 total 为 0），则回退为当前结果集大小，避免前端看到 total=0 的异常情况。
        long total = 0L;
        if (list instanceof Page) {
            total = ((Page<?>) list).getTotal();
        }
        if (total <= 0L) {
            total = list.size();
        }
        List<String> nids = new ArrayList<>(list.size());
        for (WebNote n : list) {
            if (n != null && n.getId() != null) {
                nids.add(String.valueOf(n.getId()));
            }
        }
        Date behaviorStart = new Date(0L);
        Date behaviorEnd = new Date();
        Map<String, Map<String, Object>> behaviorByNid = new HashMap<>();
        if (!nids.isEmpty()) {
            List<Map<String, Object>> behaviorRows =
                    webNoteBehaviorMapper.aggregateNoteFeaturesByNids(nids, behaviorStart, behaviorEnd);
            if (behaviorRows != null) {
                for (Map<String, Object> row : behaviorRows) {
                    Object nidObj = row.get("nid");
                    if (nidObj != null) {
                        behaviorByNid.put(String.valueOf(nidObj), row);
                    }
                }
            }
        }
        List<NoteDataCenterVo> result = new ArrayList<>(list.size());
        for (WebNote n : list) {
            NoteDataCenterVo vo = new NoteDataCenterVo();
            vo.setId(n.getId());
            vo.setTitle(n.getTitle());
            vo.setCover(n.getNoteCover());
            vo.setPublishTime(n.getCreateTime() != null ? outFmt.format(n.getCreateTime()) : null);
            vo.setNotApproved(!"1".equals(n.getAuditStatus()));
            Map<String, Object> behavior = behaviorByNid.get(String.valueOf(n.getId()));
            long exposure = getLong(behavior, "exposure");
            long views = getLong(behavior, "views");
            long shares = getLong(behavior, "shares");
            long totalDurationSec = getLong(behavior, "totalDurationSec");
            long watchCount = getLong(behavior, "watchCount");
            vo.setExposure(exposure);
            vo.setViews(views);
            vo.setCoverCtr(exposure > 0 ? String.format("%.2f%%", views * 100.0 / exposure) : "0%");
            vo.setLikes(n.getLikeCount() != null ? n.getLikeCount() : 0L);
            vo.setComments(n.getCommentCount() != null ? n.getCommentCount() : 0L);
            vo.setFavorites(n.getCollectionCount() != null ? n.getCollectionCount() : 0L);
            Date noteCreate = n.getCreateTime() != null ? n.getCreateTime() : behaviorStart;
            Calendar c = Calendar.getInstance();
            c.setTime(noteCreate);
            c.add(Calendar.DAY_OF_MONTH, 7);
            Date followEnd = c.getTime().before(behaviorEnd) ? c.getTime() : behaviorEnd;
            Long follows = webFollowerMapper.countByFidAndCreateTimeRange(n.getUid(), noteCreate, followEnd);
            vo.setFollows(follows != null ? follows : 0L);
            vo.setShares(shares);
            vo.setAvgDuration((watchCount > 0 ? (totalDurationSec / watchCount) : 0) + "s");
            vo.setDanmaku(null);
            result.add(vo);
        }
        // 封装为 Page，使 getDataTable(new PageInfo(list)) 能取到正确的 total
        Page<NoteDataCenterVo> resultPage = new Page<>();
        resultPage.addAll(result);
        resultPage.setTotal(total);
        return resultPage;
    }

    /**
     * 解析创作者 UID：
     * 1. 如果前端显式传入 uid，则优先使用该 uid；
     * 2. 否则，如果开启了演示账号并配置了用户名，则使用演示账号对应的用户ID；
     * 3. 再否则，回退到当前登录用户ID；如仍失败则返回 null。
     */
    private String resolveCreatorUid(String uid) {
        // 1. 前端显式传入 uid 时，优先使用
        if (ValidatorUtil.isNotNull(uid) && !uid.isEmpty()) {
            return uid;
        }

        // 2. 如果演示账号开启，并配置了用户名，则优先使用演示账号
        try {
            DemoAccountConfigDTO demoConfig = systemConfigService.getDemoAccountConfig();
            if (demoConfig != null
                    && Boolean.TRUE.equals(demoConfig.getEnabled())
                    && demoConfig.getUsername() != null
                    && !demoConfig.getUsername().isEmpty()) {
                WebUser user = userMapper.selectOne(
                        new QueryWrapper<WebUser>()
                                .lambda()
                                .eq(WebUser::getPhone, demoConfig.getUsername())
                );
                if (user != null && ValidatorUtil.isNotNull(user.getId())) {
                    return String.valueOf(user.getId());
                }
            }
        } catch (Exception e) {
            log.warn("解析演示账号配置失败，回退到当前登录用户: {}", e.getMessage());
        }
        // 3. 回退：当前登录用户
        try {
            return AuthContextHolder.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public NoteDataCenterDetailVo getNoteDataCenterDetail(String notesId, Integer days) {
        if (notesId == null || notesId.isEmpty()) {
            return null;
        }
        if (days == null || (days != 7 && days != 14 && days != 30)) {
            days = 30;
        }
        WebNote note = noteMapper.selectById(notesId);
        if (note == null) {
            return null;
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date endTime = new Date();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -days);
        Date startTime = cal.getTime();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -days);
        Date prevStartTime = cal.getTime();
        Date prevEndTime = new Date(startTime.getTime() - 1);

        Long exposure = webNoteBehaviorMapper.countExposureByNidAndTimeRange(notesId, startTime, endTime);
        Long viewCount = webNoteBehaviorMapper.countViewByNidAndTimeRange(notesId, startTime, endTime);
        Long prevExposure = webNoteBehaviorMapper.countExposureByNidAndTimeRange(notesId, prevStartTime, prevEndTime);
        Long prevView = webNoteBehaviorMapper.countViewByNidAndTimeRange(notesId, prevStartTime, prevEndTime);
        if (exposure == null) exposure = 0L;
        if (viewCount == null) viewCount = 0L;
        if (prevExposure == null) prevExposure = 0L;
        if (prevView == null) prevView = 0L;

        Map<String, Object> durationResult = webNoteBehaviorMapper.sumDurationAndCompletionByNid(notesId, startTime, endTime);
        Map<String, Object> prevDurationResult = webNoteBehaviorMapper.sumDurationAndCompletionByNid(notesId, prevStartTime, prevEndTime);
        Long shareCount = webNoteBehaviorMapper.countShareByNidAndTimeRange(notesId, startTime, endTime);
        Long exitWithin2s = webNoteBehaviorMapper.countWatchExitWithinSecByNid(notesId, 2, startTime, endTime);
        long totalDurationSec = getLong(durationResult, "totalDurationSec");
        long watchCount = getLong(durationResult, "watchCount");
        long completedCount = getLong(durationResult, "completedCount");
        long prevTotalDurationSec = getLong(prevDurationResult, "totalDurationSec");
        long prevWatchCount = getLong(prevDurationResult, "watchCount");

        long likes = note.getLikeCount() != null ? note.getLikeCount() : 0L;
        long comments = note.getCommentCount() != null ? note.getCommentCount() : 0L;
        long favorites = note.getCollectionCount() != null ? note.getCollectionCount() : 0L;
        long interactionCount = likes + comments + favorites;
        long prevInteraction = 0; // 环比暂不查上一周期互动，可后续扩展

        int ringExposure = ring(exposure, prevExposure);
        int ringView = ring(viewCount, prevView);
        int ringDuration = ring(watchCount > 0 ? totalDurationSec : 0, prevWatchCount > 0 ? prevTotalDurationSec : 0);
        int ringInteraction = ring(interactionCount, prevInteraction);

        SimpleDateFormat outFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        NoteDataCenterDetailVo vo = new NoteDataCenterDetailVo();
        NoteDataCenterDetailVo.NoteInfo info = new NoteDataCenterDetailVo.NoteInfo();
        info.setId(note.getId());
        info.setTitle(note.getTitle());
        info.setCover(note.getNoteCover());
        info.setAuthor(note.getAuthor());
        info.setPublishTime(note.getCreateTime() != null ? outFmt.format(note.getCreateTime()) : null);
        info.setTags(note.getTags());
        vo.setNoteInfo(info);

        vo.setOverview(Arrays.asList(
                detailItem("曝光数", formatNum(exposure), ringExposure),
                detailItem("观看数", formatNum(viewCount), ringView),
                detailItem("观看时长", formatDuration(totalDurationSec), ringDuration),
                detailItem("互动数", interactionCount, ringInteraction)
        ));

        List<NoteDataCenterDetailVo.NoteDetailTrendPoint> exposureTrend = buildTrendByDay(
                webNoteBehaviorMapper.countExposureByDayByNid(notesId, startTime, endTime), startTime, endTime, days);
        List<NoteDataCenterDetailVo.NoteDetailTrendPoint> viewTrend = buildTrendByDay(
                webNoteBehaviorMapper.countViewByDayByNid(notesId, startTime, endTime), startTime, endTime, days);
        vo.setExposureTrend(exposureTrend);
        vo.setViewTrend(viewTrend);

        List<Map<String, Object>> sceneList = webNoteBehaviorMapper.countViewBySceneByNid(notesId, startTime, endTime);
        List<NoteDataCenterDetailVo.NoteDetailNameValue> viewSource = new ArrayList<>();
        if (sceneList != null) {
            for (Map<String, Object> row : sceneList) {
                Object name = row.get("scene");
                Object cnt = row.get("cnt");
                String sceneName = sceneToName(name != null ? name.toString() : "other");
                viewSource.add(nameValue(sceneName, cnt instanceof Number ? ((Number) cnt).longValue() : 0L));
            }
        }
        if (viewSource.isEmpty()) {
            viewSource.add(nameValue("其他来源", viewCount));
        }
        vo.setViewSource(viewSource);

        List<Long> timeSlot = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) timeSlot.add(0L);
        List<Map<String, Object>> hourList = webNoteBehaviorMapper.countViewByHourByNid(notesId, startTime, endTime);
        if (hourList != null) {
            for (Map<String, Object> row : hourList) {
                Object h = row.get("hour");
                Object cnt = row.get("cnt");
                if (h instanceof Number) {
                    int idx = ((Number) h).intValue();
                    if (idx >= 0 && idx < 24) {
                        timeSlot.set(idx, cnt instanceof Number ? ((Number) cnt).longValue() : 0L);
                    }
                }
            }
        }
        vo.setViewTimeSlot(timeSlot);

        vo.setAvgDurationSec(watchCount > 0 ? (int) (totalDurationSec / watchCount) : 0);
        vo.setCompletionRate(watchCount > 0 ? completedCount * 100.0 / watchCount : 0.0);
        vo.setTwoSecExitRate(watchCount > 0 ? (exitWithin2s != null ? exitWithin2s : 0L) * 100.0 / watchCount : 0.0);
        vo.setShares(shareCount != null ? shareCount : 0L);
        vo.setLikes(likes);
        vo.setComments(comments);
        vo.setFavorites(favorites);
        vo.setAudienceProfile(this.buildAudienceProfile(notesId, startTime, endTime));
        return vo;
    }

    /**
     * 观众画像：按该笔记在统计周期内的真实观众行为统计性别/年龄/城市/兴趣。
     * 如果没有任何观看行为，则返回空数据，由前端自行展示“暂无数据”。
     */
    private NoteDataCenterDetailVo.AudienceProfile buildAudienceProfile(String notesId, java.util.Date startTime, java.util.Date endTime) {
        NoteDataCenterDetailVo.AudienceProfile profile = new NoteDataCenterDetailVo.AudienceProfile();
        profile.setGender(new ArrayList<>());
        profile.setAge(new ArrayList<>());
        profile.setRegion(new ArrayList<>());
        profile.setInterest(new ArrayList<>());

        if (ValidatorUtil.isNull(notesId)) {
            return profile;
        }

        // 1. 找出在统计周期内观看过该笔记的所有用户（view / watch）
        List<WebNoteBehavior> behaviors = webNoteBehaviorMapper.selectList(
                new QueryWrapper<WebNoteBehavior>()
                        .eq("nid", notesId)
                        .in("event_type", Arrays.asList("view", "watch"))
                        .ge("create_time", startTime)
                        .le("create_time", endTime)
                        .select("uid"));
        if (behaviors == null || behaviors.isEmpty()) {
            return profile;
        }

        Set<String> uidSet = new HashSet<>();
        for (WebNoteBehavior b : behaviors) {
            if (ValidatorUtil.isNotNull(b.getUid())) {
                uidSet.add(b.getUid());
            }
        }
        if (uidSet.isEmpty()) {
            return profile;
        }

        List<WebUser> users = userMapper.selectBatchIds(new ArrayList<>(uidSet));
        if (users == null || users.isEmpty()) {
            return profile;
        }

        // 2. 性别分布（1=男，2=女，其它为未知）
        Map<String, Long> genderCount = new HashMap<>();
        for (WebUser u : users) {
            if (ValidatorUtil.isNotNull(u.getGender())) {
                genderCount.merge(u.getGender(), 1L, Long::sum);
            }
        }
        long male = genderCount.getOrDefault("1", 0L);
        long female = genderCount.getOrDefault("2", 0L);
        long other = users.size() - male - female;
        if (other < 0) other = 0;
        List<NoteDataCenterDetailVo.NoteDetailNameValue> genderList = new ArrayList<>();
        if (female > 0) genderList.add(nameValue("女性", female));
        if (male > 0) genderList.add(nameValue("男性", male));
        if (other > 0) genderList.add(nameValue("未知", other));
        profile.setGender(genderList);

        // 3. 年龄分布（根据生日年份粗略分组）
        Map<String, Long> ageCount = new LinkedHashMap<>();
        ageCount.put("<18", 0L);
        ageCount.put("18-24", 0L);
        ageCount.put("25-34", 0L);
        ageCount.put("35-44", 0L);
        ageCount.put(">44", 0L);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (WebUser u : users) {
            String birthday = u.getBirthday();
            if (ValidatorUtil.isNull(birthday) || birthday.length() < 4) {
                continue;
            }
            int birthYear;
            try {
                birthYear = Integer.parseInt(birthday.substring(0, 4));
            } catch (NumberFormatException e) {
                continue;
            }
            int age = currentYear - birthYear;
            String group;
            if (age < 18) group = "<18";
            else if (age <= 24) group = "18-24";
            else if (age <= 34) group = "25-34";
            else if (age <= 44) group = "35-44";
            else group = ">44";
            ageCount.put(group, ageCount.get(group) + 1);
        }
        List<NoteDataCenterDetailVo.NoteDetailNameValue> ageList = new ArrayList<>();
        for (Map.Entry<String, Long> e : ageCount.entrySet()) {
            if (e.getValue() > 0) {
                ageList.add(nameValue(e.getKey(), e.getValue()));
            }
        }
        profile.setAge(ageList);

        // 4. 城市/省份分布（按省份统计，取 Top10）
        Map<String, Long> regionCount = new HashMap<>();
        for (WebUser u : users) {
            if (ValidatorUtil.isNotNull(u.getProvince())) {
                regionCount.merge(u.getProvince(), 1L, Long::sum);
            }
        }
        List<Map.Entry<String, Long>> regionEntries = new ArrayList<>(regionCount.entrySet());
        regionEntries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        List<NoteDataCenterDetailVo.NoteDetailNameValue> regionList = new ArrayList<>();
        int limit = Math.min(10, regionEntries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Long> e = regionEntries.get(i);
            regionList.add(nameValue(e.getKey(), e.getValue()));
        }
        profile.setRegion(regionList);

        // 5. 兴趣分布：当前暂无单笔记维度的兴趣画像数据，返回空列表由前端展示“暂无数据”
        profile.setInterest(new ArrayList<>());

        return profile;
    }

    private static long getLong(Map<String, Object> map, String key) {
        if (map == null) return 0L;
        Object v = map.get(key);
        return v instanceof Number ? ((Number) v).longValue() : 0L;
    }

    private static int ring(long current, long previous) {
        if (previous == 0) return 0;
        return (int) Math.round((current - previous) * 100.0 / previous);
    }

    private static String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }

    private static String formatDuration(long sec) {
        if (sec >= 3600) return (sec / 3600) + "小时";
        if (sec >= 60) return (sec / 60) + "分钟";
        return sec + "秒";
    }

    private static NoteDataCenterDetailVo.NoteDetailOverviewItem detailItem(String label, Object value, int ring) {
        NoteDataCenterDetailVo.NoteDetailOverviewItem item = new NoteDataCenterDetailVo.NoteDetailOverviewItem();
        item.setLabel(label);
        item.setValue(value);
        item.setRing(ring);
        item.setRingText(ring == 0 ? "环比 -" : (ring > 0 ? "环比 +" + ring + "%" : "环比 " + ring + "%"));
        return item;
    }

    private static NoteDataCenterDetailVo.NoteDetailNameValue nameValue(String name, long value) {
        NoteDataCenterDetailVo.NoteDetailNameValue o = new NoteDataCenterDetailVo.NoteDetailNameValue();
        o.setName(name);
        o.setValue(value);
        return o;
    }

    private static String sceneToName(String scene) {
        switch (scene == null ? "other" : scene.toLowerCase()) {
            case "recommend":
                return "首页推荐";
            case "follow":
                return "关注页面";
            case "search":
                return "搜索";
            case "profile":
                return "个人主页";
            default:
                return "其他来源";
        }
    }

    private List<NoteDataCenterDetailVo.NoteDetailTrendPoint> buildTrendByDay(List<Map<String, Object>> raw,
                                                                              Date startTime, Date endTime, int days) {
        Map<String, Long> byDate = new HashMap<>();
        if (raw != null) {
            for (Map<String, Object> row : raw) {
                Object d = row.get("date");
                Object cnt = row.get("cnt");
                if (d != null) byDate.put(d.toString(), cnt instanceof Number ? ((Number) cnt).longValue() : 0L);
            }
        }
        List<NoteDataCenterDetailVo.NoteDetailTrendPoint> list = new ArrayList<>();
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(startTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            String dateKey = sdf.format(c.getTime());
            NoteDataCenterDetailVo.NoteDetailTrendPoint p = new NoteDataCenterDetailVo.NoteDetailTrendPoint();
            p.setDate(dateKey);
            p.setCnt(byDate.getOrDefault(dateKey, 0L));
            list.add(p);
            c.add(java.util.Calendar.DAY_OF_MONTH, 1);
            if (c.getTime().after(endTime)) break;
        }
        return list;
    }
}
