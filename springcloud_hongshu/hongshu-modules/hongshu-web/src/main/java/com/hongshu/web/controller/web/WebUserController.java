package com.hongshu.web.controller.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.hongshu.common.core.constant.HttpStatus;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.web.domain.entity.IdleProduct;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.IdleProductVO;
import com.hongshu.web.service.web.IWebUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.hongshu.common.core.utils.PageUtils.startPage;

/**
 * 用户
 *
 * @author: hongshu
 */
@RequestMapping("/user")
@RestController
public class WebUserController {

    @Autowired
    private IWebUserService userService;


    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    @GetMapping("getTrendByUser/{currentPage}/{pageSize}")
    public Result<?> getTrendByUser(@PathVariable long currentPage, @PathVariable long pageSize, String userId, Integer type, String status) {
        Page<NoteSearchVo> pageInfo = userService.getTrendByUser(currentPage, pageSize, userId, type, status);
        return Result.ok(pageInfo);
    }

    /**
     * 根据用户获取闲置商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    @GetMapping("getProductByUser/{currentPage}/{pageSize}")
    public Result<?> getProductByUser(@PathVariable long currentPage,
                                      @PathVariable long pageSize,
                                      String userId,
                                      Integer type,
                                      @RequestParam(required = false) String status) {
        Page<IdleProductVO> pageInfo = userService.getProductByUser(currentPage, pageSize, userId, type, status);
        return Result.ok(pageInfo);
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     */
    @GetMapping("getUserById")
    public Result<?> getUserById(String userId) {
        WebUser user = userService.getUserById(userId);
        return Result.ok(user);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    @PostMapping("updateUser")
    public Result<?> updateUser(@RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
                                @RequestPart("user") WebUser user) {
        WebUser updateUser = userService.updateUser(avatarFile, user);
        return Result.ok(updateUser);
    }

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     */
    @GetMapping("getUserByKeyword/{currentPage}/{pageSize}")
    public Result<?> getUserByKeyword(@PathVariable long currentPage, @PathVariable long pageSize, String keyword) {
        Page<WebUser> pageInfo = userService.getUserByKeyword(currentPage, pageSize, keyword);
        return Result.ok(pageInfo);
    }

    /**
     * 保存用户的搜索记录
     *
     * @param keyword 关键词
     */
    @GetMapping("saveUserSearchRecord")
    public Result<?> saveUserSearchRecord(String keyword) {
        userService.saveUserSearchRecord(keyword);
        return Result.ok();
    }

    /**
     * 会员列表
     *
     * @param user 用户
     */
    @GetMapping("getUserList")
    public TableDataInfo getUserList(WebUser user) {
        startPage();
        List<WebUser> userList = userService.getUserList(user);
        return getDataTable(userList);
    }

    @GetMapping("collectCount")
    public Result<?> getCollectCount(String userId) {
        return Result.ok(userService.getCollectCount(userId));
    }

    @GetMapping("getUserProducts")
    public Result<?> getUserProducts(@RequestParam Map map) {
        Page<IdleProduct> pageInfo = userService.getUserProducts(map);
        return Result.ok(pageInfo);
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }
}
