<template>
	<view class="webview-page">
		<!-- #ifdef MP-WEIXIN -->
		<!-- 检查域名是否在业务域名列表中 -->
		<web-view v-if="isDomainAllowed" :src="url"></web-view>
		<!-- 如果域名未配置，显示提示 -->
		<view v-else class="webview-error">
			<view class="error-content">
				<text class="error-title">无法打开该页面</text>
				<text class="error-desc">该链接的域名未在小程序管理后台配置为业务域名</text>
				<text class="error-url">{{ url }}</text>
				<text class="error-tip">请在小程序管理后台的"开发" -> "开发管理" -> "开发设置" -> "业务域名"中添加该域名</text>
				<button class="error-btn" @click="copyUrl">复制链接</button>
				<button class="error-btn secondary" @click="goBack">返回</button>
			</view>
		</view>
		<!-- #endif -->
		
		<!-- #ifndef MP-WEIXIN -->
		<!-- 非微信小程序环境，使用其他方式打开 -->
		<view class="webview-fallback">
			<view class="fallback-content">
				<text class="fallback-title">正在打开链接...</text>
				<text class="fallback-url">{{ url }}</text>
				<button class="fallback-btn" @click="openInBrowser">在浏览器中打开</button>
			</view>
		</view>
		<!-- #endif -->
	</view>
</template>

<script>
	export default {
		data() {
			return {
				url: '',
				isDomainAllowed: true // 默认允许，实际需要根据业务域名配置判断
			}
		},
		onLoad(options) {
			// 获取传递的URL参数
			if (options.url) {
				// 解码URL
				this.url = decodeURIComponent(options.url)
				
				// #ifdef MP-WEIXIN
				// 检查域名是否在业务域名列表中
				// 注意：这里无法直接获取业务域名列表，只能通过错误处理来判断
				// 实际使用中，如果域名未配置，web-view 会触发错误事件
				this.checkDomain()
				// #endif
			} else {
				uni.showToast({
					title: '链接地址无效',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			}
			
			// 设置导航栏标题
			uni.setNavigationBarTitle({
				title: '加载中...'
			})
		},
		methods: {
			// 检查域名（在微信小程序中，如果域名未配置，web-view 会显示错误）
			checkDomain() {
				// 这里无法直接判断，只能通过 web-view 的错误事件来处理
				// 如果域名未配置，微信会自动显示错误页面
				// 我们可以提供一个降级方案：如果检测到错误，显示友好的提示
				this.isDomainAllowed = true
			},
			
			copyUrl() {
				uni.setClipboardData({
					data: this.url,
					success: () => {
						uni.showToast({
							title: '链接已复制',
							icon: 'success'
						})
					}
				})
			},
			
			goBack() {
				uni.navigateBack()
			},
			
			openInBrowser() {
				// #ifdef H5
				window.open(this.url, '_blank')
				// #endif
				
				// #ifdef APP-PLUS
				plus.runtime.openURL(this.url)
				// #endif
				
				// #ifdef MP-WEIXIN
				uni.setClipboardData({
					data: this.url,
					success: () => {
						uni.showToast({
							title: '链接已复制，请在浏览器中打开',
							icon: 'none'
						})
					}
				})
				// #endif
			}
		}
	}
</script>

<style lang="scss" scoped>
	.webview-page {
		width: 100%;
		height: 100vh;
	}
	
	.webview-error {
		width: 100%;
		height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #f5f5f5;
		padding: 40rpx;
		box-sizing: border-box;
	}
	
	.error-content {
		width: 100%;
		max-width: 600rpx;
		padding: 60rpx 40rpx;
		background-color: #fff;
		border-radius: 16rpx;
		text-align: center;
	}
	
	.error-title {
		display: block;
		font-size: 36rpx;
		color: #333;
		font-weight: 500;
		margin-bottom: 30rpx;
	}
	
	.error-desc {
		display: block;
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
		margin-bottom: 30rpx;
	}
	
	.error-url {
		display: block;
		font-size: 24rpx;
		color: #999;
		word-break: break-all;
		background-color: #f5f5f5;
		padding: 20rpx;
		border-radius: 8rpx;
		margin-bottom: 30rpx;
		text-align: left;
	}
	
	.error-tip {
		display: block;
		font-size: 24rpx;
		color: #999;
		line-height: 1.6;
		margin-bottom: 40rpx;
		text-align: left;
	}
	
	.error-btn {
		width: 100%;
		height: 80rpx;
		line-height: 80rpx;
		background-color: #1890ff;
		color: #fff;
		border-radius: 8rpx;
		font-size: 28rpx;
		margin-bottom: 20rpx;
		border: none;
		
		&.secondary {
			background-color: #f5f5f5;
			color: #666;
		}
	}
	
	.webview-fallback {
		width: 100%;
		height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #f5f5f5;
	}
	
	.fallback-content {
		width: 80%;
		padding: 40rpx;
		background-color: #fff;
		border-radius: 16rpx;
		text-align: center;
	}
	
	.fallback-title {
		display: block;
		font-size: 32rpx;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.fallback-url {
		display: block;
		font-size: 24rpx;
		color: #999;
		word-break: break-all;
		margin-bottom: 40rpx;
	}
	
	.fallback-btn {
		width: 100%;
		height: 80rpx;
		line-height: 80rpx;
		background-color: #1890ff;
		color: #fff;
		border-radius: 8rpx;
		font-size: 28rpx;
	}
</style>

