package com.hongshu.web.service.web.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.web.domain.dto.AmapConfigDTO;
import com.hongshu.web.domain.dto.IpStatDTO;
import com.hongshu.web.domain.dto.LocationInfo;
import com.hongshu.web.domain.dto.UserAppDTO;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.domain.vo.IdleProductVO;
import com.hongshu.web.domain.vo.UserLocationVO;
import com.hongshu.web.mapper.idle.IdleCollectionMapper;
import com.hongshu.web.mapper.idle.IdlePaymentOrderMapper;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.idle.IdleProductOrderMapper;
import com.hongshu.web.mapper.web.WebLikeOrCollectionMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.manager.AsyncManager;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.service.web.IWebEsNoteService;
import com.hongshu.web.service.web.IWebUserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class WebUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IWebUserService {

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private IdleProductOrderMapper productOrderMapper;
    @Autowired
    private IdlePaymentOrderMapper payOrderMapper;
    @Autowired
    private IdleCollectionMapper collectionMapper;
    @Autowired
    private WebLikeOrCollectionMapper likeOrCollectionMapper;
    @Autowired
    private IWebEsNoteService esNoteService;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private ISysSystemConfigService systemConfigService;


    private Searcher searcher;

    /**
     * 异步刷新用户相关的 ES 笔记数据，避免阻塞前端请求
     */
    private void asyncRefreshEsNoteData() {
        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                try {
                    esNoteService.refreshNoteData();
                } catch (Exception e) {
                    log.error("异步刷新 ES 笔记数据失败", e);
                }
            }
        });
    }
    //    @Autowired
//    private AmapConfig amapConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String CITY_LOCATION_CACHE_PREFIX = "city:location:";
    private static final long CACHE_EXPIRE_DAYS = 30; // 缓存30天
    /** 整表聚合结果快照，减轻重复打开地图时的 DB+高德压力 */
    private static final String USER_LOCATIONS_SNAPSHOT_KEY = "dashboard:userLocations:snapshot";
    private static final long USER_LOCATIONS_SNAPSHOT_TTL_SEC = 300L;
    private static final int GEOCODE_PARALLELISM = 4;
    private static final OkHttpClient AMAP_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build();
    // 添加限流器
    private final RateLimiter rateLimiter = RateLimiter.create(5.0); // 每秒最多5个请求


    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    @Override
    public Page<NoteSearchVo> getTrendByUser(long currentPage, long pageSize, String userId, Integer type, String status) {
        Page<NoteSearchVo> resultPage;
        if (type == 1) {
            resultPage = this.getLikeOrCollectionPageByUser(currentPage, pageSize, userId, status);
        } else {
            resultPage = this.getLikeOrCollectionPageByUser(currentPage, pageSize, userId, type);
        }
        return resultPage;
    }

    /**
     * 根据用户获取闲置商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    @Override
    public Page<IdleProductVO> getProductByUser(long currentPage, long pageSize, String userId, Integer type, String status) {
        if (type == 4 || type == 0) {
            switch (status) {
                case "publish":
                    return this.getProductPageByUser(currentPage, pageSize, userId, 0);
                case "sell":
                    return this.getProductPageByUser(currentPage, pageSize, userId, 1);
                case "buy":
                    return this.getProductPageByUser(currentPage, pageSize, userId, 2);
                case "trend":
                    return this.getProductPageByUser(currentPage, pageSize, userId, 3);
                default:
                    return new Page<>();
            }
        } else {
            return "collect".equals(status)
                    ? this.getCollectionPageByUser(currentPage, pageSize, userId, type)
                    : new Page<>();
        }
    }

    @Override
    public WebUser getUserById(String userId) {
        WebUser user = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(user)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        String address = user.getAddress();
        if (address != null && address.contains(" ")) {
            address = address.substring(address.indexOf(" ") + 1);
        } else {
            address = "未知";
        }
        user.setAddress(address);
        long noteCnt = noteMapper.selectCount(
                new QueryWrapper<WebNote>().eq("uid", userId).eq("deleted", 0));
        user.setNoteCount(noteCnt);
        long productCnt = productMapper.selectCount(
                new QueryWrapper<IdleProduct>().eq("uid", userId).eq("deleted", 0));
        user.setProductCount(productCnt);
        return user;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    @Override
    public WebUser updateUser(MultipartFile avatarFile, WebUser user) {
        // 如果有上传新头像，先处理文件上传
        if (avatarFile != null && !avatarFile.isEmpty()) {
//            String avatarUrl = ossService.upload(avatarFile);
            R<SysFile> upload = remoteFileService.upload(avatarFile);
            String avatarUrl = upload.getData().getUrl();
            user.setAvatar(avatarUrl);
        }
        WebUser webUser = userMapper.selectById(user.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setAvatar(user.getAvatar());
        webUser.setUsername(user.getUsername());
        webUser.setDescription(user.getDescription());
        webUser.setTags(user.getTags());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateAvatarUrl(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setAvatar(userAppDTO.getAvatarUrl());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateUsername(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setUsername(userAppDTO.getUsername());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateDes(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setDescription(userAppDTO.getDescription());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateTags(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setTags(userAppDTO.getTags());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateBackgroundImage(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setUserCover(userAppDTO.getUserCover());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateAvatar(MultipartFile file, String userId) {
        WebUser webUser = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 上传文件
        R<SysFile> upload = remoteFileService.upload(file);
        if (upload.getCode() != 200 || upload.getData() == null) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        String avatarUrl = upload.getData().getUrl();
        // 更新用户头像
        webUser.setAvatar(avatarUrl);
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateBackgroundImageFile(MultipartFile file, String userId) {
        WebUser webUser = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 上传文件
        R<SysFile> upload = remoteFileService.upload(file);
        if (upload.getCode() != 200 || upload.getData() == null) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        String userCover = upload.getData().getUrl();
        // 更新用户背景图
        webUser.setUserCover(userCover);
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateSex(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setGender(userAppDTO.getGender());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateBirthday(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setBirthday(userAppDTO.getBirthday());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    @Override
    public WebUser updateArea(UserAppDTO userAppDTO) {
        WebUser webUser = userMapper.selectById(userAppDTO.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        webUser.setAddress(userAppDTO.getAddress());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        this.asyncRefreshEsNoteData();

        return webUser;
    }

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     * @return
     */
    @Override
    public Page<WebUser> getUserByKeyword(long currentPage, long pageSize, String keyword) {
        Page<WebUser> resultPage;
        resultPage = userMapper.selectPage(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<WebUser>().like("username", keyword));
        return resultPage;
    }

    /**
     * 保存用户的搜索记录
     *
     * @param keyword 关键词
     */
    @Override
    public void saveUserSearchRecord(String keyword) {
    }

    /**
     * 当前用户收藏的商品
     */
    private Page<IdleProductVO> getCollectionPageByUser(long currentPage, long pageSize, String userId, Integer type) {
        // 1. 参数校验
        if (StringUtils.isBlank(userId)) {
            return new Page<>();
        }

        // 2. 查询收藏记录（翻页时跳过 COUNT，减轻 DB 压力）
        Page<IdleCollection> mpColPage = new Page<>(currentPage, pageSize);
        if (currentPage > 1) {
            mpColPage.setSearchCount(false);
        }
        Page<IdleCollection> collectionPage = collectionMapper.selectPage(
                mpColPage,
                new LambdaQueryWrapper<IdleCollection>()
                        .eq(IdleCollection::getUid, userId)
                        .eq(IdleCollection::getType, 1)
                        .orderByDesc(IdleCollection::getUpdateTime)
        );

        List<IdleCollection> collections = collectionPage.getRecords();
        if (CollectionUtil.isEmpty(collections)) {
            return new Page<>();
        }

        // 3. 获取商品ID列表
        List<String> productIds = collections.stream()
                .map(IdleCollection::getCollectionId)
                .collect(Collectors.toList());

        // 4. 批量查询商品
        List<IdleProduct> products = productMapper.selectList(
                new LambdaQueryWrapper<IdleProduct>()
                        .in(IdleProduct::getId, productIds)
        );

        // 5. 构建商品映射
        Map<String, IdleProduct> productMap = products.stream()
                .collect(Collectors.toMap(IdleProduct::getId, Function.identity()));

        // 6. 批量查询用户信息 - 使用不可变变量
        final Set<String> userIds = products.stream()
                .map(IdleProduct::getUid)
                .collect(Collectors.toSet());

        final Map<String, WebUser> userMap;
        if (!CollectionUtils.isEmpty(userIds)) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(WebUser::getId, Function.identity()));
        } else {
            userMap = Collections.emptyMap();
        }

        // 7. 构建VO列表 - 使用final/effectively final变量
        List<IdleProductVO> productVOList = collections.stream()
                .map(collection -> {
                    final IdleProduct product = productMap.get(collection.getCollectionId());
                    if (product == null) {
                        return null;
                    }

                    final IdleProductVO vo = ConvertUtils.sourceToTarget(product, IdleProductVO.class);
                    final WebUser publisher = userMap.get(product.getUid());
                    if (publisher != null) {
                        vo.setUsername(publisher.getUsername())
                                .setAvatar(publisher.getAvatar());
                    }
                    vo.setIsCollection(true)
                            .setTime(collection.getUpdateTime().getTime());
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 8. 返回结果
        return new Page<IdleProductVO>()
                .setRecords(productVOList)
                .setTotal(collectionPage.getTotal());
    }

    /**
     * 当前用户发布的商品
     */
    private Page<IdleProductVO> getProductPageByUser(long currentPage, long pageSize, String userId, Integer status) {

        QueryWrapper<IdleProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("update_time")
                .eq("deleted", 0);

        // 根据状态设置不同的查询条件
        switch (status) {
            case 0: // 上新
                queryWrapper.eq("uid", userId);
//                        .eq("status", status);
                break;
            case 1: // 卖出
                queryWrapper.eq("uid", userId)
                        .isNotNull("buy_uid")
                        .and(wrapper -> wrapper
                                .eq("status", 1)
                                .or()
                                .eq("status", 3)
                                .or()
                                .eq("status", 4)
                        );
                break;
            case 2: // 买到
                queryWrapper.eq("buy_uid", userId);
                break;
            case 3: // 动态
                queryWrapper.eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .and(wrapper -> wrapper
                                .eq("uid", userId)
                                .or()
                                .eq("buy_uid", userId)

                        );
                break;
            default:
                break;
        }

        Page<IdleProduct> mpProductPage = new Page<>(currentPage, pageSize);
        if (currentPage > 1) {
            mpProductPage.setSearchCount(false);
        }
        Page<IdleProduct> shopPage = productMapper.selectPage(mpProductPage, queryWrapper);

        return this.convertToProductVOPage(shopPage, userId);
    }

    /**
     * 将商品分页数据转换为VO分页数据
     */
    private Page<IdleProductVO> convertToProductVOPage(Page<IdleProduct> shopPage, String userId) {
        if (shopPage == null || StringUtils.isEmpty(userId)) {
            return new Page<>();
        }

        List<IdleProduct> productList = shopPage.getRecords();
        if (CollectionUtils.isEmpty(productList)) {
            return new Page<>();
        }

        // 收集所有需要查询的用户ID
        Set<String> userIds = productList.stream()
                .filter(Objects::nonNull)  // 过滤掉null元素
                .map(IdleProduct::getUid)
                .collect(Collectors.toSet());

        // 批量查询所有相关用户信息
        Map<String, WebUser> userMap = CollectionUtils.isEmpty(userIds) ?
                Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream()
                        .filter(Objects::nonNull)  // 过滤掉null用户
                        .collect(Collectors.toMap(WebUser::getId, Function.identity()));

        // 收集所有商品ID
        List<String> productIds = productList.stream()
                .filter(Objects::nonNull)
                .map(IdleProduct::getId)
                .collect(Collectors.toList());

        // 只查当前页商品的收藏状态，而非用户全量收藏
        Set<String> collectionIds;
        if (!CollectionUtils.isEmpty(productIds)) {
            collectionIds = collectionMapper.selectList(
                            new QueryWrapper<IdleCollection>()
                                    .eq("uid", userId)
                                    .eq("type", 1)
                                    .in("collection_id", productIds)
                    ).stream()
                    .filter(Objects::nonNull)
                    .map(IdleCollection::getCollectionId)
                    .collect(Collectors.toSet());
        } else {
            collectionIds = Collections.emptySet();
        }

        // 查询支付订单信息
        Map<String, IdlePaymentOrder> paymentOrderMap = CollectionUtils.isEmpty(productIds) ?
                Collections.emptyMap() :
                payOrderMapper.selectList(
                                new QueryWrapper<IdlePaymentOrder>()
                                        .in("product_id", productIds)
                        ).stream()
                        .filter(Objects::nonNull)  // 过滤掉null元素
                        .collect(Collectors.groupingBy(
                                IdlePaymentOrder::getProductId,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream()
                                                .filter(Objects::nonNull)  // 再次过滤
                                                .max(Comparator.comparing(IdlePaymentOrder::getCreateTime))
                                                .orElse(null)
                                )
                        ));

        // 查询商品订单信息（获取自提码）
        Map<String, IdleProductOrder> productOrderMap = CollectionUtils.isEmpty(productIds) ?
                Collections.emptyMap() :
                productOrderMapper.selectList(
                                new QueryWrapper<IdleProductOrder>()
                                        .in("product_id", productIds)
//                                        .isNotNull("post_self_code")
                        ).stream()
                        .filter(Objects::nonNull)  // 过滤掉null元素
                        .collect(Collectors.toMap(
                                IdleProductOrder::getProductId,
                                Function.identity(),
                                (existing, replacement) -> existing));

        // 转换商品信息
        List<IdleProductVO> productVOList = productList.stream()
                .filter(Objects::nonNull)  // 过滤掉null元素
                .map(product -> {
                    try {
                        IdleProductVO productVO = ConvertUtils.sourceToTarget(product, IdleProductVO.class);
                        // 使用商品发布者的信息
                        WebUser seller = userMap.get(product.getUid());
                        if (seller != null) {
                            productVO.setUsername(seller.getUsername())
                                    .setAvatar(seller.getAvatar());
                        }

                        // 设置支付订单信息
                        IdlePaymentOrder paymentOrder = paymentOrderMap.get(product.getId());
                        if (paymentOrder != null) {
                            productVO.setPaymentOrderId(paymentOrder.getId());
                        }

                        // 设置商品订单信息（自提码等）
                        IdleProductOrder productOrder = productOrderMap.get(product.getId());
                        if (productOrder != null && productOrder.getOrderNumber() != null) {
                            productVO.setProductOrderNumber(productOrder.getOrderNumber());
                        }
                        if (productOrder != null && productOrder.getPostSelfCode() != null) {
                            productVO.setPostSelfCode(productOrder.getPostSelfCode());
                        }
                        if (productOrder != null && productOrder.getDealStatus() != null) {
                            productVO.setDealStatus(productOrder.getDealStatus());
                        }

                        return productVO.setIsCollection(collectionIds.contains(product.getId()))
                                .setTime(Optional.ofNullable(product.getUpdateTime())
                                        .map(Date::getTime)
                                        .orElse(null));
                    } catch (Exception e) {
                        return null;  // 或者返回一个默认的VO对象
                    }
                })
                .filter(Objects::nonNull)  // 过滤掉转换失败的null元素
                .collect(Collectors.toList());

        return new Page<IdleProductVO>()
                .setRecords(productVOList)
                .setTotal(shopPage.getTotal());
    }

    /**
     * 用户数据
     */
    private Page<NoteSearchVo> getLikeOrCollectionPageByUser(long currentPage, long pageSize, String userId, Integer type) {
        Page<NoteSearchVo> noteSearchVoPage = new Page<>();

        // 查询点赞或收藏记录
        Page<WebLikeOrCollection> mpLikePage = new Page<>(currentPage, pageSize);
        if (currentPage > 1) {
            mpLikePage.setSearchCount(false);
        }
        Page<WebLikeOrCollection> likeOrCollectionPage = likeOrCollectionMapper.selectPage(
                mpLikePage,
                new QueryWrapper<WebLikeOrCollection>()
                        .eq("uid", userId)
                        .eq("type", type == 2 ? 1 : 3)  // 2-点赞(type=1), 3-收藏(type=3)
                        .orderByDesc("create_time")
        );

        List<WebLikeOrCollection> likeOrCollectionList = likeOrCollectionPage.getRecords();
        // 如果没有点赞或收藏记录，直接返回空页
        if (CollectionUtil.isEmpty(likeOrCollectionList)) {
            return noteSearchVoPage;
        }

        // 仅查询当前页涉及的笔记是否被当前用户点赞（避免全量拉取用户全部点赞记录）
        Set<String> noteIds = likeOrCollectionList.stream()
                .map(WebLikeOrCollection::getLikeOrCollectionId)
                .collect(Collectors.toSet());
        Set<String> likedNoteIdsOnPage = new HashSet<>();
        if (CollectionUtil.isNotEmpty(noteIds)) {
            List<WebLikeOrCollection> pageLikes = likeOrCollectionMapper.selectList(
                    new QueryWrapper<WebLikeOrCollection>()
                            .eq("uid", userId)
                            .eq("type", 1)
                            .in("like_or_collection_id", noteIds));
            likedNoteIdsOnPage = pageLikes.stream()
                    .map(WebLikeOrCollection::getLikeOrCollectionId)
                    .collect(Collectors.toSet());
        }

        // 获取发布者ID集合
        Set<String> publisherIds = likeOrCollectionList.stream()
                .map(WebLikeOrCollection::getPublishUid)
                .collect(Collectors.toSet());

        // 批量查询用户信息（添加空集合判断）
        Map<String, WebUser> userMap = CollectionUtil.isEmpty(publisherIds) ?
                new HashMap<>() :
                this.listByIds(publisherIds).stream()
                        .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 批量查询笔记信息（添加空集合判断）
        Map<String, WebNote> noteMap = CollectionUtil.isEmpty(noteIds) ?
                new HashMap<>() :
                noteMapper.selectBatchIds(noteIds).stream()
                        .collect(Collectors.toMap(WebNote::getId, note -> note));

        // 组装返回数据
        List<NoteSearchVo> noteSearchVoList = new ArrayList<>();
        for (WebLikeOrCollection model : likeOrCollectionList) {
            WebNote note = noteMap.get(model.getLikeOrCollectionId());
            if (note == null) continue;  // 跳过已删除的笔记

            WebUser user = userMap.get(model.getPublishUid());
            if (user == null) continue;  // 跳过已删除的用户

            NoteSearchVo noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
            noteSearchVo.setUsername(user.getUsername())
                    .setIsLike(likedNoteIdsOnPage.contains(note.getId()))
                    .setAvatar(user.getAvatar());
            noteSearchVoList.add(noteSearchVo);
        }

        return noteSearchVoPage.setRecords(noteSearchVoList)
                .setTotal(likeOrCollectionPage.getTotal());
    }

    /**
     * 根据条件分页查询角色数据
     *
     * @param user 角色信息
     * @return 角色数据集合信息
     */
    @Override
//    @DataScope(deptAlias = "d")
    public List<WebUser> getUserList(WebUser user) {
        return userMapper.getUserList(user);
    }

    @Override
    public Object getCollectCount(String userId) {
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        int productCount = Math.toIntExact(collectionMapper.selectCount(
                new QueryWrapper<IdleCollection>().eq("uid", userId)));
        int noteCount = Math.toIntExact(likeOrCollectionMapper.selectCount(
                new QueryWrapper<WebLikeOrCollection>()
                        .eq("uid", userId).eq("type", 3)));
        Map<String, Object> result = new HashMap<>(3);
        result.put("noteCount", noteCount);
        result.put("productCount", productCount);
        return result;
    }

    @Override
    public Page<IdleProduct> getUserProducts(Map map) {

        String currentPage = map.get("pageNum").toString();
        String pageSize = map.get("pageSize").toString();
        String userId = map.get("uid").toString();
        String title = map.get("title").toString();

        QueryWrapper<IdleProduct> queryWrapper = new QueryWrapper<>();
        if (userId != null && !userId.trim().isEmpty()) {
            queryWrapper.eq("uid", userId);
        }
        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.eq("title", title);
        }
        return productMapper.selectPage(
                new Page<>(Integer.parseInt(currentPage), Integer.parseInt(pageSize)),
                queryWrapper
        );
    }

    @PostConstruct
    public void init() throws Exception {
        // 1. 读取 classpath 下的 ip2region.xdb
        ClassPathResource resource = new ClassPathResource("ip2region.xdb");
        // 2. 拷贝到临时文件
        File tempFile = File.createTempFile("ip2region", ".xdb");
        try (InputStream in = resource.getInputStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        // 3. 用临时文件路径初始化
        this.searcher = Searcher.newWithFileOnly(tempFile.getAbsolutePath());
    }

    public List<UserLocationVO> getAllUserLocations() {
        try {
            String snapshot = redisTemplate.opsForValue().get(USER_LOCATIONS_SNAPSHOT_KEY);
            if (StringUtils.isNotBlank(snapshot)) {
                List<UserLocationVO> cached = JSON.parseArray(snapshot, UserLocationVO.class);
                if (cached != null) {
                    return cached;
                }
            }
        } catch (Exception e) {
            log.warn("读取用户地理分布快照缓存失败", e);
        }
        List<UserLocationVO> fresh = computeAllUserLocations();
        try {
            redisTemplate.opsForValue().set(
                    USER_LOCATIONS_SNAPSHOT_KEY,
                    JSON.toJSONString(fresh),
                    USER_LOCATIONS_SNAPSHOT_TTL_SEC,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入用户地理分布快照缓存失败", e);
        }
        return fresh;
    }

    /** 全量计算：按 IP 聚合城市 + 解析经纬度（不走整表快照） */
    private List<UserLocationVO> computeAllUserLocations() {
        List<IpStatDTO> ipStats = userMapper.selectUserIpStats();
        Map<String, Integer> cityCountMap = new HashMap<>();
        for (IpStatDTO ipStat : ipStats) {
            try {
                String city = this.parseIpToCity(ipStat.getLoginIp());
                if (city != null) {
                    cityCountMap.merge(city, ipStat.getCount(), Integer::sum);
                }
            } catch (Exception e) {
                log.error("IP解析失败: " + ipStat.getLoginIp(), e);
            }
        }
        return this.batchGetCityLocations(cityCountMap);
    }

    private boolean isGeocodableCity(String city) {
        return StringUtils.isNotBlank(city) && !"内网IP".equals(city);
    }

    /**
     * 城市坐标：Redis multiGet 批量读 + 未命中城市线程池并发调高德（共享 OkHttp + 全局限流，无额外 sleep）
     */
    private List<UserLocationVO> batchGetCityLocations(Map<String, Integer> cityCountMap) {
        List<String> cities = cityCountMap.keySet().stream()
                .filter(this::isGeocodableCity)
                .distinct()
                .collect(Collectors.toList());
        Map<String, LocationInfo> resolved = new HashMap<>();
        if (cities.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> cacheKeys = cities.stream()
                .map(c -> CITY_LOCATION_CACHE_PREFIX + c)
                .collect(Collectors.toList());
        List<String> cachedVals = redisTemplate.opsForValue().multiGet(cacheKeys);
        List<String> needApi = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            String city = cities.get(i);
            String val = cachedVals != null && i < cachedVals.size() ? cachedVals.get(i) : null;
            if (StringUtils.isNotBlank(val)) {
                try {
                    resolved.put(city, JSON.parseObject(val, LocationInfo.class));
                } catch (Exception e) {
                    log.warn("解析城市坐标缓存失败: {}", city, e);
                    needApi.add(city);
                }
            } else {
                needApi.add(city);
            }
        }
        if (!needApi.isEmpty()) {
            int nThreads = Math.min(GEOCODE_PARALLELISM, needApi.size());
            ExecutorService pool = Executors.newFixedThreadPool(nThreads);
            List<Future<LocationInfo>> futures = new ArrayList<>();
            try {
                for (String city : needApi) {
                    futures.add(pool.submit(() -> getCityLocation(city)));
                }
                for (int i = 0; i < needApi.size(); i++) {
                    try {
                        LocationInfo loc = futures.get(i).get(45, TimeUnit.SECONDS);
                        if (loc != null) {
                            resolved.put(needApi.get(i), loc);
                        }
                    } catch (Exception e) {
                        log.error("城市地理编码任务失败: {}", needApi.get(i), e);
                    }
                }
            } finally {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        pool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
        List<UserLocationVO> results = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cityCountMap.entrySet()) {
            String city = entry.getKey();
            if (!isGeocodableCity(city)) {
                continue;
            }
            LocationInfo location = resolved.get(city);
            if (location != null) {
                results.add(new UserLocationVO(
                        location.getCity(),
                        location.getLongitude(),
                        location.getLatitude(),
                        entry.getValue()));
            } else {
                log.warn("无法获取城市坐标，跳过: {}", city);
            }
        }
        return results;
    }

    private String calculateSignature(String parameters, String amapSecurityKey) {
        try {
            String stringToSign = parameters + amapSecurityKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(stringToSign.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("计算签名失败", e);
            return null;
        }
    }

    /**
     * 获取城市坐标信息，优先从缓存获取
     */
    private LocationInfo getCityLocation(String city) {
        String cacheKey = CITY_LOCATION_CACHE_PREFIX + city;
        String cachedLocation = redisTemplate.opsForValue().get(cacheKey);
        if (cachedLocation != null) {
            return JSON.parseObject(cachedLocation, LocationInfo.class);
        }
        AmapConfigDTO amapConfig = systemConfigService.getAmapConfig();
        if (amapConfig == null
                || StringUtils.isBlank(amapConfig.getKey())
                || StringUtils.isBlank(amapConfig.getSecurityKey())) {
            log.warn("高德地图未配置或缺少密钥，跳过地理编码: {}", city);
            return null;
        }
        rateLimiter.acquire();
        String amapKey = amapConfig.getKey();
        String amapSecurityKey = amapConfig.getSecurityKey();
        try {
            // 2. 对城市名进行 URL 编码
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
            // 3. 构建请求URL并添加签名
            String paramString = String.format("address=%s&key=%s&output=json",
                    encodedCity,
                    amapKey);
//                    amapConfig.getAmapKey());
//            String sig = this.calculateSignature(paramString + amapConfig.getAmapSecurityKey());
            String sig = this.calculateSignature(paramString + amapSecurityKey, amapSecurityKey);
            String url = String.format("%s?%s&sig=%s",
                    GEOCODE_URL,
                    paramString,
                    sig);
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try (Response response = AMAP_HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("高德地图API HTTP请求失败 - 状态码: {}, city: {}", response.code(), city);
                    return null;
                }
                String responseBody = response.body() != null ? response.body().string() : null;
                if (responseBody == null) {
                    log.error("高德地图API返回空响应");
                    return null;
                }
                JSONObject result = JSON.parseObject(responseBody);
                if (!"1".equals(result.getString("status"))) {
                    log.error("高德地图API调用失败 - status: {}, info: {}, infocode: {}, city: {}",
                            result.getString("status"),
                            result.getString("info"),
                            result.getString("infocode"),
                            city);
                    // 根据不同的错误码处理
                    switch (result.getString("infocode")) {
                        case "10009":
                            log.error("Key验证失败，请检查key配置和权限设置");
                            break;
                        case "10003":
                            log.error("访问已超出日访问量限额");
                            break;
                        case "10004":
                            log.error("单位时间内访问过于频繁");
                            break;
                        default:
                            log.error("未知错误");
                    }
                    return null;
                }
                JSONArray geocodes = result.getJSONArray("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    JSONObject geocode = geocodes.getJSONObject(0);
                    String location = geocode.getString("location"); // "116.405285,39.904989"
                    String[] coordinates = location.split(",");
                    LocationInfo locationInfo = new LocationInfo(
                            city,
                            Double.parseDouble(coordinates[0]),
                            Double.parseDouble(coordinates[1])
                    );
                    // 存入缓存
                    redisTemplate.opsForValue().set(
                            cacheKey,
                            JSON.toJSONString(locationInfo),
                            CACHE_EXPIRE_DAYS,
                            TimeUnit.DAYS
                    );
                    return locationInfo;
                }
            }
        } catch (IOException e) {
            log.error("网络IO异常: " + city, e);
        } catch (Exception e) {
            log.error("调用高德地图API失败: " + city, e);
        }
        return null;
    }

    /**
     * 解析IP地址为城市名
     */
    private String parseIpToCity(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        // 特殊处理本地回环地址和局域网 IP
        if (isLocalOrPrivateIp(ip)) {
            return "内网IP"; // 或返回 null/"本地"，根据业务需求
        }

        try {
            String ipInfo = searcher.search(ip);
            // ipInfo 格式: "中国|0|浙江省|杭州市|电信"
            String[] parts = ipInfo.split("\\|");
            if (parts.length >= 4) {
                // 返回城市名，如果是直辖市，使用省份名
                String province = parts[2].trim();
                String city = parts[3].trim();
                // 处理直辖市
                if (isDirectCity(province)) {
                    return province;
                }
                // 处理普通城市
                return city.equals("0") ? province : city;
            }
        } catch (Exception e) {
            log.error("IP解析失败: " + ip, e);
        }
        return null;
    }

    /**
     * 判断是否为本地或私有网络 IP
     */
    private boolean isLocalOrPrivateIp(String ip) {
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return true; // IPv4/IPv6 回环地址
        }
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            return true; // 私有网络 IP 段
        }
        return false;
    }

    /**
     * 判断是否是直辖市
     */
    private boolean isDirectCity(String province) {
        return province.equals("北京市") ||
                province.equals("上海市") ||
                province.equals("天津市") ||
                province.equals("重庆市");
    }

    /**
     * 用户数据
     */
    private Page<NoteSearchVo> getLikeOrCollectionPageByUser(long currentPage, long pageSize, String userId, String status) {
        Page<NoteSearchVo> noteSearchVoPage = new Page<>();
        // 得到当前用户发布的所有专辑
        Page<WebNote> notePage;
        QueryWrapper<WebNote> queryWrapper = new QueryWrapper<WebNote>()
                .eq("uid", userId)
                .eq("deleted", 0);
        // 只有当 status 为 -1 时才添加置顶状态排序条件
        if ("-1".equals(status)) {
            queryWrapper.orderByDesc("pinned", "update_time");
        } else {
            queryWrapper.orderByDesc("update_time");
        }
        // 只有当 status 不为 -1 时才添加审核状态条件
        if (!"-1".equals(status)) {
            queryWrapper.eq("audit_status", status);
        }

        Page<WebNote> mpNotePage = new Page<>(currentPage, pageSize);
        if (currentPage > 1) {
            mpNotePage.setSearchCount(false);
        }
        notePage = noteMapper.selectPage(mpNotePage, queryWrapper);
        List<WebNote> noteList = notePage != null ? notePage.getRecords() : new ArrayList<>();
        long total = notePage != null ? notePage.getTotal() : 0L;

        // 得到所有用户的信息（过滤null的uid）
        Set<String> uids = noteList.stream()
                .filter(note -> note != null && note.getUid() != null)
                .map(WebNote::getUid)
                .collect(Collectors.toSet());

        // 仅查询当前页笔记是否被当前用户点赞（避免全量拉取用户全部点赞记录）
        Set<String> likedNoteIdsOnPage = new HashSet<>();
        if (CollectionUtil.isNotEmpty(noteList)) {
            List<String> pageNoteIds = noteList.stream()
                    .map(WebNote::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(pageNoteIds)) {
                List<WebLikeOrCollection> pageLikes = likeOrCollectionMapper.selectList(
                        new QueryWrapper<WebLikeOrCollection>()
                                .eq("uid", userId)
                                .eq("type", 1)
                                .in("like_or_collection_id", pageNoteIds));
                likedNoteIdsOnPage = pageLikes.stream()
                        .map(WebLikeOrCollection::getLikeOrCollectionId)
                        .collect(Collectors.toSet());
            }
        }

        if (CollectionUtil.isNotEmpty(uids)) {
            Map<String, WebUser> userMap = this.listByIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            List<NoteSearchVo> noteSearchVoList = new ArrayList<>();
            for (WebNote note : noteList) {
                if (note == null) continue; // 跳过null的note

                NoteSearchVo noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
                if (noteSearchVo == null) continue; // 跳过转换失败的note

                // 安全检查：获取用户信息
                String noteUid = note.getUid();
                WebUser user = (noteUid != null) ? userMap.get(noteUid) : null;
                if (user != null) {
                    noteSearchVo.setUsername(user.getUsername())
                            .setAvatar(user.getAvatar());
                }
//                        .setViewCount(note.getViewCount())

                // 安全检查：设置点赞状态
                String noteId = note.getId();
                if (noteId != null) {
                    noteSearchVo.setIsLike(likedNoteIdsOnPage.contains(noteId));
                } else {
                    noteSearchVo.setIsLike(false);
                }

                // 处理updateTime可能为null的情况
                if (note.getUpdateTime() != null) {
                    noteSearchVo.setTime(note.getUpdateTime().getTime());
                } else if (note.getCreateTime() != null) {
                    // 如果没有updateTime，使用createTime
                    noteSearchVo.setTime(note.getCreateTime().getTime());
                } else {
                    // 如果都没有，使用当前时间戳
                    noteSearchVo.setTime(System.currentTimeMillis());
                }
                noteSearchVoList.add(noteSearchVo);
            }
            noteSearchVoPage.setRecords(noteSearchVoList);
            noteSearchVoPage.setTotal(total);
        }
        return noteSearchVoPage;
    }
}
