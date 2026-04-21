<template>
	<view class="info" :class="{ 'info-fixed': !inline }" :style="infoRootStyle">
		<template v-if="inline">
			<image v-if="userInfo.homePageBackground" class="info-bg-image" :src="userInfo.homePageBackground"
				mode="aspectFill" />
			<view class="filter filter-overlay"></view>
			<view class="info-content">
				<view :style="{ height: headerPlaceholderHeight }"></view>
				<view class="top-main">
					<view class="userinfo-main">
						<image v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" mode="aspectFill"
							@click="handleViewAvatar"></image>
						<view class="userinfo-main-right">
							<text :decode="true">{{ userInfo.nickname }}</text>
							<text :decode="true">爱宠社号：{{ userInfo.hsId }}</text>
							<text :decode="true">IP属地：{{ formatProvince(userInfo.province) }}</text>
						</view>
					</view>
					<view class="introduction">
						{{ userInfo.selfIntroduction == null ? '点击这里，填写简介' : userInfo.selfIntroduction }}
					</view>
					<view v-if="userInfo.tags && userInfo.tags.length"
						style="display: flex; gap: 16rpx; margin: 25rpx 0">
						<view v-for="(tag, idx) in userInfo.tags" :key="idx" style="
              background: hsla(0, 0%, 100%, 0.2);
              color: #fff;
              border-radius: 30rpx;
              padding: 6rpx 10rpx;
              font-size: 24rpx;
              line-height: 32rpx;
            ">
							{{ tag }}
						</view>
					</view>
					<view style="display: flex; width: 100%; margin-top: 20rpx">
						<view style="width: 40%; display: flex; justify-content: space-around">
							<view class="guanzhu" @click="handleAttentionAndFans(0)">
								<view>{{ userInfo.attentionNum }}</view>
								<view style="color: #e5e4e6">关注</view>
							</view>
							<view class="guanzhu" @click="handleAttentionAndFans(1)">
								<view>{{ userInfo.fansNum }}</view>
								<view style="color: #e5e4e6">粉丝</view>
							</view>
							<view class="guanzhu" @click="handlePraiseAndCollect">
								<view>{{ count.praiseCount + count.collectCount }}</view>
								<view style="color: #e5e4e6">获赞与收藏</view>
							</view>
						</view>
						<view style="width: 50%; margin-left: 10%; margin-bottom: 1%; display: flex; align-items: flex-end; justify-content: space-around">
							<view class="tag1" @click="handleEditData">编辑资料</view>
							<view class="tag2" @click="handleSetting">
								<u-icon name="setting" size="23" color="#fff"></u-icon>
							</view>
						</view>
					</view>
					<scroll-view scroll-x="true" enable-flex class="function-scroll" show-scrollbar="false">
						<view class="function-container">
							<view class="looked" @click="handleAIAssistant">
								<view style="display: flex">
									<u-icon name="chat" color="#ffffff" size="18"></u-icon>
									<text style="color: #ffffff; font-size: 27rpx; margin-left: 5rpx">AI助手</text>
								</view>
								<text style="font-size: 25rpx; color: #afafb0; margin-left: 5rpx">智能对话助手</text>
							</view>
							<view class="looked" @click="handleShoppingCart">
								<view style="display: flex">
									<u-icon name="bag" color="#ffffff" size="18"></u-icon>
									<text style="color: #ffffff; font-size: 27rpx; margin-left: 5rpx">心愿单</text>
								</view>
								<text style="font-size: 25rpx; color: #afafb0; margin-left: 5rpx">查看收藏好物</text>
							</view>
							<view class="looked" @click="handleOrders">
								<view style="display: flex">
									<u-icon name="order" color="#ffffff" size="18"></u-icon>
									<text style="color: #ffffff; font-size: 27rpx; margin-left: 5rpx">订单</text>
								</view>
								<text style="font-size: 25rpx; color: #afafb0; margin-left: 5rpx">我的订单</text>
							</view>
							<view class="looked" @click="handleBrowseRecord">
								<view style="display: flex">
									<u-icon name="clock" color="#ffffff" size="18"></u-icon>
									<text style="color: #ffffff; font-size: 27rpx; margin-left: 5rpx">浏览记录</text>
								</view>
								<text style="font-size: 25rpx; color: #afafb0; margin-left: 5rpx">看过的笔记</text>
							</view>
							<view class="looked" @click="handleGroupChat">
								<view style="display: flex">
									<u-icon name="chat" color="#ffffff" size="18"></u-icon>
									<text style="color: #ffffff; font-size: 27rpx; margin-left: 5rpx">群聊</text>
								</view>
								<text style="font-size: 25rpx; color: #afafb0; margin-left: 5rpx">我的群聊</text>
							</view>
						</view>
					</scroll-view>
				</view>
			</view>
		</template>
		<!-- 非 inline：图标始终显示，灰色背景仅吸顶时显示 -->
		<template v-else>
			<view v-if="showAvatar" class="filter" :style="{
          backgroundImage: userInfo.homePageBackground
            ? 'url(' + userInfo.homePageBackground + ')'
            : 'none'
        }"></view>
			<view class="status-bar" :style="{ height: '0px', backgroundColor: 'transparent' }"></view>
			<view class="navigation-bar compact-bar" :style="{
        height: '65px',
        top: (displayStatusBarNum - 50) + 'px',
        backgroundColor: showAvatar ? 'rgba(128,128,128,1)' : 'transparent'
      }">
				<view class="compact-bar-left" @click="handleOpenMore">
					<u-icon name="list" size="28" color="#fff" @click="handleOpenMore"></u-icon>
				</view>
				<view class="compact-bar-center">
					<u-transition :show="showAvatar && !!userInfo.avatarUrl" mode="fade-up">
						<image v-if="userInfo.avatarUrl"
							:style="{ height: (displayStatusBarNum * 0.8) + 'px', width: (displayStatusBarNum * 0.8) + 'px' }"
							:src="userInfo.avatarUrl" class="compact-bar-avatar" mode="aspectFill"></image>
					</u-transition>
				</view>
				<view class="compact-bar-right">
					<u-icon name="scan" size="28" color="#fff" @click="handleScan" class="compact-bar-icon compact-bar-icon-first"></u-icon>
					<u-icon name="share" size="28" color="#fff" class="compact-bar-icon"></u-icon>
				</view>
			</view>
		</template>
	</view>
</template>

<script>
	export default {
		name: 'MineHeader',
		props: {
			userInfo: {
				type: Object,
				default: () => ({})
			},
			count: {
				type: Object,
				default: () => ({})
			},
			statusBarHeight: {
				type: [Number, String],
				default: 0
			},
			navigationBarHeight: {
				type: [Number, String],
				default: ''
			},
			iconHeight: {
				type: [Number, String],
				default: ''
			},
			opacity: {
				type: Number,
				default: 0
			},
			showAvatar: {
				type: Boolean,
				default: false
			},
			inline: {
				type: Boolean,
				default: false
			}
		},
		computed: {
			displayStatusBarNum() {
				const n = parseFloat(this.statusBarHeight) || 0
				return n
			},
			statusBarBlockHeight() {
				return this.displayStatusBarNum ? this.displayStatusBarNum + 100 + 'px' : '0px'
			},
			navBarTopOffset() {
				return this.displayStatusBarNum ? this.displayStatusBarNum + 10 + 'px' : '0px'
			},
			headerPlaceholderHeight() {
				// 微信端系统栏已占位，仅留少量间距即可
				if (!this.displayStatusBarNum) return '10px'
				return parseFloat(this.statusBarHeight) + 10 + this.displayStatusBarNum * 0.6 + 'px'
			},
			// 微信小程序端 style 需要字符串，不能返回对象
			infoRootStyle() {
				if (!this.inline || !this.userInfo || !this.userInfo.homePageBackground) return ''
				const url = String(this.userInfo.homePageBackground)
				return (
					'background-image: url(' + url + ');' +
					'background-size: cover;' +
					'background-position: center;' +
					'background-repeat: no-repeat;'
				)
			}
		},
		methods: {
			formatProvince(province) {
				if (!province) return '未知';
				return province
					.replace(/省$/, '')
					.replace(/市$/, '')
					.replace(/自治区$/, '')
					.replace(/特别行政区$/, '');
			},
			handleOpenMore() {
				this.$emit('open-more');
			},
			handleScan() {
				this.$emit('scan');
			},
			handleViewAvatar() {
				this.$emit('view-avatar');
			},
			handleAttentionAndFans(type) {
				this.$emit('attention-and-fans', type);
			},
			handlePraiseAndCollect() {
				this.$emit('praise-and-collect');
			},
			handleEditData() {
				this.$emit('edit-data');
			},
			handleSetting() {
				this.$emit('setting');
			},
			handleShoppingCart() {
				this.$emit('shopping-cart');
			},
			handleOrders() {
				this.$emit('orders');
			},
			handleBrowseRecord() {
				this.$emit('browse-record');
			},
			handleAIAssistant() {
				this.$emit('ai-assistant');
			},
			handleGroupChat() {
				this.$emit('group-chat');
			}
		}
	};
</script>

<style lang="scss" scoped>
	.info {
		background-position: center;
		background-repeat: no-repeat;
		background-size: cover;
		background-color: rgba(0, 0, 0, 0.1);
		position: relative;
		z-index: 0;
	}

	.info-fixed {
		z-index: 1000;
	}

	.info-bg-image {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 120%;
		z-index: 0;
	}

	.filter {
		width: 100%;
		height: 120%;
		position: absolute;
		top: 0;
		left: 0;
		z-index: 0;
		filter: brightness(65%);
		background-color: #ffffff;
		background-repeat: no-repeat;
		background-size: cover;
		background-position: center;
	}

	.filter-overlay {
		background-image: none !important;
		background-color: rgba(0, 0, 0, 0.35);
		z-index: 1;
	}

	.info-content {
		position: relative;
		z-index: 2;
	}

	.top-main {
		padding: 0 20rpx 20rpx 20rpx;
	}

	.status-bar {
		position: fixed;
		width: 100%;
		z-index: 998;
	}

	.navigation-bar {
		position: fixed;
		width: 100%;
		display: flex;
		align-items: center;
		z-index: 999;
		justify-content: center;
		// margin-top: -15rpx;
	}

	/* 吸顶栏：左中右同一水平线，H5 与小程序端一致 */
	.compact-bar {
		justify-content: space-between;
		padding-left: 20rpx;
		padding-right: 20rpx;
		box-sizing: border-box;
		margin-top: 0;
	}
	.compact-bar-left,
	.compact-bar-center,
	.compact-bar-right {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 44px;
	}
	.compact-bar-center {
		flex: 1;
	}
	.compact-bar-avatar {
		border-radius: 50%;
		display: block;
		margin-top: 10rpx;
	}
	.compact-bar-icon-first {
		margin-right: 20rpx;
	}

	.userinfo-main {
		display: inline-flex;
		margin-top: -30rpx;
	}

	.userinfo-main image {
		border-radius: 50%;
		height: 120rpx;
		width: 120rpx;
		margin: 20rpx 30rpx 20rpx 0;
	}

	.userinfo-main-right {
		margin: 25rpx 0rpx;

		text:nth-child(1) {
			color: #ffffff;
			font-size: 35rpx;
			display: block;
		}

		text:nth-child(2) {
			margin-top: 10rpx;
			color: #95949a;
			font-size: 23rpx;
			display: block;
		}

		text:nth-child(3) {
			color: #95949a;
			font-size: 23rpx;
			display: block;
		}
	}

	.introduction {
		color: #ffffff;
		font-size: 26rpx;
		margin-right: 40rpx;
		margin-top: 10rpx;
		margin-bottom: 10rpx;
		word-wrap: break-word;
		white-space: normal;
		word-break: break-all;
	}

	.tag1 {
		color: #ffffff;
		height: 30px;
		border-radius: 30rpx;
		border-color: #ffffff;
		border: 3rpx solid #e5e6eb;
		line-height: 30px;
		padding: 0rpx 20rpx 0rpx 20rpx;
		justify-content: center;
		font-size: 25rpx;
		margin-left: 50rpx;
	}

	.tag2 {
		display: flex;
		height: 30px;
		width: 30px;
		color: #ffffff;
		border-radius: 30rpx;
		border-color: #ffffff;
		border: 3rpx solid #e5e6eb;
		line-height: 30px;
		padding: 0rpx 20rpx 0rpx 20rpx;
		justify-content: center;
		font-size: 25rpx;
	}

	.guanzhu {
		margin-top: 10rpx;
		margin-bottom: 10rpx;
		margin-right: 25rpx;
		text-align: center;
		color: #ffffff;
		font-size: 24rpx;
	}

	.looked {
		width: 6em;
		margin-top: 20rpx;
		padding: 8rpx 0rpx 8rpx 16rpx;
		background-color: rgba(89, 88, 87, 0.6);
		border-radius: 20rpx;
	}

	/* 可滑动功能按钮区域样式 */
	.function-scroll {
		width: 100%;
		white-space: nowrap;
		margin-top: 15rpx;
		// margin-bottom: 5rpx;
	}

	.function-container {
		display: flex;
		justify-content: flex-start;
		min-width: 95%;
		gap: 20rpx;
	}

	.function-container .looked {
		flex-shrink: 0;
	}
</style>