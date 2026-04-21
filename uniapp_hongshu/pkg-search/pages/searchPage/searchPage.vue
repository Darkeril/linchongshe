<template>
	<view>
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}"
			style="position: fixed;top: 0;width: 100%;z-index: 9999;background-color: #fff;"></view>
		<!-- #endif -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		<view class="search-bar-wrap">
			<!-- #ifndef MP-WEIXIN -->
			<u-icon @click="goToBack" name="arrow-left" size="25"></u-icon>
			<!-- #endif -->
			<u-search style="margin-left: 20rpx;" :showAction="true" actionText="搜索" :animation="true" :focus="true"
				v-model="searchValue" @search="search" @clear="clearSearch" @custom="search" @focus="focus"></u-search>
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
		
		<!-- 猜你想搜 -->
		<!-- <view class="recommend-section" v-if="false">
			<view class="section-header">
				<text class="section-title">猜你想搜</text>
			</view>
			<view class="recommend-list">
				<view v-for="(item, index) in recommendRecords" :key="index" class="recommend-item" @click="goToSearch(item)">
					<text class="recommend-text">{{ item }}</text>
				</view>
			</view>
		</view> -->
	</view>
</template>

<script>
	import { $request } from '@/utils/request.js'
	import { getHotTagList } from '@/apis/tag_service.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				searchHistory: [],
				startDelete: false,
				searchValue: '',
				showCount: 5,
				showHide: true,
				recommendRecords: [] // 猜你想搜
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
						console.log('历史记录已刷新:', this.searchHistory.length, '条');
					} else {
						console.error('获取历史记录失败:', response.msg);
					}
				} catch (error) {
					console.error('获取历史记录出错:', error);
				}
			},
			
			// 立即更新本地历史记录
			updateLocalHistory(keyword) {
				// 检查是否已存在相同的关键词
				const existingIndex = this.searchHistory.findIndex(item => 
					(item.keyword || item.content) === keyword
				);
				
				if (existingIndex !== -1) {
					// 如果已存在，移动到最前面
					const existingItem = this.searchHistory[existingIndex];
					this.searchHistory.splice(existingIndex, 1);
					this.searchHistory.unshift({
						...existingItem,
						keyword: keyword,
						content: keyword,
						createTime: new Date().toISOString(),
						updateTime: new Date().toISOString()
					});
				} else {
					// 如果不存在，添加到最前面
					this.searchHistory.unshift({
						id: Date.now(), // 临时ID
						keyword: keyword,
						content: keyword,
						createTime: new Date().toISOString(),
						updateTime: new Date().toISOString()
					});
				}
				
				// 限制历史记录数量，避免过多
				if (this.searchHistory.length > 20) {
					this.searchHistory = this.searchHistory.slice(0, 20);
				}
				
				console.log('本地历史记录已更新:', keyword);
			},
			
			// 添加搜索记录
			async addSearchRecord(keyword) {
				try {
					const userInfo = uni.getStorageSync('userInfo');
					if (!userInfo || !userInfo.id) {
						console.log('用户未登录，无法添加搜索记录');
						return;
					}
					
					// 去除空格换行
					keyword = keyword.replace(/[\n\r\s]+/g, '');
					if (!keyword) return;
					
					const response = await $request({
						url: '/web/app/es/record/addRecord',
						method: 'POST',
						data: {
							keyword: keyword,
							uid: userInfo.id,
							type: 'note' // 标记为笔记搜索
						}
					});
					
					if (response.code === 200) {
						console.log('搜索记录添加成功');
						// 立即更新本地历史记录，避免等待API刷新
						this.updateLocalHistory(keyword);
						// 同时重新获取历史记录确保数据同步
						this.getSearchHistory();
					} else {
						console.error('添加搜索记录失败:', response.msg);
					}
				} catch (error) {
					console.error('添加搜索记录出错:', error);
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
			// goToSearch(keyword) {
			// 	if (this.startDelete) {
			// 		return;
			// 	}
			// 	this.searchValue = ''
			// 	// 再次去除空格，以免搜索历史记录中出现空白
			// 	keyword = keyword.replace(/[\n\r\s]+/g, '');
			// 	if (keyword.length > 20) {
			// 		uni.showToast({
			// 			title: '输入内容过长',
			// 			icon: 'none'
			// 		});
			// 		return;
			// 	}
			// 	this.$sqliteUtil.SqlExecute(
			// 		`update search_history set updateTime=${new Date().getTime()} where content='${keyword}'`
			// 	).then(res => {
			// 		// 获取页面栈，判断是否已经存在搜索详情页面，如果存在则返回，否则跳转
			// 		let pages = getCurrentPages();
			// 		let page = pages[pages.length - 2];
			// 		if (page.route === 'pages/searchPage/searchDetailPage') {
			// 			uni.$emit('updateSearch', keyword);
			// 			uni.navigateBack();
			// 		} else {
			// 			uni.navigateTo({
			// 				url: '/pages/searchPage/searchDetailPage?keyword=' + keyword
			// 			});
			// 		}
			// 	});
			// },
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
				
				// 判断页面栈，避免重复跳转
				let pages = getCurrentPages();
				let page = pages[pages.length - 2];
				
				// 笔记搜索页面
				let targetPage = 'pkg-search/pages/searchPage/searchDetailPage';
				let eventName = 'updateSearch';
				
				if (page && page.route === targetPage) {
					uni.$emit(eventName, keyword);
					uni.navigateBack();
				} else {
					uni.navigateTo({
						url: `/${targetPage}?keyword=${keyword}`
					});
				}
			},
			focus() {
				this.startDelete = false;
			},
			clearSearch() {
				this.searchValue = '';
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
							keyword: recordToDelete.keyword || recordToDelete.content,
							uid: userInfo.id
						}
					});
					
					console.log('删除历史记录请求参数:', {
						keyword: recordToDelete.keyword || recordToDelete.content,
						uid: userInfo.id
					});
					
					console.log('删除历史记录响应:', response);
					
					if (response.code === 200) {
						// 删除成功后重新获取历史记录
						await this.getSearchHistory();
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						});
					} else {
						console.error('删除历史记录失败:', response);
						uni.showToast({
							title: response.msg || '删除失败',
							icon: 'error'
						});
					}
				} catch (error) {
					console.error('删除历史记录出错:', error);
					uni.showToast({
						title: '删除失败',
						icon: 'error'
					});
				}
			},
			// 删除所有历史记录
			async deleteAllHistory() {
				try {
					const userInfo = uni.getStorageSync('userInfo');
					if (!userInfo || !userInfo.id) {
						console.log('用户未登录，无法清空历史记录');
						return;
					}
					
					// 显示确认对话框
					const res = await uni.showModal({
						title: '确认清空',
						content: '确定要清空所有搜索历史吗？',
						confirmText: '清空',
						cancelText: '取消'
					});
					
					if (!res.confirm) {
						return;
					}
					
					const response = await $request({
						url: '/web/app/es/record/clearAllRecord',
						method: 'POST',
						data: {
							type: 'note' // 只清空笔记搜索记录
						}
					});
					
					if (response.code === 200) {
						// 清空成功后重新获取历史记录
						await this.getSearchHistory();
						uni.showToast({
							title: '清空成功',
							icon: 'success'
						});
						this.startDelete = false;
					} else {
						console.error('清空历史记录失败:', response.msg);
						uni.showToast({
							title: '清空失败',
							icon: 'error'
						});
					}
				} catch (error) {
					console.error('清空历史记录出错:', error);
					uni.showToast({
						title: '清空失败',
						icon: 'error'
					});
				}
			},
			goToBack() {
				uni.navigateBack();
			},
			// 获取热门标签（猜你想搜）
			async fetchHotTags() {
				try {
					const res = await getHotTagList({ page: 1, pageSize: 10 });
					if (res.code === 200 && res.data && res.data.records) {
						// 按使用次数（likeCount）从高到低排序，取前10个
						const tags = res.data.records
							.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
							.slice(0, 10)
							.map(tag => tag.title || tag.name || '');
						this.recommendRecords = tags;
					}
				} catch (error) {
					console.error('获取热门标签失败:', error);
					// 如果获取失败，使用默认值
					this.recommendRecords = ['壁纸', '风景', '情侣', '头像', '动漫', '动物'];
				}
			},
		},
		onLoad(options) {
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44);
					// H5 无系统状态栏，去掉顶部占位避免空白
					// #ifdef H5
					this.statusBarHeight = 0;
					this.navPlaceholderHeight = '0px';
					// #endif
					// App 端仅保留状态栏高度，搜索栏紧贴状态栏下沿，减少顶部留白
					// #ifdef APP-PLUS
					this.navPlaceholderHeight = (this.statusBarHeight || 0) + 'px';
					// #endif
				}
			});
			if (options.keyword) {
				this.searchValue = options.keyword
			}
		},
		onShow() {
			// 使用后端接口获取历史记录
			this.getSearchHistory();
			// 获取猜你想搜数据
			this.fetchHotTags();
		}
	}
</script>

<style lang="scss">
	input {
		caret-color: #3d8af5;
	}

	.search-bar-wrap {
		padding: 10rpx 15rpx;
		display: flex;
		height: 44px;
		align-items: center;
	}
	
	// 历史记录区域
	.history-section {
		background: #fff;
	}
	
	// 区域头部
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 32rpx 30rpx 20rpx;
	}
	
	.section-title {
		font-size: 32rpx;
		color: #333;
		font-weight: 600;
	}
	
	.header-actions {
		display: flex;
		align-items: center;
		gap: 24rpx;
	}
	
	.clear-btn {
		font-size: 26rpx;
		color: #ff4757;
		padding: 8rpx 0;
	}
	
	.manage-btn, .done-btn {
		font-size: 26rpx;
		color: #666;
		padding: 8rpx 0;
	}
	
	// 历史记录列表
	.history-list {
		padding: 0 30rpx 32rpx;
		display: flex;
		flex-wrap: nowrap;
		overflow-x: auto;
		gap: 16rpx;
		-webkit-overflow-scrolling: touch;
		
		&::-webkit-scrollbar {
			display: none;
		}
	}
	
	// 历史记录标签
	.history-tag {
		display: inline-flex;
		align-items: center;
		padding: 14rpx 24rpx;
		background: #f5f5f5;
		border-radius: 32rpx;
		position: relative;
		flex-shrink: 0;
		white-space: nowrap;
		transition: all 0.2s;
		
		&:active {
			background: #e8e8e8;
			transform: scale(0.98);
		}
	}
	
	.tag-text {
		font-size: 28rpx;
		color: #333;
		line-height: 1.2;
		font-weight: 400;
	}
	
	// 删除按钮
	.remove-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 32rpx;
		height: 32rpx;
		background: #ff4757;
		border-radius: 50%;
		margin-left: 12rpx;
		position: absolute;
		right: -8rpx;
		top: -8rpx;
		
		&:active {
			transform: scale(0.9);
		}
	}
	
	.remove-icon {
		font-size: 20rpx;
		color: #fff;
		font-weight: bold;
		line-height: 1;
	}
	
	// 更多按钮
	.more-btn {
		display: inline-flex;
		align-items: center;
		padding: 12rpx 20rpx;
		background: #f0f0f0;
		border-radius: 20rpx;
		gap: 8rpx;
		
		&:active {
			background: #e0e0e0;
		}
	}
	
	.more-text {
		font-size: 26rpx;
		color: #666;
	}
	
	.arrow-icon {
		font-size: 20rpx;
		color: #999;
	}
	
	// 空状态
	.empty-state {
		padding: 80rpx 30rpx;
		text-align: center;
	}
	
	.empty-text {
		font-size: 28rpx;
		color: #999;
	}
	
	// 猜你想搜区域
	.recommend-section {
		background: #fff;
		margin-top: 20rpx;
		padding-bottom: 20rpx;
	}
	
	.recommend-list {
		padding: 0 30rpx 20rpx;
		display: flex;
		flex-direction: column;
		gap: 0;
	}
	
	.recommend-item {
		padding: 24rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
		transition: all 0.2s;
		
		&:last-child {
			border-bottom: none;
		}
		
		&:active {
			background: #f8f8f8;
		}
	}
	
	.recommend-text {
		font-size: 30rpx;
		color: #333;
		font-weight: 400;
		line-height: 1.5;
	}
	
</style>