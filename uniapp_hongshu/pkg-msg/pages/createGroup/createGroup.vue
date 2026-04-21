<template>
	<view class="create-group-container">
		<!-- 状态栏背景（H5 需占位，微信端由系统栏展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航（H5 显示标题和返回，微信端由系统栏展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">创建群聊</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		<!-- 头部占位（微信端不预留） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		<view class="form-section">
			<view class="form-item">
				<text class="label">群名称</text>
				<input class="input" v-model="groupName" placeholder="请输入群名称" maxlength="20" />
			</view>
			<view class="form-item">
				<text class="label">群简介</text>
				<textarea class="textarea" v-model="groupDescription" placeholder="请输入群简介（选填）" maxlength="100" />
			</view>
		</view>
		
		<view class="member-section">
			<view class="section-title">选择成员（{{ selectedMembers.length }}）</view>
			<scroll-view scroll-y class="member-list">
				<!-- 空状态 -->
				<view v-if="memberList.length === 0" class="empty-state">
					<text class="empty-text">暂无关注用户</text>
					<text class="empty-tip">请先关注一些用户，然后才能创建群聊</text>
				</view>
				<!-- 成员列表 -->
				<view 
					class="member-item" 
					:class="{ 'member-item-disabled': isCurrentUser(member) }"
					v-for="(member, index) in memberList" 
					:key="member.id || member.uid || index"
					@click="toggleMember(member)"
				>
					<image class="member-avatar" :src="member.avatar || '/static/default.jpg'" mode="aspectFill"></image>
					<text class="member-name">{{ member.username || member.nickname || '未知用户' }}</text>
					<!-- 群主标签 -->
					<view v-if="isCurrentUser(member)" class="owner-tag">
						<text class="owner-text">群主</text>
					</view>
					<!-- 普通成员：选中状态：显示蓝色实心圆圈 -->
					<u-icon 
						v-else-if="isSelected(member)"
						name="checkmark-circle-fill" 
						color="#3d8af5"
						size="24"
					></u-icon>
					<!-- 未选中状态：显示灰色圆圈边框 -->
					<view v-else class="circle-unchecked"></view>
				</view>
			</scroll-view>
		</view>
		
		<view class="footer">
			<u-button 
				type="primary" 
				:disabled="!canCreate" 
				@click="createGroup"
				:loading="creating"
			>
				创建群聊
			</u-button>
		</view>
	</view>
</template>

<script>
	import { createGroup } from '@/apis/mesasage_apis.js'
	import { getFollowUser } from '@/apis/user_service.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				groupName: '',
				groupDescription: '',
				memberList: [],
				selectedMembers: [],
				creating: false,
				currentUserId: '' // 当前用户ID（群主）
			}
		},
		computed: {
			canCreate() {
				// 群主不需要选中，只要有群名称和至少选择一个其他成员即可
				const hasOtherMembers = this.selectedMembers.some(m => !this.isCurrentUser(m))
				return this.groupName.trim().length > 0 && hasOtherMembers && !this.creating
			}
		},
		onLoad() {
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
				}
			})
			// 获取当前用户ID
			const userInfo = uni.getStorageSync('userInfo')
			this.currentUserId = userInfo?.id || userInfo?.userId || ''
			this.loadFriendList()
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			async loadFriendList() {
				try {
					// 获取关注用户列表作为好友列表
					const res = await getFollowUser()
					console.log('获取关注用户列表响应:', res)
					
					if (res.code === 200 && res.data) {
						// 确保data是数组
						const userList = Array.isArray(res.data) ? res.data : []
						console.log('用户列表数据:', userList)
						
						// 映射字段 - 根据TrendUserVO的字段结构
						this.memberList = userList
							.filter(user => user && (user.uid || user.userId)) // 过滤掉无效数据
							.map(user => ({
								id: user.uid || user.userId || '',
								userId: user.uid || user.userId || '',
								uid: user.uid || user.userId || '',
								username: user.username || user.nickname || '未知用户',
								nickname: user.nickname || user.username || '未知用户',
								avatar: user.avatar || user.avatarUrl || '/static/default.jpg',
								avatarUrl: user.avatarUrl || user.avatar || '/static/default.jpg'
							}))
						
						console.log('处理后的成员列表:', this.memberList)
						
						// 将当前用户（群主）添加到成员列表（不默认选中，只显示群主标签）
						if (this.currentUserId) {
							const userInfo = uni.getStorageSync('userInfo')
							let currentUser = this.memberList.find(m => {
								const memberId = m.id || m.userId || m.uid
								return String(memberId) === String(this.currentUserId)
							})
							
							// 如果当前用户不在列表中，添加到列表开头
							if (!currentUser && userInfo) {
								currentUser = {
									id: userInfo.id || userInfo.userId || this.currentUserId,
									userId: userInfo.id || userInfo.userId || this.currentUserId,
									uid: userInfo.id || userInfo.userId || this.currentUserId,
									username: userInfo.nickname || userInfo.username || '我',
									nickname: userInfo.nickname || userInfo.username || '我',
									avatar: userInfo.avatarUrl || userInfo.avatar || '/static/default.jpg',
									avatarUrl: userInfo.avatarUrl || userInfo.avatar || '/static/default.jpg'
								}
								this.memberList.unshift(currentUser) // 添加到列表开头
							}
						}
						
						if (this.memberList.length === 0) {
							uni.showToast({
								title: '暂无关注用户',
								icon: 'none'
							})
						}
					} else {
						console.warn('API返回异常:', res)
						this.memberList = []
						if (res.code !== 200) {
							uni.showToast({
								title: res.message || '获取好友列表失败',
								icon: 'none'
							})
						}
					}
				} catch (e) {
					console.error('获取好友列表失败', e)
					uni.showToast({
						title: '获取好友列表失败',
						icon: 'none'
					})
					this.memberList = []
				}
			},
			toggleMember(member) {
				// 如果是当前用户（群主），不允许取消选择
				if (this.isCurrentUser(member)) {
					return
				}
				
				const index = this.selectedMembers.findIndex(m => 
					(m.id || m.userId || m.uid) === (member.id || member.userId || member.uid)
				)
				if (index > -1) {
					this.selectedMembers.splice(index, 1)
				} else {
					this.selectedMembers.push(member)
				}
			},
			isSelected(member) {
				return this.selectedMembers.some(m => 
					(m.id || m.userId || m.uid) === (member.id || member.userId || member.uid)
				)
			},
			isCurrentUser(member) {
				if (!this.currentUserId) return false
				const memberId = member.id || member.userId || member.uid
				return String(memberId) === String(this.currentUserId)
			},
			async createGroup() {
				if (!this.canCreate) return
				
				this.creating = true
				try {
					// 确保ID是字符串类型，并过滤掉空值
					// 注意：群主（当前用户）会自动包含在群聊中，不需要在 memberIds 中传递
					const memberIds = this.selectedMembers
						.filter(m => !this.isCurrentUser(m)) // 过滤掉群主
						.map(m => {
							const id = m.id || m.userId || m.uid
							return id ? String(id) : null
						})
						.filter(id => id && id.trim() !== '')
					
					console.log('创建群聊 - 选中的成员:', this.selectedMembers)
					console.log('创建群聊 - 成员ID列表（不含群主）:', memberIds)
					console.log('创建群聊 - 成员ID类型检查:', memberIds.map(id => typeof id))
					
					const res = await createGroup({
						groupName: this.groupName.trim(),
						groupDescription: this.groupDescription.trim(),
						memberIds: memberIds
					})
					
					console.log('创建群聊响应:', res)
					
					if (res.code === 200) {
						uni.showToast({
							title: '创建成功',
							icon: 'success'
						})
						
						// 延迟跳转，确保toast显示
						setTimeout(() => {
							// 先触发消息列表刷新事件
							uni.$emit('updateMessageList')
							// 然后返回，onShow 也会触发刷新，双重保障
							uni.navigateBack()
						}, 1500)
					} else {
						uni.showToast({
							title: res.message || '创建失败',
							icon: 'none'
						})
					}
				} catch (e) {
					console.error('创建群聊失败', e)
					uni.showToast({
						title: '创建失败',
						icon: 'none'
					})
				} finally {
					this.creating = false
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.create-group-container {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 120rpx;
	}
	
	.form-section {
		background-color: #fff;
		margin-top: 20rpx;
	}
	
	.form-item {
		display: flex;
		align-items: center;
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		&:last-child {
			border-bottom: none;
		}
	}
	
	.label {
		width: 150rpx;
		font-size: 32rpx;
		color: #333;
	}
	
	.input {
		flex: 1;
		font-size: 32rpx;
	}
	
	.textarea {
		flex: 1;
		font-size: 32rpx;
		min-height: 120rpx;
	}
	
	.member-section {
		margin-top: 20rpx;
		background-color: #fff;
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.section-title {
		padding: 30rpx;
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.member-list {
		flex: 1;
		height: 600rpx;
	}
	
	.member-item {
		display: flex;
		align-items: center;
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		&:last-child {
			border-bottom: none;
		}
		
		&	.member-item-disabled {
		opacity: 0.8;
		// 禁用点击效果
		pointer-events: none;
	}
	
	.owner-tag {
		padding: 4rpx 12rpx;
		background-color: #fff3e0;
		border-radius: 8rpx;
	}
	
	.owner-text {
		font-size: 24rpx;
		color: #ff9800;
	}
	}
	
	.member-avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		margin-right: 20rpx;
	}
	
	.member-name {
		flex: 1;
		font-size: 32rpx;
		color: #333;
	}
	
	.circle-unchecked {
		width: 48rpx;
		height: 48rpx;
		border: 2rpx solid #ccc;
		border-radius: 50%;
		background-color: transparent;
	}
	
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 30rpx;
	}
	
	.empty-text {
		font-size: 32rpx;
		color: #999;
		margin-bottom: 20rpx;
	}
	
	.empty-tip {
		font-size: 28rpx;
		color: #ccc;
		text-align: center;
	}
	
	.footer {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		padding: 20rpx 30rpx;
		background-color: #fff;
		border-top: 1rpx solid #f0f0f0;
	}

	/* H5 头部导航（与编辑资料等页一致） */
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 44px;
		padding: 0 15px;
		background-color: #fff;
		border-bottom: 1px solid #eee;
	}
	.header-left {
		width: 40px;
		display: flex;
		align-items: center;
		justify-content: flex-start;
	}
	.header-title {
		flex: 1;
		text-align: center;
		font-size: 18px;
		font-weight: 500;
		color: #333;
	}
	.header-right {
		width: 40px;
	}
</style>

