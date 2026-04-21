import { $request } from '../utils/request.js'

/**
 * 获取专辑列表
 * @param {Object} params - 参数对象
 * @param {String} params.userId - 用户ID
 */
export const getAlbumList = (params = {}) => {
	const { userId } = params
	return $request({
		url: `/web/album/getAlbumList?userId=${userId}`,
		method: 'GET'
	})
}

/**
 * 创建专辑
 * @param {Object} data - 专辑数据
 * @param {String} data.title - 专辑标题
 * @param {String} data.uid - 用户ID
 * @param {Number} data.sort - 排序，默认0
 */
export const createAlbum = (data) => {
	return $request({
		url: '/web/album/saveAlbum',
		method: 'POST',
		data
	})
}

