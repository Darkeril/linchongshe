package com.hongshu.idle.service.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.AuditStatusEnum;
import com.hongshu.idle.domain.entity.IdleCollection;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdleCollectionMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 轻量级商品推荐服务 - 适合闲置交易场景
 * 基于分类+价格区间+地理位置+热门的四维度推荐算法
 *
 
 * @date 2024/01/01
 */
@Service
@Slf4j
public class LightweightProductRecommendService {

    /** 各召回通道下限：pageSize 小时避免整池过小，去重/过滤后条数过少 */
    private static final int MIN_RECALL_CATEGORY = 100;
    private static final int MIN_RECALL_PRICE = 80;
    private static final int MIN_RECALL_LOCATION = 60;
    private static final int MIN_RECALL_HOT = 120;

    /** 未登录/新用户：固定池过小会导致 total 小、翻页很快无数据 */
    private static final int HOT_NEW_POOL = 400;

    @Autowired
    private IdleProductMapper productMapper;

    @Autowired
    private IdleCollectionMapper collectionMapper;
    
    @Autowired
    private com.hongshu.idle.mapper.web.WebUserMapper userMapper;


    /**
     * 获取推荐商品（轻量级版本）
     *
     * @param userId 用户ID
     * @param page   当前页
     * @param size   每页大小
     * @return 推荐商品分页
     */
    public Page<IdleProductVO> getRecommendProducts(String userId, long page, long size) {
        log.info("轻量级商品推荐 - 用户: {}, 页码: {}, 大小: {}", userId, page, size);

        if (StringUtils.isBlank(userId)) {
            // 未登录用户：热门商品 + 最新商品
            return getHotAndNewProducts(page, size);
        }

        // 检查用户是否为新用户（无历史行为）
        boolean isNewUser = checkIfNewUser(userId);
        if (isNewUser) {
            log.info("检测到新用户: {}, 使用热门+最新商品推荐", userId);
            return getHotAndNewProducts(page, size);
        }

        List<ProductWithScore> candidates = new ArrayList<>();

        int limCat = (int) Math.max(size * 3, MIN_RECALL_CATEGORY);
        int limPrice = (int) Math.max(size * 2, MIN_RECALL_PRICE);
        int limLoc = (int) Math.max(size * 2, MIN_RECALL_LOCATION);
        int limHot = (int) Math.max(size * 2, MIN_RECALL_HOT);

        // 1. 分类召回（40%权重，核心）
        List<ProductWithScore> categoryProducts = recallByUserCategories(userId, limCat);
        candidates.addAll(categoryProducts);
        log.info("分类召回: {} 条", categoryProducts.size());

        // 2. 价格区间召回（30%权重）
        List<ProductWithScore> priceProducts = recallByPriceRange(userId, limPrice);
        candidates.addAll(priceProducts);
        log.info("价格区间召回: {} 条", priceProducts.size());

        // 3. 地理位置召回（20%权重）- 同城优先
        List<ProductWithScore> locationProducts = recallByLocation(userId, limLoc);
        candidates.addAll(locationProducts);
        log.info("地理位置召回: {} 条", locationProducts.size());

        // 4. 热门召回（10%权重）- 原 size*0.5 过小，与笔记侧一致设下限
        List<ProductWithScore> hotProducts = recallHotProducts(limHot);
        candidates.addAll(hotProducts);
        log.info("热门召回: {} 条", hotProducts.size());

        // 5. 去重（保留得分最高的）
        Map<String, ProductWithScore> productMap = new HashMap<>();
        for (ProductWithScore product : candidates) {
            String productId = String.valueOf(product.getProduct().getId());
            if (!productMap.containsKey(productId) ||
                    productMap.get(productId).getScore() < product.getScore()) {
                productMap.put(productId, product);
            }
        }

        // 6. 过滤已收藏的商品和自己的商品
        Set<String> collectedProductIds = getUserCollectedProductIds(userId);
        List<ProductWithScore> filtered = productMap.values().stream()
                .filter(p -> !collectedProductIds.contains(String.valueOf(p.getProduct().getId())))
                .filter(p -> !userId.equals(p.getProduct().getUid())) // 过滤自己的商品
                .filter(p -> p.getProduct().getStatus() == 0) // 只要在售商品
                .collect(Collectors.toList());

        log.info("去重和过滤后: {} 条", filtered.size());

        // 6.1 如果召回结果太少（<5条），补充热门商品
        if (filtered.size() < 5) {
            log.warn("召回结果太少({})，补充热门商品", filtered.size());
            List<ProductWithScore> supplementHotProducts = recallHotProducts((int) Math.max(size * 2, MIN_RECALL_HOT));
            
            // 去重后添加
            Set<String> existingIds = filtered.stream()
                    .map(p -> String.valueOf(p.getProduct().getId()))
                    .collect(Collectors.toSet());
            
            for (ProductWithScore hotProduct : supplementHotProducts) {
                String productId = String.valueOf(hotProduct.getProduct().getId());
                if (!existingIds.contains(productId) && !collectedProductIds.contains(productId)) {
                    filtered.add(hotProduct);
                    existingIds.add(productId);
                    if (filtered.size() >= size * 2) break; // 限制补充数量
                }
            }
            log.info("补充后总数: {} 条", filtered.size());
        }

        // 7. 按分数排序
        filtered.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 8. 打散（同一卖家不连续）
        List<ProductWithScore> shuffled = shuffleBySeller(filtered);

        // 9. 分页返回
        return buildPageResult(shuffled, page, size);
    }


    /**
     * 基于用户分类召回
     */
    private List<ProductWithScore> recallByUserCategories(String userId, int limit) {
        try {
            // 1. 获取用户收藏的商品ID
            List<String> collectedProductIds = collectionMapper.selectList(
                    new QueryWrapper<IdleCollection>()
                            .eq("uid", userId)
                            .in("type", Arrays.asList(1, 3)) // 1:点赞 3:收藏
                            .orderByDesc("create_time")
                            .last("LIMIT 50")
            ).stream()
                    .map(IdleCollection::getCollectionId)
                    .collect(Collectors.toList());

            if (collectedProductIds.isEmpty()) {
                log.debug("用户 {} 没有收藏记录", userId);
                return Collections.emptyList();
            }

            // 2. 获取这些商品的分类
            List<IdleProduct> collectedProducts = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .in("id", collectedProductIds)
                            .eq("deleted", 0)
            );
            Set<String> categoryIds = collectedProducts.stream()
                    .flatMap(p -> Arrays.stream(new String[]{p.getCid(), p.getCpid()}))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());

            if (categoryIds.isEmpty()) {
                return Collections.emptyList();
            }

            log.debug("用户 {} 的兴趣分类(cid/cpid): {}", userId, categoryIds);

            // 3. 同分类商品：cid 或 cpid 命中任一即可（不少商品只填一级 cpid）
            List<IdleProduct> products = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0) // 在售状态
                            .and(w -> w.in("cid", categoryIds).or().in("cpid", categoryIds))
                            .orderByDesc("like_count", "create_time")
                            .last("LIMIT " + limit)
            );

            // 过滤掉已收藏的
            Set<String> collectedIdSet = new HashSet<>(collectedProductIds);
            return products.stream()
                    .filter(product -> !collectedIdSet.contains(String.valueOf(product.getId())))
                    .map(product -> {
                        double score = calculateScore(product, userId, "category");
                        return new ProductWithScore(product, score);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("分类召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 基于价格区间召回
     */
    private List<ProductWithScore> recallByPriceRange(String userId, int limit) {
        try {
            // 1. 获取用户收藏商品的价格范围
            List<String> collectedProductIds = collectionMapper.selectList(
                    new QueryWrapper<IdleCollection>()
                            .eq("uid", userId)
                            .in("type", Arrays.asList(1, 3))
                            .last("LIMIT 50")
            ).stream()
                    .map(IdleCollection::getCollectionId)
                    .collect(Collectors.toList());

            if (collectedProductIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 计算用户偏好的价格范围
            List<IdleProduct> collectedProducts = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .in("id", collectedProductIds)
                            .eq("deleted", 0)
            );
            double avgPrice = collectedProducts.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getPrice()))
                    .mapToDouble(p -> {
                        try {
                            return Double.parseDouble(p.getPrice());
                        } catch (Exception e) {
                            return 0.0;
                        }
                    })
                    .filter(price -> price > 0)
                    .average()
                    .orElse(100.0); // 默认100元

            // 3. 推荐相似价格区间的商品（±50%）
            double minPrice = avgPrice * 0.5;
            double maxPrice = avgPrice * 1.5;

            log.debug("用户 {} 的价格偏好范围: {}-{}", userId, minPrice, maxPrice);

            // 4. 查询该价格区间的商品（使用原生SQL或复杂查询）
            List<IdleProduct> products = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0)
                            .orderByDesc("like_count", "create_time")
                            .last("LIMIT " + (limit * 2)) // 多查一些再过滤
            );

            // 5. 过滤价格区间
            Set<String> collectedIdSet = new HashSet<>(collectedProductIds);
            return products.stream()
                    .filter(product -> !collectedIdSet.contains(String.valueOf(product.getId())))
                    .filter(product -> {
                        try {
                            if (StringUtils.isBlank(product.getPrice())) return false;
                            double price = Double.parseDouble(product.getPrice());
                            return price >= minPrice && price <= maxPrice;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .limit(limit)
                    .map(product -> {
                        double score = calculateScore(product, userId, "price");
                        return new ProductWithScore(product, score);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("价格区间召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 基于地理位置召回（同城优先）
     */
    private List<ProductWithScore> recallByLocation(String userId, int limit) {
        try {
            // 1. 获取用户收藏商品的地理位置
            List<String> collectedProductIds = collectionMapper.selectList(
                    new QueryWrapper<IdleCollection>()
                            .eq("uid", userId)
                            .in("type", Arrays.asList(1, 3))
                            .last("LIMIT 30")
            ).stream()
                    .map(IdleCollection::getCollectionId)
                    .collect(Collectors.toList());

            if (collectedProductIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 获取用户偏好的城市
            List<IdleProduct> collectedProducts = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .in("id", collectedProductIds)
                            .eq("deleted", 0)
            );
            Set<String> cities = collectedProducts.stream()
                    .map(IdleProduct::getCity)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());

            if (cities.isEmpty()) {
                return Collections.emptyList();
            }

            log.debug("用户 {} 的偏好城市: {}", userId, cities);

            // 3. 推荐同城商品
            List<IdleProduct> products = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .in("city", cities)
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0)
                            .orderByDesc("like_count", "create_time")
                            .last("LIMIT " + limit)
            );

            // 过滤已收藏的
            Set<String> collectedIdSet = new HashSet<>(collectedProductIds);
            return products.stream()
                    .filter(product -> !collectedIdSet.contains(String.valueOf(product.getId())))
                    .map(product -> {
                        double score = calculateScore(product, userId, "location");
                        return new ProductWithScore(product, score);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("地理位置召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 热门商品召回
     */
    private List<ProductWithScore> recallHotProducts(int limit) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            List<IdleProduct> products = productMapper.selectList(
                    new QueryWrapper<IdleProduct>()
                            .eq("deleted", 0)
                            .eq("audit_status", AuditStatusEnum.PASS.getCode())
                            .eq("status", 0)
                            .ge("create_time", sevenDaysAgo)
                            .orderByDesc("like_count", "create_time")
                            .last("LIMIT " + limit)
            );

            if (products.size() < limit) {
                Set<String> have = products.stream()
                        .map(p -> String.valueOf(p.getId()))
                        .collect(Collectors.toSet());
                int need = limit - products.size();
                List<IdleProduct> more = productMapper.selectList(
                        new QueryWrapper<IdleProduct>()
                                .eq("deleted", 0)
                                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                                .eq("status", 0)
                                .ge("create_time", thirtyDaysAgo)
                                .lt("create_time", sevenDaysAgo)
                                .orderByDesc("like_count", "create_time")
                                .last("LIMIT " + (need * 2))
                );
                for (IdleProduct n : more) {
                    if (have.add(String.valueOf(n.getId()))) {
                        products.add(n);
                        if (products.size() >= limit) {
                            break;
                        }
                    }
                }
            }
            if (products.size() < limit) {
                Set<String> have = products.stream()
                        .map(p -> String.valueOf(p.getId()))
                        .collect(Collectors.toSet());
                int need = limit - products.size();
                List<IdleProduct> more = productMapper.selectList(
                        new QueryWrapper<IdleProduct>()
                                .eq("deleted", 0)
                                .eq("audit_status", AuditStatusEnum.PASS.getCode())
                                .eq("status", 0)
                                .orderByDesc("like_count", "create_time")
                                .last("LIMIT " + (need * 2))
                );
                for (IdleProduct n : more) {
                    if (have.add(String.valueOf(n.getId()))) {
                        products.add(n);
                        if (products.size() >= limit) {
                            break;
                        }
                    }
                }
            }

            return products.stream()
                    .map(product -> {
                        double score = calculateScore(product, null, "hot");
                        return new ProductWithScore(product, score);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("热门商品召回异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 计算商品评分（针对闲置商品特点）
     */
    private double calculateScore(IdleProduct product, String userId, String source) {
        // 1. 商品热度分（0-100）- 基于想要数量
        double popularityScore = Math.min(100, product.getLikeCount() / 10.0);

        // 2. 新鲜度分（0-100）- 闲置商品更看重新发布
        double freshnessScore = 100;
        if (product.getCreateTime() != null) {
            long hoursSinceCreate = ChronoUnit.HOURS.between(
                    product.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    LocalDateTime.now()
            );

            if (hoursSinceCreate > 1) {
                if (hoursSinceCreate <= 24) freshnessScore = 90;
                else if (hoursSinceCreate <= 72) freshnessScore = 70;
                else if (hoursSinceCreate <= 168) freshnessScore = 50;
                else freshnessScore = 30;
            }
        }

        // 3. 价格合理性分（0-100）- 闲置商品特有
        double priceScore = 100;
        try {
            if (StringUtils.isNotBlank(product.getPrice()) && 
                StringUtils.isNotBlank(product.getOriginalPrice())) {
                double price = Double.parseDouble(product.getPrice());
                double originalPrice = Double.parseDouble(product.getOriginalPrice());
                
                if (originalPrice > 0) {
                    double discount = price / originalPrice;
                    // 折扣越低，分数越高（但太低可能有问题）
                    if (discount >= 0.1 && discount <= 0.7) {
                        priceScore = 100 - (discount * 100); // 1折100分，7折30分
                    } else if (discount < 0.1) {
                        priceScore = 50; // 太便宜可能有问题
                    } else {
                        priceScore = 30; // 折扣太小
                    }
                }
            }
        } catch (Exception e) {
            priceScore = 50; // 异常时给中等分
        }

        // 4. 来源权重
        double sourceWeight = 1.0;
        switch (source) {
            case "category":
                sourceWeight = 1.5;
                break;  // 分类最重要
            case "price":
                sourceWeight = 1.3;
                break;    // 价格区间很重要
            case "location":
                sourceWeight = 1.4;
                break; // 同城更方便交易
            case "hot":
                sourceWeight = 1.0;
                break;
        }

        // 综合评分 = 热度(30%) + 新鲜度(40%) + 价格合理性(30%)
        double finalScore = (popularityScore * 0.3 + freshnessScore * 0.4 + priceScore * 0.3) * sourceWeight;

        log.debug("商品 {} 评分 - 热度:{}, 新鲜度:{}, 价格:{}, 来源权重:{}, 最终:{}",
                product.getId(), popularityScore, freshnessScore, priceScore, sourceWeight, finalScore);

        return finalScore;
    }


    /**
     * 打散：同一卖家不连续出现
     */
    private List<ProductWithScore> shuffleBySeller(List<ProductWithScore> products) {
        List<ProductWithScore> result = new ArrayList<>();
        Set<String> recentSellers = new HashSet<>();
        Queue<ProductWithScore> pending = new LinkedList<>();

        int windowSize = 3; // 3条内不重复卖家

        for (ProductWithScore product : products) {
            String seller = product.getProduct().getUid();

            if (!recentSellers.contains(seller)) {
                result.add(product);
                recentSellers.add(seller);

                // 维护滑动窗口
                if (result.size() > windowSize) {
                    String oldSeller = result.get(result.size() - windowSize - 1)
                            .getProduct().getUid();
                    recentSellers.remove(oldSeller);
                }
            } else {
                pending.offer(product);
            }
        }

        // 将暂存的商品尽量插入
        while (!pending.isEmpty()) {
            ProductWithScore product = pending.poll();
            result.add(product);
        }

        return result;
    }


    /**
     * 检查是否为新用户（无历史行为）
     */
    private boolean checkIfNewUser(String userId) {
        try {
            long count = collectionMapper.selectCount(
                    new QueryWrapper<IdleCollection>()
                            .eq("uid", userId)
                            .in("type", Arrays.asList(1, 3))
            );
            
            // 无收藏行为，判定为新用户
            boolean isNew = (count == 0);
            if (isNew) {
                log.info("用户 {} 无收藏行为数据，判定为新用户", userId);
            }
            return isNew;
        } catch (Exception e) {
            log.error("检查新用户失败", e);
            return false;
        }
    }


    /**
     * 获取用户已收藏的商品ID
     */
    private Set<String> getUserCollectedProductIds(String userId) {
        return collectionMapper.selectList(
                new QueryWrapper<IdleCollection>()
                        .eq("uid", userId)
                        .in("type", Arrays.asList(1, 3))
        ).stream()
                .map(IdleCollection::getCollectionId)
                .collect(Collectors.toSet());
    }


    /**
     * 未登录用户：热门 + 最新商品
     * ✅ 修复：使用固定的查询数量，确保不同端查询到的数据量一致
     */
    private Page<IdleProductVO> getHotAndNewProducts(long page, long size) {
        int fixedQueryLimit = HOT_NEW_POOL;
        int hotLimit = fixedQueryLimit / 2;
        int newLimit = fixedQueryLimit - hotLimit;
        
        // 混合热门和最新商品
        List<IdleProduct> hotProducts = productMapper.selectList(
                new QueryWrapper<IdleProduct>()
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .eq("status", 0)
                        .orderByDesc("like_count")
                        .last("LIMIT " + hotLimit)
        );

        List<IdleProduct> newProducts = productMapper.selectList(
                new QueryWrapper<IdleProduct>()
                        .eq("deleted", 0)
                        .eq("audit_status", AuditStatusEnum.PASS.getCode())
                        .eq("status", 0)
                        .orderByDesc("create_time")
                        .last("LIMIT " + newLimit)
        );

        // 合并去重
        Set<String> productIds = new HashSet<>();
        List<IdleProduct> allProducts = new ArrayList<>();

        for (IdleProduct product : hotProducts) {
            if (productIds.add(String.valueOf(product.getId()))) {
                allProducts.add(product);
            }
        }

        for (IdleProduct product : newProducts) {
            if (productIds.add(String.valueOf(product.getId()))) {
                allProducts.add(product);
            }
        }

        // 随机打乱
        // 随机打乱：使用基于页码的固定随机种子，确保同一页结果一致
        Collections.shuffle(allProducts, new Random(page));

        // 分页
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + (int) size, allProducts.size());

        List<IdleProduct> pageProducts = allProducts.subList(
                Math.min(start, allProducts.size()),
                end
        );

        List<IdleProductVO> productVos = convertToVos(pageProducts);
        enrichUserInfo(productVos);

        Page<IdleProductVO> result = new Page<>(page, size);
        result.setRecords(productVos);
        result.setTotal(allProducts.size());
        return result;
    }


    /**
     * 构建分页结果
     */
    private Page<IdleProductVO> buildPageResult(List<ProductWithScore> products, long page, long size) {
        // 分页
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + (int) size, products.size());

        List<ProductWithScore> pageProducts = products.subList(
                Math.min(start, products.size()),
                end
        );

        List<IdleProductVO> productVos = pageProducts.stream()
                .map(pws -> convertToVo(pws.getProduct()))
                .collect(Collectors.toList());
        enrichUserInfo(productVos);

        Page<IdleProductVO> result = new Page<>(page, size);
        result.setRecords(productVos);
        result.setTotal(products.size());

        log.info("推荐结果 - 总数:{}, 当前页:{}, 返回:{} 条", products.size(), page, productVos.size());

        return result;
    }


    /**
     * 批量转换为VO
     */
    private List<IdleProductVO> convertToVos(List<IdleProduct> products) {
        return products.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }


    /**
     * 转换为VO
     */
    private IdleProductVO convertToVo(IdleProduct product) {
        IdleProductVO vo = new IdleProductVO();
        vo.setId(String.valueOf(product.getId()));
        vo.setUid(product.getUid());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setCover(product.getCover());
        vo.setUrls(product.getUrls());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCid(product.getCid());
        vo.setCpid(product.getCpid());
        vo.setProductType(product.getProductType());
        vo.setPostType(product.getPostType());
        vo.setLikeCount(product.getLikeCount());
        vo.setStatus(product.getStatus());
        vo.setProvince(product.getProvince());
        vo.setCity(product.getCity());
        vo.setDistrict(product.getDistrict());
        vo.setAddress(product.getAddress());
        vo.setCreateTime(product.getCreateTime());

        return vo;
    }

    private void enrichUserInfo(List<IdleProductVO> productList) {
        if (productList == null || productList.isEmpty()) return;
        Set<String> uidSet = productList.stream()
                .filter(p -> StringUtils.isNotBlank(p.getUid()))
                .map(IdleProductVO::getUid)
                .collect(Collectors.toSet());
        if (uidSet.isEmpty()) return;
        try {
            Map<String, com.hongshu.idle.domain.entity.WebUser> userMap = userMapper.selectList(
                    new QueryWrapper<com.hongshu.idle.domain.entity.WebUser>()
                            .select("id", "username", "avatar")
                            .in("id", uidSet)
            ).stream().collect(Collectors.toMap(
                    u -> String.valueOf(u.getId()), u -> u, (a, b) -> a));
            for (IdleProductVO vo : productList) {
                com.hongshu.idle.domain.entity.WebUser u = userMap.get(vo.getUid());
                if (u != null) {
                    vo.setAvatar(u.getAvatar());
                    vo.setUsername(u.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("批量填充用户信息失败", e);
        }
    }


    /**
     * 内部类：带评分的商品
     */
    @Data
    @AllArgsConstructor
    private static class ProductWithScore {
        private IdleProduct product;
        private double score;
    }
}

