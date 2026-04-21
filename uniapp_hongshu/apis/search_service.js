import { $request } from '../utils/request.js'

// 推荐用户（发现好友）
export function getRecommendUser(currentPage = 1, pageSize = 10) {
  return $request({
    url: `/web/app/es/note/getRecommendUser/${currentPage}/${pageSize}`,
    method: 'GET'
  })
}

export default { getRecommendUser }

// export const searchNotesNearby=(params={})=>{
// 	return $request({
// 		url:'/search/notes/getNotesNearBy',
// 		method:'POST',
// 		data:params.pageParam
// 	})
// }
export const searchNotesNearby = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/es/note/getNotesNearBy/${page}/${limit}`,
		method: 'GET',
		showLoading: false, // 附近页列表不显示 loading
		cache: false // 禁用缓存，确保下拉刷新时获取最新数据
	})
}

// export const searchNotesByKeyword = (params = {}) => {
// 	return $request({
// 		url: '/search/notes/getNotesByKeyword?page=' + params.page + '&pageSize=' + params.pageSize +
// 			'&keyword=' + params.keyword + '&notesType=' + params.notesType + '&hot=' + params.hot,
// 		method: 'GET'
// 	})
// }
export const searchNotesByKeyword = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const body = {
		cid: params.cid || '',
		cpid: params.cpid || '',
		keyword: params.keyword || '',
		type: params.notesType || 0
	}
	return $request({
		url: `/web/app/es/note/getNoteByDTO/${page}/${limit}`,
		method: 'POST',
		data: body
	})
}

// export const searchUserByKeyword = (params = {}) => {
// 	return $request({
// 		url: '/search/user/getUser?page=' + params.page + '&pageSize=' + params.pageSize + '&keyword=' +
// 			params.keyword,
// 		method: 'GET'
// 	})
// }
export const searchUserByKeyword = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	const body = {
		keyword: params.keyword || '',
		type: params.type || 1
	}
	return $request({
		url: `/web/app/user/getUserByKeyword/${page}/${limit}`,
		method: 'GET',
		data: body
	})
}

// 获取搜索历史记录
export const getSearchHistory = (params = {}) => {
	return $request({
		url: '/web/app/es/record/getRecordByKeyWord',
		method: 'GET',
		data: {
			keyword: params.keyword || '',
			uid: params.uid || uni.getStorageSync('userInfo')?.id || ''
		}
	})
}

// 获取热门搜索记录
export const getHotRecord = () => {
	return $request({
		url: '/web/app/es/record/getHotRecord',
		method: 'GET'
	})
}