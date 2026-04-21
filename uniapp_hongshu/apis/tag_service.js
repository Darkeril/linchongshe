import { $request } from '../utils/request.js'

export const getHotTagList = (params = {}) => {
	const page = params.page || 1
	const pageSize = params.pageSize || 20
	return $request({
		url: `/web/app/tag/getHotTagList/${page}/${pageSize}`,
		method: 'GET'
	})
}


