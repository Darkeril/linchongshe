package com.hongshu.idle.service.idle.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.utils.ConvertUtils;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.idle.domain.dto.CollectionDTO;
import com.hongshu.idle.domain.entity.*;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdleCollectionMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.idle.IdleUserMapper;
import com.hongshu.idle.mapper.web.*;
import com.hongshu.idle.service.idle.IIdleCollectionService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 点赞/收藏
 *
 * @author: hongshu
 */
@Service
public class IdleCollectionServiceImpl extends ServiceImpl<IdleCollectionMapper, IdleCollection> implements IIdleCollectionService {

    @Autowired
    private IdleUserMapper userMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private WebAlbumMapper albumMapper;
    @Autowired
    private WebCommentMapper commentMapper;
    @Autowired
    private WebCommentSyncMapper commentSyncMapper;
    @Autowired
    private WebAlbumNoteRelationMapper albumNoteRelationMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private IdleCollectionMapper collectionMapper;


    /**
     * 收藏
     *
     * @param collectionDTO 收藏
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void collectionByDTO(CollectionDTO collectionDTO) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        // 收藏
        if (this.isCollection(collectionDTO)) {
            this.remove(new QueryWrapper<IdleCollection>().eq("uid", currentUid).eq("collection_id", collectionDTO.getCollectionId()).eq("type", collectionDTO.getType()));
            this.updateCollectionCount(collectionDTO, -1);
            // 更新ES数据
            this.updateEsProduct(collectionDTO.getCollectionId(), false);
        } else {
            IdleCollection collection = ConvertUtils.sourceToTarget(collectionDTO, IdleCollection.class);
            collection.setTimestamp(System.currentTimeMillis());
            collection.setUid(currentUid);
            collection.setCreateTime(new Date());
            collection.setUpdateTime(new Date());
            this.save(collection);
            this.updateCollectionCount(collectionDTO, 1);
            // 更新ES数据
            this.updateEsProduct(collection.getCollectionId(), true);
            // 不是当前用户才进行通知
            if (!collection.getPublishUid().equals(currentUid)) {
//                chatUtils.sendMessage(collection.getPublishUid(), 0);
            }
        }
    }

    /**
     * 是否收藏
     *
     * @param collectionDTO 收藏
     */
    @Override
    public boolean isCollection(CollectionDTO collectionDTO) {
//        String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String currentUid = AuthContextHolder.getUserId();
        long count = this.count(new QueryWrapper<IdleCollection>()
                .eq("uid", currentUid)
                .eq("collection_id", collectionDTO.getCollectionId())
                .eq("type", collectionDTO.getType()));
        return count > 0;
    }

    /**
     * 获取当前用户最新的收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<IdleProductVO> getNoticeCollection(long currentPage, long pageSize) {
        String currentUid = AuthContextHolder.getUserId();
        // 创建返回的VO分页对象
        Page<IdleProductVO> voPage = new Page<>(currentPage, pageSize);
        // 1. 分页查询当前用户的收藏记录（type=1点赞商品 或 type=3收藏商品）
        Page<IdleCollection> collectionPage = this.page(
                new Page<>(currentPage, pageSize),
                new QueryWrapper<IdleCollection>()
                        .eq("uid", currentUid)
                        .in("type", Arrays.asList(1, 3))
                        .orderByDesc("timestamp")   // 按时间戳倒序
        );
        // 如果没有收藏记录，直接返回空页面
        if (collectionPage.getRecords().isEmpty()) {
            voPage.setRecords(new ArrayList<>());
            voPage.setTotal(0);
            return voPage;
        }
        // 2. 提取所有收藏的商品ID
        List<String> productIds = collectionPage.getRecords().stream()
                .map(IdleCollection::getCollectionId)
                .filter(id -> id != null && !id.isEmpty())  // 过滤掉空值
                .collect(Collectors.toList());
        // 3. 批量查询商品信息（如果商品ID列表为空，直接返回空列表）
        List<IdleProduct> products = productIds.isEmpty() ? new ArrayList<>() : productMapper.selectBatchIds(productIds);
        // 4. 转换为 Map，方便后续根据 ID 查找
        java.util.Map<String, IdleProduct> productMap = products.stream()
                .collect(Collectors.toMap(
                        product -> String.valueOf(product.getId()),
                        product -> product,
                        (existing, replacement) -> existing
                ));
        // 5. 提取所有商品发布者的用户ID
        Set<String> userIds = products.stream()
                .map(IdleProduct::getUid)
                .filter(uid -> uid != null && !uid.isEmpty())  // 过滤掉空值
                .collect(Collectors.toSet());
        // 6. 批量查询用户信息（如果用户ID列表为空，直接返回空列表）
        List<IdleUser> users = userIds.isEmpty() ? new ArrayList<>() : userMapper.selectBatchIds(userIds);
        // 7. 转换为 Map，方便后续根据 ID 查找
        java.util.Map<String, IdleUser> userMap = users.stream()
                .collect(Collectors.toMap(
                        user -> String.valueOf(user.getId()),
                        user -> user,
                        (existing, replacement) -> existing
                ));
        // 8. 构建 VO 列表（保持收藏的顺序）
        List<IdleProductVO> voList = collectionPage.getRecords().stream()
                .map(collection -> {
                    IdleProduct product = productMap.get(collection.getCollectionId());
                    if (product == null) {
                        return null;  // 商品已被删除
                    }
                    // 转换为 VO
                    IdleProductVO productVO = ConvertUtils.sourceToTarget(product, IdleProductVO.class);
                    // 设置时间戳
                    productVO.setTime(product.getUpdateTime().getTime());
                    // 设置用户信息
                    IdleUser user = userMap.get(product.getUid());
                    if (user != null) {
                        productVO.setUsername(user.getUsername());
                        productVO.setAvatar(user.getAvatar());
                    }
                    // 设置是否已收藏（当前用户已收藏，所以设置为true）
                    productVO.setIsCollection(collection.getType() == 1 || collection.getType() == 3);
                    return productVO;
                })
                .filter(vo -> vo != null)  // 过滤掉已删除的商品
                .collect(Collectors.toList());
        // 9. 设置分页信息
        voPage.setRecords(voList);
        voPage.setTotal(collectionPage.getTotal());
        voPage.setCurrent(collectionPage.getCurrent());
        voPage.setSize(collectionPage.getSize());
        voPage.setPages(collectionPage.getPages());

        return voPage;
    }

    /**
     * 更新ES浏览记录
     */
    private void updateEsProduct(String productId, boolean b) {
        try {
            // Step 1: 获取现有的数据
            GetResponse<IdleProductVO> getResponse = elasticsearchClient.get(g -> g.index(NoteConstant.PRODUCT_INDEX).id(productId), IdleProductVO.class);
            // 检查是否获取到了数据
            if (getResponse.found()) {
                IdleProductVO productSearchVO = getResponse.source();
                // Step 2: 更新 isLike 字段
                productSearchVO.setIsCollection(b);
                if (b) {
                    productSearchVO.setLikeCount(productSearchVO.getLikeCount() + 1);
                } else {
                    productSearchVO.setLikeCount(productSearchVO.getLikeCount() - 1);
                }
                // Step 3: 将更新后的数据保存回 Elasticsearch
                UpdateResponse<IdleProductVO> updateResponse = elasticsearchClient.update(u -> u.index(NoteConstant.PRODUCT_INDEX).id(productId).doc(productSearchVO), IdleProductVO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 收藏
     */
    private void updateCollectionCount(CollectionDTO collectionDTO, int val) {
        switch (collectionDTO.getType()) {
            case 1:
                IdleProduct product = productMapper.selectById(collectionDTO.getCollectionId());
                product.setLikeCount(product.getLikeCount() + val);
                productMapper.updateById(product);
                break;
            case 2:
                WebComment comment = commentMapper.selectById(collectionDTO.getCollectionId());
                if (comment == null) {
                    WebCommentSync commentSync = commentSyncMapper.selectById(collectionDTO.getCollectionId());
                    commentSync.setLikeCount(commentSync.getLikeCount() + val);
                    commentSyncMapper.updateById(commentSync);
                } else {
                    comment.setLikeCount(comment.getLikeCount() + val);
                    commentMapper.updateById(comment);
                }
                break;
            case 3:
//                String currentUid = WebUtils.getRequestHeader(UserConstant.USER_ID);
                String currentUid = AuthContextHolder.getUserId();
                IdleProduct collectionProduct = productMapper.selectById(collectionDTO.getCollectionId());
                //收藏图片
//                collectionProduct.getLikeCount(collectionProduct.getCollectionCount() + val);
                productMapper.updateById(collectionProduct);

                WebAlbumNoteRelation albumNoteRelation = new WebAlbumNoteRelation();
                albumNoteRelation.setNid(String.valueOf(collectionProduct.getId()));
                if (val == 1) {
                    WebAlbum album = albumMapper.selectOne(new QueryWrapper<WebAlbum>().eq("uid", currentUid).eq("type", 0));
                    Long imgCount = collectionProduct.getLikeCount();
                    if (ObjectUtils.isEmpty(album)) {
                        album = new WebAlbum();
                        album.setTitle("默认专辑");
                        album.setUid(currentUid);
                        album.setAlbumCover(collectionProduct.getCover());
                        album.setImgCount(imgCount);
                        album.setCreateTime(new Date());
                        album.setUpdateTime(new Date());
                        albumMapper.insert(album);
                    } else {
                        album.setImgCount(album.getImgCount() + imgCount);
                        if (StringUtils.isBlank(album.getAlbumCover())) {
                            album.setAlbumCover(collectionProduct.getCover());
                        }
                        albumMapper.updateById(album);
                    }
                    albumNoteRelation.setAid(String.valueOf(album.getId()));
                    albumNoteRelation.setCreateTime(new Date());
                    albumNoteRelation.setUpdateTime(new Date());
                    albumNoteRelationMapper.insert(albumNoteRelation);
                } else {
                    List<WebAlbumNoteRelation> albumNoteRelationList = albumNoteRelationMapper.selectList(new QueryWrapper<WebAlbumNoteRelation>().eq("nid", collectionProduct.getId()));
                    Set<String> aids = albumNoteRelationList.stream().map(WebAlbumNoteRelation::getAid).collect(Collectors.toSet());
                    List<WebAlbum> albumList = albumMapper.selectBatchIds(aids);
                    WebAlbum album = albumList.stream().filter(item -> item.getUid().equals(currentUid)).findFirst().orElse(null);
                    Long imgCount = collectionProduct.getLikeCount();
                    long nums = album.getImgCount() - imgCount;
                    if (nums <= 0) {
                        album.setAlbumCover(null);
                    }
                    album.setImgCount(nums);
                    albumMapper.updateById(album);
                    albumNoteRelationMapper.delete(new QueryWrapper<WebAlbumNoteRelation>().eq("aid", album.getId()).eq("nid", collectionProduct.getId()));
                }
                break;
            default:
                // 收藏专辑
                WebAlbum collectAlbum = albumMapper.selectById(collectionDTO.getCollectionId());
                collectAlbum.setCollectionCount(collectAlbum.getCollectionCount() + val);
                albumMapper.updateById(collectAlbum);
                break;
        }
    }
}
