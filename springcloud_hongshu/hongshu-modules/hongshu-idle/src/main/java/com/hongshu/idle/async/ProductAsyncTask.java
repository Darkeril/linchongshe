package com.hongshu.idle.async;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.ProductTypeEnum;
import com.hongshu.common.core.enums.TerminalTypeEnum;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.idle.domain.dto.ProductDTO;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.IdleUser;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.idle.IdleUserMapper;
import com.hongshu.idle.service.idle.IIdleEsProductService;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductAsyncTask {

    @Autowired
    private IdleUserMapper userMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private IIdleEsProductService esProductService;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private ISysSystemConfigService systemConfigService;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private CommonAuditService commonAuditService;
    
    // 文件上传专用线程池（用于并行上传）
    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(10, r -> {
        Thread t = new Thread(r);
        t.setName("product-upload-" + t.getId());
        t.setDaemon(true);
        return t;
    });


    @Async
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(String terminal, String currentUid, String noteData, String coverLocalPath, List<String> fileLocalPaths) {
        long totalStartTime = System.currentTimeMillis();
        try {
            log.info("========== 开始处理商品发布 ========== 用户ID: {}, 文件数量: {}", 
                currentUid, fileLocalPaths != null ? fileLocalPaths.size() : 0);
            
            // 更新用户商品数量
            long userUpdateStart = System.currentTimeMillis();
            IdleUser user = userMapper.selectById(currentUid);
            user.setProductCount(user.getProductCount() + 1);
            userMapper.updateById(user);
            long userUpdateTime = System.currentTimeMillis() - userUpdateStart;
            log.info("[性能监控] 更新用户商品数量耗时: {}ms", userUpdateTime);

            // 保存商品
            long productParseStart = System.currentTimeMillis();
            ProductDTO productDTO = JSONUtil.toBean(noteData, ProductDTO.class);
            IdleProduct product = ConvertUtils.sourceToTarget(productDTO, IdleProduct.class);
            product.setUid(currentUid);
            product.setFromType(TerminalTypeEnum.fromValue(terminal).getCode());
            product.setAuthor(user.getUsername());
            product.setDescription(productDTO.getContent());
            product.setProductType(productDTO.getProductType());
            // 初始化审核状态（先设置为待审核，后续根据审核模式处理）
            product.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
            product.setType(ProductTypeEnum.PEND_LIST.getCode());
            product.setCreator(user.getUsername());
            product.setCreateTime(new Date());
            product.setUpdateTime(new Date());
            long productParseTime = System.currentTimeMillis() - productParseStart;
            log.info("[性能监控] 解析商品数据耗时: {}ms", productParseTime);
            
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
                
                R<SysFile> upload;
                try {
                    upload = coverUploadFuture.get(); // 等待上传完成
                } catch (InterruptedException | ExecutionException e) {
                    log.error("封面上传等待异常", e);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("封面上传失败: " + e.getMessage(), e);
                }
                long coverUploadTime = System.currentTimeMillis() - coverUploadStart;
                log.info("[性能监控] 封面上传耗时: {}ms", coverUploadTime);
                
                if (upload == null || upload.getCode() != 200 || upload.getData() == null || upload.getData().getUrl() == null) {
                    String err = (upload == null) ? "远程服务无响应" : ("code=" + upload.getCode() + ", msg=" + upload.getMsg());
                    throw new RuntimeException("封面上传失败: " + err);
                }
                
                String noteCover = upload.getData().getUrl();
                log.info("封面上传成功，URL: {}", noteCover);
                product.setCover(noteCover);
                // 根据笔记类型处理文件
                List<String> dataList = new ArrayList<>();
                if (1 == product.getProductType()) {
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
                        try {
                            listR = batchUploadFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("批量上传等待异常", e);
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("批量上传失败: " + e.getMessage(), e);
                        }
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
                            .filter(response -> response.getCode() == 200 && response.getData() != null)
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
                } else if (2 == product.getProductType()) {
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
                    
                    R<SysFile> videoUpload;
                    try {
                        videoUpload = videoUploadFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("视频上传等待异常", e);
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("视频上传失败: " + e.getMessage(), e);
                    }
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
                product.setUrls(urls);
                
                // 先保存商品（状态设为审核中），然后异步审核
                Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
                if (Boolean.TRUE.equals(auditEnabled)) {
                    product.setAuditStatus(AuditStatusEnum.REVIEW.getCode()); // 先设为审核中
                } else {
                    product.setAuditStatus(AuditStatusEnum.PASS.getCode());
                    product.setType(ProductTypeEnum.ONLINE.getCode());
                }
                
                // 保存商品
                long dbInsertStart = System.currentTimeMillis();
                productMapper.insert(product);
                long dbInsertTime = System.currentTimeMillis() - dbInsertStart;
                log.info("[性能监控] 保存商品到数据库耗时: {}ms", dbInsertTime);
                
                // 如果审核未开启，商品直接通过，同步到ES
                String productId = product.getId();
                if (productId != null && !Boolean.TRUE.equals(auditEnabled)) {
                    try {
                        esProductService.applyProductEsIndexState(productId);
                        log.info("商品发布成功并同步到ES，商品ID: {}", productId);
                    } catch (Exception e) {
                        log.error("商品同步到ES失败，商品ID: {}", productId, e);
                    }
                } else if (productId != null && auditEnabled) {
                    // 异步执行审核（不阻塞保存流程）
                    auditProductAsync(productId, product.getDescription(), dataList, product.getProductType());
                }
            } catch (Exception e) {
                log.error("商品保存失败", e);
                throw new RuntimeException("商品保存失败: " + e.getMessage());
            }
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            log.info("========== 商品发布完成 ========== 总耗时: {}ms", totalTime);
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - totalStartTime;
            log.error("商品保存失败，总耗时: {}ms", totalTime, e);
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
    public void updateProduct(ProductDTO productDTO, IdleProduct product, String coverLocalPath, List<String> fileLocalPaths) {
        try {
            String previousAuditStatus = product.getAuditStatus();
            // 更新基本字段
            product.setTitle(productDTO.getTitle());
            product.setDescription(productDTO.getContent());
            product.setProductType(productDTO.getProductType());
            product.setPrice(productDTO.getPrice());
            product.setOriginalPrice(productDTO.getOriginalPrice());
            product.setAddress(productDTO.getAddress());
            product.setCpid(productDTO.getCpid());
            product.setCid(productDTO.getCid());
            product.setPostType(productDTO.getPostType());
            product.setUpdateTime(new Date());
            // 预先记录原始封面与媒体 URL，用于未上传新文件时复用
            String originalCover = product.getCover();
            String originalUrls = product.getUrls();

            // 处理封面：优先使用新上传的封面，其次使用 DTO 中传递的 URL，最后保留原封面
            if (coverLocalPath != null) {
                File coverFile = new File(coverLocalPath);
                long coverUploadStart = System.currentTimeMillis();
                // 使用 CompletableFuture 异步上传封面
                CompletableFuture<R<SysFile>> coverUploadFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return remoteFileService.upload(coverFile);
                    } catch (Exception e) {
                        log.error("封面上传异常", e);
                        throw new RuntimeException("封面上传失败: " + e.getMessage(), e);
                    }
                }, uploadExecutor);

                R<SysFile> upload;
                try {
                    upload = coverUploadFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("更新商品封面上传等待异常", e);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("封面上传失败: " + e.getMessage(), e);
                }
                long coverUploadTime = System.currentTimeMillis() - coverUploadStart;
                log.info("[性能监控] 更新商品封面上传耗时: {}ms", coverUploadTime);

                if (upload == null || upload.getCode() != 200 || upload.getData() == null || upload.getData().getUrl() == null) {
                    String err = (upload == null) ? "远程服务无响应" : ("code=" + upload.getCode() + ", msg=" + upload.getMsg());
                    throw new RuntimeException("封面上传失败: " + err);
                }
                String noteCover = upload.getData().getUrl();
                product.setCover(noteCover);
            } else if (StringUtils.isNotBlank(productDTO.getCover())) {
                // 前端可能直接传回已有或新上传的封面 URL
                product.setCover(productDTO.getCover());
            } else {
                // 未上传新封面且 DTO 中未提供，沿用原封面
                product.setCover(originalCover);
            }
            // 处理文件（图片或视频）
            List<String> dataList = new ArrayList<>();
            if (1 == productDTO.getProductType()) {
                // 图片类型
                if (fileLocalPaths != null && !fileLocalPaths.isEmpty()) {
                    List<File> files = fileLocalPaths.stream().map(File::new).collect(Collectors.toList());
                    // 批量上传图片 - 使用并行上传优化性能
                    long batchUploadStart = System.currentTimeMillis();
                    log.info("[性能监控] 开始批量上传图片（更新商品），图片数量: {}", files.size());

                    R<List<SysFile>> listR;
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
                        try {
                            listR = batchUploadFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("批量上传等待异常（更新商品）", e);
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("批量上传失败: " + e.getMessage(), e);
                        }
                    } else {
                        // 大批量：使用并行上传
                        log.info("[性能优化] 使用并行上传模式（更新商品），文件数量: {}", files.size());
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

                        CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                            uploadFutures.toArray(new CompletableFuture[0])
                        );
                        allUploads.join();

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

                        listR = R.ok(uploadedFiles);
                    }

                    long batchUploadTime = System.currentTimeMillis() - batchUploadStart;
                    log.info("[性能监控] 批量上传图片耗时（更新商品）: {}ms, 平均每张: {}ms",
                        batchUploadTime, files.size() > 0 ? batchUploadTime / files.size() : 0);
                    List<String> urls = Optional.ofNullable(listR)
                            .filter(response -> response.getCode() == 200)
                            .map(R::getData)
                            .filter(CollectionUtils::isNotEmpty)
                            .orElseThrow(() -> {
                                String errorMsg = listR != null ? listR.getMsg() : "远程服务无响应";
                                return new RuntimeException("图片上传失败: " + errorMsg);
                            })
                            .stream()
                            .map(SysFile::getUrl)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    dataList.addAll(urls);
                    product.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                } else if (productDTO.getUrls() != null && !productDTO.getUrls().isEmpty()) {
                    // 未上传新图片，优先使用前端传来的 URL 列表（前端已完成上传并处理删除/排序）
                    dataList.addAll(productDTO.getUrls());
                    product.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                } else if (StringUtils.isNotBlank(originalUrls)) {
                    // 兼容旧数据：前端未传 URLs，则沿用数据库中已有的 urls
                    List<String> existingUrls = JSONUtil.toList(originalUrls, String.class);
                    dataList.addAll(existingUrls);
                    product.setUrls(originalUrls);
                }
            } else if (2 == productDTO.getProductType()) {
                // 视频类型
                if (fileLocalPaths != null && !fileLocalPaths.isEmpty()) {
                    List<File> files = fileLocalPaths.stream().map(File::new).collect(Collectors.toList());
                    long videoUploadStart = System.currentTimeMillis();
                    log.info("[性能监控] 开始上传视频（更新商品），文件大小: {} bytes", files.get(0).length());

                    CompletableFuture<R<SysFile>> videoUploadFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            return remoteFileService.upload(files.get(0));
                        } catch (Exception e) {
                            log.error("视频上传异常", e);
                            throw new RuntimeException("视频上传失败: " + e.getMessage(), e);
                        }
                        }, uploadExecutor);

                    R<SysFile> videoUpload;
                    try {
                        videoUpload = videoUploadFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("视频上传等待异常（更新商品）", e);
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("视频上传失败: " + e.getMessage(), e);
                    }
                    long videoUploadTime = System.currentTimeMillis() - videoUploadStart;
                    log.info("[性能监控] 视频上传耗时（更新商品）: {}ms", videoUploadTime);

                    if (videoUpload == null || videoUpload.getCode() != 200 || videoUpload.getData() == null || videoUpload.getData().getUrl() == null) {
                        String err = (videoUpload == null) ? "远程服务无响应" : ("code=" + videoUpload.getCode() + ", msg=" + videoUpload.getMsg());
                        throw new RuntimeException("视频上传失败: " + err);
                    }
                    String videoResult = videoUpload.getData().getUrl();
                    if (videoResult != null) {
                        dataList.add(videoResult);
                        product.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                    } else {
                        throw new RuntimeException("视频上传失败");
                    }
                } else if (productDTO.getUrls() != null && !productDTO.getUrls().isEmpty()) {
                    // 未上传新视频，优先使用前端传来的 URL
                    dataList.addAll(productDTO.getUrls());
                    product.setUrls(JSONUtil.toJsonStr(dataList.toArray(new String[0])));
                } else if (StringUtils.isNotBlank(originalUrls)) {
                    // 兼容旧数据：沿用数据库中的 URL
                    List<String> existingUrls = JSONUtil.toList(originalUrls, String.class);
                    dataList.addAll(existingUrls);
                    product.setUrls(originalUrls);
                }
            } else {
                throw new RuntimeException("无效的商品类型");
            }
            // 先保存商品（状态设为审核中），然后异步审核
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (Boolean.TRUE.equals(auditEnabled)) {
                product.setAuditStatus(AuditStatusEnum.REVIEW.getCode()); // 先设为审核中
                if (AuditStatusEnum.PASS.getCode().equals(previousAuditStatus)) {
                    esProductService.deleteProduct(product.getId());
                }
            } else {
                product.setAuditStatus(AuditStatusEnum.PASS.getCode());
                product.setType(ProductTypeEnum.ONLINE.getCode());
            }
            
            // 更新数据库
            productMapper.updateById(product);
            
            // 如果审核未开启，商品直接通过，同步到ES
            String productId = product.getId();
            if (productId != null) {
                if (!Boolean.TRUE.equals(auditEnabled)) {
                    try {
                        esProductService.applyProductEsIndexState(productId);
                        log.info("商品更新成功并同步到ES，商品ID: {}", productId);
                    } catch (Exception e) {
                        log.error("商品同步到ES失败，商品ID: {}", productId, e);
                    }
                } else if (auditEnabled) {
                    // 异步执行审核（不阻塞更新流程）
                    auditProductAsync(productId, product.getDescription(), dataList, product.getProductType());
                }
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
     * 异步审核商品（不阻塞保存流程）
     * @param productId 商品ID
     * @param description 商品描述
     * @param dataList 图片/视频URL列表
     * @param productType 商品类型（1-图片，2-视频）
     */
    @Async
    public void auditProductAsync(String productId, String description, List<String> dataList, Integer productType) {
        try {
            log.info("开始异步审核商品 - 商品ID: {}", productId);
            
            // 查询商品
            IdleProduct product = productMapper.selectById(productId);
            if (product == null) {
                log.error("商品不存在，无法审核 - 商品ID: {}", productId);
                return;
            }

            // 检查审核是否开启
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (!Boolean.TRUE.equals(auditEnabled)) {
                log.info("内容审核未开启，商品直接通过 - 商品ID: {}", productId);
                product.setAuditStatus(AuditStatusEnum.PASS.getCode());
                product.setType(ProductTypeEnum.ONLINE.getCode());
                productMapper.updateById(product);
                try {
                    esProductService.applyProductEsIndexState(productId);
                    log.info("商品审核通过并同步到ES，商品ID: {}", productId);
                } catch (Exception e) {
                    log.error("商品同步到ES失败，商品ID: {}", productId, e);
                }
                return;
            }

            // 获取审核模式
            AuditModeEnum auditMode = commonAuditService.getAuditMode();
            log.info("执行商品审核 - 模式: {}, 商品ID: {}", auditMode.getDescription(), productId);

            // 使用通用审核服务执行审核
            String contentType = (productType != null && productType == 2) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                description, dataList, contentType, auditMode);

            // 设置审核状态
            commonAuditService.setAuditStatus(product, auditResult, auditMode);

            // 更新商品状态
            productMapper.updateById(product);

            try {
                esProductService.applyProductEsIndexState(productId);
                log.info("商品审核结束，已按库表状态同步 ES，商品ID: {}", productId);
            } catch (Exception e) {
                log.error("商品同步到ES失败，商品ID: {}", productId, e);
            }

            // 保存审核日志
            if (auditResult != null) {
                commonAuditService.saveAuditLog(productId, "product", auditResult);
            }

            log.info("商品审核完成 - 模式: {}, 结果: {}, 原因: {}, 商品ID: {}", 
                    auditMode.getDescription(), 
                    auditResult != null ? auditResult.getResult() : "null", 
                    auditResult != null ? auditResult.getReason() : "null", 
                    productId);

        } catch (Exception e) {
            log.error("异步审核商品异常 - 商品ID: {}", productId, e);
            try {
                // 审核异常时，设置为待人工审核
                IdleProduct product = productMapper.selectById(productId);
                if (product != null) {
                    product.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
                    product.setType(ProductTypeEnum.PEND_LIST.getCode());
                    productMapper.updateById(product);
                }
            } catch (Exception ex) {
                log.error("更新商品状态失败 - 商品ID: {}", productId, ex);
            }
        }
    }

    /**
     * 执行审核并设置状态（已废弃，改为异步审核）
     * @return 审核结果，用于后续保存审核日志
     */
    @Deprecated
    private AutoAuditResult performAuditAndSetStatus(IdleProduct product, List<String> dataList) {
        try {
            // 检查审核是否开启
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (!Boolean.TRUE.equals(auditEnabled)) {
                // 审核未开启，直接设置为通过
                product.setAuditStatus(AuditStatusEnum.PASS.getCode());
                product.setType(ProductTypeEnum.ONLINE.getCode());
                log.info("闲置商品审核未开启，商品直接上线");
                return null;
            }
            
            // 获取审核模式
            AuditModeEnum auditMode = commonAuditService.getAuditMode();
            log.info("开始执行闲置商品审核 - 模式: {}, 商品ID: {}", auditMode.getDescription(), product.getId());
            
            // 使用通用审核服务执行审核
            String contentType = (product.getProductType() == 2) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                product.getDescription(), dataList, contentType, auditMode);
            
            // 设置审核状态
            commonAuditService.setAuditStatus(product, auditResult, auditMode);

            log.info("闲置商品审核完成 - 模式: {}, 结果: {}, 原因: {}", 
                    auditMode.getDescription(), auditResult.getResult(), auditResult.getReason());
            
            return auditResult;

        } catch (Exception e) {
            log.error("闲置商品审核异常", e);
            // 审核异常时，设置为待人工审核
            product.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
            product.setType(ProductTypeEnum.PEND_LIST.getCode());
            return null;
        }
    }

    /**
     * 按数据库最新行同步 ES（供非异步任务入口调用，如用户端发布）。
     */
    public void syncProductToEs(IdleProduct product) {
        if (product == null || product.getId() == null) {
            return;
        }
        esProductService.applyProductEsIndexState(product.getId());
    }
}
