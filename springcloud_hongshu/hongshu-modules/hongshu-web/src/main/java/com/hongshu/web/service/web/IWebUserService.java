package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.dto.UserAppDTO;
import com.hongshu.web.domain.entity.IdleProduct;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.IdleProductVO;
import com.hongshu.web.domain.vo.UserLocationVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户
 *
 * @author: hongshu
 */
public interface IWebUserService extends IService<WebUser> {

    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    Page<NoteSearchVo> getTrendByUser(long currentPage, long pageSize, String userId, Integer type, String status);

    /**
     * 根据用户获取闲置商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    Page<IdleProductVO> getProductByUser(long currentPage, long pageSize, String userId, Integer type, String status);

    WebUser getUserById(String userId);

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    WebUser updateUser(MultipartFile avatarFile, WebUser user);

    WebUser updateAvatarUrl(UserAppDTO userAppDTO);

    /**
     * 更新用户头像（接收文件）
     *
     * @param file 头像文件
     * @param userId 用户ID
     */
    WebUser updateAvatar(MultipartFile file, String userId);

    WebUser updateUsername(UserAppDTO userAppDTO);

    WebUser updateDes(UserAppDTO userAppDTO);

    WebUser updateTags(UserAppDTO userAppDTO);

    WebUser updateBackgroundImage(UserAppDTO userAppDTO);

    /**
     * 更新用户背景图（接收文件）
     *
     * @param file 背景图文件
     * @param userId 用户ID
     */
    WebUser updateBackgroundImageFile(MultipartFile file, String userId);

    WebUser updateSex(UserAppDTO userAppDTO);

    WebUser updateBirthday(UserAppDTO userAppDTO);

    WebUser updateArea(UserAppDTO userAppDTO);

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     */
    Page<WebUser> getUserByKeyword(long currentPage, long pageSize, String keyword);

    /**
     * 保存用户的搜索记录
     *
     * @param keyword 关键词
     */
    void saveUserSearchRecord(String keyword);

    /**
     * 根据条件分页查询会员数据
     *
     * @param user 会员
     */
    List<WebUser> getUserList(WebUser user);

    Object getCollectCount(String userId);

    Page<IdleProduct> getUserProducts(Map map);

    List<UserLocationVO> getAllUserLocations();

}
