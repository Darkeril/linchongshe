<template>
	<view class="notice-list-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航（微信端系统栏已显示标题，不重复展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">通知公告</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端已无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 公告列表 -->
		<scroll-view 
			scroll-y="true" 
			class="notice-scroll"
			:style="{height: scrollHeight + 'px'}"
			refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore"
			:lower-threshold="50"
		>
			<view class="notice-list" v-if="noticeList.length > 0">
				<view 
					class="notice-item" 
					v-for="(item, index) in noticeList" 
					:key="index"
					@click="viewNoticeDetail(item)"
				>
					<!-- 时间 -->
					<view class="notice-time">{{ formatTime(item.createTime) }}</view>
					
					<!-- 公告卡片 -->
					<view class="notice-card">
						<!-- 标题行 -->
						<view class="notice-card-header">
							<view class="notice-tag" :class="item.noticeType === '1' ? 'tag-notice' : 'tag-activity'">
								{{ item.noticeType === '1' ? '通知' : '活动' }}
							</view>
							<view class="notice-title-row">
								<text class="notice-emoji" v-if="getNoticeEmoji(item)">{{ getNoticeEmoji(item) }}</text>
								<text class="notice-card-title">{{ item.noticeTitle }}</text>
							</view>
						</view>
						
						<!-- 内容 -->
						<view class="notice-card-content">
							<!-- 解析HTML，将链接单独提取出来渲染 -->
							<block v-for="(part, partIndex) in parseContentWithLinks(item.noticeContent)" :key="partIndex">
								<!-- 如果是链接，渲染为可点击的text -->
								<text 
									v-if="part.type === 'link'" 
									class="content-link"
									@tap="openLink(part.href)"
								>{{ part.text }}</text>
								<!-- 其他内容用rich-text渲染 -->
								<rich-text 
									v-else
									:nodes="part.content"
								></rich-text>
							</block>
						</view>
						
						<!-- 底部信息 -->
						<view class="notice-card-footer" v-if="item.createBy">
							<image 
								class="admin-avatar" 
								src="https://image.mayongjian.cn/profile.jpg" 
								mode="aspectFill"
							></image>
							<text class="admin-name">管理员</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-else>
				<u-empty mode="data" text="暂无通知公告"></u-empty>
			</view>
			
			<!-- 加载更多 -->
			<view class="load-more" v-if="noticeList.length > 0">
				<u-loadmore :status="loadStatus" :loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText"></u-loadmore>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getNoticeList } from '@/apis/notice_apis.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				scrollHeight: 0,
				noticeList: [],
				page: 1,
				pageSize: 10,
				refreshing: false,
				loadStatus: 'loadmore', // loadmore-加载前, loading-加载中, nomore-没有更多
				loadingText: '加载中...',
				loadmoreText: '轻轻下拉',
				nomoreText: '没有更多了',
				hasMore: true,
				linkMap: {} // 存储链接ID到URL的映射
			}
		},
		onLoad() {
			// 设置状态栏样式为白色背景
			// #ifdef MP-WEIXIN
			uni.setNavigationBarColor({
				frontColor: '#000000', // 状态栏文字颜色为黑色
				backgroundColor: '#ffffff' // 状态栏背景色为白色
			})
			// #endif
			
			// 获取系统信息并计算scroll-view高度
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
			
			// 加载公告列表
			this.loadNoticeList()
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			
			// 加载公告列表
			async loadNoticeList(reset = false) {
				if (reset) {
					this.page = 1
					this.hasMore = true
					this.loadStatus = 'loadmore'
				}
				
				if (!this.hasMore && !reset) {
					return
				}
				
				try {
					this.loadStatus = 'loading'
					const res = await getNoticeList({
						page: this.page,
						pageSize: this.pageSize
					})
					
					if (res.code === 200) {
						const data = res.data || {}
						const records = data.records || data.list || []
						
						if (reset) {
							this.noticeList = records
						} else {
							this.noticeList = [...this.noticeList, ...records]
						}
						
						// 判断是否还有更多
						if (records.length < this.pageSize) {
							this.hasMore = false
							this.loadStatus = 'nomore'
						} else {
							this.loadStatus = 'loadmore'
						}
					} else {
						uni.showToast({
							title: res.msg || '加载失败',
							icon: 'none'
						})
						this.loadStatus = 'loadmore'
					}
				} catch (e) {
					console.error('加载公告列表失败:', e)
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
					this.loadStatus = 'loadmore'
				}
			},
			
			// 下拉刷新
			async onRefresh() {
				this.refreshing = true
				await this.loadNoticeList(true)
				setTimeout(() => {
					this.refreshing = false
				}, 500)
			},
			
			// 加载更多
			loadMore() {
				if (this.hasMore && this.loadStatus !== 'loading') {
					this.page += 1
					this.loadNoticeList()
				}
			},
			
			// 查看公告详情
			viewNoticeDetail(item) {
				// 可以跳转到详情页，或者展开显示完整内容
				// 这里暂时不做处理，直接显示完整内容
			},
			
			// 格式化时间
			formatTime(timeStr) {
				if (!timeStr) return ''
				
				const date = new Date(timeStr)
				const now = new Date()
				const diff = now - date
				const days = Math.floor(diff / (1000 * 60 * 60 * 24))
				
				// 今天
				if (days === 0) {
					const hours = date.getHours()
					const minutes = date.getMinutes()
					return `今天 ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
				}
				
				// 今年
				if (date.getFullYear() === now.getFullYear()) {
					const month = date.getMonth() + 1
					const day = date.getDate()
					const hours = date.getHours()
					const minutes = date.getMinutes()
					return `${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
				}
				
				// 往年
				const year = date.getFullYear()
				const month = date.getMonth() + 1
				const day = date.getDate()
				return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
			},
			
			// 获取标签样式类
			getTagClass(noticeType) {
				return noticeType === '1' ? 'tag-notice' : 'tag-activity'
			},
			
			// 获取标签文本
			getTagText(noticeType) {
				return noticeType === '1' ? '通知' : '活动'
			},
			
			// 获取公告表情（根据标题内容判断）
			getNoticeEmoji(item) {
				const title = item.noticeTitle || ''
				if (title.includes('汇报') || title.includes('进度')) {
					return '🪴'
				}
				if (title.includes('圈子') || title.includes('变化')) {
					return '😊'
				}
				return ''
			},
			
			// 解析内容，将链接提取出来单独处理
			parseContentWithLinks(content) {
				if (!content) return [{ type: 'text', content: '' }]
				
				const parts = []
				
				// 如果包含HTML标签
				if (content.includes('<')) {
					// 使用正则表达式匹配链接
					const linkRegex = /<a[^>]*href=["']([^"']+)["'][^>]*>([^<]*)<\/a>/gi
					let lastIndex = 0
					let match
					
					while ((match = linkRegex.exec(content)) !== null) {
						// 添加链接前的文本内容
						if (match.index > lastIndex) {
							const beforeText = content.substring(lastIndex, match.index)
							if (beforeText.trim()) {
								parts.push({
									type: 'text',
									content: this.formatContent(beforeText)
								})
							}
						}
						
						// 添加链接
						parts.push({
							type: 'link',
							href: match[1],
							text: match[2] || match[1]
						})
						
						lastIndex = match.index + match[0].length
					}
					
					// 添加最后剩余的文本内容
					if (lastIndex < content.length) {
						const afterText = content.substring(lastIndex)
						if (afterText.trim()) {
							parts.push({
								type: 'text',
								content: this.formatContent(afterText)
							})
						}
					}
					
					// 如果没有找到链接，返回整个内容
					if (parts.length === 0) {
						parts.push({
							type: 'text',
							content: this.formatContent(content)
						})
					}
				} else {
					// 纯文本内容
					parts.push({
						type: 'text',
						content: content.replace(/\n/g, '<br/>')
					})
				}
				
				return parts
			},
			
			// 打开链接
			openLink(url) {
				if (!url) return
				
				// 如果是 http/https 链接
				if (url.startsWith('http://') || url.startsWith('https://')) {
					// #ifdef MP-WEIXIN
					// 个人小程序不支持业务域名，直接复制链接到剪贴板
					// 企业小程序可以配置业务域名后使用 web-view
					uni.setClipboardData({
						data: url,
						success: () => {
							uni.showModal({
								title: '链接已复制',
								content: '链接已复制到剪贴板，请在浏览器中打开',
								showCancel: false,
								confirmText: '知道了'
							})
						},
						fail: () => {
							uni.showToast({
								title: '复制失败',
								icon: 'none'
							})
						}
					})
					// #endif
					
					// #ifdef H5
					window.open(url, '_blank')
					// #endif
					
					// #ifdef APP-PLUS
					plus.runtime.openURL(url)
					// #endif
					
					// #ifndef MP-WEIXIN
					// #ifndef H5
					// #ifndef APP-PLUS
					// 其他平台，复制链接
					uni.setClipboardData({
						data: url,
						success: () => {
							uni.showToast({
								title: '链接已复制',
								icon: 'none'
							})
						}
					})
					// #endif
					// #endif
					// #endif
				} else if (url.startsWith('/')) {
					// 内部链接，使用 navigateTo
					uni.navigateTo({
						url: url
					})
				} else {
					// 其他情况，复制链接
					uni.setClipboardData({
						data: url,
						success: () => {
							// #ifdef MP-WEIXIN
							uni.showModal({
								title: '链接已复制',
								content: '链接已复制到剪贴板，请在浏览器中打开',
								showCancel: false,
								confirmText: '知道了'
							})
							// #endif
							
							// #ifndef MP-WEIXIN
							uni.showToast({
								title: '链接已复制',
								icon: 'none'
							})
							// #endif
						}
					})
				}
			},
			
			// 格式化内容 - 将HTML字符串转换为nodes数组格式（微信小程序支持更好）
			formatContent(content) {
				if (!content) return ''
				
				// 如果是HTML内容，处理列表项样式和链接
				if (content.includes('<')) {
					let html = content
					
					// 处理链接，为链接添加可点击样式和data属性
					html = html.replace(/<a[^>]*href=["']([^"']+)["'][^>]*>/gi, (match, href) => {
						// 检查是否已有样式
						let newMatch = match
						if (newMatch.includes('style=')) {
							// 在现有样式中添加颜色和下划线
							newMatch = newMatch.replace(/style=["']([^"']+)["']/gi, (styleMatch, style) => {
								if (!style.includes('color')) {
									style += ';color:#1890ff;'
								}
								if (!style.includes('text-decoration')) {
									style += ';text-decoration:underline;'
								}
								return `style="${style}"`
							})
						} else {
							// 添加样式
							newMatch = newMatch.replace('>', ' style="color:#1890ff;text-decoration:underline;">')
						}
						// 确保href属性存在（用于itemclick事件）
						if (!newMatch.includes('href=')) {
							newMatch = newMatch.replace('>', ` href="${href}">`)
						}
						return newMatch
					})
					
					// 先处理 ul 列表，为 ul 添加样式
					html = html.replace(/<ul[^>]*>/gi, '<ul style="list-style:none;padding-left:0;margin:0;">')
					// 处理 ul 下的 li，添加绿色勾选标记
					// 使用非贪婪匹配，匹配 ul 和其内部的 li
					html = html.replace(/(<ul[^>]*>)([\s\S]*?)(<\/ul>)/gi, (match, ulOpen, content, ulClose) => {
						// 替换内容中的 li 标签
						const processedContent = content.replace(/<li[^>]*>/gi, (liTag) => {
							if (liTag.includes('style=')) {
								return liTag
							}
							return liTag.replace('>', ' style="margin:4px 0;color:#666;list-style-type:none;position:relative;padding-left:20px;"><span style="position:absolute;left:0;color:#52c41a;font-weight:bold;">✓</span>')
						})
						return ulOpen + processedContent + ulClose
					})
					// 处理 ol 列表
					html = html.replace(/<ol[^>]*>/gi, '<ol style="padding-left:30px;margin:0;">')
					// 处理 ol 下的 li
					html = html.replace(/(<ol[^>]*>)([\s\S]*?)(<\/ol>)/gi, (match, olOpen, content, olClose) => {
						const processedContent = content.replace(/<li[^>]*>/gi, (liTag) => {
							if (liTag.includes('style=')) {
								return liTag
							}
							return liTag.replace('>', ' style="margin:4px 0;color:#666;">')
						})
						return olOpen + processedContent + olClose
					})
					return html
				}
				// 否则转换为HTML格式，保留换行
				return content.replace(/\n/g, '<br/>')
			}
		}
	}
</script>

<style lang="scss" scoped>
	.notice-list-page {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f5f5;
	}
	
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 44px;
		padding: 0 15px;
		background-color: #fff;
		border-bottom: 1px solid #eee;
		box-sizing: border-box;
	}
	
	.header-left {
		width: 40px;
		display: flex;
		align-items: center;
		justify-content: flex-start;
	}
	
	.header-title {
		flex: 1;
		text-align: center;
		font-size: 18px;
		font-weight: 500;
		color: #333;
	}
	
	.header-right {
		width: 40px;
	}
	
	.notice-scroll {
		width: 100%;
		background-color: #f5f5f5;
		box-sizing: border-box;
		/* 下拉刷新样式优化 */
		refresher-threshold: 100rpx;
		refresher-default-style: black;
		refresher-background: #f8f8f8;
	}
	
	.notice-list {
		padding: 20rpx 30rpx;
	}
	
	.notice-item {
		margin-bottom: 30rpx;
		
		&:last-child {
			margin-bottom: 0;
		}
	}
	
	.notice-time {
		text-align: center;
		font-size: 24rpx;
		color: #999;
		margin-bottom: 20rpx;
	}
	
	.notice-card {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
	}
	
	.notice-card-header {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;
	}
	
	.notice-tag {
		padding: 4rpx 16rpx;
		border-radius: 8rpx;
		font-size: 24rpx;
		margin-right: 16rpx;
	}
	
	.tag-notice {
		background-color: #fff7e6;
		color: #fa8c16;
	}
	
	.tag-activity {
		background-color: #f6ffed;
		color: #52c41a;
	}
	
	.notice-title-row {
		flex: 1;
		display: flex;
		align-items: center;
	}
	
	.notice-emoji {
		font-size: 32rpx;
		margin-right: 8rpx;
	}
	
	.notice-card-title {
		font-size: 32rpx;
		font-weight: 500;
		color: #333;
		flex: 1;
	}
	
	.notice-card-content {
		font-size: 28rpx;
		color: #666;
		line-height: 1.8;
		margin-bottom: 20rpx;
		word-break: break-all;
	}
	
	.content-link {
		color: #1890ff;
		text-decoration: underline;
		display: inline;
	}
	
	/* 微信小程序中，rich-text 组件内的样式需要通过全局样式或内联样式实现 */
	/* 这里使用全局样式来处理列表项样式 */
	
	.notice-card-footer {
		display: flex;
		align-items: center;
		padding-top: 20rpx;
		border-top: 1px solid #f0f0f0;
	}
	
	.admin-avatar {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		margin-right: 12rpx;
	}
	
	.admin-name {
		font-size: 24rpx;
		color: #999;
	}
	
	.empty-state {
		padding: 100rpx 0;
	}
	
	.load-more {
		padding: 20rpx 0;
		margin-bottom: 0;
	}
</style>
