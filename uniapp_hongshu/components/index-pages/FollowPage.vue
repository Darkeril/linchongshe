<template>
	<scroll-view scroll-y :style="{height: notesHeight + 'px'}" @scrolltolower="handleReach"
		:refresher-enabled="enablerefresh" @refresherrefresh="handleRefresh" :refresher-triggered="refreshing"
		:refresher-threshold="100">
		<view :id="'component' + 0">
			<!-- 关注用户头像列表 -->
			<view v-if="followUsersList.length > 0" class="follow-users-section">
				<scroll-view scroll-x="true" class="follow-users-scroll" show-scrollbar="false"
					:scroll-left="scrollLeft" :scroll-into-view="scrollIntoView">
					<view class="follow-users-container">
						<view v-for="(user, index) in followUsersList" :key="index" :id="'user-avatar-' + user.userId"
							class="follow-user-item" :class="{ 'active': selectedUserId === user.userId }"
							@click="goToFrequentUsers(index)" @tap="goToFrequentUsers(index)">
							<view class="user-avatar-container">
								<image class="user-avatar" :src="user.avatarUrl || user.avatar" mode="aspectFill"
									:lazy-load="true" :fade-show="true" :webp="true" :show-menu-by-longpress="false">
								</image>
								<!-- 红点提示 -->
								<view v-if="user.hasNewContent" class="notification-dot"></view>
								<!-- 选中状态标识 -->
								<view v-if="selectedUserId === user.userId" class="selection-indicator">
									<u-icon name="checkmark" size="16" color="#fff"></u-icon>
								</view>
							</view>
							<text class="user-nickname">{{ user.nickname || user.userName || '用户' }}</text>
						</view>
					</view>
				</scroll-view>
			</view>
			<view v-for="(item,index) in notesList" :key="index" class="follow-card">
				<view class="follow-user">
					<image class="avatar" :src="item.avatarUrl" :lazy-load="true" :fade-show="true" :webp="true"
						:show-menu-by-longpress="false" @click="$emit('goToUser', item.belongUserId)"></image>
					<view class="user-info-wrapper">
						<text class="nickname" @click="$emit('goToUser', item.belongUserId)">{{ item.nickname }}</text>
						<text class="follow-time" v-if="item.createTime">{{ item.createTime }}</text>
					</view>
					<u-icon name="more-dot-fill" size="22" class="more-btn"></u-icon>
				</view>
				<!-- 内容区域：支持展开/收起 -->
				<view class="follow-content" v-if="item.content" @click.stop>
					<text :class="{'content-collapsed': !item.expanded && item.content.length > 100}">
						<text
							v-for="(part, partIndex) in parseContentWithTags(item.expanded || item.content.length <= 100 ? item.content : item.content.substring(0, 100) + '...')"
							:key="partIndex" :class="part.isTag ? 'hashtag' : ''">
							{{ part.text }}
						</text>
					</text>
					<text v-if="item.content.length > 100" class="expand-btn" @click.stop="handleToggleExpand(index)">
						{{ item.expanded ? ' 收起' : ' 展开全文' }}
					</text>
				</view>
				<view @click="$emit('goToDetail', item.nid, item.notesType)">
					<!-- 视频内容 -->
					<view v-if="item.notesType == 2 && item.imgList && item.imgList.length > 0" class="follow-video">
						<video :src="item.imgList[0]" :poster="item.coverPicture || item.imgList[0]" :controls="false"
							:show-play-btn="true" :show-center-play-btn="true" :enable-progress-gesture="false"
							:autoplay="false" :loop="false" :muted="true" class="video-player"
							@click.stop="$emit('goToDetail', item.nid, item.notesType)"></video>
					</view>
					<!-- 单张图片 -->
					<view v-else-if="item.imgList.length === 1" class="follow-single-img">
						<image :src="item.imgList[0]" mode="aspectFill" :lazy-load="true" :fade-show="true" :webp="true"
							:show-menu-by-longpress="false" @load="onFollowCardMediaLoad(item)"></image>
					</view>
					<!-- 多张图片 -->
					<view v-else-if="item.imgList.length > 1" class="follow-multi-img">
						<image v-for="(img, i) in item.imgList" :key="i" :src="img" mode="aspectFill" :lazy-load="true"
							:fade-show="true" :webp="true" :show-menu-by-longpress="false" @load="onFollowCardMediaLoad(item)"></image>
					</view>
				</view>
				<view class="follow-bottom-bar">
					<view class="follow-location">
						<template v-if="formatFollowAddress(item)">
							<u-icon name="map" size="16"></u-icon>
							<text>{{ formatFollowAddress(item) }}</text>
						</template>
					</view>
					<view class="follow-action">
						<view class="action-item">
							<u-icon name="chat" size="22" @click="$emit('goToDetail', item.nid, item.notesType)"></u-icon>
							<view v-if="item.commentCount>0" class="comment-count">
								{{item.commentCount}}
							</view>
							<view v-else class="comment-count">
								{{'评论'}}
							</view>
						</view>
						<view class="action-item">
							<u-transition :show="!item.isLike" mode="fade" duration="200">
								<u-icon v-if="!item.isLike" name="thumb-up" size="22" color="#231710"
									@click="handlePraiseUserNotes(item.nid, item.belongUserId, index)"></u-icon>
							</u-transition>
							<u-transition :show="item.isLike" mode="fade" duration="200">
								<u-icon v-if="item.isLike" name="thumb-up-fill" size="22" color="#ff4d4f"
									@click="handlePraiseUserNotes(item.nid, item.belongUserId, index)"></u-icon>
							</u-transition>
							<view v-if="item.notesLikeNum>0" class="comment-count">
								{{item.notesLikeNum}}
							</view>
							<view v-else class="comment-count">
								{{'赞'}}
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 关注页面空状态 -->
			<view v-if="emptyState.isEmpty" class="empty-state">
				<view class="empty-icon">📝</view>
				<view class="empty-text">{{ emptyState.message }}</view>
				<view class="empty-tip">{{ emptyState.tip }}</view>
			</view>

			<u-loadmore v-if="notesList.length > 0" margin-top="20" line :status="status" :loading-text="loadingText"
				:loadmore-text="loadmoreText" :nomore-text="nomoreText" />
		</view>
	</scroll-view>
</template>

<script>
	import {
		getFollowUser
	} from '@/apis/user_service.js'
	import {
		getAttentionUserNotes,
		praiseOrCancelNotes
	} from '@/apis/notes_service.js'
	import {
		reportNoteBehaviorBatch
	} from '@/apis/note_behavior_service.js'
	import {
		weChatTimeFormat
	} from '@/utils/util.js'
	import {
		mapNoteItem
	} from '@/utils/noteUtils.js'

	export default {
		name: 'FollowPage',
		props: {
			notesHeight: Number,
			enablerefresh: Boolean
		},
		data() {
			return {
				// 关注用户列表
				followUsersList: [],
				// 当前选中的用户ID（用于筛选）
				selectedUserId: null,
				// 是否正在加载用户数据（防止重复请求）
				isLoadingUserData: false,
				// 用户列表滚动位置
				scrollLeft: 0,
				// 微信小程序滚动到指定视图（使用 scroll-into-view）
				scrollIntoView: '',
				// 笔记列表数据
				notesList: [],
				// 加载状态
				status: 'loadmore',
				page: 1,
				pageSize: 6,
				// #ifdef MP-WEIXIN
				initialLoadSize: 3, // 小程序优化：首次加载3条，极速秒开
				// #endif
				// #ifndef MP-WEIXIN
				initialLoadSize: 5, // H5首次加载5条
				// #endif
				// 刷新状态
				refreshing: false,
				// 加载文本
				loadingText: '加载中...',
				loadmoreText: '加载更多',
				nomoreText: '没有更多了',
				exposureReportedMap: {},
				exposurePendingQueue: [],
				exposureTimer: null
			}
		},
		computed: {
			// 关注页面空状态判断
			emptyState() {
				const hasNoNotes = this.notesList.length === 0 && this.status === 'nomore';
				const hasNoFollowedUsers = this.followUsersList.length === 0;

				// 检查是否有关注用户有内容
				const hasUsersWithContent = this.followUsersList.some(user => user.hasContent === true);

				return {
					isEmpty: hasNoNotes,
					hasNoFollowedUsers: hasNoFollowedUsers,
					hasUsersWithContent: hasUsersWithContent,
					message: hasNoFollowedUsers ? '还没有关注任何人' : '用户没有发布内容',
					tip: hasNoFollowedUsers ? '去发现页看看，关注感兴趣的人吧' : '期待他的新内容吧'
				};
			}
		},
		mounted() {
			// 初始化：获取关注用户列表和加载数据
			this.getFollowUsersList();
			this.refreshList();
			
			// 🔧 修复：监听关注状态变化事件，当取消关注后刷新用户列表
			uni.$on('updateAttentionList', this.handleAttentionListUpdate);
		},
		beforeDestroy() {
			// 🔧 修复：移除事件监听
			uni.$off('updateAttentionList', this.handleAttentionListUpdate);
			if (this.exposurePendingQueue.length) {
				const payload = this.exposurePendingQueue.slice();
				this.exposurePendingQueue = [];
				reportNoteBehaviorBatch(payload).catch(() => {});
			}
			if (this.exposureTimer) {
				clearTimeout(this.exposureTimer);
				this.exposureTimer = null;
			}
		},
		methods: {
			onFollowCardMediaLoad(item) {
				const noteId = item && (item.nid || item.id || item.noteId);
				if (!noteId) return;
				const key = String(noteId);
				if (this.exposureReportedMap[key]) return;
				this.$set(this.exposureReportedMap, key, true);
				this.exposurePendingQueue.push({
					eventType: 'exposure',
					nid: key,
					scene: 'follow'
				});
				if (this.exposureTimer) {
					clearTimeout(this.exposureTimer);
				}
				this.exposureTimer = setTimeout(() => {
					this.exposureTimer = null;
					if (!this.exposurePendingQueue.length) return;
					const payload = this.exposurePendingQueue.slice();
					this.exposurePendingQueue = [];
					reportNoteBehaviorBatch(payload).catch(() => {});
				}, 400);
			},
			// 获取关注用户列表
			async getFollowUsersList() {
				try {
					// 🔧 修复：添加时间戳参数，禁用缓存，确保下拉刷新时获取最新数据
					console.log('🔄 开始调用getFollowUser接口（禁用缓存）...');
					const res = await getFollowUser({
						_t: Date.now()
					});

					if (res.code === 200 && res.data) {
						console.log('✅ 关注用户API响应:', res);

						// 直接使用后端返回的去重数据，映射字段名
						this.followUsersList = res.data.map(user => ({
							userId: user.uid,
							nickname: user.username || '用户',
							avatarUrl: user.avatar,
							hasNewContent: user.tag === true, // 使用后端返回的tag字段控制红点显示
							hasContent: user.hasContent === true // 使用后端返回的hasContent字段判断是否有内容
						}));

						console.log('✅ 关注用户列表加载成功:', this.followUsersList.length);
						console.log('用户列表数据:', this.followUsersList);
					} else {
						console.log('⚠️ API响应异常:', res);
						this.followUsersList = [];
					}
				} catch (error) {
					console.error('❌ 获取关注用户列表失败:', error);
					this.followUsersList = [];
				}
			},
			// 滚动到指定用户头像位置
			scrollToUserAvatar(userId) {
				const userIndex = this.followUsersList.findIndex(user => user.userId === userId);
				if (userIndex === -1) {
					console.log('⚠️ 未找到用户，无法滚动');
					return;
				}

				// #ifdef MP-WEIXIN
				// 微信小程序：使用 scroll-into-view（更可靠）
				this.$nextTick(() => {
					this.scrollIntoView = 'user-avatar-' + userId;
					console.log('🔄 微信小程序滚动到用户头像:', userId);

					// 滚动完成后清除 scroll-into-view，以便下次可以再次触发
					setTimeout(() => {
						this.scrollIntoView = '';
					}, 300);
				});
				// #endif

				// #ifndef MP-WEIXIN
				// H5和其他平台：使用 scroll-left
				this.$nextTick(() => {
					const query = uni.createSelectorQuery().in(this);

					// 获取滚动容器和目标头像的位置信息
					query.select('.follow-users-scroll').scrollOffset();
					query.select('.follow-users-container').boundingClientRect();
					query.selectAll('.follow-user-item').boundingClientRect();

					query.exec((res) => {
						if (!res || res.length < 3) {
							console.log('⚠️ 无法获取元素位置信息，使用降级方案');
							// 降级方案：使用估算值
							this.fallbackScrollToUser(userIndex);
							return;
						}

						const scrollInfo = res[0];
						const containerInfo = res[1];
						const items = res[2];

						if (!items || !items[userIndex] || !containerInfo) {
							console.log('⚠️ 无法获取目标头像位置信息，使用降级方案');
							this.fallbackScrollToUser(userIndex);
							return;
						}

						const targetItem = items[userIndex];
						const currentScrollLeft = scrollInfo.scrollLeft || 0;

						// 获取屏幕宽度
						const systemInfo = uni.getSystemInfoSync();
						const screenWidth = systemInfo.windowWidth;

						// 计算目标头像相对于滚动容器的实际位置
						const containerLeft = containerInfo.left;
						const targetItemLeft = targetItem.left - containerLeft;

						// 计算可见区域（相对于容器）
						const visibleStart = currentScrollLeft;
						const visibleEnd = currentScrollLeft + screenWidth;

						// 计算目标头像在滚动容器中的绝对位置
						const containerPadding = 20; // 20rpx
						const rpxToPx = screenWidth / 750;
						const paddingPx = containerPadding * rpxToPx;

						// 计算每个头像在容器中的位置（累加前面的头像宽度）
						let targetScrollLeft = paddingPx;
						for (let i = 0; i < userIndex; i++) {
							if (items[i]) {
								targetScrollLeft += items[i].width + (30 * rpxToPx); // 30rpx 右边距
							}
						}

						// 计算需要滚动的位置
						let newScrollLeft = currentScrollLeft;

						// 如果目标头像不在可见区域内，需要滚动
						if (targetScrollLeft < visibleStart) {
							// 头像在屏幕左侧外部，滚动使其显示在左侧（留出一点边距）
							newScrollLeft = targetScrollLeft - 20; // 20px 左边距
						} else if (targetScrollLeft + targetItem.width > visibleEnd) {
							// 头像在屏幕右侧外部，滚动使其显示在右侧（留出一点边距）
							newScrollLeft = targetScrollLeft + targetItem.width - screenWidth +
								20; // 20px 右边距
						} else {
							// 已经在可见区域内，不需要滚动
							console.log('✅ 目标头像已在可见区域内，无需滚动');
							return;
						}

						// 确保滚动位置不为负
						newScrollLeft = Math.max(0, newScrollLeft);

						console.log('🔄 滚动到用户头像:', {
							userId,
							userIndex,
							targetScrollLeft,
							targetItemWidth: targetItem.width,
							currentScrollLeft,
							newScrollLeft,
							screenWidth,
							visibleStart,
							visibleEnd
						});

						// 执行滚动（使用技巧触发滚动）
						this.scrollLeft = newScrollLeft + 1;
						setTimeout(() => {
							this.scrollLeft = newScrollLeft;
						}, 10);
					});
				});
				// #endif
			},

			// 降级方案：使用估算值滚动
			fallbackScrollToUser(userIndex) {
				// 每个用户头像占据的空间：90rpx (头像宽度) + 30rpx (右边距) = 120rpx
				// 容器左边距：20rpx
				// 获取屏幕宽度，计算rpx到px的转换比例
				const systemInfo = uni.getSystemInfoSync();
				const screenWidth = systemInfo.windowWidth; // px
				const rpxToPx = screenWidth / 750; // 标准rpx转换比例

				const itemWidth = 120 * rpxToPx; // px
				const containerPadding = 20 * rpxToPx; // px
				const targetScrollLeft = userIndex * itemWidth + containerPadding;

				console.log('🔄 使用降级方案滚动:', {
					userIndex,
					targetScrollLeft,
					screenWidth,
					rpxToPx
				});

				this.scrollLeft = targetScrollLeft;
			},

			// 跳转到常访问的人页面
			goToFrequentUsers(index) {
				if (!this.followUsersList || this.followUsersList.length === 0) {
					return
				}
				// 将用户列表编码后传递
				const usersJson = encodeURIComponent(JSON.stringify(this.followUsersList))
				uni.navigateTo({
					url: `/pkg-main/pages/frequentUsers/frequentUsers?users=${usersJson}&index=${index}`
				})
			},
			
			// 根据用户筛选内容
			handleFilterByUser(userId) {
				console.log('🎯 点击用户头像:', userId);
				console.log('🎯 当前选中用户:', this.selectedUserId);
				console.log('🎯 当前关注页面数据长度:', this.notesList.length);

				// 防止重复点击
				if (this.isLoadingUserData) {
					console.log('⚠️ 正在加载用户数据，忽略点击');
					return;
				}

				// 如果点击的是当前选中的用户，则取消筛选
				if (this.selectedUserId === userId) {
					this.selectedUserId = null;
					console.log('✅ 取消用户筛选');
				} else {
					this.selectedUserId = userId;
					console.log('✅ 筛选用户:', userId);

					// #ifndef MP-WEIXIN
					// H5和其他平台：滚动到目标用户头像位置
					this.$nextTick(() => {
						this.scrollToUserAvatar(userId);
					});
					// #endif
					// #ifdef MP-WEIXIN
					// 微信小程序：保持头像列表原位置，不自动滚动
					// #endif
				}

				console.log('🎯 筛选后选中用户:', this.selectedUserId);

				// 通知主页面 selectedUserId 的变化
				this.$emit('selectedUserIdChange', this.selectedUserId);

				// 重置关注页面的状态，确保数据能正确加载
				this.resetFollowPageState();

				// 设置加载状态，防止重复请求
				this.isLoadingUserData = true;

				// 立即执行refreshList，不使用延迟
				console.log('🔄 立即执行refreshList，selectedUserId:', this.selectedUserId);
				this.refreshList().finally(() => {
					// 请求完成后重置加载状态
					this.isLoadingUserData = false;
					console.log('🔄 用户数据加载完成');
				});

				// 微信小程序端额外处理：强制更新视图
				this.$nextTick(() => {
					this.$forceUpdate();
					console.log('🔄 强制更新视图完成');
				});
			},

			// 重置关注页面状态
			resetFollowPageState() {
				console.log('🔄 重置关注页面状态');
				// 重置页码
				this.page = 1;
				// 重置加载状态
				this.status = 'loadmore';
				// 清空现有数据
				this.notesList = [];
			},

			// 刷新列表
			async refreshList() {
				console.log('🔄 refreshList 被调用:', {
					status: this.status,
					refreshing: this.refreshing,
					selectedUserId: this.selectedUserId
				});

				// 🔧 修复：如果正在加载中，直接返回，避免重复请求
				if (this.status === 'loading') {
					console.log('⚠️ refreshList 被阻止（正在加载中）');
					return;
				}

				// 只有在nomore状态下才阻止重新加载（但刷新时应该允许）
				if (this.status == 'nomore' && !this.refreshing) {
					console.log('⚠️ refreshList 被阻止（nomore状态且非刷新）');
					return;
				}

				// 保存当前请求的selectedUserId，防止异步请求覆盖
				const currentSelectedUserId = this.selectedUserId;
				console.log('🔄 保存当前selectedUserId:', currentSelectedUserId);

				this.status = 'loading';

				const params = {
					page: 1,
					pageSize: this.pageSize,
					// 使用保存的selectedUserId，防止异步请求覆盖
					...(currentSelectedUserId && {
						userId: currentSelectedUserId
					})
				};
				console.log('🔄 关注页面API参数:', params);
				console.log('🔄 使用的selectedUserId值:', currentSelectedUserId);

				try {
					const res = await getAttentionUserNotes(params);
					if (res.code == 200) {
						// 处理数据
						const notesData = res.data.notes || res.data.records || res.data;
						
						// 🔍 调试：查看原始数据的图片数量
						// console.log('🔍 原始数据:', notesData);
						// notesData.forEach((item, index) => {
						// 	console.log(`🔍 笔记${index + 1} 图片数量:`, {
						// 		imgUrls: item.imgUrls?.length || 0,
						// 		imgList: item.imgList?.length || 0,
						// 		urls: item.urls?.length || 0,
						// 		原始imgUrls: item.imgUrls,
						// 		原始imgList: item.imgList
						// 	});
						// });
						
						const records = notesData.map(item => mapNoteItem(item));
						
						// 🔍 调试：查看处理后的数据
						records.forEach((item, index) => {
							// console.log(`🔍 处理后笔记${index + 1} 图片数量:`, item.imgList?.length || 0, item.imgList);
						});

						// console.log('🔄 处理数据结果:', {
						// 	currentSelectedUserId: currentSelectedUserId,
						// 	recordsLength: records.length,
						// 	total: res.data.total || 0
						// });

						// 如果没有数据，清空列表并设置状态
						if (!records.length) {
							this.notesList = [];
							this.page = 1;
							this.status = 'nomore';
							console.log('📭 没有数据，设置空状态');
							return;
						}

						// 🔧 修复：刷新时直接替换数据，确保不会重复
						// 先清空现有数据，等待DOM更新后再设置新数据
						this.notesList = [];
						await this.$nextTick();
						
						// 设置新数据
						this.notesList = records;
						this.page = 2; // 下次加载从第2页开始
						this.status = records.length < this.pageSize ? 'nomore' : 'loadmore';

						console.log('✅ 刷新完成，数据量:', records.length);
					} else {
						this.status = 'nomore';
					}
				} catch (error) {
					console.error('刷新数据失败:', error);
					this.status = 'nomore';
				}
			},

			// 加载更多
			async getMoreNotes() {
				if (this.status === 'nomore' || this.status === 'loading') {
					console.log('⚠️ 加载更多被阻止:', {
						status: this.status,
						selectedUserId: this.selectedUserId
					});
					return;
				}

				console.log('🔄 开始加载更多数据:', {
					page: this.page,
					selectedUserId: this.selectedUserId
				});

				this.status = 'loading';

				const params = {
					page: this.page,
					pageSize: this.pageSize,
					...(this.selectedUserId && {
						userId: this.selectedUserId
					})
				};

				try {
					const res = await getAttentionUserNotes(params);
					if (res.code === 200) {
						// 处理数据
						const notesData = res.data.notes || res.data.records || res.data;
						const mappedRecords = notesData.map(item => mapNoteItem(item));

						// 追加到现有数据
						this.notesList = [...this.notesList, ...mappedRecords];

						// 更新页码和状态
						this.page += 1;
						this.status = mappedRecords.length < this.pageSize ? 'nomore' : 'loadmore';

						console.log('✅ 数据加载完成:', {
							page: this.page,
							totalItems: this.notesList.length,
							newItems: mappedRecords.length
						});
					} else {
						this.status = 'nomore';
					}
				} catch (error) {
					console.error('加载数据失败:', error);
					this.status = 'nomore';
				}
			},

			// 处理滚动到底部
			handleReach() {
				console.log('🔄 onReach 触发（关注页）');
				this.getMoreNotes();
			},

			// 处理下拉刷新
			async handleRefresh() {
				if (this.refreshing) return;
				this.refreshing = true;

				// 下拉刷新时清除用户筛选，恢复到初始状态
				console.log('🔄 下拉刷新关注页面，清除用户筛选');
				this.selectedUserId = null;

				// 通知主页面 selectedUserId 的变化
				this.$emit('selectedUserIdChange', null);

				// 重置用户列表滚动位置到最左边
				this.$nextTick(() => {
					// 强制触发滚动重置：先设置一个非0值，再设置为0
					this.scrollLeft = 1;
					setTimeout(() => {
						this.scrollLeft = 0;
						console.log('🔄 重置滚动位置到最左边');
					}, 50);
				});

				// 重置页面状态
				this.resetFollowPageState();
				
				// 同时刷新用户头像列表和内容列表
				await Promise.all([
					this.getFollowUsersList(),
					this.refreshList()
				]);
				
				this.refreshing = false;
			},

			// 🔧 修复：处理关注状态变化事件，刷新用户列表
			async handleAttentionListUpdate() {
				console.log('🔄 监听到关注状态变化，刷新关注用户列表');
				// 刷新关注用户列表，禁用缓存确保获取最新数据
				await this.getFollowUsersList();
			},

			// 切换内容展开/收起
			handleToggleExpand(itemIndex) {
				const item = this.notesList[itemIndex];
				if (item) {
					this.$set(item, 'expanded', !item.expanded);
				}
			},

			// 点赞笔记
			handlePraiseUserNotes(id, targetUserId, index) {
				praiseOrCancelNotes({
					notesId: id,
					userId: uni.getStorageSync('userInfo').id,
					targetUserId: targetUserId
				}).then(res => {
					console.log(res)
					if (res.code == 200) {
						const item = this.notesList[index];
						if (item) {
							if (item.isLike) {
								item.notesLikeNum = item.notesLikeNum - 1;
								item.isLike = false;
							} else {
								item.notesLikeNum = item.notesLikeNum + 1;
								item.isLike = true;
							}
						}
					}
				})
			},


			// 解析内容，将话题标签分离出来
			parseContentWithTags(content) {
				if (!content) return [{
					text: content,
					isTag: false
				}];

				// 首先去除HTML标签，只保留纯文本
				let cleanContent = content.replace(/<[^>]+>/g, '');

				// 解码HTML实体
				cleanContent = cleanContent
					.replace(/&lt;/g, '<')
					.replace(/&gt;/g, '>')
					.replace(/&amp;/g, '&')
					.replace(/&quot;/g, '"')
					.replace(/&#39;/g, "'")
					.replace(/&#x27;/g, "'")
					.replace(/&nbsp;/g, ' ')
					.replace(/&#x2F;/g, '/');

				const parts = [];
				const tagRegex = /#[\u4e00-\u9fa5a-zA-Z0-9]+/g;
				let lastIndex = 0;
				let match;

				while ((match = tagRegex.exec(cleanContent)) !== null) {
					if (match.index > lastIndex) {
						parts.push({
							text: cleanContent.substring(lastIndex, match.index),
							isTag: false
						});
					}
					parts.push({
						text: match[0],
						isTag: true
					});
					lastIndex = match.index + match[0].length;
				}

				if (lastIndex < cleanContent.length) {
					parts.push({
						text: cleanContent.substring(lastIndex),
						isTag: false
					});
				}

				return parts.length > 0 ? parts : [{
					text: cleanContent,
					isTag: false
				}];
			},

			// 格式化地址显示方法（只显示市+详细地址）
			formatFollowAddress(item) {
				if (!item) return '';
				const parts = [];
				if (item.city) parts.push(item.city);
				if (item.address) parts.push(item.address);
				// 兼容旧数据：如果没有新字段但有location字段
				if (parts.length === 0 && item.location) {
					const location = item.location;
					return location.length <= 12 ? location : location.substring(0, 12) + '...';
				}
				const fullAddress = parts.join(' ');
				return fullAddress.length > 12 ? fullAddress.substring(0, 12) + '...' : fullAddress;
			}
		}
	};
</script>

<style lang="scss" scoped>
	/* 关注用户头像列表样式 */
	.follow-users-section {
		background: #fff;
		padding: 5rpx 0;
		margin-bottom: 3rpx;
		box-shadow: 0 2rpx 2rpx 0 rgba(0, 0, 0, 0.04);
	}

	.follow-users-scroll {
		width: 100%;
		white-space: nowrap;
	}

	.follow-users-container {
		display: flex;
		padding: 0 20rpx;
	}

	.follow-user-item {
		margin-top: 10rpx;
		margin-bottom: 10rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-right: 30rpx;
		flex-shrink: 0;
		transition: all 0.3s ease;
	}

	/* 微信小程序兼容的选中状态样式 */
	.follow-user-item.active .user-avatar-container {
		position: relative;
	}

	.follow-user-item.active .user-avatar {
		border-color: #C97B4A;
		border-width: 3rpx;
	}

	.follow-user-item.active .user-nickname {
		color: #C97B4A;
		font-weight: 600;
	}

	.user-avatar-container {
		position: relative;
		// margin-bottom: 10rpx;
	}

	.user-avatar {
		width: 90rpx;
		height: 90rpx;
		border-radius: 50%;
		border: 2rpx solid rgba(80,50,30,0.1);
	}

	.notification-dot {
		position: absolute;
		top: 5rpx;
		right: 5rpx;
		width: 20rpx;
		height: 20rpx;
		background-color: #D66A5E;
		border-radius: 50%;
		border: 2rpx solid #fff;
	}

	.selection-indicator {
		position: absolute;
		bottom: 0;
		right: 0;
		width: 24rpx;
		height: 24rpx;
		background-color: #C97B4A;
		border-radius: 50%;
		border: 2rpx solid #fff;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.user-nickname {
		font-size: 20rpx;
		color: #231710;
		text-align: center;
		max-width: 80rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.follow-card {
		background: #fff;
		// border-radius: 20rpx;
		// margin: 24rpx 20rpx 0 20rpx;
		padding: 20rpx 20rpx 20rpx 20rpx;
		margin-bottom: 10rpx;
		box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.04);
		position: relative;
	}

	.follow-user {
		display: flex;
		align-items: center;
		margin-bottom: 12rpx;

		.avatar {
			width: 60rpx;
			height: 60rpx;
			border-radius: 50%;
			margin-right: 16rpx;
		}

		.user-info-wrapper {
			flex: 1;
			display: flex;
			flex-direction: column;
			margin-right: auto;
		}

		.nickname {
			font-size: 30rpx;
			color: #231710;
			font-weight: 500;
		}

		.follow-time {
			font-size: 22rpx;
			color: #8F7260;
			margin-top: 4rpx;
		}

		.more-btn {
			margin-left: auto;
			color: #C4AC95;
		}
	}

	.follow-content {
		font-size: 30rpx;
		color: #231710;
		margin-bottom: 12rpx;
		line-height: 1.5;
		word-break: break-all;

		.hashtag {
			color: #C97B4A;
			font-size: 30rpx;
			line-height: 1.5;
		}

		.content-collapsed {
			display: inline;
		}

		.expand-btn {
			color: #C97B4A;
			font-size: 24rpx;
			margin-left: 8rpx;
			cursor: pointer;
			display: inline;
		}
	}

	.follow-bottom-bar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 4rpx;

		.follow-location {
			display: flex;
			align-items: center;
			font-size: 22rpx;
			color: #8F7260;
			max-width: 200rpx; // 限制整体最大宽度，防止撑满
			background-color: #EADFCB;
			border-radius: 15px;
			padding: 5rpx 10rpx;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;

			u-icon {
				margin-right: 6rpx;
				flex-shrink: 0;
			}

			text {
				overflow: hidden;
				white-space: nowrap;
				text-overflow: ellipsis;
				display: inline-block;
				max-width: 90vw;
				vertical-align: middle;
			}
		}
	}

	.follow-action {
		display: flex;
		align-items: center;
		gap: 20rpx;
		font-size: 26rpx;
		color: #8F7260;
	}

	.action-item {
		display: flex;
		align-items: center;
		gap: 4rpx;

		u-icon {
			margin-right: 0;
		}
	}

	.follow-video {
		position: relative;
		width: 70%;
		height: 350rpx;
		border-radius: 16rpx;
		margin-bottom: 12rpx;
		overflow: hidden;
		background-color: #000;

		.video-player {
			width: 100%;
			height: 100%;
			object-fit: cover;
		}

		.video-play-overlay {
			position: absolute;
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
			z-index: 2;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 100%;
			height: 100%;
			background-color: rgba(0, 0, 0, 0.1);
		}
	}

	.follow-single-img image {
		width: 70%;
		height: 350rpx;
		border-radius: 16rpx;
		margin-bottom: 12rpx;
	}

	.follow-multi-img {
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx;
		margin-bottom: 12rpx;

		image {
			height: 32%;
			width: 32%;
			aspect-ratio: 1/1;
			border-radius: 10rpx;
		}
	}

	/* 空状态样式 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 40rpx;
		text-align: center;
	}

	.empty-icon {
		font-size: 120rpx;
		margin-bottom: 30rpx;
		opacity: 0.6;
	}

	.empty-text {
		font-size: 32rpx;
		color: #8F7260;
		margin-bottom: 20rpx;
		font-weight: 500;
	}

	.empty-tip {
		font-size: 24rpx;
		color: #8F7260;
		line-height: 1.5;
	}
</style>