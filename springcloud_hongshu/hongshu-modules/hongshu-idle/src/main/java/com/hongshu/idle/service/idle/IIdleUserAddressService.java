package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.entity.IdleUserAddress;

import java.util.List;

/**
 * 地址
 *
 
 */
public interface IIdleUserAddressService extends IService<IdleUserAddress> {

    List<IdleUserAddress> getAddressList(String id);

    IdleUserAddress getAddressById(String id);

    void saveAddress(String addressData);

    void updateAddress(String addressData);

    void deleteAddress(String id);

    void setDefaultAddress(String id, String uid);
}
