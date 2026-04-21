<template>
	<view>
		<!-- 状态栏背景 - 白色 -->
		<view :style="{height:statusBarHeight+'px'}"
			style="position: fixed;z-index: 100;width: 100%;background-color: #ffffff;"></view>
		<view :style="{top: statusBarHeight+'px'}"
			style="display: flex;height: 44px;align-items: center;position: fixed;z-index: 100;width: 100%;background-color: #ffffff;">
			<!-- #ifndef MP-WEIXIN -->
			<u-icon name="arrow-left" size="21" style="margin-left: 20rpx;" @click="backLastPage"></u-icon>
			<!-- #endif -->
			<image mode="aspectFill" style="height: 34px;width: 34px;border-radius: 50%;margin-left: 15rpx;"
				:src="targetUser.avatarUrl" @click="goToOtherMine(targetUser.userId)">
			</image>
			<view style="margin-left: 15rpx;">
				<view style="font-size: 33rpx;color: #2b2b2b;">{{targetUser.userName}}</view>
				<!-- <view style="color: #95949a;font-size: 26rpx;">今天在线</view> -->
			</view>
			<view style="margin-left: auto;margin-right: 15px;">
				<u-icon name="more-dot-fill" color="#2b2b2b" size="26"></u-icon>
			</view>
		</view>
		<view :style="{height: statusBarHeight+44+'px'}"></view>
		<u-notify ref="uNotify"></u-notify>
		<scroll-view id="scrollChat" scroll-y :scroll-with-animation="scrollAnimation" :scroll-top="scrollTop"
			:style="{height: chatMsgHeight+'px'}" @scrolltoupper="getChatHistory(page)" upper-threshold="100"
			@click="closeME">
			<view id="scrollMsg">
				<u-loadmore v-if="msgLoading==='nomore'" :status="msgLoading" line font-size="12" />
				<view v-for="(item,index) in messageList" v-bind:key="item.id">
					<!-- 时间显示 -->
					<view v-if="item.showTime"
						style="text-align: center;margin: 15rpx;font-size: 25rpx;color: #95949a;">
						{{item.time}}
					</view>

					<!-- 自己发送的消息 -->
					<view v-if="item.fromId===userInfo.userId"
						style="display: flex;padding: 20rpx;justify-content: flex-end;align-items: flex-start;">
						<view style="margin-right: 20rpx;">
							<u-icon v-if="item.isSend===2" name="error-circle-fill" color="#3d8af5" size="20"></u-icon>
							<u-loading-icon size="17" :show="item.isSend===0"></u-loading-icon>
						</view>
						<!-- 文本消息 -->
						<view v-if="item.chatType===1"
							style="padding:15rpx;display: flex;background-color: #518FF1;border-radius: 30rpx;margin-right: 15rpx;">
							<rich-text
								style="margin-left: auto;max-width: 460rpx;color:#ffffff;word-wrap: break-word;word-break: break-all;"
								:nodes="item.content"></rich-text>
						</view>
						<!-- 图片消息 -->
						<view v-else-if="item.chatType===2" style="padding:15rpx;padding-right: 15rpx;">
							<rich-text @click="preImage" :nodes="item.content" :data-node="item.content"></rich-text>
						</view>
						<view style="margin-right: 15rpx;">
							<image mode="aspectFill" style="height: 80rpx;width: 80rpx;border-radius: 50%;"
								:src="userInfo.avatarUrl" @click="goToOtherMine(userInfo.userId)">
							</image>
						</view>
					</view>

					<!-- 对方发送的消息 -->
					<view v-else style="display: flex;padding: 20rpx;justify-content: flex-start;">
						<view style="margin-left: 15rpx;">
							<image mode="aspectFill" style="height: 80rpx;width: 80rpx;border-radius: 50%;"
								:src="targetUser.avatarUrl" @click="goToOtherMine(targetUser.userId)">
							</image>
						</view>
						<!-- 文本消息 -->
						<view v-if="item.chatType===1"
							style="padding:15rpx;display: flex;margin-left: 15rpx;margin-right:110rpx;background-color: #ffffff;border-radius: 30rpx;">
							<rich-text
								style="margin-right: auto;max-width: 460rpx;color: #2b2b2b;word-wrap: break-word;word-break: break-all;"
								:nodes="item.content"></rich-text>
						</view>
						<!-- 图片消息 -->
						<view v-else-if="item.chatType===2" style="padding: 15rpx;margin-left: 15rpx;">
							<rich-text @click="preImage" :nodes="item.content" :data-node="item.content"></rich-text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<view style="display: flex;padding: 20rpx;align-items: center;height: 80px;box-sizing: border-box;width: 100%;">
			<view style="background-color: #ffffff;padding: 20rpx;border-radius: 20rpx;flex: 1;">
				<textarea v-model="messageValue" style="width: 100%;caret-color: #F56C6C;color: #2b2b2b;"
					placeholder="请输入消息内容" auto-height :adjust-position="false" @focus="focus" @blur="blur" auto-blur
					:cursor="cursor"></textarea>
			</view>
			<view style="margin-left: 20rpx;display: flex;align-items: center;" @click="openEmoji">
				<image src="@/static/image/expression.png" style="width: 70rpx;" mode="widthFix"></image>
			</view>
			<u-transition :show="!isShowSend" mode="slide-down" :duration="300"
				style="display: flex;align-items: center;margin-left: 10rpx;" @click="openMore">
				<image src="@/static/image/add.png" style="width: 79rpx;" mode="widthFix"></image>
			</u-transition>
			<u-transition :show="isShowSend" mode="fade-right" :duration="700" style="margin-left: 10rpx;">
				<u-button type="primary" text="发送" @click="sendMessage"></u-button>
			</u-transition>
		</view>
		<u-popup :show="showRecordPop" mode="bottom" :round="10" @close="closeRecordPop"
			customStyle="background-color: #f5f5f5">
			<view
				style="text-align: center;padding: 30rpx;padding-bottom: 10rpx;display: flex;flex-direction: column;justify-content:center;">
				<view :class="['mic-layer',{'mic-layer-talking':isTalking}]">
					<!-- 按钮样式 -->
					<view :class="isTalking?'mic-btn-talking':'mic-btn'" @touchstart="touchStart" @touchend="onEnd"
						@longpress="onStart" @touchmove="handleRecordMove">
						<view v-show="!isTalking">
							<image src="@/static/image/recorded.png" style="height: 100px;" mode="heightFix">
							</image>
						</view>
						<view v-show="isTalking" class="mic-btn-talking_text">说话中...</view>
						<view v-show="isTalking&&!sendLock" class="tip-text"><text class="mr-10">松开</text>发送</view>
					</view>
					<!-- 语音音阶动画 -->
					<view v-if="isTalking" :class="['record-animate-box',{'active':sendLock}]">
						<view class="voice-scale">
							<view class="item" v-for="(item,index) in 10" :key="index"></view>
						</view>
					</view>
					<!-- 取消发送 -->
					<view v-if="isTalking" :class="['record-cancel',{'active':sendLock}]">
						<text class="close-icon">x</text>
						<view class="tip-text" v-show="sendLock"><text class="mr-10">松开</text>取消</view>
					</view>
				</view>
				<view style="margin-top: 30rpx;">按住开始录音</view>
			</view>
		</u-popup>
		<view v-if="showEmoji">
			<scroll-view :style="{height: keyboardHeight+'px'}" scroll-y style="background-color: #f7f8fa;">
				<view
					style="display: grid;padding: 20rpx;grid-template-columns: repeat(8,1fr);gap: 16rpx;text-align: center;">
					<block v-for="(em,index) in emojiList" v-bind:key="index">
						<view @click="addEmoji(em)" style="font-size: 48rpx;line-height: 100rpx;height: 100rpx;">
							{{ em }}
						</view>
					</block>
				</view>
			</scroll-view>
		</view>
		<u-action-sheet :actions="phoneTypeList" :closeOnClickOverlay="true" :closeOnClickAction="true"
			:show="showChoosePhoneType" @select="telPhone" @close="showChoosePhoneType = false" title="选择通话类型"
			round="15"></u-action-sheet>
		<view v-if="showMore" style="height: 220px;background-color: #f5f5f5;color: #2b2b2b;">
			<view
				style="display: grid;text-align: center;;grid-template-columns: repeat(4,1fr);padding: 20rpx;gap: 15rpx;row-gap: 35rpx;">
				<view style="text-align: center;" @click="chooseImage">
					<view style="padding: 12rpx 16rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/photoList.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix"></image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">相册</view>
				</view>
				<view style="text-align: center;" @click="takePhoto">
					<view style="padding: 12rpx 16rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/takePhoto.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">拍照</view>
				</view>
				<view style="text-align: center;" @click="showChoosePhoneType = true">
					<view style="padding: 12rpx 16rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/phone.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">电话</view>
				</view>
				<view style="text-align: center;">
					<view style="padding: 12rpx 16rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/files.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">文件</view>
				</view>
						<view style="text-align: center;flex-basis: calc((100% - 3 * 20rpx) / 4);"
					@tap="showRecordPop = true">
					<view style="padding: 12rpx 16rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/voice.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">语音</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const innerAudioContext = uni.createInnerAudioContext();
	try {
		innerAudioContext.obeyMuteSwitch = false
	} catch (e) {}
	import {
		getUserInfo
	} from '@/apis/user_service.js'
	import {
		getHistoryChatList,
		sendMsg,
		clearMessageCount
	} from '@/apis/mesasage_apis.js'
	import {
		timestampFormat,
		weChatTimeFormat,
		transData,
		replaceImageMessage,
	} from '@/utils/util.js'
	import {
		emojiList
	} from '@/utils/emojiUtil.js'
	import {
		baseUrl
	} from '@/.env.js'
	import {
		zoomOutImage
	} from '@/utils/fileUtil.js'
	export default {
		data() {
			return {
				phoneTypeList: [{
					id: 1,
					name: '语音通话',
					color: '#2b2b2b'
				}, {
					id: 2,
					name: '视频通话',
					color: '#2b2b2b'
				}],
				showChoosePhoneType: false,
				isTalking: false, // 是否正在讲话
				sendLock: false, // 语音是否发送锁，true-不发送，false-发送（用于上滑取消发送）
				record: null, // 语音对象
				startPoint: {}, //记录长按录音开始点信息,用于后面计算滑动距离。
				timer: null, // 计时器
				leng: 0, // 计时器时间
				currentPlay: '', // 当前播放的语音id
				statusBarHeight: '',
				chatMsgHeight: '',
				keyboardHeight: 302,
				isShowSend: false,
				targetUser: {
					userId: '',
					userName: '',
					avatarUrl: ''
				},
				userInfo: {
					userId: '',
					userName: '',
					avatarUrl: ''
				},
				messageList: [],
				page: 1,
				pageSize: 115,
				scrollTop: 0,
				msgLoading: 'loading',
				scrollAnimation: false,
				messageValue: '',
				showEmoji: false,
				// fo: false,
				emojiList: [],
				cursor: 0,
				showMore: false,
				showRecordPop: false,
			};
		},
		methods: {
			telPhone(index) {
				this.showChoosePhoneType = false
				console.log(index.id)
				this.$callUtils.call(this.targetUser.userId, index.id)
			},
			playAudio(item) {
				console.log('playAudio click', item && item.id, item && item.content)
				// try {
				// 	uni.showToast({ icon: 'none', title: '开始播放语音', duration: 600 })
				// } catch (e) {}
				const raw = item && item.content
				if (!raw) {
					uni.showToast({ icon: 'none', title: '语音地址为空' })
					return
				}
				const url = decodeURIComponent(String(raw))
				if (this.currentPlay === item.id) {
					try {
						innerAudioContext.pause()
					} catch (e) {}
					this.currentPlay = ''
					return
				}
				if (this.currentPlay !== '') {
					try {
						innerAudioContext.stop()
					} catch (e) {}
				}
				this.currentPlay = item.id
				try {
					innerAudioContext.src = url
					innerAudioContext.play()
				} catch (e) {
					this.currentPlay = ''
					
					return
				}
				innerAudioContext.onEnded(() => {
					this.currentPlay = ''
				})
				innerAudioContext.onError((err) => {
					console.error('audio play error', err)
					this.currentPlay = ''
					
				})
			},
			/**
			 * 开始录音
			 */
			onStart() {
				const option = {
					format: 'mp3'
				}
				console.log('start');
				this.isTalking = true
				this.leng = 0
				uni.vibrateLong()
				this.timer = setInterval(() => {
					this.leng++
					if (this.leng >= 60) {
						this.onEnd()
						return
					}
				}, 1000)
				this.record.start(option)
				this.record.onStart(res => {
					console.log('start', res);
				})
				this.record.onError(res => {
					console.log('录音错误事件：', res);
				})
			},
			/**
			 * 结束录音
			 */
			onEnd() {
				this.isTalking = false
				this.record.stop()
				clearTimeout(this.timer)
				this.record.onStop(res => {
					console.log(res, "录音回调地址");
					if (this.sendLock) {
						this.sendLock = false // 恢复锁状态
						console.log('取消发送');
						return
					} // 取消发送
					if (res.duration < 1000) {
						uni.showToast({
							icon: 'error',
							title: '说话时间太短'
						})
						return
					}
					uni.uploadFile({
						url: baseUrl + '/web/app/third/uploadAudio',
						filePath: res.tempFilePath,
						name: 'file',
						header: {
							'token': uni.getStorageSync('uniapp_token')
						},
						success: (res) => {
							let data = JSON.parse(res.data)
							if (data.code == 200) {
								console.log(data)
								this.sendAudio(data.data, this.leng)
							}
						}
					})
				})
			},
			sendAudio(url, time) {
				const content = url
				// 后端发送语音消息
				sendMsg({
					sendUid: this.userInfo.userId,
					acceptUid: this.targetUser.userId,
					content: content,
					msgType: 3, // 3 = 语音
					chatType: 0,
					audioTime: time
				}).finally(() => {
					// 本地乐观更新
					const msg = {
						id: Date.now(),
						fromId: this.userInfo.userId,
						toId: this.targetUser.userId,
						content: content,
						time: weChatTimeFormat(new Date().getTime()),
						chatType: 3,
						isRead: 1,
						isSend: 1,
						audioTime: time,
						showTime: true
					}
					this.messageList.push(msg)
					this.scrollToBottom()
				})
			},
			/**
			 * 开始触屏
			 * @param {Object} e
			 */
			touchStart(e) {
				this.startPoint.clientX = e.changedTouches[0].clientX; //手指按下时的X坐标
				this.startPoint.clientY = e.changedTouches[0].clientY; //手指按下时的Y坐标
			},
			/**
			 * 录音时手指滑动
			 * @param {Object} e
			 */
			handleRecordMove(e) {
				let touchData = e.touches[0]; //滑动过程中，手指滑动的坐标信息
				let moveX = touchData.clientX - this.startPoint.clientX;
				let moveY = touchData.clientY - this.startPoint.clientY;
				if (moveY > -45) { // 滑动距离不够则不取消发送
					this.sendLock = false
				} else {
					this.sendLock = true
				}
			},
			// 语音气泡固定宽度
			handleVoiceWidth() {
				return '100rpx'
			},
			closeRecordPop() {
				this.showRecordPop = false
			},
			takePhoto() {
				uni.chooseImage({
					count: 1,
					sizeType: ['original', 'compressed'],
					sourceType: ['camera'],
					success: (res) => {
						res.tempFilePaths.forEach(item => {
							uni.uploadFile({
								url: baseUrl + '/web/oss/upload',
								filePath: item,
								name: 'file',
								header: {
									'accessToken': uni.getStorageSync('uniapp_token') || ''
								},
								success: (res) => {
									let data = JSON.parse(res.data)
									if (data.code == 200) {
										console.log(data)
										this.sendImage(data.data)
									}
								}

							})
						})
					}
				})
			},
			preImage(e) {
				let s = e.target.dataset.node
				// <img src='${url}' style='max-width: 200px;height: auto;'></img>`
				// 使用正则表达式匹配<img>标签中的src属性值
				const srcRegex = /<img[^>]*src=['"]([^'"]+)['"][^>]*>/;
				const match = s.match(srcRegex);
				if (match) {
					const imgSrc = match[1];
					uni.previewImage({
						urls: [imgSrc]
					});
				}

			},
			chooseImage() {
				uni.chooseImage({
					count: 1,
					sizeType: ['original', 'compressed'],
					sourceType: ['album'],
					success: (res) => {
						res.tempFilePaths.forEach(item => {
							uni.uploadFile({
								url: baseUrl + '/web/oss/upload',
								filePath: item,
								name: 'file',
								header: {
									'accessToken': uni.getStorageSync('uniapp_token') || ''
								},
								success: (res) => {
									let data = JSON.parse(res.data)
									if (data.code == 200) {
										console.log(data)
										this.sendImage(data.data)
									}
								}
							})
						})
					}
				})
			},
			sendImage(url) {
				zoomOutImage(url).then(result => {
					const contentHtml =
						`<img src='${result.src}' style='width: ${result.width}px;height: ${result.height}px;border-radius: 10px;'></img>`
					sendMsg({
						sendUid: this.userInfo.userId,
						acceptUid: this.targetUser.userId,
						content: result.src,
						msgType: 2,
						chatType: 1
					}).finally(() => {
						const msg = {
							id: Date.now(),
							fromId: this.userInfo.userId,
							toId: this.targetUser.userId,
							content: contentHtml,
							time: weChatTimeFormat(new Date().getTime()),
							chatType: 2,
							isRead: 1,
							isSend: 1,
							audioTime: 0,
							showTime: true
						}
						this.messageList.push(msg)
						this.scrollToBottom()
					})
				})
			},
			openMore() {
				const morePanelHeight = 220; // 更多选项面板固定高度
				if (this.showMore) {
					this.showMore = false
					this.chatMsgHeight = this.chatMsgHeight + morePanelHeight
					this.scrollToBottom()
					return
				} else {
					if (this.showEmoji) {
						this.showEmoji = false
						this.chatMsgHeight = this.chatMsgHeight + this.keyboardHeight
						this.scrollToBottom()
					}
					this.showMore = true
					this.chatMsgHeight = this.chatMsgHeight - morePanelHeight
					this.scrollToBottom()
				}
			},
			addEmoji(emojiChar) {
				// 直接插入 Unicode 表情，保持与发布笔记一致
				const text = String(emojiChar)
				this.messageValue = this.messageValue.slice(0, this.cursor) + text + this.messageValue.slice(this.cursor)
				this.cursor = this.cursor + text.length
			},
			openEmoji() {
				const morePanelHeight = 220; // 更多选项面板固定高度
				// 如果表情列表已显示，则隐藏
				if (this.showEmoji) {
					this.showEmoji = false
					this.chatMsgHeight = this.chatMsgHeight + this.keyboardHeight
					this.scrollToBottom()
					return
				}
				// 如果更多选项已显示，先关闭它
				if (this.showMore) {
					this.showMore = false
					this.chatMsgHeight = this.chatMsgHeight + morePanelHeight
					this.scrollToBottom()
				}
				// 展开表情列表
				this.chatMsgHeight = this.chatMsgHeight - this.keyboardHeight
				this.scrollToBottom()
				this.showEmoji = true
			},
			closeME() {
				const morePanelHeight = 220; // 更多选项面板固定高度
				if (this.showEmoji && this.showMore) {
					this.showEmoji = false
					this.showMore = false
					this.chatMsgHeight = this.chatMsgHeight + morePanelHeight
					this.scrollToBottom()
					return
				}
				if (this.showEmoji) {
					this.showEmoji = false
					this.chatMsgHeight = this.chatMsgHeight + this.keyboardHeight
					this.scrollToBottom()
					return
				}
				if (this.showMore) {
					this.showMore = false
					this.chatMsgHeight = this.chatMsgHeight + morePanelHeight
					this.scrollToBottom()
					return
				}

			},
			sendMessage() {
				if (this.messageValue.trim() === '') {
					return
				}
				const content = this.messageValue.trim()
				const now = Date.now()
				sendMsg({
					sendUid: this.userInfo.userId,
					acceptUid: this.targetUser.userId,
					content: content,
					msgType: 1,
					chatType: 1
				}).finally(() => {
					const msg = {
						id: now,
						fromId: this.userInfo.userId,
						toId: this.targetUser.userId,
						content: content,
						time: weChatTimeFormat(now),
						chatType: 1,
						isRead: 1,
						isSend: 1,
						audioTime: 0,
						showTime: true
					}
					this.messageValue = ''
					this.messageList.push(msg)
					this.scrollToBottom()
				})
			},
			backLastPage() {
				uni.navigateBack({
					delta: 1
				})
			},
			/**
			 * 前往对方用户主页
			 * @param {Object} userId
			 */
			goToOtherMine(userId) {
				const currentUserId = uni.getStorageSync('userInfo').id;
				// 如果点击的是当前登录用户的头像，跳转到mine页面
				if (userId == currentUserId) {
					uni.navigateTo({
						url: '/pkg-main/pages/mine/mine'
					})
				} else {
					// 否则跳转到otherMine页面
					uni.navigateTo({
						url: '/pkg-mine/pages/mine/otherMine?userId=' + userId
					})
				}
			},
			// 异步将数据库中的网络图片路径转换为本地路径
			async updateChatContent(message) {
				let content = message.content
				replaceImageMessage(content).then(res => {
					content = res
					let sql =
						`update chat_${this.targetUser.userId} set content="${content}" where id=${message.id}`
					this.$sqliteUtil.SqlExecute(sql)
				})
			},
			// getChatHistory(page) {
			// 	if (this.msgLoading == 'nomore') {
			// 		return
			// 	}
			// 	this.msgLoading = 'loading'
			// 	let offset = (page - 1) * 15
			// 	let sql =
			// 		`select * from chat_${this.targetUser.userId} order by id desc limit 15 offset ${offset}`
			// 	this.$sqliteUtil.SqlSelect(sql).then(res => {
			// 		res = res.reverse()
			// 		for (let i = 0; i < res.length; i++) {
			// 			// 设置显示时间
			// 			res[i].showTime = true
			// 			if (res[i - 1]) {
			// 				// 如果两条消息时间间隔小于2分钟则不显示时间
			// 				if (res[i].time - res[i - 1].time < 120000) {
			// 					res[i].showTime = false
			// 				}
			// 			}
			// 			this.updateChatContent(res[i])
			// 		}
			// 		res.forEach(item => {
			// 			item.time = weChatTimeFormat(item.time)
			// 		})
			// 		res = transData(res)
			// 		this.messageList.unshift(...res)
			// 		console.log(this.messageList)
			// 		if (res.length < 15) {
			// 			this.msgLoading = 'nomore'
			// 		} else {
			// 			this.msgLoading = 'loading'
			// 			this.page++
			// 		}
			// 	})
			// },
			async getChatHistory(page) {
				if (this.msgLoading == 'nomore') {
					return
				}
				this.msgLoading = 'loading'
				try {
					const res = await getHistoryChatList({
						userId: this.targetUser.userId,
						page: this.page,
						pageSize: this.pageSize
					})
					if (res.code === 200) {
						// 获取当前用户ID
						const currentUserId = this.userInfo.userId
						let messageList = (res.data.records || []).map(item => {
							let content = item.content
							// 处理图片消息
							if (item.msgType === 2) {
								// 根据发送方调整图片样式
								const isFromMe = item.sendUid === currentUserId
								const style = isFromMe ?
									'padding:15rpx;padding-right: 15rpx;' :
									'padding: 15rpx;margin-left: 15rpx;'
								content =
									`<img src='${item.content}' style='max-width: 200px;height: auto;border-radius: 10px;${style}'></img>`
							}
							return {
								id: item.id,
								fromId: parseInt(item.sendUid, 10) || 0, // 发送者ID
								toId: parseInt(item.acceptUid, 10) || 0, // 接收者ID
								content: content, // 消息内容
								time: item.timestamp, // 时间戳
								chatType: item.msgType, // 消息类型(1文本,2图片,4语音等)
								isRead: 1, // 已读状态
								// 如果是当前用户发送的消息，设置isSend为1，否则不需要这个状态
								isSend: item.sendUid === currentUserId ? 1 : undefined,
								audioTime: 0, // 语音时长(如果有语音消息需要添加)
								showTime: true // 是否显示时间
							}
						})
						// 按时间升序排序
						messageList.sort((a, b) => a.time - b.time)
						// 处理消息时间显示
						for (let i = 0; i < messageList.length; i++) {
							if (messageList[i - 1]) {
								// 如果两条消息时间间隔小于2分钟则不显示时间
								if (messageList[i].time - messageList[i - 1].time < 120000) {
									messageList[i].showTime = false
								}
							}
						}
						// 格式化时间显示
						messageList.forEach(item => {
							item.time = weChatTimeFormat(item.time)
						})
						this.messageList = messageList
						// 判断是否还有更多消息
						if (res.data.current >= res.data.pages) {
							this.msgLoading = 'nomore'
						} else {
							this.msgLoading = 'loading'
							this.page++
						}
						// 滚动到底部
						this.$nextTick(() => {
							this.scrollToBottom()
						})
					}
				} catch (e) {
					console.error('获取历史消息失败', e)
					uni.showToast({
						title: '获取历史消息失败',
						icon: 'none'
					})
					this.msgLoading = 'nomore'
				}
			},
			// 使页面滚动到底部
			scrollToBottom() {
				setTimeout(() => {
					let query = uni.createSelectorQuery()
					query.select('#scrollChat').boundingClientRect()
					query.select('#scrollMsg').boundingClientRect()
					query.exec((res) => {
						const chatRect = res && res[0]
						const msgRect = res && res[1]
						if (!chatRect || !msgRect) {
							return
						}
						if (msgRect.height > chatRect.height) {
							this.scrollTop = msgRect.height - chatRect.height
						} else {
							this.scrollTop = 0
						}
					})
				}, 100)
			},
			focus(e) {
				console.log(e.detail.height)
				this.keyboardHeight = e.detail.height
				this.chatMsgHeight = this.chatMsgHeight - e.detail.height
				this.scrollToBottom()
			},
			blur(e) {
				this.cursor = e.detail.cursor
				this.chatMsgHeight = this.chatMsgHeight + this.keyboardHeight
				this.scrollToBottom()
			}
		},
		watch: {
			// 监听messageValue的值，如果有值则显示发送按钮
			messageValue(val) {
				if (val) {
					this.isShowSend = true
				} else {
					this.isShowSend = false
				}
			}
		},
		onLoad(options) {
			// 设置状态栏样式为白色背景
			// #ifdef MP-WEIXIN
			uni.setNavigationBarColor({
				frontColor: '#000000', // 状态栏文字颜色为黑色
				backgroundColor: '#ffffff' // 状态栏背景色为白色
			})
			// #endif

			this.targetUser.userId = options.userId
			this.targetUser.userName = options.userName
			this.targetUser.avatarUrl = options.avatarUrl
			this.userInfo.userId = uni.getStorageSync('userInfo').id
			this.userInfo.userName = uni.getStorageSync('userInfo').nickname
			this.userInfo.avatarUrl = uni.getStorageSync('userInfo').avatarUrl
			// 进入会话即清零该会话未读（type=3=聊天，携带对端 userId），并刷新底部角标
			try {
				clearMessageCount({
					type: 3,
					userId: this.targetUser.userId
				})
				try {
					uni.$emit('im:requestRefreshBadge')
				} catch (e) {}
			} catch (e) {}
			uni.getSystemInfo({
				success: (res) => {
					this.statusBarHeight = res.statusBarHeight
					this.chatMsgHeight = res.screenHeight - res.statusBarHeight - 44 - 80
				}
			})
			// 获取表情列表（与发布笔记一致的 Unicode 表情）
			this.emojiList = emojiList
			// let sql = `select * from emoji_list`
			// this.$sqliteUtil.SqlSelect(sql).then(res => {
			// 	if (res.length == 0) {
			// 		this.emojiList = emojiList
			// 	} else {
			// 		this.emojiList = res
			// 	}
			// })
			this.record = uni.getRecorderManager()
			this.getChatHistory()
			this.scrollToBottom()
			setTimeout(() => {
				this.scrollToBottom()
			}, 200)
			// 订阅统一新消息事件，属于当前会话则即时插入
			uni.$off('im:newMessage_chat')
			uni.$on('im:newMessage', (msg) => {
				if (!msg) {
					return
				}
				const from = String(msg.fromUid)
				const to = String(msg.toUid)
				const me = String(this.userInfo.userId)
				const peer = String(this.targetUser.userId)
				// 当前会话：对方给我 或 我给对方
				const isCurrent = (from === peer && to === me) || (from === me && to === peer)
				if (!isCurrent) {
					return
				}
				const content = msg.msgType === 2 ?
					`<img src='${msg.content}' style='max-width: 200px;height: auto;border-radius: 10px;'></img>` :
					msg.content
				const normalized = {
					id: msg.id || Date.now(),
					fromId: parseInt(from, 10),
					toId: parseInt(to, 10),
					content: content,
					time: weChatTimeFormat(msg.timestamp || Date.now()),
					chatType: msg.msgType || 1,
					isRead: 1,
					isSend: from === me ? 1 : undefined,
					audioTime: 0,
					showTime: true
				}
				// 去重：若最后一条消息相同 id 则不追加
				const last = this.messageList[this.messageList.length - 1]
				if (!last || last.id !== normalized.id) {
					this.messageList.push(normalized)
					this.scrollToBottom()
				}
			})
			// uni.$on('updateChatRecord', () => {
			// 	let sql1 = `select * from chat_${this.targetUser.userId} order by id desc limit 1`
			// 	this.$sqliteUtil.SqlSelect(sql1).then(res => {
			// 		res = transData(res)
			// 		res[0].time = weChatTimeFormat(res[0].time)
			// 		this.messageList.push(res[0])
			// 		this.scrollToBottom()
			// 	})
			// })
			// uni.$on('updateMsgStatus', (data) => {
			// 	for (let i = this.messageList.length - 1; i >= 0; i--) {
			// 		if (this.messageList[i].id == data.id) {
			// 			console.log('找到了')
			// 			this.messageList[i].isSend = data.status
			// 			this.$set(this.messageList, i, this.messageList[i])
			// 			if (data.status == 2) {
			// 				console.log('发送失败')
			// 				this.$refs.uNotify.show({
			// 					type: 'error',
			// 					message: data.content,
			// 					duration: 1500,
			// 					safeAreaInsetTop: true
			// 				})
			// 			}
			// 			break
			// 		}
			// 	}
			// })
			// const sql2 = `UPDATE message_list SET unread_num=0 WHERE user_id='${this.targetUser.userId}'`
			// setInterval(()=>{
			// 	this.$sqliteUtil.SqlExecute(sql2)
			// },800)
		},
		onBackPress() {
			if (this.showChoosePhoneType) {
				this.showChoosePhoneType = false
				return true
			}
			if (this.showRecordPop) {
				this.closeRecordPop()
				return true
			}
			if (this.showEmoji || this.showMore) {
				this.closeME()
				return true
			}
			let sql2 = `UPDATE message_list SET unread_num=0 WHERE user_id='${this.targetUser.userId}'`
			this.$sqliteUtil.SqlExecute(sql2).then(res => {
				this.$ws.setCornerMark()
			})
			// 离开页面前，停止当前语音播放
			try {
				innerAudioContext.stop()
			} catch (e) {}
			this.currentPlay = ''
			return false
		},
		onHide() {
			let sql2 = `UPDATE message_list SET unread_num=0 WHERE user_id='${this.targetUser.userId}'`
			this.$sqliteUtil.SqlExecute(sql2).then(res => {
				this.$ws.setCornerMark()
			})
			// 页面进入后台时，也停止语音播放，避免在列表页继续播放
			try {
				innerAudioContext.stop()
			} catch (e) {}
			this.currentPlay = ''
		},
		onUnload() {
			// 页面卸载时做最终清理，确保没有残留播放
			try {
				innerAudioContext.stop()
			} catch (e) {}
			this.currentPlay = ''
		}
	}
</script>
<style lang="scss">
	page {
		background-color: #f5f5f5;
	}

	.mr-10 {
		margin-right: 10rpx;
	}

	.mic-layer {
		position: fixed;
		bottom: 40rpx;
		left: 0;
		right: 0;
		width: 100%;
		box-sizing: border-box;
	}

	/* 讲话中样式 */
	.mic-layer-talking {
		position: fixed;
		width: 100vw;
		height: 100vh;
		left: 0;
		top: 0;
		background-color: rgba(0, 0, 0, .6);

		.mic-btn-talking {
			position: absolute;
			bottom: 0;
			width: 100vw;
			height: 200rpx;
			border-radius: 80px 80px 0 0;
			background-color: #F2F2F2;

			&_text {
				color: #999999;
				padding: 60rpx 0;
				text-align: center;
			}
		}

		/* 发送、取消提示文字 */
		.tip-text {
			position: absolute;
			top: -60rpx;
			left: 50%;
			width: 160rpx;
			text-align: center;
			transform: translateX(-50%);
			color: #E3E4EA;
			font-size: 24rpx;
		}

		/* 上方语音动画 */
		.record-animate-box {
			position: absolute;
			left: 50%;
			top: 50%;
			transform: translate(-50%, -50%);
			width: 300rpx;
			height: 140rpx;
			background-color: #2878F4;
			border-radius: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			transition: all .3s;

			&.active {
				background-color: #f56c6c;
				width: 140rpx;
			}

			/* 语音音阶 */
			.voice-scale {
				width: 60%;
				height: 40rpx;
				display: flex;
				align-items: center;
				justify-content: space-between;

				.item {
					display: block;
					background: #333333;
					width: 4rpx;
					height: 10%;
					margin-right: 2.5px;
					float: left;

					&:last-child {
						margin-right: 0px;
					}

					&:nth-child(1) {
						animation: load 1s 0.8s infinite linear;
					}

					&:nth-child(2) {
						animation: load 1s 0.6s infinite linear;
					}

					&:nth-child(3) {
						animation: load 1s 0.4s infinite linear;
					}

					&:nth-child(4) {
						animation: load 1s 0.2s infinite linear;
					}

					&:nth-child(5) {
						animation: load 1s 0s infinite linear;
					}

					&:nth-child(6) {
						animation: load 1s 0.2s infinite linear;
					}

					&:nth-child(7) {
						animation: load 1s 0.4s infinite linear;
					}

					&:nth-child(8) {
						animation: load 1s 0.6s infinite linear;
					}

					&:nth-child(9) {
						animation: load 1s 0.8s infinite linear;
					}

					&:nth-child(10) {
						animation: load 1s 1s infinite linear;
					}
				}
			}

			@keyframes load {
				0% {
					height: 10%;
				}

				50% {
					height: 100%;
				}

				100% {
					height: 10%;
				}
			}
		}

		/* 取消按钮 */
		.record-cancel {
			position: absolute;
			left: 50%;
			bottom: 300rpx;
			transform: translateX(-50%);
			width: 100rpx;
			height: 100rpx;
			border-radius: 50%;
			background-color: rgba(0, 0, 0, .2);
			display: flex;
			justify-content: center;
			align-items: center;
			transition: all .3s;

			&.active {
				transform: translateX(-50%) scale(1.2);
				background-color: #f56c6c;
			}

			.close-icon {
				font-size: 40rpx;
				color: #FFFFFF;
			}
		}
	}
</style>
<style lang="scss" scoped>
	::v-deep .uni-textarea-wrapper {
		max-height: 70px;
	}

	::v-deep .u-tag {
		border-style: none;
	}
</style>