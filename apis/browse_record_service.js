import { $request } from '@/utils/request'

/**
 * 获取用户浏览记录
 * @param {Object} params 参数
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 */
export const getAllBrowseRecordByUser = (params = {}) => {
	const page = params.page || 1
	const limit = params.limit || 10
	
	console.log('调用浏览记录API, page:', page, 'limit:', limit)
	
	return $request({
		url: `/web/app/browseRecord/getAllBrowseRecordByUser/${page}/${limit}`,
		method: 'GET',
		cache: false, // 禁用缓存，确保每次都是真实请求
		showLoading: false // 不显示loading，由页面控制
	})
}

/**
 * 添加浏览记录
 * @param {Object} params 参数
 * @param {String} params.nid 笔记ID
 * @param {String} params.uid 用户ID
 */
export const addBrowseRecord = (params = {}) => {
	const uid = params.uid || uni.getStorageSync('userInfo')?.id
	
	console.log('添加浏览记录, nid:', params.nid, 'uid:', uid)
	
	return $request({
		url: '/web/app/browseRecord/addBrowseRecord',
		method: 'POST',
		data: {
			nid: params.nid,
			uid: uid
		}
	})
}
