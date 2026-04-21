<template>
	<view class="group-chat-list-container">
		<!-- 状态栏占位（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" class="status-bar-placeholder"></view>
		<!-- #endif -->
		<!-- 自定义导航栏（微信端系统栏已显示「我的群聊」，此处不重复） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="nav-bar" :style="{top: navBarTop}">
			<view class="nav-back" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="nav-title">我的群聊</view>
			<view class="nav-right"></view>
		</view>
		<!-- 导航栏占位，避免内容被遮挡 -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 内容区域 -->
		<scroll-view scroll-y class="content-scroll" :style="{height: scrollViewHeight + 'px'}">
			<!-- 我创建的群聊 -->
			<view class="section">
				<view class="section-title">我创建的群聊</view>
				<view class="group-list" v-if="createdGroups.length > 0">
					<view 
						class="group-item" 
						v-for="group in createdGroups" 
						:key="group.id"
						@click="goToGroupChat(group.id)"
						@longpress="goToGroupDetail(group.id)"
					>
						<image class="group-avatar" :src="group.groupAvatar || '/static/default-group.jpg'" mode="aspectFill"></image>
						<view class="group-info">
							<view class="group-name">{{ group.groupName }}</view>
							<view class="group-meta">
								<text class="member-count">{{ group.memberCount || 0 }}人</text>
								<text class="group-role owner">群主</text>
							</view>
						</view>
						<view class="group-arrow">
							<u-icon name="arrow-right" size="18" color="#999"></u-icon>
						</view>
					</view>
				</view>
				<view class="empty-section" v-else>
					<text class="empty-section-text">暂无创建的群聊</text>
				</view>
			</view>
			
			<!-- 我加入的群聊 -->
			<view class="section">
				<view class="section-title">我加入的群聊</view>
				<view class="group-list" v-if="joinedGroups.length > 0">
					<view 
						class="group-item" 
						v-for="group in joinedGroups" 
						:key="group.id"
						@click="goToGroupChat(group.id)"
						@longpress="goToGroupDetail(group.id)"
					>
						<image class="group-avatar" :src="group.groupAvatar || '/static/default-group.jpg'" mode="aspectFill"></image>
						<view class="group-info">
							<view class="group-name">{{ group.groupName }}</view>
							<view class="group-meta">
								<text class="member-count">{{ group.memberCount || 0 }}人</text>
								<text v-if="group.unreadCount > 0" class="unread-badge">{{ group.unreadCount > 99 ? '99+' : group.unreadCount }}</text>
							</view>
						</view>
						<view class="group-arrow">
							<u-icon name="arrow-right" size="18" color="#999"></u-icon>
						</view>
					</view>
				</view>
				<view class="empty-section" v-else>
					<text class="empty-section-text">暂无加入的群聊</text>
				</view>
			</view>
			
			<!-- 加载中 -->
			<view class="loading-state" v-if="loading">
				<u-loading-icon mode="spinner"></u-loading-icon>
				<text class="loading-text">加载中...</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getMyGroupList } from '@/apis/mesasage_apis.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				createdGroups: [], // 我创建的群聊
				joinedGroups: [], // 我加入的群聊
				loading: false,
				scrollViewHeight: 600,
				statusBarHeight: 0
			}
		},
		onLoad() {
			// 获取状态栏高度
			const systemInfo = uni.getSystemInfoSync()
			this.applyWeixinNavBar(systemInfo, 44)
			const windowHeight = systemInfo.windowHeight || 600
			const navBarHeight = 44
			// #ifdef MP-WEIXIN
			this.scrollViewHeight = windowHeight
			// #endif
			// #ifndef MP-WEIXIN
			this.scrollViewHeight = windowHeight - (this.statusBarHeight || 0) - navBarHeight
			// #endif
			
			this.loadGroupList()
			
			// 监听群聊更新事件（使用箭头函数确保 this 指向正确）
			const updateHandler = () => {
				console.log('收到群聊更新事件，刷新列表')
				this.loadGroupList()
			}
			uni.$on('updateGroupChatList', updateHandler)
			// 保存处理器引用，方便卸载时移除
			this._updateGroupChatListHandler = updateHandler
		},
		onShow() {
			// 页面显示时刷新列表（确保从其他页面返回时也能刷新）
			console.log('群聊列表页面显示，刷新列表')
			this.loadGroupList()
		},
		onUnload() {
			// 页面卸载时移除事件监听
			if (this._updateGroupChatListHandler) {
				uni.$off('updateGroupChatList', this._updateGroupChatListHandler)
			} else {
				uni.$off('updateGroupChatList')
			}
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			async loadGroupList() {
				this.loading = true
				try {
					const res = await getMyGroupList()
					if (res.code === 200 && res.data) {
						const groups = Array.isArray(res.data) ? res.data : []
						const currentUserId = uni.getStorageSync('userInfo')?.id || ''
						
						// 区分创建的群聊和加入的群聊
						// 使用 String 比较确保类型一致
						this.createdGroups = groups.filter(group => {
							return String(group.ownerId) === String(currentUserId)
						})
						this.joinedGroups = groups.filter(group => {
							return String(group.ownerId) !== String(currentUserId)
						})
						
						console.log('创建的群聊:', this.createdGroups.length, '加入的群聊:', this.joinedGroups.length)
					} else {
						uni.showToast({
							title: res.message || '获取群聊列表失败',
							icon: 'none'
						})
					}
				} catch (e) {
					console.error('获取群聊列表失败', e)
					uni.showToast({
						title: '获取群聊列表失败',
						icon: 'none'
					})
				} finally {
					this.loading = false
				}
			},
			goToGroupChat(groupId) {
				// 跳转到群聊聊天页面
				uni.navigateTo({
					url: `/pkg-msg/pages/chat/chat?chatType=1&groupId=${groupId}`
				})
			},
			goToGroupDetail(groupId) {
				// 长按跳转到群聊详情页面
				uni.navigateTo({
					url: `/pkg-msg/pages/groupDetail/groupDetail?groupId=${groupId}`
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.group-chat-list-container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
	}
	
	.status-bar-placeholder {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		width: 100%;
		background-color: #ffffff;
		z-index: 10000;
	}
	
	.nav-bar {
		position: fixed;
		left: 0;
		right: 0;
		height: 44px;
		background-color: #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
		border-bottom: 1rpx solid #f0f0f0;
		z-index: 9999;
	}
	
	.nav-back {
		position: absolute;
		left: 20rpx;
		width: 60rpx;
		height: 44px;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.nav-title {
		font-size: 36rpx;
		font-weight: 600;
		color: #2b2b2b;
	}
	
	.nav-right {
		position: absolute;
		right: 20rpx;
		width: 60rpx;
		height: 44px;
	}
	
	.content-scroll {
		/* margin-top 已通过占位 view 实现 */
	}
	
	.section {
		background-color: #ffffff;
		margin-bottom: 20rpx;
	}
	
	.section-title {
		padding: 30rpx 40rpx 20rpx;
		font-size: 32rpx;
		font-weight: 600;
		color: #2b2b2b;
	}
	
	.group-list {
		padding: 0 40rpx 20rpx;
	}
	
	.group-item {
		display: flex;
		align-items: center;
		padding: 30rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.group-item:last-child {
		border-bottom: none;
	}
	
	.group-avatar {
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		margin-right: 20rpx;
	}
	
	.group-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}
	
	.group-name {
		font-size: 32rpx;
		color: #2b2b2b;
		margin-bottom: 10rpx;
	}
	
	.group-meta {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}
	
	.member-count {
		font-size: 26rpx;
		color: #999;
	}
	
	.group-role {
		font-size: 24rpx;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		background-color: #e6f2ff;
		color: #3d8af5;
	}
	
	.unread-badge {
		font-size: 24rpx;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		background-color: #ff4444;
		color: #ffffff;
	}
	
	.group-arrow {
		margin-left: 20rpx;
	}
	
	.empty-section {
		padding: 40rpx;
		text-align: center;
	}
	
	.empty-section-text {
		font-size: 28rpx;
		color: #999;
	}
	
	.loading-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 200rpx 0;
	}
	
	.loading-text {
		margin-top: 20rpx;
		font-size: 28rpx;
		color: #999;
	}
</style>

