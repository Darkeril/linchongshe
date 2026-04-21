import { $request } from '../utils/request.js'

/**
 * 笔记行为上报（曝光/观看/观看时长完播）- 单条
 * @param {Object} data
 * @param {string} data.eventType - exposure | view | watch | share
 * @param {string} data.nid - 笔记ID
 * @param {string} [data.uid]
 * @param {string} [data.scene] - search | recommend | follow | profile | other
 * @param {number} [data.durationSec]
 * @param {0|1} [data.completed]
 */
export const reportNoteBehavior = (data = {}) => {
	return $request({
		url: '/web/app/noteBehavior/report',
		method: 'POST',
		data,
		showLoading: false,
		showErrorToast: false
	}).catch(() => ({}))
}

/**
 * 笔记行为批量上报
 * @param {Array<{eventType, nid, uid?, scene?, durationSec?, completed?}>} list
 */
export const reportNoteBehaviorBatch = (list = []) => {
	if (!list || list.length === 0) return Promise.resolve()
	return $request({
		url: '/web/app/noteBehavior/reportBatch',
		method: 'POST',
		data: list,
		showLoading: false,
		showErrorToast: false
	}).catch(() => ({}))
}
