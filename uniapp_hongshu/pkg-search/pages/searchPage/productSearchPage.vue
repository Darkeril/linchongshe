<template>
	<view>
		<!-- 顶部状态栏占位：微信小程序端使用系统导航栏，不需要额外占位，避免出现大块空白 -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}"
			style="position: fixed;top: 0;width: 100%;z-index: 9999;background-color: #fff;"></view>
		<view :style="{height: statusBarHeight + 'px'}"></view>
		<!-- #endif -->
		<view style="padding: 10rpx 15rpx;display: flex;height: 44px;">
			<!-- #ifndef MP-WEIXIN -->
			<u-icon @click="goToBack" name="arrow-left" size="25"></u-icon>
			<!-- #endif -->
			<u-search style="margin-left: 20rpx;" :showAction="true" actionText="搜索" :animation="true" :focus="true"
				v-model="searchValue" @search="search" @clear="clearSearch" @custom="search" @focus="focus" placeholder="搜索商品"></u-search>
		</view>
		<view class="history-section">
			<view class="section-header">
				<text class="section-title">历史记录</text>
				<view class="header-actions">
					<text v-if="startDelete" @click="deleteAllHistory" class="clear-btn">清空</text>
					<text v-if="!startDelete" @click="startDelete=true" class="manage-btn">管理</text>
					<text v-else @click="startDelete=false" class="done-btn">完成</text>
				</view>
			</view>
			
			<view class="history-list" v-if="searchHistory.length > 0">
				<view v-for="(item,index) in searchHistory" :key="index"
					v-if="index<(showHide?showCount:searchHistory.length)"
					class="history-tag" @click="!startDelete && goToSearch(item.content)">
					<text class="tag-text">{{item.content}}</text>
					<view v-if="startDelete" @click.stop="deleteHistory(item.id)" class="remove-btn">
						<text class="remove-icon">×</text>
					</view>
				</view>
				
				<view v-if="searchHistory.length > showCount" class="more-btn" @click="showHide=!showHide">
					<text class="more-text">{{showHide ? '更多' : '收起'}}</text>
					<text class="arrow-icon">{{showHide ? '▼' : '▲'}}</text>
				</view>
			</view>
			
			<view v-else class="empty-state">
				<text class="empty-text">暂无搜索历史</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { $request } from '@/utils/request.js'
	
	export default {
		data() {
			return {
				statusBarHeight: 0,
				searchHistory: [],
				startDelete: false,
				searchValue: '',
				showCount: 5,
				showHide: true
			};
		},
		methods: {
			// 获取搜索历史记录
			async getSearchHistory() {
				try {
					const userInfo = uni.getStorageSync('userInfo');
					if (!userInfo || !userInfo.id) {
						console.log('用户未登录，无法获取历史记录');
						return;
					}
					
					const response = await $request({
						url: '/web/app/es/record/getRecordByKeyWord',
						method: 'GET',
						data: {
							uid: userInfo.id
						}
					});
					
					if (response.code === 200) {
						// 对历史记录进行去重处理
						const historyData = response.data || [];
						const uniqueHistory = [];
						const seenKeywords = new Set();
						
						// 按时间倒序排列，保留最新的记录
						historyData.sort((a, b) => new Date(b.createTime || b.updateTime) - new Date(a.createTime || a.updateTime));
						
						// 去重处理
						historyData.forEach(item => {
							const keyword = item.keyword || item.content;
							if (keyword && !seenKeywords.has(keyword)) {
								seenKeywords.add(keyword);
								uniqueHistory.push({
									...item,
									keyword: keyword,
									content: keyword
								});
							}
						});
						
						this.searchHistory = uniqueHistory;
						console.log('商品搜索历史记录已刷新:', this.searchHistory.length, '条');
					} else {
						console.log('获取搜索历史失败:', response.message);
					}
				} catch (error) {
					console.error('获取搜索历史出错:', error);
				}
			},
			
			// 添加搜索记录
			async addSearchRecord(keyword) {
				try {
					const userInfo = uni.getStorageSync('userInfo');
					if (!userInfo || !userInfo.id) {
						console.log('用户未登录，无法添加搜索记录');
						return;
					}
					
					const response = await $request({
						url: '/web/app/es/record/addRecord',
						method: 'POST',
						data: {
							uid: userInfo.id,
							keyword: keyword,
							type: 'product' // 标记为商品搜索
						}
					});
					
					if (response.code === 200) {
						console.log('商品搜索记录添加成功');
						// 刷新历史记录
						this.getSearchHistory();
					} else {
						console.log('添加搜索记录失败:', response.message);
					}
				} catch (error) {
					console.error('添加搜索记录出错:', error);
				}
			},
			
			// 删除单个历史记录
			async deleteHistory(id) {
				try {
					const userInfo = uni.getStorageSync('userInfo');
					if (!userInfo || !userInfo.id) {
						console.log('用户未登录，无法删除历史记录');
						return;
					}
					
					const recordToDelete = this.searchHistory.find(item => item.id === id);
					if (!recordToDelete) {
						console.log('记录不存在');
						return;
					}
					
					const response = await $request({
						url: '/web/app/es/record/clearRecord',
						method: 'POST',
						data: {
							uid: userInfo.id,
							recordId: id
						}
					});
					
					if (response.code === 200) {
						console.log('删除搜索记录成功');
						// 从本地列表中移除
						this.searchHistory = this.searchHistory.filter(item => item.id !== id);
					} else {
						console.log('删除搜索记录失败:', response.message);
					}
				} catch (error) {
					console.error('删除搜索记录出错:', error);
				}
			},
			
			// 清空所有历史记录
			async deleteAllHistory() {
				try {
					const userInfo = uni.getStorageSync('userInfo');
					if (!userInfo || !userInfo.id) {
						console.log('用户未登录，无法清空历史记录');
						return;
					}
					
					const response = await $request({
						url: '/web/app/es/record/clearAllRecord',
						method: 'POST',
						data: {
							uid: userInfo.id,
							type: 'product' // 只清空商品搜索记录
						}
					});
					
					if (response.code === 200) {
						console.log('清空搜索历史成功');
						this.searchHistory = [];
						this.startDelete = false;
						uni.showToast({
							title: '清空成功',
							icon: 'success',
							duration: 1500
						});
					} else {
						console.log('清空搜索历史失败:', response.message);
					}
				} catch (error) {
					console.error('清空搜索历史出错:', error);
				}
			},
			
			search() {
				console.log(this.searchValue)
				// 去除空格换行
				let searchValue = this.searchValue.replace(/[\n\r\s]+/g, '');
				if (searchValue === '') return;
				// 查询数据库是否存在
				this.goToSearch(this.searchValue)
			},
			
			async goToSearch(keyword) {
				// 去除空格换行
				keyword = keyword.replace(/[\n\r\s]+/g, '');
				if (!keyword) return;
				if (keyword.length > 20) {
					uni.showToast({
						title: '输入内容过长',
						icon: 'none'
					});
					return;
				}
				
				// 添加搜索记录到后端
				await this.addSearchRecord(keyword);
				
				// 判断页面栈，避免重复跳转 - 微信小程序兼容性处理
				let pages = getCurrentPages();
				let page = null;
				
				// 确保页面栈存在且有足够的页面
				if (pages && pages.length >= 2) {
					page = pages[pages.length - 2];
				}
				
				if (page && page.route === 'pkg-search/pages/searchPage/productSearchDetailPage') {
					uni.$emit('updateProductSearch', keyword);
					uni.navigateBack();
				} else {
					uni.navigateTo({
						url: `/pkg-search/pages/searchPage/productSearchDetailPage?keyword=${keyword}`
					});
				}
			},
			
			focus() {
				this.startDelete = false;
			},
			
			clearSearch() {
				this.searchValue = '';
			},
			
			goToBack() {
				uni.navigateBack();
			}
		},
		
		onLoad(options) {
			// 获取状态栏高度
			uni.getSystemInfo({
				success: (res) => {
					// 微信小程序端使用系统导航栏，不额外占位
					// #ifdef MP-WEIXIN
					this.statusBarHeight = 0;
					// #endif
					// #ifndef MP-WEIXIN
					this.statusBarHeight = res.statusBarHeight;
					// #endif
				},
			});
			
			// 获取搜索历史
			this.getSearchHistory();
			
			// 如果有传入的关键词，设置到搜索框
			if (options.keyword) {
				this.searchValue = options.keyword;
			}
		},
		
		onShow() {
			// 每次显示页面时刷新历史记录
			this.getSearchHistory();
		}
	}
</script>

<style lang="scss">
	.history-section {
		padding: 30rpx;
		background: #fff;
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
	}
	
	.header-actions {
		display: flex;
		gap: 20rpx;
	}
	
	.manage-btn, .done-btn, .clear-btn {
		font-size: 28rpx;
		color: #666;
		padding: 8rpx 16rpx;
		border-radius: 8rpx;
		background: #f5f5f5;
	}
	
	.clear-btn {
		color: #ff4757;
		background: #ffe6e6;
	}
	
	.history-list {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}
	
	.history-tag {
		display: flex;
		align-items: center;
		padding: 16rpx 24rpx;
		background: #f8f9fa;
		border-radius: 30rpx;
		border: 1rpx solid #e9ecef;
		position: relative;
	}
	
	.tag-text {
		font-size: 26rpx;
		color: #495057;
		margin-right: 8rpx;
	}
	
	.remove-btn {
		position: absolute;
		top: -8rpx;
		right: -8rpx;
		width: 32rpx;
		height: 32rpx;
		background: #ff4757;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.remove-icon {
		color: #fff;
		font-size: 20rpx;
		font-weight: bold;
	}
	
	.more-btn {
		display: flex;
		align-items: center;
		padding: 16rpx 24rpx;
		background: #e9ecef;
		border-radius: 30rpx;
		border: 1rpx solid #dee2e6;
	}
	
	.more-text {
		font-size: 26rpx;
		color: #6c757d;
		margin-right: 8rpx;
	}
	
	.arrow-icon {
		font-size: 20rpx;
		color: #6c757d;
	}
	
	.empty-state {
		text-align: center;
		padding: 60rpx 0;
	}
	
	.empty-text {
		font-size: 28rpx;
		color: #999;
	}
</style>
