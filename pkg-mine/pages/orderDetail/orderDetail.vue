<template>
	<view class="order-detail-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="status-bar-bg" :style="{height: statusBarHeight + 'px'}"></view>
		<!-- #endif -->
		<!-- 标题栏 -->
		<view class="header" :style="{top: navBarTop}">
			<!-- #ifndef MP-WEIXIN -->
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<!-- #endif -->
			<view class="header-title">订单详情</view>
			<view class="header-right"></view>
		</view>
		
		<scroll-view scroll-y class="content" :style="{paddingTop: contentPaddingTop + 'px'}">
			<view v-if="loading" class="loading">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<view v-else-if="!orderDetail" class="empty">
				<text class="empty-icon">📦</text>
				<text class="empty-text">未找到订单</text>
			</view>
			
			<view v-else>
				<!-- 订单信息卡片 -->
				<view class="info-card">
					<view class="card-header">
						<text class="section-title">订单信息</text>
					</view>
					<view class="card-body">
						<view class="order-row">
							<text class="label">订单号</text>
							<view class="value-wrapper">
								<text class="value">{{ orderDetail.orderNumber || '-' }}</text>
							</view>
						</view>
						<view class="order-row">
							<text class="label">订单状态</text>
							<view class="status-badge" :class="'status-' + orderDetail.orderStatus">
								<text>{{ orderDetail.orderStatusText || '未知' }}</text>
							</view>
						</view>
						<view class="order-row" v-if="orderDetail.createTime">
							<text class="label">创建时间</text>
							<view class="value-wrapper">
								<text class="value">{{ formatDate(orderDetail.createTime) }}</text>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 商品信息卡片 -->
				<view class="info-card" v-if="orderDetail.productTitle || orderDetail.productCover">
					<view class="card-header">
						<text class="section-title">商品信息</text>
					</view>
					<view class="card-body">
						<view class="product-section" v-if="orderDetail.productCover || orderDetail.productTitle">
							<image v-if="orderDetail.productCover" :src="orderDetail.productCover" mode="aspectFill" class="product-image"></image>
							<view class="product-info" v-if="orderDetail.productTitle">
								<text class="product-title">{{ orderDetail.productTitle }}</text>
								<text class="product-price">¥{{ formatPrice(orderDetail.productMoney || 0) }}</text>
							</view>
						</view>
						<view class="order-row">
							<text class="label">应付金额</text>
							<text class="price">¥{{ formatPrice(orderDetail.answerBuyMoney || orderDetail.actualBuyMoney || 0) }}</text>
						</view>
						<view class="order-row" v-if="orderDetail.payTypeName">
							<text class="label">支付方式</text>
							<view class="pay-type-badge" :class="orderDetail.payType === '1' ? 'wechat-badge' : 'alipay-badge'">
								<image 
									v-if="orderDetail.payType === '1'" 
									class="pay-icon" 
									src="/static/wx.png" 
									mode="aspectFit"
								></image>
								<image 
									v-else 
									class="pay-icon" 
									src="/static/zfb.png" 
									mode="aspectFit"
								></image>
								<text>{{ orderDetail.payTypeName }}</text>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 收货信息卡片 -->
				<view class="info-card" v-if="orderDetail.postUsername || orderDetail.postAddress">
					<view class="card-header">
						<text class="section-title">收货信息</text>
					</view>
					<view class="card-body">
						<view class="order-row" v-if="orderDetail.postUsername">
							<text class="label">收货人</text>
							<view class="value-wrapper">
								<text class="value">{{ orderDetail.postUsername }}</text>
							</view>
						</view>
						<view class="order-row" v-if="orderDetail.postPhone">
							<text class="label">联系电话</text>
							<view class="value-wrapper">
								<text class="value">{{ orderDetail.postPhone }}</text>
							</view>
						</view>
						<view class="order-row address-row" v-if="orderDetail.postAddress">
							<text class="label">收货地址</text>
							<view class="value-wrapper">
								<text class="value address-value">{{ orderDetail.postAddress }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getOrderDetail } from '@/apis/order_service'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			orderId: '',
			orderDetail: null,
			loading: false
		}
	},
	computed: {
		// 计算内容区域的padding-top（状态栏高度 + header高度88rpx转px + 额外间距20rpx转px）
		contentPaddingTop() {
			const headerHeightPx = uni.upx2px(88)
			const extraSpacingPx = uni.upx2px(20)
			return (this.statusBarHeight || 0) + headerHeightPx + extraSpacingPx
		}
	},
	onLoad(options) {
		const systemInfo = uni.getSystemInfoSync()
		this.applyWeixinNavBar(systemInfo, 44)
		this.orderId = options?.orderId || ''
		if (this.orderId) {
			this.loadDetail()
		} else {
			uni.showToast({ title: '订单ID缺失', icon: 'none' })
		}
	},
	methods: {
		goBack() {
			uni.navigateBack()
		},
		formatPrice(fen) {
			const n = Number(fen || 0)
			return (n / 100).toFixed(2)
		},
		formatDate(dateStr) {
			if (!dateStr) return '-'
			const date = new Date(dateStr)
			const year = date.getFullYear()
			const month = String(date.getMonth() + 1).padStart(2, '0')
			const day = String(date.getDate()).padStart(2, '0')
			const hours = String(date.getHours()).padStart(2, '0')
			const minutes = String(date.getMinutes()).padStart(2, '0')
			return `${year}-${month}-${day} ${hours}:${minutes}`
		},
		async loadDetail() {
			this.loading = true
			try {
				const res = await getOrderDetail(this.orderId)
				if (res.code === 200) {
					this.orderDetail = res.data
				} else {
					uni.showToast({ title: res.message || '加载失败', icon: 'none' })
				}
			} catch (e) {
				console.error('加载订单详情失败:', e)
				uni.showToast({ title: e.message || '网络错误', icon: 'none' })
			} finally {
				this.loading = false
			}
		}
	}
}
</script>

<style scoped lang="scss">
.order-detail-page {
	min-height: 100vh;
	background: #f5f5f5;
	width: 100%;
	max-width: 100%;
	box-sizing: border-box;
	overflow-x: hidden;
}
.status-bar-bg {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	width: 100%;
	background-color: #ffffff;
	z-index: 1001;
}
.header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	padding: 0 30rpx;
	background: #fff;
	border-bottom: 1rpx solid #eee;
	position: fixed;
	left: 0;
	right: 0;
	z-index: 1000;
}
.header-left, .header-right {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.header-title {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}
.content {
	padding: 20rpx;
	box-sizing: border-box;
	width: 100%;
	max-width: 100%;
	overflow: hidden;
}
.loading, .empty {
	margin-top: 120rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	color: #999;
	gap: 12rpx;
}
.loading-spinner {
	width: 48rpx;
	height: 48rpx;
	border: 4rpx solid #e0e0e0;
	border-top-color: #3d8af5;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}
@keyframes spin {
	to { transform: rotate(360deg); }
}
.loading-text, .empty-text {
	font-size: 28rpx;
	color: #999;
}
.empty-icon {
	font-size: 120rpx;
}

// 信息卡片样式
.info-card {
	background: #fff;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
	overflow: hidden;
	width: 100%;
	box-sizing: border-box;
	max-width: 100%;
	
	.card-header {
		padding: 30rpx 30rpx 20rpx;
		border-bottom: 1px solid #f0f0f0;
		box-sizing: border-box;
		
		.section-title {
			font-size: 32rpx;
			font-weight: 600;
			color: #333;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
	}
	
	.card-body {
		padding: 20rpx 30rpx 30rpx;
		box-sizing: border-box;
		width: 100%;
		max-width: 100%;
		overflow: hidden;
	}
}

// 订单行样式
.order-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
	min-width: 0;
	width: 100%;
	box-sizing: border-box;
	
	&:last-child {
		margin-bottom: 0;
	}
	
	&.address-row {
		align-items: flex-start;
	}
	
	.label { 
		color: #999; 
		font-size: 28rpx; 
		width: 160rpx;
		flex-shrink: 0;
		flex-grow: 0;
		box-sizing: border-box;
	}
	
	.value-wrapper {
		flex: 1;
		min-width: 0;
		max-width: calc(100% - 160rpx - 20rpx);
		box-sizing: border-box;
		overflow: hidden;
		display: flex;
		justify-content: flex-end;
	}
	
	.value { 
		color: #333; 
		font-size: 28rpx; 
		text-align: right;
		word-break: break-all;
		overflow: hidden;
		box-sizing: border-box;
		max-width: 100%;
		
		&.address-value {
			text-align: right;
			line-height: 1.5;
			word-break: break-all;
			white-space: normal;
			overflow-wrap: break-word;
		}
		
		// 对于普通文本，使用单行省略
		&:not(.address-value) {
			white-space: nowrap;
			text-overflow: ellipsis;
		}
	}
	
	.price { 
		color: #ff2442; 
		font-size: 36rpx; 
		font-weight: 700; 
		flex-shrink: 0;
		white-space: nowrap;
	}
}

// 状态徽章
.status-badge {
	display: inline-flex;
	align-items: center;
	padding: 8rpx 20rpx;
	border-radius: 50rpx;
	font-size: 26rpx;
	font-weight: 500;
	flex-shrink: 0; // 徽章不收缩
	white-space: nowrap; // 不换行
	
	&.status-0, &.status-1 {
		background: #fff7e6;
		color: #ff9500;
	}
	
	&.status-2 {
		background: #e6f4ff;
		color: #3d8af5;
	}
	
	&.status-3, &.status-4 {
		background: #f6ffed;
		color: #52c41a;
	}
	
	&.status-5 {
		background: #f5f5f5;
		color: #999;
	}
}

// 支付方式徽章
.pay-type-badge {
	display: inline-flex;
	align-items: center;
	padding: 10rpx 24rpx;
	border-radius: 50rpx;
	font-size: 26rpx;
	font-weight: 500;
	gap: 8rpx;
	flex-shrink: 0; // 徽章不收缩
	white-space: nowrap; // 不换行
	
	.pay-icon {
		width: 32rpx;
		height: 32rpx;
		flex-shrink: 0;
	}
	
	&.wechat-badge {
		background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
		color: #fff;
	}
	
	&.alipay-badge {
		background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
		color: #fff;
	}
}

// 商品信息区域
.product-section {
	display: flex;
	gap: 20rpx;
	margin-bottom: 24rpx;
	padding-bottom: 24rpx;
	border-bottom: 1px solid #f0f0f0;
	width: 100%;
	box-sizing: border-box;
	min-width: 0;
	
	.product-image {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		flex-shrink: 0;
		background: #f5f5f5;
		box-sizing: border-box;
	}
	
	.product-info {
		flex: 1;
		min-width: 0;
		max-width: calc(100% - 160rpx - 20rpx); // 减去图片宽度和间距
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		box-sizing: border-box;
		overflow: hidden;
		
		.product-title {
			font-size: 30rpx;
			color: #333;
			line-height: 1.5;
			font-weight: 500;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			overflow: hidden;
			margin-bottom: 16rpx;
			word-break: break-all;
			width: 100%;
			max-width: 100%;
			box-sizing: border-box;
		}
		
		.product-price {
			font-size: 32rpx;
			font-weight: 600;
			color: #ff2442;
			flex-shrink: 0;
			white-space: nowrap;
		}
	}
}
</style>


