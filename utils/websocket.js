import { imWsUrl } from '../.env.js'
import {
	saveFile
} from '../utils/fileUtil.js'
import sqliteUtil from '../utils/sqliteUtil.js'
import {
	imageUrlToBase64,
	replaceImageMessage,
	replaceImageTags,
	formatTimestamp
} from '../utils/util.js';
import {
    createAlarm
} from "@/utils/nativeMsg.js";
import { getCountMessage, getChatUserList } from '@/apis/mesasage_apis.js'
//是否已经连接上ws
let isOpenSocket = false
//心跳间隔，单位毫秒（改为30秒，与web端保持一致）
let heartBeatDelay = 30000
let heartBeatInterval = null
//心跳超时，单位毫秒（如果60秒内没有收到任何消息，认为连接断开）
let heartBeatTimeout = 60000
let lastMessageTime = Date.now() // 最后一次收到消息的时间

//最大重连次数
let reconnectTimes = 10
let reconnectInterval = null
//重连间隔，单位毫秒
let reconnectDelay = 10000

// 连接地址在 init 时按当前用户ID拼接
let wsUrl = ''

let socketTask = null

/** H5 自定义双行浮层：移除上一次，避免堆叠 */
let systemNotifyH5Timer = null
let systemNotifyH5El = null

/**
 * 系统通知：标题与正文分上下展示。uni.showToast 的 title 在多数端会把换行挤成一行，故按端分流。
 */
function showSystemNotificationTips(rawContent) {
	const title = '系统通知'
	let line = typeof rawContent === 'string' ? rawContent : String(rawContent || '')
	if (!line) return
	const maxLen = 280
	if (line.length > maxLen) line = line.slice(0, maxLen) + '…'

	// #ifdef APP-PLUS
	try {
		plus.nativeUI.toast(title + '\n' + line, {
			duration: 'long',
			verticalAlign: 'center',
			align: 'center'
		})
	} catch (e) {
		uni.showModal({
			title,
			content: line,
			showCancel: false,
			confirmText: '知道了'
		})
	}
	return
	// #endif

	// #ifdef H5
	try {
		if (typeof document !== 'undefined') {
			if (systemNotifyH5Timer) {
				clearTimeout(systemNotifyH5Timer)
				systemNotifyH5Timer = null
			}
			if (systemNotifyH5El && systemNotifyH5El.parentNode) {
				systemNotifyH5El.parentNode.removeChild(systemNotifyH5El)
			}
			const wrap = document.createElement('div')
			wrap.style.cssText =
				'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);z-index:99999;' +
				'background:rgba(58,58,58,.92);color:#fff;padding:14px 18px;border-radius:10px;' +
				'max-width:78vw;text-align:center;box-sizing:border-box;pointer-events:none;'
			const titleEl = document.createElement('div')
			titleEl.textContent = title
			titleEl.style.cssText = 'font-weight:600;margin-bottom:8px;font-size:16px;line-height:1.4;'
			const bodyEl = document.createElement('div')
			bodyEl.textContent = line
			bodyEl.style.cssText = 'opacity:0.95;word-break:break-all;font-size:14px;line-height:1.45;'
			wrap.appendChild(titleEl)
			wrap.appendChild(bodyEl)
			document.body.appendChild(wrap)
			systemNotifyH5El = wrap
			systemNotifyH5Timer = setTimeout(() => {
				if (wrap.parentNode) wrap.parentNode.removeChild(wrap)
				systemNotifyH5El = null
				systemNotifyH5Timer = null
			}, 4200)
			return
		}
	} catch (e) {}
	// #endif

	uni.showModal({
		title,
		content: line,
		showCancel: false,
		confirmText: '知道了'
	})
}

//这个参数是防止重连失败之后onClose方法会重复执行reconnect方法，导致重连定时器出问题
//连接并打开之后可重连，且只执行重连方法一次
let canReconnect = false

//封装的对象，最后以模块化向外暴露，
//init方法 初始化socketTask对象
//completeClose方法 完全将socketTask关闭（不重连）
//其他关于socketTask的方法与uniapp的socketTask api一致
let ws = {
	socketTask: null,
	init,
	send,
	completeClose,
	setCornerMark,
}
// 定时器，10秒内服务器未应答,没有将对应的消息定时器清除，将对应的消息状态设置为发送失败
let timer = {}

function init() {
    const uid = (uni.getStorageSync('userInfo') || {}).id
    const base = (imWsUrl || '').endsWith('/') ? imWsUrl : (imWsUrl + '/')
    wsUrl = base + uid
	socketTask = uni.connectSocket({
		url: wsUrl,
		fail: (res) => {
			console.log(res);
		}
	})
	socketTask.onOpen(() => {
		clearInterval(heartBeatInterval);
		clearInterval(reconnectInterval);
		isOpenSocket = true;
		canReconnect = true;
		lastMessageTime = Date.now(); // 重置最后消息时间
		send({
			from: uni.getStorageSync('userInfo').id,
			messageType: 0,
		});
		heartBeat();
		// 上线即同步一次角标（总数与各模块）
		try { setCornerMark() } catch(e) {}
	})
	socketTask.onMessage((res) => {
		let message = JSON.parse(res.data)
		lastMessageTime = Date.now(); // 更新最后消息时间
		console.log(message)
		// 兼容 Web 端的 msgType：
		// 0 = 未读数更新（计数聚合），1 = 单条聊天消息
		if (typeof message.msgType === 'number') {
			if (message.msgType === 0) {
				// 刷新角标（从后端拉取最新计数）
				setCornerMark()
				return
			}
			if (message.msgType === 1) {
				// 单条聊天消息抵达时，刷新会话列表与角标
				try {
					uni.$emit('updateMessageList')
				} catch (e) {}
				setCornerMark()
					// 广播标准化的新消息事件，供会话页实时渲染
					try {
						const evt = {
							id: message.id,
							fromUid: message.from || message.sendUid || message.fromUid,
							toUid: message.to || message.acceptUid || message.toUid,
							content: message.content,
							msgType: message.msgType || message.chatType,
							timestamp: message.time || message.timestamp
						}
						uni.$emit('im:newMessage', evt)
					} catch(e) {}
				// 继续走下方老协议处理（如果后端同时带有 messageType=3 的老格式可被兼容）
			}
			if (message.msgType === 99 && message.sendUid === 'system') {
				try {
					uni.$emit('system:notification', {
						content: message.content,
						imageUrl: message.imageUrl,
						timestamp: message.time || message.timestamp || Date.now()
					})
				} catch (e) {}
				const raw =
					typeof message.content === 'string'
						? message.content.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
						: ''
				const tip = raw || (message.imageUrl ? '系统通知（含配图）' : '')
				if (tip) {
					showSystemNotificationTips(tip)
				}
			}
		}
			// 聊天信息（老协议 messageType=3）
		if (message.messageType === 3) {
			console.log('聊天信息')
			if (sqliteUtil.isOpen()) {
				console.log('数据库打开成功')
				// 更新消息列表
				let s = `select * from message_list where user_id='${message.from}'`
				sqliteUtil.SqlSelect(s).then(res => {
					console.log(res)
					imageUrlToBase64(message.fromAvatar).then(img => {
						let con = replaceImageTags(message.content)
						// 兼容老协议：chatType 4 或 3 视为语音
						if (message.chatType === 4 || message.chatType === 3) {
							con = '[语音]'
						}
						if (res.length > 0) {
							let sql =
								`UPDATE message_list SET last_message="${con}", last_time=${message.time}, stranger=${message.friendType}, avatar_url='${img}', user_name='${message.fromName}', unread_num=unread_num+1 WHERE user_id='${message.from}'`;
							sqliteUtil.SqlExecute(sql).then(res => {
								setCornerMark()
							})
						} else {
							let sql =
								`INSERT INTO message_list (user_id, stranger, last_message, last_time, avatar_url, user_name, unread_num) VALUES ('${message.from}', ${message.friendType}, "${con}", ${message.time}, '${img}', '${message.fromName}', 1)`;
							sqliteUtil.SqlExecute(sql).then(res => {
								setCornerMark()
							})
						}
						uni.$emit('updateMessageList')
					})
				})
				// 更新聊天记录，先判断是否存在与该用户的聊天记录表，如果不存在则创建
				let s2 = `create table if not exists chat_${message.from} (
				id integer primary key autoincrement, 
				from_id text, 
				to_id text, 
				content text, 
				time integer, 
				chat_type integer, 
				is_read integer,
				is_send integer,
				audio_time integer);`
				replaceImageMessage(message.content).then(res => {
					message.content = res
					console.log(message.content)
					sqliteUtil.SqlExecute(s2).then(res => {
						// 插入聊天记录
						let sql =
							`INSERT INTO chat_${message.from} (from_id, to_id, content, time, chat_type,is_read, is_send,audio_time) VALUES ('${message.from}', '${message.to}', "${message.content}", ${message.time}, ${message.chatType},0,1,${message.audioTime})`
						sqliteUtil.SqlExecute(sql).then(res => {
							console.log('插入聊天记录成功')
							uni.$emit('updateChatRecord')
						})
					})
				})
						// 同步广播标准化的新消息事件
						try {
							const evt = {
								id: message.id,
								fromUid: message.from,
								toUid: message.to,
								content: message.content,
								msgType: message.chatType,
								timestamp: message.time
							}
							uni.$emit('im:newMessage', evt)
						} catch(e) {}
				let alarmId = Date.now()
				let msg = {
					alarmId: alarmId,
					warningTypeStr: message.fromName,
					projectName: formatTimestamp(message.time),
					description: (message.chatType === 4 || message.chatType === 3) ? '[语音]' : replaceImageTags(message.content),
				};
				createAlarm(msg, res => {
					if (res.type === 'click') {
                        uni.navigateTo({
                            url: `/pkg-msg/pages/chat/chat?userId=${message.from}&userName=${message.fromName}&avatarUrl=${message.fromAvatar}`
                        })
					}
				})
			}
		}
		// 新增关注
		if (message.messageType == 4) {
			let sql =
				`INSERT INTO attention_message (user_id, avatar_url, user_name, content) VALUES ('${message.from}', '${message.fromAvatar}', '${message.fromName}', '${message.content}')`
			sqliteUtil.SqlExecute(sql).then(res => {
				console.log('新增关注成功')
				let sql2 = `UPDATE system_message SET unread_num=unread_num+1 WHERE id=2`
				sqliteUtil.SqlExecute(sql2).then(res => {
					uni.$emit('updateAttentionList')
					setCornerMark()
				})
			})
		}
		// 服务器应答
		if (message.messageType === 5) {
			console.log('服务器应答')
			if (sqliteUtil.isOpen()) {
				// 0代表发送中，1代表发送成功，2代表发送失败
				// 0未读，1已读
				let status = 1
				if (message.content) {
					status = 2
				}
				message.status = status
				// 清除定时器
				clearTimeout(timer['time' + message.to + message.id])
				// 更新聊天记录
				let sql =
					`UPDATE chat_${message.to} SET is_send=${status} WHERE id=${message.id} and from_id='${message.from}' and to_id='${message.to}'`
				sqliteUtil.SqlExecute(sql).then(res => {
					console.log('更新聊天记录成功')
					uni.$emit('updateMsgStatus', message)
				})
			}
		}
		// 鉴权失败
		if (message.messageType === 6) {
			console.log('鉴权失败')
			uni.showToast({
				title: message.content == null ? '登录状态失效，请重新登录' : message.content,
				icon: 'none',
				duration: 1000,
				mask: true
			})
			uni.removeStorageSync('userInfo')
			uni.removeStorageSync('uniapp_token')
			completeClose()
			setTimeout(() => {
				uni.reLaunch({
					url: '/pkg-auth/pages/login/login'
				})
			}, 1000)
		}
		if (message.messageType == 8) {
			console.log('新增点赞或收藏')
			console.log(message)
			let content = JSON.parse(message.content).text
			let notesId = JSON.parse(message.content).notesId
			let notesType = JSON.parse(message.content).notesType
			let coverPicture = JSON.parse(message.content).notesCoverPicture
			let sql =
				`INSERT INTO praise_and_collection (user_id, avatar_url, user_name, content, notes_id, notes_type,notes_cover_picture, time) VALUES ('${message.from}', '${message.fromAvatar}', '${message.fromName}', '${content}', '${notesId}', '${notesType}', '${coverPicture}', ${message.time})`
			sqliteUtil.SqlExecute(sql).then(res => {
				console.log('新增点赞或收藏成功')
				let sql2 = `UPDATE system_message SET unread_num=unread_num+1 WHERE id=1`
				sqliteUtil.SqlExecute(sql2).then(res => {
					uni.$emit('updatePraiseAndCollectionList')
					setCornerMark()
				})
			})
		}
	})
	socketTask.onClose(() => {
		isOpenSocket = false;
		if (canReconnect) {
			reconnect();
			canReconnect = false;
		}
	})
	ws.socketTask = socketTask;
}


function heartBeat() {
	// 清除旧的心跳定时器
	if (heartBeatInterval) {
		clearInterval(heartBeatInterval);
	}
	
	heartBeatInterval = setInterval(() => {
		// 检查是否超时（60秒内没有收到任何消息）
		const now = Date.now();
		if (now - lastMessageTime > heartBeatTimeout) {
			console.warn('⚠️ WebSocket 心跳超时，连接可能已断开，尝试重连');
			// 关闭当前连接并重连
			if (socketTask) {
				socketTask.close();
			}
			return;
		}
		
		// 检查连接状态
		if (socketTask && socketTask.readyState === 1) { // 1 = OPEN
			let heartBeatText = {
				from: uni.getStorageSync('userInfo').id,
				content: uni.getStorageSync('uniapp_token'),
				messageType: 1,
			}
			send(heartBeatText);
			console.log('💓 发送心跳消息');
		} else {
			console.warn('⚠️ WebSocket 未连接，无法发送心跳');
		}
	}, heartBeatDelay)
}

function send(value) {
	if (value.messageType === 3) {
		timer['time' + value.to + value.id] = setTimeout(() => {
			console.log('发送超时')
			if (sqliteUtil.isOpen()) {
				let sql =
					`UPDATE chat_${value.to} SET is_send=2 WHERE id=${value.id} and from_id='${value.from}' and to_id='${value.to}'`
				sqliteUtil.SqlExecute(sql).then(res => {
					console.log('更新消息状态成功')
					value.status = 2
					value.content = '发送失败'
					uni.$emit('updateMsgStatus', value)
				})
			}
		}, 10000)
	}
	if (ws.socketTask.readyState === 1) {
		ws.socketTask.send({
			data: JSON.stringify(value),
			success(res) {
				// console.log("消息发送成功");
				return true;
			},
			fail() {
				console.log("消息发送失败");
				completeClose();
				init();
				return false;
			}
		});
	} else {
		console.log("WebSocket 连接已关闭，无法发送消息");
		completeClose();
		init();
		// 观察10次，如果10次都没有连接成功，则不再发送消息
		let count = 0;
		// 每1秒观察一次是否连接成功，连接成功后发送消息
		let interval = setInterval(() => {
			if (count >= 10) {
				clearInterval(interval)
				return false;
			}
			console.log('连接状态：' + ws.socketTask.readyState)
			if (ws.socketTask.readyState === 1) {
				ws.socketTask.send({
					data: JSON.stringify(value),
					success(res) {
						// console.log("消息发送成功");
						clearInterval(interval)
						return true;
					},
					fail() {
						console.log("消息发送失败");
						completeClose();
						init();
						return false;
					}
				});
			}
			count++
		}, 1000)
		return false;
	}
}

function reconnect() {
	//停止发送心跳
	//如果不是人为关闭的话，进行重连
	if (!isOpenSocket) {
		completeClose();
		let count = 0;
		reconnectInterval = setInterval(() => {
			console.log("正在尝试重连")
			init();
			count++
			if (count >= reconnectTimes) {
				clearInterval(reconnectInterval)
				console.log("网络异常或服务器错误")
			}
		}, reconnectDelay)
	}
}

function completeClose() {
	console.log("关闭连接")
	clearInterval(heartBeatInterval);
	clearInterval(reconnectInterval);
	canReconnect = false;
	ws.socketTask = null;
	if (ws.socketTask) {
		ws.socketTask.close();
	}
}

function setCornerMark() {
    // 同时拉取：顶部三个模块计数 + 对话列表未读聚合
    Promise.all([getCountMessage(), getChatUserList()])
        .then(([resMeta, resList]) => {
            const data = resMeta?.data || {}
            const likeOrCollectionCount = Number(data.likeOrCollectionCount) || 0
            const commentCount = Number(data.commentCount) || 0
            const followCount = Number(data.followCount) || 0
            // 如果后端已提供 chatCount/productChatCount 也计入，但以列表聚合为准
            const list = Array.isArray(resList?.data) ? resList.data : []
            const chatUnreadFromList = list.reduce((sum, it) => sum + (parseInt(it.count || 0, 10) || 0), 0)
            const backendChatTotal = (Number(data.chatCount) || 0) + (Number(data.productChatCount) || 0)
            const chatTotal = Math.max(chatUnreadFromList, backendChatTotal)
            const total = chatTotal + likeOrCollectionCount + commentCount + followCount

            try { uni.setStorageSync('im_total_unread', total) } catch(e) {}
            uni.$emit('im:setTabBadge', total)
            uni.$emit('updateTabBarBadge', { index: 3, badge: total })

            // 广播顶部三个模块的明细未读数
            uni.$emit('im:setMetaCounts', {
                likeOrCollectionCount,
                commentCount,
                followCount,
                chatCount: chatTotal,
                productChatCount: Number(data.productChatCount) || 0,
            })
        })
        .catch(() => {
            // 回退到本地 SQLite 统计（如仍启用）
            const s = `select sum(unread_num) as total from message_list`
            sqliteUtil.SqlSelect(s).then(res => {
                let total = res[0]?.total || 0
                const sql = `SELECT unread_num FROM system_message`
                sqliteUtil.SqlSelect(sql).then(list => {
                    list.forEach(item => { total += item.unread_num })
                    try { uni.setStorageSync('im_total_unread', total) } catch(e) {}
                    uni.$emit('im:setTabBadge', total)
                    uni.$emit('updateTabBarBadge', { index: 3, badge: total })
                })
            })
        })
}

module.exports = ws