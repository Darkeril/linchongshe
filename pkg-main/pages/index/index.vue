<template>
	<view>
		<view class="msg-divider"></view>
		<!-- 状态栏占位（微信端由系统栏「爱宠社」占位，不重复预留） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;"></view>
		<!-- #endif -->
		<!-- 顶部主tab -->
		<view :style="{top: navBarTop}"
			style="position: fixed;width: 100%;height: 44px;background-color: #fff;z-index: 9999;display: flex;align-items: center;justify-content: center;">
			<u-tabs @click='changetabs' :current="actTab" :list="tabsList" lineWidth="40" lineColor="#3d8af5"
				:activeStyle="{
          color: '#16160e',
          fontSize: '35rpx',
          transform: 'scale(1.05)'
        }" :inactiveStyle="{
          color: '#606266',
          fontSize: '32rpx',
          transform: 'scale(1)'
        }" itemStyle="padding-left: 20px; padding-right: 20px; height: 42px;">
			</u-tabs>
			<view style="position: absolute;right: 30rpx;">
				<u-icon @click="goToSearch" name="search" color="#16160e" size="24"></u-icon>
			</view>
		</view>
		<view :style="{height: navPlaceholderHeight}" style="width: 100%;background-color:antiquewhite;"></view>

		<!-- 主swiper：支持左右滑动切换 -->
		<swiper :current="actTab" @change="onMainSwiperChange" :style="{height: notesHeight + 'px', marginTop: '5px'}" :duration="300">
			<!-- 关注页 -->
			<swiper-item>
				<follow-page :notes-height="notesHeight" :enablerefresh="enablerefresh"
					@goToUser="goToUser"
					@goToDetail="goToDetail"></follow-page>
			</swiper-item>

			<!-- 发现页 -->
			<swiper-item>
				<discover-page :notes-height="notesHeight" :enablerefresh="enablerefresh"
					@goToDetail="goToDetail" @goToUser="goToUser"></discover-page>
			</swiper-item>

			<!-- 附近页 -->
			<swiper-item>
				<nearby-page :notes-height="notesHeight" :enablerefresh="enablerefresh"
					:loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText"
					@cityNameChange="handleCityNameChange" @goToDetail="goToDetail"
					@goToUser="goToUser"></nearby-page>
			</swiper-item>
		</swiper>
        <custom-tabbar ref="tabbar" :current="0" @update:current="updateCurrentTab"></custom-tabbar>
	</view>
</template>

<script>
	// import CustomTabbar from '@/components/custom-tabbar/CustomTabBar.vue'
	import FollowPage from '@/components/index-pages/FollowPage.vue'
	import DiscoverPage from '@/components/index-pages/DiscoverPage.vue'
	import NearbyPage from '@/components/index-pages/NearbyPage.vue'
	
	import weixinNavBar from '@/utils/weixinNavBar.js'
	export default {
		mixins: [weixinNavBar],
		// components: {
		// 	CustomTabbar
		// },
		components: {
			FollowPage,
			DiscoverPage,
			NearbyPage
		},
		data() {
			return {
				statusBarHeight: 0,
				notesHeight: 0,
				actTab: 1,
				cityName: '附近',
				tabsList: [{
					name: '关注',
				}, {
					name: '发现',
				}, {
					name: '附近',
				}],
				loadingText: '加载中...',
				loadmoreText: '加载更多',
				nomoreText: '到底了',
				enablerefresh: true,
			}
		},
		methods: {
			// 反查城市并替换"附近"标签
			updateNearbyTabTitleByReverseGeocode(lat, lng) {
				if (!lat || !lng) return;
				// 使用腾讯地图/H5内置反地理或你已有的后端接口。这里先尝试 uni.reverseGeocoder（App/H5 支持），失败则忽略。
				// #ifdef APP-PLUS
				try {
					uni.reverseGeocoder({
						location: { latitude: lat, longitude: lng },
						success: (res) => {
							const city = res?.result?.addressComponent?.city || res?.result?.addressComponent?.province
							if (city) {
								// 确保触发响应式更新
								this.$set(this.tabsList, 2, { ...this.tabsList[2], name: city })
							}
						}
					})
				} catch(e) {}
				// #endif
				// #ifndef APP-PLUS
				// H5/小程序如无内置反地理，可接入你方后端反地理接口，此处占位：
				// this.fetchCityByServer(lat, lng).then(city => { if (city) this.$set(this.tabsList, 2, { ...this.tabsList[2], name: city }) })
				// #endif
			},
			updateCurrentTab(index) {
				console.log('切换到 tab:', index);
			},
			goToSearch() {
				uni.navigateTo({
					url: '/pkg-search/pages/searchPage/searchPage'
				})
			},
			goToUser(id) {
				uni.navigateTo({
					url: '/pkg-mine/pages/mine/otherMine?userId=' + id
				})
			},
			goToDetail(id, type) {
				console.log(type)
				const scene = this.actTab === 0 ? 'follow' : (this.actTab === 1 ? 'recommend' : 'other')
				if (type == 1) {
					// 图文
					uni.navigateTo({
						url: '/pkg-detail/pages/notesDetail/notesDetail?notesId=' + id + '&scene=' + scene
					})
				} else if (type == 2) {
					// 视频
					console.log('视频')
					uni.navigateTo({
						url: '/pkg-detail/pages/notesDetail/noteVideoD?notesId=' + id + '&scene=' + scene
					})
				}
			},
			changetabs(e) {
				let index = e.index;
				if (this.actTab === index) return; // 避免重复切换
				this.actTab = index;
			},
			onMainSwiperChange(e) {
				const index = e.detail.current;
				if (this.actTab === index) return;
				this.actTab = index;
			},
			handleCityNameChange(cityName) {
				if (cityName) {
					this.$set(this.tabsList, 2, { ...this.tabsList[2], name: cityName });
				}
			},
		},
		onLoad() {
			uni.getSystemInfo({
				success: (res) => {
					console.log(res);
					this.applyWeixinNavBar(res, 40);
					this.notesHeight = res.windowHeight - (this.statusBarHeight || 0) - 40;
					console.log(this.statusBarHeight);
				}
			})
			// 接收全局徽标更新事件
			uni.$on('im:setTabBadge', (count) => {
				const tabbar = this.$refs.tabbar
				if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(count)
			})
			// 🔧 修复：监听关注状态变化事件，当取消关注后刷新用户列表
			uni.$on('updateAttentionList', this.handleAttentionListUpdate);
			// 首次进入时读取本地缓存的未读数，避免空白
			try {
				const cached = uni.getStorageSync('im_total_unread') || 0
				const tabbar = this.$refs.tabbar
				if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(cached)
			} catch(e) {}
		},
		
		onUnload() {
			// 清理定时器
			if (this.tabSwitchTimer) {
				clearTimeout(this.tabSwitchTimer);
			}
			if (this.typeSwitchTimer) {
				clearTimeout(this.typeSwitchTimer);
			}
			// 🔧 修复：移除事件监听
			uni.$off('updateAttentionList', this.handleAttentionListUpdate);
		},
	};
</script>
<style lang="scss">
	.msg-divider {
		width: 100%;
		height: 1rpx;
		background: #eaeaea;
	}

	.follow-card {
		background: #fff;
		// border-radius: 20rpx;
		// margin: 24rpx 20rpx 0 20rpx;
		padding: 20rpx 20rpx 20rpx 20rpx;
		margin-bottom: 10rpx;
		box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.04);
		position: relative;
	}

	.follow-user {
		display: flex;
		align-items: center;
		margin-bottom: 12rpx;

		.avatar {
			width: 60rpx;
			height: 60rpx;
			border-radius: 50%;
			margin-right: 16rpx;
		}

		.nickname {
			font-size: 30rpx;
			color: #222;
			font-weight: 500;
			margin-right: auto;
		}

		.more-btn {
			margin-left: auto;
			color: #bbb;
		}
	}

	.follow-content {
		font-size: 30rpx;
		color: #222;
		margin-bottom: 12rpx;
		line-height: 1.5;
	}

	.follow-bottom-bar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 4rpx;

		.follow-location {
			display: flex;
			align-items: center;
			font-size: 22rpx;
			color: #888;
			max-width: 200rpx; // 限制整体最大宽度，防止撑满
			background-color: rgb(245, 246, 247);
			border-radius: 15px;
			padding: 5rpx 10rpx;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;

			u-icon {
				margin-right: 6rpx;
				flex-shrink: 0;
			}

			text {
				overflow: hidden;
				white-space: nowrap;
				text-overflow: ellipsis;
				display: inline-block;
				max-width: 90vw;
				vertical-align: middle;
			}
		}
	}

	.follow-action {
		display: flex;
		align-items: center;
		font-size: 26rpx;
		color: #888;

		u-icon {
			margin-right: 6rpx;
		}
	}

	.follow-video {
		position: relative;
		width: 70%;
		height: 350rpx;
		border-radius: 16rpx;
		margin-bottom: 12rpx;
		overflow: hidden;
		background-color: #000;

		.video-player {
			width: 100%;
			height: 100%;
			object-fit: cover;
		}

		.video-play-overlay {
			position: absolute;
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
			z-index: 2;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 100%;
			height: 100%;
			background-color: rgba(0, 0, 0, 0.1);
		}
	}

	.follow-single-img image {
		width: 70%;
		height: 350rpx;
		border-radius: 16rpx;
		margin-bottom: 12rpx;
	}

	.follow-multi-img {
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx;
		margin-bottom: 12rpx;

		image {
			height: 32%;
			width: 32%;
			aspect-ratio: 1/1;
			border-radius: 10rpx;
		}
	}

	.water-left,
	.water-right {
		width: 48%;
		margin: 20rpx auto;
	}

	.title {
		font-size: 30rpx;
		padding: 10rpx;
		margin-left: 5px;
		margin-bottom: -5px;
		color: #000000;
		overflow: hidden;
		text-overflow: ellipsis;
		word-break: break-all;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-height: 1.4em;
		max-height: 2.4em;
	}

	.note-username {
		margin-left: 10rpx;
		color: #16160e;
		font-size: 24rpx;
		line-height: 20px;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: calc(100% - 70px);
	}

	.look-views {
		display: flex;
		position: absolute;
		bottom: 135rpx;
		left: 8rpx;
		color: #ffffff;
		background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 3rpx 10rpx;
		border-radius: 50rpx;
		font-size: 22rpx;
		max-width: 200rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.video-play {
		position: absolute;
		top: 5rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 10rpx;
		border-radius: 50%;
	}
	
	.live-badge {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 10rpx;
		border-radius: 50%;
	}

	.water-left,
	.water-right {
		width: 50%;
		margin: -8rpx auto;
	}

	.note-card {
		background: #fff;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
		margin-top: 5rpx;
		margin-bottom: 10rpx;
		margin-left: 8rpx;
		margin-right: 8rpx;
		overflow: hidden;
		position: relative;
		border: 1rpx solid #f0f0f0;
	}

	page,
	.page-bg {
		background: #f5f5f5;
	}
</style>