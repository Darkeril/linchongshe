<template>
	<view @touchstart="$emit('touchstart', $event)" @touchend="$emit('touchend', $event)" style="height: 100%;">
		<!-- 话题胶囊区 (design: horizontal scroll pills) -->
		<scroll-view class="topic-pills-wrapper" scroll-x :show-scrollbar="false" enhanced :bounces="false">
			<view class="topic-pills-inner">
				<view
					v-for="(item, idx) in findList"
					:key="idx"
					class="topic-pill"
					:class="{ active: typeTabIndex === idx }"
					@click="handleChangeType({ index: idx })"
				>
					<text>{{ getCategoryEmoji(item.name) }} {{ item.name }}</text>
				</view>
			</view>
		</scroll-view>
		<!-- 二级分类swiper -->
		<swiper :current="typeTabIndex" @change="handleSwiperChange" @transition="handleSwiperTransition"
			@animationfinish="handleSwiperAnimationFinish" @touchstart="handleSwiperTouchStart"
			@touchend="handleSwiperTouchEnd" :style="{height: notesHeight - 40 + 'px'}" :duration="150"
			:disable-touch="!showCategoryTabs" :easing-function="easeOutCubic">
			<swiper-item v-for="(item, idx) in findList" :key="idx">
				<scroll-view scroll-y :style="{height: notesHeight - 40 + 'px'}" @scrolltolower="handleReach"
					@scroll="handleCategoryScroll" :refresher-enabled="enablerefresh" @refresherrefresh="handleRefresh"
					:refresher-triggered="refreshing" :refresher-threshold="100" :lower-threshold="200">
					<view>
						<!-- 使用 water-fall 组件 -->
						<water-fall 
							:ref="`waterfall_${idx}`"
							:list="(getCategoryData(idx) && getCategoryData(idx).list) ? getCategoryData(idx).list : []"
							:slot_bottom="true"
							:showViews="false"
							:type="1"
							:emitDetail="true"
							:showPinned="false"
							:showLocation="true"
							:enableExposureReport="true"
							:exposureScene="'recommend'"
							:context="{ categoryIndex: idx }"
							@goToDetail="handleWaterFallGoToDetail"
							@goToUser="$emit('goToUser', $event)"
							@needMore="handleNeedMore(idx)"
						></water-fall>

						<!-- 发现页面空状态 -->
						<view
							v-if="getCategoryData(idx).list && getCategoryData(idx).list.length === 0 && getCategoryData(idx).status === 'nomore'"
							class="empty-state">
							<view class="empty-icon">🔍</view>
							<view class="empty-text">暂无相关内容</view>
							<view class="empty-tip">换个分类试试吧</view>
						</view>

						<!-- <u-loadmore
							v-if="getCategoryData(idx).list && getCategoryData(idx).list.length > 0"
							margin-top="20" line :status="getCategoryData(idx).status" :loading-text="loadingText"
							:loadmore-text="loadmoreText" :nomore-text="nomoreText" /> -->
					</view>
				</scroll-view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
	import {
		getFindList,
		getLastNotesByPage,
		getNotesByCategoryId,
		getVideoNote,
		praiseOrCancelNotes
	} from '@/apis/notes_service.js'
	import {
		pxToRpx
	} from '@/utils/util.js'
	import {
		mapNoteItem
	} from '@/utils/noteUtils.js'
	import WaterFall from '@/components/water-fall/water-fall.vue'

	export default {
		name: 'DiscoverPage',
		components: {
			WaterFall
		},
		props: {
			notesHeight: Number,
			enablerefresh: Boolean
		},
		data() {
			return {
				// 分类列表
				findList: [],
				// 当前分类索引
				typeTabIndex: 0,
				// 分类标签显示
				showCategoryTabs: true,
				// 分类数据（每个分类独立的数据源）
				categoryData: {},
				// 分类缓存
				categoryCache: {},
				// 刷新状态
				refreshing: false,
				// 加载文本
				loadingText: '加载中...',
				loadmoreText: '加载更多',
				nomoreText: '到底了',
				// 缓动函数
				easeOutCubic: 'cubic-bezier(0.215, 0.61, 0.355, 1)',
				// 边界滑动检测相关
				lastTypeTabIndex: -1,
				swiperTransitionDx: 0,
				swiperMaxDx: 0,
				swiperTransitionStartTime: 0,
				boundaryCheckLocked: false,
				isRealTouch: false,
				isTouching: false,
				lastTouchStartX: 0,
				lastTouchStartY: 0,
				// 切换防抖定时器
				typeSwitchTimer: null,
				// 加载防抖定时器
				loadMoreTimer: null,
				// 最后一次加载时间
				lastLoadTime: 0
			}
		},
		mounted() {
			// 初始化：获取分类列表
			this.fetchFindList();
			// 加载推荐分类（索引0）
			this.handleTypeSwitch(0);
		},
		beforeDestroy() {
			// 清理定时器
			if (this.typeSwitchTimer) {
				clearTimeout(this.typeSwitchTimer);
			}
			if (this.loadMoreTimer) {
				clearTimeout(this.loadMoreTimer);
			}
		},
		methods: {
			// 获取分类列表
			fetchFindList() {
				getFindList().then(res => {
					if (res.code === 200) {
						let list = res.data.map(item => ({
							name: item.title,
							id: item.id,
							pid: item.pid,
						}));
						// 推荐始终在第一个
						list.unshift({
							name: '推荐',
							cid: '',
							cpid: '',
							type: 1
						});
						// 视频分类在第二个位置（推荐之后）
						list.splice(1, 0, {
							name: '视频',
							cid: '',
							cpid: '',
							type: 2,
							isVideo: true // 标识这是视频分类
						});
						this.findList = list;
					}
				});
			},
			// 获取分类数据（带默认值）
			getCategoryData(categoryIndex) {
				const categoryKey = `category_${categoryIndex}`;
				if (!this.categoryData[categoryKey]) {
					this.$set(this.categoryData, categoryKey, {
						list: [], // 改为单个 list，由 water-fall 组件内部处理左右列
						status: 'loadmore',
						page: 1,
						pageSize: categoryKey === 'category_0' ? 20 : 6, // 推荐分类使用20条，其他分类使用6条
						initialLoadSize: 3
					});
				}
				return this.categoryData[categoryKey];
			},
			// 处理点击进入详情页（从 water-fall 组件触发）
			handleWaterFallGoToDetail(note, context) {
				// 判断是否从视频分类进入，且笔记类型是视频
				const categoryIndex = context?.categoryIndex || 0;
				const isVideoCategory = categoryIndex === 1 && this.findList[categoryIndex] && this.findList[categoryIndex].isVideo && note.notesType === 2;
				this.$emit('goToDetail', note.id, note.notesType, isVideoCategory);
			},
			// 切换分类（点击分类标签）
			// 分类名 → emoji 映射
			getCategoryEmoji(name) {
				const map = {
					'推荐': '🔥',
					'视频': '🎬',
					'头像': '😺',
					'壁纸': '🖼️',
					'风景': '🌄',
					'动漫': '✨',
					'美食': '🥘',
					'艺术': '🎨',
					'影视': '🎬',
					'猫咪': '🐈',
					'狗狗': '🐕',
					'宠物医院': '🏥',
					'美容': '✂️',
				}
				return map[name] || '📌'
			},
			handleChangeType(e) {
				const newIndex = e.index;
				console.log('🔄 点击分类标签:', newIndex, this.findList[newIndex]?.name);

				// 避免重复切换
				if (this.typeTabIndex === newIndex) {
					console.log('⚠️ 分类相同，跳过切换');
					return;
				}

				// 更新分类索引
				this.typeTabIndex = newIndex;

				// 使用防抖避免快速切换
				if (this.typeSwitchTimer) {
					clearTimeout(this.typeSwitchTimer);
				}

				this.typeSwitchTimer = setTimeout(() => {
					this.handleTypeSwitch(newIndex);
				}, 50);

				// 通知主页面
				this.$emit('changeType', e);
			},
			// Swiper切换事件
			handleSwiperChange(e) {
				const current = e.detail.current;
				console.log('🔄 Swiper滑动切换:', current, '当前typeTabIndex:', this.typeTabIndex);

				// 避免重复处理
				if (this.typeTabIndex === current) {
					console.log('⚠️ 分类相同，跳过切换');
					return;
				}

				// 更新上一次的分类索引（在切换之前记录）
				this.lastTypeTabIndex = this.typeTabIndex;

				this.typeTabIndex = current;

				// 切换完成后解锁边界检测，允许新的滑动
				this.boundaryCheckLocked = false;
				this.isRealTouch = false;

				// 统一调用 handleTypeSwitch 处理切换逻辑
				this.handleTypeSwitch(current);

				// 通知主页面
				this.$emit('findSwiperChange', e);
			},
			// 处理分类切换逻辑
			async handleTypeSwitch(newIndex) {
				console.log('🔄 切换分类:', newIndex, this.findList[newIndex]?.name);

				const categoryKey = `category_${newIndex}`;

				// 🔧 修复：切换分类时清除缓存，确保获取最新数据
				if (this.categoryCache && this.categoryCache[categoryKey]) {
					delete this.categoryCache[categoryKey];
					console.log('🗑️ 切换分类：清除分类缓存', categoryKey);
				}
				// 同时清除该分类的所有分页缓存
				Object.keys(this.categoryCache || {}).forEach(key => {
					if (key.startsWith(`${categoryKey}_page_`)) {
						delete this.categoryCache[key];
						console.log('🗑️ 切换分类：清除分页缓存', key);
					}
				});

				// 检查是否已有分类数据（但不清除，允许快速切换）
				if (this.categoryData[categoryKey] && 
					this.categoryData[categoryKey].list?.length > 0) {
					console.log('✅ 使用已加载的分类数据');
					// 预加载下一个分类的数据
					this.preloadNextCategory(newIndex);
					return;
				}

				console.log('❌ 没有已加载数据，开始加载新分类数据');
				// 初始化分类数据（设置为loading状态）
				this.$set(this.categoryData, categoryKey, {
					list: [],
					status: 'loading',
					page: 1,
					pageSize: categoryKey === 'category_0' ? 20 : 6, // 推荐分类使用20条，其他分类使用6条
					initialLoadSize: 3
				});

				// 🔧 修复：等待数据加载完成，确保数据加载后再显示
				try {
					await this.loadCategoryData(newIndex);
					console.log('✅ 分类数据加载完成:', newIndex);
				} catch (error) {
					console.error('❌ 分类数据加载失败:', newIndex, error);
					// 加载失败时设置错误状态
					if (this.categoryData[categoryKey]) {
						this.$set(this.categoryData[categoryKey], 'status', 'nomore');
					}
				}

				// 预加载下一个分类的数据
				this.preloadNextCategory(newIndex);
			},
			// 加载分类数据
			async loadCategoryData(categoryIndex) {
				try {
					const categoryKey = `category_${categoryIndex}`;
					let categoryData = this.getCategoryData(categoryIndex);

					// 🔧 修复：不再使用缓存，每次都从服务器获取最新数据
					console.log('🌐 loadCategoryData: 发起API请求获取最新数据');

					let res;
					if (categoryIndex === 0) {
						// 推荐分类
						res = await getLastNotesByPage({
							page: 1,
							pageSize: categoryData.pageSize
						});
					} else if (categoryIndex === 1 && this.findList[categoryIndex]?.isVideo) {
						// 视频分类
						res = await getVideoNote({
							page: 1,
							pageSize: categoryData.pageSize
						});
					} else {
						// 其他分类
						const curCategory = this.findList[categoryIndex];
						if (!curCategory) return;

						res = await getNotesByCategoryId({
							page: 1,
							pageSize: categoryData.pageSize,
							cpid: curCategory.id,
							keyword: ''
						});
					}

					if (res.code === 200) {
						const records = res.data.records.map(item => mapNoteItem(item));
						const total = res.data.total || 0;
						
						// 🔧 修复：保存 total 值，用于判断是否还有更多数据
						if (total > 0) {
							const categoryData = this.getCategoryData(categoryIndex);
							this.$set(categoryData, 'total', total);
							console.log('💾 保存 total 值:', total, '分类:', categoryIndex);
						}
						
						await this.processCategoryData(categoryIndex, records);
					}
				} catch (error) {
					console.error('加载分类数据失败:', categoryIndex, error);
					// 设置错误状态
					const categoryKey = `category_${categoryIndex}`;
					if (this.categoryData[categoryKey]) {
						this.$set(this.categoryData[categoryKey], 'status', 'nomore');
					}
				}
			},
			// 处理分类数据（简化版：直接设置 list，由 water-fall 组件处理瀑布流）
			async processCategoryData(categoryIndex, records, isRefresh = false) {
				const categoryKey = `category_${categoryIndex}`;
				let categoryData = this.getCategoryData(categoryIndex);

				if (!records.length) {
					this.$set(categoryData, 'status', 'nomore');
					return;
				}

				// 🔧 调试：记录第一条和最后一条数据的ID，用于验证数据是否变化
				const firstRecordId = records[0]?.id || records[0]?.noteId || 'unknown';
				const lastRecordId = records[records.length - 1]?.id || records[records.length - 1]?.noteId || 'unknown';
				console.log('🔄 处理分类数据:', categoryIndex, '记录数:', records.length, '是否刷新:', isRefresh);
				console.log('📊 数据顺序检查 - 第一条ID:', firstRecordId, '最后一条ID:', lastRecordId);

				// 🔧 新增：如果是刷新或首次加载（list为空），对数据进行随机打乱，提供更好的用户体验
				// 注意：只在刷新或首次加载时打乱，追加数据时保持原有顺序，避免分页问题
				const isFirstLoad = !categoryData.list || categoryData.list.length === 0;
				if (isRefresh || isFirstLoad) {
					// 使用当前时间戳作为随机种子，确保每次刷新都有不同的顺序
					const shuffledRecords = [...records];
					for (let i = shuffledRecords.length - 1; i > 0; i--) {
						const j = Math.floor(Math.random() * (i + 1));
						[shuffledRecords[i], shuffledRecords[j]] = [shuffledRecords[j], shuffledRecords[i]];
					}
					records = shuffledRecords;
					console.log('🎲 发现页随机打乱数据顺序，提供更好的用户体验', { isRefresh, isFirstLoad });
				}

				// 设置加载状态
				this.$set(categoryData, 'status', 'loading');

				// 🔧 修复：如果是刷新，直接替换数据；如果是追加，合并现有数据和新数据
				// 🔧 修复：刷新时不再重复清空数据，因为 refreshFindNotes 已经清空了
				let newList;
				if (isRefresh) {
					// 刷新时，直接使用新数据，不合并
					// 注意：refreshFindNotes 已经清空了 list，这里直接设置新数据即可
					newList = [...records];
				} else {
					// 追加时，合并现有数据和新数据，并去重
					const existingList = categoryData.list || [];
					const existingIds = new Set(existingList.map(item => item.id || item.noteId || item.productId));
					// 过滤掉已存在的数据
					const uniqueRecords = records.filter(item => {
						const itemId = item.id || item.noteId || item.productId;
						return itemId && !existingIds.has(itemId);
					});
					newList = [...existingList, ...uniqueRecords];
					if (uniqueRecords.length < records.length) {
						console.log('🔍 追加数据时去重:', { originalCount: records.length, uniqueCount: uniqueRecords.length });
					}
				}
				
				// 🔧 修复：设置新数据（刷新时直接替换，追加时合并）
				// 注意：刷新时，refreshFindNotes 已经清空了 list，这里直接设置新数据
				// 使用 $set 确保响应式更新，并等待 DOM 更新完成
				this.$set(categoryData, 'list', newList);
				this.$set(categoryData, 'status', records.length < categoryData.pageSize ? 'nomore' : 'loadmore');
				
				// 🔧 修复：刷新时等待 DOM 更新，确保 water-fall 组件能够正确响应
				if (isRefresh) {
					await this.$nextTick();
				}
				
				// 🔧 修复：刷新时不应该增加page，因为已经在refreshFindNotes中重置为1了
				if (!isRefresh) {
					this.$set(categoryData, 'page', categoryData.page + 1);
				}

				console.log('✅ 分类数据加载完成:', categoryIndex, this.findList[categoryIndex]?.name, {
					listLength: newList.length,
					firstId: newList[0]?.id || newList[0]?.noteId || 'unknown',
					lastId: newList[newList.length - 1]?.id || newList[newList.length - 1]?.noteId || 'unknown'
				});
				
				// 🔧 调试：检查数据是否正确设置
				console.log('🔍 数据设置后检查:', {
					categoryKey,
					listLength: categoryData.list?.length,
					firstItem: categoryData.list?.[0],
					waterfallList: this.$refs[`waterfall_${categoryIndex}`]?.list?.length
				});

				// 🔧 修复：如果是刷新，确保water-fall组件正确响应数据变化
				if (isRefresh) {
					// 等待DOM更新完成，确保water-fall组件能够检测到数据变化
					await this.$nextTick();
					
					// 延迟检查内容高度，如果不足则自动加载更多
					setTimeout(() => {
						if (newList.length < 10 && categoryData.status !== 'nomore') {
							console.log('📏 刷新后数据较少，自动加载更多');
							this.handleNeedMore(categoryIndex);
						}
					}, 500);
				}

				// 延迟检查内容高度，如果不足则自动加载更多（首次加载也需要）
				setTimeout(() => {
					if (newList.length < 10 && categoryData.status !== 'nomore') {
						console.log('📏 首次加载数据较少，自动加载更多');
						this.handleNeedMore(categoryIndex);
					}
				}, 800);

				// 预加载下一页数据（如果还有更多数据）
				if (records.length >= categoryData.pageSize) {
					this.preloadNextPage(categoryIndex);
				}

				// 保存到缓存
				this.setCategoryCache(categoryKey, {
					list: newList
				});
			},
			// 刷新分类数据
			async refreshFindNotes() {
				console.log('🔄 开始下拉刷新，当前分类索引:', this.typeTabIndex);
				
				const categoryKey = `category_${this.typeTabIndex}`;
				const categoryData = this.getCategoryData(this.typeTabIndex);
				
				// 🔧 修复：防止重复刷新（如果正在加载中，跳过）
				if (categoryData.status === 'loading') {
					console.log('⚠️ 正在加载中，跳过刷新');
					return;
				}
				
				// 🔧 修复：下拉刷新时清除缓存，确保获取最新数据
				if (this.categoryCache && this.categoryCache[categoryKey]) {
					delete this.categoryCache[categoryKey];
					console.log('🗑️ 下拉刷新：清除分类缓存', categoryKey);
				}
				// 同时清除该分类的所有分页缓存
				Object.keys(this.categoryCache || {}).forEach(key => {
					if (key.startsWith(`${categoryKey}_page_`)) {
						delete this.categoryCache[key];
						console.log('🗑️ 下拉刷新：清除分页缓存', key);
					}
				});
				
				// 🔧 修复：清空现有数据，并等待DOM更新完成
				this.$set(categoryData, 'list', []);
				this.$set(categoryData, 'page', 1);
				this.$set(categoryData, 'status', 'loading');
				
				// 🔧 修复：等待DOM更新完成，确保water-fall组件能够正确响应清空操作
				await this.$nextTick();
				
				// 🔧 修复：刷新时先清空water-fall组件的数据，避免数据重复
				const refName = `waterfall_${this.typeTabIndex}`;
				const waterfallComponent = this.$refs[refName];
				if (waterfallComponent) {
					const component = Array.isArray(waterfallComponent) ? waterfallComponent[0] : waterfallComponent;
					if (component && typeof component.refresh === 'function') {
						console.log('🔄 调用water-fall组件的refresh方法');
						component.refresh();
					} else if (component && typeof component.clear === 'function') {
						console.log('🔄 调用water-fall组件的clear方法');
						component.clear();
					}
				}
				
				// 🔧 修复：再次等待，确保water-fall组件清空完成后再加载新数据
				await this.$nextTick();
				
				// 如果是推荐分类，使用推荐接口
				if (this.typeTabIndex === 0) {
					try {
						const res = await getLastNotesByPage({
							page: 1,
							pageSize: categoryData.pageSize
						});
						if (res.code == 200) {
						const total = res.data.total || 0;
						let records = res.data.records.map(item => mapNoteItem(item));
						
						// 🔧 修复：保存 total 值
						if (total > 0) {
							this.$set(categoryData, 'total', total);
							console.log('💾 刷新时保存 total 值:', total);
						}
						
						if (!records.length) {
							this.$set(categoryData, 'list', []);
							this.$set(categoryData, 'page', 1);
							this.$set(categoryData, 'status', 'nomore');
							return;
						}
						await this.processCategoryData(this.typeTabIndex, records, true); // 传入true表示刷新
						this.$set(categoryData, 'page', 2);
						// 🔧 修复：根据 total 值判断是否还有更多数据
						const hasMore = total > 0 ? (categoryData.list?.length || 0) < total : true;
						this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');
							
							// 保存到缓存
							this.setCategoryCache(categoryKey, {
								list: categoryData.list
							});
						} else {
							this.$set(categoryData, 'status', 'nomore');
						}
					} catch (err) {
						console.error('刷新推荐分类失败:', err);
						this.$set(categoryData, 'status', 'nomore');
						uni.showToast({
							title: '加载失败',
							icon: 'none',
							duration: 1000
						});
					}
					return;
				}

				// 如果是视频分类，使用视频接口
				if (this.typeTabIndex === 1 && this.findList[this.typeTabIndex]?.isVideo) {
					try {
						const res = await getVideoNote({
							page: 1,
							pageSize: categoryData.pageSize
						});
						if (res.code == 200) {
							const total = res.data.total || 0;
							let records = res.data.records.map(item => mapNoteItem(item));
							
							// 🔧 修复：保存 total 值
							if (total > 0) {
								this.$set(categoryData, 'total', total);
								console.log('💾 刷新视频分类时保存 total 值:', total);
							}
							
							if (!records.length) {
								this.$set(categoryData, 'list', []);
								this.$set(categoryData, 'page', 1);
								this.$set(categoryData, 'status', 'nomore');
								return;
							}
							await this.processCategoryData(this.typeTabIndex, records, true); // 传入true表示刷新
							this.$set(categoryData, 'page', 2);
							// 🔧 修复：根据 total 值判断是否还有更多数据
							const hasMore = total > 0 ? (categoryData.list?.length || 0) < total : true;
							this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');
						} else {
							this.$set(categoryData, 'status', 'nomore');
						}
					} catch (err) {
						this.$set(categoryData, 'status', 'nomore');
						uni.showToast({
							title: '加载失败',
							icon: 'none',
							duration: 1000
						});
					}
					return;
				}

				const curCategory = this.findList[this.typeTabIndex];
				try {
					const res = await getNotesByCategoryId({
						page: 1,
						pageSize: categoryData.pageSize,
						cpid: curCategory.id,
						keyword: ''
					});
					if (res.code == 200) {
						const total = res.data.total || 0;
						let records = res.data.records.map(item => mapNoteItem(item));
						// 🔧 调试：记录返回的数据ID，用于验证后端随机性
						const recordIds = records.map(r => r.id || r.noteId || 'unknown').join(',');
						console.log('📥 后端返回数据ID列表:', recordIds);
						
						// 🔧 修复：保存 total 值
						if (total > 0) {
							this.$set(categoryData, 'total', total);
							console.log('💾 刷新普通分类时保存 total 值:', total);
						}
						
						if (!records.length) {
						this.$set(categoryData, 'list', []);
						this.$set(categoryData, 'page', 1);
						this.$set(categoryData, 'status', 'nomore');
						return;
					}
					await this.processCategoryData(this.typeTabIndex, records, true); // 传入true表示刷新
					// 🔧 修复：刷新后设置page为2，为下次加载准备
					this.$set(categoryData, 'page', 2);
					// 🔧 修复：根据 total 值判断是否还有更多数据
					const hasMore = total > 0 ? (categoryData.list?.length || 0) < total : true;
					this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');

						// 保存到缓存
						this.setCategoryCache(categoryKey, {
							list: categoryData.list
						});
					} else {
						this.$set(categoryData, 'status', 'nomore');
					}
				} catch (err) {
					this.$set(categoryData, 'status', 'nomore');
					uni.showToast({
						title: '加载失败',
						icon: 'none',
						duration: 1000
					});
				}
			},
			// 加载更多分类数据
			async loadMoreCategoryData(categoryIndex) {
			try {
				const categoryKey = `category_${categoryIndex}`;
				const categoryData = this.getCategoryData(categoryIndex);

				if (!categoryData) {
					console.log(' 分类数据不存在:', categoryIndex);
					return;
				}

				if (categoryData.status === 'nomore') {
					console.log(' 已经没有更多数据了');
					return;
				}

				if (categoryData.status === 'loading') {
					console.log(' 正在加载中，请勿重复请求');
					return;
				}

				console.log(' 开始加载更多分类数据:', categoryIndex, '当前页码:', categoryData.page, '当前数据量:', categoryData.list?.length);
				this.$set(categoryData, 'status', 'loading');

					// 检查是否有预加载的缓存数据
					const cacheKey = `${categoryKey}_page_${categoryData.page}`;
					const cachedData = this.getCategoryCache(cacheKey);

					if (cachedData && cachedData.records) {
						console.log('✅ 使用预加载的缓存数据:', categoryIndex, '页码:', categoryData.page);
						const records = cachedData.records;
						const total = cachedData.total || categoryData.total || 0;
						const currentListLength = categoryData.list?.length || 0;

						// 🔧 修复：保存 total 值
						if (total > 0) {
							this.$set(categoryData, 'total', total);
						}

						// 追加到现有数据
						const addedCount = await this.appendCategoryData(categoryIndex, records);
						
						// 🔧 修复：根据 total 值和实际已加载数量判断是否还有更多数据
						const newListLength = categoryData.list?.length || 0;
						const hasMore = total > 0 ? newListLength < total : records.length >= categoryData.pageSize;

						// 更新页码和状态
						this.$set(categoryData, 'page', categoryData.page + 1);
						this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');
						
						console.log('🔍 缓存数据分页状态判断:', {
							total,
							currentListLength,
							newListLength,
							addedCount,
							recordsLength: records.length,
							hasMore,
							status: hasMore ? 'loadmore' : 'nomore'
						});

						// 预加载下一页数据（如果还有更多数据）
						if (hasMore) {
							this.preloadNextPage(categoryIndex);
						}

						// 清理已使用的缓存
						delete this.categoryCache[cacheKey];
						return;
					}

					let res;
					if (categoryIndex === 0) {
						// 推荐分类
						res = await getLastNotesByPage({
							page: categoryData.page,
							pageSize: categoryData.pageSize
						});
					} else if (categoryIndex === 1 && this.findList[categoryIndex]?.isVideo) {
						// 视频分类
						res = await getVideoNote({
							page: categoryData.page,
							pageSize: categoryData.pageSize
						});
					} else {
						// 其他分类
						const curCategory = this.findList[categoryIndex];
						if (!curCategory) return;

						res = await getNotesByCategoryId({
							page: categoryData.page,
							pageSize: categoryData.pageSize,
							cpid: curCategory.id,
							keyword: ''
						});
					}

					if (res.code === 200) {
						const records = res.data.records.map(item => mapNoteItem(item));
						const total = res.data.total || 0;
						const currentListLength = categoryData.list?.length || 0;
						
						console.log('✅ 加载更多数据成功:', records.length, '条', 'total:', total, '当前列表长度:', currentListLength);

						// 🔧 修复：保存 total 值，用于判断是否还有更多数据
						if (total > 0) {
							this.$set(categoryData, 'total', total);
						}

						// 追加到现有数据
						const addedCount = await this.appendCategoryData(categoryIndex, records);
						
						// 🔧 修复：根据 total 值和实际已加载数量判断是否还有更多数据
						const newListLength = categoryData.list?.length || 0;
						const hasMore = total > 0 ? newListLength < total : records.length >= categoryData.pageSize;
						
						// 更新页码和状态
						this.$set(categoryData, 'page', categoryData.page + 1);
						this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');
						
						console.log('🔍 分页状态判断:', {
							total,
							currentListLength,
							newListLength,
							addedCount,
							recordsLength: records.length,
							hasMore,
							status: hasMore ? 'loadmore' : 'nomore'
						});

						// 预加载下一页数据（如果还有更多数据）
						if (hasMore) {
							this.preloadNextPage(categoryIndex);
						}
					} else {
						console.error('❌ 加载更多数据失败:', res);
						// 加载失败时重置为 loadmore，允许用户重试
						this.$set(categoryData, 'status', 'loadmore');
					}
				} catch (error) {
					console.error('❌ 加载更多分类数据失败:', categoryIndex, error);
					const categoryKey = `category_${categoryIndex}`;
					if (this.categoryData[categoryKey]) {
						// 加载失败时重置为 loadmore，允许用户重试
						this.$set(this.categoryData[categoryKey], 'status', 'loadmore');
					}
				}
			},
			// 追加分类数据到现有列表（简化版：直接追加到 list，由 water-fall 组件处理）
			async appendCategoryData(categoryIndex, records) {
				const categoryKey = `category_${categoryIndex}`;
				let categoryData = this.getCategoryData(categoryIndex);

				if (!records.length) return 0;

				// 🔧 修复：统一ID获取方法，兼容不同的ID字段名
				const getItemId = (item) => {
					return item?.id || item?.noteId || item?.productId || null;
				};

				// 🔧 修复：基于现有列表做去重，确保不会添加重复数据
				const existingList = categoryData.list || [];
				const existingIds = new Set(existingList.map(item => getItemId(item)).filter(id => id !== null));
				
				// 过滤掉已存在的数据
				const uniqueRecords = records.filter(item => {
					const itemId = getItemId(item);
					return itemId && !existingIds.has(itemId);
				});
				
				if (uniqueRecords.length === 0) {
					console.log('🔍 追加数据时去重: 所有数据都已存在，跳过');
					return 0; // 返回0表示没有添加新数据
				}
				
				if (uniqueRecords.length < records.length) {
					console.log('🔍 追加数据时去重:', { 
						originalCount: records.length, 
						uniqueCount: uniqueRecords.length,
						filteredCount: records.length - uniqueRecords.length
					});
				}

				// 追加到现有 list（使用去重后的数据）
				const newList = [...existingList, ...uniqueRecords];
				
				// 🔧 修复：直接更新 list，让 water-fall 组件的 watch 自动处理数据追加
				// 这样 water-fall 组件会基于完整的 newList 来处理，而不是基于 leftList/rightList 去重
				this.$set(categoryData, 'list', newList);
				
				// 🔧 修复：不再手动调用 addList，而是让 watch 自动检测数据追加并处理
				// 这样可以确保数据同步，避免 leftList/rightList 和 categoryData.list 不一致
				
				console.log('✅ 数据追加完成:', {
					listLength: newList.length,
					addedCount: uniqueRecords.length,
					originalCount: records.length,
					existingCount: existingList.length
				});
				
				// 返回实际添加的数据量
				return uniqueRecords.length;
			},
			// 预加载下一页数据
			async preloadNextPage(categoryIndex) {
				try {
					const categoryKey = `category_${categoryIndex}`;
					const categoryData = this.getCategoryData(categoryIndex);

					if (!categoryData || categoryData.status === 'nomore') {
						return;
					}

					// 延迟预加载，避免影响当前页面的渲染
					setTimeout(async () => {
						try {
							console.log('🔄 预加载下一页数据:', categoryIndex, '页码:', categoryData.page);

							let res;
							if (categoryIndex === 0) {
								// 推荐分类
								res = await getLastNotesByPage({
									page: categoryData.page,
									pageSize: categoryData.pageSize
								});
							} else if (categoryIndex === 1 && this.findList[categoryIndex]?.isVideo) {
								// 视频分类
								res = await getVideoNote({
									page: categoryData.page,
									pageSize: categoryData.pageSize
								});
							} else {
								// 其他分类
								const curCategory = this.findList[categoryIndex];
								if (!curCategory) return;

								res = await getNotesByCategoryId({
									page: categoryData.page,
									pageSize: categoryData.pageSize,
									cpid: curCategory.id,
									keyword: ''
								});
							}

							if (res.code === 200 && res.data.records.length > 0) {
								const records = res.data.records.map(item => mapNoteItem(item));

								// 将预加载的数据添加到缓存中
								const cacheKey = `${categoryKey}_page_${categoryData.page}`;
								this.setCategoryCache(cacheKey, {
									records: records,
									page: categoryData.page,
									timestamp: Date.now()
								});

								console.log('✅ 预加载下一页完成:', categoryIndex, '页码:', categoryData.page, '数据量:',
									records.length);
							}
						} catch (error) {
							console.warn('⚠️ 预加载下一页失败:', error);
						}
					}, 2000); // 2秒后预加载

				} catch (error) {
					console.warn('⚠️ 预加载下一页异常:', error);
				}
			},
			// 预加载下一个分类的数据
			preloadNextCategory(currentIndex) {
				// 延迟预加载，避免影响当前分类的加载
				setTimeout(() => {
					const nextIndex = currentIndex + 1;
					if (nextIndex < this.findList.length) {
						const nextCacheKey = `category_${nextIndex}`;
						const nextCachedData = this.getCategoryCache(nextCacheKey);

						// 如果下一个分类没有缓存，预加载它
						if (!nextCachedData) {
							console.log('🔄 预加载下一个分类:', nextIndex, this.findList[nextIndex]?.name);
							this.preloadCategoryData(nextIndex);
						}
					}
				}, 1000); // 1秒后预加载
			},
			// 预加载分类数据
			async preloadCategoryData(categoryIndex) {
				try {
					const curCategory = this.findList[categoryIndex];
					if (!curCategory) return;

					let res;
					const categoryData = this.getCategoryData(categoryIndex);
					if (categoryIndex === 1 && curCategory.isVideo) {
						// 视频分类
						res = await getVideoNote({
							page: 1,
							pageSize: categoryData ? categoryData.pageSize : 6
						});
					} else {
						// 其他分类
						res = await getNotesByCategoryId({
							page: 1,
							pageSize: categoryData ? categoryData.pageSize : 6,
							cpid: curCategory.id,
							keyword: ''
						});
					}

					if (res.code === 200 && res.data.records.length > 0) {
						const records = res.data.records.map(item => mapNoteItem(item));
						await this.processCategoryData(categoryIndex, records);
						console.log('✅ 预加载分类完成:', categoryIndex, this.findList[categoryIndex]?.name);
					}
				} catch (error) {
					console.warn('预加载分类失败:', categoryIndex, error);
				}
			},
			// 分类数据缓存管理
			setCategoryCache(key, data) {
				if (!this.categoryCache) {
					this.categoryCache = {};
				}
				this.categoryCache[key] = {
					...data,
					timestamp: Date.now()
				};
				console.log('💾 保存到缓存:', key, {
					list: data.list?.length || 0,
					timestamp: new Date().toLocaleTimeString()
				});
			},
			getCategoryCache(key) {
				if (!this.categoryCache) {
					console.log('❌ 缓存对象不存在');
					return null;
				}
				const cached = this.categoryCache[key];
				if (!cached) {
					console.log('❌ 缓存未命中:', key);
					return null;
				}
				// 检查缓存是否过期（5分钟）
				const cacheAge = Date.now() - cached.timestamp;
				if (cacheAge > 5 * 60 * 1000) {
					console.log('❌ 缓存已过期:', key, '缓存时间:', cacheAge, 'ms');
					delete this.categoryCache[key];
					return null;
				}
				console.log('✅ 缓存命中:', key, '缓存时间:', new Date(cached.timestamp).toLocaleTimeString());
				return cached;
			},
			// 处理滚动到底部
			handleReach() {
				const now = Date.now();
				const timeSinceLastLoad = now - this.lastLoadTime;
				
				console.log('🔄 scrolltolower 触发（发现页）', {
					categoryIndex: this.typeTabIndex,
					timeSinceLastLoad: timeSinceLastLoad + 'ms',
					currentStatus: this.getCategoryData(this.typeTabIndex)?.status
				});
				
				// 防抖：500ms内只触发一次
				if (timeSinceLastLoad < 500) {
					console.log('⚠️ 触发太频繁，跳过本次加载');
					return;
				}
				
				this.lastLoadTime = now;
				this.loadMoreCategoryData(this.typeTabIndex);
				this.$emit('scrolltolower');
			},
			// 处理瀑布流内容高度不足，需要加载更多
			handleNeedMore(categoryIndex) {
				console.log('📏 瀑布流内容高度不足，自动加载更多:', categoryIndex);
				
				// 只在当前分类时才加载
				if (categoryIndex === this.typeTabIndex) {
					const categoryData = this.getCategoryData(categoryIndex);
					
					// 检查状态，避免重复加载
					if (categoryData.status === 'loading' || categoryData.status === 'nomore') {
						console.log('⚠️ 当前状态不允许加载:', categoryData.status);
						return;
					}
					
					this.loadMoreCategoryData(categoryIndex);
				}
			},
			// 处理下拉刷新
			async handleRefresh() {
				console.log('🔄 handleRefresh 触发，当前refreshing状态:', this.refreshing);
				if (this.refreshing) {
					console.log('⚠️ 正在刷新中，跳过');
					return;
				}
				
				this.refreshing = true;
				console.log('✅ 设置refreshing为true，开始刷新');

				try {
					await this.refreshFindNotes();
					console.log('✅ 刷新完成');
				} catch (error) {
					console.error('❌ 刷新失败:', error);
					uni.showToast({
						title: '刷新失败',
						icon: 'none',
						duration: 1000
					});
				} finally {
					// 延迟一下再重置刷新状态，确保UI更新完成
					setTimeout(() => {
						this.refreshing = false;
						console.log('✅ 重置refreshing为false');
					}, 300);
				}
				
				this.$emit('refresh');
			},
			// 处理分类滚动
			handleCategoryScroll(e) {
				// 固定分类栏：禁用滚动隐藏功能
				// 通知主页面
				this.$emit('categoryScroll', e);
			},
			// Swiper过渡事件
			handleSwiperTransition(e) {
				// 只在真实触摸时记录滑动，忽略程序触发和回弹
				if (!this.isRealTouch || this.boundaryCheckLocked) {
					return;
				}

				const dx = e.detail.dx;
				const dy = e.detail.dy || 0;
				this.swiperTransitionDx = dx;

				// 只有当水平滑动明显大于垂直滑动时，才认为是有效的水平滑动
				const absDx = Math.abs(dx);
				const absDy = Math.abs(dy);
				const isHorizontalSwipe = absDx > absDy * 2;

				// 只在明确的水平滑动时记录 maxDx
				if (isHorizontalSwipe && Math.abs(dx) > Math.abs(this.swiperMaxDx)) {
					this.swiperMaxDx = dx;
					console.log('📊 更新最大滑动距离:', dx, 'typeTabIndex:', this.typeTabIndex, '(水平滑动)');
				}

				// 通知主页面
				this.$emit('findSwiperTransition', e);
			},
			// Swiper触摸开始
			handleSwiperTouchStart(e) {
				// console.log('👆 二级分类 swiper touchstart');

				// 标记为真实触摸
				this.isRealTouch = true;
				this.isTouching = true;

				// 解锁边界检测（允许新的边界检测）
				this.boundaryCheckLocked = false;

				// 重置滑动状态
				this.swiperTransitionDx = 0;
				this.swiperMaxDx = 0;
				this.swiperTransitionStartTime = Date.now();

				// 记录触摸起始位置
				if (e.touches && e.touches.length > 0) {
					this.lastTouchStartX = e.touches[0].pageX;
					this.lastTouchStartY = e.touches[0].pageY;
				}

				// 通知主页面
				this.$emit('findSwiperTouchStart', e);
			},
			// Swiper触摸结束
			handleSwiperTouchEnd(e) {
				// console.log('👋 二级分类 swiper touchend');

				this.isTouching = false;

				// 立即检测边界滑动，不等动画结束
				if (this.isRealTouch && !this.boundaryCheckLocked) {
					// 立即锁定，防止后续的回弹事件重复触发
					this.boundaryCheckLocked = true;

					// 延迟很短的时间执行，确保 swiperMaxDx 已经记录完毕
					setTimeout(() => {
						this.checkBoundarySwipe();
						// 重置真实触摸标记
						this.isRealTouch = false;
					}, 50);
				} else {
					// 直接重置
					this.isRealTouch = false;
				}

				// 通知主页面
				this.$emit('findSwiperTouchEnd', e);
			},
			// 检测边界滑动
			checkBoundarySwipe() {
				// 安全检查1：必须是真实触摸
				if (!this.isRealTouch) {
					this.resetBoundaryState();
					return;
				}

				// 检测是否是边界滑动（用户尝试继续滑动但被边界阻挡）
				const threshold = 50; // 提高阈值，避免点击误触发

				// 安全检查2：swiperMaxDx必须有足够的值，防止点击和垂直滚动误触发
				const absSwiperMaxDx = Math.abs(this.swiperMaxDx);
				if (absSwiperMaxDx < threshold) {
					this.resetBoundaryState();
					return;
				}
				
				// 安全检查3：检查触摸时长，点击通常很短，滑动需要一定时长
				const touchDuration = Date.now() - this.swiperTransitionStartTime;
				if (touchDuration < 100) { // 少于100ms认为是点击，不是滑动
					this.resetBoundaryState();
					return;
				}

				// 简化逻辑：直接在边界滑动就切换，不需要两次确认
				// 在最左边（推荐）继续向左滑动 → 切换到关注页
				if (this.typeTabIndex === 0 && this.swiperMaxDx < -threshold) {
					this.$emit('switchToPrevMainTab');
				}
				// 在最右边继续向右滑动 → 切换到附近页
				else if (this.typeTabIndex === this.findList.length - 1 && this.swiperMaxDx > threshold) {
					this.$emit('switchToNextMainTab');
				}

				// 重置滑动状态
				this.resetBoundaryState();

				// 更新上一次的分类索引，记录当前位置
				this.lastTypeTabIndex = this.typeTabIndex;
			},
			// 重置边界检测状态
			resetBoundaryState() {
				this.swiperTransitionDx = 0;
				this.swiperMaxDx = 0;
				this.swiperTransitionStartTime = 0;
			},
			// Swiper动画结束
			handleSwiperAnimationFinish(e) {
				const current = e.detail.current;
				// console.log('✅ 二级分类swiper动画结束（animationfinish事件）:', {
				// 	current,
				// 	typeTabIndex: this.typeTabIndex,
				// 	swiperMaxDx: this.swiperMaxDx
				// });

				// 通知主页面
				this.$emit('findSwiperAnimationFinish', e);
			},
			// 点赞笔记
			handlePraiseNotes(id, targetUserId, type, index, categoryIndex) {
				praiseOrCancelNotes({
					notesId: id,
					userId: uni.getStorageSync('userInfo').id,
					targetUserId: targetUserId
				}).then(res => {
					console.log(res)
					if (res.code == 200) {
						const categoryData = this.getCategoryData(categoryIndex);
						if (type === 1) {
							// 左列
							if (categoryData.leftList && categoryData.leftList[index]) {
								const item = categoryData.leftList[index];
								if (item.isLike) {
									item.notesLikeNum = Math.max(0, item.notesLikeNum - 1);
									item.isLike = false;
								} else {
									item.notesLikeNum = (item.notesLikeNum || 0) + 1;
									item.isLike = true;
								}
								console.log('✅ 笔记列表左列点赞状态已更新:', item);
							}
						} else {
							// 右列
							if (categoryData.rightList && categoryData.rightList[index]) {
								const item = categoryData.rightList[index];
								if (item.isLike) {
									item.notesLikeNum = Math.max(0, item.notesLikeNum - 1);
									item.isLike = false;
								} else {
									item.notesLikeNum = (item.notesLikeNum || 0) + 1;
									item.isLike = true;
								}
								console.log('✅ 笔记列表右列点赞状态已更新:', item);
							}
						}
					}
				})
			},
			// 格式化地址显示方法（优先显示市+详细地址，如果没有详细地址则显示省+市+区）
			formatAddress(item) {
				if (!item) return '';
				
				// 优先：市+详细地址
				if (item.city && item.address) {
					const address = item.city + item.address;
					return address.length > 12 ? address.substring(0, 12) + '...' : address;
				}
				
				// 备选1：省+市+区（如果没有详细地址）
				if (item.province || item.city || item.district) {
					const parts = [];
					if (item.province) parts.push(item.province);
					if (item.city) parts.push(item.city);
					if (item.district) parts.push(item.district);
					if (parts.length > 0) {
						const address = parts.join(' ');
						return address.length > 12 ? address.substring(0, 12) + '...' : address;
					}
				}
				
				// 备选2：只有城市
				if (item.city) {
					return item.city.length > 12 ? item.city.substring(0, 12) + '...' : item.city;
				}
				
				return '';
			}
		}
	};
</script>

<style lang="scss" scoped>
	/* ── 话题胶囊横滚区 (design: flex, gap 8, padding 0 16px 10px) ── */
	.topic-pills-wrapper {
		width: 100%;
		white-space: nowrap;
		padding: 0 0 20rpx;
		background: #F4EDE2;
	}

	/* 隐藏滚动条 */
	.topic-pills-wrapper ::-webkit-scrollbar {
		display: none;
	}

	.topic-pills-inner {
		display: inline-flex;
		gap: 16rpx;
		padding: 0 32rpx;
	}

	.topic-pill {
		display: inline-flex;
		align-items: center;
		padding: 12rpx 24rpx;
		border-radius: 28rpx;
		background: #FFFFFF;
		color: #63463A;
		font-size: 24rpx;
		font-weight: 500;
		white-space: nowrap;
		box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.03);
		flex-shrink: 0;
	}

	.topic-pill.active {
		background: #C97B4A;
		color: #FFFFFF;
		box-shadow: 0 8rpx 20rpx rgba(201, 123, 74, 0.27);
	}

	/* ── 空状态 ── */
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