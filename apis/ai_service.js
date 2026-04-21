import { $request } from '@/utils/request.js'
import { aiWsUrl, baseUrl } from '@/.env.js'

/**
 * AI聊天API服务
 * 基于vue_chat_web项目的API接口设计
 */

// 内部工具：为聊天服务构造请求头（对齐 vue_chat_web）
function buildChatHeaders() {
	let token = ''
	try {
		// 与现有登录逻辑复用同一 token 存储
		token = uni.getStorageSync && uni.getStorageSync('uniapp_token') || ''
	} catch (e) { }
	const headers = { 'client-id': 'app' }
	if (token) {
		headers['Authorization'] = `Bearer ${token}`
	}
	return headers
}

// 获取用户可用模型列表
export function fetchModel() {
	return $request({
		url: '/chat/app/user/model',
		method: 'GET',
		header: buildChatHeaders()
	})
}

// 获取会话列表（对话记录）
export function fetchChatList() {
	return $request({
		url: '/chat/v1/chat',
		method: 'GET',
		header: buildChatHeaders(),
		showLoading: false
	})
}

// 删除会话
export function deleteChatConversation(chatNumber) {
	return $request({
		url: `/chat/v1/chat/${chatNumber}`,
		method: 'DELETE',
		header: buildChatHeaders(),
		showLoading: false
	})
}

// 获取会话消息（历史记录）
export function fetchChatMessages(chatNumber) {
	return $request({
		url: '/chat/v1/chat/message',
		method: 'GET',
		data: {
			chatNumber: chatNumber
		},
		header: buildChatHeaders(),
		showLoading: false
	})
}

// 创建对话会话
export function fetchChatAPI(data) {
	return $request({
		url: '/chat/v1/chat',
		method: 'POST',
		data: {
			assistantId: data.assistantId,
			systemPrompt: data.systemPrompt || data.prompt,
			prompt: data.prompt
		},
		header: buildChatHeaders(),
		showLoading: false
	})
}

// 触发 AI 回复（WebSocket 流式响应）
export function triggerAIReply(conversationId) {
	return $request({
		url: '/chat/v1/chat/completions',
		method: 'GET',
		header: buildChatHeaders(),
		data: {
			conversationId: conversationId,
			ws: 'true'
		},
		showLoading: false
	})
}

// 获取 WebSocket 连接 URL
export function getWebSocketUrl(uid) {
	// 使用配置文件中的 WebSocket 地址
	// 通过网关访问 WebSocket，网关会自动路由到 hongshu-ai 服务
	return `${aiWsUrl}/v1/chat/websocket/${uid}`
}

// 发送消息到AI
export function fetchChatMessageAPI(data) {
	return $request({
		url: '/chat/v1/chat/message',
		method: 'POST',
		data: {
			chatNumber: data.chatNumber,
			model: data.model || 'ChatGPT',
			systemPrompt: data.systemPrompt || data.prompt,
			prompt: data.prompt
		},
		header: buildChatHeaders(),
		showLoading: false
	})
}

// 流式响应聊天
export function fetchChatAPIProcess(params) {
	return $request({
		url: '/chat/v1/chat/completions',
		method: 'GET',
		data: {
			conversationId: params.conversationId
		},
		header: buildChatHeaders()
	})
}

// 创建新的对话
export function createChatConversation(data = {}) {
	return $request({
		url: '/v1/chat',
		method: 'POST',
		data: {
			title: data.title || '新对话',
			model: data.model || 'gpt-3.5-turbo'
		}
	})
}

// 获取对话历史
export function getChatHistory(conversationId) {
	return $request({
		url: `/v1/chat/history/${conversationId}`,
		method: 'GET'
	})
}

// 流式聊天（如果后端支持）
export function streamChat(data, onMessage) {
	// 这里可以实现SSE或WebSocket连接
	// 暂时使用普通请求模拟
	return fetchChatMessageAPI(data)
}

// 生成对话ID
function generateConversationId() {
	return 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 获取AI模型列表
export function getAIModels() {
	return $request({
		url: '/v1/models',
		method: 'GET'
	})
}

// 获取用户AI使用配额
export function getUserAIQuota() {
	return $request({
		url: '/app/user/ai-quota',
		method: 'GET'
	})
}

/**
 * 通义千问内容生成API
 */

/**
 * 根据图片和视频URL生成文案
 * @param {Object} data - 请求参数
 * @param {Array} data.imageUrls - 图片URL列表
 * @param {String} data.videoUrl - 视频URL
 * @param {String} data.category - 类别
 * @param {String} data.productName - 产品名称
 * @param {String} data.productDescription - 产品描述
 * @param {String} data.style - 风格
 * @param {String} data.contentType - 内容类型：note-笔记，idle-闲置商品
 * @param {Boolean} data.titleOnly - 是否只生成标题
 */
export function generateContentFromUrls(data) {
	return $request({
		url: '/web/app/tongyi/content/generate',
		method: 'POST',
		data: data,
		timeout: 60000, // 60秒超时
		showLoading: false // 禁用自动loading，由调用方手动控制（显示"AI生成中"）
	})
}

/**
 * 根据上传的图片文件生成文案
 * @param {Array} files - 图片文件路径数组
 * @param {Object} options - 选项
 * @param {String} options.category - 类别
 * @param {String} options.productName - 产品名称
 * @param {String} options.productDescription - 产品描述
 * @param {String} options.style - 风格
 * @param {String} options.contentType - 内容类型：note-笔记，idle-闲置商品
 */
export function generateContentFromFiles(files, options = {}) {
	return new Promise((resolve, reject) => {
		// 如果只有一张图片，使用单文件上传
		if (files.length === 1) {
			uni.uploadFile({
				url: baseUrl + '/file/upload',
				filePath: files[0],
				name: 'file',
				header: {
					'accessToken': uni.getStorageSync('uniapp_token') || '',
					'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
				},
				success: (res) => {
					try {
						const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
						if (data && data.code === 200) {
							const imageUrl = data.data?.url || data.data
							generateContentFromUrls({
								imageUrls: [imageUrl],
								...options
							}).then(resolve).catch(reject)
						} else {
							reject(new Error(data?.msg || '上传失败'))
						}
					} catch (e) {
						reject(e)
					}
				},
				fail: reject
			})
			return
		}

		// 多文件时，尝试使用批量上传接口（H5端可以使用FormData）
		// #ifdef H5
		const formData = new FormData()
		const uploadPromises = files.map(filePath => {
			return fetch(filePath.startsWith('http') ? filePath : `file://${filePath}`)
				.then(res => res.blob())
				.then(blob => {
					const fileName = filePath.split('/').pop() || 'image.jpg'
					formData.append('files', blob, fileName)
					return { filePath, fileName }
				})
		})

		Promise.all(uploadPromises).then(() => {
			fetch(baseUrl + '/file/batch/upload', {
				method: 'POST',
				headers: {
					'accessToken': uni.getStorageSync('uniapp_token') || '',
					'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
				},
				body: formData
			})
				.then(res => res.json())
				.then(data => {
					if (data && data.code === 200 && data.data) {
						// 处理返回的数据格式：可能是数组，每个元素可能是对象（有url属性）或直接是URL字符串
						const imageUrls = data.data.map(file => {
							if (typeof file === 'string') {
								return file
							} else if (file && file.url) {
								return file.url
							} else if (file && typeof file === 'object') {
								// 尝试从对象中提取URL
								return file.url || file.data?.url || JSON.stringify(file)
							}
							return file
						}).filter(url => url && (url.startsWith('http://') || url.startsWith('https://')))

						console.log('批量上传成功，获取到的图片URLs:', imageUrls)
						if (imageUrls.length === 0) {
							reject(new Error('批量上传成功但未获取到有效的图片URL'))
							return
						}

						generateContentFromUrls({
							imageUrls: imageUrls,
							...options
						}).then(resolve).catch(reject)
					} else {
						reject(new Error(data?.msg || '批量上传失败'))
					}
				})
				.catch(reject)
		}).catch(reject)
		// #endif

		// #ifndef H5
		// 非H5端（小程序/APP），uni.uploadFile 不支持多文件，逐个上传
		const singleUploadPromises = files.map(filePath => {
			return new Promise((uploadResolve, uploadReject) => {
				uni.uploadFile({
					url: baseUrl + '/file/upload',
					filePath: filePath,
					name: 'file',
					header: {
						'accessToken': uni.getStorageSync('uniapp_token') || '',
						'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
					},
					success: (res) => {
						try {
							const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
							if (data && data.code === 200) {
								uploadResolve(data.data?.url || data.data)
							} else {
								uploadReject(new Error(data?.msg || '上传失败'))
							}
						} catch (e) {
							uploadReject(e)
						}
					},
					fail: uploadReject
				})
			})
		})

		Promise.all(singleUploadPromises).then(imageUrls => {
			// 上传成功后调用生成API
			generateContentFromUrls({
				imageUrls: imageUrls,
				...options
			}).then(resolve).catch(reject)
		}).catch(reject)
		// #endif
	})
}

/**
 * 根据上传的视频文件生成文案
 * @param {String} filePath - 视频文件路径
 * @param {Object} options - 选项
 * @param {String} options.category - 类别
 * @param {String} options.productName - 产品名称
 * @param {String} options.productDescription - 产品描述
 * @param {String} options.style - 风格
 * @param {String} options.contentType - 内容类型：note-笔记，idle-闲置商品
 */
export function generateContentFromVideo(filePath, options = {}) {
	return new Promise((resolve, reject) => {
		// 先上传视频获取URL
		uni.uploadFile({
			url: baseUrl + '/file/upload',
			filePath: filePath,
			name: 'file',
			header: {
				'accessToken': uni.getStorageSync('uniapp_token') || '',
				'Authorization': 'Bearer ' + (uni.getStorageSync('uniapp_token') || '')
			},
			success: (res) => {
				try {
					const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
					if (data && data.code === 200) {
						const videoUrl = data.data?.url || data.data
						// 上传成功后调用生成API
						generateContentFromUrls({
							videoUrl: videoUrl,
							...options
						}).then(resolve).catch(reject)
					} else {
						reject(new Error(data?.msg || '上传失败'))
					}
				} catch (e) {
					reject(e)
				}
			},
			fail: reject
		})
	})
}

/**
 * 本地模拟AI回复（用于开发测试）
 * 实际项目中应该调用真实的AI API
 */
export function simulateAIResponse(userInput) {
	return new Promise((resolve) => {
		// 模拟网络延迟
		setTimeout(() => {
			let response = generateMockResponse(userInput)
			resolve({
				code: 200,
				data: {
					content: response,
					messageId: 'msg_' + Date.now(),
					timestamp: Date.now()
				}
			})
		}, 1000 + Math.random() * 2000)
	})
}

function generateMockResponse(input) {
	const responses = {
		greeting: [
			'你好！很高兴为你服务。有什么我可以帮助你的吗？',
			'Hi！我是你的AI助手，随时为你解答问题。',
			'你好！今天想聊什么话题呢？'
		],
		creative: [
			'我可以帮你进行创作！比如写文章、想标题、构思内容等。请告诉我你想创作什么类型的内容？',
			'创作是一个很有趣的过程！我可以为你提供灵感、结构建议或者具体的写作技巧。',
			'让我们一起创作吧！无论是文案、故事还是其他内容，我都能为你提供帮助。'
		],
		xiaohongshu: [
			'关于小红书笔记创作，我建议：\n1. 选择热门话题和标签\n2. 配图要美观吸引人\n3. 标题要有吸引力\n4. 内容要有价值和干货\n5. 适当互动回复评论',
			'小红书内容创作技巧：\n• 封面图要抓眼球\n• 标题包含关键词\n• 内容结构清晰\n• 多用表情和符号\n• 引导用户互动',
			'想在小红书获得更多关注？试试这些方法：\n- 找准目标用户群体\n- 保持内容垂直度\n- 定期发布优质内容\n- 积极与粉丝互动'
		],
		help: [
			'我可以帮你：\n• 回答各种问题\n• 提供创作建议\n• 分析内容策略\n• 生活小贴士\n• 学习辅导\n还有什么具体想了解的吗？',
			'我的功能包括：\n1. 智能问答\n2. 创作辅助\n3. 学习帮助\n4. 生活建议\n5. 情感支持\n有什么需要帮助的吗？'
		],
		default: [
			'这是一个很有趣的问题。让我从几个角度来分析：\n1. 首先需要明确具体的需求和目标\n2. 然后分析现有的条件和资源\n3. 最后制定合适的解决方案\n\n你希望我从哪个方面详细展开呢？',
			'基于你的问题，我认为可以这样考虑：\n• 明确问题的核心\n• 收集相关信息\n• 分析可能的解决方案\n• 选择最适合的方法\n\n需要我详细解释某个步骤吗？',
			'这个话题很值得探讨。我建议我们可以从不同维度来看待这个问题，这样能够得到更全面的理解。你觉得从哪个方面开始比较好？'
		]
	}

	const lowerInput = input.toLowerCase()

	if (lowerInput.includes('你好') || lowerInput.includes('hi') || lowerInput.includes('hello')) {
		return getRandomResponse(responses.greeting)
	} else if (lowerInput.includes('创作') || lowerInput.includes('写作') || lowerInput.includes('文案')) {
		return getRandomResponse(responses.creative)
	} else if (lowerInput.includes('小红书') || lowerInput.includes('笔记') || lowerInput.includes('内容')) {
		return getRandomResponse(responses.xiaohongshu)
	} else if (lowerInput.includes('帮助') || lowerInput.includes('功能') || lowerInput.includes('能做什么')) {
		return getRandomResponse(responses.help)
	} else {
		return getRandomResponse(responses.default)
	}
}

function getRandomResponse(responses) {
	return responses[Math.floor(Math.random() * responses.length)]
}
