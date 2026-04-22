<template>
	<scroll-view scroll-y :style="{height: notesHeight + 'px'}" @scrolltolower="handleScrolltolower"
		:refresher-enabled="enablerefresh" @refresherrefresh="handleRefresh" :refresher-triggered="refreshing"
		:refresher-threshold="100" :lower-threshold="50">
		<view :id="'component' + 2">
			<!-- 使用 water-fall 组件 -->
			<water-fall 
				ref="waterfall"
				:list="list"
				:slot_bottom="true"
				:showViews="false"
				:type="1"
				:showPinned="false"
				:showLocation="true"
				:emitDetail="true"
				:enableExposureReport="true"
				:exposureScene="'other'"
				@goToDetail="handleGoToDetail"
			></water-fall>

			<!-- 附近页面空状态 -->
			<view v-if="list.length === 0 && status === 'nomore'" class="empty-state">
				<view class="empty-icon">📍</view>
				<view class="empty-text">当前城市暂无内容</view>
				<view class="empty-tip">去发布属于你的专属内容吧</view>
			</view>

			<!-- <u-loadmore v-if="list.length > 0" margin-top="20" line :status="status"
				:loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText" /> -->
		</view>
	</scroll-view>
</template>

<script>
	import {
		searchNotesNearby
	} from '@/apis/search_service.js'
	import {
		praiseOrCancelNotes
	} from '@/apis/notes_service.js'
	import {
		pxToRpx,
		weChatTimeFormat
	} from '@/utils/util.js'
	import {
		preloadCriticalImages,
		getCompressedUrl
	} from '@/utils/imagePreloader.js'
	import {
		mapNoteItem
	} from '@/utils/noteUtils.js'
	import WaterFall from '@/components/water-fall/water-fall.vue'

	export default {
		name: 'NearbyPage',
		components: {
			WaterFall
		},
		props: {
			notesHeight: Number,
			enablerefresh: Boolean,
			loadingText: {
				type: String,
				default: '加载中...'
			},
			loadmoreText: {
				type: String,
				default: '加载更多'
			},
			nomoreText: {
				type: String,
				default: '到底了'
			}
		},
		data() {
			return {
				list: [], // 改为单个 list，由 water-fall 组件内部处理左右列
				status: 'loadmore',
				page: 1,
				pageSize: 6,
				initialLoadSize: 6, // 首次加载6条
				refreshing: false
			}
		},
		mounted() {
			// 组件挂载时，如果数据为空，则加载初始数据
			if (this.list.length === 0) {
				this.loadMoreNotes(true);
			}
		},
		methods: {
			// 处理跳转到详情页
			handleGoToDetail(item) {
				if (item && item.id) {
					this.$emit('goToDetail', item.id, item.notesType);
				}
			},

			// 加载更多数据
			async loadMoreNotes(isInitialLoad = false) {
				if (this.status === 'nomore' || this.status === 'loading') {
					console.log('⚠️ 加载更多被阻止:', {
						status: this.status,
						isInitialLoad
					});
					return;
				}

				console.log('🔄 附近页开始加载更多数据:', {
					page: this.page,
					isInitialLoad
				});

				this.status = 'loading';

				try {
					const params = {
						page: this.page,
						pageSize: this.pageSize
					};

					// 首次加载使用更大的pageSize
					if (isInitialLoad && this.page === 1) {
						params.pageSize = this.initialLoadSize;
					}

					const apiStartTime = Date.now();
					console.log('🌐 附近页API请求参数:', params);
					const res = await searchNotesNearby(params);
					const apiEndTime = Date.now();
					console.log('📡 附近页API响应耗时:', apiEndTime - apiStartTime, 'ms');
					console.log('📡 附近页API响应:', {
						code: res.code,
						recordsCount: res.data?.records?.length || 0,
						total: res.data?.total || 0
					});

					if (res.code === 200) {
						// 如果是首次加载，更新城市名
						if (res.data.cityName && this.page === 1) {
							const formattedCityName = this.formatCityName(res.data.cityName);
							if (formattedCityName) {
								// 通过事件向父组件传递城市名变化
								this.$emit('cityNameChange', formattedCityName);
								// 缓存城市名
								uni.setStorageSync('nearby_city', formattedCityName);
								console.log('🏙️ 附近页面首次加载时更新城市名:', formattedCityName);
							}
						}

						// 处理数据
						const dataProcessStartTime = Date.now();
						const notesData = res.data.notes || res.data.records || res.data;
						const mappedRecords = notesData.map(item => mapNoteItem(item));
						const mapEndTime = Date.now();
						console.log('🗺️ 附近页数据映射耗时:', mapEndTime - dataProcessStartTime, 'ms');

						// 使用 water-fall 组件的 addList 方法添加新数据（如果列表已有数据）
						if (this.list.length > 0 && this.$refs.waterfall && this.$refs.waterfall.addList) {
							// 列表已有数据，使用 addList 方法添加新数据
							await this.$refs.waterfall.addList(mappedRecords);
							// 同步更新 list，保持数据一致性
							this.list = [...this.list, ...mappedRecords];
						} else {
							// 首次加载或组件未初始化，直接添加到 list，让组件初始化
							this.list = [...this.list, ...mappedRecords];
						}

						// 预加载关键图片（异步执行，不阻塞）
						const preloadStartTime = Date.now();
						this.preloadImages(mappedRecords);
						const preloadEndTime = Date.now();
						console.log('🖼️ 附近页预加载触发耗时:', preloadEndTime - preloadStartTime, 'ms');

						// 更新页码和状态
						this.page += 1;

						console.log('✅ 附近页数据加载完成:', {
							page: this.page,
							newItems: mappedRecords.length,
							totalList: this.list.length
						});

						// 首次加载后，重置pageSize为正常值
						if (isInitialLoad && this.page === 2) {
							params.pageSize = this.pageSize;
						}

						this.status = res.data.records.length < (isInitialLoad ? this.initialLoadSize : this
							.pageSize) ?
							'nomore' : 'loadmore';
					} else {
						this.status = 'nomore';
						uni.showToast({
							title: res.msg || '加载失败',
							icon: 'none',
							duration: 1000
						});
					}
				} catch (error) {
					console.error('附近页加载数据失败:', error);
					this.status = 'nomore';
					uni.showToast({
						title: '加载失败，请重试',
						icon: 'none',
						duration: 1000
					});
				}
			},

			// 刷新列表
			async refreshList() {
				console.log('🔄 附近页refreshList 被调用:', {
					status: this.status,
					refreshing: this.refreshing
				});

				// 如果正在加载中，阻止重新加载
				if (this.status == 'loading') {
					console.log('⚠️ 附近页refreshList 被阻止（loading状态）');
					this.refreshing = false;
					return;
				}

				// ✅ 移除 nomore 状态的阻止逻辑，允许下拉刷新重新加载数据

				this.status = 'loading';
				this.refreshing = true;

				try {
					const res = await searchNotesNearby({
						page: 1,
						pageSize: this.pageSize
					});

					if (res.code == 200) {
						// 如果是首次刷新，更新城市名
						if (res.data.cityName) {
							const formattedCityName = this.formatCityName(res.data.cityName);
							if (formattedCityName) {
								// 通过事件向父组件传递城市名变化
								this.$emit('cityNameChange', formattedCityName);
								// 缓存城市名
								uni.setStorageSync('nearby_city', formattedCityName);
								console.log('🏙️ 刷新时更新附近页面城市名:', formattedCityName);
							}
						}

						// 处理数据
						const notesData = res.data.notes || res.data.records || res.data;
						const records = notesData.map(item => mapNoteItem(item));

						console.log('🔄 附近页处理数据结果:', {
							recordsLength: records.length,
							total: res.data.total || 0
						});

						// 如果没有数据，清空列表并设置状态
						if (!records.length) {
							this.list = [];
							this.page = 1;
							this.status = 'nomore';
							this.refreshing = false;
							console.log('📭 附近页没有数据，设置空状态');
							return;
						}

						// ✅ 修复：刷新时先清空列表，使用 nextTick 确保 DOM 更新后再设置新数据
						this.list = [];
						
						// 等待 DOM 更新，确保 water-fall 组件检测到列表清空
						await this.$nextTick();
						
						// 设置新数据，触发 water-fall 组件重新初始化
						this.list = records;

						// 预加载图片
						this.preloadImages(records);

						this.page = 2; // ✅ 修复：刷新时页码设为2，下次加载更多时从第2页开始
						this.status = records.length < this.pageSize ? 'nomore' : 'loadmore';

						console.log('🔄 附近页数据更新完成，新数据:', records.length, '条');
					} else {
						this.status = 'nomore';
					}
				} catch (err) {
					this.status = 'nomore';
					uni.showToast({
						title: '加载失败',
						icon: 'none',
						duration: 1000
					});
				} finally {
					this.refreshing = false;
				}
			},

			// 处理滚动到底部
			handleScrolltolower() {
				console.log('🔄 附近页滚动到底部，加载更多');
				this.loadMoreNotes();
			},

			// 处理下拉刷新
			async handleRefresh() {
				console.log('🔄 附近页下拉刷新，当前refreshing状态:', this.refreshing);
				if (this.refreshing) {
					console.log('⚠️ 正在刷新中，跳过');
					return;
				}
				await this.refreshList();
			},

			// 处理点赞（由 water-fall 组件内部处理，这里保留方法以防外部调用）
			handlePraiseNotes(id, targetUserId) {
				console.log('👍 附近页点赞操作:', {
					id,
					targetUserId
				});

				praiseOrCancelNotes({
					notesId: id,
					userId: uni.getStorageSync('userInfo').id,
					targetUserId: targetUserId
				}).then(res => {
					console.log('👍 附近页点赞响应:', res);
					if (res.code == 200) {
						// 根据 id 在 list 中查找并更新
						const item = this.list.find(item => item.id === id);
						if (item) {
							if (item.isLike) {
								item.notesLikeNum = Math.max(0, item.notesLikeNum - 1);
								item.isLike = false;
							} else {
								item.notesLikeNum = (item.notesLikeNum || 0) + 1;
								item.isLike = true;
							}
							console.log('✅ 附近页点赞状态已更新:', item);
						}
					}
				}).catch(error => {
					console.error('👍 附近页点赞失败:', error);
					uni.showToast({
						title: '点赞失败',
						icon: 'none'
					});
				});
			},

			// 规范城市名显示：去除"市/省/自治区/特别行政区"等后缀
			formatCityName(name) {
				if (!name) return ''
				const map = {
					'广西壮族自治区': '广西',
					'内蒙古自治区': '内蒙古',
					'新疆维吾尔自治区': '新疆',
					'宁夏回族自治区': '宁夏',
					'西藏自治区': '西藏',
					'香港特别行政区': '香港',
					'澳门特别行政区': '澳门',
				}
				if (map[name]) return map[name]
				// 一般性去后缀
				return name.replace(/(特别行政区|自治区|自治州|地区|盟|省|市)$/u, '')
			},

			// 预加载图片
			preloadImages(items) {
				if (!Array.isArray(items) || items.length === 0) return;

				console.log('🖼️ 附近页开始预加载图片:', items.length, '个项目');

				// 提取图片URL
				const imageUrls = [];
				items.forEach(item => {
					// 封面图片
					if (item.coverPicture) {
						imageUrls.push(getCompressedUrl(item.coverPicture, 'cover'));
					}
					// 头像
					if (item.avatarUrl) {
						imageUrls.push(getCompressedUrl(item.avatarUrl, 'avatar'));
					}
					// 图片列表
					if (item.imgList && Array.isArray(item.imgList)) {
						item.imgList.forEach(img => {
							if (img) {
								imageUrls.push(getCompressedUrl(img, 'content'));
							}
						});
					}
				});

				console.log('🖼️ 附近页提取到图片URL:', imageUrls.length, '个');

				// 预加载关键图片
				if (imageUrls.length > 0) {
					preloadCriticalImages(imageUrls);
				}
			}
		}
	};
</script>

<style lang="scss" scoped>
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
		color: #63463A;
		margin-bottom: 20rpx;
		font-weight: 500;
	}
	
	.empty-tip {
		font-size: 24rpx;
		color: #8F7260;
		line-height: 1.5;
	}
	
</style>