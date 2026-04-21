/**
 * 市集页面 - UI交互 Mixin
 * 负责导航、弹窗等UI交互方法
 */
export default {
	methods: {
		/**
		 * 跳转到搜索页面
		 */
		goToSearch() {
			uni.navigateTo({
				url: '/pkg-search/pages/searchPage/productSearchPage'
			})
		},

		/**
		 * 跳转到订单页面
		 */
		goToOrders() {
			uni.navigateTo({
				url: '/pkg-mine/pages/orders/orders'
			})
		},

		/**
		 * 显示更多选项弹窗
		 */
		showMoreOptions() {
			console.log('点击了更多按钮，当前状态:', this.showMoreDropdown);
			this.showMoreDropdown = !this.showMoreDropdown;
			console.log('切换后状态:', this.showMoreDropdown);
		},

		/**
		 * 关闭更多选项弹窗
		 */
		closeMoreModal() {
			this.showMoreDropdown = false;
		},

		/**
		 * 跳转到购物车（收藏）
		 */
		goToShoppingCart() {
			this.closeMoreModal();
			uni.navigateTo({
				url: '/pkg-mine/pages/shoppingCart/shoppingCart'
			});
		},

		/**
		 * 跳转到地址管理
		 */
		goToAddressManage() {
			this.closeMoreModal();
			const userInfo = uni.getStorageSync('userInfo');
			if (!userInfo || !userInfo.id) {
				uni.showToast({
					title: '请先登录',
					icon: 'none'
				});
				return;
			}
			uni.navigateTo({
				url: `/pkg-mine/pages/addressManagement/addressManagement?uid=${userInfo.id}`
			});
		},

		/**
		 * 跳转到分类页面
		 */
		goToCategories() {
			// 如果弹窗打开，先关闭
			if (this.showMoreDropdown) {
				this.closeMoreModal();
			}
			uni.navigateTo({
				url: '/pkg-main/pages/categories/categories'
			});
		},

		/**
		 * 跳转到心愿单
		 */
		goToCustomerService() {
			uni.navigateTo({
				url: '/pkg-mine/pages/shoppingCart/shoppingCart'
			})
		},

		/**
		 * 显示筛选选项
		 */
		showFilterOptions() {
			uni.showToast({
				title: '筛选功能开发中',
				icon: 'none'
			});
		},

		/**
		 * 显示排序选项
		 */
		showSortOptions() {
			uni.showActionSheet({
				itemList: ['最新发布', '价格从低到高', '价格从高到低', '最多收藏'],
				success: (res) => {
					const sortOptions = ['最新发布', '价格从低到高', '价格从高到低', '最多收藏'];
					uni.showToast({
						title: '已选择：' + sortOptions[res.tapIndex],
						icon: 'none'
					});
				}
			});
		},

		/**
		 * 显示分类管理
		 */
		showCategoryManage() {
			uni.showToast({
				title: '分类管理功能开发中',
				icon: 'none'
			});
		},

		/**
		 * 跳转到用户主页
		 * @param {String|Number} id - 用户ID
		 */
		goToUser(id) {
			uni.navigateTo({
				url: '/pkg-mine/pages/mine/otherMine?userId=' + id
			})
		},

		/**
		 * 跳转到商品详情
		 * @param {String|Number} id - 商品ID
		 * @param {Number} type - 商品类型（0=普通，1=自提，2=视频）
		 */
		goToDetail(id, type) {
			console.log('🏪 市集商品点击:', {
				id,
				type,
				typeTabIndex: this.typeTabIndex
			});

			// 检查是否从视频分类进入
			const fromVideoCategory = this.typeTabIndex === 1 && this.findList[this.typeTabIndex]?.isVideo;

			// 根据商品类型跳转到对应的详情页
			if (type === 2) {
				// 视频商品
				console.log('📹 跳转到视频商品详情页', fromVideoCategory ? '（从视频分类）' : '');
				let url = '/pkg-detail/pages/idleDetail/idleVideoD?productId=' + id;
				if (fromVideoCategory) {
					url += '&fromVideoCategory=1';
				}
				uni.navigateTo({
					url: url
				});
			} else {
				// 普通商品（包括 type === 0, 1 或其他值）
				console.log('📦 跳转到普通商品详情页');
				uni.navigateTo({
					url: '/pkg-detail/pages/idleDetail/idleDetail?productId=' + id
				});
			}
		},

		/**
		 * 动画完成
		 * @param {Object} e - 事件对象
		 */
		animationFinish(e) {
			this.enablerefresh = true;
		},

		/**
		 * 过渡动画
		 * @param {Object} e - 事件对象
		 */
		transition(e) {
			if (e.target.dx == 0) {
				this.enablerefresh = true;
			} else {
				this.enablerefresh = false;
			}
		},

		/**
		 * 更新当前Tab
		 * @param {Number} index - Tab索引
		 */
		updateCurrentTab(index) {
			console.log('切换到 tab:', index);
		},
	}
}

