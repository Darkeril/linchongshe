<template>
	<view class="message-container">
		<!-- 状态栏占位（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" class="status-bar-placeholder"></view>
		<!-- #endif -->
		<!-- 自定义导航栏 -->
		<view class="message-header" :style="{top: navBarTop}">
			<view class="header-title">消息</view>
			<view class="header-right" @click="createGroupChat">
				<u-icon name="plus-circle" color="#3d8af5" size="24"></u-icon>
			</view>
		</view>
		<!-- 导航栏占位，避免内容被遮挡 -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- 聊天列表（使用 scroll-view 包裹，顶部标签也在 scroll-view 内，可以跟随滚动） -->
		<scroll-view 
			scroll-y 
			:style="{height: scrollViewHeight + 'px'}" 
			class="chat-list-scroll"
			:refresher-enabled="true"
			@refresherrefresh="onRefresh"
			:refresher-triggered="refreshing"
		>
			<!-- 顶部消息入口栏（放在 scroll-view 内，可以跟随滚动） -->
			<view class="msg-divider"></view>
			<view class="msg-top-bar">
				<view class="msg-item" @click="goToPraiseAndCollect">
					<view class="icon-wrap">
						<view class="icon-bg" style="background: #faecea;">
							<u-icon name="heart-fill" color="rgb(235, 104, 97)" size="30"></u-icon>
						</view>
						<u-badge v-if="praiseAndCollectNum>0" :absolute="true" :offset="[6,6]" class="badge-corner" numberType="overflow" max="99" :value="praiseAndCollectNum"></u-badge>
					</view>
					<text class="msg-label">赞和收藏</text>
				</view>
				<view class="msg-item" @click="goToAttentionMessageList">
					<view class="icon-wrap">
						<view class="icon-bg" style="background: #eaeafd;">
							<u-icon name="account-fill" color="#5b6cd9" size="30"></u-icon>
						</view>
						<u-badge v-if="attentionUnreadNum>0" :absolute="true" :offset="[6,6]" class="badge-corner" numberType="overflow" max="99" :value="attentionUnreadNum"></u-badge>
					</view>
					<text class="msg-label">新增关注</text>
				</view>
				<view class="msg-item" @click="goToComment">
					<view class="icon-wrap">
						<view class="icon-bg" style="background: #eafaf1;">
							<u-icon name="chat" color="#4fc08d" size="30"></u-icon>
						</view>
						<u-badge v-if="commentUnreadNum>0" :absolute="true" :offset="[6,6]" class="badge-corner" numberType="overflow" max="99" :value="commentUnreadNum"></u-badge>
					</view>
					<text class="msg-label">评论和@</text>
				</view>
			</view>
			
			<!-- 通知公告栏 -->
			<view class="notice-bar" @click="goToNoticeList">
				<view class="notice-icon-wrap">
					<view class="notice-icon-bg">
						<u-icon name="bell-fill" color="#ffc107" size="30"></u-icon>
					</view>
				</view>
				<view class="notice-content">
					<text class="notice-title">系统消息</text>
					<text class="notice-desc">{{ systemNoticeDesc }}</text>
				</view>
				<view class="notice-arrow">
					<!-- <u-icon name="arrow-right" color="#999" size="18"></u-icon> -->
				</view>
			</view>

			<!-- 闲宝消息：商品对话入口（参考 web 端） -->
			<view class="notice-bar product-msg-bar" @click="goToProductMessageList">
				<view class="notice-icon-wrap">
					<view class="notice-icon-bg" style="background: #e8f5e9;">
						<u-icon name="bag-fill" color="#4caf50" size="30"></u-icon>
					</view>
					<u-badge v-if="productChatUnreadNum > 0" :absolute="true" :offset="[6,6]" class="badge-corner" numberType="overflow" max="99" :value="productChatUnreadNum"></u-badge>
				</view>
				<view class="notice-content">
					<text class="notice-title">闲宝消息</text>
					<text class="notice-desc">点击查看商品消息</text>
				</view>
				<!-- <u-icon name="arrow-right" color="#999" size="18" class="notice-arrow"></u-icon> -->
			</view>
			
		<!-- 聊天记录列表 -->
		<view style="padding-bottom: 280rpx;">
			<block v-for="(item,index) in list" :key="index">
					<liu-swipe-action :index="item.user_id" @clickItem="clickItem" :btnList="operation" :ref="'ref'+index">
						<view style="display: flex;padding: 20rpx;height: 100rpx;" @touchmove="touchmove(index)"
							@click="goToChat(item)">
							<image style="width: 100rpx;height: 100rpx;border-radius: 50%;margin: 0 15rpx;"
								mode="aspectFill" :src="item.avatar_url">
							</image>
							<view style="margin:0 10rpx;flex: 1;align-self: center;">
								<view>{{item.user_name}}</view>
								<rich-text class="simpleMessage" :nodes="item.last_message"></rich-text>
							</view>
							<view style="align-self: center;text-align: end;">
								<view style="color: #949495;font-size: 24rpx;">{{item.last_time}}</view>
								<view v-if="item.unread_num>0" style="display: inline-block;">
									<u-badge numberType="overflow" max="99" :value="item.unread_num"></u-badge>
								</view>
							</view>
						</view>
					</liu-swipe-action>
				</block>
			</view>
			
			<!-- 发现好友模块（点击关闭后整块不显示，刷新恢复） -->
			<!-- <view class="discover-section" v-if="!hideDiscover && recommendUsers.length">
				<view class="discover-header">
					<view class="discover-title">
						发现好友
						<u-icon class="discover-tip-icon" name="question-circle" color="#999" size="18"
							@click="openDiscoverTip"></u-icon>
					</view>
					<view class="discover-close" @click="hideDiscover = true">关闭</view>
				</view>
				<view class="discover-item" v-for="(user, index) in recommendUsers" :key="getUserKey(user, index)">
					<image 
						class="discover-avatar" 
						mode="aspectFill" 
						:src="getUserAvatar(user)" 
						@click="goToUserProfile(user, index)"
					></image>
					<view class="discover-info">
						<view class="discover-name">{{ getUserName(user) }}</view>
						<view class="discover-meta">
							<text class="discover-badge">粉丝 {{ getUserFans(user) }}</text>
							<text class="discover-desc" v-if="user && user.likeCount">被点赞超过{{ user.likeCount }}次</text>
						</view>
					</view>
					<view>
						<u-button 
							type="error" 
							size="small" 
							shape="circle" 
							v-if="!getUserFollowed(user)" 
							@click="followUser(user, index)"
							text="关注"
						></u-button>
						<u-button 
							type="default" 
							size="small" 
							shape="circle" 
							v-else 
							disabled 
							text="已关注"
						></u-button>
					</view>
				</view>
				<view class="discover-more">
					<u-button type="default" size="small" plain shape="circle" @click="loadMoreRecommend">换一换</u-button>
				</view>
			</view> -->
		</scroll-view>
		
		<!-- 客服悬浮按钮 -->
		<view class="customer-service-float-btn" @click="goToCustomerService">
			<u-icon name="server-fill" color="#ffffff" size="35"></u-icon>
		</view>
		
		<!-- 底部导航栏 -->
		<custom-tabbar :current="3" @update:current="updateCurrentTab"></custom-tabbar>
	</view>
</template>

<script>
	// import CustomTabbar from '@/components/custom-tabbar/CustomTabBar.vue'
	import {
		sqliteUtil
	} from '@/utils/sqliteUtil.js'
	import {
		clearMessageCount,
		getChatUserList,
		getCountMessage,
		getMyGroupList,
		getProductChatUserList
	} from '@/apis/mesasage_apis.js'
	import {
		stringDateFormat,
		replaceImageTags,
		weChatTimeFormat2
	} from '@/utils/util.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'

	// 列表副标题默认文案；推送预览仅内存展示，不持久化，避免刷新/重进仍显示旧推送摘要
	const SYSTEM_NOTICE_DEFAULT_DESC = '点击查看系统通知'

	export default {
		mixins: [weixinNavBar],
		// components: {
		// 	CustomTabbar
		// },
		data() {
			return {
				operation: [{
					id: 1,
					name: '已读',
					width: '150rpx',
					bgColor: '#5f92f7',
					color: '#FFFFFF',
					fontSize: '28rpx'
				}, {
					id: 2,
					name: '删除',
					width: '150rpx',
					bgColor: '#ed656d',
					color: '#FFFFFF',
					fontSize: '28rpx'
				}],
			list: [],
			userId: '',
			show: false,
			praiseAndCollectNum: 0,
			attentionUnreadNum: 0,
			commentUnreadNum: 0,
			productChatUnreadNum: 0,
			recommendUsers: [],
			recommendPage: 1,
			recommendSize: 10,
			hideDiscover: false,
			showDiscoverTip: false,
			discoverMounted: true,
			discoverTipContent: '基于下列信息，向你推荐可能认识的人：\n1. 你与他人的互动行为\n2. 你与他人的关注关系\n我们所使用的数据均基于你的授权或公开信息，且仅会在好友推荐的目的下合理使用，保护你的个人信息。\n如果你不希望自己被推荐给可能认识的人，可以前往「我 - 设置 - 个性化选项」中调整设置。',
			scrollViewHeight: 600, // scroll-view 的高度
			refreshing: false, // 下拉刷新状态
			statusBarHeight: 0, // 状态栏高度
			systemNoticeDesc: SYSTEM_NOTICE_DEFAULT_DESC,
		}
	},
		methods: {
			// 辅助方法：获取用户唯一标识
			getUserKey(user, index) {
				if (!user) return index
				return user.uid || user.userId || user.id || index
			},
			
			// 辅助方法：获取用户头像
			getUserAvatar(user) {
				if (!user) return '/static/default.jpg'
				
				const avatar = user.avatar || user.avatarUrl || ''
				if (!avatar || avatar === 'undefined' || avatar === 'null') {
					return '/static/default.jpg'
				}
				
				// 确保头像URL是完整的
				if (avatar.startsWith('http')) {
					return avatar
				} else if (avatar.startsWith('/')) {
					return avatar
				} else {
					return '/static/default.jpg'
				}
			},
			
			// 辅助方法：获取用户名称
			getUserName(user) {
				if (!user) return '未知用户'
				return user.nickname || user.username || user.name || '未知用户'
			},
			
			// 辅助方法：获取用户粉丝数
			getUserFans(user) {
				if (!user) return 0
				return user.fanCount || user.fans || user.fansCount || 0
			},
			
			// 辅助方法：获取用户关注状态
			getUserFollowed(user) {
				if (!user) return false
				return user.followed || false
			},
			
			async fetchRecommendUsers(reset = false) {
				try {
					const {
						getRecommendUser
					} = require('@/apis/search_service.js')
					if (reset) this.recommendPage = 1
					const res = await getRecommendUser(this.recommendPage, this.recommendSize)
					console.log('推荐用户API响应:', res)
					
					const rows = res?.data?.records || res?.data || []
					console.log('推荐用户数据:', rows)
					
					// 验证数据完整性
					const validUsers = rows.filter(user => {
						// 检查用户对象是否存在
						if (!user || typeof user !== 'object') {
							console.warn('发现无效用户数据（非对象）:', user)
							return false
						}
						
						// 检查是否有ID
						const hasId = user.uid || user.userId || user.id
						if (!hasId) {
							console.warn('发现无效用户数据（缺少ID）:', user)
							return false
						}
						
						// 检查头像字段是否有效
						const avatar = user.avatar || user.avatarUrl
						if (avatar && (avatar === 'undefined' || avatar === 'null')) {
							console.warn('发现无效用户数据（头像字段为undefined/null）:', user)
							// 不直接过滤，而是清理头像字段
							user.avatar = ''
							user.avatarUrl = ''
						}
						
						return true
					})
					
					if (validUsers.length !== rows.length) {
						console.warn(`过滤掉 ${rows.length - validUsers.length} 个无效用户数据`)
					}
					
					this.recommendUsers = reset ? validUsers : [...this.recommendUsers, ...validUsers]
					console.log('更新后的推荐用户列表:', this.recommendUsers)
				} catch (e) {
					console.warn('推荐用户获取失败', e)
				}
			},
			goDiscoverSetting() {
				this.showDiscoverTip = false
				// TODO: 跳转到“我-设置-个性化选项”对应页面；当前占位
				uni.showToast({
					title: '请在“我-设置-个性化选项”中调整',
					icon: 'none'
				})
			},
			openDiscoverTip() {
				this.showDiscoverTip = true
				uni.showModal({
					title: '说明',
					content: this.discoverTipContent,
					confirmText: '去设置',
					cancelText: '取消',
					success: (res) => {
						if (res.confirm) this.goDiscoverSetting()
					}
				})
			},
			goToUserProfile(user, index) {
				console.log('goToUserProfile 接收到的用户数据:', user)
				console.log('用户索引:', index)
				console.log('当前推荐用户列表:', this.recommendUsers)
				
				// 检查 user 对象是否存在
				if (!user) {
					console.error('用户数据为空')
					console.error('尝试从列表中获取用户数据...')
					
					// 尝试从列表中获取用户数据
					if (index !== undefined && this.recommendUsers[index]) {
						console.log('从列表中获取到用户数据:', this.recommendUsers[index])
						user = this.recommendUsers[index]
					} else {
						uni.showToast({
							title: '用户信息获取失败',
							icon: 'none'
						})
						return
					}
				}
				
				// 再次检查 user 对象
				if (!user) {
					console.error('用户数据仍然为空')
					uni.showToast({
						title: '用户信息获取失败',
						icon: 'none'
					})
					return
				}
				
				// 尝试获取用户ID
				const uid = user.uid || user.userId || user.id
				console.log('提取的用户ID:', uid)
				
				if (!uid) {
					console.error('用户ID缺失:', user)
					uni.showToast({
						title: '用户ID缺失',
						icon: 'none'
					})
					return
				}
				
				// 检查是否是当前用户
				const currentUserId = uni.getStorageSync('userInfo')?.id
				console.log('当前用户ID:', currentUserId, '目标用户ID:', uid)
				
				// 根据平台和用户类型选择跳转方式
				try {
					if (uid == currentUserId) {
						// 跳转到自己的个人页面
						console.log('跳转到自己的个人页面')
						uni.navigateTo({
							url: '/pkg-main/pages/mine/mine',
							fail: (err) => {
								console.error('跳转失败:', err)
								uni.showToast({
									title: '跳转失败',
									icon: 'none'
								})
							}
						})
					} else {
						// 跳转到其他用户个人页面
						console.log('跳转到其他用户个人页面:', uid)
						const url = `/pkg-mine/pages/mine/otherMine?userId=${uid}`
						console.log('跳转URL:', url)
						
						uni.navigateTo({
							url: url,
							fail: (err) => {
								console.error('跳转失败:', err)
								console.error('跳转URL:', url)
								console.error('用户数据:', user)
								
								// 微信小程序特殊处理
								// #ifdef MP-WEIXIN
								uni.showModal({
									title: '跳转失败',
									content: `无法跳转到用户页面，请检查用户ID是否正确`,
									showCancel: false,
									confirmText: '确定'
								})
								// #endif
								
								// #ifndef MP-WEIXIN
								uni.showToast({
									title: '跳转失败',
									icon: 'none'
								})
								// #endif
							},
							success: () => {
								console.log('跳转成功')
							}
						})
					}
				} catch (error) {
					console.error('跳转异常:', error)
					uni.showToast({
						title: '跳转异常',
						icon: 'none'
					})
				}
			},
			loadMoreRecommend() {
				this.recommendPage += 1
				this.fetchRecommendUsers()
			},
			async followUser(user, index) {
				console.log('followUser 接收到的用户数据:', user)
				console.log('用户索引:', index)
				
				// 检查 user 对象是否存在
				if (!user) {
					console.error('用户数据为空')
					console.error('尝试从列表中获取用户数据...')
					
					// 尝试从列表中获取用户数据
					if (index !== undefined && this.recommendUsers[index]) {
						console.log('从列表中获取到用户数据:', this.recommendUsers[index])
						user = this.recommendUsers[index]
					} else {
						uni.showToast({ title: '用户信息获取失败', icon: 'none' })
						return
					}
				}
				
				// 再次检查 user 对象
				if (!user) {
					console.error('用户数据仍然为空')
					uni.showToast({ title: '用户信息获取失败', icon: 'none' })
					return
				}
				
				try {
					const { updateAttention } = require('@/apis/user_service.js')
					const targetUserId = user.uid || user.userId || user.id
					if (!targetUserId) {
						uni.showToast({ title: '用户ID缺失', icon: 'none' })
						return
					}
					const res = await updateAttention({ targetUserId })
					if (res && Number(res.code) === 200) {
						// 与笔记详情页一致：切换关注状态（这里推荐流按关注后直接置为已关注）
						const isFollow = typeof res.data === 'boolean' ? !!res.data : true
						// 确保响应式：如果初始对象没有 followed 字段，用 $set 添加
						if (Object.prototype.hasOwnProperty.call(user, 'followed')) {
							user.followed = isFollow
						} else {
							this.$set(user, 'followed', isFollow)
						}
						// 再保险：同步到列表（避免某些平台对对象引用不追踪新增键）
						this.recommendUsers = this.recommendUsers.map(u => (u.uid === targetUserId || u.userId === targetUserId || u.id === targetUserId) ? { ...u, followed: isFollow } : u)
						uni.showToast({ title: user.followed ? '关注成功' : '取消关注成功', icon: 'none' })
					} else {
						uni.showToast({ title: res?.msg || '关注失败', icon: 'none' })
					}
				} catch (e) {
					console.warn('关注失败', e)
					uni.showToast({ title: '关注失败', icon: 'none' })
				}
			},
			getCurrentUid() {
				const fromData = this.userId
				const storageUserInfo = uni.getStorageSync('userInfo') || {}
				const fromStorageUser = storageUserInfo.id || ''
				const fromStorageUid = uni.getStorageSync('userId') || ''
				const fromApp = (getApp && getApp().globalData && getApp().globalData.userInfo && getApp().globalData.userInfo.id) || ''
				const fromStore = (this.$store && this.$store.state && this.$store.state.user && this.$store.state.user.userInfo && this.$store.state.user.userInfo.id) || ''
				const uid = fromData || fromStorageUser || fromStorageUid || fromApp || fromStore || ''
				console.log('[message] resolve uid', { fromData, fromStorageUser, fromStorageUid, fromApp, fromStore, uid })
				if (uid) this.userId = uid
				return uid
			},
			updateCurrentTab(index) {
				console.log('切换到 tab:', index);
			},
			// 清除未读消息（系统模块）
			async clearUnread(type) {
				try {
					const uid = this.getCurrentUid()
					if (!uid) {
						console.warn('[message] clearUnread missing uid, storage userInfo=', uni.getStorageSync('userInfo'))
						uni.showToast({ title: '用户未登录', icon: 'none' })
						return
					}
					const payload = { type, sendUid: uid }
					console.log('[message] clearUnread request', payload)
					await clearMessageCount(payload)
					// 清除成功后，主动刷新后端未读计数与底部徽标
					try { this.$ws.setCornerMark && this.$ws.setCornerMark() } catch(e) {}
				} catch (e) {
					console.error('清除未读失败', e)
				}
			},
			//消息列表
			async fetchMessageList() {
				try {
					// 获取私聊列表
					const res = await getChatUserList()
					const privateChatList = (res.data || []).map(item => ({
						user_id: item.uid,
						user_name: item.username,
						avatar_url: item.avatar,
						last_message: this.formatLastMessage(item.content),
						unread_num: item.count,
						last_time: weChatTimeFormat2(item.timestamp),
						timestamp: item.timestamp || 0, // 保存原始时间戳用于排序
						chat_type: 0, // 私聊
						group_chat_id: null
					}))
					
					// 获取群聊列表
					let groupChatList = []
					try {
						const groupRes = await getMyGroupList()
						groupChatList = (groupRes.data || []).map(item => ({
							user_id: item.id, // 使用群聊ID作为标识
							user_name: item.groupName || '群聊',
							avatar_url: item.groupAvatar || '/static/default-group.jpg',
							last_message: this.formatGroupLastMessage(item),
							unread_num: item.unreadCount || 0,
							last_time: item.lastMessageTime ? weChatTimeFormat2(item.lastMessageTime) : '',
							timestamp: item.lastMessageTime || 0, // 保存原始时间戳用于排序
							chat_type: 1, // 群聊
							group_chat_id: item.id
						}))
					} catch (e) {
						console.error('获取群聊列表失败', e)
					}
					
					// 合并列表并按时间戳排序（使用原始时间戳，而不是格式化后的字符串）
					this.list = [...privateChatList, ...groupChatList].sort((a, b) => {
						// 使用原始时间戳进行排序
						const timeA = a.timestamp || 0
						const timeB = b.timestamp || 0
						// 降序排序：最新的消息在前面
						return timeB - timeA
					})
				} catch (e) {
					console.error('获取消息列表失败', e)
				}
			},
			formatLastMessage(content) {
				// 若后端/本地已经是占位文案，直接返回
				if (content === '[语音]' || content === '[图片]' || content === '[视频]') {
					return content
				}
				// 如果内容是URL格式，则可能是图片/视频/语音
				if (typeof content === 'string') {
					// 音频URL：mp3/m4a/aac/wav/ogg 等
					if (content.match(/\.(mp3|m4a|aac|wav|ogg)(\?|$)/i)) {
						return '[语音]'
					}
					// 检查是否是图片URL
					if (content.match(/\.(jpg|jpeg|png|gif|webp)(\?|$)/i) ||
						content.startsWith('https://image.')) {
						return '[图片]'
					}
					// 检查是否是视频URL
					if (content.match(/\.(mp4|mov|avi|webm)(\?|$)/i) ||
						content.startsWith('https://video.')) {
						return '[视频]'
					}
					// 检查是否包含img标签
					if (content.includes('<img')) {
						return '[图片]'
					}
				}
				return content
			},
			// 群聊最后一条消息：自己发的只显示消息，他人发的显示「发送者：消息」
			formatGroupLastMessage(item) {
				const content = this.formatLastMessage(item.lastMessage || item.content || '')
				const myId = this.getCurrentUid()
				const senderId = item.lastMessageSenderId || item.lastMessageSender
				if (myId != null && senderId != null && String(senderId) === String(myId)) {
					return content
				}
				const sender = item.lastMessageSenderName || item.lastSenderName || item.senderName || item.lastMessageSender || '群成员'
				return `${sender}：${content}`
			},
			// 点击事件
			async goToPraiseAndCollect() {
				try {
					const uid = this.getCurrentUid()
					if (!uid) return
					// 只清除未读消息（与 Web 端枚举保持一致：0=点赞收藏）
					await this.clearUnread(0)
					this.praiseAndCollectNum = 0
					try { this.$ws.setCornerMark && this.$ws.setCornerMark() } catch(e) {}
					this.updateTabBadgeFromServer()
					// 直接跳转
					uni.navigateTo({
						url: '/pkg-others/pages/praiseAndCollect/praiseAndCollect'
					})
				} catch (e) {
					console.error('清除未读消息失败', e)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			},
			// 跳转到评论页面
			async goToComment() {
				try {
					const uid = this.getCurrentUid()
					if (!uid) return
					// 与 Web 端枚举保持一致：1=评论
					await this.clearUnread(1)
					this.commentUnreadNum = 0
					try { this.$ws.setCornerMark && this.$ws.setCornerMark() } catch(e) {}
					this.updateTabBadgeFromServer()
					uni.navigateTo({
						url: '/pkg-msg/pages/comment/comment'
					})
				} catch (e) {
					console.error('清除未读消息失败', e)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			},
			// 跳转到关注列表页面
			async goToAttentionMessageList() {
				try {
					const uid = this.getCurrentUid()
					if (!uid) return
					// 与 Web 端枚举保持一致：2=关注
					await this.clearUnread(2)
					this.attentionUnreadNum = 0
					try { this.$ws.setCornerMark && this.$ws.setCornerMark() } catch(e) {}
					this.updateTabBadgeFromServer()
					uni.navigateTo({
						url: '/pkg-others/pages/attentionMessageList/attentionMessageList'
					})
				} catch (e) {
					console.error('清除未读消息失败', e)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			},
			// goToPraiseAndCollect() {
			// 	this.$sqliteUtil.SqlExecute(`UPDATE system_message SET unread_num=0 WHERE id=1`).then(res => {
			// 		this.refreshPraiseAndCollectNum()
			// 		this.$ws.setCornerMark()
			// 		uni.navigateTo({
			// 			url: `/pages/praiseAndCollect/praiseAndCollect`
			// 		})
			// 	})
			// },
			// goToAttentionMessageList() {
			// 	this.$sqliteUtil.SqlExecute(`UPDATE system_message SET unread_num=0 WHERE id=2`).then(res => {
			// 		this.refreshAttentionList()
			// 		this.$ws.setCornerMark()
			// 		uni.navigateTo({
			// 			url: `/pages/attentionMessageList/attentionMessageList`
			// 		})
			// 	})
			// },
			async clickItem(e) {
				if (e.id === 1) {
					try {
					const uid = this.getCurrentUid()
					await clearMessageCount({ type: 3, userId: e.index, sendUid: uid })
						this.fetchMessageList()
						this.resetAll()
						try { this.$ws.setCornerMark && this.$ws.setCornerMark() } catch(e) {}
					} catch(err) { console.warn(err) }
				} else if (e.id === 2) {
					this.list = this.list.filter(x => x.user_id !== e.index)
					this.resetAll()
				}
			},
			resetAll() {
				this.list.forEach((res, index) => {
					this.$refs['ref' + index][0].reset()
				})
			},
			touchmove(index) {
				this.list.forEach((res, i) => {
					if (index != i) {
						this.$refs['ref' + i][0].reset()
					}
				})
			},
		refreshList() {
			this.fetchMessageList()
		},
		// scroll-view 下拉刷新（修复滚动问题）
		async onRefresh() {
			this.refreshing = true
			
			try {
				// 刷新消息列表
				await this.fetchMessageList()
				
				// 刷新各项未读数
				await Promise.all([
					this.refreshAttentionList(),
					this.refreshPraiseAndCollectNum(),
					this.refreshCommentNum()
				])
				
				// 发送 WebSocket 刷新指令
				try {
					if (this.$ws && this.$ws.send) {
						let refreshText = {
							from: uni.getStorageSync('userInfo').id,
							content: uni.getStorageSync('uniapp_token'),
							messageType: 1,
						}
						this.$ws.send(refreshText)
					}
				} catch (e) {
					console.log('WebSocket 刷新失败:', e)
				}
			} catch (error) {
				console.error('刷新失败:', error)
			} finally {
				this.systemNoticeDesc = SYSTEM_NOTICE_DEFAULT_DESC
				// 停止刷新动画
				setTimeout(() => {
					this.refreshing = false
				}, 500)
			}
		},
			async refreshAttentionList() {
				try {
					const res = await getCountMessage()
					const data = res?.data || {}
					this.attentionUnreadNum = Number(data.followCount) || 0
				} catch (e) {
					this.attentionUnreadNum = 0
				}
			},
			async refreshPraiseAndCollectNum() {
				try {
					const res = await getCountMessage()
					const data = res?.data || {}
					this.praiseAndCollectNum = Number(data.likeOrCollectionCount) || 0
				} catch (e) {
					this.praiseAndCollectNum = 0
				}
			},
			async updateTabBadgeFromServer() {
				try {
					const res = await getCountMessage()
					const d = res?.data || {}
					const total = [
						Number(d.chatCount) || 0,
						Number(d.productChatCount) || 0,
						Number(d.likeOrCollectionCount) || 0,
						Number(d.commentCount) || 0,
						Number(d.followCount) || 0,
					].reduce((a,b)=>a+b,0)
					// 同步顶部三个模块徽标数
					this.praiseAndCollectNum = Number(d.likeOrCollectionCount) || 0
					this.attentionUnreadNum = Number(d.followCount) || 0
					this.commentUnreadNum = Number(d.commentCount) || 0
					this.productChatUnreadNum = Number(d.productChatCount) || 0
					try { uni.setStorageSync('im_total_unread', total) } catch(e) {}
					uni.$emit('im:setTabBadge', total)
					uni.$emit('updateTabBarBadge', { index: 3, badge: total })
				} catch (e) {
					// 忽略错误
				}
			},
			async refreshCommentNum() {
				try {
					const res = await getCountMessage()
					const data = res?.data || {}
					this.commentUnreadNum = Number(data.commentCount) || 0
				} catch (e) {
					this.commentUnreadNum = 0
				}
			},
			goToChat(item) {
				console.log(item)
				try {
					// 判断是私聊还是群聊
					if (item.chat_type === 1) {
						// 群聊 - 清除未读消息
						const uid = this.getCurrentUid()
						clearMessageCount({
							type: 3,
							sendUid: uid,
							groupChatId: item.group_chat_id
						})
						
						uni.navigateTo({
							url: `/pkg-msg/pages/chat/chat?groupId=${item.group_chat_id}&chatType=1`,
							fail: (err) => {
								console.error('跳转失败:', err)
								uni.showToast({
									title: '页面加载失败',
									icon: 'none'
								})
							}
						})
					} else {
						// 私聊 - 清除未读消息
						const uid = this.getCurrentUid()
						clearMessageCount({
							type: 3,
							userId: item.user_id,
							sendUid: uid
						})

						// 刷新消息列表
						this.fetchMessageList()
						this.resetAll()
						// 同步底部徽标
					try { this.$ws.setCornerMark && this.$ws.setCornerMark() } catch(e) {}

					// 3. 处理头像URL编码
					// const avatarUrl = encodeURIComponent(item.avatar_url)
					const avatarUrl = item.avatar_url

					// 4. 跳转到聊天页面（分包路径）
					uni.navigateTo({
						url: `/pkg-msg/pages/chat/chat?userId=${item.user_id}&userName=${item.user_name}&avatarUrl=${avatarUrl}&chatType=0`
					})
					}
				} catch (e) {
					console.error('跳转失败', e)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			},
			// 创建群聊
			createGroupChat() {
				uni.navigateTo({
					url: '/pkg-msg/pages/createGroup/createGroup'
				})
			},
			// 跳转到通知公告列表
			goToNoticeList() {
				uni.navigateTo({
					url: '/pkg-others/pages/systemNoticeList/systemNoticeList',
					fail: (err) => {
						console.error('跳转失败:', err)
						uni.showToast({
							title: '页面加载失败',
							icon: 'none'
						})
					}
				})
			},
			// 跳转到闲宝消息（商品对话列表）
			goToProductMessageList() {
				uni.navigateTo({
					url: '/pkg-msg/pages/productMessageList/productMessageList',
					fail: (err) => {
						console.error('跳转失败:', err)
						uni.showToast({ title: '页面加载失败', icon: 'none' })
					}
				})
			},
			// 跳转到客服聊天
			goToCustomerService() {
				// 客服用户ID固定为 '0'（因为数据库字段是整数类型）
				const customerServiceId = '0'
				const customerServiceName = '客服'
				const customerServiceAvatar = '/static/customer-service-avatar.png' // 需要添加客服头像资源
				
				uni.navigateTo({
					url: `/pkg-msg/pages/chat/chat?userId=${customerServiceId}&userName=${customerServiceName}&avatarUrl=${customerServiceAvatar}&chatType=0`,
					fail: (err) => {
						console.error('跳转客服失败:', err)
						uni.showToast({
							title: '页面加载失败',
							icon: 'none'
						})
					}
				})
			}
		},
	onLoad() {
	this.userId = (uni.getStorageSync('userInfo')?.id) || ''
	
	// 获取状态栏高度
	const systemInfo = uni.getSystemInfoSync()
	this.applyWeixinNavBar(systemInfo, 44)
	// 计算 scroll-view 的高度（修复滚动问题）
	const windowInfo = uni.getWindowInfo()
	const windowHeight = windowInfo.screenHeight || windowInfo.windowHeight || 600
	const safeAreaBottom = windowInfo.safeArea?.bottom || windowHeight
	const safeAreaInsetBottom = windowHeight - safeAreaBottom
	const navBarHeight = 44 // 导航栏高度（px）
	const bottomTabBarHeight = 100 // 底部 tabbar 高度（px）
	this.scrollViewHeight = windowHeight - (this.statusBarHeight || 0) - navBarHeight - bottomTabBarHeight - safeAreaInsetBottom
	
	this.fetchMessageList()
	// this.fetchRecommendUsers(true)
	try {
		uni.removeStorageSync('system_notice_preview')
	} catch (e) {}
	uni.$on('updateMessageList', () => {
		this.fetchMessageList()
	})
	uni.$on('system:notification', (payload) => {
		const c = payload && payload.content
		const img = payload && payload.imageUrl
		const text =
			typeof c === 'string'
				? c.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
				: ''
		if (!text && !img) return
		const short = text
			? text.length > 40
				? text.slice(0, 40) + '...'
				: text
			: '[图片]'
		this.systemNoticeDesc = short
	})
	// 监听从 WS 广播的顶部模块明细未读
	uni.$on('im:setMetaCounts', (meta) => {
		if (!meta) return
		this.praiseAndCollectNum = Number(meta.likeOrCollectionCount) || 0
		this.attentionUnreadNum = Number(meta.followCount) || 0
		this.commentUnreadNum = Number(meta.commentCount) || 0
	})
	uni.$on('updateAttentionList', () => {
		this.refreshAttentionList()
	})
	uni.$on('updatePraiseAndCollectionList', () => {
		this.refreshPraiseAndCollectNum()
	})
},
			onShow() {
			this.userId = (uni.getStorageSync('userInfo')?.id) || this.userId
			this.fetchMessageList()
			this.refreshAttentionList()
			this.refreshPraiseAndCollectNum()
			this.refreshCommentNum()
			this.updateTabBadgeFromServer()
		},
		onPullDownRefresh() {
			this.systemNoticeDesc = SYSTEM_NOTICE_DEFAULT_DESC
			// ✅ 刷新消息列表
			this.refreshList()
			
			// ✅ 刷新各项未读数
			this.refreshAttentionList()
			this.refreshPraiseAndCollectNum()
			this.refreshCommentNum()
			
			// ✅ 发送 WebSocket 刷新指令
			try {
				if (this.$ws && this.$ws.send) {
					let refreshText = {
						from: uni.getStorageSync('userInfo').id,
						content: uni.getStorageSync('uniapp_token'),
						messageType: 1,
					}
					this.$ws.send(refreshText)
				}
			} catch (e) {
				console.log('WebSocket 刷新失败:', e)
			}
			
			// ✅ 停止下拉刷新动画
			setTimeout(() => {
				uni.stopPullDownRefresh()
			}, 1000)
		}
	}
</script>


<style lang="scss">
	page {
		background-color: #ffffff;
	}
	
	.message-container {
		width: 100%;
		height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: #ffffff;
		position: relative; /* 确保容器是定位上下文 */
		overflow: hidden; /* 防止内容溢出，但不影响 fixed 定位的按钮 */
	}
	
	/* 状态栏占位 */
	.status-bar-placeholder {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		width: 100%;
		background-color: #ffffff;
		z-index: 10000;
	}
	
	/* 自定义导航栏 */
	.message-header {
		position: fixed;
		left: 0;
		right: 0;
		z-index: 9999;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 44px;
		background-color: #ffffff;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.header-title {
		font-size: 36rpx;
		font-weight: 600;
		color: #2b2b2b;
	}
	
	.header-right {
		position: absolute;
		right: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		width: 60rpx;
		height: 88rpx;
	}
	
	.chat-list-scroll {
		flex: 1;
		overflow-y: auto;
		-webkit-overflow-scrolling: touch;
		position: relative;
		z-index: 1; /* 确保 scroll-view 在按钮之下 */
		/* 确保 scroll-view 不会创建新的层叠上下文，从而遮挡 fixed 定位的按钮 */
		transform: none;
		-webkit-transform: none;
	}

	.simpleMessage {
		color: #949495;
		font-size: 30rpx;
		text-overflow: ellipsis;
		word-break: break-all;
		-webkit-line-clamp: 1;
		-webkit-box-orient: vertical;
		display: -webkit-box;
		overflow: hidden;
	}

	.msg-divider {
		width: 100%;
		height: 1rpx;
		background: #eaeaea;
	}

	.msg-top-bar {
		display: flex;
		justify-content: space-around;
		align-items: center;
		margin: 30rpx 0 20rpx 0;
	}

	.msg-item {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

.icon-wrap {
		position: relative;
		display: inline-block;
		line-height: 0;
		/* 让徽标定位以图标容器为基准 */
		width: 100rpx;
		height: 100rpx;
		margin-bottom: 12rpx;
	}

.badge-corner {
		position: absolute !important;
		top: 0;
		right: 0;
		transform: translate(50%, -50%);
		z-index: 1;
	}

	.icon-bg {
		width: 100rpx;
		height: 100rpx;
		border-radius: 35rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 12rpx;
	}

	.msg-label {
		font-size: 28rpx;
		color: #333;
		margin-top: 2rpx;
	}
	
	/* 发现好友样式 */
	.discover-section {
		margin: -80rpx 20rpx 120rpx 20rpx;
		border-top: 1rpx solid #f0f0f0;
		padding-top: 20rpx;
	}
	
	.discover-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 10rpx;
	}
	
	.discover-title {
		font-size: 32rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
	}
	
	.discover-tip-icon {
		margin-left: 10rpx;
	}
	
	.discover-close {
		color: #999;
		font-size: 26rpx;
	}
	
	.discover-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f7f7f7;
	}
	
	.discover-avatar {
		width: 110rpx;
		height: 110rpx;
		border-radius: 50%;
		margin-right: 20rpx;
	}
	
	.discover-info {
		flex: 1;
	}
	
	.discover-name {
		font-size: 30rpx;
		color: #222;
		margin-bottom: 8rpx;
	}
	
	.discover-meta {
		color: #999;
		font-size: 24rpx;
	}
	
	.discover-badge {
		background: #f5f5f5;
		padding: 2rpx 10rpx;
		border-radius: 8rpx;
		margin-right: 12rpx;
	}
	
	.discover-desc {
		color: #999;
	}
	
	.discover-more {
		text-align: center;
		margin-top: 16rpx;
	}
	
	/* 通知公告栏样式 */
	.notice-bar {
		display: flex;
		align-items: center;
		background-color: #fff;
		margin: 20rpx 30rpx 0rpx 20rpx;
		padding: 15rpx 20rpx;
		border-bottom: 1rpx solid #f7f7f7;
		// border-radius: 16rpx;
		// box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
	}
	
	.notice-icon-wrap {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 20rpx;
	}
	
	.notice-icon-bg {
		width: 90rpx;
		height: 90rpx;
		border-radius: 50%;
		background-color: #fff9e6;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.notice-content {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.notice-title {
		font-size: 32rpx;
		font-weight: 500;
		color: #333;
		margin-bottom: 2rpx;
	}
	
	.notice-desc {
		font-size: 24rpx;
		color: #999;
	}
	
	.notice-arrow {
		margin-left: 20rpx;
	}
	.product-msg-bar {
		margin-top: 16rpx;
	}
	.product-msg-bar .notice-icon-wrap {
		position: relative;
	}
	
	/* 客服悬浮按钮样式 */
	.customer-service-float-btn {
		position: fixed !important; /* 使用 !important 确保样式生效 */
		right: 22rpx;
		bottom: 200rpx; /* 在 tabbar 上方（tabbar 高度约 75px + margin 20px = 95px，加上安全区域约 100-120px，按钮底部留出 200rpx 约 100px 的空间） */
		width: 100rpx;
		height: 100rpx;
		background: #3d8af5;
		border: none;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 30rpx 0 rgba(65, 147, 255, 0.4), 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.15);
		z-index: 99999 !important; /* 使用更高的 z-index 并添加 !important 确保在所有内容之上 */
		transition: all 0.3s ease;
		pointer-events: auto; /* 确保可以点击 */
		transform: translateZ(0); /* 触发 GPU 加速，提升渲染层级 */
		-webkit-transform: translateZ(0);
	}
	
	.customer-service-float-btn:active {
		transform: scale(0.95) translateZ(0);
		-webkit-transform: scale(0.95) translateZ(0);
		box-shadow: 0 4rpx 20rpx 0 rgba(65, 147, 255, 0.3), 0 2rpx 8rpx 0 rgba(0, 0, 0, 0.1);
	}
	
	
	/* 适配不同设备的安全区域 */
	/* #ifdef APP-PLUS */
	.customer-service-float-btn {
		bottom: calc(200rpx + env(safe-area-inset-bottom));
	}
	/* #endif */
</style>