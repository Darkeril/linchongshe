<template>
	<view class="splash-container" style="background: linear-gradient(180deg, #FFFFFF 0%, #F4EDE2 40%, #EFCFB0 100%);">
		<!-- 装饰爪印 -->
		<image class="paw-deco paw-1" src="/static/icons/paw-soft.svg" mode="aspectFit"></image>
		<image class="paw-deco paw-2" src="/static/icons/paw-soft.svg" mode="aspectFit"></image>
		<image class="paw-deco paw-3" src="/static/icons/paw-primary.svg" mode="aspectFit"></image>
		<image class="paw-deco paw-4" src="/static/icons/paw-primary.svg" mode="aspectFit"></image>

		<!-- 主体区域 -->
		<view class="main-content">
			<!-- 圆形 Logo 徽标 -->
			<view class="logo-badge">
				<image
					v-if="useCustomLogo"
					:src="logoImage"
					class="logo-custom-img"
					mode="aspectFit"
					@error="handleLogoError"
				></image>
				<image
					v-else
					src="/static/icons/paw-white.svg"
					class="logo-paw-icon"
					mode="aspectFit"
				></image>
			</view>

			<!-- 品牌名 -->
			<text class="brand-name">爱宠社</text>

			<!-- 副标题 -->
			<text class="brand-subtitle">和毛孩子一起，分享每一天</text>
		</view>

		<!-- 底部加载区 -->
		<view class="loading-area">
			<view class="loading-bar-track">
				<view class="loading-bar-fill"></view>
			</view>
			<text class="loading-text">LOADING · v2.0</text>
		</view>
	</view>
</template>

<script>
	import { getWebsiteConfig } from '@/apis/config_service.js'
	import { baseUrl } from '@/.env.js'

	function isValidStoredToken(t) {
		return (
			t &&
			t !== '' &&
			String(t) !== 'undefined' &&
			String(t) !== 'null'
		)
	}

	export default {
		data() {
			return {
				logoImage: '/static/logoImage.png',
				defaultLogo: '/static/logoImage.png',
				useCustomLogo: false // 默认用内置爪印 SVG，后端有 logo 时切换
			}
		},
		onLoad() {
			this.fixPageBackground()
			this.fetchWebsiteLogo()
			this.bootstrapRoute()
		},
		onShow() {
			this.fixPageBackground()
		},
		mounted() {
			this.fixPageBackground()
		},
		methods: {
			fixPageBackground() {
				// #ifdef H5
				this.$nextTick(() => {
					const bgVal = 'linear-gradient(180deg, #FFFFFF 0%, #F4EDE2 40%, #EFCFB0 100%)'
					const selectors = ['uni-page-body', 'uni-page-wrapper', 'uni-page']
					selectors.forEach(tag => {
						const el = document.querySelector(tag)
						if (el) {
							el.style.background = bgVal
							el.style.backgroundColor = '#F4EDE2'
						}
					})
					document.body.style.backgroundColor = '#F4EDE2'
				})
				// #endif
			},
			async fetchWebsiteLogo() {
				try {
					const response = await getWebsiteConfig()
					if (response && response.code === 200 && response.data && response.data.logo) {
						this.logoImage = response.data.logo
						this.useCustomLogo = true
					}
				} catch (error) {
					console.error('获取网站logo失败:', error)
					this.useCustomLogo = false
				}
			},
			handleLogoError() {
				this.useCustomLogo = false
			},

			doRefreshToken() {
				return new Promise((resolve, reject) => {
					const refreshToken = uni.getStorageSync('uniapp_refresh_token')
					if (!isValidStoredToken(refreshToken)) {
						resolve(true)
						return
					}
					uni.request({
						url:
							baseUrl +
							`/web/app/auth/refreshToken?refreshToken=${encodeURIComponent(refreshToken)}`,
						method: 'GET',
						header: {
							'Content-Type': 'application/json'
						},
						success: (res) => {
							if (res.data && res.data.code === 200 && res.data.data) {
								const { accessToken, refreshToken: newRt } = res.data.data
								if (accessToken) {
									uni.setStorageSync('uniapp_token', accessToken)
								}
								if (newRt) {
									uni.setStorageSync('uniapp_refresh_token', newRt)
								}
								resolve(true)
							} else {
								reject(new Error('refresh failed'))
							}
						},
						fail: (err) => reject(err || new Error('network'))
					})
				})
			},

			clearAuthStorage() {
				try {
					uni.removeStorageSync('uniapp_token')
					uni.removeStorageSync('uniapp_refresh_token')
					uni.removeStorageSync('userInfo')
				} catch (e) {}
			},

			goLoginMethod() {
				setTimeout(() => {
					uni.reLaunch({
						url: '/pkg-auth/pages/login/loginMethod'
					})
				}, 2500)
			},

			goHome() {
				uni.reLaunch({
					url: '/pages/index/index'
				})
			},

			bootstrapRoute() {
				const accessToken = uni.getStorageSync('uniapp_token')
				if (!isValidStoredToken(accessToken)) {
					this.goLoginMethod()
					return
				}
				const minDelay = new Promise((r) => setTimeout(r, 1500))
				const tokenRefresh = this.doRefreshToken().catch(() => false)
				Promise.all([minDelay, tokenRefresh]).then(([, tokenOk]) => {
					if (tokenOk !== false) {
						this.goHome()
					} else {
						this.clearAuthStorage()
						uni.reLaunch({
							url: '/pkg-auth/pages/login/loginMethod'
						})
					}
				})
			}
		}
	}
</script>

<!-- 非 scoped：设置 UniApp page 元素背景，防止刷新后闪白 -->
<style>
page {
	background: linear-gradient(180deg, #FFFFFF 0%, #F4EDE2 40%, #EFCFB0 100%) !important;
	background-color: #F4EDE2 !important;
}
uni-page-body {
	background: linear-gradient(180deg, #FFFFFF 0%, #F4EDE2 40%, #EFCFB0 100%) !important;
	background-color: #F4EDE2 !important;
}
</style>

<style scoped>
	.splash-container {
		width: 100%;
		height: 100vh;
		background: linear-gradient(180deg, #FFFFFF 0%, #F4EDE2 40%, #EFCFB0 100%);
		position: relative;
		overflow: hidden;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	/* ─── 装饰爪印 ─── */
	.paw-deco {
		position: absolute;
		pointer-events: none;
	}

	.paw-1 {
		width: 72rpx;
		height: 72rpx;
		top: 240rpx;
		left: 80rpx;
		transform: rotate(-20deg);
		opacity: 0.7;
	}

	.paw-2 {
		width: 48rpx;
		height: 48rpx;
		top: 360rpx;
		right: 120rpx;
		transform: rotate(15deg);
		opacity: 0.5;
	}

	.paw-3 {
		width: 56rpx;
		height: 56rpx;
		bottom: 520rpx;
		left: 140rpx;
		transform: rotate(40deg);
		opacity: 0.25;
	}

	.paw-4 {
		width: 40rpx;
		height: 40rpx;
		bottom: 680rpx;
		right: 160rpx;
		opacity: 0.2;
	}

	/* ─── 主体内容 ─── */
	.main-content {
		position: absolute;
		top: 32%;
		left: 0;
		right: 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 32rpx;
	}

	.logo-badge {
		width: 208rpx;
		height: 208rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #C97B4A, #8A4A1F);
		box-shadow: 0 28rpx 80rpx rgba(201, 123, 74, 0.35),
					inset 0 4rpx 12rpx rgba(255, 255, 255, 0.4);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.logo-paw-icon {
		width: 100rpx;
		height: 100rpx;
	}

	.logo-custom-img {
		width: 140rpx;
		height: 140rpx;
		border-radius: 50%;
	}

	.brand-name {
		font-size: 68rpx;
		font-weight: 700;
		color: #231710;
		letter-spacing: 4rpx;
	}

	.brand-subtitle {
		font-size: 28rpx;
		color: #63463A;
		letter-spacing: 2rpx;
	}

	/* ─── 底部加载 ─── */
	.loading-area {
		position: absolute;
		bottom: 180rpx;
		left: 50%;
		transform: translateX(-50%);
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 28rpx;
	}

	.loading-bar-track {
		width: 88rpx;
		height: 8rpx;
		border-radius: 4rpx;
		background: #EFCFB0;
		overflow: hidden;
		position: relative;
	}

	.loading-bar-fill {
		position: absolute;
		left: 0;
		top: 0;
		bottom: 0;
		width: 60%;
		background: #C97B4A;
		border-radius: 4rpx;
		animation: splashLoad 1.8s ease-in-out infinite;
	}

	@keyframes splashLoad {
		0% {
			transform: translateX(-100%);
		}
		50% {
			transform: translateX(80%);
		}
		100% {
			transform: translateX(180%);
		}
	}

	.loading-text {
		font-size: 22rpx;
		color: #8F7260;
		letter-spacing: 4rpx;
	}
</style>
