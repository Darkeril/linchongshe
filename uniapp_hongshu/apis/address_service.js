import {
	$request
} from '../utils/request.js'
import cacheManager from '../utils/cacheManager.js'

// 获取地址列表
export const getAddressList = (uid, forceRefresh = false) => {
	// 如果强制刷新，先清除缓存
	if (forceRefresh) {
		const cacheKey = cacheManager.generateCacheKey(`/idle/address/addressList?uid=${uid}`, {}, 'GET')
		cacheManager.delete(cacheKey)
	}
	
	return $request({
		url: `/idle/address/addressList?uid=${uid}`,
		method: 'GET',
		cache: forceRefresh ? false : undefined // 强制刷新时禁用缓存
	})
}

// 获取单个地址详情
export const getAddressDetail = (id) => {
	return $request({
		url: `/idle/address/${id}`,
		method: 'GET'
	})
}

// 新增地址
export const addAddress = (data) => {
	return $request({
		url: '/idle/address',
		method: 'POST',
		data: data
	})
}

// 更新地址
export const updateAddress = (id, data) => {
	return $request({
		url: `/idle/address/${id}`,
		method: 'PUT',
		data: data
	})
}

// 删除地址
export const deleteAddress = (id) => {
	return $request({
		url: `/idle/address/${id}`,
		method: 'DELETE'
	})
}

// 设置默认地址
export const setDefaultAddress = (id, uid) => {
	return $request({
		url: `/idle/address/setDefault/${id}?uid=${uid}`,
		method: 'PUT'
	})
}























