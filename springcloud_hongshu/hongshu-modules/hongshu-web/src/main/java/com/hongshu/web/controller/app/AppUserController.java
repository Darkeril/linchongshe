package com.hongshu.web.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.hongshu.common.core.constant.HttpStatus;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.web.domain.dto.UserAppDTO;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.service.web.IWebUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.hongshu.common.core.utils.PageUtils.startPage;

/**
 * 用户
 *
 * @author: hongshu
 */
@RequestMapping("/app/user")
@RestController
public class AppUserController {

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
    public Result<?> getTrendByUser(@PathVariable long currentPage,
                                    @PathVariable long pageSize,
                                    String userId,
                                    Integer type,
                                    String status) {
        Page<NoteSearchVo> pageInfo = userService.getTrendByUser(currentPage, pageSize, userId, type, status);
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

    @PostMapping("updateAvatarUrl")
    public Result<?> updateAvatarUrl(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateAvatarUrl(userAppDTO);
        return Result.ok(updateUser);
    }

    /**
     * 更新用户头像（接收文件）
     *
     * @param file 头像文件
     * @param userId 用户ID
     */
    @PostMapping(value = "updateAvatar", consumes = "multipart/form-data")
    public Result<?> updateAvatar(@RequestPart("file") MultipartFile file,
                                   @RequestParam("userId") String userId) {
        WebUser updateUser = userService.updateAvatar(file, userId);
        return Result.ok(updateUser);
    }

    @PostMapping("updateUsername")
    public Result<?> updateUsername(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateUsername(userAppDTO);
        return Result.ok(updateUser);
    }

    @PostMapping("updateDes")
    public Result<?> updateDes(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateDes(userAppDTO);
        return Result.ok(updateUser);
    }

    @PostMapping("updateTags")
    public Result<?> updateTags(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateTags(userAppDTO);
        return Result.ok(updateUser);
    }

    @PostMapping("updateBackgroundImage")
    public Result<?> updateBackgroundImage(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateBackgroundImage(userAppDTO);
        return Result.ok(updateUser);
    }

    /**
     * 更新用户背景图（接收文件）
     *
     * @param file 背景图文件
     * @param userId 用户ID
     */
    @PostMapping(value = "updateBackgroundImageFile", consumes = "multipart/form-data")
    public Result<?> updateBackgroundImageFile(@RequestPart("file") MultipartFile file,
                                               @RequestParam("userId") String userId) {
        WebUser updateUser = userService.updateBackgroundImageFile(file, userId);
        return Result.ok(updateUser);
    }

    @PostMapping("updateSex")
    public Result<?> updateSex(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateSex(userAppDTO);
        return Result.ok(updateUser);
    }

    @PostMapping("updateBirthday")
    public Result<?> updateBirthday(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateBirthday(userAppDTO);
        return Result.ok(updateUser);
    }

    @PostMapping("updateArea")
    public Result<?> updateArea(@RequestBody UserAppDTO userAppDTO) {
        WebUser updateUser = userService.updateArea(userAppDTO);
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
