<template>
	<view class="chat-history-container">
		<!-- 微信端使用系统栏标题「对话记录」，不重复展示导航栏 -->
		<!-- #ifndef MP-WEIXIN -->
		<u-navbar title="对话记录" :autoBack="true" :placeholder="true" bgColor="#ffffff">
		</u-navbar>
		<!-- #endif -->

		<!-- 对话列表 -->
		<scroll-view scroll-y class="history-list" :style="{height: listHeight + 'px'}">
			<view v-if="conversations.length === 0" class="empty-state">
				<u-icon name="chat" size="80" color="#cccccc"></u-icon>
				<text class="empty-text">暂无对话记录</text>
				<view class="empty-tip">开始与AI助手对话吧～</view>
				<view class="start-chat-btn" @click="goToModelSelect">
					<u-icon name="plus" size="20" color="#ffffff"></u-icon>
					<text class="btn-text">开始对话</text>
				</view>
			</view>

			<view v-else class="conversation-list">
				<view v-for="(item, index) in conversations" :key="index" class="conversation-item"
					@click="openConversation(item)">
					<!-- 内容区域 -->
					<view class="conversation-content">
						<view class="conversation-header">
							<view class="conversation-title-wrapper">
								<view class="conversation-title">{{ item.title || '未命名对话' }}</view>
								<view class="conversation-time">{{ formatTime(item.updateTime) }}</view>
							</view>
						</view>
						<view class="conversation-subtitle">
							<view class="model-tag" v-if="item.model || item.assistantTitle">
								<image v-if="getModelLogo(item.model)" :src="getModelLogo(item.model)" mode="aspectFit" class="model-logo"></image>
								<text v-else class="model-icon">🤖</text>
								<text class="model-name">{{ item.model || item.assistantTitle || 'AI助手' }}</text>
							</view>
							<text class="subtitle-text">点击继续对话</text>
						</view>
					</view>

					<!-- 删除按钮 -->
					<view class="delete-btn" @click.stop="deleteConversation(item, index)">
						<u-icon name="trash" size="18" color="#999"></u-icon>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- 底部新建对话按钮 -->
		<view class="bottom-bar" :style="{paddingBottom: safeAreaBottom + 10 + 'px'}">
			<view class="new-chat-btn" @click="goToModelSelect">
				<u-icon name="plus" size="20" color="#ffffff"></u-icon>
				<text class="btn-text">新建对话</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { fetchChatList, deleteChatConversation } from '@/apis/ai_service.js'
	
	export default {
		data() {
			return {
				statusBarHeight: 0,
				safeAreaBottom: 0,
				listHeight: 0,
				conversations: [],
				loading: false
			}
		},
		onLoad() {
			this.initPage()
			this.loadConversations()
		},
		methods: {
			initPage() {
				const systemInfo = uni.getSystemInfoSync()
				this.statusBarHeight = systemInfo.statusBarHeight || 0
				this.safeAreaBottom = systemInfo.safeAreaInsets?.bottom || 0

				// 计算列表高度
				const windowHeight = systemInfo.windowHeight
				const bottomBarHeight = 80 + this.safeAreaBottom
				// #ifdef MP-WEIXIN
				this.listHeight = windowHeight - bottomBarHeight
				// #endif
				// #ifndef MP-WEIXIN
				const navHeight = this.statusBarHeight + 44
				this.listHeight = windowHeight - navHeight - bottomBarHeight
				// #endif
			},

			// 加载对话列表
			async loadConversations() {
				this.loading = true
				try {
					const res = await fetchChatList()
					if (res.code === 200) {
						// 后端返回格式：{ title, chatNumber, createTime, assistantTitle, model, modelVersion }
						console.log('📋 后端返回的原始数据:', res.data)
						this.conversations = (res.data || []).map(item => {
							// 优先使用 model 字段（从消息记录中获取），其次使用 assistantTitle
							const model = item.model || null
							const modelVersion = item.modelVersion || item.model_version || null
							const assistantTitle = item.assistantTitle || item.assistant_title || null
							
							console.log('📝 处理对话项:', {
								chatNumber: item.chatNumber,
								title: item.title,
								model: model,
								modelVersion: modelVersion,
								assistantTitle: assistantTitle,
								rawItem: item
							})
							
							// 处理模型名称：优先使用 model，其次使用 assistantTitle
							let modelName = model || assistantTitle || 'AI助手'
							
							// 如果 assistantTitle 包含图标，尝试提取文本部分（仅当没有 model 时）
							if (!model && modelName && modelName.length > 0) {
								// 移除可能的图标字符（emoji等），保留文本
								const cleanedName = modelName.replace(/[\u{1F300}-\u{1F9FF}]/gu, '').trim()
								// 如果移除图标后不为空，使用清理后的名称
								if (cleanedName) {
									modelName = cleanedName
								}
							}
							
							// 格式化模型名称显示（如果有版本号，可以组合显示）
							let displayModelName = modelName
							if (modelVersion && modelName !== modelVersion) {
								displayModelName = `${modelName} (${modelVersion})`
							}
							
							return {
								id: item.chatNumber,
								chatNumber: item.chatNumber,
								title: item.title || '未命名对话',
								model: model || displayModelName,
								assistantTitle: displayModelName,
								modelIcon: item.modelIcon || item.model_icon || null, // 模型logo图标
								lastMessage: '', // 后端不返回最后消息，可以省略
								updateTime: new Date(item.createTime || item.create_time).getTime(),
								createTime: item.createTime || item.create_time
							}
						})
						console.log('✅ 加载对话列表成功', this.conversations.length, '条')
						console.log('📋 处理后的对话列表:', this.conversations)
					} else {
						console.error('❌ 加载对话列表失败', res)
					}
				} catch (error) {
					console.error('❌ 加载对话列表出错', error)
				} finally {
					this.loading = false
				}
			},

			// 根据模型名称获取头像
			getModelAvatar(modelName) {
				if (!modelName) return ''
				return `/static/svgs/${modelName}.svg`
			},

			// 格式化时间
			formatTime(timestamp) {
				if (!timestamp) return ''
				const now = Date.now()
				const diff = now - timestamp
				const minute = 60 * 1000
				const hour = 60 * minute
				const day = 24 * hour

				if (diff < minute) {
					return '刚刚'
				} else if (diff < hour) {
					return Math.floor(diff / minute) + '分钟前'
				} else if (diff < day) {
					return Math.floor(diff / hour) + '小时前'
				} else if (diff < 7 * day) {
					return Math.floor(diff / day) + '天前'
				} else {
					const date = new Date(timestamp)
					return `${date.getMonth() + 1}月${date.getDate()}日`
				}
			},

			// 打开对话
			openConversation(conversation) {
				// 传递 chatNumber 到聊天页面，用于加载历史消息
				uni.navigateTo({
					url: `/pkg-ai/pages/chat/chat?chatNumber=${conversation.chatNumber}&model=${conversation.model || ''}`
				})
			},

			// 删除对话
			async deleteConversation(conversation, index) {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除这条对话记录吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await deleteChatConversation(conversation.chatNumber)
								if (result.code === 200) {
									this.conversations.splice(index, 1)
									uni.showToast({
										title: '已删除',
										icon: 'none'
									})
									console.log('✅ 删除对话成功')
								} else {
									uni.showToast({
										title: result.msg || '删除失败',
										icon: 'none'
									})
									console.error('❌ 删除对话失败', result)
								}
							} catch (error) {
								console.error('❌ 删除对话出错', error)
								uni.showToast({
									title: '删除失败',
									icon: 'none'
								})
							}
						}
					}
				})
			},

			// 跳转到模型选择页面
			goToModelSelect() {
				uni.navigateTo({
					url: '/pkg-ai/pages/model-select/model-select'
				})
			},
			
			// 根据模型名称获取logo路径（使用前端静态资源）
			getModelLogo(modelName) {
				if (!modelName) {
					return '' // 没有模型时返回空
				}
				// 模型名称映射表：将后端返回的模型名称映射到SVG文件名
				const modelMap = {
					'ChatGPT': 'ChatGPT',
					'ChatGLM': 'ChatGLM',
					'DeepSeek': 'DeepSeek',
					'Doubao': 'Doubao',
					'Internlm': 'Internlm',
					'Moonshot': 'Moonshot',
					'QIANWEN': 'QIANWEN',
					'SPARK': 'SPARK',
					'WENXIN': 'WENXIN',
					// 兼容可能的其他格式
					'chatgpt': 'ChatGPT',
					'chatglm': 'ChatGLM',
					'deepseek': 'DeepSeek',
					'doubao': 'Doubao',
					'internlm': 'Internlm',
					'moonshot': 'Moonshot',
					'qianwen': 'QIANWEN',
					'spark': 'SPARK',
					'wenxin': 'WENXIN',
					'通义千问': 'QIANWEN',
					'讯飞星火': 'SPARK',
					'文心一言': 'WENXIN',
					'智谱清言': 'ChatGLM',
					'深度求索': 'DeepSeek',
					'豆包': 'Doubao',
					'月之暗面': 'Moonshot',
					'书生·浦语': 'Internlm'
				}
				
				// 查找映射的模型名称
				const mappedName = modelMap[modelName] || modelName
				
				// 返回SVG路径
				return `/static/svgs/${mappedName}.svg`
			}
		}
	}
</script>

<style lang="scss" scoped>
	.chat-history-container {
		height: 100vh;
		background-color: #f5f7fa;
		display: flex;
		flex-direction: column;
	}

	.history-list {
		flex: 1;
		background-color: #f5f7fa;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 60rpx;
		text-align: center;
	}

	.empty-text {
		margin-top: 30rpx;
		font-size: 32rpx;
		color: #999999;
	}

	.empty-tip {
		margin-top: 16rpx;
		font-size: 28rpx;
		color: #cccccc;
	}

	.start-chat-btn {
		margin-top: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 24rpx 60rpx;
		background-color: #3d8af5;
		border-radius: 50rpx;
		box-shadow: 0 4rpx 16rpx rgba(61, 138, 245, 0.3);
	}

	.start-chat-btn .btn-text {
		margin-left: 10rpx;
		font-size: 30rpx;
		color: #ffffff;
		font-weight: 500;
	}

	.conversation-list {
		padding: 24rpx 20rpx;
	}

	.conversation-item {
		display: flex;
		align-items: flex-start;
		padding: 24rpx 20rpx;
		background-color: #ffffff;
		margin-bottom: 16rpx;
		border-radius: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
		position: relative;
		transition: all 0.2s;
	}

	.conversation-item:active {
		background-color: #f8f9fe;
		transform: translateY(-2rpx);
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
	}

	.conversation-content {
		flex: 1;
		min-width: 0;
		padding-right: 12rpx;
	}

	.conversation-header {
		margin-bottom: 14rpx;
	}

	.conversation-title-wrapper {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.conversation-title {
		flex: 1;
		font-size: 30rpx;
		font-weight: 600;
		color: #1a1a1a;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		line-height: 1.5;
		min-width: 0;
	}

	.conversation-time {
		font-size: 22rpx;
		color: #999999;
		flex-shrink: 0;
		white-space: nowrap;
	}

	.conversation-subtitle {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	.model-tag {
		display: flex;
		align-items: center;
		padding: 4rpx 12rpx;
		background-color: #e6f4ff;
		border-radius: 12rpx;
		font-size: 22rpx;
		color: #3d8af5;
		font-weight: 500;
		flex-shrink: 0;
		height: 40rpx;
		box-sizing: border-box;
	}
	
	.model-icon {
		font-size: 22rpx;
		margin-right: 4rpx;
		line-height: 1;
	}
	
	.model-logo {
		width: 28rpx;
		height: 28rpx;
		margin-right: 6rpx;
		border-radius: 6rpx;
		flex-shrink: 0;
	}
	
	.model-name {
		font-size: 22rpx;
		color: #3d8af5;
		font-weight: 500;
		line-height: 1;
	}

	.subtitle-text {
		font-size: 22rpx;
		color: #999999;
		line-height: 1.4;
	}

	.delete-btn {
		padding: 12rpx;
		margin-top: 4rpx;
		flex-shrink: 0;
		display: flex;
		align-items: flex-start;
		justify-content: center;
		border-radius: 8rpx;
		transition: all 0.2s;
	}

	.delete-btn:active {
		background-color: #f5f5f5;
		opacity: 0.8;
	}

	.bottom-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		padding: 16rpx 30rpx;
		background-color: #ffffff;
		border-top: 1rpx solid #e8eaf0;
		z-index: 100;
		display: flex;
		justify-content: center;
		align-items: center;
		box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.02);
	}

	.new-chat-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 20rpx 48rpx;
		background-color: #3d8af5;
		border-radius: 50rpx;
		box-shadow: 0 4rpx 16rpx rgba(61, 138, 245, 0.3);
		min-width: 240rpx;
		max-width: 500rpx;
		transition: all 0.2s;
	}

	.new-chat-btn:active {
		opacity: 0.9;
		transform: scale(0.98);
		box-shadow: 0 2rpx 12rpx rgba(61, 138, 245, 0.25);
	}

	.new-chat-btn .btn-text {
		margin-left: 8rpx;
		font-size: 28rpx;
		color: #ffffff;
		font-weight: 500;
		letter-spacing: 0.5rpx;
	}
</style>
