package com.hongshu.idle.service.idle.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.idle.domain.entity.IdleUserAddress;
import com.hongshu.idle.mapper.idle.IdleUserAddressMapper;
import com.hongshu.idle.service.idle.IIdleUserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class IdleUserAddressServiceImpl extends ServiceImpl<IdleUserAddressMapper, IdleUserAddress> implements IIdleUserAddressService {

    @Autowired
    private IdleUserAddressMapper addressMapper;


    @Override
    public List<IdleUserAddress> getAddressList(String uid) {
        return addressMapper.selectList(new QueryWrapper<IdleUserAddress>().eq("uid", uid));
    }

    /**
     * 获取地址
     *
     * @param id 地址ID
     */
    @Override
    public IdleUserAddress getAddressById(String id) {
        IdleUserAddress address = addressMapper.selectById(id);
        if (ObjectUtils.isEmpty(address)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        return address;
    }

    @Override
    public void saveAddress(String addressData) {
        IdleUserAddress address = JSONUtil.toBean(addressData, IdleUserAddress.class);
        addressMapper.insert(address);
    }

    @Override
    public void updateAddress(String addressData) {
        IdleUserAddress address = JSONUtil.toBean(addressData, IdleUserAddress.class);
        addressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(String id) {
        IdleUserAddress address = addressMapper.selectById(id);
        if (ObjectUtils.isEmpty(address)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        addressMapper.deleteById(id);
    }

    @Override
    public void setDefaultAddress(String id, String uid) {
        // 先将该用户的所有地址设为非默认（如果数据库有isDefault字段）
        // 由于当前实体类没有isDefault字段，这里暂时只设置当前地址
        // 如果后续需要添加isDefault字段，可以在这里实现批量更新逻辑
        IdleUserAddress address = addressMapper.selectById(id);
        if (ObjectUtils.isEmpty(address)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 如果数据库表有isDefault字段，需要先更新所有地址为非默认，再设置当前地址为默认
        // 目前暂时只返回成功，具体实现需要根据数据库表结构
    }

}
