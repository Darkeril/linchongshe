package com.hongshu.web.async;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.audit.service.ContentAuditService;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.TerminalTypeEnum;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.web.domain.dto.NoteDTO;
import com.hongshu.web.domain.dto.NoteAppDTO;
import com.hongshu.web.domain.entity.IdleProduct;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebProductNoteRelation;
import com.hongshu.web.domain.entity.WebAlbum;
import com.hongshu.web.domain.entity.WebAlbumNoteRelation;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebAlbumMapper;
import com.hongshu.web.mapper.web.WebAlbumNoteRelationMapper;
import com.hongshu.web.mapper.web.WebAuditLogMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebProductNoteRelationMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.rocketmq.StatisticsServiceV2;
import com.hongshu.web.service.web.IWebEsNoteService;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoteAsyncService {

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private WebProductNoteRelationMapper noteRelationMapper;
    @Autowired
    private WebAlbumMapper albumMapper;
    @Autowired
    private WebAlbumNoteRelationMapper albumNoteRelationMapper;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private ISysSystemConfigService systemConfigService;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private ContentAuditService contentAuditService;
    @Autowired
    private WebAuditLogMapper auditLogMapper;
    @Autowired
    private CommonAuditService commonAuditService;
    @Autowired
    private IWebEsNoteService esNoteService;
    @Autowired
    private StatisticsServiceV2 statisticsServiceV2;


    // 文件上传专用线程池（用于并行上传）
    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(10, r -> {
        Thread t = new Thread(r);
        t.setName("file-upload-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    @Async
    public void addNote(String terminal, String currentUid, String noteData, String coverLocalPath, List<String> fileLocalPaths) {
        long totalStartTime = System.currentTimeMillis();
        try {
            log.info("========== 开始处理笔记发布 ========== 用户ID: {}, 文件数量: {}", 
                currentUid, fileLocalPaths != null ? fileLocalPaths.size() : 0);
            
            // 更新用户笔记数量
            long userUpdateStart = System.currentTimeMillis();
            WebUser user = userMapper.selectById(currentUid);
            user.setNoteCount(user.getNoteCount() + 1);
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
            long userUpdateTime = System.currentTimeMillis() - userUpdateStart;
            log.info("[性能监控] 更新用户笔记数量耗时: {}ms", userUpdateTime);

            // 保存笔记
            long noteParseStart = System.currentTimeMillis();
            NoteDTO noteDTO = JSONUtil.toBean(noteData, NoteDTO.class);
            WebNote note = ConvertUtils.sourceToTarget(noteDTO, WebNote.class);
            String tags = JSONUtil.toJsonStr(noteDTO.getTags().toArray(new String[0]));
            note.setTags(tags);
            note.setUid(currentUid);
            note.setFromType(TerminalTypeEnum.fromValue(terminal).getCode());
            note.setAuthor(user.getUsername());
            note.setCreator(user.getUsername());
            note.setTime(System.currentTimeMillis());
            note.setCreateTime(new Date());
            note.setUpdateTime(new Date());
            long noteParseTime = System.currentTimeMillis() - noteParseStart;
            log.info("[性能监控] 解析笔记数据耗时: {}ms", noteParseTime);
            
            try {
                // 上传封面
                if (coverLocalPath == null) {
                    throw new RuntimeException("封面文件不能为空");
                }
                File coverFile = new File(coverLocalPath);
                log.info("开始上传封面，文件路径: {}, 文件存在: {}, 文件大小: {} bytes", 
                    coverLocalPath, coverFile.exists(), coverFile.length());
                
                long coverUploadStart = System.currentTimeMillis();
                // 使用 CompletableFuture 异步上传封面，避免阻塞
                CompletableFuture<R<SysFile>> coverUploadFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return remoteFileService.upload(coverFile);
                    } catch (Exception e) {
                        log.error("封面上传异常", e);
                        throw new RuntimeException("封面上传失败: " + e.getMessage(), e);
                    }
                }, uploadExecutor);
                
                R<SysFile> upload = coverUploadFuture.get(); // 等待上传完成
                long coverUploadTime = System.currentTimeMillis() - coverUploadStart;
                log.info("[性能监控] 封面上传耗时: {}ms", coverUploadTime);
                
                // 检查上传结果
                if (upload == null) {
                    log.error("封面上传失败：远程文件服务无响应");
                    throw new RuntimeException("封面上传失败: 远程文件服务无响应");
                }
                
                log.info("封面上传响应 - code: {}, msg: {}, data: {}", 
                    upload.getCode(), upload.getMsg(), upload.getData());
                
                if (upload.getCode() != 200) {
                    log.error("封面上传失败：返回码异常 - code: {}, msg: {}", upload.getCode(), upload.getMsg());
                    throw new RuntimeException("封面上传失败: code=" + upload.getCode() + ", msg=" + upload.getMsg());
                }
                
                if (upload.getData() == null || upload.getData().getUrl() == null) {
                    log.error("封面上传失败：返回数据为空 - data: {}", upload.getData());
                    throw new RuntimeException("封面上传失败: 返回数据为空");
                }
                
                String noteCover = upload.getData().getUrl();
                log.info("封面上传成功，URL: {}", noteCover);
                note.setNoteCover(noteCover);

                // 根据笔记类型处理文件
                List<String> dataList = new ArrayList<>();
                if ("1".equals(note.getNoteType())) {
                    // 图片类型
                    if (fileLocalPaths == null || fileLocalPaths.size() <= 0) {
                        throw new RuntimeException("未上传任何图片");
                    }
                    List<File> files = fileLocalPaths.stream().map(File::new).collect(Collectors.toList());
                    // 批量上传图片 - 使用并行上传优化性能
                    long batchUploadStart = System.currentTimeMillis();
                    log.info("[性能监控] 开始批量上传图片，图片数量: {}", files.size());
                    
                    R<List<SysFile>> listR;
                    // 如果文件数量较少（<=3），使用批量上传；否则使用并行上传
                    if (files.size() <= 3) {
                        // 小批量：使用批量上传接口
                        CompletableFuture<R<List<SysFile>>> batchUploadFuture = CompletableFuture.supplyAsync(() -> {
                            try {
                                return remoteFileService.batchUpload(files);
                            } catch (Exception e) {
                                log.error("批量上传图片异常", e);
                                throw new RuntimeException("批量上传图片失败: " + e.getMessage(), e);
                            }
                        }, uploadExecutor);
                        listR = batchUploadFuture.get();
                    } else {
                        // 大批量：使用并行上传，每个文件独立上传
                        log.info("[性能优化] 使用并行上传模式，文件数量: {}", files.size());
                        List<CompletableFuture<R<SysFile>>> uploadFutures = files.stream()
                            .map(file -> CompletableFuture.supplyAsync(() -> {
                                try {
                                    return remoteFileService.upload(file);
                                } catch (Exception e) {
                                    log.error("单文件上传异常: {}", file.getName(), e);
                                    throw new RuntimeException("文件上传失败: " + file.getName() + ", " + e.getMessage(), e);
                                }
                            }, uploadExecutor))
                            .collect(Collectors.toList());
                        
                        // 等待所有上传完成
                        CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                            uploadFutures.toArray(new CompletableFuture[0])
                        );
                        allUploads.join(); // 等待所有任务完成
                        
                        // 收集所有上传结果
                        List<SysFile> uploadedFiles = new ArrayList<>();
                        for (CompletableFuture<R<SysFile>> future : uploadFutures) {
                            try {
                                R<SysFile> result = future.join();
                                if (result != null && result.getCode() == 200 && result.getData() != null) {
                                    uploadedFiles.add(result.getData());
                                } else {
                                    log.warn("文件上传失败: code={}, msg={}", 
                                        result != null ? result.getCode() : "null",
                                        result != null ? result.getMsg() : "null");
                                }
                            } catch (Exception e) {
                                log.error("文件上传异常", e);
                                throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
                            }
                        }
                        
                        if (uploadedFiles.isEmpty()) {
                            throw new RuntimeException("所有文件上传失败");
                        }
                        
                        // 构建返回结果
                        listR = R.ok(uploadedFiles);
                    }
                    
                    long batchUploadTime = System.currentTimeMillis() - batchUploadStart;
                    log.info("[性能监控] 批量上传图片耗时: {}ms, 平均每张: {}ms", 
                        batchUploadTime, files.size() > 0 ? batchUploadTime / files.size() : 0);
                    List<String> urls = Optional.ofNullable(listR)
                            .filter(response -> response.getCode() == 200 && response.getData() != null) // 成功且有数据
                            .map(R::getData)
                            .filter(CollectionUtils::isNotEmpty)
                            .orElseThrow(() -> {
                                String errorMsg = (listR == null) ? "远程服务无响应" : ("code=" + listR.getCode() + ", msg=" + listR.getMsg());
                                return new RuntimeException("图片上传失败: " + errorMsg);
                            })
                            .stream()
                            .map(SysFile::getUrl) // 提取URL
                            .filter(Objects::nonNull) // 过滤掉null的URL
                            .collect(Collectors.toList());
                    dataList.addAll(urls);
//                    List<String> listR = ossService.uploadBatch(files);
//                    if (listR != null) {
//                        dataList = listR;
//                    } else {
//                        throw new RuntimeException("图片上传失败");
//                    }
                } else if ("2".equals(note.getNoteType())) {
                    // 视频类型
                    if (fileLocalPaths == null || fileLocalPaths.size() <= 0) {
                        throw new RuntimeException("请上传一个视频文件");
                    }
                    // 上传视频 - 使用异步方式
                    List<File> files = fileLocalPaths.stream().map(File::new).collect(Collectors.toList());
                    long videoUploadStart = System.currentTimeMillis();
                    log.info("[性能监控] 开始上传视频，文件大小: {} bytes", files.get(0).length());
                    
                    CompletableFuture<R<SysFile>> videoUploadFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            return remoteFileService.upload(files.get(0));
                        } catch (Exception e) {
                            log.error("视频上传异常", e);
                            throw new RuntimeException("视频上传失败: " + e.getMessage(), e);
                        }
                    }, uploadExecutor);
                    
                    R<SysFile> videoUpload = videoUploadFuture.get();
                    long videoUploadTime = System.currentTimeMillis() - videoUploadStart;
                    log.info("[性能监控] 视频上传耗时: {}ms", videoUploadTime);
                    
                    if (videoUpload == null || videoUpload.getCode() != 200 || videoUpload.getData() == null || videoUpload.getData().getUrl() == null) {
                        String err = (videoUpload == null) ? "远程服务无响应" : ("code=" + videoUpload.getCode() + ", msg=" + videoUpload.getMsg());
                        throw new RuntimeException("视频上传失败: " + err);
                    }
                    String videoResult = videoUpload.getData().getUrl();
                    if (videoResult != null) {
                        dataList.add(videoResult);
                    } else {
                        throw new RuntimeException("视频上传失败");
                    }
                } else {
                    throw new RuntimeException("无效的笔记类型");
                }
                if (dataList.isEmpty()) {
                    throw new RuntimeException("未获取到有效的文件URL");
                }
                // 设置文件URL
                String urls = JSONUtil.toJsonStr(dataList.toArray(new String[0]));
                note.setUrls(urls);

                // 先保存笔记（状态设为审核中），然后异步审核
                Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
                if (Boolean.TRUE.equals(auditEnabled)) {
                    note.setAuditStatus(AuditStatusEnum.REVIEW.getCode()); // 先设为审核中
                } else {
                    note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                }

                // 处理关联商品
                if (noteDTO.getRelatedProducts() != null && !noteDTO.getRelatedProducts().isEmpty()) {
                    // 从关联商品中提取ID并拼接
                    String productIds = noteDTO.getRelatedProducts().stream().map(IdleProduct::getId).collect(Collectors.joining(","));
                    note.setProductId(productIds);
                }
                // 保存笔记
                long dbInsertStart = System.currentTimeMillis();
                noteMapper.insert(note);
                long dbInsertTime = System.currentTimeMillis() - dbInsertStart;
                log.info("[性能监控] 保存笔记到数据库耗时: {}ms", dbInsertTime);

                // 统计：异步发布笔记成功落库后，及时刷新工作台/首页统计缓存
                try {
                    statisticsServiceV2.notifyStatisticsUpdate("note", "create", note.getId());
                } catch (Exception e) {
                    log.warn("刷新统计缓存失败（不影响发布流程）: {}", e.getMessage());
                }

                // 审核关闭且直接通过：立即写入 ES（开启审核时由异步审核回调 upsert）
                String newNoteId = note.getId();
                if (newNoteId != null && AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())
                        && !Boolean.TRUE.equals(auditEnabled)) {
                    try {
                        esNoteService.upsertNoteFromDbById(newNoteId);
                        log.info("笔记发布成功并同步到ES，笔记ID: {}", newNoteId);
                    } catch (Exception e) {
                        log.error("笔记同步到ES失败，笔记ID: {}", newNoteId, e);
                    }
                }

                // 异步执行审核（不阻塞保存流程）
                String noteId = note.getId();
                if (noteId != null && auditEnabled) {
                    // 转换为NoteAppDTO用于异步审核
                    NoteAppDTO noteAppDTO = new NoteAppDTO();
                    noteAppDTO.setContent(noteDTO.getContent());
                    noteAppDTO.setNoteType(noteDTO.getNoteType());
                    // 获取审核模式（auto/hybrid/manual）
                    AuditModeEnum auditModeEnum = commonAuditService.getAuditMode();
                    String auditMode = auditModeEnum != null ? auditModeEnum.getCode() : "manual";
                    auditNoteAsync(noteId, noteAppDTO, dataList, auditMode);
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
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            log.info("========== 笔记发布完成 ========== 总耗时: {}ms", totalTime);
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - totalStartTime;
            log.error("笔记保存失败，总耗时: {}ms", totalTime, e);
            // 这里可以做一些失败补偿或通知
        } finally {
            // 删除封面临时文件
            if (coverLocalPath != null) {
                File coverFile = new File(coverLocalPath);
                if (coverFile.exists()) {
                    boolean deleted = coverFile.delete();
                    if (!deleted) {
                        log.warn("封面临时文件删除失败: {}", coverLocalPath);
                    }
                }
            }
            // 删除内容文件临时文件
            if (fileLocalPaths != null) {
                for (String path : fileLocalPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.warn("内容临时文件删除失败: {}", path);
                        }
                    }
                }
            }
        }
    }

    @Async
    public void updateNote(NoteDTO noteDTO, WebNote note, String coverLocalPath, List<String> fileLocalPaths) {
        try {
            // 更新基本字段
            note.setTitle(noteDTO.getTitle());
            note.setContent(noteDTO.getContent());
            note.setTags(JSONUtil.toJsonStr(noteDTO.getTags().toArray(new String[0])));
            note.setProvince(noteDTO.getProvince());
            note.setCity(noteDTO.getCity());
            note.setDistrict(noteDTO.getDistrict());
            note.setAddress(noteDTO.getAddress());
            note.setCid(noteDTO.getCid());
            note.setCpid(noteDTO.getCpid());
            note.setAlbumId(noteDTO.getAlbumId());
            note.setUpdateTime(new Date());
            // 处理封面：允许不上传新封面，优先使用新上传的，其次使用 DTO 中传递的 URL，最后保留原封面
            if (coverLocalPath != null) {
                File coverFile = new File(coverLocalPath);
//                String noteCover = ossService.upload(coverFile);
                R<SysFile> upload = remoteFileService.upload(coverFile);
                String noteCover = Optional.ofNullable(upload)
                        .filter(r -> r.getCode() == 200 && r.getData() != null)
                        .map(R::getData)
                        .map(SysFile::getUrl)
                        .orElseThrow(() -> new RuntimeException("封面上传失败"));
                note.setNoteCover(noteCover);
            } else if (noteDTO.getNoteCover() != null) {
                // 前端可能直接传回已有封面 URL
                note.setNoteCover(noteDTO.getNoteCover());
            }

            // 处理文件（图片或视频）
            List<String> dataList = new ArrayList<>();
            if ("1".equals(noteDTO.getNoteType())) {
                // 图片类型
                if (fileLocalPaths != null && !fileLocalPaths.isEmpty()) {
                    // 有新上传的图片：以「前端传回的现有图片 URL + 新上传图片 URL」为准
                    // 1）先把前端传回的现有图片 URL（未删除的老图顺序）放进去
                    if (noteDTO.getUrls() != null && !noteDTO.getUrls().isEmpty()) {
                        dataList.addAll(noteDTO.getUrls());
                    }

                    // 2）上传本次新增的图片
                    List<File> files = fileLocalPaths.stream().map(File::new).collect(Collectors.toList());
                    R<List<SysFile>> listR = remoteFileService.batchUpload(files);
                    List<String> urls = Optional.ofNullable(listR)
                            .filter(response -> response.getCode() == 200) // 检查成功状态
                            .map(R::getData) // 获取数据
                            .filter(CollectionUtils::isNotEmpty) // 检查数据不为空
                            .orElseThrow(() -> {
                                String errorMsg = listR != null ? listR.getMsg() : "远程服务无响应";
                                return new RuntimeException("图片上传失败: " + errorMsg);
                            })
                            .stream()
                            .map(SysFile::getUrl) // 提取URL
                            .filter(Objects::nonNull) // 过滤掉null的URL
                            .collect(Collectors.toList());
                    dataList.addAll(urls);

                    note.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                } else {
                    // 没有上传新图片：沿用前端传来的 URL 列表或数据库中已有的 URL
                    if (noteDTO.getUrls() != null && !noteDTO.getUrls().isEmpty()) {
                        dataList.addAll(noteDTO.getUrls());
                        note.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                    } else if (StringUtils.isNotBlank(note.getUrls())) {
                        List<String> existingUrls = JSONUtil.toList(note.getUrls(), String.class);
                        dataList.addAll(existingUrls);
                    } else {
                        throw new RuntimeException("未上传任何图片");
                    }
                }
            } else if ("2".equals(noteDTO.getNoteType())) {
                // 视频类型
                if (fileLocalPaths != null && !fileLocalPaths.isEmpty()) {
                    // 上传新视频
                    List<File> files = fileLocalPaths.stream().map(File::new).collect(Collectors.toList());
//                    String videoResult = ossService.upload(files.get(0));
                    R<SysFile> videoUpload = remoteFileService.upload(files.get(0));
                    String videoResult = Optional.ofNullable(videoUpload)
                            .filter(r -> r.getCode() == 200 && r.getData() != null)
                            .map(R::getData)
                            .map(SysFile::getUrl)
                            .orElse(null);
                    if (videoResult != null) {
                        dataList.add(videoResult);
                        note.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                    } else {
                        throw new RuntimeException("视频上传失败");
                    }
                } else {
                    // 未上传新视频，保留原有或前端传来的 URL
                    if (noteDTO.getUrls() != null && !noteDTO.getUrls().isEmpty()) {
                        dataList.addAll(noteDTO.getUrls());
                        note.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                    } else if (StringUtils.isNotBlank(note.getUrls())) {
                        List<String> existingUrls = JSONUtil.toList(note.getUrls(), String.class);
                        dataList.addAll(existingUrls);
                    } else {
                        throw new RuntimeException("请上传一个视频文件");
                    }
                }
            } else {
                throw new RuntimeException("无效的笔记类型");
            }
            // 处理关联商品
            if (noteDTO.getRelatedProducts() != null) {
                // 先删除原有所有关联
                noteRelationMapper.delete(new QueryWrapper<WebProductNoteRelation>().eq("nid", note.getId()));
                if (!noteDTO.getRelatedProducts().isEmpty()) {
                    // 有新商品，插入新关联
                    String productIds = noteDTO.getRelatedProducts().stream().map(IdleProduct::getId).collect(Collectors.joining(","));
                    note.setProductId(productIds);
                    List<WebProductNoteRelation> noteProducts = noteDTO.getRelatedProducts().stream().map(product -> {
                        WebProductNoteRelation noteProduct = new WebProductNoteRelation();
                        noteProduct.setNid(note.getId());
                        noteProduct.setPid(product.getId());
                        noteProduct.setCreateTime(new Date());
                        noteProduct.setUpdateTime(new Date());
                        return noteProduct;
                    }).collect(Collectors.toList());
                    noteRelationMapper.insertBatchSql(noteProducts);
                } else {
                    // 集合为空，清空 productId
                    note.setProductId("");
                }
            }
            // 先保存笔记（状态设为审核中），然后异步审核
            final String previousAuditStatus = note.getAuditStatus();
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (Boolean.TRUE.equals(auditEnabled)) {
                note.setAuditStatus(AuditStatusEnum.REVIEW.getCode()); // 先设为审核中
            } else {
                note.setAuditStatus(AuditStatusEnum.PASS.getCode());
            }
            
            // 更新数据库
            noteMapper.updateById(note);

            // 已通过：按库表 upsert；从「已通过」变为重新审核等：从索引移除
            if (AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())) {
                try {
                    esNoteService.upsertNoteFromDbById(note.getId());
                    log.info("笔记更新成功并同步到ES，笔记ID: {}", note.getId());
                } catch (Exception e) {
                    log.error("笔记同步到ES失败，笔记ID: {}", note.getId(), e);
                }
            } else if (AuditStatusEnum.PASS.getCode().equals(previousAuditStatus)) {
                esNoteService.deleteNote(note.getId());
            }

            // 异步执行审核（不阻塞更新流程）
            String noteId = note.getId();
            if (noteId != null && auditEnabled) {
                // 转换为NoteAppDTO用于异步审核
                NoteAppDTO noteAppDTO = new NoteAppDTO();
                noteAppDTO.setContent(noteDTO.getContent());
                noteAppDTO.setNoteType(noteDTO.getNoteType());
                // 获取审核模式（auto/hybrid/manual）
                AuditModeEnum auditModeEnum = commonAuditService.getAuditMode();
                String auditMode = auditModeEnum != null ? auditModeEnum.getCode() : "manual";
                auditNoteAsync(noteId, noteAppDTO, dataList, auditMode);
            }

            // 处理专辑变更
            WebNote oldNote = noteMapper.selectById(note.getId());
            String previousAlbumId = oldNote != null ? oldNote.getAlbumId() : null;
            String newAlbumId = noteDTO.getAlbumId();

            // 如果专辑ID发生变化
            if (previousAlbumId != null && !previousAlbumId.equals(newAlbumId)) {
                // 删除旧的专辑-笔记关联
                albumNoteRelationMapper.delete(new QueryWrapper<WebAlbumNoteRelation>()
                        .eq("nid", note.getId())
                        .eq("aid", previousAlbumId));

                // 减少旧专辑的笔记数量
                if (!previousAlbumId.isEmpty()) {
                    WebAlbum oldAlbum = albumMapper.selectById(previousAlbumId);
                    if (oldAlbum != null && oldAlbum.getImgCount() != null && oldAlbum.getImgCount() > 0) {
                        oldAlbum.setImgCount(oldAlbum.getImgCount() - 1);
                        oldAlbum.setUpdateTime(new Date());
                        albumMapper.updateById(oldAlbum);
                    }
                }
            }

            // 如果设置了新专辑
            if (newAlbumId != null && !newAlbumId.isEmpty()) {
                // 检查是否已存在关联
                WebAlbumNoteRelation existingRelation = albumNoteRelationMapper.selectOne(
                        new QueryWrapper<WebAlbumNoteRelation>()
                                .eq("nid", note.getId())
                                .eq("aid", newAlbumId));

                if (existingRelation == null) {
                    // 创建新的专辑-笔记关联
                    WebAlbumNoteRelation albumNoteRelation = new WebAlbumNoteRelation();
                    albumNoteRelation.setAid(newAlbumId);
                    albumNoteRelation.setNid(note.getId());
                    albumNoteRelation.setCreateTime(new Date());
                    albumNoteRelation.setUpdateTime(new Date());
                    albumNoteRelationMapper.insert(albumNoteRelation);

                    // 增加新专辑的笔记数量
                    WebAlbum newAlbum = albumMapper.selectById(newAlbumId);
                    if (newAlbum != null) {
                        newAlbum.setImgCount((newAlbum.getImgCount() == null ? 0L : newAlbum.getImgCount()) + 1);
                        newAlbum.setUpdateTime(new Date());
                        albumMapper.updateById(newAlbum);
                    }
                }
            } else if (previousAlbumId != null && !previousAlbumId.isEmpty()) {
                // 如果新专辑ID为空，但之前有专辑，删除所有关联
                albumNoteRelationMapper.delete(new QueryWrapper<WebAlbumNoteRelation>()
                        .eq("nid", note.getId()));
            }
        } finally {
            // 删除封面临时文件
            if (coverLocalPath != null) {
                File coverFile = new File(coverLocalPath);
                if (coverFile.exists()) {
                    boolean deleted = coverFile.delete();
                    if (!deleted) {
                        log.warn("封面临时文件删除失败: {}", coverLocalPath);
                    }
                }
            }
            // 删除内容文件临时文件
            if (fileLocalPaths != null) {
                for (String path : fileLocalPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.warn("内容临时文件删除失败: {}", path);
                        }
                    }
                }
            }
        }
    }

    /**
     * 执行审核并设置状态（使用 CommonAuditService 统一审核）
     *
     * @return 审核结果，用于后续保存审核日志
     */
    private AutoAuditResult performAuditAndSetStatus(WebNote note, NoteDTO noteDTO, List<String> dataList) {
        try {
            // 检查内容审核是否开启
            if (!commonAuditService.isContentAuditEnabled()) {
                log.info("内容审核未开启，跳过审核");
                note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                return null;
            }

            // 获取审核模式
            AuditModeEnum auditMode = commonAuditService.getAuditMode();
            log.info("开始执行笔记审核 - 模式: {}, 笔记ID: {}", auditMode.getDescription(), note.getId());

            // 使用通用审核服务执行审核
            String contentType = "2".equals(noteDTO.getNoteType()) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                    noteDTO.getContent(), dataList, contentType, auditMode);

            // 设置审核状态
            commonAuditService.setAuditStatus(note, auditResult, auditMode);

            log.info("笔记审核完成 - 模式: {}, 结果: {}, 原因: {}",
                    auditMode.getDescription(), auditResult.getResult(), auditResult.getReason());

            return auditResult;

        } catch (Exception e) {
            log.error("笔记审核异常", e);
            // 审核异常时，设置为待人工审核
            note.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
            return null;
        }
    }

    /**
     * 异步审核笔记（不阻塞发布流程）
     * @param noteId 笔记ID
     * @param noteDTO 笔记DTO（用于获取笔记类型和内容）
     * @param dataList 图片/视频URL列表
     * @param auditMode 审核模式字符串（manual/auto/hybrid）
     */
    @Async
    public void auditNoteAsync(String noteId, NoteAppDTO noteDTO, List<String> dataList, String auditMode) {
        try {
            log.info("开始异步审核笔记 - 笔记ID: {}, 审核模式: {}", noteId, auditMode);
            
            // 查询笔记，添加重试机制以处理事务提交时序问题
            WebNote note = null;
            int maxRetries = 5; // 最多重试5次
            int retryDelay = 200; // 每次重试延迟200ms
            
            for (int i = 0; i < maxRetries; i++) {
                note = noteMapper.selectById(noteId);
                if (note != null) {
                    break; // 查询成功，退出循环
                }
                if (i < maxRetries - 1) {
                    // 如果不是最后一次重试，等待后继续
                    try {
                        Thread.sleep(retryDelay);
                        log.debug("笔记查询重试 {}/{} - 笔记ID: {}", i + 1, maxRetries, noteId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("重试等待被中断 - 笔记ID: {}", noteId, e);
                        return;
                    }
                }
            }
            
            if (note == null) {
                log.error("笔记不存在，无法审核 - 笔记ID: {} (已重试{}次)", noteId, maxRetries);
                return;
            }

            // 检查内容审核是否开启
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (!Boolean.TRUE.equals(auditEnabled)) {
                log.info("内容审核未开启，笔记直接通过 - 笔记ID: {}", noteId);
                note.setAuditStatus(AuditStatusEnum.PASS.getCode());
                noteMapper.updateById(note);
                try {
                    esNoteService.upsertNoteFromDbById(noteId);
                    log.info("笔记审核通过并同步到ES，笔记ID: {}", noteId);
                } catch (Exception e) {
                    log.error("笔记同步到ES失败，笔记ID: {}", noteId, e);
                }
                return;
            }

            // 获取审核模式枚举
            AuditModeEnum auditModeEnum = commonAuditService.getAuditMode();
            log.info("执行笔记审核 - 模式: {}, 笔记ID: {}", auditModeEnum.getDescription(), noteId);

            // 使用通用审核服务执行审核
            String contentType = (noteDTO.getNoteType() != null && "2".equals(noteDTO.getNoteType())) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                noteDTO.getContent(), dataList, contentType, auditModeEnum);

            // 设置审核状态
            commonAuditService.setAuditStatus(note, auditResult, auditModeEnum);
            
            // 更新笔记状态
            noteMapper.updateById(note);

            if (AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus())) {
                try {
                    esNoteService.upsertNoteFromDbById(noteId);
                    log.info("笔记审核通过并同步到ES，笔记ID: {}", noteId);
                } catch (Exception e) {
                    log.error("笔记同步到ES失败，笔记ID: {}", noteId, e);
                }
            } else {
                esNoteService.deleteNote(noteId);
            }

            // 保存审核日志
            if (auditResult != null) {
                commonAuditService.saveAuditLog(noteId, "note", auditResult);
            }

            log.info("笔记审核完成 - 模式: {}, 结果: {}, 原因: {}, 笔记ID: {}", 
                    auditModeEnum.getDescription(), 
                    auditResult != null ? auditResult.getResult() : "null", 
                    auditResult != null ? auditResult.getReason() : "null", 
                    noteId);

        } catch (Exception e) {
            log.error("异步审核笔记异常 - 笔记ID: {}", noteId, e);
            try {
                WebNote note = noteMapper.selectById(noteId);
                if (note != null) {
                    boolean wasPass = AuditStatusEnum.PASS.getCode().equals(note.getAuditStatus());
                    note.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
                    noteMapper.updateById(note);
                    if (wasPass) {
                        esNoteService.deleteNote(noteId);
                    }
                }
            } catch (Exception ex) {
                log.error("更新笔记状态失败 - 笔记ID: {}", noteId, ex);
            }
        }
    }
}
