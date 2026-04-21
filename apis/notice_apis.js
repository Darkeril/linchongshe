import {
	$request
} from '../utils/request.js'

/**
 * 获取通知公告列表
 * @param {Object} params - 参数对象
 * @param {Number} params.page - 页码，默认1
 * @param {Number} params.pageSize - 每页数量，默认10
 */
export const getNoticeList = (params = {}) => {
	const page = params.page || 1
	const pageSize = params.pageSize || 10
	return $request({
		url: `/web/app/notice/list/${page}/${pageSize}`,
		method: 'GET',
		showLoading: false // 通知公告列表不显示 loading
	})
}

/**
 * 根据ID获取通知公告详情
 * @param {Number} noticeId - 公告ID
 */
export const getNoticeDetail = (noticeId) => {
	return $request({
		url: `/web/app/notice/${noticeId}`,
		method: 'GET',
		showLoading: false
	})
}





