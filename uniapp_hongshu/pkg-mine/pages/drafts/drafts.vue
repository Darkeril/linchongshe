<template>
	<view class="drafts-page">
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
			<view class="header-title">本地草稿</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 警告提示 -->
		<view class="warning-banner">
			<text class="warning-text">草稿在应用卸载后会被删除，请及时发布</text>
		</view>
		
		<!-- 草稿列表 -->
		<scroll-view scroll-y="true" class="drafts-scroll">
			<!-- 草稿列表 -->
			<view v-if="draftsList.length > 0" class="drafts-list">
				<view 
					class="draft-item" 
					v-for="(draft, index) in draftsList" 
					:key="index"
					@click="goToDraftDetail(draft)"
				>
					<view class="draft-preview">
						<view class="draft-image" v-if="draft.coverPicture">
							<image :src="draft.coverPicture" class="preview-img" mode="aspectFill"></image>
						</view>
						<view class="draft-icon" v-else>
							<u-icon :name="getDraftIcon(draft.draftType)" size="40" color="#999"></u-icon>
						</view>
					</view>
					
					<view class="draft-info">
						<view class="draft-title">{{ draft.title || '无标题' }}</view>
						<view class="draft-content">{{ getPlainText(draft.content) || '暂无内容' }}</view>
						<view class="draft-meta">
							<text class="draft-type">{{ getDraftTypeText(draft.draftType) }}</text>
							<text class="draft-time">{{ formatTime(draft.updateTime) }}</text>
						</view>
					</view>
					
					<view class="draft-actions">
						<view class="action-btn edit-btn" @click.stop="editDraft(draft)">
							<u-icon name="edit-pen" size="18" color="#1890ff"></u-icon>
						</view>
						<view class="action-btn delete-btn" @click.stop="deleteDraft(index)">
							<u-icon name="trash" size="18" color="#ff4757"></u-icon>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-else class="empty-drafts">
				<view class="empty-illustration">
					<view class="draft-icon-large">
						<u-icon name="folder" size="60" color="#ccc"></u-icon>
					</view>
				</view>
				<view class="empty-text">暂无草稿</view>
				<view class="empty-desc">快去创作一些内容吧</view>
				<!-- <view class="create-btn" @click="goToCreate">
					<u-icon name="plus" size="20" color="#fff"></u-icon>
					<text class="create-text">创建草稿</text>
				</view> -->
			</view>
		</scroll-view>
	</view>
</template>

<script>
import weixinNavBar from '@/utils/weixinNavBar.js'
export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			draftsList: [],
			loading: false
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
		
		this.applyWeixinNavBar(uni.getSystemInfoSync(), 44)
		this.loadDrafts()
	},
	
	onShow() {
		// 每次显示页面时刷新草稿列表
		this.loadDrafts()
	},
	
	methods: {
		/**
		 * 返回上一页
		 */
		goBack() {
			uni.navigateBack()
		},
		
		/**
		 * 加载草稿列表
		 */
		loadDrafts() {
			this.loading = true
			try {
				// 从本地存储读取草稿数据
				const drafts = uni.getStorageSync('drafts') || []
				this.draftsList = drafts
				console.log('从本地存储加载草稿列表:', this.draftsList.length)
			} catch (error) {
				console.error('加载草稿失败:', error)
				this.draftsList = []
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		/**
		 * 跳转到草稿详情
		 */
		goToDraftDetail(draft) {
			uni.navigateTo({
				url: `/pkg-publish/pages/publishNotes/publishNotes?draftId=${draft.id}&mode=edit`
			})
		},
		
		/**
		 * 编辑草稿
		 */
		editDraft(draft) {
			uni.navigateTo({
				url: `/pkg-publish/pages/publishNotes/publishNotes?draftId=${draft.id}&mode=edit`
			})
		},
		
		/**
		 * 删除草稿
		 */
		deleteDraft(index) {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这个草稿吗？删除后无法恢复',
				success: (res) => {
					if (res.confirm) {
						try {
							// 从本地存储删除草稿
							const drafts = uni.getStorageSync('drafts') || []
							drafts.splice(index, 1)
							uni.setStorageSync('drafts', drafts)
							
							// 更新页面数据
							this.draftsList.splice(index, 1)
							
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							})
						} catch (error) {
							console.error('删除草稿失败:', error)
							uni.showToast({
								title: '删除失败',
								icon: 'none'
							})
						}
					}
				}
			})
		},
		
		/**
		 * 创建新草稿
		 */
		goToCreate() {
			uni.navigateTo({
				url: '/pkg-publish/pages/publishNotes/publishNotes?mode=create'
			})
		},
		
		/**
		 * 获取纯文本内容（去除HTML标签）
		 */
		getPlainText(htmlContent) {
			if (!htmlContent) return ''
			
			// 使用正则表达式去除HTML标签
			const plainText = htmlContent.replace(/<[^>]*>/g, '').trim()
			
			// 限制显示长度
			return plainText.length > 50 ? plainText.substring(0, 50) + '...' : plainText
		},
		
		/**
		 * 获取草稿类型图标
		 */
		getDraftIcon(type) {
			const iconMap = {
				'notes': 'file-text',
				'idle': 'shopping-bag',
				'video': 'play-circle',
				'image': 'image'
			}
			return iconMap[type] || 'file-text'
		},
		
		/**
		 * 获取草稿类型文本
		 */
		getDraftTypeText(type) {
			const typeMap = {
				'notes': '笔记',
				'idle': '商品',
				'video': '视频',
				'image': '图片'
			}
			return typeMap[type] || '笔记'
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
			return time.toLocaleDateString()
		}
	}
}
</script>

<style scoped>
.drafts-page {
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

.warning-banner {
	background-color: #fff3cd;
	padding: 20rpx 30rpx;
	border-bottom: 1rpx solid #ffeaa7;
}

.warning-text {
	font-size: 26rpx;
	color: #856404;
	line-height: 1.4;
}

.drafts-scroll {
	height: calc(100vh - 88rpx - 80rpx - var(--status-bar-height));
}

.drafts-list {
	padding: 20rpx;
}

.draft-item {
	display: flex;
	align-items: center;
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.draft-preview {
	width: 120rpx;
	height: 120rpx;
	margin-right: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.draft-image {
	width: 100%;
	height: 100%;
}

.preview-img {
	width: 100%;
	height: 100%;
	border-radius: 10rpx;
}

.draft-icon {
	width: 100%;
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f5f5;
	border-radius: 10rpx;
}

.draft-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	margin-right: 20rpx;
}

.draft-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 10rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.draft-content {
	font-size: 24rpx;
	color: #666;
	margin-bottom: 15rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	line-height: 1.4;
}

.draft-meta {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.draft-type {
	font-size: 22rpx;
	color: #1890ff;
	background-color: #e6f7ff;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

.draft-time {
	font-size: 22rpx;
	color: #999;
}

.draft-actions {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.action-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background-color: #f5f5f5;
}

.edit-btn {
	background-color: #e6f7ff;
}

.delete-btn {
	background-color: #fff2f0;
}

.empty-drafts {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.empty-illustration {
	margin-bottom: 40rpx;
}

.draft-icon-large {
	width: 120rpx;
	height: 120rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f5f5;
	border-radius: 50%;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
	margin-bottom: 20rpx;
}

.empty-desc {
	font-size: 28rpx;
	color: #ccc;
	margin-bottom: 40rpx;
}

.create-btn {
	display: flex;
	align-items: center;
	background-color: #1890ff;
	color: #fff;
	padding: 20rpx 40rpx;
	border-radius: 50rpx;
	font-size: 28rpx;
}

.create-text {
	margin-left: 10rpx;
}
</style>
