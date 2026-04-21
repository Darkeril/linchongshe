import { $request } from '@/utils/request'

/**
 * 获取用户订单列表（通过订单专用接口）
 * @param {Object} params 参数
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 * @param {String} params.status 订单状态（与后端 getOrdersByUser 的 orderStatus 一致）
 *   - unpaid: 待付款（含 0 未支付 / 1 待支付 / 2 支付中）
 *   - pending: 待发货（已支付且未进入待收货）
 *   - shipped: 待收货（已支付且 deal_status=4）
 *   - received: 已签收（order_status=4）
 *   - cancelled: 已取消
 *   - paying / paid: 若后端支持可传；常用为 unpaid、pending、shipped、received
 * @param {String} params.userId 用户ID（可选，默认从本地存储获取）
 */
export const getOrdersByUser = (params = {}) => {
	const page = params.page || 1
	const limit = params.limit || 10
	const orderStatus = params.status // 订单状态
	const userId = params.userId || uni.getStorageSync('userInfo')?.id
	
	console.log('调用订单API, page:', page, 'limit:', limit, 'orderStatus:', orderStatus, 'userId:', userId)
	
	// 构建查询参数（兼容微信小程序）
	const queryParams = []
	if (userId) queryParams.push(`userId=${encodeURIComponent(userId)}`)
	if (orderStatus) queryParams.push(`orderStatus=${encodeURIComponent(orderStatus)}`)
	
	const queryString = queryParams.length > 0 ? queryParams.join('&') : ''
	const url = `/idle/product/order/getOrdersByUser/${page}/${limit}${queryString ? '?' + queryString : ''}`
	
	console.log('getOrdersByUser 请求URL:', url)
	
	// 添加时间戳参数，确保每次请求都是新的（避免缓存）
	const timestamp = Date.now()
	const finalUrl = url + (url.includes('?') ? '&' : '?') + '_t=' + timestamp
	
	return $request({
		url: finalUrl,
		method: 'GET',
		cache: false, // 禁用缓存，确保每次都是真实请求
		showLoading: false // 不显示loading，由页面控制
	})
}

/**
 * 取消订单
 * @param {String} orderId 订单ID
 */
export const cancelOrderById = (orderId) => {
	console.log('取消订单, orderId:', orderId)
	
	return $request({
		url: `/idle/product/order/cancelOrder/${orderId}`,
		method: 'POST'
	})
}

/**
 * 支付订单（已废弃，请使用 createPaymentOrder）
 * @deprecated 请使用 createPaymentOrder 创建支付订单，然后跳转到支付页面
 * @param {String} orderId 订单ID
 */
export const payOrderById = (orderId) => {
	console.warn('payOrderById 已废弃，请使用 createPaymentOrder')
	return createPaymentOrder(orderId, '1')
}

/**
 * 确认收货
 * @param {String} orderId 订单ID
 */
export const confirmOrderById = (orderId) => {
	console.log('确认收货, orderId:', orderId)
	
	return $request({
		url: `/web/app/order/confirmOrder/${orderId}`,
		method: 'POST'
	})
}

/**
 * 获取订单详情
 * @param {String} orderId 订单ID
 */
export const getOrderDetail = (orderId) => {
	console.log('获取订单详情, orderId:', orderId)
	
	return $request({
		url: `/idle/product/order/getOrderDetailById/${orderId}`,
		method: 'GET'
	})
}

/**
 * 创建商品订单
 * @param {Object} data 订单数据
 * @param {String} data.productId 商品ID
 * @param {String} data.username 收货人姓名
 * @param {String} data.phone 收货人电话
 * @param {String} data.address 收货地址
 */
export const createProductOrder = (data) => {
	console.log('创建商品订单:', data)
	
	return $request({
		url: '/idle/product/order/createOrder',
		method: 'POST',
		data: data
	})
}

/**
 * 创建支付订单（UniApp专用接口）
 * @param {String} productOrderId 商品订单ID
 * @param {String} payType 支付类型（1-微信支付，2-支付宝支付，默认2）
 */
export const createPaymentOrder = (productOrderId, payType = '2') => {
	console.log('创建支付订单:', { productOrderId, payType })
	
	return $request({
		url: '/idle/app/payment/order/createPayment',
		method: 'GET',
		data: {
			productOrderId,
			payType
		}
	})
}

/**
 * 获取支付订单信息（UniApp专用接口）
 * @param {String} paymentOrderId 支付订单ID
 */
export const getPaymentOrder = (paymentOrderId) => {
	console.log('获取支付订单信息:', paymentOrderId)
	
	return $request({
		url: '/idle/app/payment/order/getPaymentOrder',
		method: 'GET',
		data: {
			paymentOrderId
		}
	})
}

/**
 * 完成支付（生成支付二维码或表单）（UniApp专用接口）
 * @param {String} paymentPayId 支付记录ID
 * @param {String} paymentMethod 支付方式（可选）："qrCode"=扫码支付，"miniprogram"=小程序支付，"form"或不传=表单跳转（默认）
 * @param {String} openid 小程序用户openid（小程序支付时必传）
 */
export const finishPay = (paymentPayId, paymentMethod, openid) => {
	console.log('完成支付:', paymentPayId, '支付方式:', paymentMethod, 'openid:', openid)
	
	let url = `/idle/app/payment/pay/finish?paymentPayId=${encodeURIComponent(paymentPayId)}`
	if (paymentMethod) {
		url += `&paymentMethod=${encodeURIComponent(paymentMethod)}`
	}
	if (openid) {
		url += `&openid=${encodeURIComponent(openid)}`
	}
	
	return $request({
		url: url,
		method: 'POST'
	})
}

/**
 * 查询支付状态（UniApp专用接口）
 * @param {String} paymentPayId 支付记录ID
 */
export const getPayStatus = (paymentPayId) => {
	console.log('查询支付状态:', paymentPayId)
	
	return $request({
		url: '/idle/app/payment/pay/status',
		method: 'GET',
		data: {
			paymentPayId
		}
	})
}

/**
 * 创建支付记录（UniApp专用接口）
 * @param {String} paymentOrderId 支付订单ID
 */
export const createPaymentPay = (paymentOrderId) => {
	console.log('创建支付记录:', paymentOrderId)
	
	return $request({
		url: '/idle/app/payment/pay/createPaymentPay',
		method: 'GET',
		data: {
			paymentOrderId
		}
	})
}

/**
 * 更新订单状态（释放过期订单，UniApp专用接口）
 * @param {String} paymentOrderId 支付订单ID
 */
export const updateOrderStatus = (paymentOrderId) => {
	console.log('更新订单状态（释放过期订单）:', paymentOrderId)
	
	return $request({
		url: `/idle/app/payment/pay/updateOrderStatus?paymentOrderId=${encodeURIComponent(paymentOrderId)}`,
		method: 'POST'
	})
}

/**
 * 获取支付全局配置（支付开关和支付方式）
 * 注意：此接口禁用缓存，确保每次都获取最新配置
 */
export const getPaymentGlobalConfig = () => {
	console.log('获取支付全局配置')
	
	return $request({
		url: '/idle/config/paymentGlobalConfig',
		method: 'GET',
		cache: false // 禁用缓存，确保每次都获取最新的支付配置
	})
}
