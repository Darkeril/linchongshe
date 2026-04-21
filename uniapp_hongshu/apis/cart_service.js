import { $request } from '@/utils/request'

/**
 * 获取购物车商品列表（收藏的商品）
 * @param {Number} currentPage 当前页
 * @param {Number} pageSize 每页数量
 */
export const getCartItems = (currentPage = 1, pageSize = 20) => {
	console.log('获取收藏的商品列表, currentPage:', currentPage, 'pageSize:', pageSize)
	
	return $request({
		url: `/idle/collection/getNoticeCollection/${currentPage}/${pageSize}`,
		method: 'GET',
		cache: false, // 禁用缓存，确保每次都是真实请求
		showLoading: false // 不显示loading，由页面控制
	})
}

/**
 * 添加商品到购物车
 * @param {Object} params 参数
 * @param {String} params.productId 商品ID
 * @param {Number} params.quantity 数量
 * @param {String} params.spec 规格
 */
export const addToCart = (params = {}) => {
	console.log('添加商品到购物车:', params)
	
	return $request({
		url: '/web/app/cart/addToCart',
		method: 'POST',
		data: {
			productId: params.productId,
			quantity: params.quantity || 1,
			spec: params.spec || ''
		}
	})
}

/**
 * 更新购物车商品数量
 * @param {Object} params 参数
 * @param {String} params.cartItemId 购物车商品ID
 * @param {Number} params.quantity 数量
 */
export const updateCartItem = (params = {}) => {
	console.log('更新购物车商品数量:', params)
	
	return $request({
		url: '/web/app/cart/updateCartItem',
		method: 'POST',
		data: {
			cartItemId: params.cartItemId,
			quantity: params.quantity
		}
	})
}

/**
 * 删除购物车商品
 * @param {String} cartItemId 购物车商品ID
 */
export const deleteCartItem = (cartItemId) => {
	console.log('删除购物车商品:', cartItemId)
	
	return $request({
		url: `/web/app/cart/deleteCartItem/${cartItemId}`,
		method: 'POST'
	})
}

/**
 * 批量删除购物车商品
 * @param {Array} cartItemIds 购物车商品ID数组
 */
export const batchDeleteCartItems = (cartItemIds = []) => {
	console.log('批量删除购物车商品:', cartItemIds)
	
	return $request({
		url: '/web/app/cart/batchDeleteCartItems',
		method: 'POST',
		data: {
			cartItemIds: cartItemIds
		}
	})
}

/**
 * 清空购物车
 */
export const clearCart = () => {
	console.log('清空购物车')
	
	return $request({
		url: '/web/app/cart/clearCart',
		method: 'POST'
	})
}

/**
 * 获取推荐商品
 * @param {Object} params 参数
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 */
export const getRecommendProducts = (params = {}) => {
	const page = params.page || 1
	const limit = params.limit || 10
	
	console.log('获取推荐商品, page:', page, 'limit:', limit)
	
	return $request({
		url: `/web/app/product/getRecommendProducts/${page}/${limit}`,
		method: 'GET'
	})
}




