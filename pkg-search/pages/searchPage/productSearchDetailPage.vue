<template>
	<view>
		<!-- 顶部状态栏占位：微信小程序端使用系统导航栏，不需要额外占位，避免出现大块空白 -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}"
			style="position: fixed;top: 0;width: 100%;background-color: #fff;z-index: 9999;"></view>
		<view :style="{height: statusBarHeight + 'px'}"></view>
		<!-- #endif -->
		<view class="header-section">
			<view style="padding: 10rpx 30rpx 10rpx 15rpx;display: flex;height: 44px;align-items: center;">
				<!-- #ifndef MP-WEIXIN -->
				<u-icon @click="goToBack" name="arrow-left" size="25"></u-icon>
				<!-- #endif -->
				<view @click="goToSearchValue"
					style="background-color: #f2f2f2;padding: 10rpx 20rpx;color: #606266;display: flex;border-radius: 50rpx;flex: 1;margin-left: 20rpx;height: 64rpx;align-items: center;text-align: center;box-sizing: border-box;">
					<u-icon name="search" size="22" color="#909399"></u-icon>
					<view style="font-size: 30rpx;margin-left: 15rpx;">{{searchValue}}</view>
				</view>
			</view>
		</view>
		
		<!-- 商品瀑布流展示 - 参考集市页面设计 -->
		<scroll-view scroll-y :style="{height: scrollHeight + 'px'}" @scrolltolower="onReachBottom"
			:refresher-enabled="true" @refresherrefresh="onRefresh" :refresher-triggered="refreshing"
			:refresher-threshold="100">
			<view>
				<view style="width: 100%; display: flex; flex-wrap: wrap; margin-top: 5px;">
					<view class="water-left">
						<block v-for="(item,index) in productList.leftList" :key="index">
							<view class="product-card">
								<u--image @click="goToDetail(item.id, item.productType)" :src="item.coverPicture"
									:fade="false" :lazy-load="true" :webp="true" :show-menu-by-longpress="false"
									width="100%" height="auto" mode="widthFix"
									style="max-height: 500rpx;overflow: hidden;">
									<template v-slot:loading>
										<view style="height: 200rpx;text-align: center;padding: 20rpx;">
											<u-loading-icon color="#e83929"></u-loading-icon>
											<view style="font-size: 30rpx;">loading......</view>
										</view>
									</template>
								</u--image>
								
								<!-- 地理位置标签 -->
								<view class="location-tag" v-if="item.city">
									<u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
									<text style="margin-left: 5rpx;">{{item.city}}</text>
								</view>
								
								<!-- 视频播放标识 -->
								<view v-if="item.productType == 2" class="video-play">
									<u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
								</view>
								
								<!-- 商品标题 -->
								<view @click="goToDetail(item.id, item.productType)" class="product-title">{{item.title}}</view>
								
								<!-- 价格区域 -->
								<view class="goods-price-row">
									<view class="price-group">
										<text class="goods-price">¥{{ item.price }}</text>
										<text class="goods-origin-price" v-if="item.originPrice && item.originPrice !== item.price">¥{{ item.originPrice }}</text>
									</view>
									<view v-if="item.postType === 0" class="post-type-tag">包邮</view>
									<view v-else-if="item.postType === 1" class="post-type-tag">自提</view>
								</view>
								
								<!-- 用户信息区域 -->
								<view style="display: flex;position: relative;padding: 20rpx;">
									<image style="height: 22px;width: 22px;border-radius: 50%;"
										mode="aspectFill" @click="goToUser(item.belongUserId)"
										:src="item.avatarUrl" :lazy-load="true" :fade-show="true" 
										:webp="true" :show-menu-by-longpress="false">
									</image>
									<view class="user-name" @click="goToUser(item.belongUserId)">
										{{item.nickname}}
									</view>
									<view style="display: flex;position: absolute;right: 10rpx;">
										<u-transition :show="!item.isCollection" mode="fade" duration="2000">
											<u-icon v-if="!item.isCollection" name="star" size="18" color="#bbb"
												@click="toggleCollection(item.id, item.belongUserId, index, 'left')"></u-icon>
										</u-transition>
										<u-transition :show="item.isCollection" mode="fade" duration="2000">
											<u-icon v-if="item.isCollection" name="star-fill" size="18"
												color="#f7b500"
												@click="toggleCollection(item.id, item.belongUserId, index, 'left')"></u-icon>
										</u-transition>
										<view v-if="item.notesLikeNum > 0"
											style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
											{{item.notesLikeNum}}
										</view>
										<view v-else
											style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
											{{'想要'}}
										</view>
									</view>
								</view>
							</view>
						</block>
					</view>
					
					<view class="water-right">
						<block v-for="(item,index) in productList.rightList" :key="index">
							<view class="product-card">
								<u--image @click="goToDetail(item.id, item.productType)" :src="item.coverPicture"
									:fade="false" :lazy-load="true" :webp="true" :show-menu-by-longpress="false"
									width="100%" height="auto" mode="widthFix"
									style="max-height: 500rpx;overflow: hidden;">
									<template v-slot:loading>
										<view style="height: 200rpx;text-align: center;padding: 20rpx;">
											<u-loading-icon color="#e83929"></u-loading-icon>
											<view style="font-size: 30rpx;">loading......</view>
										</view>
									</template>
								</u--image>
								
								<!-- 地理位置标签 -->
								<view class="location-tag" v-if="item.city">
									<u-icon name="map" color="#ffffff" size="25rpx"></u-icon>
									<text style="margin-left: 5rpx;">{{item.city}}</text>
								</view>
								
								<!-- 视频播放标识 -->
								<view v-if="item.productType == 2" class="video-play">
									<u-icon name="play-right-fill" color="#ffffff" size="25rpx"></u-icon>
								</view>
								
								<!-- 商品标题 -->
								<view @click="goToDetail(item.id, item.productType)" class="product-title">{{item.title}}</view>
								
								<!-- 价格区域 -->
								<view class="goods-price-row">
									<view class="price-group">
										<text class="goods-price">¥{{ item.price }}</text>
										<text class="goods-origin-price" v-if="item.originPrice && item.originPrice !== item.price">¥{{ item.originPrice }}</text>
									</view>
									<view v-if="item.postType === 0" class="post-type-tag">包邮</view>
									<view v-else-if="item.postType === 1" class="post-type-tag">自提</view>
								</view>
								
								<!-- 用户信息区域 -->
								<view style="display: flex;position: relative;padding: 20rpx;">
									<image style="height: 22px;width: 22px;border-radius: 50%;"
										mode="aspectFill" @click="goToUser(item.belongUserId)"
										:src="item.avatarUrl" :lazy-load="true" :fade-show="true" 
										:webp="true" :show-menu-by-longpress="false">
									</image>
									<view class="user-name" @click="goToUser(item.belongUserId)">
										{{item.nickname}}
									</view>
									<view style="display: flex;position: absolute;right: 10rpx;">
										<u-transition :show="!item.isCollection" mode="fade" duration="2000">
											<u-icon v-if="!item.isCollection" name="star" size="18" color="#bbb"
												@click="toggleCollection(item.id, item.belongUserId, index, 'right')"></u-icon>
										</u-transition>
										<u-transition :show="item.isCollection" mode="fade" duration="2000">
											<u-icon v-if="item.isCollection" name="star-fill" size="18"
												color="#f7b500"
												@click="toggleCollection(item.id, item.belongUserId, index, 'right')"></u-icon>
										</u-transition>
										<view v-if="item.notesLikeNum > 0"
											style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
											{{item.notesLikeNum}}
										</view>
										<view v-else
											style="color: gray;font-size: 12px;line-height: 18px;margin-left: 3rpx;">
											{{'想要'}}
										</view>
									</view>
								</view>
							</view>
						</block>
					</view>
				</view>
				
				<!-- 加载更多 -->
				<u-loadmore v-if="productList.leftList.length > 0 || productList.rightList.length > 0" 
					margin-top="20" line :status="productList.status" 
					:loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText" />
			</view>
		</scroll-view>
		
		<!-- 空状态 -->
		<view v-if="isEmpty && productList.leftList.length === 0 && productList.rightList.length === 0" class="empty-state">
			<view class="empty-icon">🛍️</view>
			<view class="empty-text">没有找到相关商品</view>
			<view class="empty-tip">试试其他关键词吧</view>
		</view>
	</view>
</template>

<script>
	import {
		getProductByKeyword,
		praiseOrCancelIdles
	} from '@/apis/idles_service.js'
	import {
		pxToRpx
	} from '@/utils/util.js'
	
	export default {
		data() {
			return {
				statusBarHeight: 0,
				scrollHeight: 0,
				searchValue: '',
				refreshing: false,
				productList: {
					leftList: [],
					rightList: [],
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					pageSize: 6, // 参考集市页面，减少每页数量
				},
				loadingText: '加载中...',
				loadmoreText: '加载更多',
				nomoreText: '到底了',
				isEmpty: false
			};
		},
		methods: {
			// 商品数据映射 - 参考集市页面
			mapProductItem(item) {
				return {
					...item,
					id: String(item.id),
					coverPicture: item.cover || '',
					avatarUrl: item.avatar || '',
					nickname: item.username || '用户',
					notesLikeNum: parseInt(item.likeCount, 10) || 0,
					isCollection: item.isCollection || false,
					productType: parseInt(item.productType, 10) || 0,
					content: item.description || '',
					title: item.title || '商品标题',
					belongUserId: item.uid || item.belongUserId,
					noteCoverHeight: parseInt(item.noteCoverHeight || item.coverHeight, 10) || 0,
					imgList: item.imgUrls || [],
					province: item.province || '',
					city: item.city || '',
					address: item.address || '',
					price: item.price || '0',
					originPrice: item.originalPrice || '',
					postType: item.postType !== undefined ? item.postType : 0, // 0=邮寄，1=自提
				}
			},
			
			// 获取图片高度 - 参考集市页面优化
			getImageHeight(s) {
				return new Promise((resolve) => {
					// #ifdef MP-WEIXIN
					// 微信小程序环境，直接使用默认高度，避免网络请求
					console.warn('⚡ 微信小程序环境，直接使用默认高度，避免网络请求:', s);
					resolve({
						height: 200, // 默认高度
						path: s
					});
					return;
					// #endif
					
					// #ifndef MP-WEIXIN
					// H5环境：正常获取图片高度
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
						fail: (error) => {
							console.warn('⚠️ getImageInfo 失败，使用默认高度:', error, s);
							resolve({
								height: 200,
								path: s
							});
						}
					})
					// #endif
				})
			},
			
			// 瀑布流分配 - 参考集市页面优化算法
			async assignWaterfall(list) {
				try {
					// 优化：优先使用后端返回的图片高度，避免等待 getImageInfo
					const itemsWithHeight = [];
					const itemsWithoutHeight = [];
					
					list.forEach(item => {
						if (item.noteCoverHeight && item.noteCoverHeight > 0) {
							itemsWithHeight.push(item);
						} else {
							itemsWithoutHeight.push(item);
						}
					});
					
					// 先同步处理有高度的数据（秒开）
					const processedWithHeight = itemsWithHeight.map(item => ({
						...item,
						coverPicture: item.coverPicture,
						_imgHeight: pxToRpx(item.noteCoverHeight)
					}));
					
					// 批量添加到瀑布流（本地数组，减少 setData 次数）
					const newLeftList = [];
					const newRightList = [];
					let leftHeight = 0;
					let rightHeight = 0;
					
					processedWithHeight.forEach(item => {
						const height = item._imgHeight;
						if (leftHeight <= rightHeight) {
							newLeftList.push(item);
							leftHeight += (height + pxToRpx(50));
						} else {
							newRightList.push(item);
							rightHeight += (height + pxToRpx(50));
						}
					});
					
					// 再异步处理无高度的数据（兼容旧数据）
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
					this.productList.leftList = newLeftList;
					this.productList.rightList = newRightList;
					this.productList.leftHeight = leftHeight;
					this.productList.rightHeight = rightHeight;
				} catch (error) {
					console.error('瀑布流布局失败:', error);
				}
			},
			
			// 加载更多商品
			async getMoreProducts() {
				if (this.productList.status == 'nomore' || this.productList.status == 'loading') {
					return;
				}
				this.productList.status = 'loading';
				
				try {
					const res = await getProductByKeyword({
						keyword: this.searchValue,
						page: this.productList.page,
						pageSize: this.productList.pageSize
					});
					
					console.log('商品搜索结果:', res);
					if (res.code == 200) {
						const records = res.data.records.map(item => this.mapProductItem(item));
						
						// 使用优化的瀑布流分配
						await this.assignWaterfall(records);
						
						this.productList.page += 1;
						this.productList.status = res.data.records.length < this.productList.pageSize ? 'nomore' : 'loadmore';
						
						// 检查空状态
						if (this.productList.page === 2 && records.length === 0) {
							this.isEmpty = true;
						} else {
							this.isEmpty = false;
						}
					} else {
						this.productList.status = 'nomore';
						if (this.productList.page === 2) {
							this.isEmpty = true;
						}
						uni.showToast({
							title: res.message || '获取商品失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('商品搜索失败:', error);
					this.productList.status = 'nomore';
					if (this.productList.page === 2) {
						this.isEmpty = true;
					}
					uni.showToast({
						title: '搜索失败',
						icon: 'none'
					})
				}
			},
			
			// 跳转到商品详情 - 参考集市页面
			goToDetail(id, type) {
				console.log('🏪 商品搜索商品点击:', { id, type });
				
				// 根据商品类型跳转到对应的详情页
				if (type === 2) {
					// 视频商品
					console.log('📹 跳转到视频商品详情页');
					uni.navigateTo({
						url: '/pkg-detail/pages/idleDetail/idleVideoD?productId=' + id
					});
				} else {
					// 普通商品（包括 type === 0, 1 或其他值）
					console.log('📦 跳转到普通商品详情页');
					uni.navigateTo({
						url: '/pkg-detail/pages/idleDetail/idleDetail?productId=' + id
					});
				}
			},
			
			// 跳转到用户页面
			goToUser(id) {
				uni.navigateTo({
					url: '/pkg-mine/pages/mine/otherMine?userId=' + id
				})
			},
			
			// 收藏/取消收藏
			toggleCollection(id, targetUserId, index, colType) {
				console.log('⭐ 商品收藏操作:', { id, targetUserId, index, colType });
				
				praiseOrCancelIdles({
					notesId: id,
					userId: uni.getStorageSync('userInfo').id,
					targetUserId: targetUserId
				}).then(res => {
					console.log('⭐ 商品收藏响应:', res);
					if (res.code == 200) {
						// 更新收藏状态
						if (colType === 'left') {
							const item = this.productList.leftList[index];
							if (item) {
								item.isCollection = !item.isCollection;
								item.notesLikeNum = item.isCollection ? item.notesLikeNum + 1 : item.notesLikeNum - 1;
							}
						} else {
							const item = this.productList.rightList[index];
							if (item) {
								item.isCollection = !item.isCollection;
								item.notesLikeNum = item.isCollection ? item.notesLikeNum + 1 : item.notesLikeNum - 1;
							}
						}
					}
				}).catch(error => {
					console.error('⭐ 商品收藏失败:', error);
					uni.showToast({
						title: '收藏失败',
						icon: 'none'
					});
				});
			},
			
			// 下拉刷新
			onRefresh() {
				this.refreshing = true;
				this.productList = {
					leftList: [],
					rightList: [],
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					pageSize: 6,
				};
				this.isEmpty = false;
				this.getMoreProducts().finally(() => {
					this.refreshing = false;
				});
			},
			
			// 上拉加载更多
			onReachBottom() {
				this.getMoreProducts();
			},
			
			goToSearchValue() {
				uni.navigateTo({
					url: `/pkg-search/pages/searchPage/productSearchPage?keyword=${this.searchValue}`
				})
			},
			
			goToBack() {
				uni.navigateBack();
			}
		},
		
		onLoad(options) {
			console.log('商品搜索详情页加载，参数:', options);
			
			this.searchValue = options.keyword || ''
			uni.getSystemInfo({
				success: (res) => {
					// 微信小程序端使用系统导航栏，不额外占位
					// #ifdef MP-WEIXIN
					this.statusBarHeight = 0;
					this.scrollHeight = res.windowHeight - 44; // 仅减去页面头部高度
					// #endif
					// #ifndef MP-WEIXIN
					this.statusBarHeight = res.statusBarHeight;
					this.scrollHeight = res.windowHeight - res.statusBarHeight - 44; // 减去状态栏和头部高度
					// #endif
				},
			});
			this.getMoreProducts()
		},
		
		onShow() {
			uni.$once('updateProductSearch', (keyword) => {
				console.log('更新商品搜索关键词:', keyword)
				this.searchValue = keyword
				this.productList = {
					leftList: [],
					rightList: [],
					leftHeight: 0,
					rightHeight: 0,
					status: 'loadmore',
					page: 1,
					pageSize: 6,
				}
				this.isEmpty = false
				this.getMoreProducts()
			})
		}
	}
</script>

<style lang="scss">
	.water-left,
	.water-right {
		width: 50%;
		margin: 0rpx auto;
	}

	.product-card {
		background: #fff;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 16rpx 0 rgba(0, 0, 0, 0.08);
		margin-top: 5rpx;
		margin-bottom: 24rpx;
		margin-left: 8rpx;
		margin-right: 8rpx;
		overflow: hidden;
		position: relative;
		border: 1rpx solid #f0f0f0;
	}

	.product-title {
		font-size: 30rpx;
		padding: 10rpx;
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

	.user-name {
		margin-left: 10rpx;
		color: #16160e;
		font-size: 23rpx;
		line-height: 20px;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: calc(100% - 70px);
	}

	.location-tag {
		display: flex;
		position: absolute;
		bottom: 185rpx;
		left: 8rpx;
		color: #ffffff;
		background-color: rgba(123, 124, 125, 0.6);
		padding: 5rpx 10rpx;
		border-radius: 50rpx;
		font-size: 22rpx;
		max-width: 70%;
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
		justify-content: space-between;
		margin: 0 20rpx 8rpx 20rpx;
	}

	.price-group {
		display: flex;
		align-items: center;
		gap: 8rpx;
	}

	.goods-price {
		margin-bottom: -5px;
		color: #ff4d4f;
		font-size: 32rpx;
		font-weight: bold;
	}

	.goods-origin-price {
		margin-bottom: -5px;
		color: #999;
		font-size: 24rpx;
		text-decoration: line-through;
	}

	.post-type-tag {
		background-color: #ff6b35;
		color: #fff;
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: 12rpx;
		font-weight: 500;
	}

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
		color: #666;
		margin-bottom: 15rpx;
	}

	.empty-tip {
		font-size: 26rpx;
		color: #999;
	}
</style>