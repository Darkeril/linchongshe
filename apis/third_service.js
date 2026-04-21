import {
	$request
} from '../utils/request.js'

export const sendBindPhoneSms = (params = {}) => {
	return $request({
		url: '/web/app/third/sendBindPhoneSms?phoneNumber=' + params.phoneNumber,
		method: 'GET'
	})
}

export const sendResetPhoneSms = (params = {}) => {
	return $request({
		url: '/web/app/third/sendResetPhoneSms?phoneNumber=' + params.phoneNumber,
		method: 'GET'
	})
}
export const sendRegisterPhoneSms = (params = {}) => {
	return $request({
		url: '/web/app/third/sendRegisterPhoneSms?phoneNumber=' + params.phoneNumber,
		method: 'GET'
	})
}

export const checkBindSmsCode = (params = {}) => {
	return $request({
		url: '/web/app/third/checkBindSmsCode?phoneNumber=' + params.phoneNumber + '&smsCode=' + params.smsCode,
		method: 'POST',
	})
}

export const checkResetSmsCode = (params = {}) => {
	return $request({
		url: '/web/app/third/checkResetSmsCode?phoneNumber=' + params.phoneNumber + '&smsCode=' + params.smsCode,
		method: 'POST'
	})
}

// 获取微信JS-SDK签名配置
export const getWeChatJSSDKConfig = (params = {}) => {
	return $request({
		url: '/web/app/third/getWeChatJSSDKConfig?url=' + encodeURIComponent(params.url || ''),
		method: 'GET'
	})
}