import { $request } from '@/utils/request'

/**
 * 获取本地草稿列表
 */
export const getLocalDrafts = () => {
	console.log('获取本地草稿列表')
	
	return $request({
		url: '/web/app/draft/getLocalDrafts',
		method: 'GET'
	})
}

/**
 * 保存草稿
 * @param {Object} params 参数
 * @param {String} params.title 标题
 * @param {String} params.content 内容
 * @param {String} params.type 类型 (notes/idle/video/image)
 * @param {String} params.coverImage 封面图片
 * @param {Array} params.images 图片列表
 * @param {String} params.videoUrl 视频地址
 */
export const saveDraft = (params = {}) => {
	console.log('保存草稿:', params)
	
	return $request({
		url: '/web/app/draft/saveDraft',
		method: 'POST',
		data: {
			title: params.title || '',
			content: params.content || '',
			type: params.type || 'notes',
			coverImage: params.coverImage || '',
			images: params.images || [],
			videoUrl: params.videoUrl || ''
		}
	})
}

/**
 * 更新草稿
 * @param {Object} params 参数
 * @param {String} params.draftId 草稿ID
 * @param {String} params.title 标题
 * @param {String} params.content 内容
 * @param {String} params.type 类型
 * @param {String} params.coverImage 封面图片
 * @param {Array} params.images 图片列表
 * @param {String} params.videoUrl 视频地址
 */
export const updateDraft = (params = {}) => {
	console.log('更新草稿:', params)
	
	return $request({
		url: '/web/app/draft/updateDraft',
		method: 'POST',
		data: {
			draftId: params.draftId,
			title: params.title || '',
			content: params.content || '',
			type: params.type || 'notes',
			coverImage: params.coverImage || '',
			images: params.images || [],
			videoUrl: params.videoUrl || ''
		}
	})
}

/**
 * 删除草稿
 * @param {String} draftId 草稿ID
 */
export const deleteDraft = (draftId) => {
	console.log('删除草稿:', draftId)
	
	return $request({
		url: `/web/app/draft/deleteDraft/${draftId}`,
		method: 'POST'
	})
}

/**
 * 批量删除草稿
 * @param {Array} draftIds 草稿ID数组
 */
export const batchDeleteDrafts = (draftIds = []) => {
	console.log('批量删除草稿:', draftIds)
	
	return $request({
		url: '/web/app/draft/batchDeleteDrafts',
		method: 'POST',
		data: {
			draftIds: draftIds
		}
	})
}

/**
 * 获取草稿详情
 * @param {String} draftId 草稿ID
 */
export const getDraftDetail = (draftId) => {
	console.log('获取草稿详情:', draftId)
	
	return $request({
		url: `/web/app/draft/getDraftDetail/${draftId}`,
		method: 'GET'
	})
}

/**
 * 发布草稿
 * @param {String} draftId 草稿ID
 */
export const publishDraft = (draftId) => {
	console.log('发布草稿:', draftId)
	
	return $request({
		url: `/web/app/draft/publishDraft/${draftId}`,
		method: 'POST'
	})
}








