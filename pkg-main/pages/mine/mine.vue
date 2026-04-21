<template>
	<view>
		<!-- 编辑简介弹窗 -->
		<mine-edit-introduction-popup :show="introductionShow" :screenHeight="screenHeight"
			:statusBarHeight="statusBarHeight" :value="selfIntroduction" @close="closeintroductionShow"
			@save="saveIntroduction"></mine-edit-introduction-popup>

		<!-- 侧边栏菜单 -->
		<mine-sidebar :show="moreShow" :screenHeight="screenHeight" @close="closeMore" @edit-data="editData"
			@drafts="goToDrafts" @my-comments="goToMyComments" @browse-record="goToBrowseRecord"
			@good-product="goToGoodProduct" @orders="goToOrders" @shopping-cart="goToShoppingCart"
			@address-management="goToAddressManagement" @ai-assistant="goToAIAssistant" @chat-history="goToChatHistory"
			@help-service="goToCustomerServiceFromMine" @about="goToAbout" @scan="handleScan"
			@setting="goToSetting"></mine-sidebar>

		<!-- 获赞与收藏弹窗 -->
		<mine-praise-collect-popup :show="getPraiseAndCollect" :count="count"
			@close="getPraiseAndCollect = false"></mine-praise-collect-popup>

		<!-- 用户信息头部：仅固定顶栏，滚动后 show 时显示 44px 灰条 -->
		<mine-header :inline="false" :userInfo="userInfo" :count="count" :statusBarHeight="statusBarHeight"
			:navigationBarHeight="navigationBarHeight" :iconHeight="iconHeight" :opacity="opacity" :showAvatar="show"
			@open-more="openMore" @scan="handleScan" @view-avatar="viewAvatarUrl"
			@attention-and-fans="goToAttentionAndFans" @praise-and-collect="getPraiseAndCollect = true"
			@edit-data="editData" @setting="goToSetting" @shopping-cart="goToShoppingCart" @orders="goToOrders"
			@browse-record="goToBrowseRecord" @ai-assistant="goToAIAssistant"
			@group-chat="goToGroupChatList"></mine-header>

		<!-- 标签页容器：顶部 profile 插槽内为可滚动的资料区，上滑时被标签栏+卡片盖住 -->
		<mine-tabs-container :stickyHeight="stickyHeight" :stickyTopBarHeight="stickyTopBarHeight" :userInfo="userInfo"
			:actTab="actTab" :tabsList="tabsList" :notesHeight="notesHeight" :isScroll="isScroll" :notesList="notesList"
			:noteType="noteType" :currentType="currentType" :idleList="idleList" :idleStatus="idleStatus"
			:loadingText="loadingText" :loadmoreText="loadmoreText" :nomoreText="nomoreText" ref="tabsContainer"
			@scroll="onMineScroll" @tab-click="changetabs" @swipe-change="swipeIndex" @reach-bottom="onReach"
			@idle-detail="goToIdleDetail" @edit-idle="editIdleProduct">
			<template #profile>
				<mine-header :inline="true" :userInfo="userInfo" :count="count" :statusBarHeight="statusBarHeight"
					:navigationBarHeight="navigationBarHeight" :iconHeight="iconHeight"
					@attention-and-fans="goToAttentionAndFans" @praise-and-collect="getPraiseAndCollect = true"
					@edit-data="editData" @setting="goToSetting" @shopping-cart="goToShoppingCart" @orders="goToOrders"
					@browse-record="goToBrowseRecord" @ai-assistant="goToAIAssistant" @group-chat="goToGroupChatList"
					@view-avatar="viewAvatarUrl"></mine-header>
			</template>
		</mine-tabs-container>

		<custom-tabbar ref="tabbar" :current="4" @update:current="updateCurrentTab"></custom-tabbar>
	</view>
</template>
<script>
	// import CustomTabbar from '@/components/custom-tabbar/CustomTabBar.vue'
	import MineSidebar from '@/components/mine-pages/MineSidebar.vue';
	import MineHeader from '@/components/mine-pages/MineHeader.vue';
	import MineTabsContainer from '@/components/mine-pages/MineTabsContainer.vue';
	import MineEditIntroductionPopup from '@/components/mine-pages/MineEditIntroductionPopup.vue';
	import MinePraiseCollectPopup from '@/components/mine-pages/MinePraiseCollectPopup.vue';
	import {
		getUserInfo,
		updateAvatarUrl
	} from '@/apis/user_service.js';
	import {
		confirmQrCodeLogin,
		scanQrCode
	} from '@/apis/auth_apis.js';
	import {
		baseUrl
	} from '@/.env.js';
	import {
		weChatTimeFormat
	} from '@/utils/util.js';
	import {
		getNotesByUserId,
		getNotesCountByUserId
	} from '@/apis/notes_service.js';
	import {
		getUserIdleProducts
	} from '@/apis/idles_service.js';
	import {
		provinceToAbbr
	} from '@/utils/addressUtils.js';
	import {
		formatProvince,
		formatAddress,
		clearWaterfall
	} from '@/utils/mineUtils.js';
	import {
		setTabCache as setTabCacheUtil,
		getTabCache as getTabCacheUtil,
		clearTabCache as clearTabCacheUtil
	} from '@/utils/mineCacheService.js';
	export default {
		components: {
			MineSidebar,
			MineHeader,
			MineTabsContainer,
			MineEditIntroductionPopup,
			MinePraiseCollectPopup
		},
		data() {
			return {
				count: {
					notesCount: 0,
					praiseCount: 0,
					collectCount: 0
				},
				getPraiseAndCollect: false,
				tabsList: [{
						name: '笔记'
					},
					{
						name: '闲宝'
					},
					{
						name: '点赞'
					},
					{
						name: '收藏'
					}
				],
				area: '',
				screenHeight: '',
				statusBarHeight: '',
				navigationBarHeight: '',
				stickyHeight: '',
				stickyTopBarHeight: 0,
				notesHeight: '',
				iconHeight: '',
				mainInfoHeight: 0,
				swiperHeight: 0,
				opacity: 0,
				userInfo: {},
				actTab: 0,
				isScroll: true,
				show: false,
				moreShow: false,
				introductionShow: false,
				notesList: [{
						notesList: [],
						status: 'loadmore',
						page: 1,
						pageSize: 6,
						total: 0
					},
					{
						notesList: [],
						status: 'loadmore',
						page: 1,
						pageSize: 6,
						total: 0
					},
					{
						notesList: [],
						status: 'loadmore',
						page: 1,
						pageSize: 6,
						total: 0
					}
				],
				// 闲置状态
				idleList: [],
				idleStatus: 'loadmore',
				idlePage: 1,
				idlePageSize: 6,
				idleTotal: 0,
				loadingText: '努力加载中',
				loadmoreText: '轻轻下拉',
				nomoreText: '实在没有了',
				noteType: 0,
				selfIntroduction: '',
				// 加载状态管理
				isLoading: true,
				// 性能优化相关
				tabSwitchTimer: null,
				// 🔧 移除缓存，避免数据重复
				// 🔧 新增：微信小程序端防抖定时器
				onShowTimer: null,
				// 🔧 新增：防止重复加载的标志
				isDataLoaded: false,
				// 🔧 新增：防止重复刷新的标志
				isRefreshing: false,
				// 🔧 新增：最后刷新时间
				lastRefreshTime: null,
				// 🔧 新增：防止onLoad重复调用的标志
				isOnLoadExecuted: false,
				// 🔧 新增：防止onShow重复调用的标志
				isOnShowHandled: false,
				onShowExecuting: false,
				// 🔧 新增：每个标签页的加载状态
				tabLoadedStates: [false, false, false, false], // 对应4个标签页
				// 🔧 新增：全局加载锁，防止并发请求
				globalLoadingLock: false,
				// 🔧 新增：页面初始化标志
				isInitialized: false,
				// 🔧 新增：缓存和预加载系统
				tabCache: {}, // 标签页缓存
				preloadTimer: null, // 预加载定时器
				cacheExpireTime: 10 * 60 * 1000 // 缓存过期时间（10分钟）
			};
		},
		computed: {
			currentType() {
				// type映射关系 - 顺序为 笔记、闲宝、赞过、收藏 → 1:笔记, 0:闲宝(不用), 2:赞过, 3:收藏
				const typeMap = [1, 0, 2, 3];
				const actTab = Number(this.actTab) || 0;
				return Number(typeMap[actTab]) || 1;
			}
		},
		methods: {
			// 标签顺序：0=笔记 1=闲宝 2=赞过 3=收藏；notesList 下标 0=笔记 1=赞过 2=收藏
			getNotesListIndex(tabIndex) {
				return tabIndex === 0 ? 0 : tabIndex === 2 ? 1 : tabIndex === 3 ? 2 : -1;
			},
			// notesList 下标 -> 标签页下标（0=笔记 1=赞过 2=收藏）
			getTabIndexFromNotesListIndex(notesIdx) {
				return notesIdx === 0 ? 0 : notesIdx === 1 ? 2 : notesIdx === 2 ? 3 : -1;
			},
			// 工具方法（从 utils 导入）
			formatProvince,
			formatAddress,
			// 🔧 缓存管理方法（使用工具函数）
			setTabCache(tabIndex, data) {
				setTabCacheUtil(this.tabCache, tabIndex, data, this.cacheExpireTime);
			},

			getTabCache(tabIndex) {
				return getTabCacheUtil(this.tabCache, tabIndex, this.cacheExpireTime);
			},

			// 🔧 新增：预加载下一个标签页
			preloadNextTab(currentTabIndex) {
				// 清除之前的预加载定时器
				if (this.preloadTimer) {
					clearTimeout(this.preloadTimer);
				}

				// 延迟预加载，避免影响当前标签页的加载
				this.preloadTimer = setTimeout(() => {
					const nextTabIndex = currentTabIndex + 1;
					if (nextTabIndex < 4) {
						// 0-3 对应4个标签页
						const cachedData = this.getTabCache(nextTabIndex);

						// 如果下一个标签页没有缓存，预加载它
						if (!cachedData && !this.tabLoadedStates[nextTabIndex]) {
							console.log(`🔄 Mine预加载下一个标签页: ${nextTabIndex}`);
							this.preloadTabData(nextTabIndex);
						}
					}
				}, 1000); // 1秒后预加载
			},

			// 🔧 新增：预加载标签页数据
			async preloadTabData(tabIndex) {
				try {
					console.log(`⏳ Mine开始预加载标签页: ${tabIndex}`);

					if (tabIndex === 0) {
						// 笔记标签页
						await this.getMoreNotes(0, this.noteType, false);
					} else if (tabIndex === 2) {
						// 赞过标签页
						await this.getMoreNotes(1, 0, false);
					} else if (tabIndex === 3) {
						// 收藏标签页
						await this.getMoreNotes(2, 0, false);
					} else if (tabIndex === 1) {
						// 闲宝标签页
						await this.loadIdleProducts(false);
					}

					console.log(`✅ Mine预加载完成: ${tabIndex}`);
				} catch (error) {
					console.warn(`⚠️ Mine预加载失败: ${tabIndex}`, error);
				}
			},

			// 🔧 新增：显示缓存状态（调试用）
			showMineCacheStatus() {
				console.log('📊 Mine缓存状态报告:', {
					tabCache: Object.keys(this.tabCache),
					tabLoadedStates: this.tabLoadedStates,
					cacheDetails: Object.keys(this.tabCache).map(key => ({
						key,
						timestamp: new Date(this.tabCache[key]?.timestamp || 0).toLocaleTimeString(),
						hasData: !!this.tabCache[key]?.notesList?.length
					}))
				});
			},

			// 🔧 新增：清理所有缓存
			clearAllCache() {
				this.tabCache = {};
				console.log('🧹 Mine所有缓存已清理');
			},

			// 🔧 新增：清理指定标签页的缓存
			clearTabCache(tabIndex) {
				clearTabCacheUtil(this.tabCache, tabIndex);
			},

			goToIdleDetail(id, type) {
				try {
					if (!id) return;
					if (Number(type) === 2) {
						uni.navigateTo({
							url: '/pkg-detail/pages/idleDetail/idleVideoD?productId=' + id
						});
					} else {
						uni.navigateTo({
							url: '/pkg-detail/pages/idleDetail/idleDetail?productId=' + id
						});
					}
				} catch (e) {
					console.warn('goToIdleDetail error', e);
				}
			},
			updateCurrentTab(index) {
				console.log('切换到 tab:', index);
			},
			moveHandle(e) {},
			goToSetting() {
				uni.navigateTo({
					url: '/pkg-mine/pages/setting/setting'
				});
			},
			/**
			 * 跳转到AI助手页面
			 */
			goToAIAssistant() {
				this.closeMore();
				uni.navigateTo({
					url: '/pkg-ai/pages/model-select/model-select'
				});
			},
			/**
			 * 跳转到对话记录页面
			 */
			goToChatHistory() {
				this.closeMore();
				uni.navigateTo({
					url: '/pkg-ai/pages/chat-history/chat-history'
				});
			},
			/**
			 * 跳转到浏览记录页面
			 */
			goToGroupChatList() {
				uni.navigateTo({
					url: '/pkg-msg/pages/groupChatList/groupChatList'
				});
			},
			goToBrowseRecord() {
				uni.navigateTo({
					url: '/pkg-mine/pages/browseRecord/browseRecord'
				});
			},
			/**
			 * 侧边栏 - 帮助与客服：跳转到客服聊天页面
			 */
			goToCustomerServiceFromMine() {
				this.closeMore();
				const customerServiceId = '0';
				const customerServiceName = '客服';
				const customerServiceAvatar = '/static/customer-service-avatar.png'; // 可根据需要替换为实际客服头像

				uni.navigateTo({
					url: `/pkg-msg/pages/chat/chat?userId=${customerServiceId}&userName=${customerServiceName}&avatarUrl=${customerServiceAvatar}&chatType=0`,
					fail: err => {
						console.error('跳转客服失败:', err);
						uni.showToast({
							title: '页面加载失败',
							icon: 'none'
						});
					}
				});
			},
			/**
			 * 跳转到订单页面
			 */
			goToOrders() {
				uni.navigateTo({
					url: '/pkg-mine/pages/orders/orders'
				});
			},
			/**
			 * 跳转到市集页面
			 */
			goToShopping() {
				uni.switchTab({
					url: '/pkg-main/pages/idle/idle'
				});
			},
			/**
			 * 跳转到购物车页面
			 */
			goToShoppingCart() {
				uni.navigateTo({
					url: '/pkg-mine/pages/shoppingCart/shoppingCart'
				});
			},
			/**
			 * 跳转到草稿页面
			 */
			goToDrafts() {
				uni.navigateTo({
					url: '/pkg-mine/pages/drafts/drafts'
				});
			},
			/**
			 * 跳转到收到的评论页面（消息页面的评论和@）
			 */
			goToMyComments() {
				this.closeMore();
				uni.navigateTo({
					url: '/pkg-msg/pages/comment/comment'
				});
			},
			/**
			 * 跳转到闲置好物页面（市集页面）
			 */
			goToGoodProduct() {
				// 先关闭侧边栏
				this.closeMore();
				// 使用 navigateTo 跳转到市集页面
				uni
					.navigateTo({
						url: '/pkg-main/pages/idle/idle'
					})
					.catch(err => {
						// 如果页面已经在栈中，使用 reLaunch
						console.log('navigateTo 失败，尝试 reLaunch:', err);
						uni.reLaunch({
							url: '/pkg-main/pages/idle/idle'
						});
					});
			},
			/**
			 * 跳转到收货地址管理页面
			 */
			goToAddressManagement() {
				uni.navigateTo({
					url: '/pkg-mine/pages/addressManagement/addressManagement'
				});
			},
			/**
			 * 跳转到关于页面
			 */
			goToAbout() {
				// TODO: 实现关于页面跳转
				uni.showToast({
					title: '功能开发中',
					icon: 'none'
				});
			},
			async getMoreNotes(index, authority, isInitialLoad = false) {
				// 统一用 tab 下标做缓存/预加载/已加载标记（index 为 notesList 下标）
				const tabIndex = this.getTabIndexFromNotesListIndex(index);
				// 🔧 防重复加载：检查当前标签页是否已加载
				if (isInitialLoad && tabIndex >= 0 && this.tabLoadedStates[tabIndex]) {
					console.log(`🔄 标签页${tabIndex}已加载，跳过重复加载`);
					return;
				}

				// 🔧 防重复加载：检查全局加载锁
				if (this.globalLoadingLock) {
					console.log(`🔄 全局加载锁已锁定，跳过重复加载 [index=${index}]`);
					return;
				}

				// 🔧 新增：检查缓存（仅在首次加载时检查，缓存 key 为 tab 下标）
				if (isInitialLoad && tabIndex >= 0) {
					const cachedData = this.getTabCache(tabIndex);
					if (cachedData) {
						console.log(`✅ Mine使用缓存数据: tab_${tabIndex}`);
						// 恢复缓存的数据
						this.notesList[index] = {
							notesList: cachedData.notesList || [],
							status: cachedData.status || 'loadmore',
							page: cachedData.page || 1,
							pageSize: cachedData.pageSize || 6,
							total: cachedData.total || 0
						};

						// 更新瀑布流组件
						if (cachedData.notesList && cachedData.notesList.length > 0) {
							this.updateWaterfallComponent(index, cachedData.notesList);
						}

						// 标记为已加载（按 tab 下标）
						this.tabLoadedStates[tabIndex] = true;

						// 预加载下一个标签页（传入 tab 下标，避免赞过完成后又预加载自己）
						this.preloadNextTab(tabIndex);
						return;
					}
				}

				// 🔧 修复：下拉刷新时（isInitialLoad=false），跳过isRefreshing检查
				// 因为下拉刷新时isRefreshing可能还是true，但我们希望强制刷新
				if (this.isRefreshing && isInitialLoad) {
					console.log(`🔄 正在加载中，跳过重复加载 [index=${index}]`);
					return;
				}

				// 🔧 修复：下拉刷新时（isInitialLoad=false），允许重新加载
				// 即使状态是loading，下拉刷新也应该强制重新加载
				if (this.notesList[index].status === 'loading' && isInitialLoad) {
					console.log(`🔄 标签页${index}正在加载中，跳过重复加载`);
					return;
				}

				// 🔧 防重复加载：检查页码是否合理
				if (this.notesList[index].page < 1) {
					console.log(`🔄 页码异常，跳过加载 [index=${index}, page=${this.notesList[index].page}]`);
					return;
				}

				// 🔧 设置加载状态（只在首次加载时设置锁，后续页加载不需要）
				if (isInitialLoad) {
					this.isRefreshing = true;
					this.globalLoadingLock = true;
				}
				console.log(
					`🔄 开始加载数据 [index=${index}, page=${this.notesList[index].page}, isInitialLoad=${isInitialLoad}]`
				);

				// type映射关系 - notesList索引: 0=笔记(type1), 1=赞过(type2), 2=收藏(type3)
				const typeMap = [1, 2, 3];

				// 🔧 修复：下拉刷新时（isInitialLoad=false），允许重新加载
				// 即使状态是nomore或loading，下拉刷新也应该强制重新加载
				if (
					(this.notesList[index].status === 'nomore' || this.notesList[index].status === 'loading') &&
					isInitialLoad
				) {
					console.log(`🔄 标签页${index}状态为${this.notesList[index].status}，跳过加载`);
					this.isRefreshing = false; // 重置状态
					this.globalLoadingLock = false; // 释放全局锁
					return;
				}

				// ✅ 添加环境检测日志
				console.log('=== getMoreNotes 调试信息 ===');
				console.log('平台:', process.env.UNI_PLATFORM);
				console.log('index:', index);
				console.log('type:', typeMap[index]);

				// ✅ 检查账号信息
				try {
					const accountInfo = uni.getAccountInfoSync && uni.getAccountInfoSync();
					if (accountInfo) {
						console.log('小程序版本:', accountInfo.miniProgram?.envVersion);
						console.log('appId:', accountInfo.miniProgram?.appId);
					}
				} catch (e) {
					console.log('获取账号信息失败:', e);
				}

				// 🔧 移除预加载缓存检查，避免数据重复

				// 🔧 移除缓存机制，避免数据重复

				this.notesList[index].status = 'loading';

				try {
					const userId = uni.getStorageSync('userInfo')?.id;
					if (!userId) {
						console.error('❌ userId 不存在，请先登录');
						this.notesList[index].status = 'nomore';
						uni.showToast({
							title: '请先登录',
							icon: 'none',
							duration: 1000
						});
						return;
					}

					// ✅ 检查Token
					const token = uni.getStorageSync('uniapp_token');
					console.log('Token 存在:', !!token);
					console.log('Token 前20字符:', token ? token.substring(0, 20) + '...' : 'null');
					console.log('UserId:', userId);

					const params = {
						userId: userId,
						page: this.notesList[index].page,
						pageSize: this.notesList[index].pageSize,
						authority: authority,
						type: typeMap[index],
						_t: Date.now() // 🔧 修复：添加时间戳，防止微信小程序HTTP缓存
					};
					console.log(`✅ 加载笔记请求参数 [index=${index}]:`, JSON.stringify(params));
					if (!isInitialLoad) {
						console.log(`🔄 下拉刷新：强制从服务器获取最新数据 [index=${index}]`);
					}

					// ✅ 记录请求开始时间
					const startTime = Date.now();
					console.log('🚀 开始请求时间:', new Date().toLocaleTimeString());

					const res = await getNotesByUserId(params);

					// ✅ 记录请求结束时间
					const endTime = Date.now();
					console.log('⏱️ 请求耗时:', endTime - startTime, 'ms');

					// ✅ 详细检查响应
					console.log('响应 code:', res?.code);
					console.log('响应 msg:', res?.msg);
					console.log('响应 data:', res?.data ? '存在' : '不存在');
					console.log(
						'响应 records:',
						res?.data?.records ? `${res.data.records.length}条` : '不存在'
					);

					if (res && res.code === 200) {
						if (!res.data || !res.data.records) {
							console.warn('响应数据格式异常:', res);
							this.notesList[index].status = 'nomore';
							return;
						}

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
								pinned: parseInt(item.pinned, 10) || 0,
								auditStatus: item.auditStatus,
								uid: item.uid,
								updateTime: item.time ? weChatTimeFormat(item.time) : ''
							};
						}

						const mappedList = res.data.records.map(mapNoteItem);
						console.log(`映射后数据 [index=${index}]:`, mappedList.length, '条');

						// 处理置顶数据：将置顶数据提取到最前面
						const pinnedItems = mappedList.filter(item => item.pinned === 1);
						const normalItems = mappedList.filter(item => item.pinned !== 1);

						console.log(`置顶数据: ${pinnedItems.length}条, 普通数据: ${normalItems.length}条`);

						// 🔧 修复：在设置数据前保存是否为空数组的标志（用于判断是否是下拉刷新）
						const wasEmpty = this.notesList[index].notesList.length === 0;
						const isFirstPage = this.notesList[index].page === 1;

						// 🔧 修复：正确处理数据追加，避免重复
						if (isFirstPage) {
							// 第一页：数据加载完成后，直接设置新数据，让瀑布流组件的 watch 自动处理
							// 🔧 修复：不要先清空组件，直接设置新数据，避免空白

							// 1. 准备新数据
							const newData = [...pinnedItems, ...normalItems];

							// 2. 🔧 修复：直接设置新数据，不清空组件
							// 瀑布流组件的 watch 会检测到数据变化，自动处理清空和重新初始化
							// 使用$set确保响应式更新
							this.$set(this.notesList[index], 'notesList', newData);
							console.log(
								`第一页数据设置完成: 置顶${pinnedItems.length}条, 普通${normalItems.length}条`
							);

							// 3. 等待数据设置完成，确保UI更新
							await this.$nextTick();

							// 🔧 修复收藏/赞过首次空白：swiper 非当前项可能延后挂载，数据先到时机不对导致 watch 未触发
							// 首页加载完成后延迟强制刷新对应瀑布流，确保组件挂载后能正确渲染
							if (index >= 1 && newData.length > 0) {
								setTimeout(() => {
									const waterRef = this.$refs.tabsContainer?.$refs['water' + (index + 1)];
									if (waterRef && typeof waterRef.init === 'function') {
										waterRef.init();
										console.log(
											`首页数据已设置，已强制刷新瀑布流 [index=${index}, water${index + 1}]`
										);
									}
								}, 150);
							}
							console.log(`第一页数据已通过 :list prop 自动更新到瀑布流组件 [index=${index}]`);
						} else {
							// 后续页：去重后添加新数据
							const existingIds = new Set(this.notesList[index].notesList.map(item => item.id));
							const newItems = normalItems.filter(item => !existingIds.has(item.id));
							this.notesList[index].notesList = [...this.notesList[index].notesList, ...newItems];
							console.log(
								`去重后新增数据: ${newItems.length}条，跳过重复: ${
                normalItems.length - newItems.length
              }条`
							);

							// 🔧 修复：保存 newItems 用于后续传递给瀑布流组件
							// 因为 newItems 已经去重，可以直接使用
							this.notesList[index]._lastNewItems = newItems;
						}

						this.notesList[index].page++;
						this.notesList[index].total = res.data.total;

						// 设置状态：只有当返回的数据少于pageSize时才设置为nomore
						const isLastPage = mappedList.length < this.notesList[index].pageSize;

						// 🔧 修复：移除手动调用 updateWaterfallComponent，让组件的 watch 自动处理
						// 因为 water-fall 组件的 watch 会自动检测数据追加并调用 addList
						// 如果手动调用 addList，会导致重复调用（watch 一次 + 手动一次）
						// 第一页和后续页都通过更新 notesList[index].notesList 来触发 watch
						console.log(
							`数据已更新到 notesList，由组件的 watch 自动处理 [index=${index}, isFirstPage=${isFirstPage}]`
						);

						// 清理临时数据
						if (this.notesList[index]._lastNewItems) {
							delete this.notesList[index]._lastNewItems;
						}

						// 设置状态
						this.notesList[index].status = isLastPage ? 'nomore' : 'loadmore';

						console.log(
							`加载完成: 第${this.notesList[index].page - 1}页, 返回${
              mappedList.length
            }条数据, 状态: ${this.notesList[index].status}, 总数据: ${
              this.notesList[index].notesList.length
            }条`
						);

						// 🔧 标记当前标签页已加载（按 tab 下标，与 preloadNextTab 一致）
						if (tabIndex >= 0) this.tabLoadedStates[tabIndex] = true;

						// 🔧 新增：保存到缓存（key 为 tab 下标）
						if (tabIndex >= 0)
							this.setTabCache(tabIndex, {
								notesList: this.notesList[index].notesList,
								status: this.notesList[index].status,
								page: this.notesList[index].page,
								pageSize: this.notesList[index].pageSize,
								total: this.notesList[index].total
							});

						// 🔧 新增：预加载下一个标签页（传入 tab 下标，赞过=3 时 next=4 不再预加载）
						if (tabIndex >= 0) this.preloadNextTab(tabIndex);

						// 🔧 重置加载状态（只在首次加载时重置锁）
						if (isInitialLoad) {
							this.isRefreshing = false;
							this.globalLoadingLock = false;
						}
						console.log(
							`🔄 数据加载完成 [index=${index}], page=${this.notesList[index].page}, status=${this.notesList[index].status}`
						);
					} else {
						console.warn(`接口返回异常 [index=${index}]:`, res);
						this.notesList[index].status = 'nomore';
						if (res && res.msg) {
							uni.showToast({
								title: res.msg,
								icon: 'none',
								duration: 2000
							});
						}

						// 🔧 重置加载状态（只在首次加载时重置锁）
						if (isInitialLoad) {
							this.isRefreshing = false;
							this.globalLoadingLock = false;
						}
						console.log(`🔄 数据加载失败 [index=${index}]`);
					}
				} catch (err) {
					console.error(`加载笔记失败 [index=${index}]:`, err);
					this.notesList[index].status = 'nomore';
					uni.showToast({
						title: '加载失败，请重试',
						icon: 'none',
						duration: 2000
					});

					// 🔧 重置加载状态（只在首次加载时重置锁）
					if (isInitialLoad) {
						this.isRefreshing = false;
						this.globalLoadingLock = false;
					}
					console.log(`🔄 数据加载异常 [index=${index}]`);
				}
			},

			// 🔧 新增：手动刷新当前标签页数据
			async manualRefreshCurrentTab() {
				console.log('🔄 用户手动刷新当前标签页数据');
				await this.refreshCurrentTabData();
			},

			// 🔧 新增：手动刷新所有标签页数据
			async manualRefreshAllTabs() {
				console.log('🔄 用户手动刷新所有标签页数据');
				this.isDataLoaded = false; // 重置加载状态
				await this.refreshCurrentTabData();
			},

			// 🔧 移除缓存相关方法，避免数据重复

			// 🔧 简化：刷新当前标签页数据（移除缓存逻辑）
			async refreshCurrentTabData() {
				// 🔧 防止重复刷新 - 更严格的检查
				if (this.isRefreshing) {
					console.log('🔄 正在刷新中，跳过重复调用');
					return;
				}

				// 🔧 防止短时间内重复调用
				const now = Date.now();
				if (this.lastRefreshTime && now - this.lastRefreshTime < 1000) {
					console.log('🔄 刷新间隔太短，跳过重复调用');
					return;
				}

				this.isRefreshing = true;
				this.lastRefreshTime = now;

				try {
					console.log('🔄 刷新当前标签页数据，actTab:', this.actTab);

					// 重置当前标签页的数据
					this.resetTabData(this.actTab);

					// 重新加载数据
					if (this.actTab === 0) {
						// 笔记标签页
						await this.getMoreNotes(0, this.noteType, false); // 不使用缓存
					} else if (this.actTab === 2) {
						// 赞过标签页
						await this.getMoreNotes(1, 0, false);
					} else if (this.actTab === 3) {
						// 收藏标签页
						await this.getMoreNotes(2, 0, false);
					} else if (this.actTab === 1) {
						// 闲宝标签页
						await this.loadIdleProducts(true); // 重置数据
					}

					console.log('✅ 当前标签页数据刷新完成');
				} catch (error) {
					console.error('❌ 刷新当前标签页数据失败:', error);
				} finally {
					this.isRefreshing = false;
				}
			},

			// 🔧 移除缓存相关方法，避免数据重复

			// 🔧 重置指定标签页数据（标签顺序 0笔记 1闲宝 2收藏 3赞过）
			resetTabData(tabIndex) {
				const clearWaterfall = ref => {
					if (ref && ref.clear) {
						ref.clear();
						// #ifdef MP-WEIXIN
						this.$forceUpdate();
						// #endif
					}
				};
				if (tabIndex === 1) {
					// 闲宝
					this.idleList = [];
					this.idleStatus = 'loadmore';
					this.idlePage = 1;
					this.idleTotal = 0;
					this.$refs.tabsContainer?.$refs?.idleWaterfall?.clear();
				} else {
					const notesIdx = this.getNotesListIndex(tabIndex);
					if (notesIdx < 0) return;
					this.notesList[notesIdx] = {
						notesList: [],
						status: 'loadmore',
						page: 1,
						pageSize: 6,
						total: 0
					};
					if (tabIndex == 0) clearWaterfall(this.$refs.tabsContainer?.$refs?.water1);
					else if (tabIndex == 2) clearWaterfall(this.$refs.tabsContainer?.$refs?.water2);
					else if (tabIndex == 3) clearWaterfall(this.$refs.tabsContainer?.$refs?.water3);
				}
			},

			// 🔧 移除预加载方法，避免数据重复

			// 🔧 修复：更新瀑布流组件，避免重复数据（微信小程序端优化）
			updateWaterfallComponent(index, data) {
				console.log(
					`更新瀑布流组件: index=${index}, page=${this.notesList[index].page}, data.length=${data?.length}`
				);

				// 🔧 修复：数据验证
				if (!data || data.length === 0) {
					console.log(`数据为空，跳过更新 [index=${index}]`);
					return;
				}

				// 🔧 修复：后续页数据（page > 1，即第二页及以后）需要手动添加到瀑布流组件
				// 第一页数据通过 :list prop 自动更新
				if (this.notesList[index].page > 1) {
					// 后续页：只添加新数据（瀑布流组件内部会处理去重）
					const addOnly = ref => {
						if (ref && ref.addList) {
							console.log(
								`追加数据到瀑布流 [index=${index}, page=${this.notesList[index].page}, data.length=${data.length}]`
							);
							ref.addList(data);
						} else {
							console.warn(`瀑布流组件引用不存在或没有addList方法 [index=${index}]`);
						}
					};

					if (index == 0) {
						addOnly(this.$refs.tabsContainer?.$refs?.water1);
					} else if (index == 1) {
						addOnly(this.$refs.tabsContainer?.$refs?.water2);
					} else if (index == 2) {
						addOnly(this.$refs.tabsContainer?.$refs?.water3);
					}
				} else {
					// 第一页：数据已经通过 :list prop 自动更新，这里跳过
					console.log(`第一页数据已通过 :list prop 自动更新，跳过手动操作 [index=${index}]`);
				}
			},

			// 🔧 移除：不再需要此方法，因为瀑布流组件通过 :list prop 自动绑定数据
			// 手动调用 addList 会导致数据重复
			getMoreDraftNotes() {
				if (this.notesList[0].status == 'nomore' || this.notesList[0].status == 'loading') {
					return;
				}

				// 🔧 移除草稿缓存检查，避免数据重复

				this.notesList[0].status = 'loading';
				let offset = (this.notesList[0].page - 1) * this.notesList[0].pageSize;
				let sql1 =
					`select * from draft_notes draft_notes order by id desc limit ${this.notesList[0].pageSize} offset ${offset}`;
				this.$sqliteUtil
					.SqlSelect(sql1)
					.then(res => {
						res.forEach(item => {
							item.updateTime = weChatTimeFormat(item.updateTime);
							// OSS 默认封面已移除，保持空字符串
							// if (item.coverPicture == '') {
							// 	item.coverPicture = ''
							// }
						});
						// 追加数据而不是替换
						if (this.notesList[0].page === 1) {
							// 第一页，直接设置
							this.notesList[0].notesList = res;
						} else {
							// 后续页面，追加到现有数据
							this.notesList[0].notesList = [...this.notesList[0].notesList, ...res];
						}

						this.notesList[0].page++;
						setTimeout(() => {
							// 统一处理：只添加新数据，不清空瀑布流
							// 这样可以保持滚动位置，避免闪烁
							this.$refs.tabsContainer?.$refs?.water1?.addList(res);

							if (res.length < this.notesList[0].pageSize) {
								this.notesList[0].status = 'nomore';
							} else {
								this.notesList[0].status = 'loadmore';
								// 🔧 移除预加载，避免数据重复
							}
						}, 700);
					})
					.catch(err => {
						console.log(err);
					});
			},

			// 🔧 移除草稿预加载方法，避免数据重复

			changeNoteType(e) {
				// this.$refs.loadingMine.reset()
				this.noteType = e;
				this.notesList[0] = {
					notesList: [],
					status: 'loadmore',
					page: 1,
					pageSize: 6,
					total: 0
				};
				this.$refs.tabsContainer?.$refs?.water1?.clear();
				if (e === 2) {
					//草稿
					this.getMoreDraftNotes();
				} else {
					this.getMoreNotes(0, e);
				}
			},
			goToAttentionAndFans(e) {
				console.log(e);
				uni.navigateTo({
					url: '/pkg-mine/pages/attentionAndFans/attentionAndFans?userId=' +
						this.userInfo.id +
						'&type=' +
						e +
						'&attentionNum=' +
						this.userInfo.attentionNum +
						'&fansNum=' +
						this.userInfo.fansNum
				});
			},
			editData() {
				uni.navigateTo({
					url: '/pkg-profile/pages/editData/editData'
				});
			},
			changeIntroduction() {
				this.introductionShow = true;
				// 尝试隐藏TabBar，如果失败则忽略错误（自定义TabBar可能不支持此API）
				uni.hideTabBar({
					animation: true,
					fail: err => {
						console.log('hideTabBar failed:', err);
					}
				});
			},
			closeintroductionShow() {
				this.introductionShow = false;
				// 尝试显示TabBar，如果失败则忽略错误（自定义TabBar可能不支持此API）
				uni.showTabBar({
					animation: true,
					fail: err => {
						console.log('showTabBar failed:', err);
					}
				});
			},
			saveIntroduction(value) {
				this.selfIntroduction = value;
				updateAvatarUrl({
						userVO: {
							id: this.userInfo.id,
							selfIntroduction: value
						}
					})
					.then(res => {
						if (res.code === 200) {
							this.userInfo.selfIntroduction = value;
							uni.setStorageSync('userInfo', this.userInfo);
							this.closeintroductionShow();
							uni.showToast({
								title: '保存成功',
								icon: 'success'
							});
						} else {
							uni.showToast({
								title: res.msg || '保存失败',
								icon: 'none'
							});
						}
					})
					.catch(err => {
						console.error('保存简介失败:', err);
						uni.showToast({
							title: '保存失败',
							icon: 'none'
						});
					});
			},
			viewAvatarUrl() {
				uni.previewImage({
					urls: [this.userInfo.avatarUrl],
					current: 0,
					indicator: 'none',
					longPressActions: {
						itemList: ['更换头像', '保存到相册'],
						success: data => {
							console.log(data.tapIndex);
							plus.nativeUI.closePreviewImage();
							if (data.tapIndex === 0) {
								uni.chooseImage({
									count: 1,
									sizeType: ['original', 'compressed'],
									sourceType: ['album'],
									success: res => {
										console.log(res.tempFilePaths);
										uni.uploadFile({
											url: baseUrl + '/web/oss/upload',
											filePath: res.tempFilePaths[0],
											name: 'file',
											header: {
												accessToken: uni.getStorageSync('token') ||
													''
											},
											success: res => {
												let data = JSON.parse(res.data);
												if (res.code === 200) {
													uni.showToast({
														title: '更换成功',
														icon: 'success',
														duration: 500,
														mask: true
													});
													this.userInfo.avatarUrl = data
														.data;
													uni.setStorageSync('userInfo', this
														.userInfo);
												} else {
													uni.showToast({
														title: '更换失败',
														icon: 'none',
														duration: 500,
														mask: true
													});
												}
											},
											complete() {
												uni.closePreviewImage({
													success: function() {
														console.log('关闭成功');
													}
												});
											}
										});
									},
									fail: err => {
										console.log(err.errMsg);
									}
								});
							} else if (data.tapIndex === 1) {
								this.downLoadImg(this.userInfo.avatarUrl);
							}
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
						} else {
							uni.showToast({
								title: '下载失败',
								icon: 'none'
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
			openMore() {
				this.moreShow = true;
				// 尝试隐藏TabBar，如果失败则忽略错误（自定义TabBar可能不支持此API）
				uni.hideTabBar({
					animation: true,
					fail: err => {
						console.log('hideTabBar failed:', err);
					}
				});
			},
			closeMore() {
				this.moreShow = false;
				// 尝试显示TabBar，如果失败则忽略错误（自定义TabBar可能不支持此API）
				uni.showTabBar({
					animation: true,
					fail: err => {
						console.log('showTabBar failed:', err);
					}
				});
			},
			handleScan() {
				// 检查平台是否支持扫码功能
				// #ifdef H5
				uni.showToast({
					title: 'H5平台暂不支持扫码功能',
					icon: 'none',
					duration: 2000
				});
				return;
				// #endif

				// 调用扫码功能
				uni.scanCode({
					success: async res => {
						console.log('扫码结果:', res);

						try {
							// 检查是否是二维码登录链接
							if (res.result && res.result.includes('loginToken=')) {
								// 解析loginToken
								let loginToken = '';
								if (res.result.startsWith('hongshu://')) {
									// 处理自定义协议，手动解析参数
									const queryString = res.result.split('?')[1];
									if (queryString) {
										const params = queryString.split('&');
										for (let param of params) {
											const [key, value] = param.split('=');
											if (key === 'loginToken') {
												loginToken = value;
												break;
											}
										}
									}
								} else {
									// 处理普通URL，手动解析参数
									const queryString = res.result.split('?')[1];
									if (queryString) {
										const params = queryString.split('&');
										for (let param of params) {
											const [key, value] = param.split('=');
											if (key === 'loginToken') {
												loginToken = value;
												break;
											}
										}
									}
								}

								if (loginToken) {
									console.log('解析到loginToken:', loginToken);

									// 获取当前用户ID
									const userInfo = uni.getStorageSync('userInfo');
									console.log('当前用户信息:', userInfo);

									if (!userInfo || !userInfo.id) {
										uni.showToast({
											title: '请先登录',
											icon: 'none',
											duration: 2000
										});
										return;
									}

									// 第一步：扫描二维码
									console.log('开始扫描二维码...');
									const scanRes = await scanQrCode(loginToken);
									console.log('扫描响应:', scanRes);
									console.log('扫描响应类型:', typeof scanRes);
									console.log('扫描响应keys:', Object.keys(scanRes));

									// 检查响应是否成功
									if (scanRes.code !== 200) {
										uni.showToast({
											title: scanRes.message || '扫描失败',
											icon: 'none',
											duration: 2000
										});
										return;
									}

									// 检查业务逻辑是否成功
									// scanRes.data 应该包含 {success: true, message: "扫描成功，等待确认"}
									console.log('scanRes.data:', scanRes.data);
									console.log('scanRes.data类型:', typeof scanRes.data);

									if (!scanRes.data || !scanRes.data.success) {
										const errorMsg = scanRes.data?.message || scanRes.message || '扫描失败';
										uni.showModal({
											title: '扫码失败',
											content: errorMsg + '\n\n请刷新网页端二维码后重试',
											showCancel: false,
											confirmText: '知道了'
										});
										return;
									}

									// 第二步：确认登录
									uni.showModal({
										title: '确认登录',
										content: '是否确认在网页端登录？',
										success: async modalRes => {
											if (modalRes.confirm) {
												console.log('开始确认二维码登录...');
												const confirmRes = await confirmQrCodeLogin({
													loginToken: loginToken,
													userId: userInfo.id
												});
												console.log('确认登录响应:', confirmRes);

												if (confirmRes.success) {
													uni.showToast({
														title: '登录确认成功',
														icon: 'success',
														duration: 2000
													});
												} else {
													uni.showToast({
														title: confirmRes.message ||
															'登录确认失败',
														icon: 'none',
														duration: 2000
													});
												}
											}
										}
									});
								} else {
									uni.showToast({
										title: '无效的登录二维码',
										icon: 'none',
										duration: 2000
									});
								}
							} else {
								// 其他类型的二维码
								uni.showToast({
									title: '扫码成功',
									icon: 'success',
									duration: 2000
								});
								console.log('扫码内容:', res.result);
							}
						} catch (error) {
							console.error('处理扫码结果失败:', error);
							uni.showToast({
								title: '处理失败',
								icon: 'none',
								duration: 2000
							});
						}
					},
					fail: err => {
						console.log('扫码操作结束:', err);

						// 检查是否是用户主动取消
						const errMsg = err.errMsg || err.message || '';
						if (
							errMsg.includes('cancel') ||
							errMsg.includes('取消') ||
							errMsg.includes('用户取消') ||
							errMsg.includes('user cancel') ||
							errMsg.includes('scanCode:fail cancel') ||
							errMsg.includes('scanCode:fail user cancel')
						) {
							// 用户主动取消，不显示任何提示
							console.log('用户主动取消扫码');
							return;
						}

						// 真正的扫码失败才显示提示
						console.error('扫码失败:', err);
						uni.showToast({
							title: '扫码失败',
							icon: 'none',
							duration: 2000
						});
					}
				});
			},
			rpxToPx(rpx) {
				const windowInfo = uni.getWindowInfo();
				const screenWidth = windowInfo.screenWidth;
				return (screenWidth * Number.parseInt(rpx)) / 750;
			},
			changetabs(e) {
				let index = e.index;
				if (this.actTab == index) {
					// 防抖处理
					if (this.tabSwitchTimer) {
						clearTimeout(this.tabSwitchTimer);
					}

					this.tabSwitchTimer = setTimeout(() => {
						this.handleTabSwitch(index);
					}, 150);
					return;
				}

				this.actTab = index;
				this.handleTabSwitch(index);
			},

			// 处理标签切换逻辑
			handleTabSwitch(index) {
				console.log(`🔄 Mine切换标签页: ${index}`);

				// 闲宝页单独处理（标签顺序 0笔记 1闲宝 2收藏 3赞过）
				if (index === 1) {
					if (this.idlePage === 1 && !this.tabLoadedStates[index] && this.idleStatus !== 'loading') {
						this.loadIdleProducts(true);
					} else if (this.tabLoadedStates[index]) {
						console.log(`🔄 闲宝标签页已加载，跳过重复加载`);
					}
					return;
				}

				const notesIdx = this.getNotesListIndex(index);
				if (notesIdx < 0) return;

				// 🔧 新增：检查缓存
				const cachedData = this.getTabCache(index);
				if (cachedData) {
					console.log(`✅ Mine标签切换使用缓存: tab_${index}`);
					this.notesList[notesIdx] = {
						notesList: cachedData.notesList || [],
						status: cachedData.status || 'loadmore',
						page: cachedData.page || 1,
						pageSize: cachedData.pageSize || 6,
						total: cachedData.total || 0
					};

					if (cachedData.notesList && cachedData.notesList.length > 0) {
						this.updateWaterfallComponent(notesIdx, cachedData.notesList);
					}

					this.tabLoadedStates[index] = true;
					this.preloadNextTab(index);
					return;
				}

				const hasData = this.notesList[notesIdx].notesList.length > 0;

				if (!hasData) {
					if (index == 0) {
						if (this.noteType == 2) {
							this.getMoreDraftNotes();
						} else {
							this.getMoreNotes(0, this.noteType, true);
						}
					} else if (index == 2) {
						this.getMoreNotes(1, 0, true);
					} else if (index == 3) {
						this.getMoreNotes(2, 0, true);
					}
				}
			},
			swipeIndex(e) {
				this.actTab = e.detail.current;
				let index = e.detail.current;

				if (index === 1) {
					if (this.idlePage === 1 && !this.tabLoadedStates[index]) {
						this.loadIdleProducts(true);
					}
					return;
				}

				const notesIdx = this.getNotesListIndex(index);
				if (notesIdx < 0) return;

				if (this.notesList[notesIdx].page == 1 && !this.tabLoadedStates[index]) {
					console.log(`🔄 切换到标签页${index}，加载数据`);
					if (index == 0) {
						if (this.noteType == 2) {
							this.getMoreDraftNotes();
						} else {
							this.getMoreNotes(0, this.noteType, true);
						}
					} else if (index == 2) {
						this.getMoreNotes(1, 0, true);
					} else if (index == 3) {
						this.getMoreNotes(2, 0, true);
					}
				} else if (this.tabLoadedStates[index]) {
					console.log(`🔄 标签页${index}已加载，跳过重复加载`);
				}
			},
			setSwiperHeight() {
				setTimeout(() => {
					let query = uni.createSelectorQuery().in(this);
					query
						.selectAll('.component')
						.boundingClientRect(data => {
							console.log(data);
							// 添加安全检查，防止微信小程序中元素未渲染完成时访问 undefined
							if (data && data.length > 0 && data[this.actTab] && data[this.actTab].height) {
								this.swiperHeight = data[this.actTab].height;
							}
						})
						.exec();
				}, 1000);
			},
			onReach() {
				const status =
					this.actTab === 1 ?
					this.idleStatus :
					this.notesList[this.getNotesListIndex(this.actTab)].status;
				console.log(`触底检测: actTab=${this.actTab}, noteType=${this.noteType}, status=${status}`);

				// 如果当前状态是loading或nomore，直接返回，避免重复请求
				if (status === 'loading' || status === 'nomore') {
					console.log(`状态为${status}，跳过加载`);
					return;
				}

				// 添加防抖处理，避免快速连续触发
				if (this._reachTimer) {
					clearTimeout(this._reachTimer);
				}
				this._reachTimer = setTimeout(() => {
					this.executeReach();
				}, 200);
			},

			executeReach() {
				if (this.actTab == 0) {
					if (this.noteType == 2) {
						console.log('加载草稿笔记');
						this.getMoreDraftNotes();
					} else {
						console.log('加载公开/私密笔记');
						this.getMoreNotes(0, this.noteType, false); // 修复：添加isInitialLoad参数
					}
				} else if (this.actTab == 2) {
					console.log('加载收藏笔记');
					this.getMoreNotes(1, 0, false);
				} else if (this.actTab == 3) {
					console.log('加载赞过笔记');
					this.getMoreNotes(2, 0, false);
				} else if (this.actTab == 1) {
					console.log('加载闲宝');
					this.loadIdleProducts(false);
				}
			},
			getNotesCount() {
				// 调整为新接口：/note/noteCount?userId=xxx，code=200
				const uid = uni.getStorageSync('userInfo')?.id;
				getNotesCountByUserId({
						userId: uid
					})
					.then(res => {
						if (res.code === 200) {
							// 映射后端返回到页面展示需要的字段
							this.count = {
								notesCount: Number(res.data.allCount) || 0,
								praiseCount: Number(res.data.likeCount) || 0,
								collectCount: 0, // 新接口未返回收藏总数，先置0或后续后端补充再对接
								rejectedCount: Number(res.data.rejectedCount) || 0,
								pendingCount: Number(res.data.pendingCount) || 0,
								approvedCount: Number(res.data.approvedCount) || 0
							};
						}
					})
					.catch(err => {
						console.log(err);
					});
			},
			async loadIdleProducts(reset = false) {
				if (reset) {
					// 重置为可加载状态，避免被早退条件拦截
					this.idleStatus = 'loadmore';
					this.idlePage = 1;
					this.idleList = [];
					// 🔧 修复：清空瀑布流组件，但不触发 watch（因为 idleList 已经为空）
					this.$nextTick(() => {
						if (this.$refs.tabsContainer?.$refs?.idleWaterfall) {
							this.$refs.tabsContainer.$refs.idleWaterfall.clear();
						}
					});
					this.idleTotal = 0;
				}
				if (this.idleStatus === 'nomore' || this.idleStatus === 'loading') return;
				this.idleStatus = 'loading';

				// 🔧 标记闲宝标签页已加载（标签顺序 1=闲宝）
				this.tabLoadedStates[1] = true;
				try {
					console.log('loadIdleProducts call', {
						page: this.idlePage,
						pageSize: this.idlePageSize
					});
					const res = await getUserIdleProducts({
						currentPage: this.idlePage,
						pageSize: this.idlePageSize,
						userId: this.userInfo.id || uni.getStorageSync('userInfo')?.id,
						type: 4,
						status: 'publish',
						_t: Date.now() // 🔧 修复：添加时间戳，防止HTTP缓存
					});
					if (res && res.code === 200) {
						const pageInfo = res.data || {};
						const records = pageInfo.records || [];
						// 🔧 调试：打印后端返回的分页信息
						console.log(
							`闲置标签后端返回: current=${pageInfo.current}, total=${pageInfo.total}, records.length=${records.length}, pageSize=${this.idlePageSize}`
						);
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
									item.urls : item.urls ?
									JSON.parse(item.urls) : [],
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

						// 🔧 修复：第一页时，直接设置新数据，不清空（避免闪烁）
						if (isFirstPage) {
							// 第一页：直接设置新数据，让瀑布流组件的 watch 自动处理
							this.idleList = mapped;
							console.log(`闲置标签第一页数据设置完成: ${mapped.length}条`);
						} else {
							// 后续页：去重后添加新数据
							const existingIds = new Set(this.idleList.map(item => item.id));
							const newItems = mapped.filter(item => !existingIds.has(item.id));

							if (newItems.length > 0) {
								// 🔧 修复：只更新 idleList，让组件的 watch 自动处理 addList
								// 因为 idle-water-fall 组件的 watch 会自动检测数据追加并调用 addList
								// 如果手动调用 addList，会导致重复调用（watch 一次 + 手动一次）
								this.idleList = this.idleList.concat(newItems);
								console.log(
									`闲置标签后续页去重后新增数据: ${newItems.length}条，跳过重复: ${
                  mapped.length - newItems.length
                }条`
								);
								console.log(
									`闲置标签后续页数据已通过 :list prop 自动更新到瀑布流组件（由watch自动处理）`
								);
							} else {
								console.log(`闲置标签后续页无新数据，跳过添加 [page=${this.idlePage}]`);
								// 🔧 修复：即使没有新数据，也要更新页码，避免重复请求同一页
								console.warn(
									`⚠️ 闲置标签后续页无新数据，可能是数据重复或页码错误，当前page=${this.idlePage}, 请求的currentPage=${pageInfo.current}`
								);
							}
						}

						// 🔧 修复：更新页码和总数
						// 页码应该在数据加载成功后立即更新，确保下次请求使用正确的页码
						// 使用请求时的页码+1作为下次页码（因为已经成功加载了当前页）
						const currentRequestPage = this.idlePage; // 保存请求时的页码
						this.idlePage = currentRequestPage + 1; // 下次请求的页码
						this.idleTotal = pageInfo.total || this.idleTotal;
						console.log(
							`闲置标签页码更新: 请求页=${currentRequestPage}, 下次页=${this.idlePage}, total=${this.idleTotal}, 当前数据=${this.idleList.length}条`
						);

						// 判断是否还有下一页
						// 🔧 修复：判断逻辑：当前数据量 < 总数 且 本次返回的数据量 = pageSize（说明可能还有下一页）
						const hasMore =
							this.idleList.length < this.idleTotal && mapped.length >= this.idlePageSize;
						this.idleStatus = hasMore ? 'loadmore' : 'nomore';
						console.log(
							`闲置标签状态更新: hasMore=${hasMore}, status=${this.idleStatus}, idleList.length=${this.idleList.length}, total=${this.idleTotal}, mapped.length=${mapped.length}, pageSize=${this.idlePageSize}`
						);
					} else {
						this.idleStatus = 'nomore';
					}
				} catch (e) {
					console.error('loadIdleProducts error', e);
					this.idleStatus = 'nomore';
				}
			},
			// 加载用户信息的方法
			loadUserInfo() {
				return getUserInfo({
						userId: uni.getStorageSync('userInfo')?.id || this.userInfo?.id
					})
					.then(res => {
						if (res.code === 200) {
							console.log(res.data);
							const data = res.data;
							// 字段映射
							const userInfo = {
								avatarUrl: data.avatar, // 头像
								nickname: data.username, // 昵称
								id: parseInt(data.id, 10) || 0, // 用户id（注意这里用id，和个人中心页一致）
								hsId: data.hsId, // 红薯id
								ipAddr: data.address, // IP属地（兼容旧字段）
								province: data.province || '', // 省份（确保不为undefined）
								city: data.city || '', // 城市
								district: data.district || '', // 区
								selfIntroduction: data.description, // 简介
								tags: data.tags ?
									(() => {
										try {
											const parsed = JSON.parse(data.tags);
											return Array.isArray(parsed) ?
												parsed :
												data.tags.includes(',') ?
												data.tags.split(',').map(t => t.trim()) : [data.tags];
										} catch (e) {
											return data.tags.includes(',') ?
												data.tags.split(',').map(t => t.trim()) : [data.tags];
										}
									})() : [], // 标签（支持JSON数组或逗号分隔字符串）
								age: data.age || null, // 年龄
								sex: data.gender !== undefined ? parseInt(data.gender, 10) : null, // 性别
								attentionNum: parseInt(data.followerCount) || 0, // 关注数
								fansNum: parseInt(data.fanCount) || 0, // 粉丝数
								homePageBackground: data.userCover, // 主页背景
								area: data.address // 地区
							};
							this.userInfo = userInfo;
							uni.setStorageSync('userInfo', userInfo); // 存储映射后的 userInfo
						} else {
							uni.showToast({
								icon: 'none',
								duration: 1000,
								title: res.msg == null ? '加载失败' : res.msg
							});
						}
					})
					.catch(err => {
						console.error('加载用户信息失败:', err);
					});
			},
			async onPullDownRefresh() {
				this.getNotesCount();
				this.loadUserInfo(); // 使用统一的方法加载用户信息
				// 🔧 下拉刷新时，重置标签页加载状态，强制重新加载
				let index = this.actTab;
				if (index === 1) {
					// 🔧 闲宝标签页下拉刷新
					this.idleStatus = 'loadmore';
					this.idlePage = 1;
					// 🔧 修复：不要立即清空数据，避免空白
					// this.idleList = [] // 注释掉，让loadIdleProducts中的reset参数处理
					// 重置标签页加载状态，确保下拉刷新时能重新加载
					this.tabLoadedStates[index] = false;
					if (this.$refs.tabsContainer?.$refs?.idleWaterfall) {
						this.$refs.tabsContainer.$refs.idleWaterfall.clear();
					}
					// 🔧 修复：等待数据加载完成后再停止刷新
					try {
						await this.loadIdleProducts(true);
						console.log('✅ 闲置标签页下拉刷新数据加载完成');
					} catch (error) {
						console.error('❌ 闲置标签页下拉刷新数据加载失败:', error);
					} finally {
						uni.stopPullDownRefresh();
						console.log('✅ 闲置标签页下拉刷新完成');
					}
				} else {
					console.log('🔄 下拉刷新开始，当前标签页:', index);

					// 🔧 关键修复：下拉刷新时重置标签页加载状态，避免跳过数据加载
					this.tabLoadedStates[index] = false;
					// 🔧 清除缓存，确保下拉刷新时获取最新数据
					this.clearTabCache(index);
					console.log(`🗑️ 已清除标签页${index}的缓存`);

					// 🔧 重置加载锁，确保下拉刷新时能正常加载
					this.isRefreshing = false;
					this.globalLoadingLock = false;

					const notesIdx = this.getNotesListIndex(index);
					this.notesList[notesIdx].status = 'loading';
					this.notesList[notesIdx].page = 1;

					// 🔧 修复：不清空瀑布流组件，让新数据替换旧数据，避免空白
					// 瀑布流组件的 watch 会检测到数据变化并自动处理

					// 🔧 对于收藏(actTab2)、赞过(actTab3)标签页，authority=0
					let authority = this.noteType;
					if (index === 2) {
						authority = 0;
					} else if (index === 3) {
						authority = 0;
					}

					// 🔧 使用 isInitialLoad=false，跳过缓存检查，强制从服务器重新加载
					console.log(
						`🔄 开始调用getMoreNotes，index=${index}, authority=${authority}, isInitialLoad=false`
					);

					try {
						await this.getMoreNotes(notesIdx, authority, false);
						console.log('✅ 下拉刷新数据加载完成');
					} catch (error) {
						console.error('❌ 下拉刷新数据加载失败:', error);
					} finally {
						uni.stopPullDownRefresh();
						console.log('✅ 下拉刷新完成');
					}
				}
			},
			async onLoad() {
				// ✅ 修复：使用 getWindowInfo 获取正确的状态栏高度
				const windowInfo = uni.getWindowInfo();
				const statusBarHeight = windowInfo.statusBarHeight || 44; // 默认 44px（iPhone 标准）

				this.screenHeight = windowInfo.screenHeight / 2.5;
				this.statusBarHeight = statusBarHeight;
				const navBarH = Math.max(statusBarHeight * 1.2, 44);
				this.navigationBarHeight = navBarH + 'px';
				// 吸顶时标题栏紧贴「顶部用户状态栏」底部：MineHeader 固定条为 top:6px + height:50px，实际结束于 56px
				const navBarOffset = 6;
				const fixedNavBarH = 50;
				this.stickyTopBarHeight = navBarOffset + fixedNavBarH - 1;
				this.stickyHeight = this.stickyTopBarHeight + 50;
				// 修复高度计算，确保scroll-view有足够的高度
				// 顶栏 fixed 不占流；列表铺满到底，少减底部预留避免出现白边（tabbar 视觉约 50+10）
				const tabbarContentHeight = -10;
				const tabbarBottomOffset = 10;
				const bottomTotal = tabbarContentHeight + tabbarBottomOffset;
				this.notesHeight = windowInfo.screenHeight - bottomTotal + 'px';
				console.log(
					`屏幕高度: ${windowInfo.screenHeight}, 状态栏: ${statusBarHeight}, 吸顶高度: ${this.stickyHeight}, 计算高度: ${this.notesHeight}`
				);
				this.iconHeight = statusBarHeight * 0.55 + 'px';

				// 🔧 修复：完全移除onLoad中的数据加载，避免重复
				// 微信小程序onLoad被多次触发，导致数据重复加载
				// 解决方案：完全移除onLoad中的数据加载，只在用户主动操作时加载
				console.log('🔄 onLoad执行，但不加载数据，避免重复');

				// 只处理页面初始化，不加载数据
				// 数据加载完全由用户主动操作（下拉刷新、切换标签页）处理

				// 数据加载完成后隐藏骨架屏
				setTimeout(() => {
					this.isLoading = false;
				}, 500);
			},
			onReady() {
				let query = uni.createSelectorQuery().in(this);
				query
					.selectAll('.info')
					.boundingClientRect(data => {
						// 添加安全检查，防止微信小程序中元素未渲染完成时访问 undefined
						if (data && data.length > 0 && data[0] && data[0].height) {
							this.mainInfoHeight = data[0].height - this.stickyHeight - 5;
						}
					})
					.exec();
			},

			async onShow() {
				console.log('🔄 onShow执行');

				// 🔧 每次 onShow 都同步本地存储的最新用户信息（特别是背景图等可能在其他页面更新的字段）
				const cachedUserInfo = uni.getStorageSync('userInfo');
				if (cachedUserInfo) {
					// 处理标签格式：如果 tags 是 JSON 字符串，需要转换为数组
					if (cachedUserInfo.tags) {
						if (typeof cachedUserInfo.tags === 'string') {
							try {
								// 尝试解析 JSON 数组
								const parsed = JSON.parse(cachedUserInfo.tags);
								if (Array.isArray(parsed)) {
									cachedUserInfo.tags = parsed;
								} else {
									// 如果不是数组，可能是逗号分隔的字符串
									cachedUserInfo.tags = cachedUserInfo.tags.includes(',') ?
										cachedUserInfo.tags
										.split(',')
										.map(t => t.trim())
										.filter(t => t) : [cachedUserInfo.tags];
								}
							} catch (e) {
								// 解析失败，当作逗号分隔的字符串处理
								cachedUserInfo.tags = cachedUserInfo.tags.includes(',') ?
									cachedUserInfo.tags
									.split(',')
									.map(t => t.trim())
									.filter(t => t) : [cachedUserInfo.tags];
							}
						} else if (!Array.isArray(cachedUserInfo.tags)) {
							// 如果不是数组也不是字符串，转换为数组
							cachedUserInfo.tags = [];
						}
					} else {
						cachedUserInfo.tags = [];
					}
					// 更新用户信息，特别是背景图和标签
					this.userInfo = {
						...this.userInfo,
						...cachedUserInfo
					};
					// 强制更新视图以刷新背景图和标签
					this.$forceUpdate();
				}

				// 🔧 修复：微信小程序onShow重复触发问题
				// 使用防抖机制，避免短时间内重复调用
				if (this.onShowTimer) {
					clearTimeout(this.onShowTimer);
				}

				// 🔧 防重复执行：检查是否已经初始化过
				if (this.isInitialized) {
					console.log('🔄 页面已初始化，跳过onShow逻辑（但已同步用户信息）');
					return;
				}

				// 🔧 严格防重复：使用全局锁确保只执行一次
				if (this.onShowExecuting) {
					console.log('🔄 onShow正在执行中，跳过重复调用');
					return;
				}

				this.onShowExecuting = true;
				console.log('🔄 onShow内部逻辑执行');

				// 🔧 防重复：检查是否正在加载中
				if (this.isRefreshing) {
					console.log('🔄 正在加载中，跳过重复加载');
					this.onShowExecuting = false;
					return;
				}

				// 🔧 防重复：检查是否已经处理过onShow
				if (this.isOnShowHandled) {
					console.log('🔄 onShow已处理过，跳过重复处理');
					this.onShowExecuting = false;
					return;
				}

				this.isOnShowHandled = true;
				this.area = '';
				if (this.userInfo.area != null && this.userInfo.area != '') {
					let s = this.userInfo.area.split(' ');
					if (s.length === 2) {
						s.forEach((item, index) => {
							if (index == 0) {
								this.area += provinceToAbbr(item);
							} else {
								this.area += item.substring(0, item.length - 1);
							}
						});
					} else if (s.length === 3) {
						// 只显示省市
						s.forEach((item, index) => {
							if (index != 2) {
								if (index == 0) {
									this.area += provinceToAbbr(item);
								} else {
									this.area += item.substring(0, item.length - 1);
								}
							}
						});
					}
				}
				console.log(this.area);

				// 只在首次显示时加载数据
				if (!this.isDataLoaded) {
					console.log('🔄 首次显示，加载数据');
					this.isDataLoaded = true;

					// 🔧 修复：只加载当前显示的标签页数据，避免并行加载导致重复
					// 先加载用户信息和统计信息
					this.getNotesCount();
					this.loadUserInfo();

					if (this.actTab === 0) {
						this.getMoreNotes(0, this.noteType, true);
					} else if (this.actTab === 1) {
						this.loadIdleProducts(true);
					} else if (this.actTab === 2) {
						// 赞过标签页
						this.getMoreNotes(1, 0, true);
					} else if (this.actTab === 3) {
						// 收藏标签页
						this.getMoreNotes(2, 0, true);
					}

					// 🔧 确保锁被释放
					this.onShowExecuting = false;
					this.isInitialized = true; // 标记为已初始化
				} else {
					// 后续显示：检查当前标签页是否需要加载数据
					if (!this.tabLoadedStates[this.actTab]) {
						console.log(`🔄 标签页${this.actTab}未加载，加载数据`);

						if (this.actTab === 0) {
							this.getMoreNotes(0, this.noteType, true);
						} else if (this.actTab === 1) {
							this.loadIdleProducts(true);
						} else if (this.actTab === 2) {
							// 赞过
							this.getMoreNotes(1, 0, true);
						} else if (this.actTab === 3) {
							// 收藏
							this.getMoreNotes(2, 0, true);
						}
					} else {
						console.log(`🔄 标签页${this.actTab}已加载，跳过重复加载`);
					}

					// 🔧 确保锁被释放
					this.onShowExecuting = false;
					this.isInitialized = true; // 标记为已初始化
				}

				// 同步底部消息徽标
				try {
					const cached = uni.getStorageSync('im_total_unread') || 0;
					const tabbar = this.$refs.tabbar;
					if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(cached);
				} catch (e) {}
				uni.$off('im:setTabBadge');
				uni.$on('im:setTabBadge', count => {
					const tabbar = this.$refs.tabbar;
					if (tabbar && tabbar.setMessageBadge) tabbar.setMessageBadge(count);
				});
			},

			// 由 scroll-view 滚动驱动顶栏头像与透明度（H5/小程序内滚动不会触发 onPageScroll）
			onMineScroll(scrollTop) {
				scrollTop = Number(scrollTop) || 0;
				const threshold = this.rpxToPx(400);
				// 只有真正滑回顶部（scrollTop 很小）时才隐藏顶栏，避免“还在列表区”时顶栏就消失
				const hideWhenNearTopPx = 50;
				if (this.show) {
					if (scrollTop > hideWhenNearTopPx) {
						if (this._hideHeaderTimer) {
							clearTimeout(this._hideHeaderTimer);
							this._hideHeaderTimer = null;
						}
						this.show = true;
					} else {
						// 仅当持续在顶部附近一段时间后再隐藏，避免抖动
						if (!this._hideHeaderTimer) {
							this._hideHeaderTimer = setTimeout(() => {
								this.show = false;
								this._hideHeaderTimer = null;
							}, 120);
						}
					}
				} else {
					if (this._hideHeaderTimer) {
						clearTimeout(this._hideHeaderTimer);
						this._hideHeaderTimer = null;
					}
					this.show = scrollTop > threshold;
				}
				const avatarThreshold = threshold;
				if (scrollTop <= avatarThreshold) {
					this.opacity = 0;
				} else {
					const scrollAfterThreshold = scrollTop - avatarThreshold;
					const maxScroll = Number(this.screenHeight) - 80 - avatarThreshold;
					this.opacity = Math.min(scrollAfterThreshold / maxScroll, 1);
				}
			},
			onPageScroll(e) {
				// 页面级滚动（部分端会触发）：与 scroll-view 驱动逻辑一致
				this.onMineScroll(e.scrollTop);
			},
			onUnload() {
				// 清理定时器
				if (this.tabSwitchTimer) {
					clearTimeout(this.tabSwitchTimer);
				}
				if (this.typeSwitchTimer) {
					clearTimeout(this.typeSwitchTimer);
				}
				if (this._reachTimer) {
					clearTimeout(this._reachTimer);
				}
				if (this.onShowTimer) {
					clearTimeout(this.onShowTimer);
				}
				if (this._hideHeaderTimer) {
					clearTimeout(this._hideHeaderTimer);
					this._hideHeaderTimer = null;
				}

				// 🔧 新增：清理预加载定时器
				if (this.preloadTimer) {
					clearTimeout(this.preloadTimer);
				}

				// 🔧 清理实例状态
				this.isDataLoaded = false;
				this.isOnLoadExecuted = false;
				this.isOnShowHandled = false;
				this.onShowExecuting = false;
				this.isRefreshing = false;
				this.globalLoadingLock = false;
				this.tabLoadedStates = [false, false, false, false];
				console.log('🔄 页面卸载，清理实例状态');
			},

			onBackPress() {
				if (this.moreShow) {
					this.closeMore();
					return true;
				}
				if (this.introductionShow) {
					this.closeintroductionShow();
					return true;
				}
			},
			onReachBottom() {
				console.log('触底了');
				// 🔧 修复：调用触底检测方法
				this.onReach();
			},
			editIdleProduct(productId) {
				// 跳转到编辑闲置商品页面
				uni.navigateTo({
					url: `/pkg-publish/pages/publishIdle/publishIdle?productId=${productId}&mode=edit`
				});
			}
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
	}

	.filter {
		width: 100%;
		height: 120%;
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
		padding: 0 40rpx 30rpx 30rpx;
		/* ✅ 减少底部 padding */
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
	}

	.userinfo-main {
		display: inline-flex;
		margin-top: 30rpx;
		/* ✅ 从 50rpx 减少到 20rpx */
	}

	.userinfo-main image {
		border-radius: 50%;
		height: 120rpx;
		/* ✅ 从 160rpx 减少到 120rpx */
		width: 120rpx;
		/* ✅ 从 160rpx 减少到 120rpx */
		margin: 20rpx 30rpx 20rpx 0;
		/* ✅ 减少 margin */
	}

	.userinfo-main-right {
		margin: 25rpx 0rpx;
		/* ✅ 从 50rpx 减少到 25rpx */

		text:nth-child(1) {
			color: #ffffff;
			font-size: 35rpx;
			display: block;
		}

		text:nth-child(2) {
			margin-top: 10rpx;
			color: #95949a;
			font-size: 23rpx;
			display: block;
		}

		text:nth-child(3) {
			color: #95949a;
			font-size: 23rpx;
			display: block;
		}
	}

	.introduction {
		color: #ffffff;
		font-size: 26rpx;
		/* ✅ 从 30rpx 减少到 26rpx */
		margin-right: 40rpx;
		margin-top: 10rpx;
		/* ✅ 添加较小的上边距 */
		margin-bottom: 10rpx;
		/* ✅ 添加较小的下边距 */
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
		border: 3rpx solid #e5e6eb;
		line-height: 30px;
		padding: 0rpx 20rpx 0rpx 20rpx;
		justify-content: center;
		font-size: 25rpx;
		margin-left: 50rpx;
	}

	.tag1 .tag-icon,
	.tag2 .tag-icon {
		height: 22px;
		margin-top: 4px;
		margin-bottom: 4px;
	}

	.tag2 {
		display: flex;
		height: 30px;
		width: 30px;
		color: #ffffff;
		border-radius: 30rpx;
		border-color: #ffffff;
		border: 3rpx solid #e5e6eb;
		line-height: 30px;
		padding: 0rpx 20rpx 0rpx 20rpx;
		justify-content: center;
		font-size: 25rpx;
	}

	.guanzhu {
		margin-top: 10rpx;
		/* ✅ 从 20rpx 减少到 10rpx */
		margin-buttom: 10rpx;
		/* ✅ 从 20rpx 减少到 10rpx */
		margin-right: 25rpx;
		text-align: center;
		color: #ffffff;
		font-size: 24rpx;
	}

	.looked {
		width: 6em;
		margin-top: 20rpx;
		/* ✅ 从 40rpx 减少到 20rpx */
		padding: 8rpx 0rpx 8rpx 16rpx;
		/* ✅ 减少 padding */
		background-color: rgba(89, 88, 87, 0.6);
		border-radius: 20rpx;
	}

	::v-deep .u-sticky {
		border-top-left-radius: 20rpx;
		border-top-right-radius: 20rpx;
	}

	.notes {
		width: 750rpx;
		background-color: #ffffff;
		// overflow:auto;
	}

	/* ✅ 新增：简洁的顶部布局样式 */
	.compact-header {
		background-color: #fff;
		padding: 0 30rpx 20rpx;
	}

	.compact-nav {
		display: flex;
		justify-content: space-between;
		align-items: center;
		height: 88rpx;
	}

	.nav-btn {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.compact-user-info {
		display: flex;
		align-items: center;
		padding: 20rpx 0;
	}

	.compact-avatar {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		margin-right: 24rpx;
		flex-shrink: 0;
	}

	.user-info-right {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.nickname {
		font-size: 36rpx;
		font-weight: 600;
		color: #000;
		line-height: 1.2;
	}

	.stats-row {
		display: flex;
		align-items: center;
		gap: 24rpx;
	}

	.stat-item {
		display: flex;
		align-items: baseline;
		gap: 8rpx;
		cursor: pointer;
	}

	.stat-num {
		font-size: 28rpx;
		font-weight: 600;
		color: #000;
	}

	.stat-label {
		font-size: 24rpx;
		color: #666;
	}

	.stat-divider {
		width: 1rpx;
		height: 24rpx;
		background-color: #e0e0e0;
	}

	.edit-btn {
		flex-shrink: 0;
		background: #3d8af5;
		border-radius: 40rpx;
		padding: 14rpx 28rpx;
		margin-left: 16rpx;
	}

	.edit-text {
		font-size: 24rpx;
		color: #fff;
		white-space: nowrap;
	}

	/* 可滑动功能按钮区域样式 */
	.function-scroll {
		width: 100%;
		white-space: nowrap;
	}

	.function-container {
		display: flex;
		justify-content: flex-start;
		min-width: 95%;
		gap: 20rpx;
	}

	.function-container .looked {
		flex-shrink: 0;
		/* 保持原来的样式，不添加额外样式 */
	}

	/* 闲置商品瀑布流样式 */
	.water-left,
	.water-right {
		width: 48%;
		margin: 20rpx auto;
	}

	.note-card {
		background: #fff;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
		margin-bottom: 24rpx;
		margin-left: 8rpx;
		margin-right: 8rpx;
		overflow: hidden;
		position: relative;
		border: 1rpx solid #f0f0f0;
	}

	.title {
		font-size: 30rpx;
		padding: 10rpx;
		margin-bottom: -5px;
		color: #000000;
		overflow: hidden;
		text-overflow: ellipsis;
		word-break: break-all;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-height: 1.4em;
		max-height: 2.4em;
	}

	.note-username {
		margin-left: 10rpx;
		color: #16160e;
		font-size: 23rpx;
		line-height: 20px;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: calc(100% - 70px);
	}

	.look-views {
		display: flex;
		position: absolute;
		bottom: 205rpx;
		left: 8rpx;
		color: #ffffff;
		background-color: rgba(123, 124, 125, 0.6);
		padding: 3rpx 10rpx;
		border-radius: 50rpx;
		font-size: 22rpx;
	}

	.video-play {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		padding: 10rpx;
		border-radius: 50%;
	}

	.goods-price-row {
		display: flex;
		align-items: center;
		padding: 10rpx;
		gap: 10rpx;
	}

	.goods-price {
		color: #ff2442;
		font-size: 32rpx;
		font-weight: bold;
	}

	.goods-origin-price {
		color: #999;
		font-size: 24rpx;
		text-decoration: line-through;
	}

	.post-type-tag {
		background: #f0f0f0;
		color: #666;
		font-size: 20rpx;
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
		margin-left: auto;
	}

	/* 审核状态样式 */
	.image-container {
		position: relative;
		display: inline-block;
		width: 100%;
	}

	.audit-overlay {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 20rpx;
	}

	.audit-overlay.not-passed {
		background: rgba(0, 0, 0, 0.7);
	}

	.audit-eye {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		text-align: center;
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

	/* 侧边栏菜单卡片样式 */
	.menu-card {
		background-color: #ffffff;
		border-radius: 20rpx;
		margin: 0 30rpx 20rpx 30rpx;
		box-shadow: 0 2rpx 8rpx 0 rgba(0, 0, 0, 0.08);
		overflow: hidden;
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 20rpx 40rpx;
	}

	.menu-item:active {
		background-color: #f5f5f5;
	}

	.menu-text {
		margin-left: 20rpx;
		line-height: 60rpx;
		color: #333;
		font-size: 28rpx;
	}
</style>