<template>
	<view class="ai-chat-container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{paddingTop: statusBarHeight + 'px'}">
			<view class="navbar-content">
				<!-- #ifndef MP-WEIXIN -->
				<view class="navbar-left" @click="goBack" hover-class="navbar-hover">
					<u-icon name="arrow-left" size="24" color="#333"></u-icon>
				</view>
				<!-- #endif -->
				<!-- #ifdef MP-WEIXIN -->
				<view class="navbar-left"></view>
				<!-- #endif -->
				<!-- 使用 picker 组件替代 ActionSheet -->
				<picker mode="selector" :range="modelNames" :value="currentModelIndex" @change="onModelChange">
					<view class="navbar-title" hover-class="navbar-hover">
						<text>{{ currentModelName || 'AI助手' }}</text>
						<u-icon name="arrow-down" size="16" color="#666" style="margin-left: 8rpx;"></u-icon>
					</view>
				</picker>
				<view class="navbar-right" @click="showMoreOptions" hover-class="navbar-hover">
					<u-icon name="more-dot-fill" size="24" color="#333"></u-icon>
				</view>
			</view>
		</view>

		<!-- 聊天消息列表 -->
		<scroll-view 
			scroll-y 
			class="chat-messages" 
			:scroll-top="scrollTop"
			scroll-with-animation
		>
			<view class="messages-container">
				<!-- 欢迎消息 -->
				<view v-if="messages.length === 0" class="welcome-message">
					<view class="ai-avatar" :class="{ 'has-model-avatar': modelAvatar }">
						<image v-if="modelAvatar" :src="modelAvatar" mode="aspectFill" class="model-avatar-img"></image>
						<u-icon v-else name="chat" size="32" color="#ffffff"></u-icon>
					</view>
					<view class="welcome-text">
						<text class="welcome-title">你好！我是{{ currentModelName || 'AI助手' }}</text>
						<text class="welcome-desc" v-if="useRealAPI">我已接入AI引擎，随时为您解答问题、提供灵感或简单闲聊</text>
						<text class="welcome-desc" v-else>当前运行在模拟模式下，可以体验基本的对话功能。点击上方标题可以切换不同的模拟器。有什么我可以帮助您的吗？</text>
					</view>
				</view>

				<!-- 消息列表 -->
				<view v-for="(message, index) in messages" :key="index" class="message-item">
					<view v-if="message.role === 'user'" class="message user-message">
						<view class="message-content user-content">
							<text>{{ message.content }}</text>
						</view>
						<view class="user-avatar">
							<image v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" mode="aspectFill"></image>
							<u-icon v-else name="account-fill" size="24" color="#999"></u-icon>
						</view>
					</view>

					<view v-else class="message ai-message">
						<view class="ai-avatar" :class="{ 'has-model-avatar': getModelAvatar(message.model) }">
							<image v-if="getModelAvatar(message.model)" :src="getModelAvatar(message.model)" mode="aspectFill" class="model-avatar-img"></image>
							<u-icon v-else name="chat" size="24" color="#ffffff"></u-icon>
						</view>
						<view class="message-content ai-content">
							<text v-if="message.content">{{ message.content }}</text>
							<view v-if="message.loading" class="typing-indicator">
								<view class="typing-dot"></view>
								<view class="typing-dot"></view>
								<view class="typing-dot"></view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- 输入框 -->
		<view class="input-container" :style="{bottom: keyboardHeight + 'px', paddingBottom: (safeAreaBottom) + 'px'}">
			<view class="input-wrapper">
				<view class="input-box">
				<textarea 
				v-model="inputText" 
				placeholder="输入你想问的问题..."
				:maxlength="500"
				auto-height
				:disabled="isLoading"
				:adjust-position="false"
				@confirm="sendMessage"
				@focus="handleInputFocus"
				@blur="handleInputBlur"
				class="input-textarea"
			></textarea>
			</view>
				<view class="send-button" :class="{ active: canSend, loading: isLoading }" @click="sendMessage">
					<text class="send-text">发送</text>
				</view>
			</view>
		</view>

	</view>
</template>

<script>
import { simulateAIResponse, fetchModel, fetchChatAPI, fetchChatMessageAPI, triggerAIReply, getWebSocketUrl, fetchChatMessages } from '@/apis/ai_service.js'
import { baseUrl } from '@/.env.js'

export default {
	data() {
		return {
			statusBarHeight: 0,
			safeAreaBottom: 0,
			keyboardHeight: 0,
			scrollTop: 0,
			inputText: '',
			isLoading: false,
			messages: [],
			userInfo: {},
			// API 配置（从 .env.js 导入）
			baseURL: baseUrl,  // 后端 API 地址
			// WebSocket 相关
			socketConnected: false,
			socketTask: null, // 使用 socketTask 对象，避免与全局 WebSocket 冲突
			heartbeatTimer: null,
			// 模型相关
			availableModels: [],
			currentModel: '',
			currentModelName: '',
			chatNumber: '', // 当前会话ID
			// 是否使用真实API
			useRealAPI: false
		}
	},
	computed: {
		canSend() {
			return this.inputText.trim() && !this.isLoading
		},
		// 获取当前模型的头像路径
		modelAvatar() {
			if (!this.currentModel) {
				return '' // 没有选择模型时返回空，使用默认图标
			}
			// 根据模型名称返回对应的 SVG 头像路径
			return `/static/svgs/${this.currentModel}.svg`
		},
		// picker 组件需要的模型名称数组
		modelNames() {
			if (this.useRealAPI) {
				return this.availableModels.map(model => {
					const version = model.version ? ` (${model.version})` : ''
					return `${model.name}${version}`
				})
			} else {
				return this.availableModels.map(model => model.name)
			}
		},
		// 当前选中模型的索引
		currentModelIndex() {
			return this.availableModels.findIndex(m => m.model === this.currentModel)
		}
	},
	
	methods: {
		// 根据模型名称获取头像路径
		getModelAvatar(modelName) {
			if (!modelName) {
				return '' // 没有模型时返回空
			}
			return `/static/svgs/${modelName}.svg`
		},
	async onLoad(options) {
		this.initPage()
		await this.getUserInfo()  // 等待获取用户信息
		this.initModelFromRoute(options || {})
		await this.loadAvailableModels()  // 等待模型加载完成
		
		// 延迟连接 WebSocket，确保页面完全加载
		this.$nextTick(() => {
			setTimeout(() => {
				this.connectWebSocket()  // 确保 userInfo 已获取后再连接
			}, 500) // 延迟500ms，确保页面完全初始化
		})
		
		// 如果有 chatNumber 参数，加载历史消息
		if (options && options.chatNumber) {
			this.loadChatHistory(options.chatNumber)
		}
	},
	onUnload() {
		// 关闭 WebSocket 连接
		if (this.heartbeatTimer) {
			clearInterval(this.heartbeatTimer)
		}
		if (this.socketTask) {
			this.socketTask.close()
			this.socketTask = null
		}
		this.socketConnected = false
	},
		initPage() {
		const systemInfo = uni.getSystemInfoSync()
		// 微信端系统栏已含状态栏，不再预留顶部间距，使大模型选择紧贴「爱宠社」下方
		// #ifdef MP-WEIXIN
		this.statusBarHeight = 0
		// #endif
		// #ifndef MP-WEIXIN
		this.statusBarHeight = systemInfo.statusBarHeight || 0
		// #endif
		this.safeAreaBottom = systemInfo.safeAreaInsets?.bottom || 0
		
		console.log('📐 页面初始化:', {
			statusBarHeight: this.statusBarHeight,
			safeAreaBottom: this.safeAreaBottom
		})
	},
		
		getUserInfo() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (userInfo) {
					this.userInfo = userInfo
				}
			} catch (e) {
				console.error('获取用户信息失败:', e)
			}
		},

		// 从路由参数初始化模型信息
		initModelFromRoute(options) {
			if (!options) return
			const model = options.model ? decodeURIComponent(options.model) : ''
			const modelName = options.modelName ? decodeURIComponent(options.modelName) : ''
			const version = options.version ? decodeURIComponent(options.version) : ''
			if (model || modelName) {
				this.currentModel = model
				this.currentModelName = modelName || 'AI助手'
				// 先假定使用真实API，后续由 loadAvailableModels 校正
				this.useRealAPI = true
				console.log('🌐 来自模型选择页的路由参数:', { model, modelName, version })
			}
		},

		goBack() {
			uni.navigateBack()
		},

		showMoreOptions() {
			uni.showActionSheet({
				itemList: ['对话记录', '清空聊天记录', '使用帮助'],
				success: (res) => {
					if (res.tapIndex === 0) {
						this.goToChatHistory()
					} else if (res.tapIndex === 1) {
						this.clearMessages()
					} else if (res.tapIndex === 2) {
						this.showHelp()
					}
				}
			})
		},

		goToChatHistory() {
			uni.navigateTo({
				url: '/pkg-ai/pages/chat-history/chat-history'
			})
		},

		clearMessages() {
			uni.showModal({
				title: '确认清空',
				content: '确定要清空所有聊天记录吗？',
				success: (res) => {
					if (res.confirm) {
						this.messages = []
						uni.showToast({
							title: '已清空',
							icon: 'none',
							duration: 1500
						})
					}
				}
			})
		},

		showHelp() {
			uni.showModal({
				title: 'AI助手使用帮助',
				content: '1. 直接输入问题即可开始对话\n2. 支持创作建议、问题解答等\n3. 点击右上角菜单可清空记录',
				showCancel: false
			})
		},

		async sendMessage() {
			if (!this.canSend) return

			const userMessage = {
				role: 'user',
				content: this.inputText.trim(),
				timestamp: Date.now()
			}

			// 添加用户消息
			this.messages.push(userMessage)
			
			// 清空输入框
			const inputText = this.inputText
			this.inputText = ''
			
			// 添加AI加载消息
			const aiMessage = {
				role: 'assistant',
				content: '',
				loading: true,
				timestamp: Date.now(),
				model: this.currentModel // 记录当前使用的模型
			}
			this.messages.push(aiMessage)
			
			this.isLoading = true
			this.scrollToBottom()

			try {
				if (this.useRealAPI) {
					// 使用真实API
					await this.callRealAI(inputText, aiMessage)
				} else {
					// 使用模拟回复
					await this.simulateAIResponse(inputText, aiMessage)
				}
			} catch (error) {
				console.error('AI回复失败:', error)
				aiMessage.content = '抱歉，我暂时无法回复，请稍后再试。'
				aiMessage.loading = false
			} finally {
				this.isLoading = false
				this.scrollToBottom()
			}
		},

		async simulateAIResponse(userInput, aiMessage) {
			try {
				// 根据当前模型生成不同风格的回复
				let response
				if (this.useRealAPI) {
					// 如果是真实API模式但降级了，使用通用模拟
					response = await simulateAIResponse(userInput)
				} else {
					// 模拟模式，根据选择的模拟器生成不同风格的回复
					response = this.generateSimulateResponse(userInput)
				}
				
				if (response.code === 200) {
					const content = response.data.content
					
					// 模拟打字效果
					aiMessage.loading = false
					aiMessage.content = ''
					
					for (let i = 0; i < content.length; i++) {
						aiMessage.content += content[i]
						if (i % 5 === 0) { // 每5个字符更新一次
							await new Promise(resolve => setTimeout(resolve, 50))
							this.scrollToBottom()
						}
					}
				} else {
					throw new Error('AI服务响应异常')
				}
			} catch (error) {
				console.error('AI回复失败:', error)
				aiMessage.loading = false
				aiMessage.content = '抱歉，我暂时无法回复，请稍后再试。'
			}
		},

		// 生成模拟回复
		generateSimulateResponse(userInput) {
			return new Promise((resolve) => {
				setTimeout(() => {
					let content = ''
					const modelName = this.currentModelName
					
					// 根据不同模拟器生成不同风格的回复
					if (this.currentModel === 'simulate-gpt') {
						content = `[GPT模拟器] 您好！我是GPT模拟器。关于"${userInput}"，我认为这是一个很有趣的问题。基于我的训练数据，我可以为您提供以下见解：\n\n1. 首先，让我们分析问题的核心\n2. 然后考虑可能的解决方案\n3. 最后提供一些建议\n\n请注意，这是模拟回复，实际效果可能有所不同。`
					} else if (this.currentModel === 'simulate-claude') {
						content = `[Claude模拟器] 感谢您的提问！作为Claude模拟器，我很乐意帮助您。对于"${userInput}"这个话题，我想从以下几个角度来分析：\n\n• 问题的背景和重要性\n• 相关的考虑因素\n• 实用的建议和方法\n\n我会尽力为您提供有用且准确的信息。有什么具体方面您希望我详细解释吗？`
					} else if (this.currentModel === 'simulate-qwen') {
						content = `[通义千问模拟器] 您好！我是通义千问模拟器。针对您提到的"${userInput}"，我来为您详细解答：\n\n🔍 问题分析：\n这是一个很好的问题，涉及到多个方面的考虑。\n\n💡 解决思路：\n1. 明确需求和目标\n2. 分析现有条件\n3. 制定可行方案\n\n📝 建议：\n建议您可以从实际情况出发，结合具体需求来选择最适合的方法。\n\n还有什么其他问题需要我帮助解答吗？`
					}
					
					resolve({
						code: 200,
						data: {
							content: content,
							messageId: 'sim_' + Date.now(),
							timestamp: Date.now()
						}
					})
				}, 1000 + Math.random() * 1500)
			})
		},

		// 调用真实AI API
		async callRealAI(userInput, aiMessage) {
			try {
				// 如果没有会话ID，先创建会话
				if (!this.chatNumber) {
					const chatResponse = await fetchChatAPI({
						prompt: userInput,
						systemPrompt: userInput
					})
					
					if (chatResponse.code === 200) {
						this.chatNumber = chatResponse.data.chatNumber
					} else {
						throw new Error('创建会话失败')
					}
				}

				// 发送用户消息到后端
				const messageResponse = await fetchChatMessageAPI({
					chatNumber: this.chatNumber,
					model: this.currentModel,
					prompt: userInput,
					systemPrompt: userInput
				})

				if (messageResponse.code !== 200) {
					throw new Error('发送消息失败')
				}

				const conversationId = messageResponse.data
				
				// 保存 conversationId 到 aiMessage，用于匹配 WebSocket 回复
				aiMessage.conversationId = conversationId
				aiMessage.waitingForReply = true
				
				console.log('📤 已发送消息，等待AI回复，conversationId:', conversationId)
				
				// 触发 AI 回复（通过 WebSocket 接收）
				try {
					await triggerAIReply(conversationId)
					console.log('✅ 已触发AI回复请求')
				} catch (triggerError) {
					console.error('⚠️ 触发AI回复失败，但继续等待WebSocket消息:', triggerError)
					// 即使触发失败，也继续等待WebSocket消息，因为后端可能已经自动处理
				}
				
				// 设置超时：如果30秒内没有收到回复，显示错误
				setTimeout(() => {
					if (aiMessage.waitingForReply && aiMessage.loading) {
						console.error('⏰ AI回复超时，conversationId:', conversationId)
						aiMessage.loading = false
						aiMessage.waitingForReply = false
						aiMessage.content = '抱歉，AI回复超时，请稍后再试。'
						this.scrollToBottom()
					}
				}, 30000) // 30秒超时
				
			} catch (error) {
				console.error('真实AI调用失败:', error)
				// 降级到模拟模式
				aiMessage.waitingForReply = false
				await this.simulateAIResponse(userInput, aiMessage)
			}
		},

		scrollToBottom() {
			// 使用 nextTick 确保 DOM 更新后再滚动
			this.$nextTick(() => {
				// 使用一个足够大的值确保滚动到底部
				// 每次设置不同的值以触发 scroll-view 更新
				this.scrollTop = Date.now()
				// 再次确保滚动到底部（双保险）
				setTimeout(() => {
					this.scrollTop = this.scrollTop + 1
				}, 100)
			})
		},

		// 处理输入框获得焦点（键盘弹起）
		handleInputFocus(e) {
			console.log('⌨️ 键盘弹起，高度:', e.detail.height)
			// 保存键盘高度，减去一些偏移找到最佳位置
			// 减去30px，让输入框在键盘上方保持合适距离
			const offset = 30
			this.keyboardHeight = Math.max(0, (e.detail.height || 0) - offset)
			// 滚动到底部
			setTimeout(() => {
				this.scrollToBottom()
			}, 300)
		},

		// 处理输入框失去焦点（键盘收起）
		handleInputBlur(e) {
			console.log('⌨️ 键盘收起')
			// 恢复输入框位置
			this.keyboardHeight = 0
		},

		updateCurrentTab(index) {
			// TabBar切换处理
			console.log('TabBar切换到:', index)
		},

		// 加载可用模型
		async loadAvailableModels() {
			try {
				const response = await fetchModel()
				if (response.code === 200 && response.data && Array.isArray(response.data) && response.data.length > 0) {
					// 过滤状态为1的可用模型，并按sort排序
					this.availableModels = response.data
						.filter(model => model.status === 1)
						.sort((a, b) => a.sort - b.sort)
						.map(model => ({
							id: model.id,
							name: model.name,
							model: model.model,
							version: model.version,
							icon: model.icon
						}))
					
					if (this.availableModels.length > 0) {
						// 如果路由已传入模型，则优先使用路由指定的模型
						if (this.currentModel) {
							const existed = this.availableModels.find(m => m.model === this.currentModel)
							if (existed) {
								this.currentModelName = existed.name
							} else {
								// 路由模型不在列表中时，回退到第一个
								this.currentModel = this.availableModels[0].model
								this.currentModelName = this.availableModels[0].name
							}
						} else {
							// 没有路由参数时，默认使用排序最靠前的模型
							this.currentModel = this.availableModels[0].model
							this.currentModelName = this.availableModels[0].name
						}
						this.useRealAPI = true
						console.log('✅ 已连接到AI后端服务，加载了', this.availableModels.length, '个模型')
						console.log('📋 可用模型:', this.availableModels.map(m => m.name).join(', '))
						console.log('🎯 当前模型:', this.currentModelName)
					} else {
						this.fallbackToSimulateMode('没有可用的模型')
					}
				} else {
					this.fallbackToSimulateMode('后端返回数据格式错误')
				}
			} catch (error) {
				console.log('🔄 AI后端服务不可用，使用模拟模式:', error.message || error)
				
				// 检测CORS错误
				const errorMsg = error.message || error.toString()
				if (errorMsg.includes('CORS') || errorMsg.includes('Access-Control-Allow-Origin')) {
					this.fallbackToSimulateMode('CORS跨域问题')
				} else if (errorMsg.includes('Network Error') || errorMsg.includes('网络错误')) {
					this.fallbackToSimulateMode('网络连接问题')
				} else {
					this.fallbackToSimulateMode('后端服务不可用')
				}
			}
		},

		// 降级到模拟模式
		fallbackToSimulateMode(reason) {
			this.useRealAPI = false
			this.currentModelName = 'AI助手(模拟)'
			this.availableModels = [
				{ model: 'simulate-gpt', name: 'GPT模拟器' },
				{ model: 'simulate-claude', name: 'Claude模拟器' },
				{ model: 'simulate-qwen', name: '通义千问模拟器' },
				{ model: 'simulate-deepseek', name: 'DeepSeek模拟器' },
				{ model: 'simulate-kimi', name: 'Kimi模拟器' }
			]
			this.currentModel = 'simulate-gpt'
			console.log('📱 已启用模拟模式，原因:', reason)
			
			// 如果是CORS错误，给出特殊提示
			if (reason.includes('CORS') || reason.includes('跨域')) {
				uni.showModal({
					title: '网络配置提示',
					content: '检测到跨域问题，已自动切换到模拟模式。如需使用真实AI，请配置代理或在小程序环境中使用。',
					showCancel: false
				})
			}
		},

		// Picker 选择器回调（支持无限数量模型）
		onModelChange(e) {
			const index = e && e.detail ? Number(e.detail.value) : -1
			if (isNaN(index) || index < 0 || index >= this.availableModels.length) return
			
			const selectedModel = this.availableModels[index]
			this.currentModel = selectedModel.model
			this.currentModelName = selectedModel.name
			
			const versionText = this.useRealAPI && selectedModel.version ? ` ${selectedModel.version}` : ''
			
			uni.showToast({
				title: `已切换至 ${selectedModel.name}${versionText}`,
				icon: 'none',
				duration: 1500
			})
			
			console.log('🔄 模型切换:', {
				name: selectedModel.name,
				model: selectedModel.model,
				version: selectedModel.version
			})
		},

		// 建立 WebSocket 连接
		connectWebSocket() {
			const that = this
			const uid = this.userInfo.userId || this.userInfo.uid || this.userInfo.id || ''
			
			if (!uid) {
				console.error('❌ WebSocket 连接失败：用户ID为空', this.userInfo)
				// 延迟重试
				setTimeout(() => {
					if (that.userInfo.userId || that.userInfo.uid || that.userInfo.id) {
						console.log('🔄 重试 WebSocket 连接...')
						that.connectWebSocket()
					}
				}, 1000)
				return
			}
			
			// 如果已经连接，先关闭
			if (this.socketTask) {
				console.log('⚠️ WebSocket 已连接，先关闭旧连接')
				this.socketTask.close()
				this.socketTask = null
				this.socketConnected = false
			}
			
			// 使用封装的方法获取 WebSocket URL
			const wsUrl = getWebSocketUrl(uid)
			console.log('🔌 正在连接 WebSocket:', wsUrl)
			console.log('👤 用户信息:', this.userInfo)
			
			// 获取token用于WebSocket认证
			const token = uni.getStorageSync('uniapp_token') || ''
			console.log('🔑 Token:', token ? '已获取' : '未获取')
			
			// 使用 socketTask 方式连接，避免与全局 WebSocket 冲突
			this.socketTask = uni.connectSocket({
				url: wsUrl,
				header: {
					'Authorization': token ? `Bearer ${token}` : '',
					'client-id': 'app'
				},
				success: () => {
					console.log('✅ WebSocket 连接请求已发送')
					// 5秒后检查连接状态
					setTimeout(() => {
						if (!that.socketConnected) {
							console.warn('⚠️ WebSocket 连接超时，5秒后仍未连接成功')
							console.warn('⚠️ 请检查：1. WebSocket服务器是否运行 2. URL是否正确 3. 网络是否正常')
							console.warn('⚠️ WebSocket URL:', wsUrl)
						}
					}, 5000)
				},
				fail: (err) => {
					console.error('❌ WebSocket 连接失败:', err)
					that.socketTask = null
					// 连接失败时，3秒后重试
					setTimeout(() => {
						console.log('🔄 3秒后重试 WebSocket 连接...')
						that.connectWebSocket()
					}, 3000)
				}
			})
			
			// 使用 socketTask 的 onOpen 方法（而不是全局的 uni.onSocketOpen）
			this.socketTask.onOpen((res) => {
				console.log('✅ WebSocket 连接已打开', res)
				that.socketConnected = true
				
				// 发送绑定消息
				that.sendSocketMessage({
					functionCode: 'bind'
				})
				console.log('📤 已发送绑定消息')
				
				// 设置心跳包
				if (that.heartbeatTimer) {
					clearInterval(that.heartbeatTimer)
				}
				that.heartbeatTimer = setInterval(() => {
					if (that.socketConnected && that.socketTask && that.socketTask.readyState === 1) {
						that.sendSocketMessage({
							functionCode: 'pong'
						})
					}
				}, 20000) // 每20秒发送一次心跳
			})
			
			// 使用 socketTask 的 onMessage 方法
			this.socketTask.onMessage((res) => {
				try {
					// console.log('📥 收到原始 WebSocket 数据:', res.data)
					const data = JSON.parse(res.data)
					// console.log('📨 解析后的 WebSocket 消息:', JSON.stringify(data, null, 2))
					
					if (data.functionCode === 'message' && data.message) {
						// 处理 AI 回复消息
						console.log('✅ 识别为 AI 回复消息，准备处理')
						that.handleWebSocketMessage(data.message)
					} else if (data.functionCode === 'bind') {
						// 处理绑定成功确认
						console.log('✅ WebSocket 绑定成功')
						// 绑定成功，不需要特殊处理，连接已建立
					} else if (data.functionCode === 'error') {
						// 处理错误消息
						console.error('❌ WebSocket 错误消息:', data)
						const lastMessage = that.messages[that.messages.length - 1]
						if (lastMessage && lastMessage.role === 'assistant' && lastMessage.waitingForReply) {
							lastMessage.loading = false
							lastMessage.waitingForReply = false
							lastMessage.content = data.message || data.error || '抱歉，AI回复出现错误，请稍后再试。'
							that.scrollToBottom()
						}
					} else if (data.functionCode === 'ping') {
						// 响应心跳
						console.log('💓 收到心跳，回复 pong')
						that.sendSocketMessage({
							functionCode: 'pong'
						})
					} else {
						console.warn('⚠️ 未识别的 WebSocket 消息类型:', data.functionCode, data)
					}
				} catch (e) {
					console.error('❌ 解析 WebSocket 消息失败:', e, res.data)
				}
			})

			// 使用 socketTask 的 onError 方法
			this.socketTask.onError((err) => {
				console.error('❌ WebSocket 连接错误:', err)
				that.socketConnected = false
			})

			// 使用 socketTask 的 onClose 方法
			this.socketTask.onClose((res) => {
				console.log('🔌 WebSocket 已关闭', res)
				that.socketConnected = false
				that.socketTask = null
				if (that.heartbeatTimer) {
					clearInterval(that.heartbeatTimer)
				}
			})
		},

		// 发送 WebSocket 消息
		sendSocketMessage(data) {
			if (!this.socketTask || this.socketTask.readyState !== 1) {
				console.error('❌ WebSocket 未连接，无法发送消息')
				return
			}
			this.socketTask.send({
				data: JSON.stringify(data),
				success: () => {
					console.log('✅ WebSocket 消息发送成功')
				},
				fail: (err) => {
					console.error('❌ WebSocket 消息发送失败:', err)
				}
			})
		},

		// 处理 WebSocket 接收到的 AI 回复
		handleWebSocketMessage(message) {
			// console.log('📨 收到 WebSocket AI 回复:', message)
			// console.log('📨 消息结构:', JSON.stringify(message, null, 2))
			
			// 提取 conversationId（可能在不同的字段中）
			const conversationId = message.conversationId || message.id || message.messageId
			const content = message.content || ''
			const role = message.role || 'assistant'
			const finish = message.finish === true // 是否完成（明确检查 true）
			
			// console.log('🔍 提取的信息:', { 
			// 	conversationId, 
			// 	contentLength: content?.length || 0, 
			// 	contentPreview: content?.substring(0, 50) || '', 
			// 	role,
			// 	finish 
			// })
			
			// 查找正在等待回复的消息
			// 优先匹配 conversationId，如果没有则匹配最后一条 assistant 消息
			let targetMessage = null
			
			if (conversationId) {
				// 通过 conversationId 精确匹配
				targetMessage = this.messages.find(msg => 
					msg.role === 'assistant' && 
					msg.conversationId === conversationId &&
					msg.waitingForReply
				)
				console.log('🎯 通过 conversationId 查找消息:', conversationId, targetMessage ? '找到' : '未找到')
			}
			
			// 如果没有找到，使用最后一条等待回复的 assistant 消息
			if (!targetMessage) {
				// 从后往前查找最后一条等待回复的 assistant 消息
				for (let i = this.messages.length - 1; i >= 0; i--) {
					const msg = this.messages[i]
					if (msg.role === 'assistant' && msg.waitingForReply) {
						targetMessage = msg
						console.log('🎯 使用最后一条等待回复的消息:', i)
						// 如果消息有 conversationId，更新它
						if (conversationId && !targetMessage.conversationId) {
							targetMessage.conversationId = conversationId
						}
						break
					}
				}
			}
			
			if (targetMessage) {
				// 流式更新：更新同一条消息的内容（打字机效果）
				if (content) {
					const oldContentLength = targetMessage.content?.length || 0
					const newContentLength = content.length
					
					// 如果新内容比现有内容长，说明是流式追加
					if (newContentLength > oldContentLength) {
						// 流式追加，直接更新内容
						targetMessage.content = content
						console.log(`📝 流式更新: ${oldContentLength} -> ${newContentLength} 字符`)
					} else if (newContentLength === oldContentLength && content === targetMessage.content) {
						// 内容相同，可能是重复消息，忽略
						console.log('⚠️ 收到重复消息，忽略')
						return
					} else {
						// 新内容（可能比旧内容短，说明是重新开始），直接替换
						targetMessage.content = content
						console.log(`🔄 内容替换: ${oldContentLength} -> ${newContentLength} 字符`)
					}
				}
				
				// 更新 conversationId（如果之前没有）
				if (!targetMessage.conversationId && conversationId) {
					targetMessage.conversationId = conversationId
				}
				
				// 如果消息完成（finish=true），停止loading和等待
				if (finish) {
					targetMessage.loading = false
					targetMessage.waitingForReply = false
					console.log('✅ AI回复完成（finish=true）')
				} else {
					// 流式更新中，保持loading状态（显示打字动画）
					targetMessage.loading = true
					// 清除之前的超时定时器（如果有）
					if (targetMessage._finishTimer) {
						clearTimeout(targetMessage._finishTimer)
					}
					// 设置超时：如果3秒内没有收到新消息，认为已完成
					targetMessage._finishTimer = setTimeout(() => {
						if (targetMessage && targetMessage.waitingForReply) {
							console.log('⏰ 流式更新超时（3秒无新消息），认为已完成')
							targetMessage.loading = false
							targetMessage.waitingForReply = false
							delete targetMessage._finishTimer
						}
					}, 3000)
				}
				
				console.log('✅ 已更新AI回复消息，内容长度:', targetMessage.content?.length || 0, '完成:', finish)
				
				// 每次消息更新都滚动到底部
				this.$nextTick(() => {
					this.scrollToBottom()
				})
			} else {
				console.warn('⚠️ 未找到匹配的等待回复消息，conversationId:', conversationId)
				console.warn('⚠️ 当前消息列表:', this.messages.map(m => ({
					role: m.role,
					waitingForReply: m.waitingForReply,
					conversationId: m.conversationId
				})))
				// 如果没有找到匹配的消息，创建一条新消息（不应该发生，但作为兜底）
				this.messages.push({
					role: 'assistant',
					content: content || '',
					loading: !finish,
					timestamp: Date.now(),
					model: this.currentModel,
					conversationId: conversationId,
					waitingForReply: !finish
				})
				this.$nextTick(() => {
					this.scrollToBottom()
				})
			}
		},
		
		// 加载历史消息
		async loadChatHistory(chatNumber) {
			try {
				console.log('📜 加载历史消息，chatNumber:', chatNumber)
				const res = await fetchChatMessages(chatNumber)
				if (res.code === 200 && res.data && res.data.length > 0) {
					// 后端返回格式：{ messageId, parentMessageId, createTime, content, contentType, model, role }
					this.messages = res.data.map(msg => ({
						role: msg.role || 'user', // 'user' 或 'assistant'
						content: msg.content || '',
						timestamp: new Date(msg.createTime).getTime(),
						loading: false,
						model: msg.model || this.currentModel
					}))
					console.log('✅ 加载历史消息成功', this.messages.length, '条')
					this.$nextTick(() => {
						this.scrollToBottom()
					})
				} else {
					console.log('⚠️ 暂无历史消息')
				}
			} catch (error) {
				console.error('❌ 加载历史消息失败', error)
			}
		}
	}
}
</script>

<style scoped>
.ai-chat-container {
	height: 100vh;
	background: linear-gradient(180deg, #f8f9fe 0%, #f0f2f8 100%);
	display: flex;
	flex-direction: column;
}

.custom-navbar {
	background-color: #fff;
	border-bottom: 1rpx solid #e8eaf0;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.custom-navbar .navbar-content {
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
}

.custom-navbar .navbar-content .navbar-left,
.custom-navbar .navbar-content .navbar-right {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.custom-navbar .navbar-content .navbar-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 12rpx 24rpx;
	border-radius: 24rpx;
	transition: background-color 0.3s ease;
	min-height: 60rpx;
	cursor: pointer;
	/* 防止文本选中 */
	user-select: none;
	-webkit-user-select: none;
}

.custom-navbar .navbar-content .navbar-title:active {
	background-color: #f5f7fa;
	transform: scale(0.98);
}

/* 微信小程序 hover 样式 */
.navbar-hover {
	background-color: #f5f7fa !important;
	opacity: 0.8;
}

.chat-messages {
	flex: 1;
	overflow-y: auto;
	background: transparent;
	/* flex布局自动填充剩余空间，无需手动计算高度 */
}

.messages-container {
	padding: 24rpx;
	padding-bottom: 180rpx; /* 为固定定位的输入框留出空间 */
	min-height: 100%;
	box-sizing: border-box;
}

.welcome-message {
	display: flex;
	align-items: flex-start;
	margin-bottom: 40rpx;
}

.welcome-message .ai-avatar {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
	overflow: hidden;
}

/* 有模型头像时，使用白色背景 */
.welcome-message .ai-avatar.has-model-avatar {
	background: #ffffff;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.welcome-message .welcome-text {
	flex: 1;
	background-color: #fff;
	border-radius: 24rpx;
	padding: 32rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.welcome-message .welcome-text .welcome-title {
	display: block;
	font-size: 34rpx;
	font-weight: 600;
	color: #1a1a1a;
	margin-bottom: 16rpx;
}

.welcome-message .welcome-text .welcome-desc {
	display: block;
	font-size: 28rpx;
	color: #666;
	line-height: 1.7;
}

.message-item {
	margin-bottom: 32rpx;
}

.message {
	display: flex;
	align-items: flex-start;
}

.user-message {
	justify-content: flex-end;
}

.user-message .user-content {
	background: linear-gradient(135deg, #4a90e2 0%, #667eea 100%);
	color: #fff;
	margin-right: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);
}

.user-message .user-avatar {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	overflow: hidden;
	background-color: #e8eaf0;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.user-message .user-avatar image {
	width: 100%;
	height: 100%;
}

.ai-message {
	justify-content: flex-start;
}

.ai-message .ai-avatar {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #4a90e2 0%, #667eea 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
	overflow: hidden;
}

/* 有模型头像时，使用白色背景 */
.ai-message .ai-avatar.has-model-avatar {
	background: #ffffff;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.model-avatar-img {
	width: 75%;
	height: 75%;
	object-fit: contain;
}

.ai-message .ai-content {
	background-color: #fff;
	color: #333;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.message-content {
	max-width: 70%;
	padding: 26rpx 32rpx;
	border-radius: 24rpx;
	font-size: 28rpx;
	line-height: 1.7;
	word-break: break-word;
}

.typing-indicator {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.typing-indicator .typing-dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background-color: #999;
	animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator .typing-dot:nth-child(1) {
	animation-delay: -0.32s;
}

.typing-indicator .typing-dot:nth-child(2) {
	animation-delay: -0.16s;
}

.typing-indicator .typing-dot:nth-child(3) {
	animation-delay: 0s;
}

@keyframes typing {
	0%,
	80%,
	100% {
		transform: scale(0.8);
		opacity: 0.5;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}

.input-container {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 1) 100%);
	backdrop-filter: blur(10rpx);
	border-top: 1rpx solid #e8eaf0;
	padding: 16rpx 20rpx 20rpx;
	z-index: 100;
	box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.input-wrapper {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.input-box {
	flex: 1;
	background-color: #f5f7fa;
	border-radius: 28rpx;
	padding: 16rpx 24rpx;
	min-height: 32rpx;
	margin-bottom: 10rpx;
	box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.04);
	transition: all 0.3s ease;
}

.input-box .input-textarea {
	width: 100%;
	font-size: 28rpx;
	line-height: 1.6;
	color: #333;
	background-color: transparent;
	border: none;
	outline: none;
}

.send-button {
	min-width: 140rpx;
	height: 72rpx;
	padding: 0 32rpx;
	border-radius: 50rpx;
	margin-bottom: 10rpx;
	background-color: #e0e0e0;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.send-button.active {
	background: linear-gradient(135deg, #4a90e2 0%, #667eea 100%);
	box-shadow: 0 6rpx 16rpx rgba(74, 144, 226, 0.35);
	transform: translateY(-2rpx);
}

.send-text {
	font-size: 28rpx;
	font-weight: 600;
	color: #ffffff;
}
</style>
