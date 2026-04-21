/**
 * 市集页面 - API调用 Mixin
 * 负责所有数据加载、刷新、加载更多等API相关方法
 * 
 * 注意：由于代码量很大，此文件包含所有API相关方法
 * 如需进一步拆分，可以考虑按功能模块再次拆分
 */
import {
	getLastIdleByPage,
	getIdleCategoryList,
	getIdlesByCategoryId,
	getVideoProduct,
} from '@/apis/idles_service.js'
import { getCarouselConfig } from '@/apis/config_service.js'
import { pxToRpx } from '@/utils/util.js'

export default {
	methods: {
		/**
		 * 获取分类列表
		 */
		fetchFindList() {
			getIdleCategoryList().then(res => {
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
					// 视频分类插入在推荐后面
					list.splice(1, 0, {
						name: '视频',
						id: 'video',
						isVideo: true
					});
					this.findList = list;
				}
			});
		},

		/**
		 * 切换分类类型
		 * @param {Object} e - 事件对象
		 */
		changeType(e) {
			const newIndex = e.index;
			if (this.typeTabIndex === newIndex) return; // 避免重复切换

			this.typeTabIndex = newIndex;

			// 优化：使用防抖避免快速切换
			if (this.typeSwitchTimer) {
				clearTimeout(this.typeSwitchTimer);
			}

			this.typeSwitchTimer = setTimeout(() => {
				this.handleTypeSwitch(newIndex);
			}, 50); // 减少防抖延迟到50ms
		},

		/**
		 * 处理分类切换逻辑
		 * @param {Number} newIndex - 新分类索引
		 */
		async handleTypeSwitch(newIndex) {
			console.log('🔄 切换分类:', newIndex, this.findList[newIndex]?.name);

			const categoryKey = `category_${newIndex}`;
			const requestId = Date.now();

			// 🔧 修复：先检查是否已有有效的分类数据（在清空之前检查）
			const existingData = this.categoryData[categoryKey];
			if (existingData) {
				// 检查是否有有效数据
				const currentList = existingData.list || [];
				const hasLeftList = existingData.leftList && existingData.leftList.length > 0;
				const hasRightList = existingData.rightList && existingData.rightList.length > 0;

				if (currentList.length > 0 || hasLeftList || hasRightList) {
					console.log('✅ 使用已加载的分类数据，跳过清空和重新加载', {
						listLength: currentList.length,
						leftListLength: existingData.leftList?.length || 0,
						rightListLength: existingData.rightList?.length || 0
					});
					// 更新请求ID，防止旧的异步请求覆盖数据
					this.$set(existingData, 'requestId', requestId);
					// 预加载下一个分类的数据
					this.preloadNextCategory(newIndex);
					return;
				}
			}

			// 🔧 修复：只在没有数据时才初始化分类数据
			// 如果分类数据不存在或为空，才设置为loading状态
			const categoryData = this.getCategoryData(newIndex);
			if (!categoryData.list || categoryData.list.length === 0) {
				this.$set(categoryData, 'status', 'loading');
				this.$set(categoryData, 'requestId', requestId);
			}

			// 加载分类数据
			await this.loadCategoryData(newIndex);
		},

		/**
		 * 处理swiper滑动改变
		 * @param {Object} e - 事件对象
		 */
		onFindSwiperChange(e) {
			const current = e.detail.current;
			console.log('🔄 Swiper滑动切换:', current, '当前typeTabIndex:', this.typeTabIndex);

			// 避免重复处理
			if (this.typeTabIndex === current) {
				console.log('⚠️ 分类相同，跳过切换');
				return;
			}

			// 🔧 修复：先更新 typeTabIndex，然后再处理数据切换
			// 这样可以确保 getCategoryData 能正确获取到对应的数据
			this.typeTabIndex = current;

			// 统一调用 handleTypeSwitch 处理切换逻辑
			this.handleTypeSwitch(current);
		},

		/**
		 * 缓存相关方法
		 */
		setCategoryCache(key, data) {
			if (!this.categoryCache) {
				this.categoryCache = {};
			}
			this.categoryCache[key] = {
				...data,
				timestamp: Date.now()
			};
		},

		getCategoryCache(key) {
			if (!this.categoryCache) return null;

			const cached = this.categoryCache[key];
			if (!cached) return null;

			// 检查缓存是否过期（10分钟）
			const isExpired = Date.now() - cached.timestamp > 10 * 60 * 1000;
			if (isExpired) {
				delete this.categoryCache[key];
				return null;
			}

			return cached;
		},

		/**
		 * 防抖函数
		 * @param {Function} fn - 要防抖的函数
		 * @param {Number} delay - 延迟时间（毫秒）
		 * @returns {Function} 防抖后的函数
		 */
		debounce(fn, delay = 200) {
			let timer = null;
			return function(...args) {
				if (timer) clearTimeout(timer);
				timer = setTimeout(() => {
					fn.apply(this, args);
				}, delay);
			}
		},

		/**
		 * 基础的 onReach 处理函数
		 * 🔧 修复：在微信小程序端增加更严格的状态检查，防止重复触发
		 */
		handleReach() {
			const categoryIndex = this.typeTabIndex;
			const categoryKey = `category_${categoryIndex}`;
			const categoryData = this.categoryData[categoryKey];
			
			// 🔧 修复：在 handleReach 中也添加状态检查，防止微信小程序端快速多次触发
			if (!categoryData) {
				console.log('⚠️ handleReach: 分类数据不存在，跳过');
				return;
			}
			
			if (categoryData.status === 'loading') {
				console.log('⚠️ handleReach: 正在加载中，跳过（防止微信小程序端重复触发）');
				return;
			}
			
			if (categoryData.status === 'nomore') {
				console.log('⚠️ handleReach: 没有更多数据，跳过');
				return;
			}
			
			console.log('🔄 触发加载更多，当前分类:', categoryIndex, '状态:', categoryData.status);
			this.loadMoreCategoryData(categoryIndex);
		},

		/**
		 * 获取压缩后的图片URL
		 * @param {String} url - 图片URL
		 * @param {String} type - 图片类型（avatar/cover）
		 * @returns {String} 压缩后的URL
		 */
		getCompressedUrl(url, type = 'cover') {
			if (!url) return url;

			const quality = type === 'avatar' ? 60 : type === 'cover' ? 70 : 80;

			// 如果是OSS图片，添加压缩参数
			if (url.includes('oss') || url.includes('aliyuncs')) {
				const separator = url.includes('?') ? '&' : '?';
				return `${url}${separator}x-oss-process=image/quality,q_${quality}/format,webp`;
			}

			return url;
		},

		/**
		 * 预加载图片
		 * @param {Array} items - 商品列表
		 */
		preloadImages(items) {
			if (!Array.isArray(items) || items.length === 0) return;

			// 提取图片URL
			const imageUrls = [];
			items.forEach(item => {
				// 封面图片
				if (item.coverPicture) {
					imageUrls.push(this.getCompressedUrl(item.coverPicture, 'cover'));
				}
				// 头像
				if (item.avatarUrl) {
					imageUrls.push(this.getCompressedUrl(item.avatarUrl, 'avatar'));
				}
			});

			// 预加载关键图片
			if (imageUrls.length > 0) {
				this.preloadCriticalImages(imageUrls);
			}
		},

		/**
		 * 预加载关键图片
		 * @param {Array} imageUrls - 图片URL列表
		 */
		preloadCriticalImages(imageUrls) {
			if (!Array.isArray(imageUrls) || imageUrls.length === 0) return;

			// 优先预加载前3张图片
			const criticalUrls = imageUrls.slice(0, 3);
			criticalUrls.forEach(url => {
				uni.getImageInfo({
					src: url,
					success: () => console.log('✅ 图片预加载成功:', url),
					fail: () => console.warn('⚠️ 图片预加载失败:', url)
				});
			});
		},

		/**
		 * 预加载下一个分类的数据
		 * @param {Number} currentIndex - 当前分类索引
		 */
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
						this.loadCategoryData(nextIndex);
					}
				}
			}, 1000); // 1秒后预加载
		},

		// ========== 核心数据加载方法 ==========
		// 注意：以下方法代码量很大，保持原有逻辑不变
		// 如需查看完整实现，请参考 idle.vue 中的原始代码
		
		/**
		 * 加载分类数据
		 * @param {Number} categoryIndex - 分类索引
		 */
		async loadCategoryData(categoryIndex) {
			try {
				const categoryKey = `category_${categoryIndex}`;

				// 🔧 修复：获取当前请求ID，防止异步请求返回时覆盖了新的分类数据
				// 🔧 修复：如果 categoryData 不存在，生成新的 requestId；如果存在，使用现有的 requestId
				let currentRequestId;
				if (this.categoryData[categoryKey]?.requestId) {
					currentRequestId = this.categoryData[categoryKey].requestId;
				} else {
					currentRequestId = Date.now();
					// 如果 categoryData 不存在，先初始化它
					if (!this.categoryData[categoryKey]) {
						this.$set(this.categoryData, categoryKey, {
							list: [],
							leftList: [],
							rightList: [],
							leftHeight: 0,
							rightHeight: 0,
							status: 'loading',
							page: 1,
							total: 0,
							pageSize: categoryKey === 'category_0' ? 20 : 6,
							requestId: currentRequestId
						});
					} else {
						this.$set(this.categoryData[categoryKey], 'requestId', currentRequestId);
					}
				}

				// 先检查缓存
				const cachedData = this.getCategoryCache(categoryKey);
				if (cachedData &&
					((cachedData.list && cachedData.list.length > 0) ||
						(cachedData.leftList && cachedData.leftList.length > 0) ||
						(cachedData.rightList && cachedData.rightList.length > 0))) {
					console.log('✅ 市集loadCategoryData: 使用缓存数据，跳过API请求');
					// 🔧 修复：检查请求ID，防止使用过期的缓存数据
					if (this.categoryData[categoryKey]?.requestId === currentRequestId) {
						// 使用缓存数据
						this.$set(this.categoryData, categoryKey, {
							list: cachedData.list || [],
							leftList: cachedData.leftList || [],
							rightList: cachedData.rightList || [],
							leftHeight: cachedData.leftHeight || 0,
							rightHeight: cachedData.rightHeight || 0,
							status: 'loadmore',
							page: cachedData.page || 1,
							pageSize: categoryKey === 'category_0' ? 20 : 6, // 推荐分类使用20条，其他分类使用6条
							initialLoadSize: 3,
							requestId: currentRequestId
						});
					}
					return;
				}

				console.log('🌐 市集loadCategoryData: 缓存未命中，发起API请求');

				// 确保分类数据存在
				if (!this.categoryData[categoryKey]) {
					this.$set(this.categoryData, categoryKey, {
						list: [],
						leftList: [],
						rightList: [],
						leftHeight: 0,
						rightHeight: 0,
						status: 'loading',
						page: 1,
						pageSize: categoryKey === 'category_0' ? 20 : 6, // 推荐分类使用20条，其他分类使用6条
						initialLoadSize: 3,
						requestId: currentRequestId
					});
				}

				const categoryData = this.categoryData[categoryKey];

				if (categoryIndex === 0) {
					// 推荐分类
					const res = await getLastIdleByPage({
						page: 1,
						pageSize: categoryData.pageSize
					});

					// 🔧 修复：检查请求ID，防止异步请求返回时覆盖了新的分类数据
					// 🔧 修复：如果 categoryData 不存在或 requestId 不匹配，说明请求已过期
					const actualRequestId = this.categoryData[categoryKey]?.requestId;
					if (actualRequestId && actualRequestId !== currentRequestId) {
						console.log('ℹ️ 请求已过期，忽略返回数据（这是正常的，可能是预加载或切换分类导致的）', {
							expected: currentRequestId,
							actual: actualRequestId
						});
						return;
					}

					if (res.code === 200) {
						console.log('✅ 推荐分类数据加载成功:', res.data.records.length, '条');
						const records = res.data.records.map(item => this.mapIdleItem(item));
						const total = res.data.total || 0; // 🔧 修复：获取总数
						// 🔧 修复：先保存 total 到 categoryData，确保 processCategoryData 能获取到正确的 total
						if (this.categoryData[categoryKey]) {
							this.$set(this.categoryData[categoryKey], 'total', total);
							console.log('✅ 已保存 total 到 categoryData:', total);
						} else {
							console.warn('⚠️ categoryData 不存在，无法保存 total');
						}
						console.log('✅ 数据映射完成:', records, '总数:', total);
						// 🔧 修复：传递 total 给 processCategoryData，确保状态判断正确
						await this.processCategoryData(categoryIndex, records, currentRequestId, false, total);
					} else {
						console.error('❌ 推荐分类数据加载失败:', res);
					}
				} else if (categoryIndex === 1 && this.findList[categoryIndex]?.isVideo) {
					// 视频分类
					const res = await getVideoProduct({
						page: 1,
						pageSize: categoryData.pageSize
					});

					// 🔧 修复：检查请求ID，防止异步请求返回时覆盖了新的分类数据
					// 🔧 修复：如果 categoryData 不存在或 requestId 不匹配，说明请求已过期
					const actualRequestId = this.categoryData[categoryKey]?.requestId;
					if (actualRequestId && actualRequestId !== currentRequestId) {
						console.log('ℹ️ 请求已过期，忽略返回数据（这是正常的，可能是预加载或切换分类导致的）', {
							expected: currentRequestId,
							actual: actualRequestId
						});
						return;
					}

					if (res.code === 200) {
						console.log('✅ 视频分类数据加载成功:', res.data.records.length, '条');
						const records = res.data.records.map(item => this.mapIdleItem(item));
						const total = res.data.total || 0; // 🔧 修复：获取总数
						// 🔧 修复：先保存 total 到 categoryData，确保 processCategoryData 能获取到正确的 total
						if (this.categoryData[categoryKey]) {
							this.$set(this.categoryData[categoryKey], 'total', total);
							console.log('✅ 已保存 total 到 categoryData:', total);
						} else {
							console.warn('⚠️ categoryData 不存在，无法保存 total');
						}
						// 🔧 修复：传递 total 给 processCategoryData，确保状态判断正确
						await this.processCategoryData(categoryIndex, records, currentRequestId, false, total);
					} else {
						console.error('❌ 视频分类数据加载失败:', res);
					}
				} else {
					// 其他分类
					const curCategory = this.findList[categoryIndex];
					if (!curCategory) return;

					const res = await getIdlesByCategoryId({
						page: 1,
						pageSize: categoryData.pageSize,
						cpid: curCategory.id,
						keyword: ''
					});

					// 🔧 修复：检查请求ID，防止异步请求返回时覆盖了新的分类数据
					// 🔧 修复：如果 categoryData 不存在或 requestId 不匹配，说明请求已过期
					const actualRequestId = this.categoryData[categoryKey]?.requestId;
					if (actualRequestId && actualRequestId !== currentRequestId) {
						console.log('ℹ️ 请求已过期，忽略返回数据（这是正常的，可能是预加载或切换分类导致的）', {
							expected: currentRequestId,
							actual: actualRequestId
						});
						return;
					}

					if (res.code === 200) {
						const records = res.data.records.map(item => this.mapIdleItem(item));
						const total = res.data.total || 0; // 🔧 修复：获取总数
						// 🔧 修复：先保存 total 到 categoryData，确保 processCategoryData 能获取到正确的 total
						if (this.categoryData[categoryKey]) {
							this.$set(this.categoryData[categoryKey], 'total', total);
							console.log('✅ 已保存 total 到 categoryData:', total);
						} else {
							console.warn('⚠️ categoryData 不存在，无法保存 total');
						}
						// 🔧 修复：传递 total 给 processCategoryData，确保状态判断正确
						await this.processCategoryData(categoryIndex, records, currentRequestId, false, total);
					}
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

		/**
		 * 处理分类数据
		 * @param {Number} categoryIndex - 分类索引
		 * @param {Array} records - 数据记录
		 * @param {Number} requestId - 请求ID
		 * @param {Boolean} isRefresh - 是否刷新
		 * @param {Number} apiTotal - API返回的总数
		 */
		async processCategoryData(categoryIndex, records, requestId, isRefresh = false, apiTotal = null) {
			const categoryKey = `category_${categoryIndex}`;
			const categoryData = this.getCategoryData(categoryIndex);
			
			// 🔧 修复：确保 categoryData 对象存在且是响应式的
			if (!this.categoryData[categoryKey]) {
				console.warn('⚠️ processCategoryData: categoryData 不存在，重新创建:', categoryKey);
				this.$set(this.categoryData, categoryKey, {
					list: [],
					leftList: [],
					rightList: [],
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					total: 0,
					pageSize: categoryKey === 'category_0' ? 20 : 6
				});
			}
			
			// 🔧 修复：如果传入了 apiTotal，优先使用它（确保使用最新的 total）
			if (apiTotal !== null && apiTotal >= 0) {
				// 即使 total 为 0 也要保存，确保后续判断正确
				this.$set(this.categoryData[categoryKey], 'total', apiTotal);
				console.log('✅ processCategoryData: 使用传入的 total:', apiTotal);
			}

			// 🔧 修复：检查请求ID，防止异步请求返回时覆盖了新的分类数据
			// 🔧 修复：如果 requestId 存在且不匹配，说明请求已过期（这是正常的，可能是预加载或切换分类导致的）
			if (requestId && categoryData?.requestId && categoryData.requestId !== requestId) {
				console.log('ℹ️ 请求已过期，忽略数据更新（这是正常的，可能是预加载或切换分类导致的）:', {
					categoryIndex,
					expectedRequestId: requestId,
					actualRequestId: categoryData.requestId
				});
				return;
			}

			// 🔧 调试：记录第一条和最后一条数据的ID，用于验证数据是否变化
			const firstRecordId = records[0]?.id || records[0]?.productId || 'unknown';
			const lastRecordId = records[records.length - 1]?.id || records[records.length - 1]?.productId ||
				'unknown';
			console.log('🔄 处理分类数据:', categoryIndex, '记录数:', records.length, '是否刷新:', isRefresh);
			console.log('📊 数据顺序检查 - 第一条ID:', firstRecordId, '最后一条ID:', lastRecordId);

			if (!records.length) {
				console.log('⚠️ 没有数据，设置为nomore状态');
				this.$set(categoryData, 'status', 'nomore');
				return;
			}

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
				console.log('🎲 随机打乱数据顺序，提供更好的用户体验', { isRefresh, isFirstLoad });
			}

			// 🔧 修复：如果是刷新，先确保list为空，触发water-fall组件的清空逻辑
			if (isRefresh) {
				this.$set(categoryData, 'list', []);
				this.$set(categoryData, 'leftList', []);
				this.$set(categoryData, 'rightList', []);
				// 等待DOM更新后再设置新数据，确保water-fall组件能够正确响应
				await this.$nextTick();
			}

			// 计算瀑布流布局
			const leftList = [];
			const rightList = [];
			let leftHeight = 0;
			let rightHeight = 0;

			// 处理有高度信息的数据
			const itemsWithHeight = records.filter(item => item.coverHeight && item.coverHeight > 0);
			const itemsWithoutHeight = records.filter(item => !item.coverHeight || item.coverHeight <= 0);

			// 先处理有高度的数据
			itemsWithHeight.forEach(item => {
				const imgHeight = pxToRpx(item.coverHeight);
				if (leftHeight <= rightHeight) {
					leftList.push({
						...item,
						_imgHeight: imgHeight
					});
					leftHeight += (imgHeight + pxToRpx(50));
				} else {
					rightList.push({
						...item,
						_imgHeight: imgHeight
					});
					rightHeight += (imgHeight + pxToRpx(50));
				}
			});

			// 优化：对于无高度的数据，直接使用默认高度，避免网络请求
			if (itemsWithoutHeight.length > 0) {
				console.log('⚡ 市集页面使用默认高度处理无高度数据，避免网络请求');
				itemsWithoutHeight.forEach(item => {
					const defaultHeight = 200; // 默认高度
					if (leftHeight <= rightHeight) {
						leftList.push({
							...item,
							_imgHeight: defaultHeight
						});
						leftHeight += (defaultHeight + pxToRpx(50));
					} else {
						rightList.push({
							...item,
							_imgHeight: defaultHeight
						});
						rightHeight += (defaultHeight + pxToRpx(50));
					}
				});
			}

			// 合并为统一列表（供组件使用）
			// 🔧 修复：确保 list 的顺序稳定，左列在前，右列在后
			const list = [...leftList, ...rightList];

			// 🔧 修复：状态判断逻辑 - 如果 total > 0，需要判断 list.length < total
			// 如果 total === 0，说明可能是首次加载，暂时设置为 loadmore
			// 🔧 修复：优先使用传入的 apiTotal，如果没有则使用 categoryData.total
			// 🔧 修复：确保从响应式对象中获取最新的 total
			const savedTotal = this.categoryData[categoryKey]?.total;
			const currentTotal = apiTotal !== null ? apiTotal : (savedTotal !== undefined ? savedTotal : (categoryData.total || 0));
			let status = 'loadmore';
			if (records.length === 0) {
				// 如果没有数据，设置为 nomore
				status = 'nomore';
			} else if (currentTotal > 0) {
				// 如果有 total，判断是否还有更多数据
				status = list.length < currentTotal ? 'loadmore' : 'nomore';
				console.log('🔍 processCategoryData 状态判断（有total）:', {
					listLength: list.length,
					currentTotal: currentTotal,
					apiTotal: apiTotal,
					savedTotal: savedTotal,
					categoryDataTotal: categoryData.total,
					hasMore: list.length < currentTotal,
					status: status
				});
			} else {
				// 如果 total === 0，说明可能是首次加载，暂时设置为 loadmore
				status = 'loadmore';
				console.log('🔍 processCategoryData 状态判断（无total）: 设置为 loadmore，等待后续加载获取 total', {
					apiTotal: apiTotal,
					savedTotal: savedTotal,
					categoryDataTotal: categoryData.total
				});
			}
			
			const updatedData = {
				...categoryData,
				leftList: leftList,
				rightList: rightList,
				list: list, // 🔧 修复：确保 list 已经设置好，避免在模板中合并
				leftHeight: leftHeight,
				rightHeight: rightHeight,
				status: status,
				page: 2,
				total: currentTotal // 🔧 修复：使用计算出的 currentTotal，确保 total 被正确保存
			};
			
			console.log('🔍 processCategoryData 状态判断:', {
				recordsLength: records.length,
				listLength: list.length,
				currentTotal: currentTotal,
				status: status
			});

			// 🔧 修复：使用 Vue.set 一次性更新整个对象，只触发一次响应式更新
			// 这样可以避免多次触发组件 watch，防止出现数据闪烁
			this.$set(this.categoryData, categoryKey, updatedData);

			console.log('✅ 分类数据更新完成:', {
				leftList: leftList.length,
				rightList: rightList.length,
				list: list.length,
				leftHeight,
				rightHeight,
				status: updatedData.status,
				total: updatedData.total,
				page: updatedData.page
			});

			// 🔧 修复：如果是刷新，强制刷新water-fall组件
			if (isRefresh) {
				await this.$nextTick();
				const refName = `idleWaterfall_${categoryIndex}`;
				const waterfallComponent = this.$refs[refName];
				if (waterfallComponent) {
					const component = Array.isArray(waterfallComponent) ? waterfallComponent[0] :
						waterfallComponent;
					if (component && typeof component.refresh === 'function') {
						console.log('🔄 强制刷新idle-water-fall组件');
						component.refresh();
					}
				}
			}

			// 延迟检查内容高度，如果不足则自动加载更多（首次加载和刷新都需要）
			setTimeout(() => {
				if (list.length < 10 && updatedData.status !== 'nomore') {
					console.log('📏 数据较少，自动加载更多', '分类:', categoryIndex);
					this.loadMoreCategoryData(categoryIndex);
				}
			}, 800);

			// 🔧 修复：使用 $nextTick 确保数据更新后再触发组件更新
			// 这样可以避免数据更新过程中的多次触发
			this.$nextTick(() => {
				// 确保组件能正确接收到最新的数据
				console.log('✅ 分类数据已更新到视图:', categoryKey, 'list长度:', list.length, {
					firstId: list[0]?.id || list[0]?.productId || 'unknown',
					lastId: list[list.length - 1]?.id || list[list.length - 1]?.productId ||
						'unknown'
				});
			});

			// 预加载下一页数据（如果还有更多数据）
			if (records.length >= categoryData.pageSize) {
				this.preloadNextPage(categoryIndex);
			}

			// 预加载图片
			this.preloadImages(records);

			// 保存到缓存
			this.setCategoryCache(categoryKey, {
				leftList: leftList,
				rightList: rightList,
				leftHeight: leftHeight,
				rightHeight: rightHeight
			});

			console.log('✅ 分类数据加载完成:', categoryIndex, this.findList[categoryIndex]?.name);
		},

		/**
		 * 加载更多分类数据
		 * @param {Number} categoryIndex - 分类索引
		 */
		async loadMoreCategoryData(categoryIndex) {
			try {
				const categoryKey = `category_${categoryIndex}`;
				const categoryData = this.categoryData[categoryKey];

				// 🔧 修复：更严格的状态检查，防止微信小程序端快速多次触发导致重复加载
				if (!categoryData) {
					console.log('⚠️ 分类数据不存在，跳过加载');
					return;
				}
				
				if (categoryData.status === 'nomore') {
					console.log('⚠️ 已经没有更多数据了');
					return;
				}
				
				// 🔧 修复：在设置 loading 状态前再次检查，防止并发请求（微信小程序端特别重要）
				if (categoryData.status === 'loading') {
					console.log('⚠️ 正在加载中，请勿重复请求（微信小程序端防重复）');
					return;
				}

				// 🔧 修复：先保存当前页码，避免在异步操作中页码被修改
				const currentPage = categoryData.page;
				console.log('⏳ 开始加载更多分类数据:', categoryIndex, '页码:', currentPage);
				
				// 🔧 修复：立即设置 loading 状态，防止在异步操作前再次触发（微信小程序端特别重要）
				this.$set(categoryData, 'status', 'loading');

				// 检查是否有预加载的缓存数据
				const cacheKey = `${categoryKey}_page_${currentPage}`;
				const cachedData = this.getCategoryCache(cacheKey);

				if (cachedData && cachedData.records) {
					console.log('✅ 使用预加载的缓存数据:', categoryIndex, '页码:', currentPage);
					const records = cachedData.records;
					if (!records || records.length === 0) {
						console.warn('⚠️ 预加载缓存页返回空数据，停止继续加载', { categoryIndex, currentPage });
						this.$set(categoryData, 'status', 'nomore');
						delete this.categoryCache[cacheKey];
						return;
					}
					const total = cachedData.total || 0; // 从缓存中获取总数
					const currentListLength = categoryData.list ? categoryData.list.length : 0;

					// 🔧 修复：完全参考mine页面的实现，保存 total
					// mine页面：this.idleTotal = pageInfo.total || this.idleTotal
					// 如果新的total存在，使用新的；否则保持旧的
					if (total > 0) {
						this.$set(categoryData, 'total', total);
						console.log('✅ 从缓存更新 total:', total);
					} else if (categoryData.total === undefined || categoryData.total === 0) {
						// 如果旧的total也不存在或为0，才设置为0
						this.$set(categoryData, 'total', 0);
						console.log('⚠️ 缓存中total为0或不存在，设置为0');
					} else {
						// 保持旧的total（参考mine页面的逻辑）
						console.log('ℹ️ 保持旧的total:', categoryData.total);
					}

					// 🔧 修复：在追加数据前，先更新状态为 loading，防止重复触发
					// 注意：这里已经在方法开始时设置为 loading，但为了确保，再次确认
					if (categoryData.status !== 'loading') {
						this.$set(categoryData, 'status', 'loading');
					}

					// 🔧 修复：直接追加（现在会在内部做去重，参考mine页面的实现）
					const appendedCount = await this.appendCategoryData(categoryIndex, records);
					
					// 🔧 修复：在 appendCategoryData 后重新获取 list.length，确保使用最新的长度
					await this.$nextTick(); // 等待响应式更新完成
					const newListLength = this.categoryData[categoryKey]?.list?.length || (currentListLength + appendedCount);

					// 🔧 修复：更新页码（参考"我"页面的实现，在数据加载成功后立即更新页码）
					const currentRequestPage = currentPage; // 保存请求时的页码
					this.$set(categoryData, 'page', currentRequestPage + 1); // 下次请求的页码
					
					// 🔧 修复：完全参考mine页面的实现，标准分页判断逻辑
					// mine页面：const noMore = this.idleList.length >= this.idleTotal || mapped.length < this.idlePageSize
					// 1. 如果去重后的列表长度 >= total，说明已经加载完
					// 2. 如果原始返回的数据量 < pageSize，说明是最后一页
					const finalTotal = this.categoryData[categoryKey]?.total || total || 0;
					const pageSize = categoryData.pageSize || 0;
					
					// 🔧 修复：完全参考mine页面的判断逻辑
					// mine页面使用：this.idleList.length >= this.idleTotal || mapped.length < this.idlePageSize
					// newListLength 已经是去重后的列表长度（类似 mine 页面的 this.idleList.length）
					// records.length 是原始返回的数据量（类似 mine 页面的 mapped.length）
					const noMore = (finalTotal > 0 && newListLength >= finalTotal) || records.length < pageSize;
					const hasMore = !noMore;
					
					console.log('🔍 分页判断（完全参考mine页面，缓存数据）:', {
						appendedCount: appendedCount,
						originalRecordsLength: records.length,
						newListLength: newListLength,
						finalTotal: finalTotal,
						pageSize: pageSize,
						noMore: noMore,
						hasMore: hasMore,
						reason: (finalTotal > 0 && newListLength >= finalTotal) ? '去重后列表长度 >= total' : (records.length < pageSize ? '原始返回数据量 < pageSize' : '还有更多数据')
					});
					
					// 🔧 修复：立即更新状态，避免在状态更新前再次触发加载
					this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');
					console.log(`市集页面缓存数据状态更新: hasMore=${hasMore}, status=${hasMore ? 'loadmore' : 'nomore'}, newListLength=${newListLength}, total=${finalTotal}, appendedCount=${appendedCount}, originalRecordsLength=${records.length}, pageSize=${pageSize}, currentRequestPage=${currentRequestPage}, nextPage=${currentRequestPage + 1}, currentListLength=${currentListLength}`);

					// 预加载下一页数据（如果还有更多数据）
					if (hasMore) {
						this.preloadNextPage(categoryIndex);
					}

					// 清理已使用的缓存
					delete this.categoryCache[cacheKey];
					return;
				}

				// 🔧 修复：使用保存的 currentPage，避免使用可能已被修改的 categoryData.page
				let res;
				if (categoryIndex === 0) {
					// 推荐分类
					res = await getLastIdleByPage({
						page: currentPage,
						pageSize: categoryData.pageSize
					});
				} else if (categoryIndex === 1 && this.findList[categoryIndex]?.isVideo) {
					// 视频分类
					res = await getVideoProduct({
						page: currentPage,
						pageSize: categoryData.pageSize
					});
				} else {
					// 其他分类
					const curCategory = this.findList[categoryIndex];
					if (!curCategory) return;

					// 🔧 修复：添加详细日志，确认传递的页码参数
					console.log('📤 请求分类数据 API:', {
						categoryIndex,
						categoryName: curCategory.name,
						cpid: curCategory.id,
						page: currentPage,
						pageSize: categoryData.pageSize,
						url: `/idle/app/es/product/getProductByDTO/${currentPage}/${categoryData.pageSize}`
					});

					res = await getIdlesByCategoryId({
						page: currentPage,
						pageSize: categoryData.pageSize,
						cpid: curCategory.id,
						keyword: ''
					});

					// 🔧 修复：检查返回数据中的页码信息
					if (res.code === 200 && res.data) {
						const pageMatch = res.data.current === currentPage;
						console.log('📥 API 返回数据信息:', {
							requestPage: currentPage,
							responseCurrent: res.data.current,
							responseTotal: res.data.total,
							responseSize: res.data.size,
							responsePages: res.data.pages,
							recordsCount: res.data.records?.length || 0,
							match: pageMatch ? '✅ 页码匹配' : '⚠️ 页码不匹配！后端可能没有正确处理页码参数'
						});
						
						// 🔧 修复：如果后端返回的页码与请求不一致，记录警告
						// 这可能是后端分页逻辑的问题，但前端会通过去重逻辑来处理重复数据
						if (!pageMatch && currentPage > 1) {
							console.warn('⚠️ 后端分页异常：请求页码与返回页码不一致', {
								requestPage: currentPage,
								responseCurrent: res.data.current,
								message: '后端可能没有正确处理页码参数，导致返回了错误页的数据。前端会通过去重逻辑处理重复数据。'
							});
						}
					}
				}

				if (res.code === 200) {
					const records = (res.data.records || []).map(item => this.mapIdleItem(item));
					const total = res.data.total || 0; // 总数据量
					const currentListLength = categoryData.list ? categoryData.list.length : 0;
					
					// 🔧 修复：更新 total（在判断空数据之前）
					if (total > 0) {
						this.$set(categoryData, 'total', total);
						console.log('✅ 更新 total:', total);
					} else if (categoryData.total === undefined || categoryData.total === 0) {
						this.$set(categoryData, 'total', 0);
						console.log('⚠️ total为0或不存在，设置为0');
					} else {
						console.log('ℹ️ 保持旧的total:', categoryData.total);
					}
					
					if (!records || records.length === 0) {
						// 🔧 修复：当返回空数据时，检查去重后的列表长度是否已达到 total
						const finalTotal = this.categoryData[categoryKey]?.total || 0;
						const currentListLengthAfterCheck = this.categoryData[categoryKey]?.list?.length || currentListLength;
						
						if (finalTotal > 0 && currentListLengthAfterCheck < finalTotal) {
							// 🔧 修复：如果列表长度还没达到 total，可能是后端分页有问题（数据重复或分页错误）
							// 尝试继续加载下一页，但为了避免无限请求，最多尝试一次
							const retryKey = `retry_empty_${categoryKey}`;
							if (!this[retryKey]) {
								this[retryKey] = true;
								console.warn('⚠️ 本页返回空数据，但去重后的列表长度还未达到 total，尝试继续加载', {
									categoryIndex,
									currentPage,
									currentListLength: currentListLengthAfterCheck,
									total: finalTotal,
									missingCount: finalTotal - currentListLengthAfterCheck
								});
								// 继续加载下一页
								this.$set(categoryData, 'page', currentPage + 1);
								this.$set(categoryData, 'status', 'loadmore');
								// 延迟后继续加载，避免立即触发
								setTimeout(() => {
									this.loadMoreCategoryData(categoryIndex);
								}, 300);
								return;
							} else {
								// 已经尝试过一次，再次遇到空数据，停止加载
								console.warn('⚠️ 已尝试跳过空页，但再次遇到空数据，停止加载', {
									categoryIndex,
									currentPage,
									currentListLength: currentListLengthAfterCheck,
									total: finalTotal,
									missingCount: finalTotal - currentListLengthAfterCheck
								});
								this.$set(categoryData, 'status', 'nomore');
								return;
							}
						} else {
							// 列表长度已达到 total，或者 total 为 0，正常停止
							console.log('✅ 本页返回空数据，列表长度已达到 total 或 total 为 0，停止加载', {
								categoryIndex,
								currentPage,
								currentListLength: currentListLengthAfterCheck,
								total: finalTotal
							});
							this.$set(categoryData, 'status', 'nomore');
							return;
						}
					}
					console.log('✅ 加载更多数据成功:', {
						recordsLength: records.length,
						total: total,
						currentListLength: currentListLength,
						pageSize: categoryData.pageSize,
						savedTotal: categoryData.total
					});

					// 🔧 修复：在追加数据前，确保状态为 loading，防止重复触发
					if (categoryData.status !== 'loading') {
						this.$set(categoryData, 'status', 'loading');
					}

					// 🔧 修复：直接追加（现在会在内部做去重，参考mine页面的实现）
					const appendedCount = await this.appendCategoryData(categoryIndex, records);
					
					// 🔧 修复：在 appendCategoryData 后重新获取 list.length，确保使用最新的长度
					await this.$nextTick(); // 等待响应式更新完成
					const newListLength = this.categoryData[categoryKey]?.list?.length || (currentListLength + appendedCount);

					// 🔧 修复：更新页码（参考"我"页面的实现，在数据加载成功后立即更新页码）
					const currentRequestPage = currentPage; // 保存请求时的页码
					this.$set(categoryData, 'page', currentRequestPage + 1); // 下次请求的页码
					
					// 🔧 修复：完全参考mine页面的实现，标准分页判断逻辑
					// mine页面：const noMore = this.idleList.length >= this.idleTotal || mapped.length < this.idlePageSize
					// 1. 如果去重后的列表长度 >= total，说明已经加载完
					// 2. 如果原始返回的数据量 < pageSize，说明是最后一页
					const finalTotal = this.categoryData[categoryKey]?.total || total || 0;
					const pageSize = categoryData.pageSize || 0;
					
					// 🔧 修复：完全参考mine页面的判断逻辑
					// mine页面使用：this.idleList.length >= this.idleTotal || mapped.length < this.idlePageSize
					// newListLength 已经是去重后的列表长度（类似 mine 页面的 this.idleList.length）
					// records.length 是原始返回的数据量（类似 mine 页面的 mapped.length）
					const noMore = (finalTotal > 0 && newListLength >= finalTotal) || records.length < pageSize;
					const hasMore = !noMore;
					
					console.log('🔍 分页判断（完全参考mine页面）:', {
						appendedCount: appendedCount,
						originalRecordsLength: records.length,
						newListLength: newListLength,
						finalTotal: finalTotal,
						pageSize: pageSize,
						noMore: noMore,
						hasMore: hasMore,
						reason: (finalTotal > 0 && newListLength >= finalTotal) ? '去重后列表长度 >= total' : (records.length < pageSize ? '原始返回数据量 < pageSize' : '还有更多数据')
					});
					
					// 🔧 修复：立即更新状态，避免在状态更新前再次触发加载
					this.$set(categoryData, 'status', hasMore ? 'loadmore' : 'nomore');
					console.log(`市集页面状态更新: hasMore=${hasMore}, status=${hasMore ? 'loadmore' : 'nomore'}, newListLength=${newListLength}, total=${finalTotal}, appendedCount=${appendedCount}, originalRecordsLength=${records.length}, pageSize=${pageSize}, currentRequestPage=${currentRequestPage}, nextPage=${currentRequestPage + 1}, currentListLength=${currentListLength}`);

					// 预加载下一页数据（如果还有更多数据）
					if (hasMore) {
						this.preloadNextPage(categoryIndex);
					}
				} else {
					console.error('❌ 加载更多数据失败:', res);
					this.$set(categoryData, 'status', 'nomore');
				}
			} catch (error) {
				console.error('❌ 加载更多分类数据失败:', categoryIndex, error);
				const categoryKey = `category_${categoryIndex}`;
				if (this.categoryData[categoryKey]) {
					this.$set(this.categoryData[categoryKey], 'status', 'nomore');
				}
			}
		},

		/**
		 * 追加分类数据到现有列表
		 * @param {Number} categoryIndex - 分类索引
		 * @param {Array} records - 数据记录
		 * @returns {Number} 实际追加的数据量
		 */
		async appendCategoryData(categoryIndex, records) {
			const categoryKey = `category_${categoryIndex}`;
			
			// 🔧 修复：添加锁机制，防止微信小程序端并发追加导致重复
			if (this.appendLocks && this.appendLocks[categoryKey]) {
				console.log('⚠️ appendCategoryData: 正在追加中，跳过本次请求（防止微信小程序端重复）', categoryKey);
				return 0;
			}
			
			// 设置锁
			if (!this.appendLocks) {
				this.$set(this, 'appendLocks', {});
			}
			this.$set(this.appendLocks, categoryKey, true);
			
			try {
				// 🔧 修复：确保 categoryData 对象存在，避免响应式丢失
				if (!this.categoryData[categoryKey]) {
					console.warn('⚠️ appendCategoryData: categoryData 不存在，重新创建:', categoryKey);
					this.$set(this.categoryData, categoryKey, {
						list: [],
						leftList: [],
						rightList: [],
						leftHeight: 0,
						rightHeight: 0,
						status: 'loadmore',
						page: 1,
						total: 0,
						pageSize: categoryKey === 'category_0' ? 20 : 6
					});
				}
				
				// 🔧 修复：在确保 categoryData 存在后，重新获取引用
				const categoryData = this.categoryData[categoryKey];

				if (!records.length) {
					return 0; // 🔧 修复：返回0，表示没有数据可追加
				}

				// 🔧 修复：在追加前再次读取最新的 list，确保使用的是最新的数据（防止并发问题）
				// 因为可能在读取 currentList 和更新 list 之间，有其他请求已经更新了 list
				const currentList = categoryData.list || [];
				const existingIds = new Set(currentList.map(item => {
					const id = item.id || item.productId || item.noteId;
					return id !== undefined && id !== null ? String(id) : null;
				}).filter(id => id !== null));
			
				const newItems = records.filter(item => {
					const id = item.id || item.productId || item.noteId;
					if (id === undefined || id === null) {
						console.warn('⚠️ appendCategoryData: 发现无效ID的数据', item);
						return false;
					}
					return !existingIds.has(String(id));
				});

				if (newItems.length === 0) {
					console.log('⚠️ appendCategoryData: 所有数据都已存在，跳过追加');
					return 0; // 🔧 修复：返回0，表示没有追加任何数据
				}

				console.log('✅ appendCategoryData: 去重后新数据', {
					originalCount: records.length,
					newItemsCount: newItems.length,
					currentListLength: currentList.length
				});

				// 🔧 修复：在更新 list 之前，再次读取最新的 list，确保使用的是最新的数据
				// 因为可能在去重检查和更新 list 之间，有其他请求已经更新了 list
				const latestList = categoryData.list || [];
				const latestIds = new Set(latestList.map(item => {
					const id = item.id || item.productId || item.noteId;
					return id !== undefined && id !== null ? String(id) : null;
				}).filter(id => id !== null));
				
				// 再次过滤，确保不会添加已存在的数据
				const finalNewItems = newItems.filter(item => {
					const id = item.id || item.productId || item.noteId;
					if (id === undefined || id === null) {
						return false;
					}
					return !latestIds.has(String(id));
				});
				
				if (finalNewItems.length === 0) {
					console.log('⚠️ appendCategoryData: 在更新前再次检查，所有数据都已存在，跳过追加');
					return 0;
				}
				
				if (finalNewItems.length < newItems.length) {
					console.log('🔍 appendCategoryData: 在更新前再次检查，过滤掉重复数据', {
						originalNewItems: newItems.length,
						finalNewItems: finalNewItems.length,
						filteredCount: newItems.length - finalNewItems.length
					});
				}

				// 🔧 修复：只更新 list，让组件的 watch 自动处理 addList
				// 因为 idle-water-fall 组件的 watch 会自动检测数据追加并调用 addList
				// 如果手动调用 addList，会导致重复调用（watch 一次 + 手动一次）
				const list = [...latestList, ...finalNewItems];
				this.$set(categoryData, 'list', list);
				
				// 🔧 修复：等待组件 watch 处理完成，获取实际显示的数据量
				await this.$nextTick();
				await new Promise(resolve => setTimeout(resolve, 150)); // 等待 watch 处理完成
				
				// 获取组件实际显示的数据量（通过 ref 获取）
				const refName = `idleWaterfall_${categoryIndex}`;
				let component = this.$refs[refName];
				
				// 🔧 修复：v-for 中的 ref 可能是数组，需要取对应索引的元素
				if (Array.isArray(component)) {
					component = component[categoryIndex] || component.find(c => c) || component[0];
				}
				
				if (component) {
					const actualDisplayCount = (component.leftList?.length || 0) + (component.rightList?.length || 0);
					console.log('📊 组件实际显示的数据量（watch自动处理）:', {
						leftListLength: component.leftList?.length || 0,
						rightListLength: component.rightList?.length || 0,
						actualDisplayCount: actualDisplayCount,
						listLength: list.length
					});
					
					// 保存实际显示的数据量，用于状态判断
					this.$set(categoryData, 'actualDisplayCount', actualDisplayCount);
				}

				console.log('✅ 数据追加完成（由组件watch自动处理）:', {
					categoryKey: categoryKey,
					currentListLength: latestList.length,
					originalRecordsLength: records.length,
					newItemsLength: newItems.length,
					finalNewItemsLength: finalNewItems.length,
					newListLength: list.length,
					categoryDataExists: !!this.categoryData[categoryKey],
					categoryDataListLength: this.categoryData[categoryKey]?.list?.length || 0,
					note: '组件watch会自动调用addList，无需手动调用，避免重复'
				});
				
				// 🔧 修复：返回实际追加的数据量，用于状态判断
				return finalNewItems.length; // 🔧 修复：返回实际追加的数据量
			} finally {
				// 🔧 修复：释放锁，确保即使出错也能释放
				if (this.appendLocks) {
					this.$set(this.appendLocks, categoryKey, false);
				}
			}
		},

		/**
		 * 预加载下一页数据
		 * @param {Number} categoryIndex - 分类索引
		 */
		async preloadNextPage(categoryIndex) {
			try {
				const categoryKey = `category_${categoryIndex}`;
				const categoryData = this.categoryData[categoryKey];

				if (!categoryData || categoryData.status === 'nomore') {
					return;
				}

				// 延迟预加载，避免影响当前页面的渲染
				setTimeout(async () => {
					try {
						// 🔧 修复：保存当前页码，避免在异步操作中页码被修改
						const preloadPage = categoryData.page;
						console.log('🔄 预加载下一页数据:', categoryIndex, '页码:', preloadPage);

						let res;
						if (categoryIndex === 0) {
							// 推荐分类
							res = await getLastIdleByPage({
								page: preloadPage,
								pageSize: categoryData.pageSize
							});
						} else if (categoryIndex === 1 && this.findList[categoryIndex]?.isVideo) {
							// 视频分类
							res = await getVideoProduct({
								page: preloadPage,
								pageSize: categoryData.pageSize
							});
						} else {
							// 其他分类
							const curCategory = this.findList[categoryIndex];
							if (!curCategory) return;

							res = await getIdlesByCategoryId({
								page: preloadPage,
								pageSize: categoryData.pageSize,
								cpid: curCategory.id,
								keyword: ''
							});
						}

						if (res.code === 200 && res.data.records.length > 0) {
							const records = res.data.records.map(this.mapIdleItem);
							const total = res.data.total || 0; // 获取总数

							// 将预加载的数据添加到缓存中（包含total字段）
							// 🔧 修复：使用保存的 preloadPage，避免使用可能已被修改的 categoryData.page
							const cacheKey = `${categoryKey}_page_${preloadPage}`;
							this.setCategoryCache(cacheKey, {
								records: records,
								total: total, // 🔧 修复：保存总数，用于后续判断是否还有更多数据
								page: preloadPage,
								timestamp: Date.now()
							});

							console.log('✅ 预加载下一页完成:', categoryIndex, '页码:', preloadPage, '数据量:',
								records.length, '总数:', total);
						}
					} catch (error) {
						console.warn('⚠️ 预加载下一页失败:', error);
					}
				}, 2000); // 2秒后预加载

			} catch (error) {
				console.warn('⚠️ 预加载下一页异常:', error);
			}
		},

		/**
		 * 下拉刷新
		 */
		async onRefresh() {
			console.log('🔄 市集页面下拉刷新，当前分类索引:', this.typeTabIndex);
			if (this.refreshing) {
				console.log('⚠️ 正在刷新中，跳过');
				return;
			}
			this.refreshing = true;
			console.log('✅ 设置refreshing为true，开始刷新');

			// 下拉刷新时同步拉取最新轮播图配置
			getCarouselConfig().then((res) => {
				if (res && res.code === 200 && res.data && Array.isArray(res.data) && res.data.length > 0) {
					this.bannerList = res.data.map((item) => ({
						url: item.url || '',
						link: item.link || '',
						title: item.title || ''
					}));
				}
			}).catch(() => {});

			// 🔧 修复：下拉刷新时清除缓存，确保获取最新数据
			const categoryKey = `category_${this.typeTabIndex}`;
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

			this.notesList.status = 'loadmore';
			this.notesList.page = 1;

			// 注意：市集页面暂不需要定位功能，已移除定位调用
			// 如果未来需要基于位置的商品推荐，可以在这里添加定位逻辑
			// 当前移除定位调用以避免 iOS 系统级别的定位错误日志（kCLErrorLocationUnknown）
			try {
				if (this.typeTabIndex === 0) {
					// 推荐
					console.log('🔄 刷新推荐分类');
					const categoryKey = `category_${this.typeTabIndex}`;
					const categoryData = this.getCategoryData(this.typeTabIndex);

					// 🔧 修复：刷新时先清空数据
					this.$set(categoryData, 'list', []);
					this.$set(categoryData, 'leftList', []);
					this.$set(categoryData, 'rightList', []);
					this.$set(categoryData, 'leftHeight', 0);
					this.$set(categoryData, 'rightHeight', 0);
					this.$set(categoryData, 'page', 1);
					this.$set(categoryData, 'status', 'loadmore'); // 设置为loadmore，允许刷新

					// 🔧 修复：刷新时先清空water-fall组件的数据
					this.$nextTick(() => {
						const refName = `idleWaterfall_${this.typeTabIndex}`;
						const waterfallComponent = this.$refs[refName];
						if (waterfallComponent) {
							const component = Array.isArray(waterfallComponent) ? waterfallComponent[0] :
								waterfallComponent;
							if (component && typeof component.refresh === 'function') {
								console.log('🔄 调用idle-water-fall组件的refresh方法');
								component.refresh();
							} else if (component && typeof component.clear === 'function') {
								console.log('🔄 调用idle-water-fall组件的clear方法');
								component.clear();
							}
						}
					});
					await this.refreshList();
				} else if (this.typeTabIndex === 1 && this.findList[this.typeTabIndex]?.isVideo) {
					// 视频分类
					console.log('🔄 刷新视频分类');
					const categoryKey = `category_${this.typeTabIndex}`;
					const categoryData = this.getCategoryData(this.typeTabIndex);

					// 🔧 修复：刷新时先清空数据
					this.$set(categoryData, 'list', []);
					this.$set(categoryData, 'leftList', []);
					this.$set(categoryData, 'rightList', []);
					this.$set(categoryData, 'leftHeight', 0);
					this.$set(categoryData, 'rightHeight', 0);
					this.$set(categoryData, 'page', 1);
					this.$set(categoryData, 'status', 'loading');

					// 🔧 修复：刷新时先清空water-fall组件的数据
					this.$nextTick(() => {
						const refName = `idleWaterfall_${this.typeTabIndex}`;
						const waterfallComponent = this.$refs[refName];
						if (waterfallComponent) {
							const component = Array.isArray(waterfallComponent) ? waterfallComponent[0] :
								waterfallComponent;
							if (component && typeof component.refresh === 'function') {
								console.log('🔄 调用idle-water-fall组件的refresh方法');
								component.refresh();
							} else if (component && typeof component.clear === 'function') {
								console.log('🔄 调用idle-water-fall组件的clear方法');
								component.clear();
							}
						}
					});

					const res = await getVideoProduct({
						page: 1,
						pageSize: categoryData?.pageSize || 6
					});

					if (res.code === 200) {
						const records = res.data.records.map(item => this.mapIdleItem(item));
						// 🔧 修复：传递 isRefresh 参数
						await this.processCategoryData(this.typeTabIndex, records, null, true);
					}
				} else {
					// 其他分类
					console.log('🔄 刷新其他分类');
					await this.refreshFindNotes();
				}
			} catch (e) {
				console.error('刷新失败:', e);
			}
			// 🔧 修复：延迟重置refreshing，确保UI更新
			setTimeout(() => {
				this.refreshing = false;
				console.log('✅ 重置refreshing为false');
			}, 300);
		},

		/**
		 * 刷新列表（推荐分类）
		 */
		async refreshList() {
			console.log('🔄 刷新推荐分类列表');
			const categoryIndex = this.typeTabIndex;
			const categoryKey = `category_${categoryIndex}`;
			const categoryData = this.getCategoryData(categoryIndex);

			// 🔧 修复：设置加载状态
			this.$set(categoryData, 'status', 'loading');

			// 🔧 修复：重置数据，使用 categoryData 结构
			this.$set(categoryData, 'list', []);
			this.$set(categoryData, 'leftList', []);
			this.$set(categoryData, 'rightList', []);
			this.$set(categoryData, 'leftHeight', 0);
			this.$set(categoryData, 'rightHeight', 0);
			this.$set(categoryData, 'page', 1);

			try {
				let records = [];
				if (this.typeTabIndex === 0) {
					// 推荐
					console.log('📥 请求推荐商品数据');
					const res = await getLastIdleByPage({
						page: 1,
						pageSize: categoryData.pageSize || 6
					});

					// 🔧 调试：记录返回的数据ID，用于验证后端随机性
					if (res.code === 200 && res.data.records) {
						const recordIds = res.data.records.map(r => r.id || r.productId || 'unknown').join(',');
						console.log('📥 后端返回数据ID列表:', recordIds);
					}

					if (res.code === 200) {
						records = res.data.records.map(item => this.mapIdleItem(item));
						console.log('✅ 推荐商品数据映射完成，记录数:', records.length);
					} else {
						console.error('❌ 推荐商品数据加载失败:', res);
					}
				} else if (this.typeTabIndex === 1 && this.findList[this.typeTabIndex]?.isVideo) {
					// 视频分类
					const res = await getVideoProduct({
						page: 1,
						pageSize: categoryData.pageSize || 6
					});
					if (res.code === 200) {
						records = res.data.records.map(item => this.mapIdleItem(item));
					}
				} else {
					// 其它分类
					const curCategory = this.findList[this.typeTabIndex];
					const res = await getIdlesByCategoryId({
						page: 1,
						pageSize: categoryData.pageSize || 6,
						cpid: curCategory.id,
						keyword: ''
					});
					if (res.code === 200) {
						records = res.data.records.map(this.mapIdleItem);
					}
				}

				if (!records.length) {
					console.log('⚠️ 没有数据，设置为nomore状态');
					this.$set(categoryData, 'status', 'nomore');
					return;
				}

				// 🔧 修复：使用 processCategoryData 处理数据，传递 isRefresh=true
				await this.processCategoryData(categoryIndex, records, null, true);

				// 🔧 修复：刷新后设置page为2，为下次加载准备
				this.$set(categoryData, 'page', 2);
			} catch (err) {
				console.error('刷新失败:', err);
				this.$set(categoryData, 'status', 'nomore');
				uni.showToast({
					title: '加载失败',
					icon: 'none',
					duration: 1000
				});
			}
		},

		/**
		 * 刷新其他分类
		 */
		async refreshFindNotes() {
			console.log('🔄 开始下拉刷新其他分类，当前分类索引:', this.typeTabIndex);

			// 如果是推荐分类或视频分类，不执行此方法
			if (this.typeTabIndex === 0) return;
			if (this.typeTabIndex === 1 && this.findList[this.typeTabIndex]?.isVideo) return;

			const categoryKey = `category_${this.typeTabIndex}`;
			const categoryData = this.getCategoryData(this.typeTabIndex);
			const curCategory = this.findList[this.typeTabIndex];

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

			// 清空现有数据
			this.$set(categoryData, 'list', []);
			this.$set(categoryData, 'leftList', []);
			this.$set(categoryData, 'rightList', []);
			this.$set(categoryData, 'leftHeight', 0);
			this.$set(categoryData, 'rightHeight', 0);
			this.$set(categoryData, 'page', 1);
			this.$set(categoryData, 'status', 'loading');

			// 🔧 修复：刷新时先清空water-fall组件的数据
			this.$nextTick(() => {
				const refName = `idleWaterfall_${this.typeTabIndex}`;
				const waterfallComponent = this.$refs[refName];
				if (waterfallComponent) {
					const component = Array.isArray(waterfallComponent) ? waterfallComponent[0] :
						waterfallComponent;
					if (component && typeof component.refresh === 'function') {
						console.log('🔄 调用idle-water-fall组件的refresh方法');
						component.refresh();
					} else if (component && typeof component.clear === 'function') {
						console.log('🔄 调用idle-water-fall组件的clear方法');
						component.clear();
					}
				}
			});

			try {
				const res = await getIdlesByCategoryId({
					page: 1,
					pageSize: categoryData.pageSize,
					cpid: curCategory.id,
					keyword: ''
				});

				// 🔧 调试：记录返回的数据ID，用于验证后端随机性
				if (res.code == 200 && res.data.records) {
					const recordIds = res.data.records.map(r => r.id || r.productId || 'unknown').join(',');
					console.log('📥 后端返回数据ID列表:', recordIds);
				}

				if (res.code == 200) {
					let records = res.data.records.map(item => this.mapIdleItem(item));
					if (!records.length) {
						this.$set(categoryData, 'list', []);
						this.$set(categoryData, 'leftList', []);
						this.$set(categoryData, 'rightList', []);
						this.$set(categoryData, 'page', 1);
						this.$set(categoryData, 'status', 'nomore');
						return;
					}

					// 🔧 修复：使用 processCategoryData 处理数据，传递 isRefresh=true
					await this.processCategoryData(this.typeTabIndex, records, null, true);
				} else {
					this.$set(categoryData, 'status', 'nomore');
				}
			} catch (err) {
				console.error('加载分类商品失败:', err);
				this.$set(categoryData, 'status', 'nomore');
				uni.showToast({
					title: '加载失败，请重试',
					icon: 'none',
					duration: 1000
				});
			}
		},

		/**
		 * 加载更多其他分类
		 */
		async loadMoreFindNotes() {
			if (this.notesList.status == 'nomore' || this.notesList.status == 'loading') {
				return;
			}
			this.notesList.status = 'loading';
			const curCategory = this.findList[this.typeTabIndex];

			try {
				const res = await getIdlesByCategoryId({
					page: this.notesList.page,
					pageSize: this.notesList.pageSize,
					cpid: curCategory.id,
					keyword: ''
				});

				if (res.code == 200) {
					const records = res.data.records.map(item => this.mapIdleItem(item));

					// 优化：批量处理数据，减少 setData 次数（小程序性能优化）
					const itemsWithHeight = [];
					const itemsWithoutHeight = [];

					records.forEach(item => {
						if (item.noteCoverHeight && item.noteCoverHeight > 0) {
							itemsWithHeight.push(item);
						} else {
							itemsWithoutHeight.push(item);
						}
					});

					// 先同步处理有高度的数据
					const newLeftList = [];
					const newRightList = [];
					let leftHeight = this.notesList.leftHeight;
					let rightHeight = this.notesList.rightHeight;

					itemsWithHeight.forEach(item => {
						const height = pxToRpx(item.noteCoverHeight);
						if (leftHeight <= rightHeight) {
							newLeftList.push(item);
							leftHeight += (height + pxToRpx(50));
						} else {
							newRightList.push(item);
							rightHeight += (height + pxToRpx(50));
						}
					});

					// 再异步处理无高度的数据
					if (itemsWithoutHeight.length > 0) {
						const imgInfos = await Promise.all(
							itemsWithoutHeight.map(item => this.getImageHeight(item.coverPicture))
						);

						itemsWithoutHeight.forEach((item, i) => {
							item.coverPicture = imgInfos[i].path;
							const height = imgInfos[i].height;
							if (leftHeight <= rightHeight) {
								newLeftList.push(item);
								leftHeight += (height + pxToRpx(50));
							} else {
								newRightList.push(item);
								rightHeight += (height + pxToRpx(50));
							}
						});
					}

					// 一次性更新（只触发 1 次 setData，小程序性能优化关键）
					this.notesList.leftList = [...this.notesList.leftList, ...newLeftList];
					this.notesList.rightList = [...this.notesList.rightList, ...newRightList];
					this.notesList.leftHeight = leftHeight;
					this.notesList.rightHeight = rightHeight;

					this.notesList.page += 1;
					this.notesList.status = res.data.records.length < this.notesList.pageSize ?
						'nomore' : 'loadmore';
				} else {
					this.notesList.status = 'nomore';
				}
			} catch (error) {
				console.error('加载分类商品失败:', error);
				this.notesList.status = 'nomore';
				uni.showToast({
					title: '加载失败，请重试',
					icon: 'none',
					duration: 1000
				});
			}
		},

		/**
		 * 显示缓存状态（调试方法）
		 */
		showCacheStatus() {
			console.log('📊 市集缓存状态报告:', {
				categoryData: Object.keys(this.categoryData),
				categoryCache: Object.keys(this.categoryCache || {}),
				cacheDetails: Object.keys(this.categoryCache || {}).map(key => ({
					key,
					leftList: this.categoryCache[key]?.leftList?.length || 0,
					rightList: this.categoryCache[key]?.rightList?.length || 0,
					timestamp: new Date(this.categoryCache[key]?.timestamp || 0)
						.toLocaleTimeString()
				}))
			});
		},
	},
	// 生命周期钩子
	created() {
		// 初始化防抖版本的 onReach 方法
		// 注意：必须在 created 中初始化，因为模板中使用了 @scrolltolower="onReach"
		// 🔧 修复：微信小程序端 scroll-view 的 scrolltolower 事件可能被快速多次触发
		// 增加防抖延迟时间到 300ms，并在 handleReach 中增加状态检查
		this.onReach = this.debounce(this.handleReach, 300);
	}
}

