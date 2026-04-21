import {
	$request
} from '../utils/request.js'

/**
 * 获取网站配置
 */
export const getWebsiteConfig = () => {
	return $request({
		url: '/web/system/config/websiteConfig',
		method: 'GET',
		showLoading: false,
		cache: false // 禁用缓存，确保每次都获取最新配置
	})
}

/**
 * 获取市集页轮播图配置（用于商城/市集顶部轮播）
 */
export const getCarouselConfig = () => {
	return $request({
		url: '/web/system/config/carousel',
		method: 'GET',
		showLoading: false,
		cache: false // 禁用缓存，管理端修改后刷新即可看到最新轮播图
	})
}




