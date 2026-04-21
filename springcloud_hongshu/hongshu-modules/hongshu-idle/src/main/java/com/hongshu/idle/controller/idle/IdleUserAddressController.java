package com.hongshu.idle.controller.idle;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.entity.IdleUserAddress;
import com.hongshu.idle.service.idle.IIdleUserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址
 *
 * @author: hongshu
 */
@RequestMapping("/address")
@RestController
public class IdleUserAddressController {

    @Autowired
    private IIdleUserAddressService addressService;


    /**
     * 获取地址列表
     */
    @GetMapping("addressList")
    @NoLoginIntercept
    public Result<?> getAddressList(String uid) {
        List<IdleUserAddress> addressList = addressService.getAddressList(uid);
        return Result.ok(addressList);
    }

    /**
     * 获取地址
     *
     * @param id 地址ID
     */
    @GetMapping("getAddress")
    @NoLoginIntercept
    public Result<?> getAddressById(String id) {
        IdleUserAddress address = addressService.getAddressById(id);
        return Result.ok(address);
    }

    /**
     * 获取地址详情（RESTful风格）
     * 注意：此接口必须放在其他具体路径之后，避免与 addressList、getAddress 等路径冲突
     *
     * @param id 地址ID
     */
    @GetMapping("{id}")
    @NoLoginIntercept
    public Result<?> getAddressByIdRest(@PathVariable String id) {
        IdleUserAddress address = addressService.getAddressById(id);
        return Result.ok(address);
    }

    /**
     * 新增地址（RESTful风格）
     *
     * @param address 地址对象
     */
    @PostMapping
    public Result<?> addAddress(@RequestBody IdleUserAddress address) {
        // 新增地址，如果ID为空或null
        if (address.getId() == null || address.getId().isEmpty()) {
            addressService.saveAddress(JSONUtil.toJsonStr(address));
        } else {
            // 如果有ID，执行更新
            addressService.updateAddress(JSONUtil.toJsonStr(address));
        }
        return Result.ok();
    }

    /**
     * 新增地址
     *
     * @param address 地址对象
     */
    @PostMapping("saveAddress")
    public Result<?> saveAddress(@RequestBody IdleUserAddress address) {
        if (address.getId() != null && !address.getId().isEmpty()) {
            // 如果有ID，执行更新
            addressService.updateAddress(JSONUtil.toJsonStr(address));
        } else {
            // 没有ID，执行新增
            addressService.saveAddress(JSONUtil.toJsonStr(address));
        }
        return Result.ok();
    }

    /**
     * 更新地址
     *
     * @param address 地址对象
     */
    @PostMapping("updateAddress")
    public Result<?> updateAddress(@RequestBody IdleUserAddress address) {
        addressService.updateAddress(JSONUtil.toJsonStr(address));
        return Result.ok();
    }

    /**
     * 更新地址（RESTful风格）
     *
     * @param id 地址ID
     * @param address 地址对象
     */
    @PutMapping("{id}")
    public Result<?> updateAddressRest(@PathVariable String id, @RequestBody IdleUserAddress address) {
        address.setId(id);
        addressService.updateAddress(JSONUtil.toJsonStr(address));
        return Result.ok();
    }

    /**
     * 删除地址
     *
     * @param id 地址对象
     */
    @PostMapping("deleteAddress")
    public Result<?> deleteAddress(String id) {
        addressService.deleteAddress(id);
        return Result.ok();
    }

    /**
     * 删除地址（RESTful风格）
     *
     * @param id 地址ID
     */
    @DeleteMapping("{id}")
    public Result<?> deleteAddressRest(@PathVariable String id) {
        addressService.deleteAddress(id);
        return Result.ok();
    }

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @param uid 用户ID
     */
    @PutMapping("setDefault/{id}")
    public Result<?> setDefaultAddress(@PathVariable String id, @RequestParam String uid) {
        addressService.setDefaultAddress(id, uid);
        return Result.ok();
    }

}
