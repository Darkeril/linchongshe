package com.hongshu.idle.service.sys.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.constant.Constantss;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.ProductEnum;
import com.hongshu.common.core.enums.ProductTypeEnum;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.global.SysConf;
import com.hongshu.common.core.utils.*;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.idle.domain.Query;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.WebUser;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.sys.SysIdleCategoryMapper;
import com.hongshu.idle.mapper.web.WebUserMapper;
import com.hongshu.idle.service.idle.IIdleEsProductService;
import com.hongshu.idle.service.sys.ISysIdleProductService;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.idle.async.ProductAsyncTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 闲宝信息 服务层处理
 */
@Slf4j
@Service
public class SysIdleProductServiceImpl implements ISysIdleProductService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IIdleEsProductService esProductService;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private SysIdleCategoryMapper idleCategoryMapper;
    @Autowired
    private CommonAuditService commonAuditService;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private ProductAsyncTask productAsyncTask;


    /**
     * 查询笔记信息集合
     */
    @Override
    public List<IdleProduct> getAllProductList() {
        QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
        // 审核通过的商品，且未删除
        qw.lambda().like(IdleProduct::getAuditStatus, 1).eq(IdleProduct::getDeleted, 0);
        return productMapper.selectList(qw);
    }

    /**
     * 查询笔记信息集合
     *
     * @param query 笔记信息
     */
    @Override
    public List<IdleProduct> selectProductList(Query query) {
        QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
        // 只查询未删除的商品
        qw.lambda().eq(IdleProduct::getDeleted, 0);
        qw.lambda().like(ValidatorUtil.isNotNull(query.getTitle()), IdleProduct::getTitle, query.getTitle());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getUid()), IdleProduct::getUid, query.getUid());

        // 处理作者筛选
        String author = (String) query.get("author");
        if (StringUtils.isNotBlank(author)) {
            QueryWrapper<WebUser> userQw = new QueryWrapper<>();
            userQw.lambda().like(WebUser::getUsername, author);
            List<WebUser> users = userMapper.selectList(userQw);
            if (users.isEmpty()) {
                return new ArrayList<>();
            }
            List<String> userIds = users.stream()
                    .map(WebUser::getId)
                    .collect(Collectors.toList());
            qw.lambda().in(IdleProduct::getUid, userIds);
        }

        // 处理类型筛选（支持 type 和 productType）
        Object productType = query.get("productType");
        if (productType == null) {
            productType = query.get("type");
        }
        qw.lambda().eq(ValidatorUtil.isNotNull(productType), IdleProduct::getProductType, productType);

        // 处理审核状态
        qw.lambda().eq(ValidatorUtil.isNotNull(query.get("auditStatus")), IdleProduct::getAuditStatus, query.get("auditStatus"));

        // 处理分类筛选（支持 pid 和 cpid）
        Object cpid = query.get("cpid");
        if (cpid == null) {
            cpid = query.get("pid");
        }
        qw.lambda().eq(ValidatorUtil.isNotNull(cpid), IdleProduct::getCpid, cpid);

        // 处理来源筛选
        qw.lambda().eq(ValidatorUtil.isNotNull(query.get("fromType")), IdleProduct::getFromType, query.get("fromType"));

        // 处理 beginTime 和 endTime
        String beginTimeStr = (String) query.get("params[beginTime]");
        String endTimeStr = (String) query.get("params[endTime]");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginTimeStr != null) {
            try {
                // 将字符串转为 Date
                Date beginTime = sdf.parse(beginTimeStr);
                // 添加大于等于条件
                qw.lambda().ge(IdleProduct::getCreateTime, beginTime);
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
                qw.lambda().le(IdleProduct::getCreateTime, endTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 处理排序
        String orderByColumn = (String) query.get("orderByColumn");
        String isAsc = (String) query.get("isAsc");
        if (StringUtils.isNotBlank(orderByColumn)) {
            if ("createTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(IdleProduct::getCreateTime);
                } else {
                    qw.lambda().orderByDesc(IdleProduct::getCreateTime);
                }
            } else if ("updateTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(IdleProduct::getUpdateTime);
                } else {
                    qw.lambda().orderByDesc(IdleProduct::getUpdateTime);
                }
            } else {
                qw.lambda().orderByDesc(IdleProduct::getUpdateTime);
            }
        } else {
            qw.lambda().orderByDesc(IdleProduct::getUpdateTime);
        }

        return productMapper.selectList(qw);
    }

    /**
     * 获取未审核笔记列表
     */
    @Override
    public List<IdleProduct> selectUnAuditProductList(Query query) {
        QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
        // 只查询未删除的商品
        qw.lambda().eq(IdleProduct::getDeleted, 0);
        qw.lambda().like(ValidatorUtil.isNotNull(query.getTitle()), IdleProduct::getTitle, query.getTitle());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getUid()), IdleProduct::getUid, query.getUid());
        qw.lambda().like(ValidatorUtil.isNotNull(query.get("type")), IdleProduct::getType, query.get("type"));
        qw.lambda().like(ValidatorUtil.isNull(query.get("auditStatus")), IdleProduct::getAuditStatus, 0);
        // 处理 beginTime 和 endTime
        String beginTimeStr = (String) query.get("params[beginTime]");
        String endTimeStr = (String) query.get("params[endTime]");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginTimeStr != null) {
            try {
                // 将字符串转为 Date
                Date beginTime = sdf.parse(beginTimeStr);
                // 添加大于等于条件
                qw.lambda().ge(IdleProduct::getCreateTime, beginTime);
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
                qw.lambda().le(IdleProduct::getCreateTime, endTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 排序
        qw.lambda().orderByDesc(IdleProduct::getCreateTime);
        return productMapper.selectList(qw);
    }

    /**
     * 通过笔记ID查询笔记信息
     *
     * @param id 笔记ID
     */
    @Override
    public IdleProduct selectProductById(Long id) {
        QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
        qw.lambda().eq(IdleProduct::getId, id).eq(IdleProduct::getDeleted, 0);
        return productMapper.selectOne(qw);
    }

    /**
     * 新增笔记信息
     *
     * @param product 笔记信息
     */
    @Override
    public int insertProduct(IdleProduct product, MultipartFile file) {
        // 上传头像
        if (ObjectUtils.isNotEmpty(file)) {
//            String productCover = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String productCover = upload.getData().getUrl();
            product.setCover(productCover);
        }
        product.setCreator("System");
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        return productMapper.insert(product);
    }

    /**
     * 修改保存笔记信息
     *
     * @param product 笔记信息
     * @param file 封面文件
     * @param files 商品图片/视频文件数组（管理后台暂不支持批量上传，此参数保留用于未来扩展）
     */
    @Override
    public int updateProduct(IdleProduct product, MultipartFile file, MultipartFile[] files) {
        // 上传封面
        if (ObjectUtils.isNotEmpty(file)) {
//            String productCover = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String productCover = upload.getData().getUrl();
            product.setCover(productCover);
        }
        
        // 管理后台的商品更新逻辑：直接使用前端传来的 urls（前端已上传好的 URL）
        // urls 和 count 字段已经在 product 对象中，直接更新到数据库即可
        // 如果前端传了 urls JSON 字符串，解析并设置 count
        if (StringUtils.isNotBlank(product.getUrls())) {
            try {
                List<String> urlList = JSONUtil.toList(product.getUrls(), String.class);
                if (urlList != null && !urlList.isEmpty()) {
                    product.setCount(urlList.size());
                } else {
                    product.setCount(0);
                }
            } catch (Exception e) {
                log.warn("解析商品图片 URL 失败: {}", e.getMessage());
                // 如果解析失败，保持原有 count 值
            }
        } else {
            // 如果没有 urls，count 设为 0
            product.setCount(0);
        }
        
        // 查询原商品的审核状态
        IdleProduct oldProduct = productMapper.selectById(product.getId());
        boolean needReaudit = false;
        if (oldProduct != null) {
            String oldAuditStatus = oldProduct.getAuditStatus();
            // 如果原商品是未审核（0）或已驳回（2）状态，修改后需要重新审核
            if ("0".equals(oldAuditStatus) || "2".equals(oldAuditStatus)) {
                needReaudit = true;
                product.setAuditStatus("0"); // 重新设置为未审核状态，等待审核
                log.info("商品ID:{} 原状态:{} 修改后重新提交审核", product.getId(), oldAuditStatus);
            }
        }
        
        product.setUpdater("System");
        product.setUpdateTime(new Date());
        
        // 如果需要重新审核，执行自动审核流程（参考用户端商品更新逻辑）
        if (needReaudit) {
            try {
                // 解析图片/视频URL列表
                List<String> dataList = new ArrayList<>();
                if (StringUtils.isNotBlank(product.getUrls())) {
                    try {
                        List<String> urlList = JSONUtil.toList(product.getUrls(), String.class);
                        if (urlList != null && !urlList.isEmpty()) {
                            dataList.addAll(urlList);
                        }
                    } catch (Exception e) {
                        log.warn("解析商品图片 URL 失败: {}", e.getMessage());
                    }
                }
                
                // 先保存商品（状态设为审核中），然后异步审核
                product.setAuditStatus("0"); // 设为待审核
                productMapper.updateById(product);
                
                // 异步执行审核（不阻塞更新流程）
                String productId = product.getId();
                if (productId != null) {
                    productAsyncTask.auditProductAsync(productId, product.getDescription(), dataList, product.getProductType());
                }
            } catch (Exception e) {
                log.error("商品自动审核异常，商品ID: {}", product.getId(), e);
                // 审核异常时，保持为待审核状态
                product.setAuditStatus("0");
            }
        }

        int rows = productMapper.updateById(product);
        if (rows > 0 && product.getId() != null && !needReaudit) {
            try {
                esProductService.applyProductEsIndexState(product.getId());
            } catch (Exception e) {
                log.error("管理端更新商品后同步 ES 失败, productId={}", product.getId(), e);
            }
        }
        return rows;
    }
    
    /**
     * 执行审核并设置状态（使用 CommonAuditService 统一审核）
     * 参考 ProductAsyncTask.performAuditAndSetStatus 方法
     * @param product 商品对象
     * @param dataList 图片/视频URL列表
     */
    private void performAuditAndSetStatus(IdleProduct product, List<String> dataList) {
        try {
            // 检查审核是否开启
            Boolean auditEnabled = commonAuditService.isContentAuditEnabled();
            if (!Boolean.TRUE.equals(auditEnabled)) {
                // 审核未开启，直接设置为通过
                product.setAuditStatus(AuditStatusEnum.PASS.getCode());
                product.setType(ProductTypeEnum.ONLINE.getCode());
                log.info("闲置商品审核未开启，商品直接上线，商品ID: {}", product.getId());
                return;
            }
            
            // 获取审核模式
            AuditModeEnum auditMode = commonAuditService.getAuditMode();
            log.info("开始执行闲置商品审核 - 模式: {}, 商品ID: {}", auditMode.getDescription(), product.getId());
            
            // 使用通用审核服务执行审核
            String contentType = (product.getProductType() != null && product.getProductType() == 2) ? "video" : "image";
            AutoAuditResult auditResult = commonAuditService.performAuditWithMode(
                product.getDescription(), dataList, contentType, auditMode);
            
            // 设置审核状态
            commonAuditService.setAuditStatus(product, auditResult, auditMode);

            log.info("闲置商品审核完成 - 模式: {}, 结果: {}, 原因: {}, 商品ID: {}", 
                    auditMode.getDescription(), auditResult.getResult(), auditResult.getReason(), product.getId());

        } catch (Exception e) {
            log.error("闲置商品审核异常，商品ID: {}", product.getId(), e);
            // 审核异常时，设置为待人工审核
            product.setAuditStatus(AuditStatusEnum.REVIEW.getCode());
            product.setType(ProductTypeEnum.PEND_LIST.getCode());
        }
    }

    /**
     * 批量删除笔记信息
     *
     * @param ids 需要删除的笔记ID
     */
    @Override
    public int deleteProductByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
            qw.lambda().eq(IdleProduct::getId, id).eq(IdleProduct::getDeleted, 0);
            IdleProduct product = productMapper.selectOne(qw);
            if (ValidatorUtil.isNull(product)) {
                log.info("商品不存在或已删除:{}", id);
                continue;
            }
            // 逻辑删除：更新 deleted 字段为 1
            product.setDeleted(1);
            product.setUpdateTime(new Date());
            int result = productMapper.updateById(product);
            if (result > 0) {
                count++;
                try {
                    esProductService.deleteProduct(String.valueOf(id));
                } catch (Exception e) {
                    log.error("逻辑删除商品后同步删除 ES 失败, id={}", id, e);
                }
            }
        }
        return count;
    }

    /**
     * 审核管理
     *
     * @param productId 笔记ID
     * @param auditType 审核状态
     */
    @Override
    public void auditProduct(String productId, String auditType) {
        QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
        qw.lambda().eq(IdleProduct::getId, productId).eq(IdleProduct::getDeleted, 0);
        IdleProduct product = productMapper.selectOne(qw);
        if (ObjectUtil.isEmpty(product)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 通过：1
        if ("pass".equals(auditType)) {
            product.setAuditStatus(AuditStatusEnum.PASS.getCode());
        }
        // 拒绝：2
        if ("reject".equals(auditType)) {
            product.setAuditStatus(AuditStatusEnum.REJECT.getCode());
        }
        product.setUpdateTime(new Date());
        productMapper.updateById(product);
        try {
            esProductService.applyProductEsIndexState(productId);
        } catch (Exception e) {
            log.error("审核后同步 ES 失败, productId={}", productId, e);
        }
    }

    /**
     * 批量增加商品
     */
    @Override
    public void addProductBulkData() {
        QueryWrapper<IdleProduct> qw = new QueryWrapper<>();
        // 只过滤未删除的商品，不限制status（与/product/list接口保持一致）
        // 只导入审核通过的商品到ES
        qw.eq("deleted", 0)
                .eq("audit_status", AuditStatusEnum.PASS.getCode());
        List<IdleProduct> productList = productMapper.selectList(qw);
        List<IdleProductVO> productVOList = DozerUtil.convertor(productList, IdleProductVO.class);
        for (IdleProductVO productVO : productVOList) {
            // 确保关键字段不为null，避免ES字段类型推断错误
            if (productVO.getProductType() == null) {
                productVO.setProductType(1); // 默认为图文类型
            }
            if (productVO.getStatus() == null) {
                productVO.setStatus(0); // 默认为上线状态
            }
            if (productVO.getAuditStatus() == null) {
                productVO.setAuditStatus(AuditStatusEnum.PASS.getCode()); // 默认为审核通过
            }
            // 设置时间字段（如果为空）
            if (productVO.getTime() == 0 && productVO.getUpdateTime() != null) {
                productVO.setTime(productVO.getUpdateTime().getTime());
            } else if (productVO.getTime() == 0 && productVO.getCreateTime() != null) {
                productVO.setTime(productVO.getCreateTime().getTime());
            }

            QueryWrapper<WebUser> userQw = new QueryWrapper<>();
            userQw.eq("id", productVO.getUid()).eq("deleted", 0);
            WebUser user = userMapper.selectOne(userQw);
            if (user != null) {
                productVO.setAvatar(user.getAvatar());
                productVO.setUsername(user.getUsername());
            }
        }
        try {
            List<BulkOperation> result = new ArrayList<>();
            for (IdleProductVO productVO : productVOList) {
                result.add(new BulkOperation.Builder().create(d -> d.document(productVO).id(productVO.getId()).index(NoteConstant.PRODUCT_INDEX)).build());
            }
            BulkResponse bulkResponse = elasticsearchClient.bulk(e -> e.index(NoteConstant.PRODUCT_INDEX).operations(result));
            log.info("批量导入商品到ES完成，共导入 {} 条数据", productVOList.size());
            if (bulkResponse.errors()) {
                log.warn("批量导入存在错误: {}", bulkResponse.toString());
            }
        } catch (Exception e) {
            log.error("批量导入商品到ES失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 清空笔记
     */
    @Override
    public void delProductBulkData() {
        try {
            // 检查索引是否存在
            if (elasticsearchClient.indices().exists(existsRequest -> existsRequest.index(NoteConstant.PRODUCT_INDEX)).value()) {
                // 删除索引
                DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.of(builder -> builder.index(NoteConstant.PRODUCT_INDEX));
                elasticsearchClient.indices().delete(deleteIndexRequest);
                log.info("成功删除索引: {}", NoteConstant.PRODUCT_INDEX);
            } else {
                log.info("索引 {} 不存在，无需删除", NoteConstant.PRODUCT_INDEX);
            }
            // 重新创建索引
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(builder -> builder.index(NoteConstant.PRODUCT_INDEX));
            elasticsearchClient.indices().create(createIndexRequest);
            log.info("成功创建索引: {}", NoteConstant.PRODUCT_INDEX);
        } catch (Exception e) {
            log.error("Error occurred while deleting and recreating index: {}", NoteConstant.PRODUCT_INDEX, e);
        }
    }

    /**
     * 重置
     */
    @Override
    public void refreshProductData() {
        this.delProductBulkData();
        this.addProductBulkData();
    }

    @Override
    public List<Map<String, Object>> getProductCountByCategory() {
        try {
            // 1. 获取分类统计数据
            List<Map<String, Object>> categoryStats = productMapper.getProductByCategory();
            if (categoryStats.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 提取分类ID并获取分类数量
            Map<String, Integer> categoryCountMap = categoryStats.stream().collect(Collectors.toMap(item -> String.valueOf(item.get("cpid")), item -> Optional.ofNullable(item.get(SysConf.COUNT)).map(num -> ((Number) num).intValue()).orElse(0)));

            // 3. 批量查询分类信息
            List<IdleCategory> categories = idleCategoryMapper.selectBatchIds(categoryCountMap.keySet());
            if (categories.isEmpty()) {
                return Collections.emptyList();
            }

            // 4. 组装最终结果
            return categories.stream().filter(category -> StringUtilss.isNotEmpty(category.getTitle())).map(category -> {
                String categoryId = String.valueOf(category.getId());
                Map<String, Object> result = new HashMap<>(3);
                result.put(SysConf.BLOG_SORT_UID, categoryId);
                result.put(SysConf.NAME, category.getTitle());
                result.put(SysConf.VALUE, categoryCountMap.getOrDefault(categoryId, 0));
                return result;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取分类商品统计失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Integer getProductCount() {
        QueryWrapper<IdleProduct> queryWrapper = new QueryWrapper<>();
        // 添加逻辑删除过滤条件：只统计未删除的商品
        queryWrapper.eq("deleted", 0);
        // 如果需要根据状态过滤，可以添加：
        // queryWrapper.eq(BaseSQLConf.STATUS, EStatus.ENABLE);
        return Math.toIntExact(productMapper.selectCount(queryWrapper));
    }

    @Override
    public List<Map<String, Object>> getProductCountByType() {
        List<Map<String, Object>> productByType = productMapper.getProductCountByType();
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 统计现有数据
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (Map<String, Object> item : productByType) {
            String productType = String.valueOf(item.get("product_type"));
            Number count = (Number) item.get(SysConf.COUNT);
            typeCountMap.put(productType, count != null ? count.intValue() : 0);
        }

        // 遍历所有笔记类型，确保包含所有类型的统计
        for (ProductEnum type : ProductEnum.values()) {
            Map<String, Object> resultItem = new HashMap<>();
            resultItem.put(SysConf.BLOG_SORT_UID, type.getCode());
            resultItem.put(SysConf.NAME, type.getDesc());
            resultItem.put(SysConf.VALUE, typeCountMap.getOrDefault(type.getCode(), 0));
            resultList.add(resultItem);
        }
        return resultList;
    }

    @Override
    public Map<String, Object> getProductContributeCount() {
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
        List<Map<String, Object>> productContributeMap = productMapper.getProductContributeCount(startTime, endTime);
        List<String> dateList = DateUtilss.getDayBetweenDates(startTime, endTime);
        Map<String, Object> dateMap = new HashMap<>();
        for (Map<String, Object> itemMap : productContributeMap) {
            dateMap.put(itemMap.get("DATE").toString(), itemMap.get("COUNT"));
        }

        List<List<Object>> resultList = new ArrayList<>();
        for (String item : dateList) {
            int count = 0;
            if (dateMap.get(item) != null) {
                count = Integer.parseInt(dateMap.get(item).toString());
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
        resultMap.put(SysConf.GOODS_CONTRIBUTE_COUNT, resultList);
        // 将 全年商品贡献度 存入到Redis【过期时间2小时】
//        redisUtil.setEx(RedisConf.DASHBOARD + Constantss.SYMBOL_COLON + RedisConf.BLOG_CONTRIBUTE_COUNT, JsonUtils.objectToJson(resultMap), 2, TimeUnit.HOURS);
        return resultMap;
    }
}
