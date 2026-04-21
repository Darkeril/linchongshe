package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.web.domain.NavbarTreeSelect;
import com.hongshu.web.domain.entity.WebCategory;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.mapper.sys.SysNavbarMapper;
import com.hongshu.web.mapper.sys.SysNoteMapper;
import com.hongshu.web.service.sys.ISysNavbarService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 导航栏 业务层处理
 *

 */
@Service
public class SysNavbarServiceImpl implements ISysNavbarService {

//    @Autowired
//    private IOssService ossService;
    @Autowired
    private RemoteFileService remoteFileService;
    @Autowired
    private SysNoteMapper noteMapper;
    @Autowired
    private SysNavbarMapper navbarMapper;
//    @Value("${oss.type}")
//    Integer type;


    /**
     * 查询系统导航栏列表
     *
     * @param category 导航栏信息
     * @return 导航栏列表
     */
    @Override
    public List<WebCategory> selectNavbarList(WebCategory category) {
        QueryWrapper<WebCategory> qw = new QueryWrapper<>();
        // 只查询未删除的分类
        qw.lambda().eq(WebCategory::getDeleted, 0);
        qw.lambda().like(ValidatorUtil.isNotNull(category.getTitle()), WebCategory::getTitle, category.getTitle());
        qw.lambda().like(ValidatorUtil.isNotNull(category.getStatus()), WebCategory::getStatus, category.getStatus());

        if ("sort".equals(category.getOrderColumn())) {
            if ("asc".equals(category.getTimeOrder())) {
                qw.orderByAsc("sort");
            } else {
                qw.orderByDesc("sort");
            }
        } else if ("createTime".equals(category.getOrderColumn())) {
            if ("asc".equals(category.getTimeOrder())) {
                qw.orderByAsc("create_time");
            } else {
                qw.orderByDesc("create_time");
            }
        } else if ("updateTime".equals(category.getOrderColumn())) {
            if ("asc".equals(category.getTimeOrder())) {
                qw.orderByAsc("update_time");
            } else {
                qw.orderByDesc("update_time");
            }
        } else {
            // 默认按sort升序
            qw.orderByAsc("sort");
        }
        List<WebCategory> categoryList = navbarMapper.selectList(qw);

        // 查询所有分类下的笔记数量
        List<Map<String, Object>> noteCounts = noteMapper.countNoteByCategory();
        // 转成Map方便查找
        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> map : noteCounts) {
            String cpid = String.valueOf(map.get("cpid"));
            Long count = ((Number) map.get("count")).longValue();
            countMap.put(cpid, count);
        }
        // 填充noteCount
        for (WebCategory cat : categoryList) {
            Long count = countMap.getOrDefault(cat.getId(), 0L);
            cat.setNoteCount(String.valueOf(count));
        }
        return categoryList;
    }

    /**
     * 根据导航栏ID查询信息
     *
     * @param id 导航栏ID
     * @return 导航栏信息
     */
    @Override
    public WebCategory selectById(Long id) {
        QueryWrapper<WebCategory> qw = new QueryWrapper<>();
        qw.lambda().eq(WebCategory::getId, id).eq(WebCategory::getDeleted, 0);
        return navbarMapper.selectOne(qw);
    }

    /**
     * 是否存在导航栏子节点
     *
     * @param id 导航栏ID
     * @return 结果
     */
    @Override
    public boolean hasChildById(Long id) {
        int result = navbarMapper.hasChildById(id);
        return result > 0;
    }

    /**
     * 新增导航栏信息
     *
     * @param category 导航栏信息
     * @return 结果
     */
    @Override
    public int insertNavbar(WebCategory category, MultipartFile file) {
        // 如果有文件，上传文件并设置封面图
        if (ObjectUtils.isNotEmpty(file)) {
//            String normalCover = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String normalCover = upload.getData().getUrl();
            category.setNormalCover(normalCover);
        }
        category.setCreator("system");
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        // 设置删除标识为0（正常状态）
        if (category.getDeleted() == null) {
            category.setDeleted(0);
        }
        return navbarMapper.insert(category);
    }

    /**
     * 修改导航栏信息
     *
     * @param category 导航栏信息
     * @return 结果
     */
    @Override
    public int updateNavbar(WebCategory category, MultipartFile file) {
        if (ObjectUtils.isNotEmpty(file)) {
//            String normalCover = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String normalCover = upload.getData().getUrl();
            category.setNormalCover(normalCover);
        }
        category.setUpdater("system");
        category.setUpdateTime(new Date());
        return navbarMapper.updateById(category);
    }

    /**
     * 删除导航栏信息
     *
     * @param id 导航栏ID
     * @return 结果
     */
    @Override
    public int deleteById(Long id) {
        // 检查该导航栏下是否有笔记数据（只检查未删除的笔记）
        QueryWrapper<WebNote> noteQw = new QueryWrapper<>();
        noteQw.lambda().eq(WebNote::getCpid, id).eq(WebNote::getDeleted, 0);
        long noteCount = noteMapper.selectCount(noteQw);
        if (noteCount > 0) {
            throw new HongshuException("该导航栏下存在笔记数据，无法删除");
        }
        // 逻辑删除：更新 deleted 字段为 1
        WebCategory category = navbarMapper.selectById(id);
        if (category != null) {
            category.setDeleted(1);
            category.setUpdateTime(new Date());
            return navbarMapper.updateById(category);
        }
        return 0;
    }

    /**
     * 更新导航栏状态
     */
    @Override
    public void updateStatus(Long id, String status) {
        WebCategory category = navbarMapper.selectById(id);
        if (ObjectUtils.isNotEmpty(category)) {
            category.setStatus(status);
            category.setUpdateTime(new Date());
            navbarMapper.updateById(category);
        }
    }

//    /**
//     * 校验导航栏名称是否唯一
//     *
//     * @param category 导航栏信息
//     * @return 结果
//     */
//    @Override
//    public boolean checkWebNavbarNameUnique(WebCategory category) {
//        Long id = StringUtils.isNull(category.getId()) ? -1L : category.getId();
//        WebCategory info = category.checkWebNavbarNameUnique(category.getTitle(), category.getPid());
//        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue()) {
//            return UserConstants.NOT_UNIQUE;
//        }
//        return UserConstants.UNIQUE;
//    }

    /**
     * 递归列表
     *
     * @param list 分类表
     * @param t    子节点
     */
    private void recursionFn(List<WebCategory> list, WebCategory t) {
        // 得到子节点列表
        List<WebCategory> childList = this.getChildList(list, t);
        t.setChildren(childList);
        for (WebCategory tChild : childList) {
            if (hasChild(list, tChild)) {
                this.recursionFn(list, tChild);
            }
        }
    }

    /**
     * 构建前端所需要树结构
     *
     * @param categoryList 菜单列表
     * @return 树结构列表
     */
    @Override
    public List<WebCategory> buildNavbarTree(List<WebCategory> categoryList) {
        List<WebCategory> returnList = new ArrayList<>();
        List<String> tempList = categoryList.stream().map(WebCategory::getId).collect(Collectors.toList());
        for (Iterator<WebCategory> iterator = categoryList.iterator(); iterator.hasNext(); ) {
            WebCategory category = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(category.getPid())) {
                this.recursionFn(categoryList, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty()) {
            returnList = categoryList;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param categoryList 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<NavbarTreeSelect> buildNavbarTreeSelect(List<WebCategory> categoryList) {
        List<WebCategory> navbarTrees = this.buildNavbarTree(categoryList);
        return navbarTrees.stream().map(NavbarTreeSelect::new).collect(Collectors.toList());
    }

//    /**
//     * 根据父节点的ID获取所有子节点
//     *
//     * @param list 分类表
//     * @param pid  传入的父节点ID
//     * @return String
//     */
//    public List<WebCategory> getChildPerms(List<WebCategory> list, int pid) {
//        List<WebCategory> returnList = new ArrayList<WebCategory>();
//        for (Iterator<WebCategory> iterator = list.iterator(); iterator.hasNext(); ) {
//            WebCategory t = iterator.next();
//            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
//            if (t.getPid() == pid) {
//                recursionFn(list, t);
//                returnList.add(t);
//            }
//        }
//        return returnList;
//    }

//    /**
//     * 递归列表
//     *
//     * @param list 分类表
//     * @param t    子节点
//     */
//    private void recursionFn(List<WebCategory> list, WebCategory t) {
//        // 得到子节点列表
//        List<WebCategory> childList = getChildList(list, t);
//        t.setChildren(childList);
//        for (WebCategory tChild : childList) {
//            if (hasChild(list, tChild)) {
//                recursionFn(list, tChild);
//            }
//        }
//    }

    /**
     * 得到子节点列表
     */
    private List<WebCategory> getChildList(List<WebCategory> list, WebCategory t) {
        List<WebCategory> tlist = new ArrayList<>();
        Iterator<WebCategory> it = list.iterator();
        while (it.hasNext()) {
            WebCategory n = it.next();
            if (n.getPid() == (t.getId())) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<WebCategory> list, WebCategory t) {
        return getChildList(list, t).size() > 0;
    }
}
