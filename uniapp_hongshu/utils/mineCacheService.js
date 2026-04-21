/**
 * Mine页面缓存管理服务
 */

/**
 * 设置标签页缓存
 * @param {Object} cacheStore 缓存存储对象
 * @param {Number} tabIndex 标签页索引
 * @param {Object} data 缓存数据
 * @param {Number} cacheExpireTime 缓存过期时间（毫秒）
 */
export function setTabCache(cacheStore, tabIndex, data, cacheExpireTime = 10 * 60 * 1000) {
	const cacheKey = `tab_${tabIndex}`;
	cacheStore[cacheKey] = {
		...data,
		timestamp: Date.now()
	};
	console.log(`✅ Mine缓存已保存: tab_${tabIndex}`, data);
}

/**
 * 获取标签页缓存
 * @param {Object} cacheStore 缓存存储对象
 * @param {Number} tabIndex 标签页索引
 * @param {Number} cacheExpireTime 缓存过期时间（毫秒）
 * @returns {Object|null} 缓存数据或null
 */
export function getTabCache(cacheStore, tabIndex, cacheExpireTime = 10 * 60 * 1000) {
	const cacheKey = `tab_${tabIndex}`;
	const cached = cacheStore[cacheKey];

	if (!cached) return null;

	// 检查缓存是否过期
	const isExpired = Date.now() - cached.timestamp > cacheExpireTime;
	if (isExpired) {
		delete cacheStore[cacheKey];
		console.log(`⚠️ Mine缓存已过期: tab_${tabIndex}`);
		return null;
	}

	console.log(`✅ Mine缓存命中: tab_${tabIndex}`);
	return cached;
}

/**
 * 清除标签页缓存
 * @param {Object} cacheStore 缓存存储对象
 * @param {Number} tabIndex 标签页索引
 */
export function clearTabCache(cacheStore, tabIndex) {
	const cacheKey = `tab_${tabIndex}`;
	if (cacheStore[cacheKey]) {
		delete cacheStore[cacheKey];
		console.log(`🗑️ Mine缓存已清除: tab_${tabIndex}`);
	}
}

/**
 * 清除所有缓存
 * @param {Object} cacheStore 缓存存储对象
 */
export function clearAllCache(cacheStore) {
	Object.keys(cacheStore).forEach(key => {
		delete cacheStore[key];
	});
	console.log('🗑️ Mine所有缓存已清除');
}






















