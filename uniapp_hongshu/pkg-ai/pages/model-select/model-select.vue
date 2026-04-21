<template>
	<view class="model-select-container">
		<!-- 顶部导航栏（微信端系统栏已显示「选择大模型」，此处不重复） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="navbar-content">
				<view class="navbar-left" @click="handleBack" v-if="showBackButton">
					<u-icon name="arrow-left" size="20" color="#333" />
				</view>
				<view class="navbar-left" v-else />
				<view class="navbar-title">选择大模型</view>
				<view class="navbar-right" />
			</view>
		</view>
		<!-- #endif -->

		<!-- 中间区域：仅显示一个下拉选择框 -->
		<view class="selector-wrapper">
			<view v-if="loading" class="loading-wrapper">
				<u-loading-icon mode="circle" :size="28" />
				<text class="loading-text">正在加载可用模型...</text>
			</view>
			<view v-else-if="models.length === 0" class="empty-wrapper">
				<text class="empty-title">暂时没有可用模型</text>
				<text class="empty-desc">可以先在网页端配置模型，或使用聊天页的模拟模式体验。</text>
			</view>
			<view v-else class="selector-content">
				<!-- 功能介绍 -->
				<view class="feature-intro">
					<view class="feature-title">
						<text class="feature-icon">✨</text>
						<text class="feature-title-text">一键切换</text>
					</view>
					<view class="feature-subtitle">多模型对话，畅所欲言</view>
					<view class="feature-desc">
						基于ChatGPT、ChatGLM、文心一言、通义千问、讯飞星火、月之暗面等主流大模型开发，支持多轮对话，具备内容创作、信息归纳总结等能力。
					</view>
				</view>
				
				<picker mode="selector" :range="modelNames" @change="onModelChange">
					<view class="selector-box">
						<view class="selector-left">
							<view class="selector-icon-circle">
								<text class="selector-icon-text">AI</text>
							</view>
							<text class="selector-single">请选择聊天助手</text>
						</view>
						<view class="selector-icon">
							<u-icon name="arrow-down" size="18" color="#999" />
						</view>
					</view>
				</picker>
				<view v-if="selectedModelDesc" class="selector-desc">
					<text class="selector-desc-text">当前选择：{{ selectedModelDesc }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { fetchModel } from '@/apis/ai_service.js'

export default {
	data() {
		return {
			statusBarHeight: 0,
			loading: false,
			models: [],
			selectedModelIndex: -1,
			selectedModelName: '',
			selectedModelDesc: '',
			showBackButton: false
		}
	},

	onLoad() {
		this.initPage()
		this.loadModels()
		this.checkShowBackButton()
	},

	computed: {
		// 下拉框展示用的模型名称数组
		modelNames() {
			return this.models.map(m => m.name || '')
		}
	},

	methods: {
		initPage() {
			const systemInfo = uni.getSystemInfoSync()
			this.statusBarHeight = systemInfo.statusBarHeight || 0
		},

		checkShowBackButton() {
			// 检查页面栈，如果有上一页则显示返回按钮
			const pages = getCurrentPages()
			this.showBackButton = pages.length > 1
		},

		handleBack() {
			uni.navigateBack({
				delta: 1
			})
		},

		async loadModels() {
			this.loading = true
			try {
				const res = await fetchModel()
				if (res.code === 200 && Array.isArray(res.data)) {
					this.models = res.data
						.filter(m => m.status === 1)
						.sort((a, b) => a.sort - b.sort)
				} else if (res.code === 401 && (res.msg || '').indexOf('签名错误') !== -1) {
					// 后端签名校验未通过：使用本地静态模型列表作为降级方案
					console.warn('模型接口签名错误，使用本地静态模型列表')
					this.models = [
						{ id: 8, name: 'DeepSeek',    icon: '', model: 'DeepSeek',   version: '',                    status: 1, sort: 8 },
						{ id: 9, name: '书生·浦语',   icon: '', model: 'Internlm',  version: 'internlm3-latest',    status: 1, sort: 8 },
						{ id: 7, name: '豆包',        icon: '', model: 'Doubao',    version: 'ep-20251019174330-nz98m', status: 1, sort: 7 },
						{ id: 3, name: '通义千问',    icon: '', model: 'QIANWEN',   version: 'qwen-turbo',          status: 1, sort: 6 },
						{ id: 4, name: '讯飞星火',    icon: '', model: 'SPARK',     version: 'max-32k',             status: 1, sort: 5 },
						{ id: 5, name: '智谱清言',    icon: '', model: 'ChatGLM',   version: 'chatGLM_6b_SSE',      status: 1, sort: 4 },
						{ id: 6, name: '文心一言',    icon: '', model: 'WENXIN',    version: 'ERNIE_Bot_turbo',    status: 1, sort: 3 },
						{ id: 2, name: 'Kimi',        icon: '', model: 'Moonshot',  version: 'moonshot-v1-8k',     status: 1, sort: 2 },
						{ id: 1, name: 'ChatGPT',     icon: '', model: 'ChatGPT',   version: 'gpt-3.5-turbo-1106', status: 1, sort: 1 }
					].sort((a, b) => a.sort - b.sort)
				} else {
					this.models = []
				}
			} catch (e) {
				console.error('加载模型列表失败', e)
				this.models = []
			} finally {
				this.loading = false
			}
		},

		getModelDesc(item) {
			if (!item) return ''
			const name = item.name || ''
			const version = item.version || ''
			if (version) return `${name} · ${version}`
			return `${name} · 通用大模型`
		},

		onModelChange(e) {
			const index = e && e.detail ? Number(e.detail.value) : -1
			if (isNaN(index) || index < 0 || index >= this.models.length) return
			const item = this.models[index]
			this.selectedModelIndex = index
			this.selectedModelName = item.name || ''
			this.selectedModelDesc = this.getModelDesc(item)
			// 选择后直接进入聊天页面
			this.handleSelect(item)
		},

		handleSelect(item) {
			if (!item) return
			const query = `model=${encodeURIComponent(item.model || '')}` +
				`&modelName=${encodeURIComponent(item.name || '')}` +
				`&version=${encodeURIComponent(item.version || '')}`
			uni.navigateTo({
				url: `/pkg-ai/pages/chat/chat?${query}`
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.model-select-container {
	height: 100vh;
	background-color: #f5f5f5;
	display: flex;
	flex-direction: column;
}

.custom-navbar {
	position: relative;
	z-index: 10;
	background: #ffffff;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

	.navbar-content {
		height: 44px;
		padding: 0 24rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.navbar-left,
	.navbar-right {
		width: 80rpx;
		height: 44px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.navbar-title {
		flex: 1;
		text-align: center;
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
	}
}

.model-list {
	flex: 1;
}

.selector-wrapper {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 32rpx 280rpx;
}

.selector-content {
	width: 100%;
	max-width: 600rpx;
}

.selector-box {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 24rpx 28rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.04);
}

.selector-left {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	flex: 1;
}

.selector-icon-circle {
	width: 40rpx;
	height: 40rpx;
	border-radius: 20rpx;
	background: linear-gradient(135deg, #3d8af5, #2563eb);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 16rpx;
}

.selector-icon-text {
	font-size: 20rpx;
	color: #fff;
	font-weight: 600;
}

.selector-single {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.selector-icon {
	margin-left: 20rpx;
}

.selector-desc {
	margin-top: 20rpx;
	padding: 16rpx 20rpx;
	border-radius: 16rpx;
	background-color: #eef2ff;
}

.selector-desc-text {
	font-size: 24rpx;
	color: #5a67d8;
}

.feature-intro {
	margin-bottom: 64rpx;
	padding: 32rpx 28rpx;
	background: linear-gradient(135deg, #3d8af5 0%, #2563eb 100%);
	border-radius: 24rpx;
	box-shadow: 0 8rpx 24rpx rgba(61, 138, 245, 0.25);
}

.feature-title {
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 12rpx;
}

.feature-icon {
	font-size: 32rpx;
	margin-right: 8rpx;
}

.feature-title-text {
	font-size: 32rpx;
	font-weight: 700;
	color: #ffffff;
	letter-spacing: 2rpx;
}

.feature-subtitle {
	font-size: 28rpx;
	font-weight: 600;
	color: rgba(255, 255, 255, 0.95);
	text-align: center;
	margin-bottom: 20rpx;
	letter-spacing: 1rpx;
}

.feature-desc {
	font-size: 26rpx;
	line-height: 40rpx;
	color: rgba(255, 255, 255, 0.85);
	text-align: justify;
	text-indent: 2em;
}

.loading-wrapper,
.empty-wrapper {
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	color: #999;
}

.loading-text {
	margin-top: 16rpx;
	font-size: 26rpx;
}

.empty-title {
	font-size: 30rpx;
	font-weight: 500;
	margin-bottom: 8rpx;
}

.empty-desc {
	font-size: 24rpx;
	color: #aaa;
	padding: 0 40rpx;
	text-align: center;
}

.models-wrapper {
	padding: 20rpx 24rpx 40rpx;
}

.model-item {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 24rpx;
	margin-bottom: 16rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.model-main {
	display: flex;
	align-items: center;
}

.model-avatar {
	width: 72rpx;
	height: 72rpx;
	border-radius: 36rpx;
	background: linear-gradient(135deg, #3d8af5, #2563eb);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
}

.model-avatar-text {
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
}

.model-info {
	display: flex;
	flex-direction: column;
}

.model-name-row {
	display: flex;
	align-items: center;
	margin-bottom: 4rpx;
}

.model-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-right: 12rpx;
}

.model-version {
	font-size: 24rpx;
	color: #888;
}

.model-desc {
	font-size: 24rpx;
	color: #999;
}

.model-arrow {
	margin-left: 20rpx;
}

.bottom-tip {
	padding: 12rpx 24rpx 24rpx;
	background-color: #f5f5f5;
}

.tip-text {
	font-size: 24rpx;
	color: #999;
	text-align: center;
}
</style>
