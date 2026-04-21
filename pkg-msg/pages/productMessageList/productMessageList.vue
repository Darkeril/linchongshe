<template>
	<view class="page">
		<!-- 微信端也预留状态栏，避免与状态栏重合 -->
		<view :style="{ height: statusBarHeight + 'px' }" class="status-bar"></view>
		<view class="nav-bar" :style="{ top: navBarTop }">
			<u-icon name="arrow-left" size="22" @click="goBack"></u-icon>
			<text class="nav-title">闲宝消息</text>
		</view>
		<view :style="{ height: navPlaceholderHeight }"></view>
		<scroll-view scroll-y :style="{ height: scrollHeight + 'px' }" @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
			<view v-if="loading && list.length === 0" class="loading-wrap">
				<u-loading-icon size="40"></u-loading-icon>
			</view>
			<view v-else-if="list.length === 0" class="empty-wrap">
				<text class="empty-text">暂无商品对话</text>
			</view>
			<view v-else class="list">
				<view
					v-for="(item, index) in list"
					:key="itemKey(item, index)"
					class="item"
					@click="goToChat(index)"
				>
					<!-- 第一行：头像 + 昵称/时间 同一水平线，视觉一体 -->
					<view class="head">
						<image class="avatar" mode="aspectFill" :src="item.avatarUrl || item.avatar || '/static/default.jpg'"></image>
						<view class="head-right">
							<text class="name">{{ item.userName || item.username || item.user_name || '用户' }}</text>
							<text class="time">{{ item.lastTime || item.time || formatTime(item.timestamp || item.last_time) }}</text>
						</view>
					</view>
					<!-- 商品卡片：通栏，与上方头像行对齐 -->
					<view v-if="item.productName || item.product_name" class="product-card" @click.stop="goToProduct(index)">
						<image class="product-img" mode="aspectFill" :src="item.productImage || item.product_image || item.coverPicture || ''"></image>
						<view class="product-info">
							<text class="product-name">{{ item.productName || item.product_name || '' }}</text>
							<text class="product-price">¥{{ item.price != null ? item.price : (item.productPrice || '') }}</text>
						</view>
						<view class="product-arrow">›</view>
					</view>
					<!-- 最后一条消息：通栏 -->
					<view class="row2">
						<text class="last-msg">{{ formatContent(item.lastMessage || item.content || item.last_message || '') }}</text>
						<u-badge v-if="(item.unreadNum || item.count || 0) > 0" class="unread-badge" :value="item.unreadNum || item.count" numberType="overflow" max="99"></u-badge>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getProductChatUserList } from '@/apis/mesasage_apis.js'
import { weChatTimeFormat2 } from '@/utils/util.js'
import weixinNavBar from '@/utils/weixinNavBar.js'

export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			list: [],
			loading: true,
			refreshing: false,
			scrollHeight: 600
		}
	},
	onLoad() {
		uni.getSystemInfo({
			success: (res) => {
				this.applyWeixinNavBar(res, 44)
				// 微信小程序端：仍预留状态栏高度，避免标题与状态栏重合
				// #ifdef MP-WEIXIN
				const h = res.statusBarHeight || 0
				this.statusBarHeight = h
				this.navBarTop = h + 'px'
				this.navPlaceholderHeight = (h + 44) + 'px'
				// #endif
				this.scrollHeight = res.windowHeight - (this.statusBarHeight || 0) - 44
			}
		})
		this.fetchList()
	},
	onShow() {
		// 从闲宝列表返回消息页时刷新角标
		uni.$emit('im:requestRefreshBadge')
	},
	methods: {
		itemKey(item, index) {
			const uid = item.uid || item.userId || item.user_id
			const pid = item.productId || item.product_id
			return (uid || '') + '_' + (pid || '') + '_' + index
		},
		formatTime(t) {
			if (!t) return ''
			return weChatTimeFormat2(t)
		},
		// 与笔记消息列表一致：图片/视频显示为 [图片]/[视频]，不直接展示链接
		formatContent(str) {
			if (!str || typeof str !== 'string') return ''
			const s = String(str).trim()
			if (!s) return ''
			// 已是占位文案
			if (s === '[图片]' || s === '[视频]' || s === '[语音]') return s
			if (s.indexOf('[图片]') !== -1) return '[图片]'
			if (s.indexOf('[视频]') !== -1) return '[视频]'
			// 视频 URL 优先判断（避免 .mp4 等被误判为图片）
			if (s.match(/\.(mp4|mov|avi|webm|m3u8)(\?|$)/i) || /video\./i.test(s)) return '[视频]'
			// 图片 URL（扩展名或 image 域名）
			if (s.match(/\.(jpg|jpeg|png|gif|webp)(\?|$)/i) || s.startsWith('https://image.') || s.startsWith('http://image.')) return '[图片]'
			// 其他以 http 开头的链接多为图片（如 CDN 无扩展名）
			if (s.startsWith('http://') || s.startsWith('https://')) return '[图片]'
			// 富文本里的图片
			if (s.includes('<img')) return '[图片]'
			// 普通文本截断
			return s.length > 30 ? s.substring(0, 30) + '...' : s
		},
		normalizeItem(raw) {
			const myId = uni.getStorageSync('userInfo')?.id
			const otherUid = raw.uid ?? raw.userId ?? raw.user_id ??
				(raw.send_uid == myId || raw.send_uid === String(myId) ? raw.accept_uid : raw.send_uid) ??
				raw.accept_uid ?? raw.send_uid
			return {
				...raw,
				uid: otherUid,
				userId: otherUid,
				user_id: otherUid,
				userName: raw.username ?? raw.user_name ?? raw.nickname,
				avatarUrl: raw.avatar ?? raw.avatar_url,
				productId: raw.productId ?? raw.product_id ?? raw.pid,
				product_id: raw.productId ?? raw.product_id ?? raw.pid,
				productName: raw.productName ?? raw.product_name,
				productImage: raw.productImage ?? raw.product_image ?? raw.coverPicture,
				lastTime: raw.time ?? (raw.timestamp ? weChatTimeFormat2(raw.timestamp) : null) ?? weChatTimeFormat2(raw.last_time),
				lastMessage: raw.content ?? raw.last_message,
				unreadNum: raw.count ?? raw.unread_num ?? 0
			}
		},
		async fetchList() {
			this.loading = true
			try {
				const res = await getProductChatUserList()
				const raw = res?.data || res?.records || (Array.isArray(res) ? res : [])
				this.list = (Array.isArray(raw) ? raw : []).map((item) => this.normalizeItem(item))
			} catch (e) {
				console.error('闲宝消息列表失败', e)
				this.list = []
			}
			this.loading = false
			this.refreshing = false
		},
		onRefresh() {
			this.refreshing = true
			this.fetchList()
		},
		loadMore() {},
		goBack() {
			uni.navigateBack()
		},
		goToProduct(index) {
			const item = this.list[index]
			if (!item) return
			const productId = item.productId ?? item.product_id
			if (!productId) return
			uni.navigateTo({
				url: `/pkg-detail/pages/idleDetail/idleDetail?productId=${productId}`
			})
		},
		goToChat(index) {
			const item = this.list[index]
			if (!item) {
				uni.showToast({ title: '无法打开对话', icon: 'none' })
				return
			}
			// 对方用户：兼容多种后端字段（含 send_uid/accept_uid）
			const myId = uni.getStorageSync('userInfo')?.id
			const uid = item.uid ?? item.userId ?? item.user_id ??
				(item.send_uid == myId || item.send_uid === String(myId) ? item.accept_uid : item.send_uid) ??
				item.accept_uid ?? item.send_uid
			const userName = item.userName || item.username || item.user_name || '用户'
			const avatarUrl = item.avatarUrl || item.avatar || ''
			const productId = String(item.productId ?? item.product_id ?? '')
			const productName = item.productName || item.product_name || ''
			const productImage = item.productImage || item.product_image || item.coverPicture || ''
			if (!uid) {
				uni.showToast({ title: '无法打开对话', icon: 'none' })
				return
			}
			// productId 放在前面，避免 URL 过长被截断导致丢失
			const params = [
				`userId=${uid}`,
				`productId=${productId}`,
				`chatType=0`,
				`userName=${encodeURIComponent(userName)}`,
				`avatarUrl=${encodeURIComponent(avatarUrl)}`,
				`productName=${encodeURIComponent(productName)}`,
				`productImage=${encodeURIComponent(productImage)}`
			]
			const url = `/pkg-msg/pages/chat/chat?${params.join('&')}`
			uni.navigateTo({ url })
		}
	}
}
</script>

<style lang="scss" scoped>
.page {
	background: #f0f2f5;
	min-height: 100vh;
}
.status-bar {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	background: #fff;
	z-index: 10;
}
.nav-bar {
	position: fixed;
	left: 0;
	right: 0;
	height: 44px;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	background: #fff;
	z-index: 10;
}
.nav-title {
	font-size: 36rpx;
	font-weight: 500;
	color: #333;
	margin-left: 20rpx;
}
.loading-wrap,
.empty-wrap {
	padding: 80rpx 0;
	text-align: center;
}
.empty-text {
	color: #999;
	font-size: 28rpx;
}
.list {
	padding: 24rpx;
}
.item {
	display: flex;
	flex-direction: column;
	background: #fff;
	border-radius: 20rpx;
	padding: 20rpx;
	margin-bottom: 12rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
/* 第一行：头像与昵称、时间同一水平线，左对齐 */
.head {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}
.avatar {
	width: 55rpx;
	height: 55rpx;
	border-radius: 50%;
	flex-shrink: 0;
	margin-right: 20rpx;
	border: 2rpx solid #f0f0f0;
}
.head-right {
	flex: 1;
	min-width: 0;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.name {
	font-size: 32rpx;
	color: #1a1a1a;
	font-weight: 600;
}
.time {
	font-size: 22rpx;
	color: #999;
	flex-shrink: 0;
	margin-left: 16rpx;
}
/* 商品卡片：通栏，与卡片左右边距一致 */
.product-card {
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, #f8f9fc 0%, #f2f4f8 100%);
	border-radius: 16rpx;
	padding: 20rpx;
	margin-bottom: 16rpx;
	border: 1rpx solid #eee;
}
.product-img {
	width: 88rpx;
	height: 88rpx;
	border-radius: 12rpx;
	margin-right: 20rpx;
	background: #e8e8e8;
	flex-shrink: 0;
}
.product-info {
	flex: 1;
	min-width: 0;
}
.product-name {
	font-size: 28rpx;
	color: #333;
	display: block;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.product-price {
	font-size: 28rpx;
	color: #f56c6c;
	font-weight: 600;
	margin-top: 8rpx;
}
.product-arrow {
	font-size: 32rpx;
	color: #c0c0c0;
	margin-left: 8rpx;
	flex-shrink: 0;
}
.row2 {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}
.last-msg {
	font-size: 26rpx;
	margin-left: 18rpx;
	color: #8c8c8c;
	flex: 1;
	min-width: 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.unread-badge {
	flex-shrink: 0;
}
</style>
