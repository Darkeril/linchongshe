<template>
	<view class="praise-collect-container">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航（微信端系统栏已显示标题，不重复展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">收到的赞和收藏</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端已无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<view class="list-container">
			<block v-if="praiseAndCollectList.length > 0">
				<view v-for="(item,index) in praiseAndCollectList" 
					:key="index" 
					class="list-item"
					:class="{'no-border': index === praiseAndCollectList.length - 1}">
					<!-- 左侧头像 -->
					<image class="avatar" 
						:src="item.avatarUrl" 
						@click="goToOtherMine(item.userId)" 
						mode="aspectFill" />
					
					<!-- 中间内容区 -->
					<view class="content" @click="goToNotesDetail(item.notesId, item.notesType)">
						<!-- 用户名 -->
						<view class="username">{{item.userName}}</view>
						<!-- 操作信息 -->
						<view class="action-info">
							<text class="action-text">{{item.actionText}}</text>
							<text class="note-tag">笔记</text>
							<text class="time">{{timeFormat(item.time)}}</text>
						</view>
					</view>
					
					<!-- 右侧预览图 -->
					<image class="preview-image" 
						:src="item.notesCoverPicture" 
						@click="goToNotesDetail(item.notesId, item.notesType)"
						mode="aspectFill" />
				</view>
			</block>

			<!-- 无数据提示 -->
			<view v-else-if="!isLoading" class="empty-tip">
				暂无数据
			</view>

			<!-- 加载更多 -->
			<view v-if="praiseAndCollectList.length > 0" class="loadmore">
				<u-loadmore :status="status" loadingIcon="spinner" line />
			</view>
		</view>
	</view>
</template>

<script>
import { getNoticeLikeOrCollection } from '@/apis/mesasage_apis.js'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			praiseAndCollectList: [],
			page: 1,
			pageSize: 10,
			status: "loading",
			isLoading: false
		}
	},
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		timeFormat(time) {
			const currentDate = new Date()
			const inputDate = new Date(time)
			const timeDiff = currentDate - inputDate

			// 24小时内显示具体时间
			if (timeDiff < 24 * 60 * 60 * 1000) {
				const hours = inputDate.getHours().toString().padStart(2, '0')
				const minutes = inputDate.getMinutes().toString().padStart(2, '0')
				return `${hours}:${minutes}`
			}
			
			// 两个月内显示月日
			const twoMonthsInMillis = 2 * 30 * 24 * 60 * 60 * 1000
			if (timeDiff < twoMonthsInMillis) {
				const month = (inputDate.getMonth() + 1).toString().padStart(2, '0')
				const day = inputDate.getDate().toString().padStart(2, '0')
				return `${month}-${day}`
			}
			
			// 两个月前显示年月日
			const year = inputDate.getFullYear()
			const month = (inputDate.getMonth() + 1).toString().padStart(2, '0')
			const day = inputDate.getDate().toString().padStart(2, '0')
			return `${year}-${month}-${day}`
		},

		goToOtherMine(userId) {
			const currentUserId = uni.getStorageSync('userInfo').id;
			// 如果点击的是当前登录用户的头像，跳转到mine页面
			if (userId == currentUserId) {
				uni.navigateTo({
					url: '/pkg-main/pages/mine/mine'
				})
			} else {
				// 否则跳转到otherMine页面
				uni.navigateTo({
					url: `/pkg-mine/pages/mine/otherMine?userId=${userId}`
				})
			}
		},

		goToNotesDetail(notesId, notesType) {
			const url = notesType === '1' ? 
				`/pkg-detail/pages/notesDetail/noteVideoD?notesId=${notesId}` :
				`/pkg-detail/pages/notesDetail/notesDetail?notesId=${notesId}`
			uni.navigateTo({ url })
		},

		async getPraiseAndCollectList() {
			if (this.isLoading || this.status === "nomore") return

			this.isLoading = true
			try {
				const res = await getNoticeLikeOrCollection({
					page: this.page,
					pageSize: this.pageSize
				})

				if (res.code === 200) {
					const list = res.data.records.map(item => ({
						id: item.itemId,
						notesId: item.itemId,
						notesCoverPicture: item.itemCover,
						userId: item.uid,
						userName: item.username,
						avatarUrl: item.avatar,
						actionText: item.type === 1 ? '赞了你的' : '收藏了你的',
						time: item.time,
						notesType: '0'
					}))

					this.praiseAndCollectList = this.page === 1 ? 
						list : [...this.praiseAndCollectList, ...list]

					this.status = list.length < this.pageSize || res.data.current >= res.data.pages ? 
						"nomore" : "loading"
					
					if (this.status === "loading") this.page++
				} else {
					throw new Error(res.message || '请求失败')
				}
			} catch (e) {
				console.error('获取赞和收藏列表失败', e)
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				})
			} finally {
				this.isLoading = false
			}
		},

		resetList() {
			this.page = 1
			this.praiseAndCollectList = []
			this.status = "loading"
			this.isLoading = false
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
		
		// 获取状态栏高度
		uni.getSystemInfo({
			success: (res) => {
				this.applyWeixinNavBar(res, 44)
			}
		})
		
		this.getPraiseAndCollectList()
	},

	onReachBottom() {
		if (!this.isLoading && this.status !== "nomore") {
			this.getPraiseAndCollectList()
		}
	},

	async onPullDownRefresh() {
		try {
			this.resetList()
			await this.getPraiseAndCollectList()
		} catch (e) {
			console.error('下拉刷新失败', e)
			uni.showToast({
				title: '刷新失败',
				icon: 'none'
			})
		} finally {
			uni.stopPullDownRefresh()
		}
	}
}
</script>

<style lang="scss">
.praise-collect-container {
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 44px;
		padding: 0 15px;
		background-color: #fff;
		border-bottom: 1px solid #eee;
		
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
	
	.list-container {
		padding: 30rpx;
		margin-top: -30rpx;
	}

	.list-item {
		display: flex;
		align-items: center;
		padding: 25rpx 0;
		border-bottom: 1rpx solid #f3f3f2;

		&.no-border {
			border-bottom: none;
		}
	}

	.avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
	}

	.content {
		flex: 1;
		margin-left: 20rpx;
	}

	.username {
		font-size: 30rpx;
		color: #2b2b2b;
		margin-bottom: 10rpx;
	}

	.action-info {
		display: flex;
		align-items: center;
		font-size: 26rpx;
		color: #999;
	}

	.action-text {
		color: #999;
	}

	.note-tag {
		display: inline-block;
		padding: 2rpx 12rpx;
		border-radius: 30rpx;
		font-size: 24rpx;
		margin: 0 6rpx;
		color: #007AFF;
		background-color: rgba(0, 122, 255, 0.1);
	}

	.time {
		margin-left: 50rpx;
	}

	.preview-image {
		width: 120rpx;
		height: 120rpx;
		border-radius: 8rpx;
		margin-left: 20rpx;
	}

	.empty-tip {
		text-align: center;
		color: #999;
		padding: 30rpx;
	}

	.loadmore {
		margin-top: 70rpx;
	}
}
</style>