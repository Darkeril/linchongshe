<template>
	<view class="shopping-cart-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航（微信端标题在系统栏展示，不重复） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">我的收藏</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 购物车内容 -->
		<scroll-view 
			scroll-y="true" 
			class="cart-scroll"
			:style="scrollHeight ? 'height: ' + scrollHeight + 'px' : ''"
			refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@refresherrestore="onRestore"
		>
			<!-- 空购物车状态 -->
			<view v-if="cartItems.length === 0" class="empty-cart">
				<view class="empty-illustration">
					<!-- 购物车盒子图标 -->
					<view class="cart-box-icon">
						<view class="box-body"></view>
						<view class="box-flap flap-1"></view>
						<view class="box-flap flap-2"></view>
						<view class="box-flap flap-3"></view>
						<view class="box-flap flap-4"></view>
					</view>
					<!-- 背景装饰元素 -->
					<view class="bg-elements">
						<view class="bg-circle circle-1"></view>
						<view class="bg-circle circle-2"></view>
						<view class="bg-circle circle-3"></view>
					</view>
				</view>
				<view class="empty-text">您还没有收藏商品，快去收藏喜欢的商品吧</view>
			</view>
			
			<!-- 购物车商品列表 -->
			<view v-else class="cart-items">
				<view 
					class="cart-item" 
					:class="{ 'item-sold-out': isSoldOut(item) }"
					v-for="(item, index) in cartItems" 
					:key="index"
				>
					<view class="item-image" @click="goToProductDetail(item)">
						<image :src="item.image" class="product-img" mode="aspectFill"></image>
						<!-- 待支付标签（当前用户的订单） -->
						<view v-if="isPendingPayment(item)" class="pending-payment-badge" @click.stop="goToPayment(item)">待支付</view>
						<!-- 卖掉了标签（其他用户的订单） -->
						<view v-else-if="isSoldOut(item)" class="sold-out-badge">卖掉了</view>
					</view>
					
					<view class="item-info">
						<!-- 商品标题 -->
						<view class="item-name" :class="{ 'item-disabled': isSoldOut(item) }" @click="goToProductDetail(item)">
							{{ item.name }}
						</view>
						
						<!-- 商品描述 -->
						<view class="item-description" v-if="item.description">
							{{ item.description }}
						</view>
						
						<!-- 价格和地址信息 -->
						<view class="item-price-location-row">
							<view class="price-info">
								<text class="current-price">¥{{ item.price }}</text>
								<text class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
							</view>
							<view class="location-info">
								<text class="item-spec">{{ item.spec }}</text>
							</view>
						</view>
						
						<!-- 用户信息 -->
                        <view class="user-info" v-if="item.productData && item.productData.username">
                            <image 
                                :src="item.productData.avatar || '/static/img/default-avatar.png'" 
                                class="user-avatar"
                                mode="aspectFill"
                            ></image>
                            <text class="user-name">{{ item.productData.username }}</text>
                            <view class="collect-section" @click.stop="toggleCollection(item, index)">
                                <u-icon
                                    v-if="!(item.productData.isCollection === true)"
                                    class="collect-icon"
                                    name="star"
                                    size="18"
                                    color="#bbb"
                                ></u-icon>
                                <u-icon
                                    v-else
                                    class="collect-icon"
                                    name="star-fill"
                                    size="18"
                                    color="#ffcc00"
                                ></u-icon>
                                <text class="collect-count">{{ item.productData.likeCount || 0 }}人想要</text>
                            </view>
                        </view>
					</view>
				</view>
			</view>
		</scroll-view>
		
		
		<!-- 开发中提示 -->
		<u-popup 
			v-model="showDevNotice" 
			mode="center" 
			:border-radius="20"
			:mask-close-able="false"
		>
			<view class="dev-notice">
				<view class="dev-icon">🚧</view>
				<view class="dev-title">商城功能开发中</view>
				<view class="dev-desc">购物车功能正在紧张开发中，敬请期待！</view>
				<view class="dev-tip">预计将在下个版本上线</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
import { getCartItems } from '@/apis/cart_service'
import { praiseOrCancelIdles } from '@/apis/idles_service'
import { getOrdersByUser, createPaymentOrder } from '@/apis/order_service.js'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
		mixins: [weixinNavBar],
		data() {
		return {
			statusBarHeight: 0,
			scrollHeight: 0,
			cartItems: [],
			loading: false,
			showDevNotice: false, // 显示开发中提示
			currentPage: 1,
			pageSize: 20,
			total: 0,
			hasMore: true,
			needRefresh: false, // 是否需要刷新数据
			refreshing: false // 下拉刷新状态
		}
	},
	
	computed: {
		// 商品总数
		totalCount() {
			return this.cartItems.length
		}
	},
	
	onLoad() {
		// 设置状态栏样式为白色背景
		// #ifdef MP-WEIXIN
		uni.setNavigationBarColor({
			frontColor: '#000000', // 状态栏文字颜色为黑色
			backgroundColor: '#ffffff' // 状态栏背景色为白色
		})
		// #endif
		
		const systemInfo = uni.getSystemInfoSync()
		this.applyWeixinNavBar(systemInfo, 44)
		// 与订单页一致：用 windowHeight 计算滚动区高度，避免底部遮挡
		// #ifdef MP-WEIXIN
		this.scrollHeight = systemInfo.windowHeight
		// #endif
		// #ifndef MP-WEIXIN
		this.scrollHeight = systemInfo.windowHeight - (this.statusBarHeight || 0) - 44
		// #endif
		this.loadCartData()
	},
	
	onShow() {
		// 页面显示时检查是否需要刷新数据
		if (this.needRefresh) {
			// 重置分页参数，重新加载第一页数据
			this.currentPage = 1
			this.hasMore = true
			this.needRefresh = false
			this.loadCartData()
		}
	},
	
	methods: {
		/**
		 * 返回上一页
		 */
		goBack() {
			uni.navigateBack()
		},
		
		/**
		 * 下拉刷新
		 */
		async onRefresh() {
			console.log('=== 开始下拉刷新 ===')
			this.refreshing = true
			
			try {
				// 重置分页参数
				this.currentPage = 1
				this.hasMore = true
				console.log('重置分页参数 - 当前页:', this.currentPage, '是否有更多:', this.hasMore)
				
				// 清空当前数据，确保刷新效果
				this.cartItems = []
				console.log('清空当前数据，商品数量:', this.cartItems.length)
				
				// 重新请求接口加载数据（不显示loading）
				console.log('开始调用loadCartData方法...')
				await this.loadCartData(false)
				
				console.log('=== 下拉刷新完成 ===')
				console.log('最终商品数量:', this.cartItems.length)
			} catch (error) {
				console.error('=== 下拉刷新失败 ===')
				console.error('错误详情:', error)
				uni.showToast({
					title: '刷新失败',
					icon: 'none'
				})
			} finally {
				// 立即结束刷新状态
				this.refreshing = false
				console.log('刷新状态结束，refreshing:', this.refreshing)
			}
		},
		
		/**
		 * 刷新状态恢复
		 */
		onRestore() {
			console.log('刷新状态恢复')
			this.refreshing = false
		},
		
		/**
		 * 加载购物车数据
		 */
		async loadCartData(showLoading = true) {
			if (showLoading) {
				this.loading = true
			}
			try {
				// 加载收藏的商品
				console.log('=== 开始请求接口 ===')
				console.log('请求参数 - 页码:', this.currentPage, '每页数量:', this.pageSize)
				console.log('请求URL:', `/idle/collection/getNoticeCollection/${this.currentPage}/${this.pageSize}`)
				
				const cartRes = await getCartItems(this.currentPage, this.pageSize)
				
				console.log('=== 接口响应 ===')
				console.log('响应状态码:', cartRes?.code)
				console.log('响应数据:', cartRes)
				
				if (cartRes.code === 200 && cartRes.data) {
					const pageData = cartRes.data
					
					// 获取当前用户ID
					const userInfo = uni.getStorageSync('userInfo')
					const currentUserId = userInfo ? (userInfo.id || userInfo.userId) : null
					
					// 转换数据格式
					const items = (pageData.records || []).map(item => ({
						id: item.id,
						name: item.title,
						description: item.description || item.content || '',
						image: this.getProductCover(item),
						spec: this.getProductSpec(item),
						price: parseFloat(item.price) || 0,
						originalPrice: item.originalPrice ? parseFloat(item.originalPrice) : null,
						status: item.status || 0, // 商品状态：0-可售，1-已售出/被锁定
						buyUid: item.buyUid || '', // 购买者ID
						currentUserId: currentUserId, // 当前用户ID
						// 保留原始数据
						productData: item
					}))
					
					// 设置购物车数据
					if (this.currentPage === 1) {
						// 第一页时直接替换数据
						this.cartItems = items
						console.log('刷新后商品数量:', this.cartItems.length)
					} else {
						// 后续页时追加数据
						this.cartItems = [...this.cartItems, ...items]
					}
					
					// 设置分页信息
					this.total = pageData.total || 0
					this.hasMore = this.cartItems.length < this.total
					
					console.log('当前页:', this.currentPage, '商品总数:', this.total, '已加载:', this.cartItems.length)
				} else {
					this.cartItems = []
					this.total = 0
					this.hasMore = false
				}
				
			} catch (error) {
				console.error('加载购物车数据失败:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'none'
				})
			} finally {
				if (showLoading) {
					this.loading = false
				}
			}
		},
		
		/**
		 * 获取商品封面图
		 */
		getProductCover(product) {
			if (product.cover) {
				return product.cover
			}
			if (product.urls) {
				try {
					const urlList = JSON.parse(product.urls)
					return urlList && urlList.length > 0 ? urlList[0] : '/static/img/default-product.png'
				} catch (e) {
					return '/static/img/default-product.png'
				}
			}
			return '/static/img/default-product.png'
		},
		
		/**
		 * 获取商品规格描述
		 */
		getProductSpec(product) {
			const specs = []
			if (product.province) {
				specs.push(product.province)
			}
			if (product.postType === 0) {
				specs.push('邮寄')
			} else if (product.postType === 1) {
				specs.push('自提')
			}
			return specs.join(' · ') || '默认规格'
		},
		
		
		/**
		 * 判断是否是当前用户待支付的订单
		 */
		isPendingPayment(item) {
			// status=1 且 buyUid 等于当前用户ID，表示当前用户创建的订单但未支付
			// 注意：如果订单超时，status会恢复为0，buyUid会被清空，此时返回false
			const status = item.status === 1 || (item.productData && item.productData.status === 1)
			if (!status) {
				return false // 商品已释放，不是待支付状态
			}
			
			const buyUid = item.buyUid || (item.productData && item.productData.buyUid) || ''
			const currentUserId = item.currentUserId || (uni.getStorageSync('userInfo') && (uni.getStorageSync('userInfo').id || uni.getStorageSync('userInfo').userId))
			
			// 只有当status=1且buyUid等于当前用户ID时，才是当前用户待支付的订单
			return buyUid && currentUserId && String(buyUid) === String(currentUserId)
		},
		
		/**
		 * 判断商品是否已售出或被锁定（其他用户的订单）
		 */
		isSoldOut(item) {
			// status=1 表示已售出或被锁定（创建订单但未支付）
			// 注意：如果订单超时，status会恢复为0，此时返回false，商品恢复正常显示
			const status = item.status === 1 || (item.productData && item.productData.status === 1)
			if (!status) {
				return false // 商品已释放，恢复正常状态
			}
			
			// 如果是当前用户的订单，不显示"卖掉了"
			if (this.isPendingPayment(item)) {
				return false
			}
			
			// 其他用户的订单，显示"卖掉了"
			return true
		},
		
		/**
		 * 跳转到支付页面
		 */
		async goToPayment(item) {
			try {
				// 查询该商品的未支付订单
				const res = await getOrdersByUser({
					page: 1,
					limit: 50,
					status: 'unpaid' // 未支付状态
				})
				
				if (res.code === 200 && res.data && res.data.records) {
					const unpaidOrder = res.data.records.find(order => {
						const match = order.productId === item.id ||
							(order.product && order.product.id === item.id)
						if (!match) return false
						const st = String(order.orderStatus)
						return st === '0' || st === '1' || st === '2'
					})
					
					if (unpaidOrder) {
						let paymentOrderId = unpaidOrder.paymentOrderId
						if (!paymentOrderId) {
							const cr = await createPaymentOrder(unpaidOrder.id, '1')
							if (cr.code === 200 && cr.data) {
								paymentOrderId = cr.data
							}
						}
						if (paymentOrderId) {
							uni.navigateTo({
								url: `/pkg-others/pages/payEntry/payEntry?paymentOrderId=${paymentOrderId}`
							})
						} else {
							uni.showToast({ title: '创建支付订单失败', icon: 'none' })
						}
					} else {
						uni.showToast({
							title: '未找到待支付订单',
							icon: 'none'
						})
					}
				} else {
					uni.showToast({
						title: '查询订单失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('跳转支付页面失败:', error)
				uni.showToast({
					title: '跳转失败',
					icon: 'none'
				})
			}
		},
		
		/**
		 * 跳转到商品详情
		 */
		goToProductDetail(item) {
			// 如果商品已售出（其他用户的订单），禁止跳转
			if (this.isSoldOut(item)) {
				uni.showToast({
					title: '该商品已售出',
					icon: 'none'
				})
				return
			}
			
			// 如果是当前用户待支付的订单，跳转到支付页面
			if (this.isPendingPayment(item)) {
				this.goToPayment(item)
				return
			}
			
			// 正常商品，跳转到商品详情页
			uni.navigateTo({
				url: `/pkg-detail/pages/idleDetail/idleDetail?productId=${item.productData.id}`
			})
		},

        /**
         * 收藏/取消收藏
         */
        async toggleCollection(item, index) {
            const productData = item.productData || {}
            const isCollected = productData.isCollection === true
            try {
                const res = await praiseOrCancelIdles({
                    notesId: item.id,
                    targetUserId: productData.uid || ''
                })
                if (res && (res.code === 200 || res.success)) {
                    if (isCollected) {
                        // 取消收藏：更新状态、数量，并从列表中移除
                        this.$set(item.productData, 'isCollection', false)
                        const currentCount = item.productData.likeCount || 0
                        this.$set(item.productData, 'likeCount', Math.max(currentCount - 1, 0))
                        // 从收藏列表移除
                        this.cartItems.splice(index, 1)
                        uni.showToast({
                            title: '已取消收藏',
                            icon: 'none'
                        })
                    } else {
                        // 收藏
                        this.$set(item.productData, 'isCollection', true)
                        const currentCount = item.productData.likeCount || 0
                        this.$set(item.productData, 'likeCount', currentCount + 1)
                        uni.showToast({
                            title: '已收藏',
                            icon: 'success'
                        })
                    }
                } else {
                    uni.showToast({ 
                        title: (res && (res.msg || res.message)) || '操作失败', 
                        icon: 'none' 
                    })
                }
            } catch (error) {
                console.error('收藏操作失败:', error)
                uni.showToast({ 
                    title: '操作失败', 
                    icon: 'none' 
                })
            }
        },
		
		
		/**
		 * 显示开发中提示
		 */
		showDevelopmentNotice() {
			this.showDevNotice = true
			// 3秒后自动隐藏
			setTimeout(() => {
				this.showDevNotice = false
			}, 3000)
		}
	}
}
</script>

<style scoped>
.shopping-cart-page {
	background-color: #f5f5f5;
	min-height: 100vh;
}

.header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	padding: 0 30rpx;
	background-color: #fff;
	border-bottom: 1rpx solid #eee;
	position: relative;
}

.header-left, .header-right {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.header-title {
	font-size: 36rpx;
	font-weight: 600;
	color: #333;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
}

.manage-text {
	font-size: 28rpx;
	color: #ff6b35;
}

.cart-scroll {
	/* 高度优先用 scrollHeight（onLoad 后），未设置时用下方兜底 */
	height: calc(100vh - 88rpx - var(--status-bar-height));
	padding-bottom: 20rpx;
	/* 下拉刷新样式优化 */
	refresher-threshold: 100rpx;
	refresher-default-style: black;
	refresher-background: #f8f8f8;
}

.empty-cart {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.empty-illustration {
	width: 300rpx;
	height: 300rpx;
	margin-bottom: 40rpx;
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
}

/* 购物车盒子图标 */
.cart-box-icon {
	position: relative;
	width: 120rpx;
	height: 120rpx;
	transform: perspective(200rpx) rotateX(15deg) rotateY(-10deg);
}

.box-body {
	width: 100rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #ff8a65, #ff7043);
	border-radius: 8rpx;
	position: relative;
	box-shadow: 0 8rpx 20rpx rgba(255, 112, 67, 0.3);
}

.box-flap {
	position: absolute;
	background: linear-gradient(135deg, #ff8a65, #ff7043);
	border-radius: 4rpx;
}

.flap-1 {
	width: 100rpx;
	height: 20rpx;
	top: -10rpx;
	left: 0;
	transform: rotateX(45deg);
	transform-origin: bottom;
}

.flap-2 {
	width: 20rpx;
	height: 80rpx;
	top: 0;
	right: -10rpx;
	transform: rotateY(45deg);
	transform-origin: left;
}

.flap-3 {
	width: 100rpx;
	height: 20rpx;
	bottom: -10rpx;
	left: 0;
	transform: rotateX(-45deg);
	transform-origin: top;
}

.flap-4 {
	width: 20rpx;
	height: 80rpx;
	top: 0;
	left: -10rpx;
	transform: rotateY(-45deg);
	transform-origin: right;
}

/* 背景装饰元素 */
.bg-elements {
	position: absolute;
	width: 100%;
	height: 100%;
	pointer-events: none;
}

.bg-circle {
	position: absolute;
	border-radius: 50%;
	background: linear-gradient(135deg, #ffcc80, #ffb74d);
	opacity: 0.6;
}

.circle-1 {
	width: 20rpx;
	height: 20rpx;
	top: 20%;
	left: 15%;
	animation: float 3s ease-in-out infinite;
}

.circle-2 {
	width: 15rpx;
	height: 15rpx;
	top: 60%;
	right: 20%;
	animation: float 3s ease-in-out infinite 1s;
}

.circle-3 {
	width: 12rpx;
	height: 12rpx;
	bottom: 25%;
	left: 25%;
	animation: float 3s ease-in-out infinite 2s;
}

@keyframes float {
	0%, 100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-10rpx);
	}
}

.empty-text {
	font-size: 28rpx;
	color: #999;
	text-align: center;
	line-height: 1.5;
}

.cart-items {
	padding: 20rpx;
}

.cart-item {
	display: flex;
	align-items: flex-start;
	background-color: #fff;
	border-radius: 20rpx;
	padding: 15rpx 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	min-height: 200rpx;
	
}

/* 已售出商品置灰样式（其他用户的订单） */
.cart-item.item-sold-out {
	opacity: 0.6;
}

.cart-item.item-sold-out .product-img {
	filter: grayscale(100%);
}

.cart-item.item-sold-out .item-name,
.cart-item.item-sold-out .item-description,
.cart-item.item-sold-out .current-price,
.cart-item.item-sold-out .original-price {
	color: #999;
}


.item-image {
	width: 200rpx;
	height: 200rpx;
	margin-right: 20rpx;
	position: relative;
}

/* 已售出商品禁用点击 */
.cart-item.item-sold-out .item-image {
	pointer-events: none;
}

.product-img {
	width: 100%;
	height: 100%;
	border-radius: 10rpx;
}

/* 卖掉了标签 */
.sold-out-badge {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	background-color: rgba(0, 0, 0, 0.7);
	color: #fff;
	font-size: 28rpx;
	font-weight: 600;
	padding: 12rpx 24rpx;
	border-radius: 8rpx;
	z-index: 10;
	white-space: nowrap;
}

/* 待支付标签 */
.pending-payment-badge {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	background-color: rgba(255, 107, 53, 0.9);
	color: #fff;
	font-size: 28rpx;
	font-weight: 600;
	padding: 12rpx 24rpx;
	border-radius: 8rpx;
	z-index: 10;
	white-space: nowrap;
	cursor: pointer;
}

.item-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
	min-height: 170rpx;
}

.item-name {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.3;
	margin-bottom: 20rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.item-name.item-disabled {
	cursor: not-allowed;
}

.item-description {
	font-size: 24rpx;
	color: #666;
	line-height: 1.4;
	margin-bottom: 8rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.item-spec {
	font-size: 24rpx;
	color: #999;
	margin: 0;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.item-price-location-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8rpx;
}

.price-info {
	display: flex;
	align-items: center;
	flex: 1;
}

.location-info {
	display: flex;
	align-items: center;
	margin-right: 20rpx;
	flex-shrink: 0;
	max-width: 200rpx;
}

.current-price {
	font-size: 28rpx;
	color: #ff6b35;
	font-weight: 600;
	margin-right: 20rpx;
}

.original-price {
	font-size: 24rpx;
	color: #999;
	text-decoration: line-through;
}

.user-info {
	display: flex;
	align-items: center;
	margin-top: auto;
	margin-top: 25rpx;
	width: 100%;
}

.user-avatar {
	width: 40rpx;
	height: 40rpx;
	border-radius: 50%;
	margin-right: 12rpx;
}

.user-name {
	font-size: 24rpx;
	color: #666;
	/* 单行省略，随可用空间自适应 */
	flex: 1;
	min-width: 0;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	margin-right: 16rpx;
}

.collect-section {
	display: flex;
	align-items: center;
	/* 靠右对齐，避免固定间距导致换行 */
	margin-left: auto;
	flex-shrink: 0;
}

.collect-icon {
	margin-right: 10rpx;
}

.collect-count {
	font-size: 20rpx;
	color: #999;
	white-space: nowrap;
}




/* 开发中提示样式 */
.dev-notice {
	padding: 60rpx 40rpx;
	text-align: center;
	background: #fff;
	border-radius: 20rpx;
	min-width: 500rpx;
}

.dev-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.dev-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.dev-desc {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
	margin-bottom: 15rpx;
}

.dev-tip {
	font-size: 24rpx;
	color: #999;
	font-style: italic;
}
</style>
