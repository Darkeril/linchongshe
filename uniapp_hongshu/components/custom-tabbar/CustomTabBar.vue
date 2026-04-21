<template>
	<view class="tabbar-root">
		<view class="tabbar">
			<view v-for="(item, index) in tabList" :key="index" class="tabbar-item"
				:class="{ active: current === index }" @click="handleClick(item, index)">
				<view v-if="item.midButton" class="mid-button-wrapper" @click.stop="onMidButton">
					<image class="mid-icon" :src="midButton.iconPath" />
				</view>
				<view v-else>
					<text class="tabbar-text">{{ item.text }}</text>
					<view v-if="Number(item.badge) > 0" class="tabbar-badge">
						{{ item.badgeText || (Number(item.badge) > 99 ? '99+' : String(Number(item.badge))) }}</view>
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
						text: '首页'
					},
					{
						pagePath: 'pkg-main/pages/idle/idle',
						text: '市集'
					},
					{
						midButton: true
					},
					{
						pagePath: 'pkg-main/pages/message/message',
						text: '消息',
						badge: 0
					},
					{
						pagePath: 'pkg-main/pages/mine/mine',
						text: '我'
					}
				],
				midButton: {
					iconPath: '/static/image/publish.png'
				},
				isSelecting: false, // 防止重复点击
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
					if (item && !item.midButton) { // 中间按钮不显示徽章
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
				if (this.isSelecting) return; // 防止重复点击
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
					console.log('准备调用 handlePublish', {
						category: this.selectedCategory,
						type
					});
					// 延迟以避免在操作表关闭动画期间调用选择器被拦截（特别是 mp-weixin）
					setTimeout(() => {
						this.handlePublish(this.selectedCategory, type);
					}, 200);
				} catch (err) {
					console.error('showTypeSelection 出错:', err);
				}
			},

			// 封装 uni.showActionSheet 为 Promise，兼容 H5 取消
			showActionSheet(itemList) {
				return new Promise((resolve, reject) => {
					uni.showActionSheet({
						itemList,
						success: resolve,
						fail: (err) => {
							// H5 端可能不会触发 fail，手动检测
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
					maxDuration: 60, // 最长60秒
					success: (res) => {
						// 检查视频大小，不能超过20M
						const maxSize = 20 * 1024 * 1024; // 20M in bytes
						if (res.size > maxSize) {
							uni.showToast({
								title: '视频大小不能超过20M',
								icon: 'none',
								duration: 2000
							});
							return;
						}
						
						// #ifdef MP-WEIXIN
						// 微信小程序端：使用系统生成的缩略图作为封面
						if (res.thumbTempFilePath) {
							console.log('✅ 选择视频成功，使用系统缩略图:', res.thumbTempFilePath);
							this.navigateToPublishPage(page, '2', [res.tempFilePath], res.thumbTempFilePath);
						} else {
							console.log('⚠️ 未获取到系统缩略图，需手动添加封面');
							this.navigateToPublishPage(page, '2', [res.tempFilePath]);
						}
						// #endif
						
						// #ifndef MP-WEIXIN
						// 其他平台：跳转后自动提取视频帧作为封面
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
					// Live 图选择（支持 Live Photo、GIF、APNG、WebP 等）
					uni.chooseMedia({
						count: 1,
						mediaType: ['image', 'video'],
						sourceType: ['album', 'camera'],
						success: (res) => {
							const file = res.tempFiles[0];
							
							console.log('选择的文件信息:', {
								fileType: file.fileType,
								duration: file.duration,
								size: file.size,
								tempFilePath: file.tempFilePath
							});
							
							// 检测是否为 Live Photo
							const isLivePhoto = (file.fileType === 'image' && file.duration && file.duration > 0);
							
							// 如果是视频或 Live Photo，检查大小限制
							if (isLivePhoto || file.fileType === 'video') {
								const maxSize = 20 * 1024 * 1024; // 20M in bytes
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
								console.log('✅ 检测到 Live Photo，将作为视频笔记发布');
								// Live Photo：作为视频笔记
								let videoPath = file.tempFilePath;
								// iOS 系统规则：视频文件与图片同名，扩展名为 .MOV
								if (videoPath.toLowerCase().endsWith('.jpg') || videoPath.toLowerCase().endsWith('.jpeg')) {
									videoPath = videoPath.replace(/\.(jpg|jpeg)$/i, '.MOV');
								}
								// type=2 视频笔记，传递视频路径和封面图
								this.navigateToPublishPage(page, '2', [videoPath], file.tempFilePath);
							} else if (file.fileType === 'video') {
								console.log('普通视频');
								// 普通视频
								this.navigateToPublishPage(page, '2', [file.tempFilePath]);
							} else {
								console.log('普通图片/动图');
								// 普通图片/动图（GIF、APNG、WebP）
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
					// H5 暂不支持 Live 图，仅提示，不打开选图（与 uniapp_im 一致）
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
				
				// 如果有封面图（Live Photo 的 JPG 或微信小程序的系统缩略图），添加到参数中
				if (coverPicture) {
					params.coverPicture = coverPicture;
				}
				
				// #ifdef H5
				// H5端直接跳转，使用URL参数传递
				params.tempFilePaths = JSON.stringify(tempFilePaths);
				const queryString = Object.keys(params)
					.map(key => `${key}=${encodeURIComponent(params[key])}`)
					.join('&');
				uni.navigateTo({
					url: `${page}?${queryString}`
				});
				console.log('✅ H5端跳转到:', `${page}?${queryString}`);
				// #endif
				
				// #ifdef MP-WEIXIN
				// 微信小程序端：使用全局存储传递文件路径，避免URL长度限制
				// 存储文件路径和封面图到全局存储
				console.log('🔍 准备存储文件路径到全局存储，数量:', tempFilePaths.length);
				console.log('🔍 文件路径详情:', tempFilePaths);
				uni.setStorageSync('publish_tempFilePaths', tempFilePaths);
				// 验证存储是否成功
				const verifyStored = uni.getStorageSync('publish_tempFilePaths');
				console.log('🔍 验证存储结果:', verifyStored, '类型:', typeof verifyStored, '是否为数组:', Array.isArray(verifyStored));
				
				if (coverPicture) {
					uni.setStorageSync('publish_coverPicture', coverPicture);
					console.log('✅ 封面图已存储:', coverPicture);
				} else {
					uni.removeStorageSync('publish_coverPicture');
				}
				
				const queryString = Object.keys(params)
					.map(key => `${key}=${encodeURIComponent(params[key])}`)
					.join('&');
				
				console.log('🔍 准备跳转，URL参数:', queryString);
				console.log('🔍 准备调用 uni.navigateTo，URL:', `${page}?${queryString}`);
				uni.navigateTo({
					url: `${page}?${queryString}`,
					success: (res) => {
						console.log('✅ navigateTo 成功:', res);
					},
					fail: (err) => {
						console.error('❌ navigateTo 失败:', err);
					},
					complete: () => {
						console.log('🔍 navigateTo 完成');
					}
				});
				console.log('✅ 微信小程序端跳转到:', page, '参数:', params, '文件路径已存储到全局存储');
				// #endif
				
				// #ifdef APP-PLUS
				// APP端需要先保存文件，再跳转
				Promise.all(tempFilePaths.map(item =>
					new Promise((resolve) => {
						uni.saveFile({
							tempFilePath: item,
							success: resolve,
							fail: () => {
								// 保存失败，直接使用临时路径
								resolve({ savedFilePath: item });
							}
						});
					})
				)).then(savedFiles => {
					const filePaths = savedFiles.map(file => file.savedFilePath);
					params.tempFilePaths = JSON.stringify(filePaths);
					
					const queryString = Object.keys(params)
						.map(key => `${key}=${encodeURIComponent(params[key])}`)
						.join('&');
					
					uni.navigateTo({
						url: `${page}?${queryString}`
					});
					console.log('✅ APP端跳转到:', `${page}?${queryString}`);
				});
				// #endif
			}
		}
	};
</script>

<style scoped>
	.tabbar-root {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 20px;
		z-index: 999;
		/* iOS26 液态玻璃：统一变量（降级在不支持 backdrop-filter 的端仍可用） */
		background: var(--im-glass-bg-fallback, linear-gradient(135deg, rgba(255, 255, 255, 0.62), rgba(255, 255, 255, 0.28)));
		border: 0px solid var(--im-glass-border, rgba(255, 255, 255, 0.52));
		border-radius: 25px;
		margin: 0 20px;
		box-shadow: var(--im-glass-shadow, 0 12px 34px rgba(15, 23, 42, 0.12), inset 0 0 0 rgba(255, 255, 255, 0.72));
		overflow: hidden;

		/* #ifdef H5 || APP-PLUS */
		-webkit-backdrop-filter: blur(20px) saturate(185%);
		backdrop-filter: blur(20px) saturate(185%);
		background: var(--im-glass-bg, linear-gradient(135deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0.2)));
		/* #endif */
	}

	.tabbar-root::before {
		content: '';
		position: absolute;
		left: -16%;
		top: -130%;
		width: 60%;
		height: 260%;
		background: var(--im-glass-highlight, radial-gradient(circle, rgba(255, 255, 255, 0.42) 0%, rgba(255, 255, 255, 0) 72%));
		transform: rotate(16deg);
		pointer-events: none;
	}

	.tabbar-root::after {
		content: '';
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		height: 0;
		background: var(--im-glass-topline, linear-gradient(90deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0.85), rgba(255, 255, 255, 0.02)));
		pointer-events: none;
	}

	.tabbar {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		height: 55px;
		position: relative;
		background: transparent;
		border-radius: 25px;
		padding: 0 10px;
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
		/* 让徽标相对每个tab定位 */
	}

	.tabbar-text {
		font-size: 22px;
		color: var(--im-glass-text, rgba(34, 34, 34, 0.55));
		font-weight: normal;
		transition: all 0.2s ease;
	}

	.tabbar-badge {
		position: absolute;
		/* 让徽标贴在当前tab文字的右上角 */
		right: 22%;
		top: 2px;
		background: #ff4d4f;
		color: #fff;
		border-radius: 10px;
		padding: 0 6px;
		font-size: 10px;
		line-height: 18px;
		min-width: 18px;
		height: 18px;
		text-align: center;
	}

	.active .tabbar-text {
		color: var(--im-glass-text-active, rgba(9, 9, 11, 0.95));
		font-weight: bold;
		font-size: 24px;
		text-shadow: 0 1px 0 rgba(255, 255, 255, 0.55);
	}

	.mid-button-wrapper {
		width: 100%;
		height: 100%;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.mid-icon {
		width: 45px;
		height: 40px;
		border-radius: 10px;
		background: #3d8af5;
		/* box-shadow: 0 2px 8px rgba(255, 45, 45, 0.2); */
		border: 0 solid #fff;
		object-fit: contain;
		transition: all 0.3s ease;
		box-shadow: 0 10px 24px rgba(61, 138, 245, 0.22), inset 0 0 0 rgba(255, 255, 255, 0.65);
	}

	.mid-button-wrapper:active .mid-icon {
		transform: scale(0.95);
		background: #3d8af5;
	}
</style>