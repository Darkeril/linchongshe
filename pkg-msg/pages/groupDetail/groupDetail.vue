<template>
	<view class="group-detail-container">
		<!-- 自定义导航栏（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height:statusBarHeight+'px'}" style="position: fixed;z-index: 100;width: 100%;background-color: #ffffff;"></view>
		<!-- #endif -->
		<view :style="{top: navBarTop}"
			style="display: flex;height: 44px;align-items: center;position: fixed;z-index: 100;width: 100%;background-color: #ffffff;border-bottom: 1rpx solid #f0f0f0;">
			<!-- #ifndef MP-WEIXIN -->
			<u-icon name="arrow-left" size="21" style="margin-left: 20rpx;" @click="backToChat"></u-icon>
			<!-- #endif -->
			<view style="flex: 1;text-align: center;font-size: 33rpx;color: #2b2b2b;font-weight: bold;">
				群聊详情
			</view>
			<view style="margin-right: 20rpx;width: 60rpx;display: flex;justify-content: flex-end;">
				<u-icon 
					v-if="isOwner && !isEditMode" 
					name="edit-pen" 
					color="#3d8af5" 
					size="22" 
					@click="enterEditMode"
				></u-icon>
				<text 
					v-if="isOwner && isEditMode" 
					style="font-size: 30rpx;color: #3d8af5;"
					@click="exitEditMode"
				>
					完成
				</text>
			</view>
		</view>
		<view :style="{height: navPlaceholderHeight}"></view>
		
		<!-- 群信息区域 -->
		<view class="group-info-section">
		<view class="avatar-wrapper" @click="isEditMode ? chooseAvatar() : null">
			<image class="group-avatar" :src="(isEditMode ? editForm.groupAvatar : groupInfo.groupAvatar) || '/static/default-group.jpg'" mode="aspectFill"></image>
			<view v-if="isEditMode" class="camera-icon-wrapper">
				<image class="camera-icon" src="@/static/image/camera.png" mode="widthFix"></image>
			</view>
		</view>
			<!-- 编辑模式：显示输入框 -->
			<view v-if="isEditMode" class="group-name-wrapper-edit">
				<input 
					class="group-name-input" 
					v-model="editForm.groupName" 
					placeholder="请输入群名称" 
					maxlength="20"
					:focus="false"
				/>
			</view>
			<!-- 非编辑模式：显示文本 -->
			<view v-else class="group-name-wrapper">
				<text class="group-name">{{ groupInfo.groupName || '群聊' }}</text>
			</view>
			<!-- 编辑模式：显示文本域 -->
			<view v-if="isEditMode" class="group-description-wrapper-edit">
				<textarea 
					class="group-description-textarea" 
					v-model="editForm.groupDescription" 
					:placeholder="editForm.groupDescription ? '' : '请输入群简介（选填）'" 
					maxlength="100"
					auto-height
				></textarea>
			</view>
			<!-- 非编辑模式：显示文本 -->
			<view v-else class="group-description-wrapper">
				<text v-if="groupInfo.groupDescription" class="group-description">{{ groupInfo.groupDescription }}</text>
				<text v-else class="group-description-placeholder"></text>
			</view>
		</view>
		
		<!-- 群成员区域 -->
		<view class="member-section">
			<view class="section-header">
				<text class="section-title">群成员（{{ memberList.length }}）</text>
			</view>
			<scroll-view scroll-y class="member-list">
				<!-- 加载中 -->
				<view v-if="loading" class="loading">
					<u-loading-icon mode="circle"></u-loading-icon>
				</view>
				<!-- 成员列表 -->
				<view 
					class="member-item" 
					v-for="(member, index) in memberList" 
					:key="getMemberKey(member, index)"
				>
					<view class="member-left" @click="goToUserProfile(getMemberId(member))">
						<image class="member-avatar" :src="(member && member.avatar) || '/static/default.jpg'" mode="aspectFill"></image>
						<view class="member-info">
							<text class="member-name">{{ (member && (member.username || member.nickname)) || '未知用户' }}</text>
							<text v-if="member && member.groupNickname" class="member-nickname">{{ member.groupNickname }}</text>
						</view>
					</view>
					<view class="member-right">
						<view class="member-role">
							<text v-if="member && member.role === 0" class="role-tag owner">群主</text>
							<text v-else-if="member && member.role === 1" class="role-tag admin">管理员</text>
						</view>
						<!-- 移除按钮（仅编辑模式下，群主可见，且不能移除自己） -->
						<view 
							v-if="isEditMode && isOwner && member && getMemberId(member) !== currentUserId && member.role !== 0"
							class="remove-btn"
							@click.stop="showRemoveMember(member)"
						>
							<view class="remove-icon-circle">
								<view class="remove-icon-line"></view>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
		
		<!-- 操作按钮区域 -->
		<view class="action-section">
			<u-button 
				v-if="isOwner" 
				type="error" 
				plain
				@click="dissolveGroup"
				:loading="dissolving"
			>
				解散群聊
			</u-button>
			<u-button 
				v-else 
				type="error" 
				plain
				@click="quitGroup"
				:loading="quitting"
			>
				退出群聊
			</u-button>
		</view>
		
	</view>
</template>

<script>
	import { getGroupDetail, getGroupMemberList, quitGroup, dissolveGroup, updateGroupInfo, removeMember } from '@/apis/mesasage_apis.js'
	import { baseUrl } from '@/.env.js'
	
	import weixinNavBar from '@/utils/weixinNavBar.js'
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				groupChatId: '',
				groupInfo: {},
				memberList: [],
				loading: false,
				dissolving: false,
				quitting: false,
				currentUserId: '',
				// 编辑模式
				isEditMode: false,
				// 编辑相关
				editForm: {
					groupName: '',
					groupAvatar: '',
					groupDescription: ''
				},
				saving: false,
				uploading: false
			}
		},
		computed: {
			isOwner() {
				// 确保类型一致进行比较
				const ownerId = String(this.groupInfo.ownerId || '')
				const currentId = String(this.currentUserId || '')
				return ownerId === currentId && ownerId !== ''
			}
		},
		onLoad(options) {
			// 获取状态栏高度
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
				}
			})
			
			if (options.groupId) {
				this.groupChatId = options.groupId
				this.currentUserId = uni.getStorageSync('userInfo')?.id || ''
				this.loadGroupDetail()
				this.loadMemberList()
			} else {
				uni.showToast({
					title: '群聊ID不存在',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			}
		},
		methods: {
			async loadGroupDetail() {
				try {
					const res = await getGroupDetail(this.groupChatId)
					if (res.code === 200 && res.data) {
						this.groupInfo = res.data
					} else {
						uni.showToast({
							title: res.message || '获取群信息失败',
							icon: 'none'
						})
					}
				} catch (e) {
					console.error('获取群信息失败', e)
					uni.showToast({
						title: '获取群信息失败',
						icon: 'none'
					})
				}
			},
			async loadMemberList() {
				this.loading = true
				try {
					const res = await getGroupMemberList(this.groupChatId)
					if (res.code === 200 && res.data) {
						const newList = Array.isArray(res.data) ? res.data : []
						// 直接赋值新数组，确保 Vue 能检测到变化
						this.memberList = [...newList]
					} else {
						uni.showToast({
							title: res.message || '获取成员列表失败',
							icon: 'none'
						})
					}
				} catch (e) {
					console.error('获取成员列表失败', e)
					uni.showToast({
						title: '获取成员列表失败',
						icon: 'none'
					})
				} finally {
					this.loading = false
				}
			},
			// 返回聊天页面
			backToChat() {
				uni.navigateBack()
			},
			// 进入编辑模式
			enterEditMode() {
				// 将当前值复制到编辑表单
				this.editForm = {
					groupName: this.groupInfo.groupName || '',
					groupAvatar: this.groupInfo.groupAvatar || '',
					groupDescription: this.groupInfo.groupDescription || ''
				}
				this.isEditMode = true
			},
			// 退出编辑模式并保存
			async exitEditMode() {
				// 检查是否有修改
				const hasChanged = 
					this.editForm.groupName !== (this.groupInfo.groupName || '') ||
					this.editForm.groupAvatar !== (this.groupInfo.groupAvatar || '') ||
					this.editForm.groupDescription !== (this.groupInfo.groupDescription || '')
				
				if (hasChanged) {
					// 有修改，保存
					await this.saveGroupInfo()
				} else {
					// 无修改，直接退出
					this.isEditMode = false
				}
			},
			// 获取成员ID（兼容多种字段名）
			getMemberId(member) {
				if (!member) return null
				return member.userId || member.user_id || member.id || null
			},
			// 获取成员key（用于v-for的key）
			getMemberKey(member, index) {
				if (!member) return `member-${index}`
				const id = this.getMemberId(member)
				return `member-${id || index}`
			},
			goToUserProfile(userId) {
				if (!userId) return
				const currentUserId = uni.getStorageSync('userInfo')?.id || ''
				// 确保类型一致进行比较
				const userIdStr = String(userId)
				const currentUserIdStr = String(currentUserId)
				if (userIdStr === currentUserIdStr) {
					// 点击自己的头像，跳转到自己的主页
					uni.navigateTo({
						url: '/pkg-main/pages/mine/mine'
					})
				} else {
					// 点击其他人的头像，跳转到对方主页
					uni.navigateTo({
						url: `/pkg-mine/pages/mine/otherMine?userId=${userIdStr}`
					})
				}
			},
			// 选择头像
			chooseAvatar() {
				uni.chooseImage({
					count: 1,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						this.uploadAvatar(res.tempFilePaths[0])
					}
				})
			},
			// 上传头像
			async uploadAvatar(filePath) {
				this.uploading = true
				try {
					const uploadRes = await uni.uploadFile({
						url: baseUrl + '/web/oss/upload',
						filePath: filePath,
						name: 'file',
						header: {
							'accessToken': uni.getStorageSync('uniapp_token') || ''
						}
					})
					
					const data = JSON.parse(uploadRes.data)
					if (data.code === 200) {
						this.editForm.groupAvatar = data.data
					} else {
						uni.showToast({
							title: data.message || '上传失败',
							icon: 'none'
						})
					}
				} catch (e) {
					console.error('上传头像失败', e)
					uni.showToast({
						title: '上传失败',
						icon: 'none'
					})
				} finally {
					this.uploading = false
				}
			},
			// 保存群信息
			async saveGroupInfo() {
				if (!this.editForm.groupName.trim()) {
					uni.showToast({
						title: '请输入群名称',
						icon: 'none'
					})
					return
				}
				
				this.saving = true
				try {
					const params = {
						groupChatId: this.groupChatId,
						groupName: this.editForm.groupName.trim(),
						groupAvatar: this.editForm.groupAvatar || '',
						groupDescription: this.editForm.groupDescription.trim() || ''
					}
					
				const result = await updateGroupInfo(params)
				if (result.code === 200) {
					// 立即更新本地数据，确保界面立即刷新
					const newGroupName = this.editForm.groupName.trim()
					const newGroupAvatar = this.editForm.groupAvatar || ''
					const newGroupDescription = this.editForm.groupDescription.trim() || ''
					
					// 使用 $set 确保 Vue 响应式更新
					this.$set(this.groupInfo, 'groupName', newGroupName)
					this.$set(this.groupInfo, 'groupAvatar', newGroupAvatar)
					this.$set(this.groupInfo, 'groupDescription', newGroupDescription)
					
					// 退出编辑模式
					this.isEditMode = false
					
					// 使用 $nextTick 确保 DOM 更新后再显示提示
					this.$nextTick(() => {
						uni.showToast({
							title: '保存成功',
							icon: 'success',
							duration: 1500
						})
					})
					
					// 异步重新加载群信息（从后端获取最新数据，但不阻塞界面更新）
					this.loadGroupDetail().catch(e => {
						console.error('重新加载群信息失败', e)
					})
					
					// 触发消息列表更新事件，让聊天页面也能看到更新
					uni.$emit('updateGroupInfo', {
						groupChatId: this.groupChatId,
						groupName: newGroupName,
						groupAvatar: newGroupAvatar
					})
				} else {
						uni.showToast({
							title: result.message || '保存失败',
							icon: 'none',
							duration: 2000
						})
					}
				} catch (e) {
					console.error('保存群信息失败', e)
					uni.showToast({
						title: '保存失败',
						icon: 'none',
						duration: 2000
					})
				} finally {
					this.saving = false
				}
			},
			// 显示移除成员确认
			showRemoveMember(member) {
				if (!member) return
				const memberId = this.getMemberId(member)
				if (!memberId) {
					uni.showToast({
						title: '成员信息错误',
						icon: 'none'
					})
					return
				}
				uni.showModal({
					title: '确认移除',
					content: `确定要将"${member.username || member.nickname || '该成员'}"移出群聊吗？`,
					success: async (res) => {
						if (res.confirm) {
							await this.removeMember(memberId)
						}
					}
				})
			},
			// 移除成员
			async removeMember(userId) {
				try {
					const result = await removeMember(this.groupChatId, userId)
					if (result.code === 200) {
						// 立即从列表中移除该成员
						const filteredList = this.memberList.filter(m => {
							if (!m) return false
							// 保留不匹配的成员（兼容多种可能的字段名）
							const memberId = this.getMemberId(m)
							return memberId && String(memberId) !== String(userId)
						})
						
						// 直接替换整个数组，创建新的数组引用
						this.$set(this, 'memberList', [...filteredList])
						
						// 更新群信息中的成员数（减1）
						if (this.groupInfo.memberCount && this.groupInfo.memberCount > 0) {
							this.groupInfo.memberCount = this.groupInfo.memberCount - 1
						}
						
						// 显示小提示
						uni.showToast({
							title: '已移除',
							icon: 'success',
							duration: 1500
						})
						
						// 不重新加载成员列表，避免后端延迟导致成员重新出现
						// 只在后台静默刷新群信息（更新成员数等）
						setTimeout(async () => {
							await this.loadGroupDetail()
						}, 500)
					} else {
						uni.showToast({
							title: result.message || '移除失败',
							icon: 'none',
							duration: 2000
						})
					}
				} catch (e) {
					console.error('移除成员失败', e)
					uni.showToast({
						title: '移除失败',
						icon: 'none',
						duration: 2000
					})
				}
			},
			async quitGroup() {
				uni.showModal({
					title: '确认退出',
					content: '确定要退出该群聊吗？',
					success: async (res) => {
						if (res.confirm) {
							this.quitting = true
							try {
							const result = await quitGroup(this.groupChatId)
							if (result.code === 200) {
								// 触发事件，通知群聊列表页面刷新
								console.log('退出群聊成功，触发更新事件')
								uni.$emit('updateGroupChatList')
								// 也触发消息列表更新
								uni.$emit('updateMessageList')
								uni.showToast({
									title: '已退出群聊',
									icon: 'success',
									duration: 1000
								})
								// 延迟返回，确保事件已触发
								setTimeout(() => {
									// 获取页面栈，判断是否需要返回到群聊列表
									const pages = getCurrentPages()
									console.log('当前页面栈:', pages.map(p => p.route))
									const hasGroupChatList = pages.some(page => page.route && page.route.includes('groupChatList'))
									
									if (hasGroupChatList) {
										// 如果页面栈中有群聊列表，返回到群聊列表
										console.log('返回到群聊列表页面')
										uni.navigateBack({
											delta: 1
										})
									} else {
										// 否则返回两层，回到消息列表
										console.log('返回到消息列表页面')
										uni.navigateBack({
											delta: 2
										})
									}
								}, 800)
								} else {
									uni.showToast({
										title: result.message || '退出失败',
										icon: 'none'
									})
								}
							} catch (e) {
								console.error('退出群聊失败', e)
								uni.showToast({
									title: '退出失败',
									icon: 'none'
								})
							} finally {
								this.quitting = false
							}
						}
					}
				})
			},
			async dissolveGroup() {
				uni.showModal({
					title: '确认解散',
					content: '确定要解散该群聊吗？解散后所有成员将被移除。',
					success: async (res) => {
						if (res.confirm) {
							this.dissolving = true
							try {
							const result = await dissolveGroup(this.groupChatId)
							if (result.code === 200) {
								// 触发事件，通知群聊列表页面刷新
								uni.$emit('updateGroupChatList')
								// 也触发消息列表更新
								uni.$emit('updateMessageList')
								uni.showToast({
									title: '群聊已解散',
									icon: 'success',
									duration: 1500
								})
								// 延迟跳转，确保事件已触发
								setTimeout(() => {
									// 直接跳转到消息列表页面
									uni.reLaunch({
										url: '/pkg-main/pages/message/message'
									})
								}, 1000)
								} else {
									uni.showToast({
										title: result.message || '解散失败',
										icon: 'none'
									})
									this.dissolving = false
								}
							} catch (e) {
								console.error('解散群聊失败', e)
								uni.showToast({
									title: '解散失败',
									icon: 'none'
								})
								this.dissolving = false
							}
						}
					}
				})
			}
		}
	}
</script>

<style scoped>
	.group-detail-container {
		min-height: 10vh;
		background-color: #f5f5f5;
		/* padding-bottom: 20rpx; */
	}
	
	/* 群信息区域 */
	.group-info-section {
		background-color: #ffffff;
		padding: 40rpx 40rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-top: 10rpx;
		margin-bottom: 20rpx;
	}
	
	.avatar-wrapper {
		position: relative;
		margin-bottom: 30rpx;
	}
	
	.group-avatar {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
	}
	
	.camera-icon-wrapper {
		position: absolute;
		bottom: 5rpx;
		right: 5rpx;
		width: 50rpx;
		height: 50rpx;
		background-color: #000000;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 5rpx;
	}
	
	.camera-icon {
		width: 40rpx;
		height: auto;
	}
	
	.group-name-wrapper {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 20rpx;
	}
	
	.group-name {
		font-size: 36rpx;
		font-weight: bold;
		color: #2b2b2b;
	}
	
	.group-name-wrapper-edit {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 20rpx;
		padding: 0 40rpx;
		width: 100%;
	}
	
	.group-name-input {
		width: 100%;
		font-size: 36rpx;
		font-weight: bold;
		color: #2b2b2b;
		text-align: center;
		padding: 10rpx 0;
		border: none;
		border-bottom: 1rpx solid #e0e0e0;
		background-color: transparent;
	}
	
	.group-description-wrapper {
		display: flex;
		align-items: center;
		justify-content: center;
		max-width: 500rpx;
	}
	
	.group-description {
		font-size: 28rpx;
		color: #999;
		text-align: center;
		line-height: 1.6;
	}
	
	.group-description-placeholder {
		font-size: 28rpx;
		color: #ccc;
		text-align: center;
		line-height: 1.6;
	}
	
	.group-description-wrapper-edit {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0 40rpx;
		margin-top: 10rpx;
		width: 100%;
		max-width: 500rpx;
		margin-left: auto;
		margin-right: auto;
	}
	
	.group-description-textarea {
		width: 100%;
		min-height: 80rpx;
		font-size: 28rpx;
		color: #999;
		text-align: center;
		padding: 10rpx 0;
		border: none;
		border-bottom: 1rpx solid #e0e0e0;
		background-color: transparent;
		line-height: 1.6;
		word-break: break-all;
	}
	
	/* 成员区域 */
	.member-section {
		background-color: #ffffff;
		/* margin-bottom: 10rpx; */
	}
	
	.section-header {
		padding: 30rpx 40rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #2b2b2b;
	}
	
	.member-list {
		max-height: 820rpx;
	}
	
	.member-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx 40rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.member-item:last-child {
		border-bottom: none;
	}
	
	.member-left {
		display: flex;
		align-items: center;
		flex: 1;
	}
	
	.member-avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		margin-right: 20rpx;
	}
	
	.member-info {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.member-name {
		font-size: 30rpx;
		color: #2b2b2b;
		margin-bottom: 8rpx;
	}
	
	.member-nickname {
		font-size: 24rpx;
		color: #999;
	}
	
	.member-right {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}
	
	.member-role {
		margin-left: auto;
	}
	
	.role-tag {
		font-size: 24rpx;
		padding: 4rpx 12rpx;
		border-radius: 8rpx;
	}
	
	.role-tag.owner {
		background-color: #fff3e0;
		color: #ff9800;
	}
	
	.role-tag.admin {
		background-color: #e3f2fd;
		color: #2196f3;
	}
	
	.remove-btn {
		padding: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.remove-icon-circle {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		border: 2rpx solid #ff4444;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
	}
	
	.remove-icon-line {
		width: 20rpx;
		height: 2rpx;
		background-color: #ff4444;
		position: absolute;
	}
	
	.loading {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 60rpx 0;
	}
	
	/* 操作按钮区域 */
	.action-section {
		padding: 40rpx;
	}
	
	/* 编辑弹窗 */
	.edit-popup {
		padding: 40rpx;
	}
	
	.popup-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 40rpx;
	}
	
	.popup-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #2b2b2b;
	}
	
	.popup-content {
		margin-bottom: 40rpx;
	}
	
	.edit-avatar-content {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 30rpx;
	}
	
	.preview-avatar {
		width: 200rpx;
		height: 200rpx;
		border-radius: 50%;
	}
	
	.edit-name-content {
		padding: 20rpx 0;
	}
	
	.edit-input {
		width: 100%;
		padding: 20rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 10rpx;
		font-size: 30rpx;
	}
	
	.edit-description-content {
		padding: 20rpx 0;
	}
	
	.edit-textarea {
		width: 100%;
		min-height: 200rpx;
		padding: 20rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 10rpx;
		font-size: 30rpx;
	}
	
	.popup-footer {
		display: flex;
		gap: 20rpx;
	}
	
	.popup-footer .u-button {
		flex: 1;
	}
</style>