<template>
	<view class="page">
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" class="status-placeholder"></view>
		<view class="header" :style="{top: navBarTop}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">系统消息</view>
			<view class="header-right"></view>
		</view>
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->

		<scroll-view
			scroll-y
			class="scroll"
			:style="{height: scrollHeight + 'px'}"
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view v-if="list.length > 0" class="list">
				<view v-for="(item, index) in list" :key="item.id || index" class="card-wrap" @click="openDetail(item)">
					<view class="time">{{ formatTs(item.timestamp) }}</view>
					<view class="card">
						<view class="tag">系统通知</view>
						<view class="title">{{ item.description || '系统消息' }}</view>
						<image
							v-if="item.imageUrl"
							class="thumb"
							:src="item.imageUrl"
							mode="aspectFill"
						/>
						<view class="preview">{{ preview(item) }}</view>
					</view>
				</view>
			</view>
			<view v-else class="empty">
				<u-empty mode="data" text="暂无系统通知"></u-empty>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getSystemNotificationConfigList } from '@/apis/mesasage_apis.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'

	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				list: [],
				refreshing: false,
				scrollHeight: 600,
				statusBarHeight: 0
			}
		},
		onLoad() {
			// #ifdef MP-WEIXIN
			uni.setNavigationBarTitle({ title: '系统消息' })
			// #endif
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
					// #ifdef MP-WEIXIN
					this.scrollHeight = res.windowHeight
					// #endif
					// #ifndef MP-WEIXIN
					this.scrollHeight = res.windowHeight - (this.statusBarHeight || 0) - 44
					// #endif
				}
			})
			this.loadList()
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			stripHtml(s) {
				if (!s || typeof s !== 'string') return ''
				return s.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
			},
			preview(item) {
				const c = item && item.content
				if (c && typeof c === 'string') {
					const plain = this.stripHtml(c)
					if (plain) return plain.length > 100 ? plain.slice(0, 100) + '…' : plain
				}
				return item && item.imageUrl ? '[图片]' : ''
			},
			formatTs(ts) {
				if (ts == null || ts === '') return ''
				const n = Number(ts)
				if (!Number.isFinite(n)) return ''
				const d = new Date(n)
				if (Number.isNaN(d.getTime())) return ''
				const y = d.getFullYear()
				const m = String(d.getMonth() + 1).padStart(2, '0')
				const day = String(d.getDate()).padStart(2, '0')
				const h = String(d.getHours()).padStart(2, '0')
				const min = String(d.getMinutes()).padStart(2, '0')
				return `${y}-${m}-${day} ${h}:${min}`
			},
			async loadList() {
				try {
					const res = await getSystemNotificationConfigList()
					if (res.code === 200 && Array.isArray(res.data)) {
						this.list = res.data
					} else {
						this.list = []
						if (res.msg) {
							uni.showToast({ title: res.msg, icon: 'none' })
						}
					}
				} catch (e) {
					console.error(e)
					this.list = []
					uni.showToast({ title: '加载失败', icon: 'none' })
				}
			},
			async onRefresh() {
				this.refreshing = true
				await this.loadList()
				setTimeout(() => {
					this.refreshing = false
				}, 400)
			},
			openDetail(item) {
				const title = item.description || '系统消息'
				const raw =
					typeof item.content === 'string' ? this.stripHtml(item.content) : ''
				const img = item.imageUrl
				if (raw && img) {
					uni.showModal({
						title,
						content: raw,
						cancelText: '关闭',
						confirmText: '查看配图',
						success: (r) => {
							if (r.confirm) {
								uni.previewImage({ urls: [img], current: img })
							}
						}
					})
					return
				}
				if (img && !raw) {
					uni.previewImage({ urls: [img], current: img })
					return
				}
				uni.showModal({
					title,
					content: raw || '',
					showCancel: false,
					confirmText: '知道了'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		background: #f5f5f5;
		min-height: 100vh;
	}
	.status-placeholder {
		position: fixed;
		top: 0;
		width: 100%;
		background: #fff;
		z-index: 10000;
	}
	.header {
		position: fixed;
		width: 100%;
		height: 44px;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 12px;
		background: #fff;
		z-index: 9999;
		box-sizing: border-box;
	}
	.header-title {
		font-size: 17px;
		font-weight: 600;
	}
	.scroll {
		box-sizing: border-box;
	}
	.list {
		padding: 12px;
	}
	.card-wrap {
		margin-bottom: 14px;
	}
	.time {
		font-size: 12px;
		color: #999;
		margin-bottom: 6px;
		width: 100%;
		text-align: center;
	}
	.card {
		background: #fff;
		border-radius: 10px;
		padding: 12px 14px;
		border: 1px solid #eee;
	}
	.tag {
		display: inline-block;
		font-size: 12px;
		padding: 2px 8px;
		border-radius: 4px;
		background: #fff8e6;
		color: #d48806;
		margin-bottom: 8px;
	}
	.title {
		font-size: 15px;
		font-weight: 600;
		margin-bottom: 8px;
		line-height: 1.4;
	}
	.thumb {
		width: 100%;
		height: 140px;
		border-radius: 8px;
		margin-bottom: 8px;
		background: #f0f0f0;
	}
	.preview {
		font-size: 13px;
		color: #666;
		line-height: 1.5;
		word-break: break-all;
	}
	.empty {
		padding: 48px 0;
	}
</style>
