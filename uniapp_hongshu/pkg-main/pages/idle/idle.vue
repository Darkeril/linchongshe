<template>
	<view>
		<!-- 状态栏背景（微信端由系统栏占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 顶部标题栏（H5 显示「市集」，微信端由系统栏展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="idle-header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-title">商城</view>
		</view>
		<!-- #endif -->
		<!-- 头部占位（微信端不预留） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		<view class="msg-divider"></view>

		<!-- 顶部搜索栏 -->
		<view class="search-header">
			<view class="search-container">
				<!-- 左侧分类图标 -->
				<view class="category-icon" @click="goToCategories">
					<u-icon name="grid" size="20" color="#666"></u-icon>
					<text class="category-text">分类</text>
				</view>

				<!-- 中间搜索框 -->
				<view class="search-box" @click="goToSearch">
					<u-icon name="search" size="16" color="#999" style="margin-right: 10rpx;"></u-icon>
					<text class="search-placeholder">输入商品关键字</text>
				</view>

				<!-- 右侧更多选项 -->
				<view class="more-icon" @click="showMoreOptions">
					<u-icon name="more-dot-fill" size="20" color="#666"></u-icon>
					<text class="more-text">更多</text>
				</view>
			</view>
		</view>

		<!-- 更多功能弹窗 -->
		<view v-if="showMoreDropdown" class="more-modal-overlay" @click="closeMoreModal">
			<view class="more-modal" @click.stop>
				<!-- 弹窗头部 -->
				<view class="modal-header">
					<text class="modal-title">更多功能</text>
					<view class="close-btn" @click="closeMoreModal">
						<u-icon name="close" size="20" color="#666"></u-icon>
					</view>
				</view>

				<!-- 功能网格 -->
				<view class="function-grid">
					<view class="function-item" @click="goToCategories">
						<view class="function-icon">
							<u-icon name="grid" size="25" color="#333"></u-icon>
						</view>
						<text class="function-text">分类</text>
					</view>

					<view class="function-item" @click="goToShoppingCart">
						<view class="function-icon">
							<u-icon name="bag" size="25" color="#333"></u-icon>
						</view>
						<text class="function-text">心愿单</text>
					</view>

					<view class="function-item" @click="goToOrders">
						<view class="function-icon">
							<u-icon name="list" size="25" color="#333"></u-icon>
						</view>
						<text class="function-text">订单</text>
					</view>

					<view class="function-item" @click="goToAddressManage">
						<view class="function-icon">
							<u-icon name="map" size="25" color="#333"></u-icon>
						</view>
						<text class="function-text">地址管理</text>
					</view>

				</view>
			</view>
		</view>

	<!-- Banner轮播 -->
	<swiper :indicator-dots="true" :autoplay="true" :circular="true"
		style="width: 100vw; height: 260rpx; margin: 0; padding: 0; overflow: hidden; display: block;">
		<swiper-item v-for="(item, idx) in bannerList" :key="idx">
			<image :src="item.url" mode="aspectFill" style="width: 100%; height: 100%; display: block;" />
		</swiper-item>
	</swiper>
	<!-- 分类tabs -->
	<view class="category-tabs-wrapper">
			<!-- <view style="width: 100%; height: 40px; background-color: #fff; z-index: 9999;"> -->
		<u-tabs @click="changeType" :current="typeTabIndex" :list="findList" :lineWidth="0" :activeStyle="{
		    color: '#222',
		    fontWeight: 'bold',  
		    fontSize: '33rpx',
		    padding: '0 15rpx',
		    height: '50rpx',  
		    display: 'flex',
		    alignItems: 'center',
		    justifyContent: 'center'
		  }" :inactiveStyle="{
		    color: '#606266',
		    fontSize: '30rpx',
		    padding: '0 15rpx',  
		    height: '50rpx',
		    display: 'flex',
		    alignItems: 'center',
		    justifyContent: 'center'
		  }" itemStyle="margin: 0; padding: 0rpx 1rpx; height: 70rpx;"></u-tabs>
		</view>
	<!-- 分类swiper -->
	<swiper :current="typeTabIndex" @change="onFindSwiperChange" :style="{height: notesHeight - 40 + 'px', margin: 0, padding: 0, display: 'block'}"
		:duration="300">
		<swiper-item v-for="(item, idx) in findList" :key="idx">
			<scroll-view scroll-y :style="{height: notesHeight - 40 + 'px', margin: 0, padding: 0}" @scrolltolower="onReach"
				:refresher-enabled="enablerefresh" @refresherrefresh="onRefresh" :refresher-triggered="refreshing"
				:refresher-threshold="100" :lower-threshold="50">
				<view style="margin: 0; padding: 0;">
						<!-- 🔧 修复：使用 getCategoryData(idx) 获取对应索引的分类数据 -->
						<!-- 使用稳定的 key，只基于分类索引，避免因数据长度变化导致组件重新创建 -->
						<idle-water-fall :key="`idle-waterfall-${idx}`" :list="getCategoryList(idx)"
							:userId="currentUserId" :showCollection="true" :ref="`idleWaterfall_${idx}`"
							@detail="goToDetail" @collection="handleCollection" @user-click="goToUser">
						</idle-water-fall>

						<!-- 市集页面空状态 -->
						<view v-if="getCategoryList(idx).length === 0 && getCategoryData(idx).status === 'nomore'"
							class="empty-state">
							<view class="empty-icon">🔍</view>
							<view class="empty-text">暂无相关商品</view>
							<view class="empty-tip">换个分类试试吧</view>
						</view>

						<!-- <u-loadmore 
							v-if="getCategoryList(idx).length > 0" 
							margin-top="20" 
							line 
							:status="getCategoryData(idx).status" 
							:loading-text="loadingText"
							:loadmore-text="loadmoreText" 
							:nomore-text="nomoreText" /> -->
					</view>
				</scroll-view>
			</swiper-item>
		</swiper>
		<!-- 客服悬浮按钮 -->
		<view class="customer-service-float-btn" @click="goToCustomerService">
			<u-icon name="bag" color="#ffffff" size="40"></u-icon>
		</view>

		<custom-tabbar ref="tabbar" :current="1" @update:current="updateCurrentTab"></custom-tabbar>
	</view>
</template>

<script>
	// import CustomTabbar from '@/components/custom-tabbar/CustomTabBar.vue'
	// 注意：所有 API 和工具函数的导入已迁移到对应的 mixin 中
	// - API 导入 -> idleApiMixin.js, idleCollectionMixin.js
	// - 工具函数导入 -> idleUtilsMixin.js
	
	// 引入 Mixins
	import idleDataMixin from './mixins/idleDataMixin.js'
	import idleUtilsMixin from './mixins/idleUtilsMixin.js'
	import idleUIMixin from './mixins/idleUIMixin.js'
	import idleCollectionMixin from './mixins/idleCollectionMixin.js'
	import idleApiMixin from './mixins/idleApiMixin.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	import { getCarouselConfig } from '@/apis/config_service.js'
	
	export default {
		// 使用 Mixins
		mixins: [
			idleDataMixin,
			idleUtilsMixin,
			idleUIMixin,
			idleCollectionMixin,
			idleApiMixin,
			weixinNavBar,
		],
		// components: {
		// 	CustomTabbar
		// },
		data() {
			return {
				showMoreDropdown: false, // 控制更多功能下拉框显示
				bannerList: [{
						url: 'https://mall4cloud.oss-cn-guangzhou.aliyuncs.com/2022/12/22/12c3f1f1ce7f44c5bd11e1023d274cc8'
					},
					{
						url: 'https://mall4cloud.oss-cn-guangzhou.aliyuncs.com/2022/12/22/9fab8089529942ee9934cb8e36a41fcd'
					},
					{
						url: 'https://mall4cloud.oss-cn-guangzhou.aliyuncs.com/2022/12/26/5a9d3219358c407cad594e2b7bbfd379'
					},
				],
				statusBarHeight: 0,
				notesHeight: 0,
				findList: [],
				typeTabIndex: 0,
				notesList: {
					notesList: [],
					leftList: [],
					rightList: [],
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					pageSize: 6, // 优化性能：减少每页数量避免 setData 过大
					// #ifdef MP-WEIXIN
					initialLoadSize: 3, // 小程序优化：首次加载3条，极速秒开
					// #endif
					// #ifndef MP-WEIXIN
					initialLoadSize: 5, // H5首次加载5条
					// #endif
				},
				loadingText: '加载中...',
				loadmoreText: '加载更多',
				nomoreText: '到底了',
				refreshing: false,
				enablerefresh: true,
				// 性能优化相关
				easeOutCubic: 'cubic-bezier(0.215, 0.61, 0.355, 1)',
				// 注意：categoryData 和 categoryCache 已移到 idleDataMixin
			}
		},
		computed: {
			// 🔧 修复：为每个分类创建计算属性，避免在模板中频繁调用方法
			// 注意：由于分类数量可能很多，这里不使用计算属性，而是优化 getCategoryData 方法
			// 获取当前用户ID
			currentUserId() {
				const userInfo = uni.getStorageSync('userInfo');
				return userInfo?.id || '';
			}
		},
		methods: {
			// 注意：以下方法已迁移到对应的 mixin 中，这里不再重复定义
			// - getCategoryData, getCategoryList -> idleDataMixin
			// - formatAddress, getImageHeight, mapIdleItem -> idleUtilsMixin
			// - goToSearch, goToOrders, showMoreOptions, closeMoreModal, goToShoppingCart, 
			//   goToAddressManage, goToCategories, goToUser, goToDetail, updateCurrentTab, 
			//   goToCustomerService, animationFinish, transition -> idleUIMixin
			// - handleCollection, praiseUserIdles, updateCategoryDataCollectionStatus -> idleCollectionMixin
			// - fetchFindList, changeType, handleTypeSwitch, onFindSwiperChange, loadCategoryData,
			//   processCategoryData, loadMoreCategoryData, appendCategoryData, preloadNextPage,
			//   onRefresh, refreshList, refreshFindNotes, loadMoreFindNotes -> idleApiMixin
			// 注意：refreshList, onRefresh 已迁移到 idleApiMixin，这里不再重复定义
			// 注意：changeType, handleTypeSwitch, setCategoryCache, getCategoryCache 已迁移到 idleApiMixin
			// 调试：显示缓存状态
			// 注意：showCacheStatus 已迁移到 idleApiMixin，这里不再重复定义
			// 注意：loadCategoryData, processCategoryData, preloadNextCategory, preloadImages, 
			// getCompressedUrl, preloadCriticalImages, debounce, handleReach, loadMoreCategoryData,
			// preloadNextPage, appendCategoryData 已迁移到 idleApiMixin，这里不再重复定义
		},
		onLoad() {
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
					// 微信端无自定义标题栏，内容区按原逻辑；H5 减去标题栏 44px
					if (process.env.UNI_PLATFORM === 'mp-weixin') {
						this.notesHeight = res.windowHeight - (this.statusBarHeight || 0) + 40
					} else {
						this.notesHeight = res.windowHeight - (this.statusBarHeight || 0) - 44 + 40
					}
				}
			})
			// 首次加载使用新的分类数据加载逻辑
			this.handleTypeSwitch(0); // 加载推荐分类
			this.fetchFindList();
			// 拉取市集轮播图配置（后台系统配置-轮播图管理）
			getCarouselConfig().then((res) => {
				if (res && res.code === 200 && res.data && Array.isArray(res.data) && res.data.length > 0) {
					this.bannerList = res.data.map((item) => ({
						url: item.url || '',
						link: item.link || '',
						title: item.title || ''
					}))
				}
			}).catch(() => {})
			// 接收全局徽标更新事件
			uni.$on('im:setTabBadge', (count) => {
				const tabbar = this.$refs.tabbar
				if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(count)
			})
			// 首次进入时读取本地缓存的未读数，避免空白
			try {
				const cached = uni.getStorageSync('im_total_unread') || 0
				const tabbar = this.$refs.tabbar
				if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(cached)
			} catch (e) {}
		},

		onShow() {
			console.log('🔄 市集页面显示，检查数据状态');

			// 每次进入市集页都拉取最新轮播图配置（管理端修改后刷新即可生效）
			getCarouselConfig().then((res) => {
				if (res && res.code === 200 && res.data && Array.isArray(res.data) && res.data.length > 0) {
					this.bannerList = res.data.map((item) => ({
						url: item.url || '',
						link: item.link || '',
						title: item.title || ''
					}));
				}
			}).catch(() => {});

			// 检查当前分类是否有数据
			const categoryKey = `category_${this.typeTabIndex}`;
			const categoryData = this.categoryData[categoryKey];

			// 如果当前分类没有数据，重新加载
			if (!categoryData || ((!categoryData.list || categoryData.list.length === 0) && (!categoryData.leftList ||
					categoryData.leftList.length === 0) && (!categoryData.rightList || categoryData.rightList
					.length === 0))) {
				console.log('🔄 市集页面：当前分类无数据，重新加载');
				this.handleTypeSwitch(this.typeTabIndex);
			} else {
				console.log('✅ 市集页面：当前分类已有数据，跳过重新加载');
			}

			// 如果分类列表为空，重新获取
			if (!this.findList.length) {
				console.log('🔄 市集页面：分类列表为空，重新获取');
				this.fetchFindList();
			}
		},

		onUnload() {
			// 清理定时器
			if (this.typeSwitchTimer) {
				clearTimeout(this.typeSwitchTimer);
			}
		},
	};
</script>
<style lang="scss">
	/* 二级分类滚动隐藏样式 */
	.category-tabs-wrapper {
		width: 100%;
		height: 38px;
		background-color: #fff;
		z-index: 9999;
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		transform: translateY(0);
		opacity: 1;
		overflow: hidden;
		margin: 0; /* 移除默认 margin */
		padding: 0; /* 移除默认 padding */
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
		display: block; /* 确保块级显示，避免行内间隙 */
	}

	.category-tabs-wrapper.hide-tabs {
		transform: translateY(-100%);
		opacity: 0;
		height: 0;
		/* 高度为0，彻底隐藏 */
		box-shadow: none;
		/* 移除阴影 */
		pointer-events: none;
		/* 隐藏时不可点击 */
	}

	.msg-divider {
		width: 100%;
		height: 1rpx;
		background: #eaeaea;
		margin: 0;
		padding: 0;
	}

	/* 市集顶部标题栏（H5，与消息页一致） */
	.idle-header {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 44px;
		background-color: #fff;
		border-bottom: 1px solid #eee;
	}
	.idle-header .header-title {
		font-size: 18px;
		font-weight: 500;
		color: #333;
	}
	
	/* 确保页面元素紧密贴合 */
	swiper, swiper-item, scroll-view {
		margin: 0 !important;
		padding: 0 !important;
	}

	/* 搜索栏样式 */
	.search-header {
		background-color: #f8f8f8;
		padding: 15rpx 30rpx;
		border-bottom: 1rpx solid #eaeaea;
		position: relative;
		/* 添加相对定位，使下拉框能正确定位 */
	}

	.search-container {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 80rpx;
	}

	.category-icon,
	.more-icon {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		width: 80rpx;
		height: 80rpx;
		cursor: pointer;
	}

	.category-text,
	.more-text {
		font-size: 24rpx;
		color: #666;
		margin-top: 8rpx;
	}

	.search-box {
		flex: 1;
		height: 60rpx;
		background-color: #fff;
		border-radius: 30rpx;
		display: flex;
		align-items: center;
		padding: 0 20rpx;
		margin: 0 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
	}

	.search-placeholder {
		font-size: 28rpx;
		color: #999;
	}

	/* 更多功能弹窗样式 */
	.more-modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 99999;
		display: flex;
		justify-content: center;
		align-items: flex-start;
		padding-top: 0;
	}

	.more-modal {
		background-color: #fff;
		border-radius: 0 0 20rpx 20rpx;
		width: 100%;
		box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.2);
		animation: slideDown 0.3s ease-out;
	}

	@keyframes slideDown {
		from {
			transform: translateY(-100%);
			opacity: 0;
		}

		to {
			transform: translateY(0);
			opacity: 1;
		}
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.modal-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	.close-btn {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		justify-content: center;
		align-items: center;
		border-radius: 50%;
		background-color: #f5f5f5;
	}

	.function-grid {
		display: grid;
		grid-template-columns: 1fr 1fr 1fr 1fr;
		gap: 20rpx;
		padding: 30rpx;
	}

	.function-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 15rpx 15rpx;
		background-color: #f8f8f8;
		border-radius: 15rpx;
		transition: all 0.2s ease;
	}

	.function-item:active {
		background-color: #e8e8e8;
		transform: scale(0.95);
	}

	.function-icon {
		margin-bottom: 10rpx;
	}

	.function-text {
		font-size: 24rpx;
		color: #333;
		font-weight: 500;
		text-align: center;
	}

	.water-left,
	.water-right {
		width: 48%;
		margin: 20rpx auto;
	}

	.title {
		font-size: 30rpx;
		padding: 10rpx;
		color: #000000;
		overflow: hidden;
		text-overflow: ellipsis;
		word-break: break-all;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-height: 1.4em;
		max-height: 2.4em;
	}

	.note-username {
		margin-left: 10rpx;
		color: #16160e;
		font-size: 23rpx;
		line-height: 20px;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: calc(100% - 70px);
	}

	.look-views {
		display: flex;
		position: absolute;
		bottom: 185rpx;
		left: 8rpx;
		color: #ffffff;
		background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 5rpx 10rpx;
		border-radius: 50rpx;
		font-size: 22rpx;
		max-width: 70%; // 与发现页保持一致
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;

		u-icon {
			margin-right: 6rpx;
			flex-shrink: 0;
		}

		text {
			overflow: hidden;
			white-space: nowrap;
			text-overflow: ellipsis;
			display: inline-block;
			max-width: 90vw;
			vertical-align: middle;
		}
	}

	.video-play {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 10rpx;
		border-radius: 50%;
	}

	.water-left,
	.water-right {
		width: 50%;
		margin: 0rpx auto;
	}

	.note-card {
		background: #fff;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
		margin-top: 5rpx;
		margin-bottom: 24rpx;
		margin-left: 8rpx;
		margin-right: 8rpx;
		overflow: hidden;
		position: relative;
		border: 1rpx solid #f0f0f0;
	}

	.goods-price-row {
		display: flex;
		align-items: center;
		margin: 0 20rpx 8rpx 20rpx;
	}

	.goods-price {
		margin-bottom: -5px;
		color: #ff4d4f;
		font-size: 32rpx;
		font-weight: bold;
		margin-right: 12rpx;
	}

	.goods-origin-price {
		margin-bottom: -5px;
		color: #b0b0b0;
		font-size: 24rpx;
		text-decoration: line-through;
	}

	.post-type-tag {
		margin-bottom: -5px;
		display: inline-block;
		margin-left: 40rpx;
		padding: 0 12rpx;
		height: 30rpx;
		line-height: 30rpx;
		border: 1rpx solid rgb(243, 123, 29);
		color: rgb(243, 123, 29);
		border-radius: 14rpx;
		font-size: 22rpx;
		background: #fff;
		vertical-align: middle;
	}

	page,
	.page-bg {
		background: #f5f5f5;
	}

	// 优化"到底了"提示样式
	::v-deep .u-loadmore__nomore-text,
	::v-deep .u-loadmore-text {
		font-size: 24rpx !important;
		color: #c8c9cc !important;
	}

	::v-deep .u-loadmore__nomore-line {
		border-color: #e4e7ed !important;
	}

	/* 空状态样式 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 40rpx;
		text-align: center;
	}

	.empty-icon {
		font-size: 120rpx;
		margin-bottom: 30rpx;
		opacity: 0.6;
	}

	.empty-text {
		font-size: 32rpx;
		color: #666;
		margin-bottom: 20rpx;
		font-weight: 500;
	}

	.empty-tip {
		font-size: 24rpx;
		color: #999;
		line-height: 1.5;
	}

	/* 客服悬浮按钮样式 */
	.customer-service-float-btn {
		position: fixed !important;
		/* 使用 !important 确保样式生效 */
		right: 22rpx;
		bottom: 200rpx;
		/* 在 tabbar 上方（tabbar 高度约 75px + margin 20px = 95px，加上安全区域约 100-120px，按钮底部留出 200rpx 约 100px 的空间） */
		width: 100rpx;
		height: 100rpx;
		background: #3d8af5;
		border: none;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 30rpx 0 rgba(65, 147, 255, 0.4), 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.15);
		z-index: 99999 !important;
		/* 使用更高的 z-index 并添加 !important 确保在所有内容之上 */
		transition: all 0.3s ease;
		pointer-events: auto;
		/* 确保可以点击 */
		transform: translateZ(0);
		/* 触发 GPU 加速，提升渲染层级 */
		-webkit-transform: translateZ(0);
	}

	.customer-service-float-btn:active {
		transform: scale(0.95) translateZ(0);
		-webkit-transform: scale(0.95) translateZ(0);
		box-shadow: 0 4rpx 20rpx 0 rgba(65, 147, 255, 0.3), 0 2rpx 8rpx 0 rgba(0, 0, 0, 0.1);
	}


	/* 适配不同设备的安全区域 */
	/* #ifdef APP-PLUS */
	.customer-service-float-btn {
		bottom: calc(200rpx + env(safe-area-inset-bottom));
	}

	/* #endif */
</style>