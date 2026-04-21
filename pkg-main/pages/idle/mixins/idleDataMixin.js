/**
 * 市集页面 - 数据管理 Mixin
 * 负责分类数据的管理、缓存等
 */
export default {
	data() {
		return {
			// 为每个分类创建独立的数据源
			categoryData: {},
			// 性能优化相关
			categoryCache: {},
			typeSwitchTimer: null,
			// 🔧 修复：添加追加锁，防止微信小程序端并发追加导致重复
			appendLocks: {}, // 格式：{ category_0: false, category_1: false, ... }
		}
	},
	methods: {
		/**
		 * 获取分类数据（带默认值）
		 * @param {Number} categoryIndex - 分类索引
		 * @returns {Object} 分类数据对象
		 */
		getCategoryData(categoryIndex) {
			const categoryKey = `category_${categoryIndex}`;

			// 如果分类数据不存在，初始化它（确保响应式）
			if (!this.categoryData[categoryKey]) {
				this.$set(this.categoryData, categoryKey, {
					list: [], // 统一使用 list，组件内部处理瀑布流
					leftList: [], // 兼容旧数据
					rightList: [], // 兼容旧数据
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					total: 0,
					pageSize: categoryKey === 'category_0' ? 20 : 6, // 推荐分类使用20条，其他分类使用6条
					initialLoadSize: 3,
					requestId: null, // 用于防止过期请求覆盖新数据
				});
			}

			return this.categoryData[categoryKey];
		},

		/**
		 * 获取分类列表（优化：单独的方法获取 list，避免在模板中复杂判断）
		 * @param {Number} categoryIndex - 分类索引
		 * @returns {Array} 分类商品列表
		 */
		getCategoryList(categoryIndex) {
			const categoryKey = `category_${categoryIndex}`;
			
			// 如果 categoryData 不存在，初始化它（确保响应式）
			if (!this.categoryData[categoryKey]) {
				this.$set(this.categoryData, categoryKey, {
					list: [],
					leftList: [],
					rightList: [],
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					total: 0,
					pageSize: categoryKey === 'category_0' ? 20 : 6
				});
			}
			
			const data = this.categoryData[categoryKey];

			// 优先使用 list
			if (data.list && Array.isArray(data.list) && data.list.length > 0) {
				return data.list;
			}

			// 如果没有 list，从 leftList 和 rightList 合并
			if ((data.leftList && data.leftList.length > 0) || (data.rightList && data.rightList.length > 0)) {
				return [...(data.leftList || []), ...(data.rightList || [])];
			}

			return [];
		},

		/**
		 * 重置分类数据
		 * @param {Number} categoryIndex - 分类索引
		 */
		resetCategoryData(categoryIndex) {
			const categoryKey = `category_${categoryIndex}`;
			const categoryData = this.getCategoryData(categoryIndex);
			
			this.$set(categoryData, 'list', []);
			this.$set(categoryData, 'leftList', []);
			this.$set(categoryData, 'rightList', []);
			this.$set(categoryData, 'leftHeight', 0);
			this.$set(categoryData, 'rightHeight', 0);
			this.$set(categoryData, 'page', 1);
			this.$set(categoryData, 'status', 'loadmore');
			this.$set(categoryData, 'total', 0);
			this.$set(categoryData, 'requestId', null);
		},
	}
}

