package com.hongshu.ai.controller.gpt;

import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.annotation.Log;
import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.constant.SysLogTypeConstant;
import com.hongshu.ai.domain.command.CombCommand;
import com.hongshu.ai.domain.vo.CombVO;
import com.hongshu.ai.service.ICombService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员套餐接口
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/gpt/comb")
public class CombController extends BaseController {

    @Autowired
    private ICombService combService;


    /**
     * 查询会员套餐分页列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/page")
//    @PreAuthorize("hasAuthority('gpt:comb:list')" )
    public ResponseInfo<IPageInfo<CombVO>> pageComb(@RequestParam Map map) {
        return combService.pageComb(new Query(map, true));
    }

    /**
     * 查询会员套餐列表
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('gpt:comb:list')" )
    public ResponseInfo<List<CombVO>> listComb(@RequestParam Map map) {
        return combService.listComb(new Query(map));
    }

    /**
     * 获取会员套餐详细信息
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @GetMapping(value = "/{id}")
//    @PreAuthorize("hasAuthority('gpt:comb:query')" )
    public ResponseInfo<CombVO> getCombById(@PathVariable("id") Long id) {
        return combService.getCombById(id);
    }

    /**
     * 新增会员套餐
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PostMapping
//    @PreAuthorize("hasAuthority('gpt:comb:save')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.INSERT)
    public ResponseInfo saveComb(@Validated @RequestBody CombCommand command) {
        return combService.saveComb(command);
    }

    /**
     * 修改会员套餐
     *
     * @author: myj
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @PutMapping
//    @PreAuthorize("hasAuthority('gpt:comb:update')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.UPDATE)
    public ResponseInfo updateComb(@Validated @RequestBody CombCommand command) {
        return combService.updateComb(command);
    }

    /**
     * 批量删除会员套餐
     *
     * @author: myj false
     * @date: 2023-04-28
     * @version: 1.0.0
     */
    @DeleteMapping("/{ids}")
//    @PreAuthorize("hasAuthority('gpt:comb:remove')" )
    @Log(type = SysLogTypeConstant.DEFAULT, businessType = BusinessTypeEnum.DELETE)
    public ResponseInfo removeCombByIds(@PathVariable List<Long> ids) {
        return combService.removeCombByIds(ids);
    }

}
