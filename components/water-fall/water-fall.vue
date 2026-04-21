<template>
	<view style="width: 100%;display: flex;flex-wrap: wrap; background: #f5f5f5;">
		<view class="water-left">
			<block v-for="(item,index) in leftList" :key="index">
				<view class="note-card">
					<view class="image-container">
						<u--image v-if="item.coverPicture && String(item.coverPicture).trim()"
							@load="onCoverLoad(item)"
							@click="goToDetail(item.id,item.notesType)" :src="item.coverPicture" width="100%"
							height="auto" mode="widthFix" :fade="false" lazyLoad webp :style="imageStyle">
							<template v-slot:loading>
								<view style="height: 200rpx;text-align: center;padding: 20rpx;">
									<u-loading-icon color="#3d8af5"></u-loading-icon>
									<view style="font-size: 30rpx;">loading......</view>
								</view>
							</template>
						</u--image>
						<!-- 审核中状态 -->
						<view v-if="item.auditStatus === '0'" class="audit-overlay">
							<view class="audit-eye">
								<u-icon name="eye" color="#ffffff" size="48"></u-icon>
								<view style="color: #ffffff; font-size: 24rpx; margin-top: 8rpx;">审核中...</view>
							</view>
						</view>
						<!-- 未通过状态 -->
						<view v-if="item.auditStatus === '2'" class="audit-overlay not-passed">
							<view class="audit-eye">
								<view style="color: #3d8af5; font-size: 24rpx;">未通过⚠️</view>
								<u-button v-if="item.uid === userId" type="error" size="mini"
									@click.stop="editNote(item.id)" style="margin-top: 10rpx; font-size: 20rpx;">
									重新编辑
								</u-button>
							</view>
						</view>
						<!-- 置顶标签 -->
						<view class="top-wrapper" v-if="item.pinned ==1 && type ==1 && showPinned">
							<view style="margin-left: 5rpx;">置顶</view>
						</view>
						<!-- 浏览数 -->
						<view class="look-views" v-if="showViews && item.notesViewNum!=null ">
							<u-icon name="eye" color="#ffffff" size="25rpx"></u-icon>
							<view style="margin-left: 5rpx;">{{item.notesViewNum}}</view>
						</view>
						<!-- 位置信息 -->
						<view class="look-views" v-if="showLocation && formatAddress(item)">
							<u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
							<view style="margin-left: 5rpx;">{{formatAddress(item)}}</view>
						</view>
						<!-- 视频标识 -->
						<view v-if="item.notesType==2" class="video-play">
							<image src="@/static/svgs/video.svg" style="width: 45rpx;" mode="widthFix"></image>
						</view>
						<!-- <view v-if="item.notesType==2" class="video-play">
							<u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
						</view> -->
						<!-- Live 图标识 -->
						<view v-if="item.notesType==3" class="live-badge">
							<image src="@/static/svgs/live.svg" style="width: 45rpx;" mode="widthFix"></image>
						</view>
						<!-- <view v-if="item.notesType==3" class="live-badge">
							<text>Live</text>
						</view> -->
					</view>
					<view class="title" @click="goToDetail(item.id,item.notesType)">{{item.title}}</view>
					<view style="display: flex;position: relative;padding: 20rpx;" v-if="slot_bottom">
						<image v-if="item.avatarUrl && String(item.avatarUrl).trim()"
							style="height: 22px;width: 22px;border-radius: 50%;" mode="aspectFill"
							:src="item.avatarUrl">
						</image>
						<view class="note-username">
							{{item.nickname}}
						</view>
						<view style="display: flex;position: absolute;right: 10rpx;">
							<!-- 只有审核通过的笔记才显示点赞按钮 -->
							<template v-if="item.auditStatus === '1'">
								<view @tap.stop="praiseNotes(item.id,item.belongUserId,1,index)"
									style="display: flex; align-items: center;">
									<u-transition :show="!item.isLike" mode="fade" duration="2000">
										<u-icon v-if="!item.isLike" name="/static/praise.png" size="18"></u-icon>
									</u-transition>
									<u-transition :show="item.isLike" mode="fade" duration="2000">
										<u-icon v-if="item.isLike" name="/static/praise_select.png" size="18"></u-icon>
									</u-transition>
								</view>
							</template>
							<!-- 审核中或未通过的笔记显示不可操作状态 -->
							<template v-else>
								<u-icon name="/static/praise.png" size="18" color="#ccc" style="opacity: 0.5;"></u-icon>
							</template>
							<view v-if="item.notesLikeNum>0"
								style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
								{{item.notesLikeNum}}
							</view>
							<view v-else style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
								{{'赞'}}
							</view>
						</view>
					</view>
					<view v-else style="display: flex;position: relative;padding: 20rpx;">
						<view style="margin-right: auto;color: #c8c9cc;font-size: 23rpx;">{{item.updateTime}}</view>
						<u-icon name="trash" @click="deleteDraft(item.id,0)" color="#c8c9cc" size="18"></u-icon>
					</view>
				</view>
			</block>
		</view>
		<view class="water-right">
			<block v-for="(item,index) in rightList" :key="index">
				<view class="note-card">
					<view class="image-container" @click="goToDetail(item.id,item.notesType)">
						<u--image v-if="item.coverPicture && String(item.coverPicture).trim()"
							@load="onCoverLoad(item)" :src="item.coverPicture"
							width="100%" height="auto" mode="widthFix" :fade="false" lazyLoad webp :style="imageStyle">
							<template v-slot:loading>
								<view style="height: 200rpx;text-align: center;padding: 20rpx;margin-bottom: 30rpx;">
									<u-loading-icon color="#3d8af5"></u-loading-icon>
									<view style="font-size: 30rpx;">loading......</view>
								</view>
							</template>
						</u--image>
						<!-- 审核中状态 -->
						<view v-if="item.auditStatus === '0'" class="audit-overlay">
							<view class="audit-eye">
								<u-icon name="eye" color="#ffffff" size="48"></u-icon>
								<view style="color: #ffffff; font-size: 24rpx; margin-top: 8rpx;">审核中...</view>
							</view>
						</view>
						<!-- 未通过状态 -->
						<view v-if="item.auditStatus === '2'" class="audit-overlay not-passed">
							<view class="audit-eye">
								<view style="color: #3d8af5; font-size: 24rpx;">未通过⚠️</view>
								<u-button v-if="item.uid === userId" type="error" size="mini"
									@click.stop="editNote(item.id)" style="margin-top: 10rpx; font-size: 20rpx;">
									重新编辑
								</u-button>
							</view>
						</view>
						<!-- 置顶标签 -->
						<view class="top-wrapper" v-if="item.pinned ==1 && type ==1 && showPinned">
							<view style="margin-left: 5rpx;">置顶</view>
						</view>
						<!-- 浏览数 -->
						<view class="look-views" v-if="showViews && item.notesViewNum!=null ">
							<u-icon name="eye" color="#ffffff" size="25rpx"></u-icon>
							<view style="margin-left: 5rpx;">{{item.notesViewNum}}</view>
						</view>
						<!-- 位置信息 -->
						<view class="look-views" v-if="showLocation && formatAddress(item)">
							<u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
							<view style="margin-left: 5rpx;">{{formatAddress(item)}}</view>
						</view>
						<!-- 视频标识 -->
						<view v-if="item.notesType==2" class="video-play">
							<image src="@/static/svgs/video.svg" style="width: 45rpx;" mode="widthFix"></image>
						</view>
						<!-- <view v-if="item.notesType==2" class="video-play">
							<u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
						</view> -->
						<!-- Live 图标识 -->
						<view v-if="item.notesType==3" class="live-badge">
							<image src="@/static/svgs/live.svg" style="width: 45rpx;" mode="widthFix"></image>
						</view>
						<!-- <view v-if="item.notesType==3" class="live-badge">
							<text>Live</text>
						</view> -->
					</view>

					<view class="title" @click.stop="goToDetail(item.id,item.notesType)">{{item.title}}</view>
					<view style="display: flex;position: relative;padding: 20rpx;" v-if="slot_bottom">
						<image v-if="item.avatarUrl && String(item.avatarUrl).trim()"
							style="height: 22px;width: 22px;border-radius: 50%;" mode="aspectFill"
							:src="item.avatarUrl">
						</image>
						<view class="note-username">
							{{item.nickname}}
						</view>
						<view
							style="display: flex;flex-direction: row;align-items: center;position: absolute;right: 10rpx;">
							<!-- 只有审核通过的笔记才显示点赞按钮 -->
							<template v-if="item.auditStatus === '1'">
								<view @tap.stop="praiseNotes(item.id,item.belongUserId,2,index)"
									style="display: flex; align-items: center;">
									<u-transition :show="!item.isLike" mode="fade" duration="2000">
										<u-icon v-if="!item.isLike" name="/static/praise.png" size="18"></u-icon>
									</u-transition>
									<u-transition :show="item.isLike" mode="fade" duration="2000">
										<u-icon v-if="item.isLike" name="/static/praise_select.png" size="18"></u-icon>
									</u-transition>
								</view>
							</template>
							<!-- 审核中或未通过的笔记显示不可操作状态 -->
							<template v-else>
								<u-icon name="/static/praise.png" size="18" color="#ccc" style="opacity: 0.5;"></u-icon>
							</template>
							<view v-if="item.notesLikeNum>0"
								style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
								{{item.notesLikeNum}}
							</view>
							<view v-else style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
								{{'赞'}}
							</view>
						</view>
					</view>
					<view v-else style="display: flex;position: relative;padding: 20rpx;">
						<view style="margin-right: auto;color: #c8c9cc;font-size: 23rpx;">{{item.updateTime}}</view>
						<u-icon name="trash" @click="deleteDraft(item.id,1)" color="#c8c9cc" size="18"></u-icon>
					</view>
				</view>
			</block>
		</view>
	</view>
</template>

<script>
	import {
		praiseOrCancelNotes
	} from '@/apis/notes_service.js'
	import {
		reportNoteBehaviorBatch
	} from '@/apis/note_behavior_service.js'
	import {
		pxToRpx
	} from '@/utils/util.js'
	export default {
		name: "water-fall",
		data() {
			return {
				leftList: [],
				rightList: [],
				leftHeight: 0,
				rightHeight: 0,
				userId: uni.getStorageSync('userInfo').id,
				// 🔧 新增：防止重复初始化的标志
				isInitializing: false,
				// 曝光上报（单次去重 + 批量）
				exposureReportedMap: {},
				exposurePendingQueue: [],
				exposureTimer: null
			};
		},
		props: {
			list: {
				type: Array,
				default: () => []
			},
			slot_bottom: {
				type: Boolean,
				default: true
			},
			showViews: {
				type: Boolean,
				default: false
			},
			type: {
				type: Number,
				default: 1
			},
			// 是否使用自定义跳转（emit事件而不是内部跳转）
			emitDetail: {
				type: Boolean,
				default: false
			},
			// 额外的上下文数据，用于传递给父组件
			context: {
				type: Object,
				default: () => ({})
			},
			// 是否显示置顶标签
			showPinned: {
				type: Boolean,
				default: true
			},
			// 是否显示位置信息
			showLocation: {
				type: Boolean,
				default: false
			},
			// 是否开启曝光埋点
			enableExposureReport: {
				type: Boolean,
				default: false
			},
			// 曝光场景
			exposureScene: {
				type: String,
				default: 'other'
			}
		},
		computed: {
			// 根据平台设置不同的图片最大高度
			imageStyle() {
				// #ifdef H5
				// H5 端：rpx 会被转换为 px，需要根据屏幕宽度计算
				// 假设屏幕宽度 375px，750rpx = 375px，500rpx = 250px
				// 但为了保持视觉效果一致，使用更大的值
				const windowWidth = uni.getSystemInfoSync().windowWidth || 375
				const maxHeight = (500 / 750) * windowWidth // 将 500rpx 转换为对应的 px
				return {
					'max-height': maxHeight + 'px',
					'overflow': 'hidden'
				}
				// #endif
				// #ifdef MP-WEIXIN
				// 微信小程序端：使用 rpx 单位
				return {
					'max-height': '500rpx',
					'overflow': 'hidden'
				}
				// #endif
				// 默认
				return {
					'max-height': '500rpx',
					'overflow': 'hidden'
				}
			}
		},
		mounted() {
			this.init();
		},
		watch: {
			list: {
				handler(newList, oldList) {
					console.log('瀑布流list变化:', 'old:', oldList?.length, 'new:', newList?.length);

					// 🔧 修复：防止微信小程序端重复初始化
					// 如果新列表为空，清空所有数据
					if (!newList || newList.length === 0) {
						console.log('新列表为空，清空瀑布流');
						this.clear();
						return;
					}

					// 🔧 修复：更准确地检测数据完全替换的情况
					// 只有当旧列表的所有ID都不在新列表中时，才是真正的数据替换
					let isDataReplaced = false;
					if (oldList && oldList.length > 0 && newList.length > 0) {
						const getItemId = (item) => {
							return item?.id || item?.noteId || item?.productId || null;
						};
						
						const oldIds = new Set(oldList.map(item => getItemId(item)).filter(id => id !== null));
						const newIds = new Set(newList.map(item => getItemId(item)).filter(id => id !== null));
						
						// 检查旧列表的所有ID是否都不在新列表中
						let allOldIdsNotInNew = true;
						for (let id of oldIds) {
							if (newIds.has(id)) {
								allOldIdsNotInNew = false;
								break;
							}
						}
						
						// 只有当旧列表的所有ID都不在新列表中时，才是真正的数据替换
						isDataReplaced = allOldIdsNotInNew && oldIds.size > 0;
					}
					
					// 🔧 修复：更严格的条件判断，避免重复初始化
					// 只有在真正需要时才重新初始化
					if (((!oldList || oldList.length === 0) || isDataReplaced) && newList.length > 0 && !this.isInitializing) {
						console.log(isDataReplaced ? '检测到数据完全替换，重新初始化' : '从空列表到有数据，重新初始化', {
							oldLength: oldList?.length,
							newLength: newList?.length,
							isDataReplaced
						});

						// 🔧 修复：先清空现有数据，确保不会重复
						this.leftList = []
						this.rightList = []
						this.leftHeight = 0
						this.rightHeight = 0
						
						// 🔧 修复：重置初始化状态，确保可以重新初始化
						this.isInitializing = false;

						// 🔧 修复：使用nextTick确保DOM更新完成后再初始化
						this.$nextTick(() => {
							// 🔧 修复：再次检查，防止在nextTick期间list又被更新
							if (this.list && this.list.length > 0 && !this.isInitializing) {
								this.init();
							}
						});
					} else if (oldList && oldList.length > 0 && newList.length > 0 && !isDataReplaced) {
						// 🔧 修复：更准确地检测数据追加（而不是替换）
						// 先检查旧列表的所有ID是否都在新列表中，如果是且新列表更长，说明是追加
						const getItemId = (item) => {
							return item?.id || item?.noteId || item?.productId || null;
						};
						
						const oldIds = new Set(oldList.map(item => getItemId(item)).filter(id => id !== null));
						const newIds = new Set(newList.map(item => getItemId(item)).filter(id => id !== null));
						
						// 检查旧列表的所有ID是否都在新列表中
						let allOldIdsInNew = true;
						for (let id of oldIds) {
							if (!newIds.has(id)) {
								allOldIdsInNew = false;
								break;
							}
						}
						
						// 如果旧列表的所有ID都在新列表中，且新列表更长，说明是追加
						const isAppended = allOldIdsInNew && newList.length > oldList.length;
						
						// 🔧 修复：如果长度相同且所有ID都匹配，说明只是状态更新，不需要重新初始化
						if (oldList.length === newList.length && allOldIdsInNew && oldIds.size === newIds.size) {
							console.log('数据长度相同且ID匹配，只同步状态更新，不重新初始化', {
								oldLength: oldList.length,
								newLength: newList.length,
								oldIdsCount: oldIds.size,
								newIdsCount: newIds.size
							});
							// 跳过处理，避免不必要的重新初始化
							return;
						}
						
						if (isAppended) {
							console.log('检测到数据追加，自动调用 addList 处理新数据');
							// 计算新增的数据
							const newItems = newList.filter(item => {
								const itemId = getItemId(item);
								return itemId && !oldIds.has(itemId);
							});
							if (newItems.length > 0) {
								console.log('自动添加新数据:', newItems.length, '条');
								this.addList(newItems);
							} else {
								console.log('没有新数据需要添加');
							}
						} else if (oldList.length === newList.length && allOldIdsInNew && oldIds.size === newIds.size) {
							// 长度相同且所有ID都匹配，说明只是状态更新，不需要重新初始化
							console.log('数据长度相同且ID匹配，只同步状态更新，不重新初始化');
							// 跳过处理，避免不必要的重新初始化
						} else {
							console.log('列表已有数据，但数据不是追加，可能是替换，跳过处理（避免误判）');
						}
					} else if (this.isInitializing) {
						console.log('⚠️ 正在初始化中，跳过watch处理');
					}
					// 如果两个列表都有数据，说明是追加数据，自动调用 addList 处理
				},
				immediate: false
			}
		},
		beforeDestroy() {
			if (this.exposureTimer) {
				clearTimeout(this.exposureTimer);
				this.exposureTimer = null;
			}
		},
		methods: {
			onCoverLoad(item) {
				this.collectExposure(item);
			},
			collectExposure(item) {
				if (!this.enableExposureReport) return;
				const noteId = item && (item.id || item.noteId || item.nid);
				if (!noteId) return;
				const key = String(noteId);
				if (this.exposureReportedMap[key]) return;
				this.$set(this.exposureReportedMap, key, true);
				this.exposurePendingQueue.push({
					eventType: 'exposure',
					nid: key,
					scene: this.exposureScene || 'other'
				});
				this.scheduleExposureReport();
			},
			scheduleExposureReport() {
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
			async init() {
				console.log('瀑布流init开始，当前list长度:', this.list?.length)

				// 🔧 修复：防止重复初始化
				if (this.isInitializing) {
					console.log('瀑布流正在初始化中，跳过重复初始化');
					return;
				}

				// 🔧 修复：设置初始化标志
				this.isInitializing = true;

				// 🔧 修复：不在这里清空数据，避免与外部调用冲突
				// 数据清空应该由外部调用者控制

				// 如果没有数据，直接返回
				if (!this.list || this.list.length === 0) {
					console.log('瀑布流init：没有数据，直接返回')
					this.isInitializing = false;
					return
				}

				try {
					// 🔧 修复：先清空左右列，确保重新初始化
					this.leftList = [];
					this.rightList = [];
					this.leftHeight = 0;
					this.rightHeight = 0;

					// 分批处理，避免阻塞主线程
					const batchSize = 5
					for (let i = 0; i < this.list.length; i += batchSize) {
						const batch = this.list.slice(i, i + batchSize)
						console.log(`🔍 处理批次 ${Math.floor(i / batchSize) + 1}，数据量: ${batch.length}`)
						await this.processBatch(batch)

						// 让出主线程，避免阻塞
						await this.yieldToMain()
					}

					console.log('瀑布流init完成，左列:', this.leftList.length, '右列:', this.rightList.length)
					console.log('🔍 瀑布流init完成 - leftList前3项:', this.leftList.slice(0, 3))
					console.log('🔍 瀑布流init完成 - rightList前3项:', this.rightList.slice(0, 3))
				} catch (error) {
					console.error('瀑布流初始化失败:', error);
					console.error('🔍 初始化失败时的list:', this.list);
				} finally {
					this.isInitializing = false;
				}
			},

			// 处理一批数据
			async processBatch(batch) {
				const imagePromises = batch.map(item =>
					this.getImageHeight(item.coverPicture).then(res => ({
						...item,
						coverPicture: res.path,
						height: res.height
					})).catch(err => {
						// 🔧 修复：如果获取图片信息失败，使用默认值
						console.warn('获取图片信息失败，使用默认值:', item.coverPicture, err);
						return {
							...item,
							coverPicture: item.coverPicture || '',
							height: 300 // 默认高度
						}
					})
				)

				const items = await Promise.all(imagePromises)

				// 批量分配到左右列
				items.forEach(item => {
					const itemHeight = item.height || 300

					if (this.leftHeight <= this.rightHeight) {
						this.leftList.push(item)
						this.leftHeight += itemHeight
					} else {
						this.rightList.push(item)
						this.rightHeight += itemHeight
					}
				})
			},

			// 让出主线程
			yieldToMain() {
				return new Promise(resolve => {
					setTimeout(resolve, 0)
				})
			},
			getImageHeight(s) {
				return new Promise((resolve, reject) => {
					// 🔧 修复：如果图片URL为空或无效，直接返回默认值
					if (!s || s.trim() === '') {
						console.warn('图片URL为空，使用默认值');
						resolve({
							height: 300,
							path: ''
						});
						return;
					}

					uni.getImageInfo({
						src: s,
						success: (res) => {
							let imageHeight = pxToRpx(res.height);
							let imageWidth = pxToRpx(res.width);
							const width = 360;
							const maxHeight = 500;
							let height = width * imageHeight / imageWidth;
							if (height > maxHeight) {
								height = maxHeight;
							}
							let obj = {
								height: height,
								path: res.path
							}
							resolve(obj)
						},
						fail: (err) => {
							// 🔧 修复：获取图片信息失败时，返回默认值而不是reject
							console.warn('获取图片信息失败:', s, err);
							resolve({
								height: 300,
								path: s // 使用原始URL
							});
						}
					})
				})
			},
			praiseNotes(id, targetUserId, type, index) {
				console.log(' 点赞方法被调用:', {
					id,
					targetUserId,
					type,
					index
				});

				// 检查审核状态，只有审核通过的笔记才能点赞
				const item = type === 1 ? this.leftList[index] : this.rightList[index];
				console.log(' 笔记信息:', item);

				if (item && item.auditStatus !== '1') {
					console.log(' 笔记未通过审核，审核状态:', item.auditStatus);
					uni.showToast({
						title: '该笔记未通过审核，无法点赞',
						icon: 'none',
						duration: 2000
					});
					return;
				}

				praiseOrCancelNotes({
					notesId: id,
					userId: uni.getStorageSync('userInfo').id,
					targetUserId: targetUserId
				}).then(res => {
					console.log(res)
					if (res.code == 200) {
						if (type === 1) {
							if (this.leftList[index].isLike) {
								this.leftList[index].notesLikeNum = this.leftList[index].notesLikeNum - 1
								this.leftList[index].isLike = false
							} else {
								this.leftList[index].notesLikeNum = this.leftList[index].notesLikeNum + 1
								this.leftList[index].isLike = true
							}
						} else {
							if (this.rightList[index].isLike) {
								this.rightList[index].notesLikeNum = this.rightList[index].notesLikeNum - 1
								this.rightList[index].isLike = false
							} else {
								this.rightList[index].notesLikeNum = this.rightList[index].notesLikeNum + 1
								this.rightList[index].isLike = true
							}
						}
					}
				}).catch(error => {
					console.error('点赞失败:', error);
					uni.showToast({
						title: '点赞失败',
						icon: 'none'
					});
				});
			},
			async addList(e) {
				console.log('添加新数据到瀑布流:', e?.length)

				// 🔧 修复：统一ID获取方法，兼容不同的ID字段名
				const getItemId = (item) => {
					return item?.id || item?.noteId || item?.productId || null;
				};

				// 🔧 修复：只基于 leftList 和 rightList 去重
				// 因为 this.list 是数据源，它已经包含了所有数据（包括新增的）
				// leftList 和 rightList 是显示用的，它们可能还没有包含新增的数据
				const existingIds = new Set([
					...this.leftList.map(item => getItemId(item)).filter(id => id !== null),
					...this.rightList.map(item => getItemId(item)).filter(id => id !== null)
				]);

				const newItems = e.filter(item => {
					const itemId = getItemId(item);
					return itemId && !existingIds.has(itemId);
				});
				console.log('去重后的新数据:', newItems?.length, '原始数据:', e?.length, '显示中已存在:', existingIds.size, 'leftList:', this.leftList.length, 'rightList:', this.rightList.length)

				if (newItems.length === 0) {
					console.log('没有新数据需要添加')
					return
				}

				// 分批处理新数据，避免阻塞
				const batchSize = 3
				for (let i = 0; i < newItems.length; i += batchSize) {
					const batch = newItems.slice(i, i + batchSize)
					await this.processBatch(batch)

					// 让出主线程
					await this.yieldToMain()
				}

				console.log('addList完成，左列:', this.leftList.length, '右列:', this.rightList.length)
			},
			refresh() {
				this.leftList = []
				this.rightList = []
				this.leftHeight = 0
				this.rightHeight = 0
				this.init()
			},
			clear() {
				console.log('瀑布流clear执行');
				this.leftList = []
				this.rightList = []
				this.leftHeight = 0
				this.rightHeight = 0
				// 🔧 修复：重置初始化状态
				this.isInitializing = false
			},

			// 🔧 新增：强制重置初始化状态
			resetInitializing() {
				console.log('瀑布流强制重置初始化状态')
				this.isInitializing = false
			},
			// 🔧 新增：检测数据是否是追加（而不是替换）
			isDataAppended(oldList, newList) {
				if (!oldList || oldList.length === 0) {
					return false;
				}
				
				// 🔧 修复：统一ID获取方法，兼容不同的ID字段名
				const getItemId = (item) => {
					return item?.id || item?.noteId || item?.productId || null;
				};
				
				// 如果新列表包含所有旧列表的数据，且新列表更长，说明是追加
				const oldIds = new Set(oldList.map(item => getItemId(item)).filter(id => id !== null));
				const newIds = new Set(newList.map(item => getItemId(item)).filter(id => id !== null));
				
				// 检查旧列表的所有ID是否都在新列表中
				for (let id of oldIds) {
					if (!newIds.has(id)) {
						return false; // 有旧数据不在新列表中，说明是替换
					}
				}
				// 如果新列表比旧列表长，说明是追加
				return newList.length > oldList.length;
			},
			deleteDraft(id, num) {
				this.$showModal({
					title: "提示",
					content: "确认删除该草稿吗？",
					align: "left", // 对齐方式 left/center/right
					cancelText: "取消", // 取消按钮的文字
					cancelColor: "#FF2442", // 取消按钮颜色
					confirmText: "确定", // 确认按钮文字
					confirmColor: "#FF2442", // 确认按钮颜色 
					showCancel: true, // 是否显示取消按钮，默认为 true
				}).then(res => {
					console.log(id)
					let sql = `delete from draft_notes where id = ${id}`
					this.$sqliteUtil.SqlExecute(sql).then(res => {
						if (num == 0) {
							this.leftList = this.leftList.filter(item => item.id != id)
						} else {
							this.rightList = this.rightList.filter(item => item.id != id)
						}
					})
				})
			},
			goToDetail(id, type) {
				// 检查审核状态
				const item = this.leftList.find(i => i.id === id) || this.rightList.find(i => i.id === id)
				if (item && item.auditStatus === '0') {
					uni.showToast({
						title: '内容审核中，暂无法查看',
						icon: 'none'
					})
					return
				}
				if (item && item.auditStatus === '2') {
					uni.showToast({
						title: '内容未通过审核',
						icon: 'none'
					})
					return
				}

				// 如果启用了自定义跳转，emit事件给父组件处理
				if (this.emitDetail) {
					this.$emit('goToDetail', item, this.context);
					return;
				}

				// 当作为闲置商品瀑布流使用时（type prop 为 0），跳转到商品详情
				if (this.type === 0) {
					const productType = item && item.productType
					if (Number(productType) === 2) {
						uni.navigateTo({
							url: '/pkg-detail/pages/idleDetail/idleVideoD?productId=' + id
						})
					} else {
						uni.navigateTo({
							url: '/pkg-detail/pages/idleDetail/idleDetail?productId=' + id
						})
					}
					return
				}

				if (!this.slot_bottom) {
					// 草稿
					uni.navigateTo({
						url: '/pkg-publish/pages/publishNotes/publishNotes?update=1&tableId=' + id
					})
				} else {
					// 笔记
					if (type == 1) {
						uni.navigateTo({
							url: '/pkg-detail/pages/notesDetail/notesDetail?notesId=' + id
						})
					} else if (type == 2) {
						uni.navigateTo({
							url: '/pkg-detail/pages/notesDetail/noteVideoD?notesId=' + id
						})
					} else if (type == 3) {
						// Live 图使用图文详情页展示
						uni.navigateTo({
							url: '/pkg-detail/pages/notesDetail/notesDetail?notesId=' + id
						})
					}
				}
			},
			editNote(noteId) {
				// 跳转到编辑笔记页面
				uni.navigateTo({
					url: `/pkg-publish/pages/publishNotes/publishNotes?noteId=${noteId}&mode=edit`
				})
			},
			// 格式化地址显示（优先显示市+详细地址，如果没有详细地址则显示省+市+区）
			formatAddress(item) {
				if (!item) return '';

				// 优先：市+详细地址
				if (item.city && item.address) {
					const address = item.city + item.address;
					return address.length > 12 ? address.substring(0, 12) + '...' : address;
				}

				// 如果只有市，没有详细地址，也显示
				if (item.city && !item.address) {
					return item.city.length > 12 ? item.city.substring(0, 12) + '...' : item.city;
				}

				// 如果address字段包含完整地址（可能包含省+市+详细地址），直接使用
				if (item.address) {
					let address = item.address.trim();

					// 处理常见的地址格式
					// 1. "北京市" -> "北京"
					if (address === '北京市') {
						address = '北京';
					}
					// 2. "四川省 成都市 详细地址" -> "成都 详细地址"
					else if (address.includes('四川省 成都市')) {
						address = address.replace(/^四川省\s+成都市\s+/, '成都 ');
					}
					// 3. "北京市 北京市 详细地址" -> "北京 详细地址"
					else if (address.includes('北京市 北京市')) {
						address = address.replace(/^北京市\s+北京市\s+/, '北京 ');
					}
					// 4. 其他省市的处理：提取市名（去掉"市"后缀）
					else {
						// 尝试匹配 "省名 市名 详细地址" 格式
						const provinceCityMatch = address.match(/^(.+?省)\s+(.+?市)\s+(.+)$/);
						if (provinceCityMatch) {
							const city = provinceCityMatch[2].replace(/市$/, ''); // 去掉"市"后缀
							const detail = provinceCityMatch[3];
							address = city + ' ' + detail;
						}
						// 如果只是 "市名" 格式，去掉"市"后缀
						else if (address.match(/^.+?市$/)) {
							address = address.replace(/市$/, '');
						}
					}

					return address.length > 12 ? address.substring(0, 12) + '...' : address;
				}

				// 备选1：省+市+区（如果没有详细地址）
				if (item.province || item.city || item.district) {
					const parts = [];
					if (item.province) parts.push(item.province);
					if (item.city) parts.push(item.city);
					if (item.district) parts.push(item.district);
					if (parts.length > 0) {
						const address = parts.join('');
						return address.length > 12 ? address.substring(0, 12) + '...' : address;
					}
				}

				// 兼容旧数据：如果没有新字段但有location字段
				if (item.location) {
					const location = item.location;
					return location.length > 12 ? location.substring(0, 12) + '...' : location;
				}

				return '';
			}
		},
	}
</script>

<style lang="scss">
	.water-left,
	.water-right {
		width: 48%;
		margin: 10rpx auto;
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

	.top-wrapper {
		position: absolute;
		left: 5px;
		top: 5px;
		z-index: 4;
		background: #3d8af5;
		border-radius: 999px;
		font-weight: 500;
		color: #fff;
		line-height: 120%;
		font-size: 12px;
		padding: 5px 8px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.look-views {
		display: flex;
		position: absolute;
		bottom: 5rpx;
		left: 8rpx;
		color: #ffffff;
		background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 3rpx 10rpx;
		border-radius: 50rpx;
		font-size: 22rpx;
		z-index: 2;
	}

	/* 审核状态样式 */
	.image-container {
		position: relative;
		display: inline-block;
		width: 100%;
		overflow: hidden;
	}

	/* #ifdef H5 */
	.image-container ::v-deep .u-image {
		max-height: inherit !important;
	}

	.image-container ::v-deep .u-image__image {
		max-height: inherit !important;
		object-fit: cover;
	}

	/* #endif */

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

	.video-play {
		position: absolute;
		top: 5rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 10rpx;
		border-radius: 50%;
	}
	
	.live-badge {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		// background-color: rgba(123, 124, 125, 0.6);
		// filter: brightness(65%);
		padding: 10rpx;
		border-radius: 50%;
	}

	// .live-badge {
	// 	position: absolute;
	// 	top: 10rpx;
	// 	right: 10rpx;
	// 	background-color: #3d8af5;
	// 	color: #ffffff;
	// 	font-size: 24rpx;
	// 	padding: 8rpx 16rpx;
	// 	border-radius: 24rpx;
	// 	z-index: 2;

	// 	text {
	// 		color: #ffffff;
	// 		font-size: 24rpx;
	// 	}
	// }
</style>