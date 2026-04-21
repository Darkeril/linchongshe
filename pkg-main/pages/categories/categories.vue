<template>
	<view class="categories-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 顶部导航栏（微信端系统栏已显示「全部分类」，此处不重复展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="navbar" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="back-btn" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="navbar-title">全部分类</view>
			<view class="placeholder"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端已无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<view class="content" :style="{height: contentHeight}">
			<!-- 左侧一级分类 -->
			<scroll-view scroll-y class="left-menu">
				<view 
					v-for="(item, index) in categoryList" 
					:key="item.id"
					class="menu-item"
					:class="{ active: currentIndex === index }"
					@click="selectCategory(index)">
					<text class="menu-text">{{ item.name }}</text>
					<view v-if="currentIndex === index" class="active-indicator"></view>
				</view>
			</scroll-view>
			
			<!-- 右侧二级分类 -->
			<scroll-view scroll-y class="right-content">
				<view v-if="currentCategory" class="category-section">
					<!-- 分类标题 -->
					<view class="section-title">
						<view class="title-line"></view>
						<text class="title-text">{{ currentCategory.name }}</text>
						<view class="title-line"></view>
					</view>
					
					<!-- 二级分类网格 -->
					<view class="sub-category-grid">
						<view 
							v-for="sub in currentCategory.children" 
							:key="sub.id"
							class="sub-item"
							@click="goToCategoryProducts(sub)">
							<view class="sub-icon">
								<image v-if="sub.icon" :src="sub.icon" mode="aspectFill" class="icon-img"></image>
								<view v-else class="icon-placeholder">
									<u-icon name="grid" size="30" color="#999"></u-icon>
								</view>
							</view>
							<text class="sub-name">{{ sub.name }}</text>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
import { getIdleCategoryList } from '@/apis/idles_service.js';
import weixinNavBar from '@/utils/weixinNavBar.js';

export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			currentIndex: 0,
			categoryList: []
		};
	},
	computed: {
		currentCategory() {
			return this.categoryList[this.currentIndex] || null;
		},
		// 微信端无自定义顶栏占位，内容区填满视口
		contentHeight() {
			// #ifdef MP-WEIXIN
			return '100vh';
			// #endif
			// #ifndef MP-WEIXIN
			return 'calc(100vh - ' + (this.statusBarHeight + 44) + 'px)';
			// #endif
		}
	},
	onLoad() {
		const systemInfo = uni.getSystemInfoSync();
		this.applyWeixinNavBar(systemInfo, 44);
		this.loadCategories();
	},
	onShow() {
		// 再次进入页面时重新拉取分类（如从后台改完分类后返回）
		this.loadCategories();
	},
	onPullDownRefresh() {
		// 下拉刷新时重新拉取分类
		this.loadCategories().finally(() => {
			uni.stopPullDownRefresh();
		});
	},
	methods: {
		// 加载分类数据
		async loadCategories() {
			try {
				const res = await getIdleCategoryList();
				if (res.code === 200 && res.data) {
					this.categoryList = res.data.map(item => ({
						id: item.id,
						name: item.title,
						pid: item.pid,
						children: (item.children || []).map(child => ({
							id: child.id,
							name: child.title || child.name,
							icon: child.icon || child.normalCover || ''
						}))
					}));
				}
			} catch (error) {
				console.error('加载分类失败:', error);
				uni.showToast({
					title: '加载分类失败',
					icon: 'none'
				});
			}
		},
		
		// 选择一级分类
		selectCategory(index) {
			this.currentIndex = index;
		},
		
		// 跳转到分类商品列表
		goToCategoryProducts(subCategory) {
			console.log('点击二级分类:', subCategory);
			// 跳转到商品列表页面
			uni.navigateTo({
				url: `/pkg-main/pages/categoryProducts/categoryProducts?categoryId=${subCategory.id}&categoryName=${subCategory.name}`
			});
		},
		
		// 返回
		goBack() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped lang="scss">
.categories-page {
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
		position: absolute;
		left: 50%;
		transform: translateX(-50%);
		font-size: 17px;
		font-weight: 500;
		color: #333;
	}
	
	.placeholder {
		width: 40px;
	}
}

.content {
	display: flex;
	overflow: hidden;
}

.left-menu {
	width: 130rpx;
	height: 100%;
	background-color: #f8f8f8;
	
	.menu-item {
		height: 100rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		background-color: #f8f8f8;
		
		&.active {
			background-color: #fff;
			
			.menu-text {
				color: #3d8af5;
				font-weight: 500;
			}
		}
		
		.menu-text {
			font-size: 14px;
			color: #333;
		}
		
		.active-indicator {
			position: absolute;
			left: 0;
			top: 50%;
			transform: translateY(-50%);
			width: 4px;
			height: 30rpx;
			background-color: #3d8af5;
			border-radius: 0 4px 4px 0;
		}
	}
}

.right-content {
	flex: 1;
	height: 100%;
	background-color: #fff;
	padding: 20rpx 30rpx;
}

.category-section {
	.section-title {
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 30rpx 0 40rpx;
		
		.title-line {
			flex: 1;
			height: 1px;
			background-color: #e5e5e5;
			max-width: 60rpx;
		}
		
		.title-text {
			font-size: 16px;
			font-weight: 500;
			color: #333;
			margin: 0 20rpx;
		}
	}
	
	.sub-category-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 30rpx 20rpx;
		
		.sub-item {
			display: flex;
			flex-direction: column;
			align-items: center;
			padding: 10rpx;
			
			.sub-icon {
				width: 120rpx;
				height: 120rpx;
				border-radius: 50%;
				background-color: #f8f8f8;
				overflow: hidden;
				display: flex;
				align-items: center;
				justify-content: center;
				margin-bottom: 15rpx;
				
				.icon-img {
					width: 100%;
					height: 100%;
				}
				
				.icon-placeholder {
					width: 100%;
					height: 100%;
					display: flex;
					align-items: center;
					justify-content: center;
				}
			}
			
			.sub-name {
				font-size: 13px;
				color: #333;
				text-align: center;
			}
		}
	}
}
</style>
