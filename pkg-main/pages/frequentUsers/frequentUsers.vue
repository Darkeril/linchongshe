<template>
	<view class="frequent-users-page">
		<!-- 状态栏占位（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px', backgroundColor: '#fff'}" class="status-bar-placeholder"></view>
		<!-- #endif -->
		<!-- 头部导航（微信小程序端使用系统导航栏标题，避免出现两个标题栏） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">常看的人</view>
			<view class="header-right"></view>
		</view>
		<!-- 导航栏占位，避免内容被遮挡 -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 用户头像列表 -->
		<view v-if="usersList.length > 0" class="users-avatar-list">
			<scroll-view 
				scroll-x 
				class="avatar-scroll" 
				:scroll-left="scrollLeft"
				:scroll-into-view="scrollIntoView"
				show-scrollbar="false"
			>
				<view class="avatar-container">
				<view 
					v-for="(user, index) in usersList" 
					:key="index"
					:id="'avatar-' + user.userId"
					class="avatar-item" 
					:class="{ 'active': currentIndex === index }"
					@click="switchToUser(index)"
				>
						<view class="avatar-wrapper">
							<image 
								class="avatar-img" 
								:src="user.avatarUrl || user.avatar" 
								mode="aspectFill"
							></image>
							<view v-if="currentIndex === index" class="active-indicator">
								<u-icon name="checkmark" size="12" color="#fff"></u-icon>
							</view>
						</view>
						<text class="avatar-name">{{ user.nickname || user.userName || '用户' }}</text>
					</view>
				</view>
			</scroll-view>
		</view>
		
		<!-- 用户内容区域（左右滑动切换） -->
		<swiper 
			class="content-swiper"
			:current="currentIndex"
			@change="onSwiperChange"
			:duration="300"
			:style="{height: swiperHeight + 'px'}"
		>
			<swiper-item v-for="(user, index) in usersList" :key="index">
				<scroll-view 
					scroll-y 
					class="user-content-scroll"
					:style="{height: swiperHeight + 'px'}"
					@scrolltolower="loadMoreUserData(index)"
					:refresher-enabled="true"
					@refresherrefresh="onRefreshUserData(index)"
					:refresher-triggered="getRefreshingStatus(index)"
				>
					<!-- 用户动态内容 -->
					<view v-if="userData[index] && userData[index].list && userData[index].list.length > 0" class="content-grid">
						<view 
							v-for="(item, itemIndex) in userData[index].list" 
							:key="itemIndex"
							class="content-item"
							@click="goToDetail(item.nid || item.id, item.notesType)"
						>
							<!-- 图片内容 -->
							<image 
								v-if="item.imgList && item.imgList.length > 0 && item.notesType != 2"
								:src="item.imgList[0]" 
								mode="aspectFill"
								class="content-image"
							></image>
							<!-- 视频内容 -->
							<view v-else-if="item.notesType == 2 && (item.coverPicture || (item.imgList && item.imgList.length > 0))" class="video-wrapper">
								<image 
									:src="item.coverPicture || item.imgList[0]" 
									mode="aspectFill"
									class="content-image"
								></image>
								<!-- 视频标识：右上角小圆形播放图标 -->
								<view class="video-play">
									<u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
								</view>
							</view>
							<!-- 底部信息 -->
							<view class="item-info">
								<text class="item-caption" v-if="item.content">{{ formatCaption(item.content) }}</text>
								<view class="item-meta">
									<view class="item-user-section">
										<image 
											class="item-user-avatar" 
											:src="item.avatarUrl || user.avatarUrl || user.avatar || '/static/default-avatar.png'" 
											mode="aspectFill"
										></image>
										<view class="item-user-text">
											<text class="item-username">{{ item.nickname || user.nickname || user.userName || '用户' }}</text>
											<text class="item-time" v-if="item.time || item.createTime || item.updateTime">{{ formatTime(item.time || item.createTime || item.updateTime) }}</text>
										</view>
									</view>
									<view class="item-like">
										<u-icon 
											:name="item.isLike ? 'heart-fill' : 'heart'" 
											:color="item.isLike ? '#ff4d4f' : '#fff'" 
											size="14"
										></u-icon>
										<text>{{ item.notesLikeNum || 0 }}</text>
									</view>
								</view>
							</view>
						</view>
					</view>
					
					<!-- 空状态 -->
					<view v-else-if="userData[index] && !userData[index].loading" class="empty-content">
						<u-empty mode="data" text="该用户暂无动态"></u-empty>
					</view>
					
					<!-- 加载更多 -->
					<view v-if="userData[index] && userData[index].list && userData[index].list.length > 0" class="load-more">
						<u-loadmore 
							:status="userData[index].status || 'loadmore'" 
							:loading-text="loadingText"
							:loadmore-text="loadmoreText"
							:nomore-text="nomoreText"
						></u-loadmore>
					</view>
				</scroll-view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
	import { getAttentionUserNotes } from '@/apis/notes_service.js'
	import { getFollowUser } from '@/apis/user_service.js'
	import { mapNoteItem } from '@/utils/noteUtils.js'
	import { weChatTimeFormat } from '@/utils/util.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				swiperHeight: 0,
				currentIndex: 0,
				usersList: [],
				userData: {}, // 存储每个用户的数据 { index: { list: [], page: 1, status: 'loadmore', loading: false, refreshing: false } }
				scrollLeft: 0,
				scrollIntoView: '',
				pageSize: 10,
				loadingText: '加载中...',
				loadmoreText: '加载更多',
				nomoreText: '没有更多了'
			}
		},
		onLoad(options) {
			// 设置状态栏样式为白色背景
			// #ifdef APP-PLUS
			plus.navigator.setStatusBarStyle('dark')
			// #endif
			// #ifdef H5
			// H5端通过CSS控制
			// #endif
			// #ifdef MP-WEIXIN
			wx.setNavigationBarColor({
				frontColor: '#000000',
				backgroundColor: '#ffffff'
			})
			// #endif
			
			// 获取状态栏高度
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
					this.$nextTick(() => {
						let headerHeight = 44
						// 微信小程序端使用系统导航栏，不渲染自定义 header，这里不再扣除 44px
						// #ifdef MP-WEIXIN
						headerHeight = 0
						// #endif
						const avatarListHeight = uni.upx2px(148)
						this.swiperHeight = res.screenHeight - (this.statusBarHeight || 0) - headerHeight - avatarListHeight
					})
				}
			})
			
			// 从参数中获取用户列表
			if (options.users) {
				try {
					this.usersList = JSON.parse(decodeURIComponent(options.users))
				} catch (e) {
					console.error('解析用户列表失败:', e)
					this.usersList = []
				}
			}
			
			// 如果有初始选中用户索引
			if (options.index) {
				this.currentIndex = parseInt(options.index) || 0
			}
			
			// 初始化第一个用户的数据
			if (this.usersList.length > 0) {
				this.loadUserData(this.currentIndex)
			}
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			
			// 获取刷新状态
			getRefreshingStatus(index) {
				return this.userData[index] && this.userData[index].refreshing ? this.userData[index].refreshing : false
			},
			
			// 切换到指定用户
			switchToUser(index) {
				if (index === this.currentIndex) return
				this.currentIndex = index
				this.scrollToAvatar(index)
				// 如果该用户数据未加载，则加载
				if (!this.userData[index] || !this.userData[index].list) {
					this.loadUserData(index)
				}
			},
			
			// Swiper切换事件
			onSwiperChange(e) {
				const newIndex = e.detail.current
				if (newIndex !== this.currentIndex) {
					this.currentIndex = newIndex
					this.scrollToAvatar(newIndex)
					// 如果该用户数据未加载，则加载
					if (!this.userData[newIndex] || !this.userData[newIndex].list) {
						this.loadUserData(newIndex)
					}
				}
			},
			
			// 滚动到指定头像位置（仅在不在可视范围内时滚动）
			scrollToAvatar(index) {
				if (!this.usersList[index]) return
				const userId = this.usersList[index].userId
				const avatarId = 'avatar-' + userId
				
				// 使用 uni.createSelectorQuery 检查头像是否在可视范围内
				this.$nextTick(() => {
					const query = uni.createSelectorQuery().in(this)
					query.select(`#${avatarId}`).boundingClientRect()
					query.select('.avatar-scroll').boundingClientRect()
					query.select('.avatar-scroll').scrollOffset()
					query.exec((res) => {
						if (!res || !res[0] || !res[1] || !res[2]) {
							// 如果查询失败，使用默认滚动
							this.scrollIntoView = avatarId
							setTimeout(() => {
								this.scrollIntoView = ''
							}, 300)
							return
						}
						
						const avatarRect = res[0] // 头像位置
						const scrollRect = res[1] // scroll-view 位置
						const scrollOffset = res[2] // scroll-view 滚动位置
						
						// 计算头像相对于 scroll-view 的位置
						const avatarLeft = avatarRect.left - scrollRect.left + scrollOffset.scrollLeft
						const avatarRight = avatarLeft + avatarRect.width
						const scrollLeft = scrollOffset.scrollLeft
						const scrollRight = scrollLeft + scrollRect.width
						
						// 判断头像是否在可视范围内（留一些边距）
						const margin = 20 // 边距
						const isVisible = avatarLeft >= scrollLeft - margin && avatarRight <= scrollRight + margin
						
						// 如果不在可视范围内，才滚动
						if (!isVisible) {
							this.scrollIntoView = avatarId
							setTimeout(() => {
								this.scrollIntoView = ''
							}, 300)
						}
					})
				})
			},
			
			// 加载用户数据
			async loadUserData(index, reset = false) {
				const user = this.usersList[index]
				if (!user || !user.userId) {
					console.log('用户数据无效:', index, user)
					return
				}
				
				// 初始化用户数据
				if (!this.userData[index]) {
					this.$set(this.userData, index, {
						list: [],
						page: 1,
						status: 'loadmore',
						loading: false,
						refreshing: false
					})
				}
				
				const userDataItem = this.userData[index]
				
				// 如果正在加载或没有更多，且不是重置，则返回
				if (userDataItem.loading || (userDataItem.status === 'nomore' && !reset)) {
					return
				}
				
				userDataItem.loading = true
				if (reset) {
					userDataItem.page = 1
					userDataItem.status = 'loadmore'
					userDataItem.list = []
				}
				
				try {
					const res = await getAttentionUserNotes({
						page: userDataItem.page,
						pageSize: this.pageSize,
						userId: user.userId
					})
					
				if (res.code === 200) {
					const notesData = res.data.notes || res.data.records || res.data || []
					const records = notesData.map(item => {
						const mapped = mapNoteItem(item)
						// 调试：检查映射后的数据
						if (!mapped.nid && !mapped.id) {
							console.warn('笔记ID缺失:', { original: item, mapped })
						}
						return mapped
					})
					
					if (reset) {
						userDataItem.list = records
					} else {
						userDataItem.list = [...userDataItem.list, ...records]
					}
						
					userDataItem.page += 1
					userDataItem.status = records.length < this.pageSize ? 'nomore' : 'loadmore'
					} else {
						userDataItem.status = 'nomore'
					}
				} catch (error) {
					console.error('加载用户数据失败:', error)
					userDataItem.status = 'nomore'
				} finally {
					userDataItem.loading = false
					userDataItem.refreshing = false
				}
			},
			
			// 加载更多
			loadMoreUserData(index) {
				const userDataItem = this.userData[index]
				if (userDataItem && userDataItem.status === 'loadmore' && !userDataItem.loading) {
					this.loadUserData(index)
				}
			},
			
			// 获取关注用户列表（顶部用户列表）
			async getFollowUsersList() {
				try {
					console.log('🔄 常看的人页面：开始调用getFollowUser接口...');
					// 🔧 修复：添加时间戳参数，禁用缓存，确保下拉刷新时获取最新数据
					const res = await getFollowUser({
						_t: Date.now()
					});
					console.log('📥 常看的人页面：getFollowUser接口响应:', res);
					if (res.code === 200 && res.data) {
						console.log('✅ 常看的人页面：关注用户列表API响应:', res);
						// 直接使用后端返回的去重数据，映射字段名
						const newUsersList = res.data.map(user => ({
							userId: user.uid,
							nickname: user.username || '用户',
							avatarUrl: user.avatar,
							hasNewContent: user.tag === true, // 使用后端返回的tag字段控制红点显示
							hasContent: user.hasContent === true // 使用后端返回的hasContent字段判断是否有内容
						}));
						
						// 🔧 修复：更新用户列表，但保持当前选中的用户索引（如果用户还在列表中）
						const currentUserId = this.usersList[this.currentIndex]?.userId;
						const newCurrentIndex = newUsersList.findIndex(user => user.userId === currentUserId);
						
						// 记录是否需要切换用户
						let needSwitchUser = false;
						
						// 如果当前用户还在新列表中，保持索引；否则切换到第一个用户
						if (newCurrentIndex >= 0) {
							this.currentIndex = newCurrentIndex;
						} else if (newUsersList.length > 0) {
							// 如果当前用户不在新列表中，且新列表不为空，切换到第一个用户
							this.currentIndex = 0;
							needSwitchUser = true;
						} else {
							// 如果新列表为空，保持当前索引（虽然列表为空）
							this.currentIndex = 0;
						}
						
						this.usersList = newUsersList;
						console.log('✅ 常看的人页面：关注用户列表更新成功:', this.usersList.length, '当前索引:', this.currentIndex);
						
						// 🔧 修复：如果切换了用户，需要加载新用户的数据，避免空白页面
						if (needSwitchUser && this.currentIndex >= 0 && this.currentIndex < this.usersList.length) {
							// 清空旧用户的数据
							if (this.userData[this.currentIndex]) {
								this.userData[this.currentIndex].list = [];
								this.userData[this.currentIndex].page = 1;
							}
							// 加载新用户的数据
							console.log('🔄 切换到新用户，开始加载数据，索引:', this.currentIndex);
							await this.loadUserData(this.currentIndex, true);
						} else if (this.currentIndex >= 0 && this.currentIndex < this.usersList.length) {
							// 如果当前用户还在列表中，清空数据准备重新加载
							if (this.userData[this.currentIndex]) {
								this.userData[this.currentIndex].list = [];
								this.userData[this.currentIndex].page = 1;
							}
						}
					} else {
						console.log('⚠️ 常看的人页面：API响应异常:', res);
					}
				} catch (error) {
					console.error('❌ 常看的人页面：获取关注用户列表失败:', error);
				}
			},
			
			// 下拉刷新
			async onRefreshUserData(index) {
				const userDataItem = this.userData[index]
				if (userDataItem) {
					userDataItem.refreshing = true
					// 🔧 修复：下拉刷新时，先更新用户列表，再刷新用户内容
					try {
						await this.getFollowUsersList();
						console.log('✅ 常看的人页面：用户列表更新完成');
					} catch (error) {
						console.error('❌ 常看的人页面：更新用户列表失败:', error);
					}
					// 刷新当前用户的内容数据
					await this.loadUserData(index, true)
				}
			},
			
			// 跳转到详情页
			goToDetail(nid, notesType) {
				if (!nid) {
					console.error('笔记ID为空，无法跳转', { nid, notesType })
					uni.showToast({
						title: '笔记ID缺失',
						icon: 'none'
					})
					return
				}
				// 使用 notesId 参数名，与其他页面保持一致
				if (notesType == 2) {
					uni.navigateTo({
						url: `/pkg-detail/pages/notesDetail/noteVideoD?notesId=${nid}`
					})
				} else {
					uni.navigateTo({
						url: `/pkg-detail/pages/notesDetail/notesDetail?notesId=${nid}`
					})
				}
			},
			
			// 格式化标题（去除HTML标签，截取前20字符）
			formatCaption(content) {
				if (!content) return ''
				// 去除HTML标签
				const text = content.replace(/<[^>]+>/g, '')
				// 截取前20字符
				return text.length > 20 ? text.substring(0, 20) + '...' : text
			},
			
			// 格式化时间
			formatTime(timeValue) {
				if (!timeValue) return ''
				
				// 获取时间戳
				let timestamp = 0
				if (typeof timeValue === 'number') {
					timestamp = timeValue
				} else if (typeof timeValue === 'string' && /^\d+$/.test(timeValue)) {
					timestamp = parseInt(timeValue)
				} else if (typeof timeValue === 'string' && (timeValue.includes('-') || timeValue.includes('/'))) {
					// 如果已经是格式化后的字符串，尝试解析
					try {
						timestamp = new Date(timeValue).getTime()
					} catch (e) {
						return timeValue
					}
				} else {
					try {
						timestamp = new Date(timeValue).getTime()
					} catch (e) {
						return ''
					}
				}
				
				if (!timestamp || isNaN(timestamp)) return ''
				
				const now = new Date()
				const targetDate = new Date(timestamp)
				const diffTime = now.getTime() - timestamp
				const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
				const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
				
				// 24小时内：显示"X小时前"
				if (diffHours >= 1 && diffHours < 24) {
					return `${diffHours}小时前`
				}
				
				// 1-7天前：显示"X天前"
				if (diffDays >= 1 && diffDays <= 7) {
					return `${diffDays}天前`
				}
				
				// 超过7天
				const currentYear = now.getFullYear()
				const targetYear = targetDate.getFullYear()
				const month = String(targetDate.getMonth() + 1).padStart(2, '0')
				const day = String(targetDate.getDate()).padStart(2, '0')
				
				// 如果是今年：显示"月-日"
				if (targetYear === currentYear) {
					return `${month}-${day}`
				}
				
				// 不是今年：显示"年-月-日"
				const year = targetDate.getFullYear()
				return `${year}-${month}-${day}`
			}
		}
	}
</script>

<style lang="scss" scoped>
	.frequent-users-page {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f5f5;
	}
	
	.status-bar-placeholder {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		width: 100%;
		z-index: 999;
		background-color: #fff;
	}
	
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 44px;
		background-color: #fff;
		padding: 0 15px;
		border-bottom: 1px solid #eee;
		position: fixed;
		left: 0;
		right: 0;
		width: 100%;
		z-index: 998;
		
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
	}
	
	.users-avatar-list {
		background-color: #fff;
		padding: 10rpx 0;
		border-bottom: 1px solid #eee;
	}
	
	.avatar-scroll {
		width: 100%;
		white-space: nowrap;
	}
	
	.avatar-container {
		display: flex;
		padding: 0 20rpx;
	}
	
	.avatar-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-right: 30rpx;
		flex-shrink: 0;
		opacity: 0.5;
		filter: brightness(0.7);
		transition: all 0.3s ease;
		
		.avatar-wrapper {
			position: relative;
			margin-bottom: 8rpx;
		}
		
		.avatar-img {
			width: 90rpx;
			height: 90rpx;
			border-radius: 50%;
			border: 2rpx solid #f0f0f0;
		}
		
		.active-indicator {
			position: absolute;
			bottom: 0;
			right: 0;
			width: 24rpx;
			height: 24rpx;
			background-color: #1890ff;
			border-radius: 50%;
			border: 2rpx solid #fff;
			display: flex;
			align-items: center;
			justify-content: center;
		}
		
		&.active {
			opacity: 1;
			filter: brightness(1);
			
			.avatar-img {
				border-color: #1890ff;
				border-width: 3rpx;
			}
		}
		
		.avatar-name {
			font-size: 20rpx;
			color: #333;
			text-align: center;
			max-width: 80rpx;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
		
		&.active .avatar-name {
			color: #1890ff;
			font-weight: 600;
		}
	}
	
	.content-swiper {
		flex: 1;
		width: 100%;
		overflow: hidden;
	}
	
	.user-content-scroll {
		width: 100%;
		background-color: #f5f5f5;
		box-sizing: border-box;
	}
	
	.content-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 10rpx;
		padding: 10rpx 10rpx 0 10rpx; /* 移除底部 padding，让内容显示到底部 */
	}
	
	.content-item {
		background-color: #fff;
		border-radius: 12rpx;
		overflow: hidden;
		position: relative;
		aspect-ratio: 3/4;
	}
	
	.content-image {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
	
	.video-wrapper {
		position: relative;
		width: 100%;
		height: 100%;
	}
	
	.video-play {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		padding: 10rpx;
		border-radius: 50%;
		z-index: 2;
	}
	
	.item-info {
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
		padding: 20rpx;
		color: #fff;
	}
	
	.item-caption {
		font-size: 24rpx;
		color: #fff;
		display: block;
		margin-bottom: 8rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	.item-meta {
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 20rpx;
		color: #fff;
		opacity: 0.9;
	}
	
	.item-user-section {
		display: flex;
		align-items: center;
		flex: 1;
		min-width: 0; /* 防止内容溢出 */
	}
	
	.item-user-avatar {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		margin-right: 12rpx;
		border: 1rpx solid rgba(255, 255, 255, 0.3);
		flex-shrink: 0;
		align-self: center; /* 垂直居中 */
	}
	
	.item-user-text {
		display: flex;
		flex-direction: column;
		flex: 1;
		min-width: 0;
	}
	
	.item-username {
		font-size: 20rpx;
		color: #fff;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		line-height: 1.2;
	}
	
	.item-time {
		font-size: 18rpx;
		opacity: 0.8;
		color: #fff;
		white-space: nowrap;
		line-height: 1.2;
		margin-top: 2rpx;
	}
	
	.item-like {
		display: flex;
		align-items: center;
		gap: 4rpx;
	}
	
	.empty-content {
		padding: 100rpx 0;
	}
	
	.load-more {
		padding: 20rpx 0;
	}
</style>

