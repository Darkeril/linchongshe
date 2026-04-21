<template>
	<view class="splash-container">
		<view class="content-wrapper">
			<view class="logo-wrapper">
				<image 
					:src="logoImage" 
					class="logo" 
					mode="aspectFit"
					@error="handleLogoError"
				></image>
			</view>
			<view class="welcome-text">
				<text>你的生活兴趣社区</text>
			</view>
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
				logoImage: '/static/logoImage.png', // 默认logo
				defaultLogo: '/static/logoImage.png' // 保存默认logo路径
			}
		},
		onLoad() {
			this.fetchWebsiteLogo()
			this.bootstrapRoute()
		},
		methods: {
			// 获取网站配置中的logo
			async fetchWebsiteLogo() {
				try {
					const response = await getWebsiteConfig()
					if (response && response.code === 200 && response.data && response.data.logo) {
						this.logoImage = response.data.logo
					}
				} catch (error) {
					console.error('获取网站logo失败:', error)
					// 获取失败时使用默认logo
					this.logoImage = this.defaultLogo
				}
			},
			// 处理logo加载错误
			handleLogoError() {
				// logo加载失败时，回退到默认logo
				this.logoImage = this.defaultLogo
			},

			/** 与 utils/request.js 中刷新逻辑一致，启动时先轮换 token 再进首页（对齐 uniapp_im splash） */
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

			/**
			 * 本地已有 accessToken 则视为可静默恢复登录；可选刷新 refreshToken。
			 * 无 token 时再进登录方式页（原逻辑总是进登录页，导致 token 未过期也要重登）。
			 */
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

<style scoped>
	.splash-container {
		width: 100%;
		height: 100vh;
		background: #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
	}

	.content-wrapper {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		margin-top: -10vh;
	}

	.logo-wrapper {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 10rpx;
	}

	.logo {
		width: 200rpx;
		height: 200rpx;
	}

	.welcome-text {
		margin-top: -20rpx;
		font-size: 38rpx;
		color: #333333;
		font-weight: 400;
		text-align: center;
	}
</style>
