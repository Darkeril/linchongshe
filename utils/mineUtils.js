/**
 * Mine页面工具方法
 */

/**
 * 格式化省份显示（去掉省、市、自治区等后缀）
 * @param {String} province 省份名称
 * @returns {String} 格式化后的省份名称
 */
export function formatProvince(province) {
	if (!province) return '未知';
	// 去掉后缀：省、市、自治区、特别行政区
	return province
		.replace(/省$/, '')
		.replace(/市$/, '')
		.replace(/自治区$/, '')
		.replace(/特别行政区$/, '');
}

/**
 * 格式化地址显示（只显示市+详细地址）
 * @param {Object} item 包含地址信息的对象
 * @returns {String} 格式化后的地址
 */
export function formatAddress(item) {
	if (!item) return '';
	const parts = [];
	if (item.city) parts.push(item.city);
	if (item.address) parts.push(item.address);
	// 兼容旧数据：如果没有新字段但有location字段
	if (parts.length === 0 && item.location) {
		const location = item.location;
		return location.length <= 12 ? location : location.substring(0, 12) + '...';
	}
	const fullAddress = parts.join(' ');
	return fullAddress.length > 12 ? fullAddress.substring(0, 12) + '...' : fullAddress;
}

/**
 * 清除瀑布流组件
 * @param {Object} ref 瀑布流组件引用
 */
export function clearWaterfall(ref) {
	if (ref && ref.clear) {
		ref.clear();
	}
}

/**
 * 只添加数据到瀑布流（不清空）
 * @param {Object} ref 瀑布流组件引用
 */
export function addOnlyToWaterfall(ref) {
	if (ref && ref.addList) {
		// 这里需要调用组件的方法来添加数据
		// 具体实现依赖于瀑布流组件的API
	}
}






















