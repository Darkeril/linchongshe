<template>
	<view class="category-products-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 顶部导航栏 -->
		<view class="navbar" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<!-- #ifndef MP-WEIXIN -->
			<view class="back-btn" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<!-- #endif -->
			<view class="navbar-title">{{ categoryName }}</view>
			<view class="placeholder"></view>
		</view>
		
		<!-- 头部占位 -->
		<view :style="{height: navPlaceholderHeight}"></view>
		
		<!-- 筛选排序栏 -->
		<view class="filter-bar">
			<view 
				v-for="(item, index) in filterOptions" 
				:key="index"
				class="filter-item"
				:class="{ active: currentFilter === index }"
				@click="selectFilter(index)">
				<text class="filter-text">{{ item.name }}</text>
				<u-icon v-if="item.showArrow" name="arrow-down" size="12" :color="currentFilter === index ? '#1890ff' : '#999'"></u-icon>
			</view>
		</view>
		
		<!-- 商品列表 -->
		<scroll-view 
			scroll-y 
			:style="{height: scrollHeight + 'px'}" 
			@scrolltolower="onReachBottom"
			:refresher-enabled="true" 
			@refresherrefresh="onRefresh" 
			:refresher-triggered="refreshing">
			<!-- 使用瀑布流组件 -->
			<idle-water-fall 
				:list="productList" 
				:userId="currentUserId" 
				:showCollection="true"
				@detail="goToDetail"
				@collection="handleCollection"
				@user-click="goToUser">
			</idle-water-fall>
			
			<!-- 加载状态 -->
			<view class="load-more">
				<u-loadmore :status="loadStatus" />
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getIdlesByCategoryId, praiseOrCancelIdles } from '@/apis/idles_service.js';
import weixinNavBar from '@/utils/weixinNavBar.js';

export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			scrollHeight: 0,
			categoryId: '',
			categoryName: '',
			currentUserId: '',
			currentFilter: 0,
			filterOptions: [
				{ name: '销量', value: 'sales', showArrow: true },
				{ name: '时间', value: 'time', showArrow: true },
				{ name: '价格', value: 'price', showArrow: true }
			],
			productList: [],
			currentPage: 1,
			pageSize: 10,
			refreshing: false,
			loadStatus: 'loadmore'
		};
	},
	onLoad(options) {
		// 获取状态栏高度
		const systemInfo = uni.getSystemInfoSync();
		this.applyWeixinNavBar(systemInfo, 44);
		// 计算滚动区域高度：屏幕高度 - 状态栏 - 导航栏 - 筛选栏
		this.scrollHeight = systemInfo.windowHeight - 44 - 44;
		
		// 获取分类信息
		this.categoryId = options.categoryId || '';
		this.categoryName = options.categoryName || '商品列表';
		
		// 获取用户ID
		const userInfo = uni.getStorageSync('userInfo');
		this.currentUserId = userInfo ? userInfo.id : '';
		
		// 加载商品列表
		this.loadProducts();
	},
	methods: {
		// 加载商品列表
		async loadProducts(isRefresh = false) {
			try {
				if (isRefresh) {
					this.currentPage = 1;
					this.productList = [];
				}
				
				this.loadStatus = 'loading';
				
				const res = await getIdlesByCategoryId({
					cid: this.categoryId,
					page: this.currentPage,
					pageSize: this.pageSize
				});
				
				console.log('商品列表数据:', res);
				
				if (res.code === 200 && res.data && res.data.records) {
					// 映射数据字段
					const records = res.data.records.map(this.mapProductItem);
					console.log('映射后的商品数据:', records.slice(0, 2)); // 打印前2条数据
					
					if (isRefresh) {
						this.productList = records;
					} else {
						this.productList = [...this.productList, ...records];
					}
					
					// 更新加载状态
					if (records.length < this.pageSize) {
						this.loadStatus = 'nomore';
					} else {
						this.loadStatus = 'loadmore';
					}
				} else {
					this.loadStatus = 'nomore';
				}
			} catch (error) {
				console.error('加载商品失败:', error);
				this.loadStatus = 'loadmore';
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			} finally {
				this.refreshing = false;
			}
		},
		
		// 选择筛选项
		selectFilter(index) {
			this.currentFilter = index;
			// TODO: 根据筛选项重新加载数据
			this.loadProducts(true);
		},
		
		// 下拉刷新
		onRefresh() {
			this.refreshing = true;
			this.loadProducts(true);
		},
		
		// 上拉加载更多
		onReachBottom() {
			if (this.loadStatus === 'loadmore') {
				this.currentPage++;
				this.loadProducts();
			}
		},
		
		// 处理收藏
		async handleCollection(data) {
			try {
				const { item, index, position } = data;
				const res = await praiseOrCancelIdles({
					notesId: item.id,
					targetUserId: item.belongUserId || item.uid
				});
				
				if (res.code === 200) {
					// 更新列表中的收藏状态
					const product = this.productList.find(p => p.id === item.id);
					if (product) {
						product.isCollection = !product.isCollection;
						if (product.isCollection) {
							product.notesLikeNum = (product.notesLikeNum || 0) + 1;
						} else {
							product.notesLikeNum = Math.max(0, (product.notesLikeNum || 0) - 1);
						}
					}
					uni.showToast({
						title: product.isCollection ? '收藏成功' : '取消收藏',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('收藏操作失败:', error);
			}
		},
		
		// 跳转到用户主页
		goToUser(userId) {
			console.log('跳转到用户主页:', userId);
			if (userId) {
				uni.navigateTo({
					url: `/pkg-detail/pages/userHomePage/userHomePage?userId=${userId}`
				});
			}
		},
		
		// 跳转到商品详情
		goToDetail(productId, productType) {
			console.log('跳转到商品详情:', { productId, productType });
			
			// 确保ID存在
			if (!productId) {
				console.error('商品ID不存在');
				return;
			}
			
			// 根据商品类型跳转到不同的详情页
			// productType: 0=普通商品，2=视频商品
			if (productType === 2 || productType === '2') {
				// 视频商品
				uni.navigateTo({
					url: `/pkg-detail/pages/idleDetail/idleVideoD?productId=${productId}`
				});
			} else {
				// 普通商品
				uni.navigateTo({
					url: `/pkg-detail/pages/idleDetail/idleDetail?productId=${productId}`
				});
			}
		},
		
		// 数据字段映射（与市集页面保持一致）
		mapProductItem(item) {
			return {
				...item,
				id: item.id || item.noteId, // 确保有ID
				coverPicture: item.cover,
				avatarUrl: item.avatar,
				nickname: item.username,
				notesLikeNum: parseInt(item.likeCount, 10) || 0,
				isCollection: item.isCollect || false,
				productType: parseInt(item.noteType, 10) || 0,
				content: item.content || '',
				title: item.title,
				belongUserId: item.uid,
				noteCoverHeight: parseInt(item.noteCoverHeight, 10) || 0,
				imgList: item.imgUrls || [],
				province: item.province,
				city: item.city,
				address: item.address,
				price: item.price,
				originPrice: item.originalPrice,
				postType: item.postType // 0=邮寄，1=自提
			};
		},
		
		// 返回
		goBack() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped lang="scss">
.category-products-page {
	width: 100vw;
	min-height: 100vh;
	background-color: #f5f5f5;
}

.navbar {
	height: 44px;
	background-color: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 15px;
	border-bottom: 1px solid #eee;
	
	.back-btn {
		width: 40px;
		height: 40px;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.navbar-title {
		flex: 1;
		text-align: center;
		font-size: 17px;
		font-weight: 500;
		color: #333;
	}
	
	.placeholder {
		width: 40px;
	}
}

.filter-bar {
	position: sticky;
	top: 0;
	z-index: 100;
	display: flex;
	height: 44px;
	background-color: #fff;
	border-bottom: 1px solid #eee;
	
	.filter-item {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 5rpx;
		
		&.active {
			.filter-text {
				color: #1890ff;
				font-weight: 500;
			}
		}
		
		.filter-text {
			font-size: 14px;
			color: #333;
		}
	}
}

.load-more {
	padding: 20rpx 0;
	background-color: #f5f5f5;
}
</style>
