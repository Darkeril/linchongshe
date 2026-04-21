<template>
	<view>
		<!-- 一个滚动流：顶部 profile + 吸顶标签栏，下方仅内容区用 swiper 左右切换 -->
		<view class="tabs-swiper-wrap" :style="{ height: notesHeight }">
			<scroll-view :scroll-y="isScroll" :style="{height: notesHeight}" :throttle="false" @scroll="onScroll"
				@scrolltolower="handleReach" lower-threshold="80" :enable-back-to-top="true"
				:scroll-with-animation="true">
				<slot name="profile"></slot>
				<!-- 同一 container 分层：标题栏（顶层固定层）+ 内容区（下层），容器统一背景无脱节 -->
				<view class="mine-content-container">
					<view class="tabs-bar-placeholder" style="height:0;"></view>
					<view class="tabs-bar-wrapper" :style="isTabsFixed ? 'height:0;overflow:hidden;' : ''">
						<view id="tabsBar" class="tabs-bar" :style="tabsBarStyle">
							<u-tabs @click='handleTabClick' :current="actTab" :list="tabsList" lineWidth="40"
								lineColor="#1890ff"
								:activeStyle="{ color: '#16160e', fontSize: '35rpx', transform: 'scale(1.05)' }"
								:inactiveStyle="{ color: '#606266', fontSize: '32rpx', transform: 'scale(1)' }"
								itemStyle="height: 40px;">
							</u-tabs>
							<view style="position: absolute; right: 20px;">
								<u-icon name="search" color="#16160e" size="25"></u-icon>
							</view>
						</view>
					</view>
					<!-- 内容层：与标题栏同属一个 container，同背景 -->
					<swiper class="content-swiper" :current="actTab" @change="handleSwipeChange" :duration="300"
						:style="contentSwiperStyle">
						<swiper-item>
							<view id="tabPane0" class="tab-pane">
								<view class="component component-with-bottom-pad">
									<water-fall :list="(notesList[0] && notesList[0].notesList) || []" :showViews="true"
										ref="water1" :slot_bottom="noteType!=2" :type="currentType || 1">
									</water-fall>
									<view v-if="notesList[0].notesList.length === 0 && notesList[0].status === 'nomore'"
										class="empty-state">
										<view class="empty-icon">📝</view>
										<view class="empty-text">还没有发布笔记</view>
										<view class="empty-tip">快去发布你的第一篇笔记吧</view>
									</view>
								</view>
							</view>
						</swiper-item>
						<swiper-item>
							<view id="tabPane1" class="tab-pane">
								<view class="component component-with-bottom-pad">
									<idle-water-fall :list="idleList" :userId="userInfo.id" ref="idleWaterfall"
										@detail="handleIdleDetail" @edit="handleEditIdle"></idle-water-fall>
									<view v-if="idleList.length === 0 && idleStatus === 'nomore'" class="empty-state">
										<view class="empty-icon">🛍️</view>
										<view class="empty-text">还没有发布闲置商品</view>
										<view class="empty-tip">去发布你的闲置物品吧</view>
									</view>
								</view>
							</view>
						</swiper-item>
						<swiper-item>
							<view id="tabPane2" class="tab-pane">
								<view class="component component-with-bottom-pad">
									<water-fall :list="(notesList[1] && notesList[1].notesList) || []"
										:showViews="false" ref="water2" :type="currentType || 1"></water-fall>
									<view v-if="notesList[1].notesList.length === 0 && notesList[1].status === 'nomore'"
										class="empty-state">
										<view class="empty-icon">👍</view>
										<view class="empty-text">还没有点赞内容</view>
										<view class="empty-tip">给喜欢的笔记点个赞吧</view>
									</view>
								</view>
							</view>
						</swiper-item>
						<swiper-item>
							<view id="tabPane3" class="tab-pane">
								<view class="component component-with-bottom-pad">
									<water-fall :list="(notesList[2] && notesList[2].notesList) || []"
										:showViews="false" ref="water3" :type="currentType || 1"></water-fall>
									<view v-if="notesList[2].notesList.length === 0 && notesList[2].status === 'nomore'"
										class="empty-state">
										<view class="empty-icon">⭐</view>
										<view class="empty-text">还没有收藏内容</view>
										<view class="empty-tip">收藏喜欢的笔记，方便随时查看</view>
									</view>
								</view>
							</view>
						</swiper-item>
					</swiper>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import IdleWaterFall from '@/components/idle-water-fall/idle-water-fall.vue';

	export default {
		name: 'MineTabsContainer',
		components: {
			IdleWaterFall
		},
		props: {
			stickyHeight: {
				type: [Number, String],
				default: 0
			},
			stickyTopBarHeight: {
				type: [Number, String],
				default: 0
			},
			showAvatar: {
				type: Boolean,
				default: false
			},
			headerOpacity: {
				type: [Number, String],
				default: 0
			},
			userInfo: {
				type: Object,
				default: () => ({})
			},
			statusBarHeight: {
				type: [Number, String],
				default: 0
			},
			navigationBarHeight: {
				type: String,
				default: '44px'
			},
			iconHeight: {
				type: String,
				default: '24px'
			},
			actTab: {
				type: Number,
				default: 0
			},
			tabsList: {
				type: Array,
				default: () => []
			},
			notesHeight: {
				type: [Number, String],
				default: ''
			},
			isScroll: {
				type: Boolean,
				default: true
			},
			notesList: {
				type: Array,
				default: () => []
			},
			noteType: {
				type: Number,
				default: 0
			},
			currentType: {
				type: Number,
				default: 1
			},
			idleList: {
				type: Array,
				default: () => []
			},
			idleStatus: {
				type: String,
				default: 'loadmore'
			},
			userInfo: {
				type: Object,
				default: () => ({})
			},
			loadingText: {
				type: String,
				default: '努力加载中'
			},
			loadmoreText: {
				type: String,
				default: '轻轻下拉'
			},
			nomoreText: {
				type: String,
				default: '实在没有了'
			}
		},
		data() {
			return {
				_nearBottomFired: false,
				_tabsBarTopInScroll: null,
				isTabsFixed: false,
				tabsBarHeight: 50,
				_tabsMeasureBusy: false,
				contentSwiperHeight: 0,
				_measureTimer: null
			}
		},
		computed: {
			stickyTopPx() {
				const n = parseFloat(this.stickyTopBarHeight) || 0
				return n
			},
			tabsBarStyle() {
				// 非吸顶、吸顶都强制不透明白底，与图 2 一致
				const base = 'background-color:#fff !important;'
				if (!this.isTabsFixed) return base
				return (
					base +
					'position: fixed;' +
					'top:' + this.stickyTopPx + 'px;' +
					'left:0;right:0;' +
					'z-index:999;' +
					'display:flex;' +
					'justify-content:flex-start;' +
					'height:' + this.tabsBarHeight + 'px;' +
					'align-items:center;' +
					'padding-left:20rpx;' +
					'box-shadow:none;border-bottom:none;'
				)
			},
			contentSwiperStyle() {
				const minH = this.contentMinHeight
				const h = this.contentSwiperHeight || (this.parseHeightToPx(this.notesHeight) || 600)
				// 内容层与 container 同背景，吸顶时 padding-top 预留标题栏高度
				const pad = this.isTabsFixed ? this.tabsBarHeight : 0
				return 'min-height:' + minH + ';height:' + (h + pad) + 'px;padding-top:' + pad +
					'px;background-color:#f5f5f5;box-sizing:border-box;'
			},
			contentMinHeight() {
				// 内容区足够高，保证 scroll-view 可滚动并触发触底（小程序内 swiper 可能导致 scrolltolower 不触发）
				return '150vh'
			},
			avatarSizePx() {
				const n = parseFloat(this.statusBarHeight) || 0
				return Math.max(n * 0.8, 32)
			}
		},
		watch: {
			actTab() {
				this.scheduleMeasureContentHeight()
			},
			notesList: {
				deep: true,
				handler() {
					this.scheduleMeasureContentHeight()
				}
			},
			idleList: {
				deep: true,
				handler() {
					this.scheduleMeasureContentHeight()
				}
			}
		},
		mounted() {
			this.scheduleMeasureContentHeight()
		},
		methods: {
			// preserveFixed: 切换标签时只重置测量值，不取消吸顶，避免标签栏消失
			resetTabsMeasure(preserveFixed = false) {
				this._tabsBarTopInScroll = null
				if (!preserveFixed) this.isTabsFixed = false
			},
			scheduleMeasureContentHeight() {
				if (this._measureTimer) clearTimeout(this._measureTimer)
				this._measureTimer = setTimeout(() => {
					this.measureCurrentPaneHeight()
				}, 80)
			},
			measureCurrentPaneHeight() {
				const paneId = '#tabPane' + (this.actTab || 0)
				uni.createSelectorQuery().in(this)
					.select(paneId).boundingClientRect()
					.exec(res => {
						const rect = res && res[0]
						if (!rect || !rect.height) return
						// 额外加一点缓冲，避免图片异步加载导致高度略小从而“滑不动”
						this.contentSwiperHeight = Math.ceil(rect.height + 80)
					})
			},
			ensureTabsBarTop(scrollTop) {
				if (this._tabsBarTopInScroll != null || this._tabsMeasureBusy) return
				this._tabsMeasureBusy = true
				// 计算 tabsBar 在 scroll-view 内容流中的 top：scrollTop + (tabsBarRect.top - scrollViewRect.top)
				uni.createSelectorQuery().in(this)
					.select('.tabs-swiper-wrap').boundingClientRect()
					.select('#tabsBar').boundingClientRect()
					.exec(res => {
						this._tabsMeasureBusy = false
						const wrapRect = res && res[0]
						const barRect = res && res[1]
						if (!wrapRect || !barRect) return
						this._tabsBarTopInScroll = scrollTop + (barRect.top - wrapRect.top)
					})
			},
			updateTabsFixed(scrollTop) {
				if (this._tabsBarTopInScroll == null) return
				const fixThreshold = this._tabsBarTopInScroll - this.stickyTopPx
				// 回到资料区就取消吸顶；调大 unfix 值使上滑时更早恢复流内样式，不一直保持吸顶
				const unfixWhenBackToProfilePx = 280
				let shouldFix
				if (this.isTabsFixed) {
					shouldFix = scrollTop >= unfixWhenBackToProfilePx
				} else {
					shouldFix = scrollTop >= fixThreshold
				}
				if (shouldFix !== this.isTabsFixed) this.isTabsFixed = shouldFix
			},
			onScroll(e) {
				const scrollTop = (e && e.detail && e.detail.scrollTop != null) ? e.detail.scrollTop : 0;
				this.$emit('scroll', scrollTop);
				this.ensureTabsBarTop(scrollTop)
				this.updateTabsFixed(scrollTop)
				// 小程序中 scroll-view 内嵌 swiper 时 scrolltolower 可能不触发，用 scroll 手动判断触底
				const d = e && e.detail;
				if (d && typeof d.scrollHeight === 'number' && d.scrollHeight > 0) {
					const viewHeight = this.parseHeightToPx(this.notesHeight) || 500;
					const threshold = 120;
					const nearBottom = scrollTop + viewHeight >= d.scrollHeight - threshold;
					if (nearBottom && !this._nearBottomFired) {
						this._nearBottomFired = true;
						this.handleReach();
					} else if (!nearBottom) {
						this._nearBottomFired = false;
					}
				}
			},
			parseHeightToPx(val) {
				if (val == null || val === '') return 0;
				if (typeof val === 'number') return val;
				const num = parseFloat(String(val));
				if (String(val).indexOf('px') !== -1) return num;
				// rpx 等需要运行时转换，这里只处理数字和 px
				return num;
			},
			handleTabClick(e) {
				this.$emit('tab-click', e);
				// 切换标签时只重置测量，保持吸顶状态，避免标签栏消失
				this.resetTabsMeasure(true)
				this.$nextTick(() => this.scheduleMeasureContentHeight())
			},
			handleSwipeChange(e) {
				this.$emit('swipe-change', e);
				// 左右滑切换内容时只重置测量，保持吸顶状态
				this.resetTabsMeasure(true)
				this.$nextTick(() => this.scheduleMeasureContentHeight())
			},
			handleReach() {
				this.$emit('reach-bottom');
			},
			handleIdleDetail(id, type) {
				this.$emit('idle-detail', id, type);
			},
			handleEditIdle(productId) {
				this.$emit('edit-idle', productId);
			},
		}
	}
</script>

<style lang="scss" scoped>
	.tabs-bar {
		display: flex;
		justify-content: flex-start;
		height: 50px;
		align-items: center;
		padding-left: 20rpx;
		background-color: #fff;
		position: relative;
		z-index: 1001;
	}

	/* 同一 container 分层：标题栏 + 内容区共用一个容器，统一背景 #f5f5f5，无脱节 */
	.mine-content-container {
		background-color: #f5f5f5;
		margin-top: 0;
		position: relative;
		z-index: 1;
	}

	.tabs-bar-placeholder {
		width: 100%;
		box-sizing: border-box;
	}

	/* 非吸顶时包裹层白底，且 z-index 高于 Header(1000) 才能压住 profile 背景图 */
	.tabs-bar-wrapper {
		width: 100%;
		background-color: #fff;
		position: relative;
		z-index: 1001;
	}

	.sticky-block {
		width: 100%;
		background-color: #fff;
	}

	.sticky-top-bar {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		flex-shrink: 0;
	}

	.sticky-nav-left {
		position: absolute;
		left: 20rpx;
		top: 50%;
		transform: translateY(-50%);
		height: 40rpx;
	}

	.sticky-nav-center {
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 32px;
	}

	.sticky-avatar {
		border-radius: 50%;
	}

	.sticky-nav-right {
		position: absolute;
		right: 20rpx;
		top: 50%;
		transform: translateY(-50%);
		display: flex;
		align-items: center;
		gap: 30rpx;
	}

	.sticky-tabs-row {
		width: 100%;
		flex-shrink: 0;
	}

	::v-deep .u-sticky {
		margin-bottom: 0;
	}

	::v-deep .u-sticky__content {
		margin-bottom: 0;
		padding-bottom: 0;
		width: 100%;
	}

	.tabs-swiper-wrap {
		margin-top: 0;
		padding-top: 0;
		background-color: #f5f5f5;
	}

	.data_list,
	.content-swiper {
		margin-top: 0;
		padding-top: 0;
		margin-bottom: 0;
		padding-bottom: 0;
	}

	.component {
		margin-top: 0;
		padding-top: 0;
		padding-bottom: 0;
		min-height: 100%;
		background-color: #f5f5f5;
	}

	/* 笔记卡片平铺到底，不预留白色留白 */
	.component-with-bottom-pad {
		padding-bottom: 0;
	}

	// 确保swiper-item和scroll-view紧贴
	::v-deep .swiper-item {
		margin: 0;
		padding: 0;
	}

	::v-deep scroll-view {
		// background-color: #f5f5f5;
	}

	// 优化"到底了"提示样式
	::v-deep .u-loadmore__nomore-text,
	::v-deep .u-loadmore-text {
		font-size: 20rpx !important;
		color: #999999 !important;
	}

	::v-deep .u-loadmore__nomore-line {
		border-color: #e0e0e0 !important;
	}

	/* 空状态样式 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 150rpx 40rpx;
		text-align: center;
		min-height: 400rpx;
	}

	.empty-icon {
		font-size: 120rpx;
		margin-bottom: 30rpx;
		opacity: 0.6;
	}

	.empty-text {
		font-size: 32rpx;
		color: #666;
		margin-bottom: 20rpx;
		font-weight: 500;
	}

	.empty-tip {
		font-size: 24rpx;
		color: #999;
		line-height: 1.5;
	}
</style>