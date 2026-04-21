import {
	$request
} from '../utils/request.js'

export const resetPassword = (params = {}) => {
	return $request({
		url: '/web/app/user/resetPasswordByPhone',
		method: 'POST',
		data: {
			phone: params.phoneNumber,
			password: params.password,
			checkPassword: params.password,
			code: params.smsCode
		},
		showLoading: false
	})
}

// export const getUserInfo = (params = {}) => {
// 	return $request({
// 		url: '/user/getUserInfo?userId=' + params.userId,
// 		method: 'GET'
// 	})
// }
export const getUserInfo = (params = {}) => {
	const userId = params.userId
	return $request({
		url: '/web/app/user/getUserById?userId=' + userId,
		method: 'GET',
		showLoading: false
	})
}

export const updateAvatarUrl = (params = {}) => {
	return $request({
		url: '/web/app/user/updateAvatarUrl',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

/**
 * 更新用户头像（直接传文件）
 * @param {Object} params - 参数对象
 * @param {String} params.filePath - 文件路径
 * @param {String} params.userId - 用户ID
 */
export const updateAvatar = (params = {}) => {
	const {
		baseUrl
	} = require('../.env.js')
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: baseUrl + '/web/app/user/updateAvatar',
			filePath: params.filePath,
			name: 'file',
			formData: {
				userId: params.userId
			},
			header: {
				'accessToken': uni.getStorageSync('uniapp_token') || ''
			},
			success: (res) => {
				try {
					const data = JSON.parse(res.data)
					if (data.code === 200) {
						resolve(data)
					} else {
						reject(data)
					}
				} catch (e) {
					reject({
						code: -1,
						msg: '解析响应失败',
						error: e
					})
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

export const updateBackgroundImage = (params = {}) => {
	return $request({
		url: '/web/app/user/updateBackgroundImage',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

/**
 * 更新用户背景图（直接传文件）
 * @param {Object} params - 参数对象
 * @param {String} params.filePath - 文件路径
 * @param {String} params.userId - 用户ID
 */
export const updateBackgroundImageFile = (params = {}) => {
	const {
		baseUrl
	} = require('../.env.js')
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: baseUrl + '/web/app/user/updateBackgroundImageFile',
			filePath: params.filePath,
			name: 'file',
			formData: {
				userId: params.userId
			},
			header: {
				'accessToken': uni.getStorageSync('uniapp_token') || ''
			},
			success: (res) => {
				try {
					const data = JSON.parse(res.data)
					if (data.code === 200) {
						resolve(data)
					} else {
						reject(data)
					}
				} catch (e) {
					reject({
						code: -1,
						msg: '解析响应失败',
						error: e
					})
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

export const updateNickname = (params = {}) => {
	return $request({
		url: '/web/app/user/updateUsername',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

export const updateIntroduction = (params = {}) => {
	return $request({
		url: '/web/app/user/updateDes',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

export const updateTags = (params = {}) => {
	return $request({
		url: '/web/app/user/updateTags',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

export const updateSex = (params = {}) => {
	return $request({
		url: '/web/app/user/updateSex',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

export const updateBirthday = (params = {}) => {
	return $request({
		url: '/web/app/user/updateBirthday',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

export const updateArea = (params = {}) => {
	return $request({
		url: '/web/app/user/updateArea',
		method: 'POST',
		data: params.userVO,
		showLoading: false
	})
}

// export const getAttentionList = (params = {}) => {
// 	return $request({
// 		url: '/user/relation/attentionList?userId=' + params.userId + '&pageNum=' + params.pageNum +
// 			'&pageSize=' + params.pageSize,
// 		method: 'GET'
// 	})
// }
export const getAttentionList = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const body = {
		uid: params.userId,
		type: params.type || 1
	}
	return $request({
		url: `/web/app/follower/getFriend/${page}/${limit}`,
		method: 'GET',
		data: body
	})
}

// export const getFansList = (params = {}) => {
// 	return $request({
// 		url: '/user/relation/fansList?userId=' + params.userId + '&pageNum=' + params.pageNum +
// 			'&pageSize=' + params.pageSize,
// 		method: 'GET'
// 	})
// }
export const getFansList = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const body = {
		uid: params.userId,
		type: params.type || 0
	}
	return $request({
		url: `/web/app/follower/getFriend/${page}/${limit}`,
		method: 'GET',
		data: body
	})
}

// export const updateAttention = (params = {}) => {
// 	return $request({
// 		url: '/user/relation/attention?userId=' + params.userId + '&targetUserId=' + params.targetUserId,
// 		method: 'POST'
// 	})
// }
export const updateAttention = (params = {}) => {
	const followerId = params.targetUserId
	return $request({
		url: '/web/app/follower/followById',
		method: 'GET',
		data: {
			followerId
		}
	})
}

export const isFollow = (params = {}) => {
	const followerId = params.followerId
	return $request({
		url: '/web/app/follower/isFollow',
		method: 'GET',
		data: {
			followerId
		}
	})
}

export const updateRemarkName = (params = {}) => {
	return $request({
		url: '/user/relation/updateRemarkName?userId=' + params.userId + '&targetUserId=' + params
			.targetUserId + '&remarkName=' + params.remarkName,
		method: 'POST'
	})
}

// export const getViewUserInfo = (params = {}) => {
// 	return $request({
// 		url: '/user/viewUserInfo?userId=' + params.userId,
// 		method: 'GET'
// 	})
// }
export const getViewUserInfo = (params = {}) => {
	const userId = params.userId
	return $request({
		url: '/web/app/user/getUserById',
		method: 'GET',
		data: {
			userId
		}
	})
}

export const getUserIsBindThird = (params = {}) => {
	return $request({
		url: '/web/app/user/getUserIsBindThird',
		method: 'GET',
		showLoading: false
	})
}

export const cancelAccount = () => {
	return $request({
		url: '/web/app/user/cancelAccount',
		method: 'POST',
		showLoading: false
	})
}

export const updatePhoneNumber = (params = {}) => {
	return $request({
		url: '/web/app/user/updatePhoneNumber?phoneNumber=' + params.phoneNumber + '&smsCode=' + params.smsCode,
		method: 'POST',
		showLoading: false
	})
}

export const resetPasswordByOld = (params = {}) => {
	return $request({
		url: '/web/app/user/resetPasswordByOld',
		method: 'POST',
		data: params.passwordVO,
		showLoading: false
	})
}

// 获取关注用户列表（去重）
export const getFollowUser = (params = {}) => {
	// 🔧 修复：添加时间戳参数，防止HTTP缓存
	const url = params._t 
		? `/web/app/follower/getFollowUser?_t=${params._t}`
		: `/web/app/follower/getFollowUser`
	return $request({
		url: url,
		method: 'GET',
		data: params,
		cache: params._t ? false : undefined // 🔧 如果有时间戳，禁用缓存
	})
}