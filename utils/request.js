import {
	baseUrl
} from '../.env.js'
import ws from './websocket.js'
import errorHandler from './errorHandler.js'
import cacheManager from './cacheManager.js'

let isRedirectingToLogin = false
let isRefreshingToken = false // token 刷新锁
let refreshTokenPromise = null // 刷新 token 的 Promise，用于让其他请求等待

function redirectToLoginOnce() {
    if (isRedirectingToLogin) return
    isRedirectingToLogin = true
    try {
        uni.removeStorageSync('uniapp_token')
        uni.removeStorageSync('uniapp_refresh_token')
        uni.removeStorageSync('userInfo')
    } catch (e) {}
    // 按项目实际登录页路径修改
    uni.reLaunch({ url: '/pkg-auth/pages/login/loginMethod' })
    setTimeout(() => { isRedirectingToLogin = false }, 1000)
}

/**
 * 刷新 token
 * @returns {Promise} 返回刷新后的新 token
 */
async function tryRefreshToken() {
	// 如果正在刷新，返回现有的刷新 Promise
	if (isRefreshingToken && refreshTokenPromise) {
		console.log('[Token Refresh] 等待现有刷新完成...')
		return refreshTokenPromise
	}
	
	// 获取 refreshToken
	const refreshToken = uni.getStorageSync('uniapp_refresh_token')
	if (!refreshToken || refreshToken === '' || refreshToken === 'undefined' || refreshToken === 'null') {
		console.warn('[Token Refresh] 没有 refreshToken，无法刷新')
		return Promise.reject(new Error('No refresh token'))
	}
	
	// 设置刷新锁
	isRefreshingToken = true
	console.log('[Token Refresh] 开始刷新 token...')
	
	// 创建刷新 Promise
	refreshTokenPromise = new Promise((resolve, reject) => {
		uni.request({
			url: baseUrl + `/web/app/auth/refreshToken?refreshToken=${encodeURIComponent(refreshToken)}`,
			method: 'GET',
			header: {
				'Content-Type': 'application/json'
			},
			success: (res) => {
				if (res.data && res.data.code === 200 && res.data.data) {
					const { accessToken, refreshToken: newRefreshToken } = res.data.data
					
					// 保存新的 token
					if (accessToken) {
						uni.setStorageSync('uniapp_token', accessToken)
						console.log('[Token Refresh] AccessToken 刷新成功')
					}
					
					// 如果返回了新的 refreshToken，也保存
					if (newRefreshToken) {
						uni.setStorageSync('uniapp_refresh_token', newRefreshToken)
						console.log('[Token Refresh] RefreshToken 已更新')
					}
					
					resolve(accessToken)
				} else {
					console.error('[Token Refresh] 刷新失败:', res.data)
					reject(new Error('Token refresh failed'))
				}
			},
			fail: (err) => {
				console.error('[Token Refresh] 请求失败:', err)
				reject(err)
			},
			complete: () => {
				// 释放刷新锁
				isRefreshingToken = false
				refreshTokenPromise = null
			}
		})
	})
	
	return refreshTokenPromise
}

export const $request = (params = {}) => {
	return new Promise(async function(resolve, reject) {
		// 检查是否需要缓存
		const shouldCache = params.cache !== false && (params.method === 'GET' || !params.method)
		const cacheKey = shouldCache ? cacheManager.generateCacheKey(params.url, params.data, params.method) : null
		
		// 如果启用缓存，先检查缓存
		if (shouldCache && cacheKey) {
			const cachedData = cacheManager.get(cacheKey)
			if (cachedData) {
				console.log(`[Cache Hit] ${params.method || 'GET'} ${params.url}`)
				resolve(cachedData)
				return
			}
		}
		
		// 内部请求函数
		const makeRequest = (retryCount = 0) => {
			// 显示加载状态（添加标志追踪）
			let isShowLoading = false
			if (params.showLoading !== false && retryCount === 0) {
				isShowLoading = true
				uni.showLoading({
					title: params.loadingText || '加载中',
					mask: true
				})
			}
			
			let header = {
				'accessToken': uni.getStorageSync('uniapp_token'),
				'Content-Type': 'application/json'
			}
			
			// 合并自定义请求头
			if (params.header) {
				header = { ...header, ...params.header }
			}
			
			const timeout = params.timeout || 10000
			const timer = setTimeout(() => {
				if (isShowLoading) {
					try {
						uni.hideLoading()
					} catch (e) {
						console.warn('hideLoading failed:', e)
					}
				}
				const timeoutError = new Error('请求超时')
				errorHandler.handleNetworkError(timeoutError, `请求 ${params.url}`)
				reject(timeoutError)
			}, timeout)
			
			uni.request({
				url: baseUrl + params.url,
				method: params.method || 'GET',
				header: header,
				data: params.data,
				success: async (res) => {
					clearTimeout(timer)
					
					// 记录请求成功日志
					console.log(`[API Success] ${params.method || 'GET'} ${params.url}`, {
						statusCode: res.statusCode,
						response: res.data
					})
					
					// 处理 token 过期（业务码 501 或 401）或未授权
					const isTokenExpired = (res.data && (res.data.code === 501 || res.data.code === 401)) || 
					                       (res.statusCode === 401 || res.statusCode === 403)
					
				if (isTokenExpired && !params.skipTokenRefresh && retryCount === 0) {
						console.log('[Token Expired] 检测到 token 过期，尝试刷新...')
						
						try {
							// 尝试刷新 token
							await tryRefreshToken()
							console.log('[Token Refresh] 刷新成功，重试原请求...')
							
							// 隐藏加载状态
							if (isShowLoading) {
								try {
									uni.hideLoading()
								} catch (e) {}
							}
							
							// 重试原请求
							makeRequest(1)
							return
						} catch (refreshError) {
							// 没有 refreshToken 的场景属于「未登录」/「已退出」，降低日志级别，避免在登录页报红
							if (refreshError && refreshError.message === 'No refresh token') {
								console.warn('[Token Refresh] 无 refreshToken，直接跳转登录页')
							} else {
								console.error('[Token Refresh] 刷新失败，跳转登录页', refreshError)
							}
							// 刷新失败，跳转登录页
							redirectToLoginOnce()
							reject(res.data || { code: res.statusCode, msg: '登录已过期' })
							return
						}
					}
					
					// 如果仍然是 token 错误（刷新后重试仍失败），跳转登录页
					if (isTokenExpired) {
						console.error('[Token Error] Token 仍然无效，跳转登录页')
						redirectToLoginOnce()
						reject(res.data || { code: res.statusCode, msg: '登录已过期' })
						return
					}

					// 处理业务错误
					if (res.data.code && res.data.code !== 200) {
						console.error(`[API Error] ${params.method || 'GET'} ${params.url}`, {
							code: res.data.code,
							message: res.data.msg || res.data.message,
							response: res.data
						})
						errorHandler.handle(res.data, `请求 ${params.url}`, {
							showToast: params.showErrorToast !== false
						})
						reject(res.data)
						return
					}
					
					// 缓存成功的响应数据
					if (shouldCache && cacheKey && res.data.code === 200) {
						const cacheTTL = params.cacheTTL || (5 * 60 * 1000) // 默认5分钟
						cacheManager.set(cacheKey, res.data, cacheTTL)
						console.log(`[Cache Set] ${params.method || 'GET'} ${params.url}`)
					}
					
					resolve(res.data)
				},
				fail(err) {
					clearTimeout(timer)
					ws.completeClose()
					
					// 记录网络错误日志
					console.error(`[Network Error] ${params.method || 'GET'} ${params.url}`, {
						error: err,
						url: baseUrl + params.url,
						header: header
					})
					
					errorHandler.handleNetworkError(err, `请求 ${params.url}`)
					reject(err)
				},
				complete() {
					if (isShowLoading) {
						try {
							uni.hideLoading()
						} catch (e) {
							console.warn('hideLoading failed in complete:', e)
						}
					}
				}
			})
		}
		
		// 开始请求
		makeRequest(0)
	})
}