package com.hongshu.idle.service.sys;

import com.hongshu.idle.domain.IdleNavbarTreeSelect;
import com.hongshu.idle.domain.entity.IdleCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 导航栏 业务层
 *

 */
public interface ISysIdleNavbarService {

    /**
     * 根据用户查询系统导航栏列表
     *
     * @param category 导航栏信息
     */
    List<IdleCategory> selectNavbarList(IdleCategory category);

    /**
     * 根据导航栏ID查询信息
     *
     * @param id 导航栏ID
     * @return 导航栏信息
     */
    IdleCategory selectById(Long id);

    /**
     * 构建前端所需要树结构
     *
     * @param categoryList 菜单列表
     * @return 树结构列表
     */
    List<IdleCategory> buildNavbarTree(List<IdleCategory> categoryList);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param categoryList 导航栏列表
     */
    List<IdleNavbarTreeSelect> buildNavbarTreeSelect(List<IdleCategory> categoryList);

    /**
     * 是否存在导航栏子节点
     *
     * @param id 导航栏ID
     * @return 结果 true 存在 false 不存在
     */
    boolean hasChildById(Long id);

//    /**
//     * 构建前端所需要树结构
//     *
//     * @param categoryList 导航栏列表
//     */
//    List<WebCategory> buildNavbarTree(List<WebCategory> categoryList);

//    /**
//     * 构建前端所需要下拉树结构
//     *
//     * @param categoryList 导航栏列表
//     */
//    List<NavbarTreeSelect> buildNavbarTreeSelect(List<WebCategory> categoryList);

    /**
     * 新增导航栏信息
     *
     * @param category 导航栏信息
     * @return 结果
     */
    int insertNavbar(IdleCategory category, MultipartFile file);

    /**
     * 修改导航栏信息
     *
     * @param category 导航栏信息
     * @return 结果
     */
    int updateNavbar(IdleCategory category, MultipartFile file);

    /**
     * 删除导航栏信息
     *
     * @param id 导航栏ID
     * @return 结果
     */
    int deleteById(Long id);

    /**
     * 更新导航栏状态
     */
    void updateStatus(Long id, String status);

//    /**
//     * 校验导航栏名称是否唯一
//     *
//     * @param category 导航栏信息
//     * @return 结果
//     */
//    boolean checkWebNavbarNameUnique(WebCategory category);
}
