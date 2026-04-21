import {
	$request
} from '../utils/request.js'

export const getChatUserList = () => {
    // 禁用缓存，确保拿到最新会话
    return $request({
        url: '/web/im/chat/getChatUserList',
        method: 'GET',
        cache: false,
        showLoading: false // 消息列表不显示 loading
    })
}

/** C端：管理端「系统通知配置」列表（非通知公告） */
export const getSystemNotificationConfigList = () => {
	return $request({
		url: '/web/im/chat/systemNotificationConfigList',
		method: 'GET',
		cache: false,
		showLoading: false
	})
}

/** 闲宝消息：获取商品对话用户列表（头像、昵称、时间、商品卡片、未读等） */
export const getProductChatUserList = () => {
    return $request({
        url: '/web/im/chat/productChatUserList',
        method: 'GET',
        cache: false,
        showLoading: false
    })
}

export const getHistoryChatList = (params = {}) => {
	const acceptUid = params.userId
	const page = params.page || 1
	const limit = params.pageSize || 10
	const data = { acceptUid }
	if (params.productId != null && params.productId !== '') data.productId = params.productId
	return $request({
		url: `/web/app/im/chat/getAllChatRecord/${page}/${limit}`,
		method: 'GET',
		data,
		showLoading: false // 聊天记录不显示 loading
	})
}

/** 商品对话历史（web 端商品会话专用：/web/im/chat/productChatRecord） */
export const getProductChatRecord = (params = {}) => {
	const acceptUid = params.userId
	const page = params.page || 1
	const limit = params.pageSize || 10
	const productId = params.productId
	if (acceptUid == null || acceptUid === '' || productId == null || productId === '') {
		return Promise.reject(new Error('getProductChatRecord 需要 userId 和 productId'))
	}
	return $request({
		url: `/web/im/chat/productChatRecord/${page}/${limit}`,
		method: 'GET',
		data: { acceptUid, productId },
		showLoading: false
	})
}

export const clearMessageCount = (params = {}) => {
    // 兼容旧入参：优先使用 params.sendUid；若无则回退到 params.userId
    const sendUid = params.sendUid != null ? params.sendUid : params.userId
    const type = params.type
    const productId = params.productId
    const groupChatId = params.groupChatId
    return $request({
        url: '/web/app/im/chat/clearMessageCount',
        method: 'GET',
        data: {
            sendUid,
            type,
            productId,
            groupChatId
        }
    })
}

export const sendMsg = (params = {}) => {
	const body = {
		sendUid: params.sendUid,
		acceptUid: params.acceptUid,
		content: params.content,
		msgType: typeof params.msgType === 'number' ? params.msgType : parseInt(params.msgType || 1, 10),
		chatType: typeof params.chatType === 'number' ? params.chatType : parseInt(params.chatType || 0, 10),
		productId: params.productId || undefined,
		groupChatId: params.groupChatId || undefined,
		// 语音时长（秒），仅语音消息需要，其它类型忽略
		audioTime: typeof params.audioTime === 'number'
			? params.audioTime
			: (params.audioTime != null ? parseInt(params.audioTime, 10) || undefined : undefined)
	}
	return $request({
		url: `/web/app/im/chat/sendMsg`,
		method: 'POST',
		header: { 'Content-Type': 'application/json' },
		data: JSON.stringify(body)
	})
}

export const getNoticeLikeOrCollection = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/likeOrCollection/getNoticeLikeOrCollection/${page}/${limit}`,
		method: 'GET',
		showLoading: false // 点赞收藏通知不显示 loading
	})
}

export const getNoticeAttention = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/follower/getNoticeFollower/${page}/${limit}`,
		method: 'GET',
		showLoading: false // 关注通知不显示 loading
	})
}

export const getNoticeComment = (params = {}) => {
	const page = params.page || 1
	const limit = params.pageSize || 10
	return $request({
		url: `/web/app/comment/getNoticeComment/${page}/${limit}`,
		method: 'GET',
		showLoading: false // 评论通知不显示 loading
	})
}

// 获取各类未读数量（chatCount、productChatCount、likeOrCollectionCount、commentCount、followCount）
export const getCountMessage = () => {
    return $request({
        url: '/web/app/im/chat/getCountMessage',
        method: 'GET',
        cache: false,
        showLoading: false // 未读消息数量不显示 loading
    })
}

export const getFriend = () => {
	return $request({
		url: `/web/app/follower/getFriend/${page}/${limit}`,
		method: 'GET',
		showLoading: false // 好友列表不显示 loading
	})
}

// ========== 群聊相关API ==========

/**
 * 创建群聊
 */
export const createGroup = (params = {}) => {
	return $request({
		url: '/web/app/im/group/create',
		method: 'POST',
		data: {
			groupName: params.groupName,
			groupAvatar: params.groupAvatar,
			groupDescription: params.groupDescription,
			memberIds: params.memberIds || [] // 直接传递数组，后端会自动接收
		}
	})
}

/**
 * 获取我的群聊列表
 * 后端 GET /web/app/im/group/myList 建议返回每条：
 *   - id, groupName, groupAvatar
 *   - lastMessage / content（最后一条消息内容）
 *   - lastMessageTime / timestamp（最后一条消息时间）
 *   - unreadCount（未读数）
 *   - 最后一条消息发送者（任选其一即可，用于消息列表展示「发送者：消息」）：
 *     lastMessageSenderName / lastSenderName / senderName / lastMessageSender
 * 若未返回发送者字段，前端将显示「群成员：消息」
 */
export const getMyGroupList = () => {
	return $request({
		url: '/web/app/im/group/myList',
		method: 'GET',
		showLoading: false
	})
}

/**
 * 获取群聊详情
 */
export const getGroupDetail = (groupChatId) => {
	return $request({
		url: `/web/app/im/group/detail/${groupChatId}`,
		method: 'GET'
	})
}

/**
 * 获取群成员列表
 */
export const getGroupMemberList = (groupChatId) => {
	return $request({
		url: `/web/app/im/group/members/${groupChatId}`,
		method: 'GET'
	})
}

/**
 * 加入群聊
 */
export const joinGroup = (groupChatId) => {
	return $request({
		url: `/web/app/im/group/join/${groupChatId}`,
		method: 'POST'
	})
}

/**
 * 退出群聊
 */
export const quitGroup = (groupChatId) => {
	return $request({
		url: `/web/app/im/group/quit/${groupChatId}`,
		method: 'POST'
	})
}

/**
 * 解散群聊
 */
export const dissolveGroup = (groupChatId) => {
	return $request({
		url: `/web/app/im/group/dissolve/${groupChatId}`,
		method: 'POST'
	})
}

/**
 * 获取群聊历史消息
 */
export const getGroupChatHistory = (params = {}) => {
	const groupChatId = params.groupChatId
	const page = params.page || 1
	const limit = params.pageSize || 10
	// 注意：这里使用私聊接口，但通过groupChatId参数区分
	// 后端需要根据groupChatId查询群聊消息
	return $request({
		url: `/web/app/im/chat/getAllChatRecord/${page}/${limit}`,
		method: 'GET',
		data: {
			groupChatId: groupChatId,
			chatType: 1 // 群聊
		},
		showLoading: false
	})
}

/**
 * 更新群信息
 */
export const updateGroupInfo = (params = {}) => {
	// 构建查询参数字符串
	const queryParams = []
	if (params.groupChatId) queryParams.push(`groupChatId=${encodeURIComponent(params.groupChatId)}`)
	if (params.groupName) queryParams.push(`groupName=${encodeURIComponent(params.groupName)}`)
	if (params.groupAvatar) queryParams.push(`groupAvatar=${encodeURIComponent(params.groupAvatar)}`)
	if (params.groupDescription !== undefined) queryParams.push(`groupDescription=${encodeURIComponent(params.groupDescription || '')}`)
	
	return $request({
		url: `/web/app/im/group/updateInfo?${queryParams.join('&')}`,
		method: 'POST'
	})
}

/**
 * 移除群成员
 */
export const removeMember = (groupChatId, userId) => {
	return $request({
		url: `/web/app/im/group/removeMember?groupChatId=${groupChatId}&userId=${userId}`,
		method: 'POST'
	})
}