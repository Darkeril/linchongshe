<template>
	<view>
		<!-- <dLoading :status="true" ref="loadingMine"></dLoading> -->
		<view class="info" :style="{ height: screenHeight + 'px' }">
			<view class="filter" :style="{ backgroundImage: 'url(' + userInfo.homePageBackground + ')' }"></view>
			<!-- 吸顶时与 mine 页一致：纯深灰不透明顶栏；高度与导航栏底对齐，避免下方多出空隙 -->
			<view class="status-bar" :style="{
          height: (headerHeightPx - 40 || 96) + 'px',
          backgroundColor: showSend ? 'rgba(128,128,128,1)' : ('rgba(128,128,128,' + Math.min(1, opacity) + ')')
        }"></view>
			<view class="navigation-bar" :style="{
          height: navigationBarHeight,
          top: statusBarHeight + 'px',
          backgroundColor: showSend ? 'rgba(128,128,128,1)' : ('rgba(128,128,128,' + Math.min(1, opacity) + ')')
        }">
				<view :style="{ height: iconHeight }"
					style="position: absolute; left: 20rpx; top: 20rpx; opacity: 1; height: 40rpx" @click="goToBack">
					<!-- #ifndef MP-WEIXIN -->
					<u-icon name="arrow-left" color="#fff" size="28"></u-icon>
					<!-- #endif -->
				</view>
				<view :style="{ height: iconHeight }"
					style="opacity: 1; display: flex; flex-direction: column; justify-content: center">
					<u-transition :show="show" mode="fade-up">
						<image :style="{ height: statusBarHeight * 0.8 + 'px', width: statusBarHeight * 0.8 + 'px' }"
							:src="userInfo.avatarUrl" style="border-radius: 50%" mode="aspectFill"></image>
					</u-transition>
				</view>
				<view v-if="showSend" @click="goToChat" class="btn-private-msg"
					:class="{ 'btn-private-msg-sticky': showSend }">
					<u-transition :show="showSend" mode="fade-up">
						<view>私信</view>
					</u-transition>
				</view>
				<view :style="{ height: iconHeight }"
					style="position: absolute; right: 20rpx; top: 20rpx; opacity: 1; height: 40rpx">
					<u-icon name="more-dot-fill" color="#fff" size="28"></u-icon>
				</view>
			</view>
			<view :style="{ height: stickyHeight + 'px' }"></view>
			<view class="top-main">
				<view class="userinfo-main">
					<image :src="userInfo.avatarUrl" mode="aspectFill" @click="viewAvatarUrl"></image>
					<view class="userinfo-main-right">
						<text :decode="true">{{ userInfo.nickname }}</text>
						<text :decode="true">爱宠社号：{{ userInfo.hsId }}</text>
						<text :decode="true">IP属地：{{ formatProvince(userInfo.province) }}</text>
					</view>
				</view>
				<view class="introduction" v-if="userInfo.selfIntroduction">
					{{ userInfo.selfIntroduction }}
				</view>
				<!-- <view style="display: flex;">
					<view v-if="userInfo.age!=null&&userInfo.sex===1" class="tag">
						<u-icon name="man" color="#2ca9e1" size="20"></u-icon>
						<view v-if="userInfo.age!=null">{{userInfo.age}}岁</view>
					</view>
					<view v-if="userInfo.age!=null&&userInfo.sex===0" class="tag">
						<u-icon name="woman" color="#e198b4" size="20"></u-icon>
						<view v-if="userInfo.age!=null">{{userInfo.age}}岁</view>
					</view>
					<view v-if="userInfo.age!=null&&userInfo.sex===2">
						<view>{{userInfo.age}}岁</view>
					</view>
					<view v-if="userInfo.age==null&&userInfo.sex==1">
						<u-icon name="man" color="#2ca9e1" size="20"></u-icon>
					</view>
					<view v-if="userInfo.age==null&&userInfo.sex==0">
						<u-icon name="woman" color="#e198b4" size="20"></u-icon>
					</view>
					<view class="tag">{{area}}</view>
				</view> -->
				<!-- 标签渲染区 -->
				<view v-if="userInfo.tags && userInfo.tags.length > 0"
					style="display: flex; flex-wrap: wrap; gap: 16rpx; margin: 16rpx 0">
					<view v-for="(tag, idx) in userInfo.tags" :key="idx" style="
              background: hsla(0, 0%, 100%, 0.2);
              color: #fff;
              border-radius: 30rpx;
              padding: 6rpx 10rpx;
              font-size: 24rpx;
              line-height: 32rpx;
            ">
						{{ tag }}
					</view>
				</view>
				<view style="display: flex; width: 100%">
					<view style="width: 50%; display: flex; justify-content: space-around">
						<view class="guanzhu">
							<view>{{ userInfo.attentionNum }}</view>
							<view style="color: #e5e4e6">关注</view>
						</view>
						<view class="guanzhu">
							<view>{{ userInfo.fansNum }}</view>
							<view style="color: #e5e4e6">粉丝</view>
						</view>
						<view class="guanzhu">
							<view>8</view>
							<view style="color: #e5e4e6">获赞与收藏</view>
						</view>
					</view>
					<view style="
              width: 50%;
              display: flex;
              align-items: flex-end;
              justify-content: flex-end;
              gap: 20rpx;
              padding-right: 20rpx;
            ">
						<view class="tag1" v-if="userInfo.attentionStatus === 1" @click="cancelAttention(userInfo.id)">
							已关注
						</view>
						<view class="tag1 tag1-follow" v-else @click="attention(userInfo.id)">关注</view>
						<view class="tag1 tag1-small" @click="goToChat">私信</view>
					</view>
				</view>
			</view>
		</view>
		<!-- 流内标签栏：吸顶时不渲染，内容区自然上移贴住固定标签栏，无空隙 -->
		<view v-if="!tabsFixed" class="othermine-tabs-bar tabs-bar-inflow">
			<u-tabs @click="changetabs" :current="actTab" :list="tabsList" lineWidth="40" lineColor="#1890ff"
				:activeStyle="{ color: '#16160e', fontSize: '35rpx', transform: 'scale(1.05)' }"
				:inactiveStyle="{ color: '#606266', fontSize: '32rpx', transform: 'scale(1)' }"
				itemStyle="padding-left: 15px; padding-right: 15px; height: 40px;"></u-tabs>
			<view style="position: absolute; right: 20px">
				<u-icon name="search" color="#16160e" size="25"></u-icon>
			</view>
		</view>
		<!-- 参考 mine：吸顶时标签栏与下方内容区同一块背景；用 v-if 使吸顶 u-tabs 挂载时再计算下划线，避免下划线缩短、偏左 -->
		<view v-if="tabsFixed" class="othermine-sticky-block" :style="{ top: ((headerHeightPx - 38 || 96) - 2) + 'px' }">
			<view class="othermine-tabs-bar tabs-bar-fixed-inner">
				<u-tabs :key="'sticky-tabs'" @click="changetabs" :current="actTab" :list="tabsList" lineWidth="40" lineColor="#1890ff"
					:activeStyle="{ color: '#16160e', fontSize: '35rpx', transform: 'scale(1.05)' }"
					:inactiveStyle="{ color: '#606266', fontSize: '32rpx', transform: 'scale(1)' }"
					itemStyle="padding-left: 15px; padding-right: 15px; height: 40px;"></u-tabs>
				<view style="position: absolute; right: 20px">
					<u-icon name="search" color="#16160e" size="25"></u-icon>
				</view>
			</view>
			<view class="othermine-sticky-block-bg"></view>
		</view>
		<view class="othermine-content-wrap" >
		<!-- <view class="othermine-content-wrap" :style="contentWrapStickyStyle"> -->
			<swiper class="data_list" @change="swipeIndex" :current="actTab" :duration="300" previous-margin="0"
				:style="{ height: notesHeight }">
				<swiper-item>
					<scroll-view :scroll-y="isScroll" :style="{ height: notesHeight }" @scrolltolower="onReach"
						lower-threshold="20">
						<view class="component">
							<water-fall :list="notesList[0].notesList" ref="water1"></water-fall>
							<u-loadmore v-if="notesList[0].notesList.length > 0" margin-top="20" line
								:status="notesList[0].status" :loading-text="loadingText" :loadmore-text="loadmoreText"
								:nomore-text="nomoreText" />
						</view>
					</scroll-view>
				</swiper-item>
				<swiper-item>
					<scroll-view :scroll-y="isScroll" :style="{ height: notesHeight }" @scrolltolower="onReach"
						lower-threshold="20">
						<view class="component">
							<water-fall :list="notesList[1].notesList" ref="water2"></water-fall>
							<u-loadmore v-if="notesList[1].notesList.length > 0" margin-top="20" line
								:status="notesList[1].status" :loading-text="loadingText" :loadmore-text="loadmoreText"
								:nomore-text="nomoreText" />
						</view>
					</scroll-view>
				</swiper-item>
				<swiper-item>
					<scroll-view :scroll-y="isScroll" :style="{ height: notesHeight }" @scrolltolower="onReach"
						lower-threshold="20">
						<view class="component">
							<idle-water-fall :list="idleList" :userId="userInfo.id" ref="idleWaterfall"
								@detail="goToIdleDetail"></idle-water-fall>
							<u-loadmore v-if="idleList.length > 0" margin-top="20" line :status="idleStatus"
								:loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText" />
							<!-- 闲置空状态 -->
							<view v-if="idleList.length === 0 && idleStatus === 'nomore'" class="empty-state">
								<view class="empty-icon">🛍️</view>
								<view class="empty-text">还没有发布闲置商品</view>
							</view>
						</view>
					</scroll-view>
				</swiper-item>
			</swiper>
			<!-- </scroll-view> -->
		</view>
	</view>
</template>

<script>
	import {
		getViewUserInfo,
		updateAttention,
		getAttentionList,
		isFollow
	} from '@/apis/user_service.js';
	import {
		provinceToAbbr
	} from '@/utils/addressUtils.js';
	import {
		weChatTimeFormat
	} from '@/utils/util.js';
	import {
		formatProvince
	} from '@/utils/mineUtils.js';
	import {
		getNotesByView,
		getNotesCountByUserId
	} from '@/apis/notes_service.js';
	import {
		getUserIdleProducts
	} from '@/apis/idles_service.js';
	import IdleWaterFall from '@/components/idle-water-fall/idle-water-fall.vue';
	export default {
		data() {
			return {
				tabsList: [{
						name: '笔记'
					},
					{
						name: '收藏'
					},
					{
						name: '闲宝'
					}
				],
				area: '',
				screenHeight: '',
				statusBarHeight: '',
				navigationBarHeight: '',
				stickyHeight: '',
				stickyOffsetTop: '',
				headerHeightPx: 0, // 顶栏总高度 px，与 status-bar、吸顶 offset 一致
				notesHeight: '',
				iconHeight: '',
				mainInfoHeight: 0,
				swiperHeight: 0,
				opacity: 0,
				userInfo: {},
				actTab: 0,
				isScroll: true,
				show: false,
				showSend: false,
				tabsFixed: false,
				pageScrollTop: 0,
				moreShow: false,
				loadingText: '努力加载中',
				loadmoreText: '轻轻下拉',
				nomoreText: '实在没有了',
				notesList: [{
						notesList: [],
						status: 'loadmore',
						page: 1,
						pageSize: 10
					},
					{
						notesList: [],
						status: 'loadmore',
						page: 1,
						pageSize: 10
					}
				],
				// 闲置商品数据
				idleList: [],
				idleStatus: 'loadmore',
				idlePage: 1,
				idlePageSize: 6,
				idleTotal: 0,
				// 🔧 修复：添加防抖定时器，防止微信小程序端快速多次触发
				_reachTimer: null
			};
		},
		computed: {
			// 参考 mine 的 contentSwiperStyle：吸顶时预留标签栏高度 + 上移贴住，减少突兀
			// contentWrapStickyStyle() {
			// 	const header = this.headerHeightPx || 96;
			// 	const tabBarBottom = header - 2 + 50;
			// 	const style = {};
			// 	if (this.tabsFixed) {
			// 		style.paddingTop = '50px';
			// 		style.boxSizing = 'border-box';
			// 		if (this.pageScrollTop > this.rpxToPx(270)) {
			// 			const pull = Number(this.screenHeight) - this.pageScrollTop - tabBarBottom;
			// 			if (pull > 0) {
			// 				const marginPx = -Math.round(pull / 16) * 16;
			// 				style.marginTop = marginPx + 'px';
			// 			}
			// 		}
			// 	}
			// 	return style;
			// }
		},
		methods: {
			formatProvince,
			getNoteStats() {
				getNotesCountByUserId({
						userId: this.userInfo.id
					})
					.then(res => {
						if (res.code === 200) {
							// 你可以将这些数据显示到页面相应位置，这里仅演示打印
							console.log('noteCount stats:', res.data);
						}
					})
					.catch(err => console.log(err));
			},
			goToChat() {
				uni.navigateTo({
					url: '/pkg-msg/pages/chat/chat?userId=' +
						this.userInfo.id +
						'&userName=' +
						this.userInfo.nickname +
						'&avatarUrl=' +
						this.userInfo.avatarUrl
				});
			},
			viewAvatarUrl() {
				uni.previewImage({
					urls: [this.userInfo.avatarUrl],
					current: 0,
					indicator: 'none',
					longPressActions: {
						itemList: ['保存到相册'],
						success: data => {
							console.log(data.tapIndex);
							plus.nativeUI.closePreviewImage();
							this.downLoadImg(this.userInfo.avatarUrl);
						},
						fail: err => {
							console.log(err.errMsg);
						}
					}
				});
			},
			downLoadImg(e) {
				// 验证 URL 是否有效
				if (!e || typeof e !== 'string') {
					uni.showToast({
						title: '图片地址无效',
						icon: 'none'
					});
					return;
				}

				// 检查域名是否在合法列表中
				// #ifdef MP-WEIXIN
				if (e.includes('image.mayongjian.cn')) {
					uni.showToast({
						title: '图片域名未配置，请先在微信小程序后台配置合法域名',
						icon: 'none',
						duration: 3000
					});
					return;
				}
				// #endif

				uni.downloadFile({
					url: e,
					success: res => {
						if (res.statusCode === 200) {
							uni.saveImageToPhotosAlbum({
								filePath: res.tempFilePath,
								success: function() {
									uni.showToast({
										title: '保存成功',
										icon: 'none'
									});
								},
								fail: function() {
									uni.showToast({
										title: '保存失败，请检查应用权限',
										icon: 'none'
									});
								}
							});
						}
					},
					fail: err => {
						console.error('downloadFile 失败:', err);
						uni.showToast({
							title: '下载失败，请检查网络或域名配置',
							icon: 'none'
						});
					}
				});
			},
			goToBack() {
				uni.navigateBack({
					delta: 1
				});
			},
			cancelAttention(userId) {
				// 取消关注操作
				updateAttention({
						userId: uni.getStorageSync('userInfo').id,
						targetUserId: userId
					})
					.then(res => {
						if (res.code == 200) {
							this.userInfo.attentionStatus = 0;
							uni.showToast({
								title: '取消关注成功',
								icon: 'success'
							});
						}
					})
					.catch(err => {
						uni.showToast({
							title: '操作失败',
							icon: 'error'
						});
					});
			},
			attention(userId) {
				// 关注操作
				updateAttention({
						userId: uni.getStorageSync('userInfo').id,
						targetUserId: userId
					})
					.then(res => {
						if (res.code == 200) {
							this.userInfo.attentionStatus = 1;
							uni.showToast({
								title: '关注成功',
								icon: 'success'
							});
						}
					})
					.catch(err => {
						uni.showToast({
							title: '操作失败',
							icon: 'error'
						});
					});
			},
			// 获取关注状态
			getAttentionStatus(targetUserId) {
				const currentUserId = uni.getStorageSync('userInfo').id;

				// 如果当前用户和目标用户是同一个，不需要检查关注状态
				if (currentUserId == targetUserId) {
					return;
				}

				// 使用专门的 isFollow 接口检查关注状态
				isFollow({
						followerId: targetUserId
					})
					.then(res => {
						console.log('检查关注状态返回:', res);
						if (res.code == 200) {
							// 根据接口返回的 boolean 值设置关注状态
							this.userInfo.attentionStatus = res.data ? 1 : 0;
						} else {
							// 如果接口返回失败，默认为未关注状态
							this.userInfo.attentionStatus = 0;
						}
					})
					.catch(err => {
						console.log('获取关注状态失败:', err);
						// 如果获取失败，默认为未关注状态
						this.userInfo.attentionStatus = 0;
					});
			},
			rpxToPx(rpx) {
				const screenWidth = uni.getSystemInfoSync().screenWidth;
				return (screenWidth * Number.parseInt(rpx)) / 750;
			},
			changetabs(e) {
				let index = e.index;
				if (this.actTab == index) {
					// ✅ 移除已注释的 loadingMine ref 调用
					this.notesList[index].status = 'loadmore';
					this.notesList[index].page = 1;
					this.notesList[index].notesList = [];
					if (index == 0) {
						this.$refs.water1.clear();
						this.getMoreNotes(0);
					} else if (index == 1) {
						this.$refs.water2.clear();
						this.getMoreNotes(1);
					} else if (index == 2) {
						// 闲置标签页
						this.idleStatus = 'loadmore';
						this.idlePage = 1;
						this.idleList = [];
						if (this.$refs.idleWaterfall) {
							this.$refs.idleWaterfall.clear();
						}
						this.loadIdleProducts(true);
					}
					return;
				}
				this.actTab = index;
				if (index == 0) {
					if (this.notesList[0].page == 1) {
						this.getMoreNotes(0);
					}
				} else if (index == 1) {
					if (this.notesList[1].page == 1) {
						this.getMoreNotes(1);
					}
				} else if (index == 2) {
					// 闲置标签页
					if (this.idleList.length === 0 && this.idleStatus === 'loadmore') {
						this.loadIdleProducts(true);
					}
				}
			},
			swipeIndex(e) {
				this.actTab = e.detail.current;
				let index = e.detail.current;
				if (index == 0) {
					if (this.notesList[0].page == 1) {
						this.getMoreNotes(0);
					}
				} else if (index == 1) {
					if (this.notesList[1].page == 1) {
						this.getMoreNotes(1);
					}
				} else if (index == 2) {
					// 闲置标签页
					if (this.idleList.length === 0 && this.idleStatus === 'loadmore') {
						this.loadIdleProducts(true);
					}
				}
			},
			setSwiperHeight() {
				setTimeout(() => {
					let query = uni.createSelectorQuery().in(this);
					query
						.selectAll('.component')
						.boundingClientRect(data => {
							console.log(data);
							if (data && data.length > 0 && data[this.actTab] && data[this.actTab].height) {
								this.swiperHeight = data[this.actTab].height;
							}
						})
						.exec();
				}, 1000);
			},
			onReach() {
				// 🔧 修复：添加防抖和状态检查，防止微信小程序端快速多次触发
				const status = this.actTab === 2 ? this.idleStatus : this.notesList[this.actTab].status;

				if (status === 'loading' || status === 'nomore') {
					console.log(`⚠️ onReach: 状态为${status}，跳过加载`);
					return;
				}

				// 添加防抖处理
				if (this._reachTimer) {
					clearTimeout(this._reachTimer);
				}
				this._reachTimer = setTimeout(() => {
					if (this.actTab === 2) {
						// 闲置标签页
						this.loadMoreIdle();
					} else {
						// 笔记和收藏标签页
						this.getMoreNotes(this.actTab);
					}
				}, 300);
			},
			getMoreNotes(index) {
				const typeMap = [1, 3, 2];
				if (this.notesList[index].status == 'nomore' || this.notesList[index].status == 'loading') {
					return;
				}
				this.notesList[index].status = 'loading';
				getNotesByView({
						userId: this.userInfo.id,
						page: this.notesList[index].page,
						pageSize: this.notesList[index].pageSize,
						type: typeMap[index] // 这里用映射表
					})
					.then(res => {
						if (res.code === 200) {
							console.log(res);

							function mapNoteItem(item) {
								return {
									id: item.id,
									title: item.title,
									content: item.content,
									coverPicture: item.noteCover,
									noteCoverHeight: item.noteCoverHeight,
									belongUserId: parseInt(item.uid, 10) || 0,
									nickname: item.username,
									avatarUrl: item.avatar,
									imgList: item.urls ? JSON.parse(item.urls) : [],
									tags: item.tags ? JSON.parse(item.tags) : [],
									notesType: Number(item.noteType),
									isLike: !!item.isLike,
									notesLikeNum: parseInt(item.likeCount, 10) || 0,
									notesViewNum: parseInt(item.viewCount, 10) || 0,
									updateTime: item.time ? weChatTimeFormat(item.time) : ''
								};
							}
							const mappedList = res.data.records.map(mapNoteItem);

							// 🔧 修复：判断是否是第一页
							const isFirstPage = this.notesList[index].page === 1;

							if (isFirstPage) {
								// 第一页：直接设置新数据，让瀑布流组件的 watch 自动处理
								this.notesList[index].notesList = mappedList;
								console.log(`第一页数据设置完成: ${mappedList.length}条`);
							} else {
								// 后续页：去重后追加数据
								const existingIds = new Set(this.notesList[index].notesList.map(item => item.id));
								const newItems = mappedList.filter(item => !existingIds.has(item.id));
								this.notesList[index].notesList = [...this.notesList[index].notesList, ...newItems];
								console.log(
									`去重后新增数据: ${newItems.length}条，跳过重复: ${
                  mappedList.length - newItems.length
                }条`
								);
							}

							this.notesList[index].page++;

							// 🔧 修复：移除手动调用 addList，让组件的 watch 自动处理
							// 因为 water-fall 组件的 watch 会自动检测数据追加并调用 addList
							// 如果手动调用 addList，会导致重复调用（watch 一次 + 手动一次）

							// 设置状态
							if (this.notesList[index].notesList.length < this.notesList[index].pageSize) {
								this.notesList[index].status = 'nomore';
							} else {
								this.notesList[index].status = 'loadmore';
							}
						} else {
							this.notesList[index].status = 'nomore';
						}
					})
					.catch(err => {
						console.log(err);
						this.notesList[index].status = 'nomore';
					});
			},
			// 加载闲置商品
			async loadIdleProducts(reset = false) {
				if (reset) {
					this.idleStatus = 'loadmore';
					this.idlePage = 1;
					this.idleList = [];
					this.$nextTick(() => {
						if (this.$refs.idleWaterfall) {
							this.$refs.idleWaterfall.clear();
						}
					});
					this.idleTotal = 0;
				}
				if (this.idleStatus === 'nomore' || this.idleStatus === 'loading') return;
				this.idleStatus = 'loading';

				try {
					const res = await getUserIdleProducts({
						currentPage: this.idlePage,
						pageSize: this.idlePageSize,
						userId: this.userInfo.id,
						type: 4,
						status: 'publish'
					});
					if (res && res.code === 200) {
						const pageInfo = res.data || {};
						const records = pageInfo.records || [];
						const mapped = records.map(item => {
							// 🔧 修复：确保 id 字段正确映射，兼容字符串和数字类型
							const itemId = item.id || item.productId || null;
							if (!itemId) {
								console.warn('⚠️ 商品数据缺少 id 字段:', item);
							}
							return {
								id: itemId,
								title: item.title,
								desc: item.description,
								coverPicture: item.cover,
								noteCoverHeight: item.noteCoverHeight,
								imgList: Array.isArray(item.urls) ?
									item.urls :
									item.urls ?
									JSON.parse(item.urls) :
									[],
								belongUserId: parseInt(item.uid, 10) || 0,
								nickname: item.username,
								avatarUrl: item.avatar,
								productType: item.productType,
								price: item.price,
								originalPrice: item.originalPrice,
								province: item.province,
								city: item.city,
								district: item.district,
								address: item.address,
								postType: item.postType,
								auditStatus: item.auditStatus,
								uid: item.uid,
								updateTime: item.createTime
							};
						});

						// 🔧 修复：判断是否是第一页
						const isFirstPage = this.idlePage === 1;

						if (isFirstPage && !reset && this.idleList.length > 0) {
							// 第一页且不是重置：清空现有数据，准备加载新数据
							this.idleList = [];
							this.$nextTick(() => {
								if (this.$refs.idleWaterfall) {
									this.$refs.idleWaterfall.clear();
								}
							});
						}

						if (isFirstPage) {
							// 第一页：直接设置新数据，让瀑布流组件的 watch 自动处理
							this.idleList = mapped;
							console.log(`闲置第一页数据设置完成: ${mapped.length}条`);
						} else {
							// 后续页：去重后追加数据
							const existingIds = new Set(this.idleList.map(item => item.id));
							const newItems = mapped.filter(item => !existingIds.has(item.id));
							this.idleList = this.idleList.concat(newItems);
							console.log(
								`闲置去重后新增数据: ${newItems.length}条，跳过重复: ${
                mapped.length - newItems.length
              }条`
							);
						}

						this.idlePage = (pageInfo.current || this.idlePage) + 1;
						this.idleTotal = pageInfo.total || this.idleTotal;

						// 🔧 修复：移除手动调用 addList，让组件的 watch 自动处理
						// 因为 idle-water-fall 组件的 watch 会自动检测数据追加并调用 addList
						// 如果手动调用 addList，会导致重复调用（watch 一次 + 手动一次）

						const noMore =
							this.idleList.length >= this.idleTotal || mapped.length < this.idlePageSize;
						this.idleStatus = noMore ? 'nomore' : 'loadmore';
					} else {
						this.idleStatus = 'nomore';
					}
				} catch (e) {
					console.error('loadIdleProducts error', e);
					this.idleStatus = 'nomore';
				}
			},
			// 加载更多闲置商品
			loadMoreIdle() {
				if (this.idleStatus === 'nomore' || this.idleStatus === 'loading') return;
				this.loadIdleProducts(false);
			},
			// 跳转到闲置商品详情
			goToIdleDetail(id, type) {
				// 🔧 修复：添加参数验证，防止传递 undefined
				if (!id || id === 'undefined' || id === 'null') {
					console.error('❌ goToIdleDetail: 商品ID无效', id);
					uni.showToast({
						title: '商品ID无效',
						icon: 'none'
					});
					return;
				}
				uni.navigateTo({
					url: `/pkg-detail/pages/idleDetail/idleDetail?productId=${id}`
				});
			}
		},
		getAgeFromBirthday(birthday) {
			if (!birthday) return null;
			const birthDate = new Date(birthday);
			const now = new Date();
			let age = now.getFullYear() - birthDate.getFullYear();
			const m = now.getMonth() - birthDate.getMonth();
			if (m < 0 || (m === 0 && now.getDate() < birthDate.getDate())) {
				age--;
			}
			return age;
		},
		onLoad(options) {
			console.log('otherMine onLoad options:', options);
			console.log('接收到的userId:', options.userId);

			// 检查userId参数
			if (!options.userId) {
				console.error('userId参数缺失');
				uni.showToast({
					title: '用户ID缺失',
					icon: 'none'
				});
				// 返回上一页
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
				return;
			}

			// ✅ 修复：使用 getWindowInfo 获取正确的状态栏高度，与mine.vue保持一致
			const windowInfo = uni.getWindowInfo();
			const statusBarHeight = windowInfo.statusBarHeight || 44; // 默认 44px（iPhone 标准）

			this.screenHeight = windowInfo.screenHeight / 2.5;
			this.statusBarHeight = statusBarHeight;
			this.navigationBarHeight = statusBarHeight * 1.2 + 'px';
			// 顶栏底边 = 状态栏 + 导航栏，统一用 headerHeightPx；吸顶时标签栏紧贴顶栏底（多减几 px 抵消误差、去缝）
			const navBarH = Math.max(statusBarHeight * 1.2, 44);
			this.headerHeightPx = statusBarHeight + navBarH;
			this.stickyHeight = this.headerHeightPx - 2;
			this.stickyOffsetTop = Math.max(statusBarHeight, this.headerHeightPx - 6);
			// 修复高度计算：H5/App 不减底部预留，微信小程序需预留 tabbar/安全区/额外空间
			// #ifdef H5 || APP-PLUS
			const tabbarHeight = 5;
			const safeAreaBottom = 5;
			const extraSpace = 5;
			// #endif
			// #ifdef MP-WEIXIN
			const tabbarHeight = 50; // 底部 tabbar 高度
			const safeAreaBottom = 34; // 底部安全区域（iPhone X 等）
			const extraSpace = 20; // 额外空间，确保滚动流畅
			// #endif
			this.notesHeight =
				windowInfo.screenHeight -
				this.stickyHeight -
				tabbarHeight -
				safeAreaBottom -
				extraSpace +
				'px';
			console.log(
				`屏幕高度: ${windowInfo.screenHeight}, 状态栏: ${statusBarHeight}, 吸顶高度: ${this.stickyHeight}, 计算高度: ${this.notesHeight}`
			);
			this.iconHeight = statusBarHeight * 0.55 + 'px';

			// 获取用户信息
			getViewUserInfo({
				userId: options.userId
			}).then(res => {
				console.log('getViewUserInfo响应:', res);
				if (res.code == 200) {
					const data = res.data;
					// 在回调内用局部函数计算年龄，避免 this 在 Promise 中指向异常
					const getAgeFromBirthday = (birthday) => {
						if (!birthday) return null;
						const birthDate = new Date(birthday);
						const now = new Date();
						let age = now.getFullYear() - birthDate.getFullYear();
						const m = now.getMonth() - birthDate.getMonth();
						if (m < 0 || (m === 0 && now.getDate() < birthDate.getDate())) {
							age--;
						}
						return age;
					};
					// 字段映射
					// 解析标签：确保正确处理JSON字符串或数组
					let tags = [];
					if (data.tags) {
						try {
							if (typeof data.tags === 'string') {
								tags = JSON.parse(data.tags);
							} else if (Array.isArray(data.tags)) {
								tags = data.tags;
							}
						} catch (e) {
							console.error('解析标签失败:', e);
							tags = [];
						}
					}

					const userInfo = {
						id: data.id,
						hsId: data.hsId, // 爱宠社号
						ipAddr: data.address, // IP属地
						selfIntroduction: data.description || '', // 简介
						nickname: data.username,
						avatarUrl: data.avatar,
						sex: data.gender !== undefined ? parseInt(data.gender, 10) : null,
						age: data.birthday ? getAgeFromBirthday(data.birthday) : null,
						phone: data.phone,
						email: data.email,
						area: data.address,
						homePageBackground: data.userCover,
						tags: tags,
						noteCount: data.noteCount,
						productCount: data.productCount,
						attentionNum: parseInt(data.followerCount) || 0, // 关注数
						fansNum: parseInt(data.fanCount) || 0, // 粉丝数
						attentionStatus: 0 // 初始值，后续会通过API获取
						// 其它字段按需添加
					};

					// 调试日志：检查简介字段
					console.log('用户信息 - description字段值:', data.description);
					console.log('用户信息 - selfIntroduction映射值:', userInfo.selfIntroduction);
					this.userInfo = userInfo;
					// 获取关注状态
					this.getAttentionStatus(options.userId);
					// 省市处理逻辑不变
					this.area = '';
					// if (userInfo.area != null && userInfo.area != '') {
					//     let s = userInfo.area.split(' ')
					//     if (s.length === 2) {
					//         s.forEach((item, index) => {
					//             if (index == 0) {
					//                 this.area += provinceToAbbr(item)
					//             } else {
					//                 this.area += item.substring(0, item.length - 1)
					//             }
					//         })
					//     } else if (s.length === 3) {
					//         // 只显示省市
					//         s.forEach((item, index) => {
					//             if (index != 2) {
					//                 if (index == 0) {
					//                     this.area += provinceToAbbr(item)
					//                 } else {
					//                     this.area += item.substring(0, item.length - 1)
					//                 }
					//             }
					//         })
					//     }
					// }
					// 加载笔记
					this.getMoreNotes(0);
					this.getNoteStats();
				}
			});
		},
		onPullDownRefresh() {
			if (this.$refs.water1) {
				this.$refs.water1.refresh();
			}
			if (this.$refs.water2) {
				this.$refs.water2.refresh();
			}
			uni.stopPullDownRefresh();
		},
		onReady() {
			let query = uni.createSelectorQuery().in(this);
			query
				.selectAll('.info')
				.boundingClientRect(data => {
					if (data && data.length > 0 && data[0] && data[0].height) {
						this.mainInfoHeight = data[0].height - this.stickyHeight - 5;
					}
				})
				.exec();
		},
		onPageScroll(e) {
			// ✅ 修复：内容区域始终可以滚动，不需要根据滚动位置动态设置isScroll（与mine.vue保持一致）
			// if (e.scrollTop < this.mainInfoHeight) {
			// 	this.isScroll = false
			// 	this.showSend = false
			// } else {
			// 	this.isScroll = true
			// 	this.showSend = true
			// }

			// 参考 mine：吸顶滞后阈值；调大 unstickAt 使上滑时更早恢复流内样式，不一直保持吸顶
			const scrollTop = e.scrollTop;
			const stickAt = this.rpxToPx(520);
			const unstickAt = this.rpxToPx(400);
			if (scrollTop > stickAt) {
				this.tabsFixed = true;
				this.showSend = true;
				this.show = true;
			} else if (scrollTop < unstickAt) {
				this.tabsFixed = false;
				this.showSend = false;
				this.show = false;
			}
			this.pageScrollTop = Math.round(scrollTop / 16) * 16;
			const raw =
				e.scrollTop / (Number(this.screenHeight) - 80) >= 0 ?
				e.scrollTop / (Number(this.screenHeight) - 80) :
				e.scrollTop / Number(this.screenHeight);
			this.opacity = Math.min(1, raw);
		}
	};
</script>

<style lang="scss">
	.info {
		background-position: center;
		background-repeat: no-repeat;
		background-size: cover;
		background-color: rgba(0, 0, 0, 0.1);
		position: relative;
		z-index: 0;
	}

	.filter {
		width: 100%;
		height: 100%;
		position: absolute;
		top: 0;
		left: 0;
		z-index: -1;
		filter: brightness(65%);
		background-color: #ffffff;
		background-repeat: no-repeat;
		background-size: cover;
		background-position: center;
	}

	.top-main {
		margin-top: -50rpx;
		padding: 0 40rpx 20rpx 40rpx;
	}

	.status-bar {
		position: fixed;
		width: 100%;
		z-index: 1;
	}

	.navigation-bar {
		position: fixed;
		width: 100%;
		display: flex;
		align-items: center;
		z-index: 1;
		justify-content: center;
		margin-top: -75rpx;
	}

	/* 私信按钮：吸顶时浅底深字，与 mine 页参考一致、对比清晰 */
	.btn-private-msg {
		position: absolute;
		right: 120rpx;
		padding: 10rpx 25rpx;
		color: #f5f5f5;
		border: 1rpx solid rgba(245, 245, 245, 0.8);
		border-radius: 30rpx;
		height: 35rpx;
		line-height: 35rpx;
		font-size: 26rpx;
	}

	.btn-private-msg-sticky {
		background-color: rgba(245, 245, 245, 0.95);
		color: #333;
		border-color: rgba(245, 245, 245, 0.95);
	}

	/* 吸顶时 tab 栏：纯白底、顶部分割线，与 mine 页一致 */
	.othermine-tabs-bar {
		display: flex;
		position: relative;
		justify-content: center;
		height: 50px;
		align-items: center;
		background-color: #fff;
		border-top: 1rpx solid #eee;
	}

	/* 固定吸顶标签栏：紧贴顶栏底部，无空隙 */
	.tabs-bar-fixed {
		position: fixed;
		left: 0;
		right: 0;
		z-index: 998;
		box-shadow: 0 1rpx 0 #eee;
	}

	/* 参考 mine：吸顶时标签栏+下方同一块背景，与内容区同色，不突兀 */
	.othermine-sticky-block {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 5;
		display: flex;
		flex-direction: column;
	}

	.tabs-bar-fixed-inner {
		flex-shrink: 0;
		width: 100%;
		height: 55px;
		background-color: #fff;
		box-shadow: 0 1rpx 0 #eee;
		z-index: 1;
	}

	.othermine-sticky-block-bg {
		// flex: 1;
		// min-height: 0;
		// background-color: #f5f5f5;
	}

	.userinfo-main {
		display: inline-flex;
		margin-top: 80rpx;
	}

	.userinfo-main image {
		border-radius: 50%;
		height: 160rpx;
		width: 160rpx;
		margin: 40rpx 40rpx 40rpx 0;
	}

	.userinfo-main-right {
		margin: 50rpx 0rpx;

		text:nth-child(1) {
			color: #ffffff;
			font-size: 35rpx;
			display: block;
		}

		text:nth-child(2) {
			margin-top: 10rpx;
			color: #95949a;
			font-size: 25rpx;
			display: block;
		}

		text:nth-child(3) {
			color: #95949a;
			font-size: 25rpx;
			display: block;
		}
	}

	.introduction {
		color: #ffffff;
		font-size: 30rpx;
		margin-right: 40rpx;
		word-wrap: break-word;
		white-space: normal;
		word-break: break-all;
	}

	.tag {
		height: 19px;
		margin-top: 20rpx;
		margin-right: 20rpx;
		display: inline-flex;
		line-height: 20px;
		color: #ffffff;
		font-size: 28rpx;
		border-radius: 30rpx;
		background-color: rgba(255, 255, 255, 0.1);
		padding: 5rpx 20rpx;
	}

	.tag1 {
		color: #ffffff;
		height: 30px;
		border-radius: 30rpx;
		border-color: #ffffff;
		border-style: solid;
		border-width: 1px;
		line-height: 30px;
		padding: 0rpx 30rpx 0rpx 30rpx;
		justify-content: center;
		font-size: 27rpx;
		margin-bottom: 10px;

		image {
			height: 22px;
			margin-top: 4px;
			margin-bottom: 4px;
		}
	}

	.tag1-small {
		padding: 0rpx 18rpx 0rpx 18rpx;
	}

	.tag1-follow {
		background-color: #3d8af5;
		border-color: #3d8af5;
		color: #ffffff;
	}

	.guanzhu {
		margin-top: 20rpx;
		margin-bottom: 20rpx;
		margin-right: 25rpx;
		text-align: center;
		color: #ffffff;
		font-size: 24rpx;
	}

	.looked {
		width: 6em;
		margin-top: 30rpx;
		padding: 10rpx 0rpx 10rpx 20rpx;
		background-color: rgba(89, 88, 87, 0.6);
		border-radius: 20rpx;
	}

	::v-deep .u-sticky {
		border-top-left-radius: 20rpx;
		border-top-right-radius: 20rpx;
	}

	/* 内容区在用户资料背景之上，避免被 .info 背景压住；与标签栏紧贴无空隙 */
	.othermine-content-wrap {
		position: relative;
		z-index: 10;
		margin-top: 0;
		padding-top: 0;
		background-color: #f5f5f5;
	}

	.othermine-content-wrap .data_list,
	.othermine-content-wrap ::v-deep .uni-swiper-wrapper {
		margin-top: 0;
		padding-top: 0;
	}

	.othermine-content-wrap .component,
	.othermine-content-wrap ::v-deep scroll-view,
	.othermine-content-wrap ::v-deep .uni-swiper-item {
		margin-top: 0;
		padding-top: 0;
	}

	.notes {
		width: 750rpx;
		background-color: #ffffff;
		// overflow:auto;
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
</style>