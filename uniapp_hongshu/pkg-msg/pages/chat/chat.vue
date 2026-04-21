<template>
	<view>
		<!-- 状态栏背景 - 白色 -->
		<view :style="{height:statusBarHeight+'px'}"
			style="position: fixed;z-index: 100;width: 100%;background-color: #ffffff;"></view>
		<view :style="{top: statusBarHeight+'px'}"
			style="display: flex;height: 44px;align-items: center;position: fixed;z-index: 100;width: 100%;background-color: #ffffff;">
			<!-- 左侧：返回 -->
			<view style="flex-shrink: 0;padding-left: 20rpx;" @click="backLastPage">
				<u-icon name="arrow-left" size="21"></u-icon>
			</view>
			<!-- 中间：头像 + 名称（居中） -->
			<view style="flex: 1;display: flex;justify-content: center;align-items: center;min-width: 0;">
				<image mode="aspectFill"
					style="height: 34px;width: 34px;border-radius: 50%;margin-right: 15rpx;flex-shrink: 0;"
					:src="targetUser.avatarUrl" @click="handleHeaderAvatarClick">
				</image>
				<view
					style="font-size: 33rpx;color: #2b2b2b;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">
					{{isGroupChat && groupInfo.memberCount ? `${targetUser.userName}（${groupInfo.memberCount}）` : targetUser.userName}}
				</view>
			</view>
			<!-- 右侧：更多 -->
			<view style="flex-shrink: 0;padding-right: 30rpx;">
				<u-icon name="more-dot-fill" color="#2b2b2b" size="26"></u-icon>
			</view>
		</view>
		<view :style="{height: statusBarHeight+44+'px'}"></view>
		<u-notify ref="uNotify"></u-notify>
    <!-- 商品对话提示条 -->
    <view
      v-if="!isGroupChat && productId"
      class="product-chat-banner"
      @click="goToProductDetail"
    >
      <text class="product-chat-tag">[商品对话]</text>
      <text class="product-chat-text">点击查看该商品</text>
      <text class="product-chat-arrow">›</text>
    </view>
		<scroll-view id="scrollChat" scroll-y :scroll-with-animation="scrollAnimation" :scroll-top="scrollTop"
			:style="{height: chatMsgHeight+'px'}" @scrolltoupper="getChatHistory(page)" upper-threshold="100"
			@click="closeME">
			<view id="scrollMsg">
				<!-- <u-loadmore v-if="msgLoading==='nomore'" :status="msgLoading" line font-size="12" /> -->
				<view v-for="(item,index) in messageList" v-bind:key="item.id">
					<!-- 时间显示 -->
					<view v-if="item.showTime"
						style="text-align: center;margin: 15rpx;font-size: 25rpx;color: #95949a;">
						{{item.displayTime}}
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
							style="padding:15rpx 20rpx;background-color: #518FF1;border-radius: 30rpx;margin-right: 15rpx;max-width: 500rpx;display: inline-block;">
							<rich-text style="color:#ffffff;word-wrap: break-word;word-break: break-all;"
								:nodes="item.content"></rich-text>
						</view>
						<!-- 图片消息 -->
						<view v-else-if="item.chatType===2"
							style="padding:15rpx;padding-right: 15rpx;max-width: 500rpx;">
							<rich-text @click="preImage" :nodes="item.content" :data-node="item.content"></rich-text>
						</view>
						<!-- 语音消息 -->
						<view v-else-if="item.chatType===3"
							style="padding:15rpx 20rpx;background-color: #518FF1;border-radius: 50rpx;margin-right: 15rpx;max-width: 500rpx;display: inline-flex;align-items: center;justify-content: flex-end;"
							:style="{width: handleVoiceWidth(item.audioTime || 1)}" @tap="playAudio(item)">
							<u-icon :name="currentPlay === item.id ? 'pause-circle' : 'volume'" color="#ffffff" size="25"></u-icon>
							<text style="margin-left: 10rpx;margin-right: 15rpx;color:#ffffff;font-size:28rpx;">
								{{ (item.audioTime || 1) + '”' }}
							</text>
						</view>
						<view style="margin-right: 15rpx;flex-shrink: 0;">
							<image mode="aspectFill" style="height: 80rpx;width: 80rpx;border-radius: 50%;"
								:src="userInfo.avatarUrl" @click="goToOtherMine(userInfo.userId)">
							</image>
						</view>
					</view>

					<!-- 对方发送的消息 -->
					<view v-else
						style="display: flex;padding: 20rpx;justify-content: flex-start;align-items: flex-start;">
						<view style="margin-left: 15rpx;flex-shrink: 0;">
							<image mode="aspectFill" style="height: 80rpx;width: 80rpx;border-radius: 50%;"
								:src="isGroupChat ? (item.senderAvatar || '/static/default.jpg') : targetUser.avatarUrl"
								@click="handleMessageAvatarClick(isGroupChat ? item.fromId : targetUser.userId)">
							</image>
						</view>
						<view style="flex: 1;margin-left: 15rpx;min-width: 0;">
							<!-- 群聊显示发送者名称 -->
							<view v-if="isGroupChat && item.senderName"
								style="font-size: 24rpx;color: #999;margin-bottom: 5rpx;">
								{{item.senderName}}
							</view>
							<!-- 文本消息 -->
							<view v-if="item.chatType===1"
								style="padding:15rpx 20rpx;margin-right:110rpx;background-color: #ffffff;border-radius: 30rpx;max-width: 500rpx;display: inline-block;">
								<rich-text style="color: #2b2b2b;word-wrap: break-word;word-break: break-all;"
									:nodes="item.content"></rich-text>
							</view>
							<!-- 图片消息 -->
							<view v-else-if="item.chatType===2" style="padding: 15rpx;max-width: 500rpx;">
								<rich-text @click="preImage" :nodes="item.content"
									:data-node="item.content"></rich-text>
							</view>
							<!-- 语音消息 -->
							<view v-else-if="item.chatType===3"
								style="padding:15rpx 20rpx;margin-right:110rpx;background-color: #ffffff;border-radius: 30rpx;max-width: 500rpx;display: inline-flex;align-items: center;justify-content: flex-start;"
								:style="{width: handleVoiceWidth(item.audioTime || 1)}" @tap="playAudio(item)">
								<u-icon :name="currentPlay === item.id ? 'pause-circle' : 'volume'" color="#2b2b2b" size="25"></u-icon>
								<text style="margin-left: 12rpx;color:#2b2b2b;font-size:26rpx;">
									{{ (item.audioTime || 1) + '”' }}
								</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- 录音状态条：紧贴输入区域上方显示 -->
		<view v-if="showRecordPop" class="record-inline-bar">
			<view :class="['mic-layer',{'mic-layer-talking':isTalking}]">
				<!-- 录音过程面板：波形 + 时长 + 取消 + 提示 -->
				<view v-if="isTalking" class="record-panel">
					<view class="rc-wave">
						<view class="note" v-for="n in 7" :key="n" :style="{'--d': n-1}"></view>
					</view>
					<view class="record-duration">录音时长: {{ leng }}s</view>
					<view class="cancel-btn" @click="sendLock = true; onEnd();">
						<text class="cancel-icon">×</text>
					</view>
					<view class="opt-tip" :class="{'opt-tip-cancel': sendLock}">
						{{ sendLock ? '松手取消' : '松手发送,上划取消' }}
					</view>
				</view>
			</view>
		</view>

		<view
			style="display: flex;padding: 40rpx;align-items: center;height: 80px;box-sizing: border-box;width: 100%;background-color: #ffffff;">
			<!-- 语音/键盘切换：尽量保持原始样式，只加一个入口 -->
			<view style="margin-right: 20rpx;display: flex;align-items: center;" @click="toggleRecordInput">
				<image v-if="!showRecordInput" src="@/static/image/voice.png" style="width: 52rpx;" mode="widthFix">
				</image>
				<u-icon v-else name="edit-pen" size="30" color="#2b2b2b"></u-icon>
			</view>
			<!-- 输入框保持灰色，其余区域保持白色 -->
			<view style="background-color: #f2f2f2;padding: 20rpx;border-radius: 20rpx;flex: 1;">
				<!-- 文本输入 -->
				<textarea v-if="!showRecordInput" v-model="messageValue"
					style="width: 100%;caret-color: #F56C6C;color: #2b2b2b;" placeholder="请输入消息内容" auto-height
					:adjust-position="false" @focus="focus" @blur="blur" auto-blur :cursor="cursor"></textarea>
				<!-- 语音输入：在输入框区域内显示，按下即开始录音，松开结束 -->
				<view v-else class="hs-record-bar" @touchstart.prevent.stop="handleRecordTouchStart"
					@touchend.prevent.stop="onEnd" @touchmove.prevent.stop="handleRecordMove"
					@contextmenu.prevent.stop="preventContextMenu">
					{{ isTalking ? '正在录音' : '按住 说话' }}
				</view>
			</view>
			<view style="margin-left: 20rpx;display: flex;align-items: center;" @click="openEmoji">
				<image src="@/static/image/expression.png" style="width: 70rpx;" mode="widthFix"></image>
			</view>
			<!-- 右侧：+ 号 / 发送按钮，直接切换，不做动画 -->
			<view v-if="!isShowSend" style="display: flex;align-items: center;margin-left: 10rpx;" @click="openMore">
				<image src="@/static/image/add.png" style="width: 75rpx;" mode="widthFix"></image>
			</view>
			<view v-else style="margin-left: 10rpx;">
				<u-button type="primary" text="发送" @click="sendMessage"></u-button>
			</view>
		</view>
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
				style="display: grid;text-align: center;;grid-template-columns: repeat(4,1fr);padding: 20rpx;gap: 20rpx;row-gap: 35rpx;">
				<view style="text-align: center;" @click="chooseImage">
					<view style="padding: 25rpx 15rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/photoList.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix"></image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">相册</view>
				</view>
				<view style="text-align: center;" @click="takePhoto">
					<view style="padding: 25rpx 15rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/takePhoto.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">拍照</view>
				</view>
				<view style="text-align: center;" @click="showChoosePhoneType = true">
					<view style="padding: 25rpx 15rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/phone.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">电话</view>
				</view>
				<view style="text-align: center;">
					<view style="padding: 25rpx 15rpx;background-color: #ffffff;border-radius: 15rpx;">
						<image src="@/static/image/files.png" style="width: 50rpx;border-radius: 10rpx;"
							mode="widthFix">
						</image>
					</view>
					<view style="font-size: 24rpx;margin-top: 8rpx;">文件</view>
				</view>
				<!-- 语音入口已在输入栏左侧，这里隐藏避免重复 -->
			</view>
		</view>
	</view>
</template>

<script>
	const innerAudioContext = uni.createInnerAudioContext();
	// 兼容 iOS 静音开关，点击播放时仍有声音
	try {
		innerAudioContext.obeyMuteSwitch = false
	} catch (e) {}
	import {
		getUserInfo
	} from '@/apis/user_service.js'
import {
		getHistoryChatList,
		getProductChatRecord,
		sendMsg,
		getGroupDetail,
		getGroupChatHistory,
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
				// 群聊相关
				isGroupChat: false, // 是否是群聊
				groupChatId: '', // 群聊ID
				groupInfo: {
					id: '',
					groupName: '',
					groupAvatar: ''
				},
				groupMembers: [], // 群成员列表（用于显示发送者名称）
				userInfo: {
					userId: '',
					userName: '',
					avatarUrl: ''
				},
				messageList: [],
				page: 1,
				pageSize: 115,
				totalPages: 0,
				currentPage: 1,
				initialized: false,
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
				showRecordInput: false,
				// 商品对话相关
				productId: '',
				productName: '',
				productImage: '',
			};
		},
		methods: {
			toggleRecordInput() {
				const morePanelHeight = 220; // 与 openMore / closeME 保持一致
				// 当前是文本输入 -> 切换到语音输入
				if (!this.showRecordInput) {
					// 如果表情面板打开，先关闭并还原高度
					if (this.showEmoji) {
						this.showEmoji = false;
						this.chatMsgHeight = this.chatMsgHeight + this.keyboardHeight;
					}
					// 如果「更多」面板打开，先关闭并还原高度
					if (this.showMore) {
						this.showMore = false;
						this.chatMsgHeight = this.chatMsgHeight + morePanelHeight;
					}
					// 语音输入不显示发送按钮，关闭键盘
					this.isShowSend = false;
					uni.hideKeyboard();

					this.showRecordInput = true;
					this.scrollToBottom();
				} else {
					// 语音输入 -> 切回文本输入（不需要动高度）
					this.showRecordInput = false;
				}
			},
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
				// 再次点击同一条，暂停播放
				if (this.currentPlay === item.id) {
					try {
						innerAudioContext.pause()
					} catch (e) {}
					this.currentPlay = ''
					return
				}
				// 切换语音前先停止上一条
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
				// 统一通过录音适配器（H5 MediaRecorder / App&小程序 RecorderManager）
				if (!this.$rc || typeof this.$rc.start !== 'function') {
					uni.showToast({
						icon: 'none',
						title: '当前环境不支持语音录制'
					})
					return
				}
				const doStart = () => {
					console.log('start');
					this.showRecordPop = true
					this.isTalking = true
					this.leng = 0
					try {
						uni.vibrateLong()
					} catch (e) {}
					this.timer = setInterval(() => {
						this.leng++
						if (this.leng >= 60) {
							this.onEnd()
							return
						}
					}, 1000)
					// H5: checkIsEnable 会校验 https/兼容性
					if (typeof this.$rc.checkIsEnable === 'function' && !this.$rc.checkIsEnable()) {
						this.isTalking = false
						if (this.timer) {
							clearInterval(this.timer)
							this.timer = null
						}
						return
					}
					Promise.resolve(this.$rc.start()).catch((e) => {
						console.log('record.start failed:', e)
						this.isTalking = false
						if (this.timer) {
							clearInterval(this.timer)
							this.timer = null
						}
						uni.showToast({
							icon: 'none',
							title: '录音启动失败'
						})
					})
				}
				// #ifdef MP-WEIXIN
				uni.getSetting({
					success: (res) => {
						const auth = res.authSetting && res.authSetting['scope.record']
						if (auth === false) {
							uni.showToast({
								icon: 'none',
								title: '请在设置中开启录音权限'
							})
							return
						}
						if (auth === true) {
							doStart()
						} else {
							uni.authorize({
								scope: 'scope.record',
								success: () => {
									doStart()
								},
								fail: () => {
									uni.showToast({
										icon: 'none',
										title: '未授权录音权限'
									})
								}
							})
						}
					},
					fail: () => {
						doStart()
					}
				})
				// #endif
				// #ifndef MP-WEIXIN
				doStart()
				// #endif
			},
			/**
			 * 结束录音
			 */
			onEnd() {
				this.isTalking = false
				this.showRecordPop = false
				// 停止录音
				try {
					if (this.$rc && typeof this.$rc.close === 'function') {
						this.$rc.close()
					} else if (this.record && typeof this.record.stop === 'function') {
						// 兼容兜底
						this.record.stop()
					}
				} catch (e) {}
				if (this.timer) {
					clearInterval(this.timer)
					this.timer = null
				}
				// 取消发送
				if (this.sendLock) {
					this.sendLock = false
					console.log('取消发送');
					return
				}
				// 小于 1s 不发送
				if ((Number(this.leng) || 0) < 1) {
					uni.showToast({
						icon: 'none',
						title: '说话时间太短'
					})
					return
				}
				if (!this.$rc || typeof this.$rc.upload !== 'function') {
					uni.showToast({
						icon: 'none',
						title: '当前环境不支持语音录制'
					})
					return
				}
				this.$rc.upload().then((data) => {
					const url = data && data.url
					const duration = Number(data && (data.duration || data.audioTime)) || Number(this.leng) || 1
					if (!url) {
						uni.showToast({
							icon: 'none',
							title: '语音上传失败'
						})
						return
					}
					this.sendAudio(url, duration)
				}).catch((e) => {
					console.log('upload audio failed:', e)
					uni.showToast({
						icon: 'none',
						title: '语音上传失败'
					})
				})
			},
			sendAudio(url, time) {
				const content = url
				// 本地乐观更新（先展示）
				const nowTs = Date.now()
				const localMsg = {
					id: nowTs,
					fromId: this.userInfo.userId,
					toId: this.targetUser.userId,
					content: content,
					time: nowTs,
					displayTime: weChatTimeFormat(nowTs),
					chatType: 3,
					isRead: 1,
					isSend: 0,
					audioTime: time,
					showTime: true
				}
				this.messageList.push(localMsg)
				this.scrollToBottom()
				// 后端发送语音消息（异步更新发送状态，msgType=3 表示语音）
				sendMsg({
					sendUid: this.userInfo.userId,
					acceptUid: this.isGroupChat ? this.groupChatId : this.targetUser.userId,
					content: content,
					msgType: 3,
					chatType: this.isGroupChat ? 1 : 0, // 1=群聊，0=私聊
					productId: this.productId || undefined,
					groupChatId: this.isGroupChat ? this.groupChatId : undefined,
					audioTime: time
				}).then(() => {
					localMsg.isSend = 1
				}).catch(() => {
					localMsg.isSend = 2
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
			// 输入栏里的录音触摸开始：同时记录起点并启动录音
			handleRecordTouchStart(e) {
				this.touchStart(e);
				this.onStart();
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
			// 语音气泡固定宽度，由秒数文本区分长短
			handleVoiceWidth() {
				return '100rpx'
			},
			closeRecordPop() {
				this.showRecordPop = false
			},
			preventContextMenu() {
				// H5 长按/右键时阻止默认菜单弹出
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
			goToProductDetail() {
				if (!this.productId) return
				uni.navigateTo({
					url: `/pkg-detail/pages/idleDetail/idleDetail?productId=${this.productId}`
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
					// 本地乐观更新（先展示）
					const nowTs = Date.now()
					const localMsg = {
						id: nowTs,
						fromId: this.userInfo.userId,
						toId: this.targetUser.userId,
						content: contentHtml,
						time: nowTs,
						displayTime: weChatTimeFormat(nowTs),
						chatType: 2,
						isRead: 1,
						isSend: 0,
						audioTime: 0,
						showTime: true
					}
					this.messageList.push(localMsg)
					this.scrollToBottom()
					// 后端发送图片消息（异步更新发送状态）
					sendMsg({
						sendUid: this.userInfo.userId,
						acceptUid: this.isGroupChat ? this.groupChatId : this.targetUser.userId,
						content: result.src,
						msgType: 2,
						chatType: this.isGroupChat ? 1 : 0, // 1=群聊，0=私聊
						productId: this.productId || undefined,
						groupChatId: this.isGroupChat ? this.groupChatId : undefined
					}).then(() => {
						localMsg.isSend = 1
					}).catch(() => {
						localMsg.isSend = 2
					})
				})
			},
			openMore() {
				const morePanelHeight = 220; // 更多选项面板固定高度
				// 如果当前是语音输入模式，先切回文字输入
				if (this.showRecordInput) {
					this.showRecordInput = false;
				}
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
				// 如果当前是语音输入模式，先切回文字输入
				if (this.showRecordInput) {
					this.showRecordInput = false;
				}
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
				// 本地乐观更新（先展示）
				const localMsg = {
					id: now,
					fromId: this.userInfo.userId,
					toId: this.targetUser.userId,
					content: content,
					time: now,
					displayTime: weChatTimeFormat(now),
					chatType: 1,
					isRead: 1,
					isSend: 0,
					audioTime: 0,
					showTime: true
				}
				this.messageValue = ''
				this.messageList.push(localMsg)
				this.scrollToBottom()
				// 发送文本消息（异步更新发送状态）
				sendMsg({
					sendUid: this.userInfo.userId,
					acceptUid: this.isGroupChat ? this.groupChatId : this.targetUser.userId,
					content: content,
					msgType: 1,
					chatType: this.isGroupChat ? 1 : 0, // 1=群聊，0=私聊
					productId: this.productId || undefined,
					groupChatId: this.isGroupChat ? this.groupChatId : undefined
				}).then(() => {
					localMsg.isSend = 1
				}).catch(() => {
					localMsg.isSend = 2
				})
			},
			backLastPage() {
				uni.navigateBack({
					delta: 1
				})
			},
			/**
			 * 处理头部头像点击
			 */
			handleHeaderAvatarClick() {
				if (this.isGroupChat) {
					this.goToGroupDetail()
				} else {
					// 客服ID为 '0'，不跳转
					const CUSTOMER_SERVICE_ID = '0'
					if (this.targetUser.userId === CUSTOMER_SERVICE_ID || this.targetUser.userId === '0' || this.targetUser
						.userId === 0) {
						return // 客服头像不可点击跳转
					}
					this.goToOtherMine(this.targetUser.userId)
				}
			},
			/**
			 * 处理消息中头像点击
			 */
			handleMessageAvatarClick(userId) {
				// 客服ID为 '0'，不跳转
				const CUSTOMER_SERVICE_ID = '0'
				if (userId === CUSTOMER_SERVICE_ID || userId === '0' || userId === 0) {
					return // 客服头像不可点击跳转
				}
				this.goToOtherMine(userId)
			},
			/**
			 * 前往对方用户主页
			 * @param {Object} userId
			 */
			goToOtherMine(userId) {
				// 客服ID为 '0'，不跳转
				const CUSTOMER_SERVICE_ID = '0'
				if (userId === CUSTOMER_SERVICE_ID || userId === '0' || userId === 0) {
					return // 客服头像不可点击跳转
				}
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
			// 跳转到群聊详情
			goToGroupDetail() {
				if (this.groupChatId) {
					uni.navigateTo({
						url: `/pkg-msg/pages/groupDetail/groupDetail?groupId=${this.groupChatId}`
					})
				} else {
					uni.showToast({
						title: '群聊ID不存在',
						icon: 'none'
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
					const reqPage = this.initialized ? this.currentPage : this.page
					let res
					if (this.isGroupChat) {
						// 群聊历史消息
						res = await getGroupChatHistory({
							groupChatId: this.groupChatId,
							page: reqPage,
							pageSize: this.pageSize
						})
					} else {
						// 私聊历史消息：
						// - 普通私信：getHistoryChatList（按用户维度）
						// - 商品对话：getProductChatRecord（按用户 + 商品维度）
						if (this.productId) {
							res = await getProductChatRecord({
								userId: this.targetUser.userId,
								page: reqPage,
								pageSize: this.pageSize,
								productId: this.productId
							})
						} else {
							res = await getHistoryChatList({
								userId: this.targetUser.userId,
								page: reqPage,
								pageSize: this.pageSize
							})
						}
					}
					if (res.code === 200) {
						// 分页初始化：若接口第一页是最旧数据，则首次跳转至最后一页
						const pages = Number(res.data.pages || 1)
						const current = Number(res.data.current || reqPage)
						if (!this.initialized) {
							this.totalPages = pages
							// 若当前是第1页且总页数>1，认为第1页是最旧页，切到最后一页
							if (current === 1 && pages > 1) {
								this.currentPage = pages
								this.initialized = true
								this.msgLoading = 'loading'
								await this.getChatHistory()
								return
							} else {
								this.currentPage = current
								this.initialized = true
							}
						}
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
								chatType: item.msgType, // 消息类型(1文本,2图片,3语音,4视频等)
								isRead: 1, // 已读状态
								// 如果是当前用户发送的消息，设置isSend为1，否则不需要这个状态
								isSend: item.sendUid === currentUserId ? 1 : undefined,
								audioTime: item.audioTime || item.duration || 0, // 语音时长（秒）
								showTime: true, // 是否显示时间
								// 群聊相关
								senderName: item.senderName || '', // 发送者名称（群聊用）
								senderAvatar: item.senderAvatar || '' // 发送者头像（群聊用）
							}
						})
						// 确保按时间升序排序（旧->新），底部为最新
						messageList.sort((a, b) => (Number(a.time) || 0) - (Number(b.time) || 0))
						// 处理消息时间显示
						for (let i = 0; i < messageList.length; i++) {
							if (messageList[i - 1]) {
								// 如果两条消息时间间隔小于2分钟则不显示时间
								if (messageList[i].time - messageList[i - 1].time < 120000) {
									messageList[i].showTime = false
								}
							}
						}
						// 单独提供显示字段，保留 time 作为数值排序依据
						messageList.forEach(item => {
							item.displayTime = weChatTimeFormat(Number(item.time))
						})
						if (this.messageList.length === 0) {
							// 首屏：渲染当前页（通常为最后一页=最新），滚动到底部
							this.messageList = messageList
							this.currentPage = Math.max(this.currentPage - 1, 1)
							this.$nextTick(() => {
								this.scrollToBottom()
							})
						} else {
							// 上拉历史：在顶部拼接更旧的一页
							this.messageList = [...messageList, ...this.messageList]
						}
						// 跨页重算时间显示，避免边界两条相邻不显示时间异常
						for (let i = 0; i < this.messageList.length; i++) {
							const prev = this.messageList[i - 1]
							const cur = this.messageList[i]
							cur.showTime = true
							if (prev) {
								if ((Number(cur.time) - Number(prev.time)) < 120000) {
									cur.showTime = false
								}
							}
							cur.displayTime = weChatTimeFormat(Number(cur.time))
						}
						// 是否还有更多历史
						if (this.currentPage <= 1) {
							this.msgLoading = 'nomore'
						} else {
							this.msgLoading = 'loading'
						}
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
						// 容错：节点可能尚未渲染完成，避免空引用
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
		async onLoad(options) {
			// 🔧 客服功能：如果是客服ID（0），使用固定的客服信息
			const CUSTOMER_SERVICE_ID = '0';
			const CUSTOMER_SERVICE_NAME = '客服';
			const CUSTOMER_SERVICE_AVATAR = '/static/customer-service-avatar.png';
			// 设置状态栏样式为白色背景
			// #ifdef MP-WEIXIN
			uni.setNavigationBarColor({
				frontColor: '#000000', // 状态栏文字颜色为黑色
				backgroundColor: '#ffffff' // 状态栏背景色为白色
			})
			// #endif

			// 商品对话参数
			this.productId = options.productId || ''
			this.productName = options.productName ? decodeURIComponent(options.productName) : ''
			this.productImage = options.productImage ? decodeURIComponent(options.productImage) : ''

			// 判断是私聊还是群聊
			const chatType = parseInt(options.chatType || '0', 10)
			this.isGroupChat = chatType === 1

			if (this.isGroupChat) {
				// 群聊
				this.groupChatId = options.groupId
				// 加载群聊信息
				try {
					const res = await getGroupDetail(this.groupChatId)
					if (res.code === 200 && res.data) {
						this.groupInfo = res.data
						this.targetUser.userName = res.data.groupName || '群聊'
						this.targetUser.avatarUrl = res.data.groupAvatar || '/static/default-group.jpg'
					}
				} catch (e) {
					console.error('获取群聊信息失败', e)
				}
			} else {
				// 私聊
				this.targetUser.userId = options.userId

				// 🔧 客服功能：如果是客服ID（0），使用固定的客服信息，不需要调用getUserInfo
				if (options.userId === CUSTOMER_SERVICE_ID || options.userId === '0' || options.userId === 0) {
					this.targetUser.userName = CUSTOMER_SERVICE_NAME
					this.targetUser.avatarUrl = CUSTOMER_SERVICE_AVATAR
				} else {
					// H5 / 小程序都会把参数 encode 后再拼到 URL，这里需要解码
					if (options.userName) {
						try {
							this.targetUser.userName = decodeURIComponent(options.userName)
						} catch (e) {
							this.targetUser.userName = options.userName
						}
					}
					if (options.avatarUrl) {
						let raw = options.avatarUrl
						try {
							raw = decodeURIComponent(raw)
						} catch (e) { }
						this.targetUser.avatarUrl = raw
					}
				}
			}

			this.userInfo.userId = uni.getStorageSync('userInfo').id
			this.userInfo.userName = uni.getStorageSync('userInfo').nickname
			this.userInfo.avatarUrl = uni.getStorageSync('userInfo').avatarUrl

			// 进入会话即清零该会话未读（type=3=聊天），并刷新底部角标
			try {
				if (this.isGroupChat) {
					// 群聊 - 清除群聊未读数
					clearMessageCount({
						type: 3,
						groupChatId: this.groupChatId
					})
				} else {
					// 私聊 - 清除私聊未读数
					clearMessageCount({
						type: 3,
						userId: this.targetUser.userId
					})
				}
				try {
					uni.$emit('im:requestRefreshBadge')
				} catch (e) {}
			} catch (e) {
				console.error('清除未读消息数失败', e)
			}
			uni.getSystemInfo({
				success: (res) => {
					this.statusBarHeight = res.statusBarHeight
					// 基础高度：屏幕高度 - 状态栏 - 顶部导航栏(44px) - 底部输入栏预估高度(80px)
					let baseHeight = res.screenHeight - res.statusBarHeight - 44 - 80
					// 如果是商品对话，上方有一条「[商品对话] 点击查看该商品」提示条
					// 这里再预留一段高度（约 44px），避免输入栏被整体挤到屏幕外
					if (this.productId) {
						baseHeight -= 44
					}
					this.chatMsgHeight = baseHeight
				}
			})
			// 获取表情列表（与发布笔记一致的 Unicode 表情）
			this.emojiList = emojiList
			// 旧实现使用 getRecorderManager.onStop，这里仅保留对象赋值做兼容兜底
			// #ifndef H5
			try {
				this.record = uni.getRecorderManager()
			} catch (e) {}
			// #endif
			// 首次加载从最新页开始（内部会自动跳到最后一页）
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

				let isCurrent = false
				if (this.isGroupChat) {
					// 群聊：检查groupChatId是否匹配
					const msgGroupId = String(msg.groupChatId || '')
					isCurrent = msgGroupId === String(this.groupChatId)
				} else {
					// 私聊：对方给我 或 我给对方
					const peer = String(this.targetUser.userId)
					isCurrent = (from === peer && to === me) || (from === me && to === peer)
				}
				if (!isCurrent) {
					return
				}
				const content = msg.msgType === 2 ?
					`<img src='${msg.content}' style='max-width: 200px;height: auto;border-radius: 10px;'></img>` :
					msg.content
				const ts = Number(msg.timestamp) || Date.now()
				const normalized = {
					id: msg.id || ts,
					fromId: parseInt(from, 10),
					toId: parseInt(to, 10),
					content: content,
					time: ts,
					displayTime: weChatTimeFormat(ts),
					chatType: msg.msgType || 1,
					isRead: 1,
					isSend: from === me ? 1 : undefined,
					audioTime: msg.audioTime || msg.duration || 0,
					showTime: true,
					// 群聊相关
					senderName: msg.senderName || '', // 发送者名称（群聊用）
					senderAvatar: msg.senderAvatar || '' // 发送者头像（群聊用）
				}
				// 去重：
				// 1) 若最后一条消息 id 相同则不追加
				// 2) 若是我发送的回显，且与最后一条本地消息内容相同且时间差<10s，则不追加
				const last = this.messageList[this.messageList.length - 1]
				const isEchoOfMine = (from === me && last && last.fromId === Number(me) && last.toId ===
					Number(peer) && last.content === normalized.content && Math.abs(Number(last.time) -
					ts) < 10000)
				if (!last || (last.id !== normalized.id && !isEchoOfMine)) {
					this.messageList.push(normalized)
					this.scrollToBottom()
				}
			})
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
			// 返回列表前，停止当前语音播放
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
			// 页面隐藏时停止语音播放，避免在其它页面继续播
			try {
				innerAudioContext.stop()
			} catch (e) {}
			this.currentPlay = ''
		},
		onUnload() {
			// 页面卸载时做最终清理
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

	/* 商品对话提示条样式 */
	.product-chat-banner {
		margin: 0;
		padding: 18rpx 24rpx;
		background-color: #fffaf0;
		display: flex;
		align-items: center;
		font-size: 26rpx;
		color: #666666;
	}

	.product-chat-tag {
		color: #3d8af5;
		margin-right: 8rpx;
	}

	.product-chat-text {
		flex: 1;
	}

	.product-chat-arrow {
		color: #999999;
		margin-left: 8rpx;
	}

	/* 录音波形容器，作为内联条的内容容器使用 */
	.mic-layer {
		width: 100%;
		box-sizing: border-box;
	}

	/* 底部内联录音提示条：固定在输入框正上方 */
	.record-inline-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 400rpx; /* 再往上抬一些，让提示条更靠近聊天区 */
		padding: 100rpx 40rpx;
		background-color: transparent;
		z-index: 99;
	}

	/* 输入栏内“长按说话”语音条 */
	.hs-record-bar {
		height: 32rpx;
		border-radius: 20rpx;
		/* 语音输入条与输入框同底色，避免出现“外层阴影/高亮圈” */
		background: #f2f2f2;
		border: none;
		box-shadow: none;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #2b2b2b;
		font-size: 28rpx;
		font-weight: 600;
		letter-spacing: 8rpx;
		user-select: none;
		-webkit-user-select: none;
	}

	/* 讲话中样式（内联模式，不再全屏遮罩） */
	.mic-layer-talking {
		position: static;
		width: 100%;
		height: auto;
		background-color: transparent;

		.mic-btn-talking {
			position: relative;
			width: 100%;
			height: 200rpx;
			border-radius: 80px 80px 0 0;
			background-color: #F2F2F2;

			&_text {
				color: #999999;
				padding: 60rpx 0;
				text-align: center;
			}
		}

		/* 顶部录音面板样式（仿 uniapp_im） */
		.record-panel {
			position: absolute;
			top: 25%;
			left: 50%;
			transform: translateX(-50%);
			width: 80vw;
			background: #ffffff;
			border-radius: 24rpx;
			padding: 40rpx 20rpx 30rpx;
			box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.18);
		}

		.rc-wave {
			display: flex;
			align-items: flex-end;
			justify-content: center;
			height: 80rpx;
			margin-bottom: 20rpx;
		}

		.rc-wave .note {
			background: linear-gradient(to top, #c6ddff 0%, #2878f4 100%);
			width: 6rpx;
			height: 40%;
			border-radius: 4rpx;
			margin: 0 4rpx;
			animation: rcLoading 0.5s infinite linear;
			animation-delay: calc(0.08s * var(--d));
		}

		@keyframes rcLoading {
			0% {
				height: 25%;
			}

			50% {
				height: 90%;
			}

			100% {
				height: 25%;
			}
		}

		.record-duration {
			text-align: center;
			font-size: 26rpx;
			color: #666666;
			margin-bottom: 30rpx;
		}

		.cancel-btn {
			width: 110rpx;
			height: 110rpx;
			border-radius: 50%;
			background-color: #f3f4f6;
			display: flex;
			align-items: center;
			justify-content: center;
			margin: 0 auto 18rpx;
		}

		.cancel-icon {
			font-size: 48rpx;
			color: #666666;
		}

		.opt-tip {
			text-align: center;
			font-size: 26rpx;
			color: #555;
		}

		.opt-tip-cancel {
			color: #f56c6c;
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