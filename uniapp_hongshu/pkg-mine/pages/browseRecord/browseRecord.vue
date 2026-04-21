<template>
	<view class="browse-record-page">
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
			<view class="header-title">浏览记录</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 内容区域 -->
		<scroll-view 
			scroll-y="true" 
			class="content-scroll"
			:style="scrollHeight ? 'height: ' + scrollHeight + 'px' : ''"
			@scrolltolower="loadMore"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			:refresher-threshold="45"
			refresher-default-style="black"
			refresher-background="#f5f5f5"
		>
			<!-- 瀑布流布局 -->
			<water-fall 
				v-if="recordList.length > 0"
				:key="waterfallKey"
				:list="formattedRecordList"
				:slot_bottom="true"
				:showViews="false"
				:type="1"
				ref="waterfall"
				@init="onWaterfallInit"
			></water-fall>
			
			<!-- 空状态 -->
			<view v-else-if="!loading" class="empty-state">
				<u-icon name="clock" size="60" color="#ccc"></u-icon>
				<text class="empty-text">暂无浏览记录</text>
				<text class="empty-desc">快去浏览一些内容吧</text>
			</view>
			
			<!-- 加载更多 -->
			<u-loadmore 
				v-if="recordList.length > 0" 
				:status="loadStatus" 
				:loading-text="loadingText"
				:loadmore-text="loadmoreText"
				:nomore-text="nomoreText"
			></u-loadmore>
		</scroll-view>
		
		<!-- 加载中 -->
		<u-loading-page :loading="loading && recordList.length === 0"></u-loading-page>
	</view>
</template>

<script>
import { getAllBrowseRecordByUser } from '@/apis/browse_record_service'
import WaterFall from '@/components/water-fall/water-fall.vue'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
	mixins: [weixinNavBar],
	components: {
		WaterFall
	},
	data() {
		return {
			statusBarHeight: 0,
			scrollHeight: 0,
			recordList: [],
			page: 1,
			pageSize: 10,
			loading: false,
			refreshing: false,
			loadStatus: 'loadmore', // loadmore, loading, nomore
			loadingText: '加载中...',
			loadmoreText: '轻轻下拉',
			nomoreText: '没有更多了',
			waterfallKey: 0 // 用于强制重新渲染瀑布流组件
		}
	},
	
	computed: {
		/**
		 * 格式化浏览记录数据以适配瀑布流组件
		 */
		formattedRecordList() {
			// 确保数据去重
			const uniqueRecords = this.recordList.filter((item, index, self) => 
				index === self.findIndex(t => t.id === item.id)
			)
			
			console.log('格式化记录数据，原始数量:', this.recordList.length, '去重后数量:', uniqueRecords.length)
			
			return uniqueRecords.map(item => {
				// 🔧 修复视频类型判断：noteType 可能是数字 2、"2" 或字符串 "video"
				// 使用 parseInt 统一处理，如果是 "video" 则返回 NaN，需要特殊处理
				let notesType = 1; // 默认图文
				if (item.noteType === 'video') {
					notesType = 2; // 视频
				} else {
					const parsedType = parseInt(item.noteType, 10);
					if (!isNaN(parsedType)) {
						notesType = parsedType === 2 ? 2 : 1; // 2 表示视频，其他为图文
					}
				}
				
				return {
					id: item.id,
					coverPicture: item.noteCover || item.cover || item.image,
					title: item.title || item.content || '无标题',
					nickname: item.username || '用户',
					avatarUrl: item.avatar,
					notesType: notesType, // 1: 图文, 2: 视频
					notesLikeNum: item.likeCount || 0,
					isLike: false, // 浏览记录不显示点赞状态
					updateTime: this.formatTime(item.time),
					auditStatus: '1', // 浏览记录都是已审核通过的内容
					pinned: 0, // 浏览记录不显示置顶
					uid: item.uid,
					belongUserId: item.uid
				}
			})
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
		
		console.log('浏览记录页面 onLoad 触发')
		const systemInfo = uni.getSystemInfoSync()
		this.applyWeixinNavBar(systemInfo, 44)
		// 与订单页一致：用 windowHeight 计算滚动区高度，避免底部遮挡
		// #ifdef MP-WEIXIN
		this.scrollHeight = systemInfo.windowHeight
		// #endif
		// #ifndef MP-WEIXIN
		this.scrollHeight = systemInfo.windowHeight - (this.statusBarHeight || 0) - 44
		// #endif
		// 完全重置页面状态
		this.resetPageState()
		
		// 立即加载数据，不使用延迟
		this.loadBrowseRecords()
	},
	
	onShow() {
		console.log('浏览记录页面 onShow 触发')
		// 移除onShow中的重复加载逻辑，避免数据重复
		// 浏览记录数据相对稳定，不需要每次onShow都重新加载
	},
	
	methods: {
		/**
		 * 重置页面状态
		 */
		resetPageState() {
			console.log('重置页面状态')
			
			// 清空现有数据
			this.recordList = []
			this.page = 1
			this.loadStatus = 'loadmore'
			this.loading = false
			this.refreshing = false
			
			// 强制重新渲染瀑布流组件
			this.waterfallKey++
			
			// 清空瀑布流组件
			this.$nextTick(() => {
				if (this.$refs.waterfall) {
					this.$refs.waterfall.clear()
				}
			})
			
			console.log('页面状态重置完成，waterfallKey:', this.waterfallKey)
		},
		
		/**
		 * 返回上一页
		 */
		goBack() {
			uni.navigateBack()
		},
		
		/**
		 * 加载浏览记录
		 */
		async loadBrowseRecords() {
			console.log('=== 开始加载浏览记录 ===')
			console.log('请求参数 - 页码:', this.page, '每页数量:', this.pageSize)
			if (this.loading) {
				console.log('正在加载中，跳过')
				return
			}
			
			this.loading = true
			this.loadStatus = 'loading'
			
			try {
				console.log('调用API获取浏览记录')
				console.log('请求URL:', `/web/app/browseRecord/getAllBrowseRecordByUser/${this.page}/${this.pageSize}`)
				const res = await getAllBrowseRecordByUser({
					page: this.page,
					limit: this.pageSize
				})
				
				console.log('=== 浏览记录API响应 ===')
				console.log('响应状态码:', res?.code)
				console.log('响应数据:', res)
				
				if (res.code === 200) {
					const pageData = res.data || {}
					const records = pageData.records || []
					const total = pageData.total || 0
					const current = pageData.current || 1
					const size = pageData.size || 10
					
					console.log('获取到记录数量:', records.length, '总数:', total, '当前页:', current)
					
					if (this.page === 1) {
						// 第一页：直接替换数据，避免重复
						this.recordList = records
						console.log('第一页数据加载完成，记录数量:', this.recordList.length)
					} else {
						// 去重处理：只添加不存在的记录
						const existingIds = new Set(this.recordList.map(item => item.id))
						const newRecords = records.filter(item => !existingIds.has(item.id))
						
						if (newRecords.length > 0) {
							this.recordList = this.recordList.concat(newRecords)
							console.log('追加新记录数量:', newRecords.length, '总记录数量:', this.recordList.length)
							// 瀑布流组件会自动根据formattedRecordList的变化更新显示
						} else {
							console.log('没有新记录，跳过追加')
						}
					}
					
					// 更新加载状态
					if (records.length < this.pageSize || this.recordList.length >= total) {
						this.loadStatus = 'nomore'
					} else {
						this.loadStatus = 'loadmore'
						this.page++
					}
				} else {
					console.error('API返回错误:', res)
					// 只有在非下拉刷新时才显示错误提示
					if (!this.refreshing) {
						uni.showToast({
							title: res.message || '加载失败',
							icon: 'none'
						})
					}
					this.loadStatus = 'loadmore'
				}
			} catch (error) {
				console.error('加载浏览记录失败:', error)
				// 只有在非下拉刷新时才显示错误提示
				if (!this.refreshing) {
					uni.showToast({
						title: '网络错误',
						icon: 'none'
					})
				}
				this.loadStatus = 'loadmore'
			} finally {
				this.loading = false
				// 只有在非下拉刷新时才设置 refreshing 为 false
				if (!this.refreshing) {
					this.refreshing = false
				}
				console.log('加载完成, loading:', this.loading)
				
				// 移除第一页的瀑布流刷新逻辑，避免重复显示
				// 瀑布流组件会自动根据formattedRecordList的变化重新渲染
			}
		},
		
		/**
		 * 加载更多
		 */
		loadMore() {
			if (this.loadStatus === 'loadmore' && !this.loading) {
				this.loadBrowseRecords()
			}
		},
		
		/**
		 * 下拉刷新
		 */
		async onRefresh() {
			console.log('=== 开始浏览记录下拉刷新 ===')
			this.refreshing = true
			
			try {
				// 重置分页和状态
				this.page = 1
				this.loadStatus = 'loadmore'
				console.log('重置分页参数 - 当前页:', this.page, '加载状态:', this.loadStatus)
				
				// 清空现有数据
				this.recordList = []
				console.log('清空现有数据，记录数量:', this.recordList.length)
				
				// 清空瀑布流
				if (this.$refs.waterfall) {
					this.$refs.waterfall.clear()
				}
				
				// 强制重新渲染瀑布流组件
				this.waterfallKey++
				console.log('强制重新渲染瀑布流组件')
				
				// 重新加载数据
				console.log('开始调用loadBrowseRecords方法...')
				await this.loadBrowseRecords()
				
				console.log('=== 浏览记录下拉刷新完成 ===')
				console.log('最终记录数量:', this.recordList.length)
				
			} catch (error) {
				console.error('=== 浏览记录下拉刷新失败 ===')
				console.error('错误详情:', error)
				uni.showToast({
					title: '刷新失败',
					icon: 'none',
					duration: 2000
				})
			} finally {
				// 立即结束刷新状态
				this.refreshing = false
				console.log('刷新状态结束，refreshing:', this.refreshing)
			}
		},
		
		/**
		 * 瀑布流初始化完成回调
		 */
		onWaterfallInit() {
			console.log('瀑布流初始化完成，记录数量:', this.recordList.length)
		},
		
		/**
		 * 刷新瀑布流
		 */
		refreshWaterfall() {
			if (this.$refs.waterfall) {
				this.$refs.waterfall.refresh()
			}
		},
		
		/**
		 * 格式化时间
		 */
		formatTime(timestamp) {
			if (!timestamp) return ''
			
			const now = new Date()
			const time = new Date(timestamp)
			const diff = now - time
			
			// 小于1分钟
			if (diff < 60000) {
				return '刚刚'
			}
			
			// 小于1小时
			if (diff < 3600000) {
				return Math.floor(diff / 60000) + '分钟前'
			}
			
			// 小于1天
			if (diff < 86400000) {
				return Math.floor(diff / 3600000) + '小时前'
			}
			
			// 小于7天
			if (diff < 604800000) {
				return Math.floor(diff / 86400000) + '天前'
			}
			
			// 超过7天显示具体日期
			const month = time.getMonth() + 1
			const day = time.getDate()
			const hour = time.getHours()
			const minute = time.getMinutes()
			
			return `${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
		},
		
		/**
		 * 图片加载错误处理
		 */
		onImageError(e) {
			console.log('图片加载失败:', e)
		}
	}
}
</script>

<style scoped>
.browse-record-page {
	background-color: #f5f5f5;
	min-height: 100vh;
}

.header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 80rpx;
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

.content-scroll {
	/* 高度优先用 scrollHeight（onLoad 后），未设置时用下方兜底 */
	height: calc(100vh - 88rpx - var(--status-bar-height));
}

/* 瀑布流组件会处理样式，这里只保留必要的样式 */

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
</style>
