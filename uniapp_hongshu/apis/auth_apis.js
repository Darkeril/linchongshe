import {
	$request
} from '../utils/request.js'

export const checkToken = () => {
	return $request({
		url: '/web/app/auth/checkToken',
		method: 'GET'
	})
}

// 刷新token
export const refreshToken = (refreshToken) => {
	return $request({
		url: `/web/app/auth/refreshToken?refreshToken=${encodeURIComponent(refreshToken)}`,
		method: 'GET',
		showLoading: false,
		showErrorToast: false, // 刷新失败时不显示错误提示，由调用方处理
		skipTokenRefresh: true // 标记此请求不需要自动刷新token，避免循环
	})
}

// export const generalLogin=(params={})=>{
// 	return $request({
// 		url: '/auth/login?phoneNumber=' + params.phoneNumber+'&password='+params.password,
// 		method: 'POST'
// 	})
// }
export const generalLogin = (params = {}) => {
	return $request({
		url: '/web/app/auth/login',
		method: 'POST',
		data: {
			phone: params.phone,
			password: params.password,
			captchaVerification: params.captchaVerification
		},
		showLoading: false, // 密码登录不显示 loading
		skipTokenRefresh: true // 登录接口本身不需要自动刷新 token
	})
}

export const loginByCode = (params = {}) => {
	return $request({
		url: '/web/app/auth/loginByCode',
		method: 'POST',
		data: {
			phone: params.phone,
			code: params.code,
			captchaVerification: params.captchaVerification
		},
		showLoading: false, // 验证码登录不显示 loading
		skipTokenRefresh: true // 登录接口本身不需要自动刷新 token
	})
}

export const sendSmsCode = (params = {}) => {
	return $request({
		url: `/web/app/auth/sendCode?phone=${encodeURIComponent(params.phone)}&captchaVerification=${encodeURIComponent(params.captchaVerification)}`,
		method: 'POST'
	})
}

export const bindNumberPhone = (params = {}) => {
	return $request({
		url: '/auth/bindPhone',
		method: 'POST',
		data: params.registerVo
	})
}
export const register = (params = {}) => {
	return $request({
		url: '/auth/register',
		method: 'POST',
		data: params.registerVo
	})
}

export const getTrtcUserSig = (params = {}) => {
	return $request({
		url: '/auth/getTrtcUserSig?userId=' + params.userId,
		method: 'POST'
	})
}

// export const logout=(params={})=>{
// 	return $request({
// 		url:'/auth/logout?userId='+params.userId,
// 		method:'POST'
// 	})
// }
export const logout = (params = {}) => {
	return $request({
		url: '/web/app/auth/loginOut?userId=' + params.userId,
		method: 'GET'
	})
}

// 获取演示账号配置
export const getDemoAccountConfig = () => {
	return $request({
		url: '/web/system/config/demoAccountConfig',
		method: 'GET'
	})
}

// 演示账号登录
export const demoLogin = (params = {}) => {
	return $request({
		url: '/web/app/auth/demoLogin',
		method: 'POST',
		data: {
			phone: params.phone,
			password: params.password
		},
		showLoading: false, // 演示账号登录不显示 loading
		skipTokenRefresh: true // 登录接口本身不需要自动刷新 token
	})
}

// 微信小程序登录
export const wechatMiniProgramLogin = (params = {}) => {
	return $request({
		url: '/web/app/auth/wechatLogin',
		method: 'POST',
		data: {
			code: params.code,
			userInfo: params.userInfo, // 用户信息（昵称、头像等）
			encryptedData: params.encryptedData, // 加密数据（如果需要）
			iv: params.iv, // 加密算法的初始向量（如果需要）
			phoneCode: params.phoneCode // 手机号授权code（通过getPhoneNumber获取）
		},
		showLoading: true,
		skipTokenRefresh: true // 小程序首次登录同样不需要自动刷新 token
	})
}

// 扫描二维码
export const scanQrCode = (loginToken) => {
	return $request({
		url: `/web/auth/qrcode/scan?loginToken=${encodeURIComponent(loginToken)}`,
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
			// 不包含 accessToken，因为这个接口不需要认证
		}
	})
}

// 确认二维码登录
export const confirmQrCodeLogin = (data) => {
	return $request({
		url: '/web/auth/qrcode/confirm',
		method: 'POST',
		data: data
	})
}