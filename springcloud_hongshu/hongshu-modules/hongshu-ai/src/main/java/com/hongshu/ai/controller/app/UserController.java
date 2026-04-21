package com.hongshu.ai.controller.app;

import com.hongshu.ai.common.FileInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.constant.OssConstant;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.controller.base.BaseAppController;
import com.hongshu.ai.domain.command.SysUserShareCommand;
import com.hongshu.ai.domain.command.UserCommand;
import com.hongshu.ai.domain.command.UserPasswordCommand;
import com.hongshu.ai.domain.vo.ModelVO;
import com.hongshu.ai.domain.vo.UserVO;
import com.hongshu.ai.service.IBaseConfigService;
import com.hongshu.ai.service.IModelService;
import com.hongshu.ai.service.IUserService;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 会员信息接口
 *
 * @author: myj
 * @date: 2023/5/4
 * @version: 1.0.0
 */
@RestController(value = "appUserController")
@RequestMapping("/app/user")
public class UserController extends BaseAppController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IBaseConfigService baseConfigService;
    @Autowired
    private RemoteFileService remoteFileService;


    /**
     * 获取用户信息接口
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping()
    public ResponseInfo<UserVO> getUserInfo() {
        return userService.getLoginUserById(getLoginUser().getId());
    }

    /**
     * 获取用户模型接口
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/model")
    public ResponseInfo<List<ModelVO>> getUserModel() {
        Query query = new Query();
        query.put("status", StatusEnum.ENABLED.getValue());
        return modelService.listModel(query);
    }

    /**
     * 修改个人信息
     *
     * @author: myj
     * @date: 2024/4/1
     * @version: 1.0.0
     */
    @PutMapping()
    public ResponseInfo updateUser(@RequestBody UserCommand command) {
        command.setId(getUserId());
        return userService.updateUser(command);
    }

    /**
     * 修改账号头像
     *
     * @author: myj
     * @date: 2024/4/1
     * @version: 1.0.0
     */
    @PostMapping("/avatar")
    public ResponseInfo<FileInfo> updateUserAvatar(@RequestParam("file") MultipartFile file) {

        FileInfo fileInfo = new FileInfo();
        R<SysFile> upload = remoteFileService.upload(file);
        String avatar = upload.getData().getUrl();
        fileInfo.setFileUrl(avatar);
        if (avatar == null) {
            throw new RuntimeException("头像上传失败");
        }
        userService.updateUserAvatar(getUserId(), avatar);
        return ResponseInfo.success(fileInfo);

//        String pathName = "avatar/";
//        ExtraInfoDTO extraInfo = baseConfigService.getBaseConfigByName("extraInfo", ExtraInfoDTO.class);
//        FileInfo fileInfo = null;
//        if (ValidatorUtil.isNull(extraInfo) || ValidatorUtil.isNull(extraInfo.getOssType()) || OssEnum.LOCAL.getValue().equals(extraInfo.getOssType())) {
//            String filePath = SystemConfig.uploadPath + getPathName(pathName);
//            fileInfo = FileUploadUtils.upload(filePath, file);
//            fileInfo.setFileUrl(fileInfo.getFileUrl());
//        } else if (OssEnum.ALI.getValue().equals(extraInfo.getOssType())) {
//            fileInfo = AliyunOSSUtil.uploadFile(extraInfo, file, getPathName(pathName));
//        } else if (OssEnum.TECENT.getValue().equals(extraInfo.getOssType())) {
//            fileInfo = TencentCOSUtil.upload(extraInfo, file, FileUploadUtils.getPathName(pathName));
//        } else {
//            return ResponseInfo.validateFail("未知的上传文件方式，上传失败");
//        }
//        if (ValidatorUtil.isNull(fileInfo) || ValidatorUtil.isNull(fileInfo.getFileUrl())) {
//            return ResponseInfo.validateFail("上传失败");
//        }
//        userService.updateUserAvatar(getUserId(), fileInfo.getFileUrl());
//        return ResponseInfo.success(fileInfo);
    }

    /**
     * 获取文件路径名称
     *
     * @param pathName
     * @return
     */
    private String getPathName(String pathName) {
        if (ValidatorUtil.isNull(pathName)) {
            return OssConstant.DEFAULT_FILE;
        }
        if (pathName.startsWith(StringPoolConstant.SLASH)) {
            pathName = pathName.substring(1, pathName.length());
        }
        if (!pathName.endsWith(StringPoolConstant.SLASH)) {
            pathName = pathName + StringPoolConstant.SLASH;
        }
        return pathName;
    }

    /**
     * 修改密码
     *
     * @author: myj
     * @date: 2024/4/1
     * @version: 1.0.0
     */
    @PutMapping("/password/update")
    public ResponseInfo updatePassword(@RequestBody UserPasswordCommand command) {
        return userService.updatePassword(command);
    }

    /**
     * 开启/关闭上下文
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @PutMapping("/context")
    public ResponseInfo openContext(@RequestBody UserCommand command) {
        command.setOperaterId(getUserId());
        return userService.updateUserContext(command);
    }

    /**
     * 账户分销
     *
     * @author: myj
     * @date: 2024/4/1
     * @version: 1.0.0
     */
    @PutMapping("/share")
    public ResponseInfo shareAccount(@RequestBody SysUserShareCommand command) {
        return userService.shareAccount(command);
    }

}
