<template>
	<view class="orders-page">
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
			<view class="header-title">我的订单</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 订单状态筛选 -->
		<view class="status-tabs">
			<view 
				class="tab-item" 
				:class="{active: currentTab === index}"
				v-for="(tab, index) in statusTabs" 
				:key="index"
				@click="switchTab(index)"
			>
				{{ tab.name }}
			</view>
		</view>
		
		<!-- 订单列表 swiper -->
		<swiper 
			:current="currentTab" 
			@change="onSwiperChange"
			class="orders-swiper"
			:style="{height: swiperHeight + 'px'}"
		>
			<swiper-item v-for="(tab, tabIndex) in statusTabs" :key="tabIndex">
				<scroll-view 
					scroll-y="true" 
					class="orders-scroll"
					@scrolltolower="loadMore(tabIndex)"
					:refresher-enabled="true"
					:refresher-triggered="tabData[tabIndex].refreshing"
					@refresherrefresh="onRefresh(tabIndex)"
				>
					<view class="orders-list" v-if="tabData[tabIndex].ordersList.length > 0">
						<view 
							class="order-item" 
							v-for="(order, index) in tabData[tabIndex].ordersList" 
							:key="index"
							@click="goToOrderDetail(order)"
						>
							<!-- 订单头部 -->
							<view class="order-header">
								<view class="order-number">订单号: {{ order.orderNumber }}</view>
								<view class="order-status" :class="'status-' + order.status">
									{{ getStatusText(order.status) }}
								</view>
							</view>
							
							<!-- 商品信息 -->
							<view class="product-info" v-for="(product, pIndex) in order.products" :key="pIndex">
								<view class="product-image">
									<image 
										:src="product.image" 
										class="product-img"
										mode="aspectFill"
										@error="onImageError"
									></image>
								</view>
								<view class="product-details">
									<view class="product-name">{{ product.name }}</view>
									<view class="product-spec">{{ product.spec }}</view>
									<view class="product-tags" v-if="product.tags">
										<text class="tag">{{ product.tags }}</text>
									</view>
									<view class="product-price-quantity">
										<text class="price">¥{{ product.price }}</text>
										<text class="quantity">×{{ product.quantity }}</text>
									</view>
								</view>
							</view>
							
							<!-- 订单汇总 -->
							<view class="order-summary">
								<text class="summary-text">共{{ order.totalQuantity }}件商品</text>
								<text class="total-price">¥{{ order.totalPrice }}</text>
							</view>
							
							<!-- 操作按钮：0/1/2 均为待付款流程，均需「取消 + 付款」 -->
							<view class="order-actions">
								<view 
									class="action-btn cancel-btn" 
									v-if="isUnpaidFlow(order.status)"
									@click.stop="cancelOrder(order)"
								>
									取消订单
								</view>
								<view 
									class="action-btn pay-btn" 
									v-if="isUnpaidFlow(order.status)"
									@click.stop="payOrder(order)"
								>
									付款
								</view>
								<view 
									class="action-btn confirm-btn" 
									v-if="order.status === 'shipped'"
									@click.stop="confirmOrder(order)"
								>
									确认收货
								</view>
								<view 
									class="action-btn review-btn" 
									v-if="order.status === 'received'"
									@click.stop="reviewOrder(order)"
								>
									评价
								</view>
							</view>
						</view>
					</view>
					
					<!-- 空状态 -->
					<view v-else-if="!tabData[tabIndex].loading" class="empty-state">
						<u-icon name="order" size="60" color="#ccc"></u-icon>
						<text class="empty-text">暂无订单</text>
						<text class="empty-desc">快去购买一些商品吧</text>
					</view>
					
					<!-- 加载更多 -->
					<u-loadmore 
						v-if="tabData[tabIndex].ordersList.length > 0" 
						:status="tabData[tabIndex].loadStatus" 
						:loading-text="loadingText"
						:loadmore-text="loadmoreText" 
						:nomore-text="nomoreText"
					></u-loadmore>
				</scroll-view>
				
				<!-- 加载中 -->
				<u-loading-page :loading="tabData[tabIndex].loading && tabData[tabIndex].ordersList.length === 0"></u-loading-page>
			</swiper-item>
		</swiper>
		
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
				<view class="dev-desc">订单功能正在紧张开发中，敬请期待！</view>
				<view class="dev-tip">预计将在下个版本上线</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
import { getOrdersByUser, cancelOrderById, createPaymentOrder, confirmOrderById } from '@/apis/order_service'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
		mixins: [weixinNavBar],
		data() {
			// 初始化每个标签的数据
			const initTabData = () => {
				const tabs = [
					{ name: '全部', status: 'all' },
					{ name: '待付款', status: 'unpaid' },
					{ name: '待发货', status: 'pending' },
					{ name: '待收货', status: 'shipped' },
					{ name: '已签收', status: 'received' }
				]
				return tabs.map(() => ({
					ordersList: [],
					page: 1,
					loading: false,
					refreshing: false,
					loadStatus: 'loadmore', // loadmore, loading, nomore
					hasLoaded: false // 是否已加载过数据
				}))
			}
			
			return {
				statusBarHeight: 0,
				currentTab: 0, // 默认显示"全部"标签（索引0）
				statusTabs: [
					{ name: '全部', status: 'all' },
					{ name: '待付款', status: 'unpaid' },
					{ name: '待发货', status: 'pending' },
					{ name: '待收货', status: 'shipped' },
					{ name: '已签收', status: 'received' }
				],
				tabData: initTabData(), // 每个标签的独立数据
				pageSize: 10,
				loadingText: '加载中...',
				loadmoreText: '轻轻下拉',
				nomoreText: '没有更多了',
				showDevNotice: false, // 显示开发中提示
				fromPayPage: false, // 标记是否从支付页面返回
				swiperHeight: 0 // swiper 高度
			}
		},
	
	onLoad(options) {
		// 设置状态栏样式为白色背景
		// #ifdef MP-WEIXIN
		uni.setNavigationBarColor({
			frontColor: '#000000', // 状态栏文字颜色为黑色
			backgroundColor: '#ffffff' // 状态栏背景色为白色
		})
		// #endif
		
		const systemInfo = uni.getSystemInfoSync()
		this.applyWeixinNavBar(systemInfo, 44)
		// #ifdef MP-WEIXIN
		this.swiperHeight = systemInfo.windowHeight - 40
		// #endif
		// #ifndef MP-WEIXIN
		this.swiperHeight = systemInfo.windowHeight - (this.statusBarHeight || 0) - 44 - 40
		// #endif
		
		// 加载第一个标签的数据
		this.loadOrders(0)
	},
	
	onShow() {
		// 监听刷新事件（从支付页面返回时触发）
		uni.$once('refreshOrderList', (data) => {
			console.log('收到刷新订单列表事件:', data)
			// 切换到待付款标签
			if (data && data.tab === 'unpaid') {
				const unpaidTabIndex = this.statusTabs.findIndex(tab => tab.status === 'unpaid')
				if (unpaidTabIndex !== -1) {
					this.currentTab = unpaidTabIndex
					console.log('切换到待付款标签，索引:', unpaidTabIndex)
				}
			}
			// 强制刷新当前标签的数据
			const currentTabData = this.tabData[this.currentTab]
			currentTabData.page = 1
			currentTabData.ordersList = []
			currentTabData.hasLoaded = false
			// 立即刷新
			this.loadOrders(this.currentTab)
		})
		
		// 检查页面栈，判断是否从支付页面返回（备用方案）
		const pages = getCurrentPages()
		if (pages.length > 1) {
			const prevPage = pages[pages.length - 2]
			if (prevPage && prevPage.route && prevPage.route.includes('payEntry')) {
				console.log('检测到从支付页面返回，刷新订单列表')
				// 确保在"待付款"标签下
				const unpaidTabIndex = this.statusTabs.findIndex(tab => tab.status === 'unpaid')
				if (unpaidTabIndex !== -1 && this.currentTab !== unpaidTabIndex) {
					this.currentTab = unpaidTabIndex
				}
				// 强制刷新当前标签的数据
				const currentTabData = this.tabData[this.currentTab]
				currentTabData.page = 1
				currentTabData.ordersList = []
				currentTabData.hasLoaded = false
				this.$nextTick(() => {
					this.loadOrders(this.currentTab)
				})
			}
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
		 * 切换订单状态标签
		 */
		switchTab(index) {
			if (this.currentTab === index) return
			this.currentTab = index
			// 如果该标签还没有加载过数据，则加载
			if (!this.tabData[index].hasLoaded) {
				this.loadOrders(index)
			}
		},
		
		/**
		 * swiper 切换事件
		 */
		onSwiperChange(e) {
			const index = e.detail.current
			if (this.currentTab === index) return
			this.currentTab = index
			// 如果该标签还没有加载过数据，则加载
			if (!this.tabData[index].hasLoaded) {
				this.loadOrders(index)
			}
		},
		
		/**
		 * 加载订单列表
		 * @param {Number} tabIndex 标签索引，如果不传则使用 currentTab
		 */
		async loadOrders(tabIndex) {
			const index = tabIndex !== undefined ? tabIndex : this.currentTab
			const tabDataItem = this.tabData[index]
			
			if (tabDataItem.loading) return
			
			tabDataItem.loading = true
			tabDataItem.loadStatus = 'loading'
			
			try {
				const currentStatus = this.statusTabs[index].status
				console.log('=== 开始加载订单 ===')
				console.log('标签索引:', index, '请求参数 - 页码:', tabDataItem.page, '每页数量:', this.pageSize, '状态:', currentStatus)
				
				const res = await getOrdersByUser({
					page: tabDataItem.page,
					limit: this.pageSize,
					status: currentStatus === 'all' ? undefined : currentStatus
				})
				
				console.log('=== 订单API响应 ===')
				console.log('响应状态码:', res?.code)
				console.log('响应数据:', res)
				
				if (res.code === 200) {
					const pageData = res.data || {}
					const records = pageData.records || []
					const total = pageData.total || 0
					
					console.log('获取到订单数量:', records.length, '总数:', total)
					console.log('原始订单数据:', records)
					
					// 处理订单数据，转换状态值，并过滤掉已取消的订单（除非专门查询已取消）
					const processedRecords = records
						.map(order => {
							const priceFen = Number(order.productMoney || 0)
							const totalFen = Number(order.actualBuyMoney != null ? order.actualBuyMoney : order.answerBuyMoney || 0)
							return {
								...order,
								status: this.convertOrderStatus(order),
								// 处理商品信息（单商品订单）
								products: order.productId ? [{
									id: order.productId,
									name: order.productTitle || '商品名称',
									image: order.productCover || '/static/images/default-product.png',
									price: this.formatPrice(priceFen),
									quantity: order.productNum || 1,
									spec: order.productDescription || ''
								}] : [],
								totalQuantity: order.productNum || 1,
								totalPrice: this.formatPrice(totalFen)
							}
						})
						.filter(order => {
							// 如果不是专门查询已取消的订单，则过滤掉已取消的订单
							if (currentStatus !== 'cancelled' && order.status === 'cancelled') {
								return false
							}
							return true
						})
					
					console.log('处理后的订单数据:', processedRecords)
					
					if (tabDataItem.page === 1) {
						tabDataItem.ordersList = processedRecords
					} else {
						tabDataItem.ordersList = tabDataItem.ordersList.concat(processedRecords)
					}
					
					// 更新加载状态
					if (records.length < this.pageSize || tabDataItem.ordersList.length >= total) {
						tabDataItem.loadStatus = 'nomore'
					} else {
						tabDataItem.loadStatus = 'loadmore'
						tabDataItem.page++
					}
					
					tabDataItem.hasLoaded = true
				} else {
					console.error('订单API返回错误:', res)
					uni.showToast({
						title: res.message || '加载失败',
						icon: 'none'
					})
					tabDataItem.loadStatus = 'loadmore'
				}
			} catch (error) {
				console.error('加载订单失败:', error)
				// 检查是否是404错误
				if (error.status === 404 || error.code === 404 || (error.message && error.message.includes('404'))) {
					this.showDevelopmentNotice()
				} else {
					uni.showToast({
						title: '网络错误',
						icon: 'none'
					})
				}
				tabDataItem.loadStatus = 'loadmore'
			} finally {
				tabDataItem.loading = false
				tabDataItem.refreshing = false
				console.log('订单加载完成, loading:', tabDataItem.loading)
			}
		},
		
		/**
		 * 加载更多
		 */
		loadMore(tabIndex) {
			const index = tabIndex !== undefined ? tabIndex : this.currentTab
			const tabDataItem = this.tabData[index]
			
			if (tabDataItem.loadStatus === 'loadmore' && !tabDataItem.loading) {
				this.loadOrders(index)
			}
		},

		/**
		 * 金额分转元保留两位
		 */
		formatPrice(fen) {
			const n = Number(fen || 0)
			return (n / 100).toFixed(2)
		},
		
		/**
		 * 下拉刷新
		 * @param {Number} tabIndex 标签索引
		 */
		async onRefresh(tabIndex) {
			const index = tabIndex !== undefined ? tabIndex : this.currentTab
			const tabDataItem = this.tabData[index]
			
			console.log('=== 开始订单下拉刷新 ===', '标签索引:', index)
			tabDataItem.refreshing = true
			tabDataItem.page = 1
			console.log('重置分页参数 - 当前页:', tabDataItem.page)
			
			try {
				// 清空现有数据
				tabDataItem.ordersList = []
				console.log('清空现有数据，订单数量:', tabDataItem.ordersList.length)
				
				// 重新加载数据
				console.log('开始调用loadOrders方法...')
				await this.loadOrders(index)
				
				console.log('=== 订单下拉刷新完成 ===')
				console.log('最终订单数量:', tabDataItem.ordersList.length)
			} catch (error) {
				console.error('=== 订单下拉刷新失败 ===')
				console.error('错误详情:', error)
			} finally {
				// 立即结束刷新状态
				tabDataItem.refreshing = false
				console.log('刷新状态结束，refreshing:', tabDataItem.refreshing)
			}
		},
		
		/**
		 * 跳转到订单详情
		 */
		goToOrderDetail(order) {
			uni.navigateTo({
				url: `/pkg-mine/pages/orderDetail/orderDetail?orderId=${order.id}`
			})
		},
		
		/**
		 * 取消订单
		 */
		async cancelOrder(order) {
			uni.showModal({
				title: '确认取消',
				content: '确定要取消这个订单吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '处理中...' })
							const result = await cancelOrderById(order.id)
							if (result.code === 200) {
								uni.showToast({
									title: '订单已取消',
									icon: 'success'
								})
								// 刷新当前标签的订单列表
								const currentTabData = this.tabData[this.currentTab]
								currentTabData.page = 1
								currentTabData.ordersList = []
								currentTabData.hasLoaded = false
								this.loadOrders(this.currentTab)
							} else {
								uni.showToast({
									title: result.message || '取消失败',
									icon: 'none'
								})
							}
						} catch (error) {
							console.error('取消订单失败:', error)
							// 检查是否是404错误
							if (error.status === 404 || error.code === 404 || (error.message && error.message.includes('404'))) {
								this.showDevelopmentNotice()
							} else {
								uni.showToast({
									title: '取消失败',
									icon: 'none'
								})
							}
						} finally {
							uni.hideLoading()
						}
					}
				}
			})
		},
		
		/**
		 * 支付订单
		 */
		async payOrder(order) {
			try {
				uni.showLoading({ title: '跳转支付...' })
				
				let paymentOrderId = order.paymentOrderId
				
				// 如果订单已有支付订单ID，直接跳转；否则创建新的支付订单
				if (!paymentOrderId) {
					// 创建支付订单（默认使用微信支付）
					const result = await createPaymentOrder(order.id, '1')
					
					if (result.code === 200 && result.data) {
						paymentOrderId = result.data
					} else {
						uni.showToast({
							title: result.msg || result.message || '创建支付订单失败',
							icon: 'none'
						})
						return
					}
				}
				
				// 记录当前标签，确保返回时保持在"待付款"标签
				const unpaidTabIndex = this.statusTabs.findIndex(tab => tab.status === 'unpaid')
				if (unpaidTabIndex !== -1) {
					this.currentTab = unpaidTabIndex
				}
				
				// 跳转到支付页面
				uni.navigateTo({
					url: `/pkg-others/pages/payEntry/payEntry?paymentOrderId=${paymentOrderId}`
				})
			} catch (error) {
				console.error('支付订单失败:', error)
				uni.showToast({
					title: error.message || '支付失败，请稍后重试',
					icon: 'none'
				})
			} finally {
				uni.hideLoading()
			}
		},
		
		/**
		 * 确认收货
		 */
		async confirmOrder(order) {
			uni.showModal({
				title: '确认收货',
				content: '确定已收到商品吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '处理中...' })
							const result = await confirmOrderById(order.id)
							if (result.code === 200) {
								uni.showToast({
									title: '确认收货成功',
									icon: 'success'
								})
								// 刷新当前标签的订单列表
								const currentTabData = this.tabData[this.currentTab]
								currentTabData.page = 1
								currentTabData.ordersList = []
								currentTabData.hasLoaded = false
								this.loadOrders(this.currentTab)
							} else {
								uni.showToast({
									title: result.message || '确认失败',
									icon: 'none'
								})
							}
			} catch (error) {
				console.error('确认收货失败:', error)
				// 检查是否是404错误
				if (error.status === 404 || error.code === 404 || (error.message && error.message.includes('404'))) {
					this.showDevelopmentNotice()
				} else {
					uni.showToast({
						title: '确认失败',
						icon: 'none'
					})
				}
			} finally {
							uni.hideLoading()
						}
					}
				}
			})
		},
		
		/**
		 * 评价订单
		 */
		reviewOrder(order) {
			uni.navigateTo({
				url: `/pkg-mine/pages/review/review?orderId=${order.id}`
			})
		},
		
		/** 待付款相关 Tab：含未支付(0)/待支付(1)/支付中(2) */
		isUnpaidFlow(status) {
			return status === 'unpaid' || status === 'pending_payment' || status === 'paying'
		},
		
		/**
		 * 转换后端订单状态为前端状态
		 * 后端：0-未支付，1-待支付，2-支付中，3-已支付，4-交易完成，5-已取消
		 * 已支付(3) 需结合 deal_status：4=待收货，否则待发货
		 */
		convertOrderStatus(order) {
			const orderStatus = order != null && typeof order === 'object' ? order.orderStatus : order
			const dealStatus = order != null && typeof order === 'object' ? order.dealStatus : undefined
			const key = orderStatus != null ? String(orderStatus) : ''
			if (key === '3' && String(dealStatus) === '4') {
				return 'shipped'
			}
			const statusMap = {
				'0': 'unpaid',
				'1': 'pending_payment',
				'2': 'paying',
				'3': 'pending',
				'4': 'received',
				'5': 'cancelled'
			}
			return statusMap[key] || 'unpaid'
		},
		
		/**
		 * 获取订单状态文本
		 */
		getStatusText(status) {
			const statusMap = {
				'unpaid': '未支付',
				'pending_payment': '待付款',
				'paying': '支付中',
				'pending': '待发货',
				'shipped': '待收货',
				'received': '已签收',
				'cancelled': '已取消'
			}
			return statusMap[status] || '未知状态'
		},
		
		/**
		 * 获取订单状态样式类
		 */
		getStatusClass(status) {
			const classMap = {
				'unpaid': 'status-unpaid',
				'pending_payment': 'status-unpaid',
				'paying': 'status-unpaid',
				'pending': 'status-pending',
				'shipped': 'status-shipped',
				'received': 'status-received',
				'cancelled': 'status-cancelled'
			}
			return classMap[status] || ''
		},
		
		/**
		 * 图片加载错误处理
		 */
		onImageError(e) {
			console.log('商品图片加载失败:', e)
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
.orders-page {
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
}

.status-tabs {
	display: flex;
	width: 100%;
	background-color: #fff;
	border-bottom: 1rpx solid #eee;
}

.tab-item {
	flex: 1;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	color: #666;
	position: relative;
}

.tab-item.active {
	color: #3d8af5;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 10rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background-color: #3d8af5;
	border-radius: 2rpx;
}

.orders-swiper {
	width: 100%;
}

.orders-scroll {
	height: 100%;
}

.orders-list {
	padding: 20rpx;
}

.order-item {
	background-color: #fff;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.order-number {
	font-size: 26rpx;
	color: #666;
}

.order-status {
	font-size: 26rpx;
	font-weight: 600;
}

.status-unpaid {
	color: #ff6b35;
}

.status-pending_payment {
	color: #ff6b35;
}

.status-paying {
	color: #ff6b35;
}

.status-pending {
	color: #ffa500;
}

.status-shipped {
	color: #1890ff;
}

.status-received {
	color: #52c41a;
}

.status-cancelled {
	color: #999;
}

.product-info {
	display: flex;
	margin-bottom: 20rpx;
}

.product-image {
	width: 120rpx;
	height: 120rpx;
	margin-right: 20rpx;
}

.product-img {
	width: 100%;
	height: 100%;
	border-radius: 10rpx;
}

.product-details {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.product-name {
	font-size: 28rpx;
	color: #333;
	line-height: 1.4;
	margin-bottom: 10rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.product-spec {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 10rpx;
}

.product-tags {
	margin-bottom: 10rpx;
}

.tag {
	display: inline-block;
	padding: 4rpx 12rpx;
	background-color: #f0f0f0;
	color: #666;
	font-size: 20rpx;
	border-radius: 10rpx;
}

.product-price-quantity {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.price {
	font-size: 28rpx;
	color: #ff6b35;
	font-weight: 600;
}

.quantity {
	font-size: 24rpx;
	color: #999;
}

.order-summary {
	display: flex;
	justify-content: flex-end;
	align-items: center;
	margin-bottom: 20rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
}

.summary-text {
	font-size: 24rpx;
	color: #666;
	margin-right: 20rpx;
}

.total-price {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}

.order-actions {
	display: flex;
	justify-content: flex-end;
	gap: 20rpx;
}

.action-btn {
	padding: 12rpx 30rpx;
	border-radius: 30rpx;
	font-size: 24rpx;
	text-align: center;
	min-width: 120rpx;
}

.cancel-btn {
	background-color: #fff;
	color: #666;
	border: 1rpx solid #ddd;
}

.pay-btn {
	background-color: #3d8af5;
	color: #fff;
}

.confirm-btn {
	background-color: #1890ff;
	color: #fff;
}

.review-btn {
	background-color: #52c41a;
	color: #fff;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
	margin: 30rpx 0 20rpx;
}

.empty-desc {
	font-size: 28rpx;
	color: #ccc;
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

