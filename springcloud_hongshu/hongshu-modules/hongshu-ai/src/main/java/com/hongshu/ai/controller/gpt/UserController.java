package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.domain.command.UserCommand;
import com.hongshu.ai.domain.vo.UserVO;
import com.hongshu.ai.service.IUserService;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员用户接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;


    /**
     * 查询会员用户分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:user:list')" )
    public ResponseInfo<IPageInfo<UserVO>> pageUser(@RequestParam Map map) {
        return userService.pageUser(new Query(map, true));
    }

    /**
     * 查询会员用户列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:user:list')" )
    public ResponseInfo<List<UserVO>> listUser(@RequestParam Map map) {
        return userService.listUser(new Query(map));
    }

    /**
     * 获取会员用户详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:user:query')" )
    public ResponseInfo<UserVO> getUserById(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    /**
     * 新增会员用户
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PostMapping
//    @PreAuthorize("hasAuthority('gpt:user:save')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveUser(@Validated @RequestBody UserCommand command) {
        return userService.saveUser(command);
    }

    /**
     * 修改会员用户
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PutMapping
//    @PreAuthorize("hasAuthority('gpt:user:update')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateUser(@Validated @RequestBody UserCommand command) {
        return userService.updateUser(command);
    }

    /**
     * 批量删除会员用户
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:user:remove')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeUserByIds(@PathVariable List<Long> ids) {
        return userService.removeUserByIds(ids);
    }

}
