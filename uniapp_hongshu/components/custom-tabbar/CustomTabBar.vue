<template>
	<view class="tabbar-root">
		<view class="tabbar">
			<view v-for="(item, index) in tabList" :key="index" class="tabbar-item"
				:class="{ active: current === index }" @click="handleClick(item, index)">
				<!-- 中间发布按钮 -->
				<view v-if="item.midButton" class="mid-button-wrapper" @click.stop="onMidButton">
					<view class="mid-button">
						<image class="mid-icon" src="/static/icons/tab-plus.svg" mode="aspectFit" />
					</view>
				</view>
				<!-- 普通 Tab -->
				<view v-else class="tab-content">
					<view class="tab-icon-wrapper">
						<image
							class="tab-icon"
							:src="current === index ? item.activeIcon : item.icon"
							mode="aspectFit"
						/>
						<!-- 徽标 -->
						<view v-if="Number(item.badge) > 0" class="tabbar-badge">
							{{ item.badgeText || (Number(item.badge) > 99 ? '99+' : String(Number(item.badge))) }}
						</view>
					</view>
					<text :class="['tab-label', current === index ? 'tab-label-active' : '']">{{ item.text }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		props: {
			current: {
				type: Number,
				default: 0
			}
		},
		data() {
			return {
				tabList: [{
						pagePath: 'pages/index/index',
						text: '首页',
						icon: '/static/icons/tab-home.svg',
						activeIcon: '/static/icons/tab-home-active.svg'
					},
					{
						pagePath: 'pkg-main/pages/idle/idle',
						text: '市集',
						icon: '/static/icons/tab-bag.svg',
						activeIcon: '/static/icons/tab-bag-active.svg'
					},
					{
						midButton: true
					},
					{
						pagePath: 'pkg-main/pages/message/message',
						text: '消息',
						icon: '/static/icons/tab-msg.svg',
						activeIcon: '/static/icons/tab-msg-active.svg',
						badge: 0
					},
					{
						pagePath: 'pkg-main/pages/mine/mine',
						text: '我',
						icon: '/static/icons/tab-me.svg',
						activeIcon: '/static/icons/tab-me-active.svg'
					}
				],
				midButton: {
					iconPath: '/static/icons/tab-plus.svg'
				},
				isSelecting: false,
				selectedCategory: null
			};
		},
		mounted() {
			// 首次挂载时读取本地缓存的未读数，避免切换页面后徽标丢失
			try {
				const cached = uni.getStorageSync('im_total_unread') || 0
				this.setMessageBadge(cached)
			} catch (e) {}
			// 直接在组件内监听全局事件，保证任意页面切换后也能及时更新
			uni.$off('im:setTabBadge')
			uni.$on('im:setTabBadge', (count) => {
				this.setMessageBadge(count)
			})

			// 监听徽章更新事件
			uni.$off('updateTabBarBadge')
			uni.$on('updateTabBarBadge', (data) => {
				this.updateTabBarBadge(data.index, data.badge)
			})
		},
		methods: {
			handleClick(item, index) {
				if (item.midButton) return;

				// 自定义 tabBar 使用 reLaunch
				uni.reLaunch({
					url: '/' + item.pagePath
				});
				this.$emit('update:current', index);
			},

			// 外部调用：设置消息 tab 的徽标
			setMessageBadge(count) {
				const msgIndex = this.tabList.findIndex(i => i.pagePath === 'pkg-main/pages/message/message')
				if (msgIndex !== -1) {
					const num = typeof count === 'string' ? (parseInt(count, 10) || 0) : (Number(count) || 0)
					const text = num > 99 ? '99+' : String(num)
					if (this.$set) {
						this.$set(this.tabList[msgIndex], 'badge', num)
						this.$set(this.tabList[msgIndex], 'badgeText', text)
					} else {
						this.tabList[msgIndex].badge = num
						this.tabList[msgIndex].badgeText = text
					}
				}
			},

			// 通用方法：更新指定索引的 TabBar 徽章
			updateTabBarBadge(index, badge) {
				if (index >= 0 && index < this.tabList.length) {
					const item = this.tabList[index]
					if (item && !item.midButton) {
						const num = typeof badge === 'string' ? (parseInt(badge, 10) || 0) : (Number(badge) || 0)
						const text = (typeof badge === 'string' && badge.indexOf('+') !== -1) ? badge : (num > 99 ? '99+' :
							String(num))
						if (this.$set) {
							this.$set(item, 'badge', num)
							this.$set(item, 'badgeText', text)
						} else {
							item.badge = num
							item.badgeText = text
						}
					}
				}
			},

			async onMidButton() {
				if (this.isSelecting) return;
				this.isSelecting = true;
				try {
					await this.showCategorySelection();
				} finally {
					this.isSelecting = false;
				}
			},

			// 一级选择：发布笔记/闲置
			async showCategorySelection() {
				try {
					const res = await this.showActionSheet(['发布笔记', '发布闲置']);
					this.selectedCategory = res.tapIndex === 0 ? 'note' : 'idle';

					// H5 端修复：延迟 300ms，防止误触二级选项
					if (process.env.UNI_PLATFORM === 'h5') {
						await new Promise(resolve => setTimeout(resolve, 300));
					}

					await this.showTypeSelection();
				} catch (err) {
					console.log('用户取消选择分类', err);
					this.selectedCategory = null;
				}
			},

			// 二级选择：图片/视频/Live图
			async showTypeSelection() {
				try {
					const res = await this.showActionSheet(['图片', '视频', 'Live图']);
					let type;
					if (res.tapIndex === 0) {
						type = 'image';
					} else if (res.tapIndex === 1) {
						type = 'video';
					} else if (res.tapIndex === 2) {
						type = 'live';
					}
					setTimeout(() => {
						this.handlePublish(this.selectedCategory, type);
					}, 200);
				} catch (err) {
					console.error('showTypeSelection 出错:', err);
				}
			},

			// 封装 uni.showActionSheet 为 Promise
			showActionSheet(itemList) {
				return new Promise((resolve, reject) => {
					uni.showActionSheet({
						itemList,
						success: resolve,
						fail: (err) => {
							if (process.env.UNI_PLATFORM === 'h5') {
								setTimeout(() => reject(new Error('用户取消')), 300);
							} else {
								reject(err);
							}
						}
					});
				});
			},

			// 处理发布逻辑
			handlePublish(category, type) {
				let page = category === 'note' ? '/pkg-publish/pages/publishNotes/publishNotes' :
					'/pkg-publish/pages/publishIdle/publishIdle';

				if (type === 'image') {
					uni.chooseImage({
						count: 9,
						sizeType: ['original', 'compressed'],
						sourceType: ['album', 'camera'],
						success: (res) => {
							this.navigateToPublishPage(page, '1', res.tempFilePaths);
						},
						fail: (err) => {
							if (!/cancel/.test(err?.errMsg || '')) {
								console.error('选择图片失败:', err);
							}
						}
					});
			} else if (type === 'video') {
				uni.chooseVideo({
					sourceType: ['album', 'camera'],
					compressed: true,
					maxDuration: 60,
					success: (res) => {
						const maxSize = 20 * 1024 * 1024;
						if (res.size > maxSize) {
							uni.showToast({
								title: '视频大小不能超过20M',
								icon: 'none',
								duration: 2000
							});
							return;
						}

						// #ifdef MP-WEIXIN
						if (res.thumbTempFilePath) {
							this.navigateToPublishPage(page, '2', [res.tempFilePath], res.thumbTempFilePath);
						} else {
							this.navigateToPublishPage(page, '2', [res.tempFilePath]);
						}
						// #endif

						// #ifndef MP-WEIXIN
						this.navigateToPublishPage(page, '2', [res.tempFilePath]);
						// #endif
					},
					fail: (err) => {
						if (!/cancel/.test(err?.errMsg || '')) {
							console.error('选择视频失败:', err);
						}
					}
				});
			} else if (type === 'live') {
					// #ifdef APP-PLUS || MP-WEIXIN
					uni.chooseMedia({
						count: 1,
						mediaType: ['image', 'video'],
						sourceType: ['album', 'camera'],
						success: (res) => {
							const file = res.tempFiles[0];
							const isLivePhoto = (file.fileType === 'image' && file.duration && file.duration > 0);

							if (isLivePhoto || file.fileType === 'video') {
								const maxSize = 20 * 1024 * 1024;
								if (file.size > maxSize) {
									uni.showToast({
										title: '视频大小不能超过20M',
										icon: 'none',
										duration: 2000
									});
									return;
								}
							}

							if (isLivePhoto) {
								let videoPath = file.tempFilePath;
								if (videoPath.toLowerCase().endsWith('.jpg') || videoPath.toLowerCase().endsWith('.jpeg')) {
									videoPath = videoPath.replace(/\.(jpg|jpeg)$/i, '.MOV');
								}
								this.navigateToPublishPage(page, '2', [videoPath], file.tempFilePath);
							} else if (file.fileType === 'video') {
								this.navigateToPublishPage(page, '2', [file.tempFilePath]);
							} else {
								this.navigateToPublishPage(page, '3', [file.tempFilePath]);
							}
						},
						fail: (err) => {
							if (!/cancel/.test(err?.errMsg || '')) {
								console.error('选择Live图失败:', err);
							}
						}
					});
					// #endif

					// #ifdef H5
					uni.showToast({
						title: 'H5 暂不支持 Live 图，请选择图片或视频',
						icon: 'none',
						duration: 2500
					});
					// #endif
				}
			},

			// 封装跳转逻辑
			navigateToPublishPage(page, type, tempFilePaths, coverPicture = '') {
				const params = {
					update: 0,
					type: type
				};

				if (coverPicture) {
					params.coverPicture = coverPicture;
				}

				// #ifdef H5
				params.tempFilePaths = JSON.stringify(tempFilePaths);
				const queryString = Object.keys(params)
					.map(key => `${key}=${encodeURIComponent(params[key])}`)
					.join('&');
				uni.navigateTo({
					url: `${page}?${queryString}`
				});
				// #endif

				// #ifdef MP-WEIXIN
				uni.setStorageSync('publish_tempFilePaths', tempFilePaths);
				const verifyStored = uni.getStorageSync('publish_tempFilePaths');

				if (coverPicture) {
					uni.setStorageSync('publish_coverPicture', coverPicture);
				} else {
					uni.removeStorageSync('publish_coverPicture');
				}

				const queryString2 = Object.keys(params)
					.map(key => `${key}=${encodeURIComponent(params[key])}`)
					.join('&');

				uni.navigateTo({
					url: `${page}?${queryString2}`,
					success: (res) => {
						console.log('navigateTo 成功:', res);
					},
					fail: (err) => {
						console.error('navigateTo 失败:', err);
					}
				});
				// #endif

				// #ifdef APP-PLUS
				Promise.all(tempFilePaths.map(item =>
					new Promise((resolve) => {
						uni.saveFile({
							tempFilePath: item,
							success: resolve,
							fail: () => {
								resolve({ savedFilePath: item });
							}
						});
					})
				)).then(savedFiles => {
					const filePaths = savedFiles.map(file => file.savedFilePath);
					params.tempFilePaths = JSON.stringify(filePaths);

					const qs = Object.keys(params)
						.map(key => `${key}=${encodeURIComponent(params[key])}`)
						.join('&');

					uni.navigateTo({
						url: `${page}?${qs}`
					});
				});
				// #endif
			}
		}
	};
</script>

<style scoped>
	/* ─── 设计稿: AppTabBar ─── */
	/* position: absolute, left: 14, right: 14, bottom: 16, height: 60 */
	/* borderRadius: radius*1.4 ≈ 31, bg rgba(255,255,255,0.72), blur(24px) */
	.tabbar-root {
		position: fixed;
		left: 28rpx;
		right: 28rpx;
		bottom: 32rpx;
		z-index: 999;
		height: 120rpx;
		border-radius: 62rpx;
		background: rgba(255, 255, 255, 0.72);
		box-shadow: 0 16rpx 56rpx rgba(201, 123, 74, 0.13),
					0 4rpx 12rpx rgba(0, 0, 0, 0.05),
					inset 0 2rpx 0 rgba(255, 255, 255, 0.9);
		overflow: visible;

		/* #ifdef H5 || APP-PLUS */
		-webkit-backdrop-filter: blur(24px) saturate(180%);
		backdrop-filter: blur(24px) saturate(180%);
		/* #endif */
	}

	.tabbar {
		display: flex;
		flex-direction: row;
		justify-content: space-around;
		align-items: center;
		height: 120rpx;
		position: relative;
		background: transparent;
		padding: 0 16rpx;
	}

	.tabbar-item {
		flex: 1;
		text-align: center;
		height: 100%;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		position: relative;
	}

	/* ─── 普通 Tab 内容（图标 + 文字） ─── */
	.tab-content {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 4rpx;
	}

	.tab-icon-wrapper {
		position: relative;
		width: 44rpx;
		height: 44rpx;
	}

	.tab-icon {
		width: 44rpx;
		height: 44rpx;
	}

	/* 设计稿: fontSize 10, normal inkMuted, active primary+bold */
	.tab-label {
		font-size: 20rpx;
		color: #8F7260;
		font-weight: 500;
		transition: all 0.2s ease;
	}

	.tab-label-active {
		color: #C97B4A;
		font-weight: 700;
	}

	/* ─── 徽标（设计稿: minWidth 16, height 16, bg warn #D66A5E） ─── */
	.tabbar-badge {
		position: absolute;
		top: -8rpx;
		right: -16rpx;
		min-width: 32rpx;
		height: 32rpx;
		padding: 0 8rpx;
		border-radius: 16rpx;
		background: #D66A5E;
		color: #fff;
		font-size: 20rpx;
		font-weight: 700;
		line-height: 32rpx;
		text-align: center;
		box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.15);
	}

	/* ─── 中间发布按钮 ─── */
	/* 设计稿: 48x48, marginTop -18, borderRadius 16, gradient primary→primaryInk */
	.mid-button-wrapper {
		width: 100%;
		height: 100%;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.mid-button {
		width: 96rpx;
		height: 96rpx;
		margin-top: -36rpx;
		border-radius: 32rpx;
		background: linear-gradient(135deg, #C97B4A, #8A4A1F);
		box-shadow: 0 20rpx 44rpx rgba(201, 123, 74, 0.35),
					inset 0 2rpx 0 rgba(255, 255, 255, 0.4);
		display: flex;
		align-items: center;
		justify-content: center;
		transition: transform 0.2s ease;
	}

	.mid-button:active {
		transform: scale(0.92);
	}

	.mid-icon {
		width: 48rpx;
		height: 48rpx;
	}
</style>
