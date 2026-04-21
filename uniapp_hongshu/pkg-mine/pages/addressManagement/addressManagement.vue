<template>
	<view class="address-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航（微信端标题在系统栏展示，不重复） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">收货地址</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		
		<!-- 头部占位（微信端无自定义栏，不预留空白） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		
		<!-- 地址列表 -->
		<scroll-view 
			scroll-y="true" 
			class="address-scroll"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view class="address-list" v-if="addressList.length > 0">
				<view 
					class="address-item" 
					v-for="(item, index) in addressList" 
					:key="item.id"
					@click="selectAddress(item)"
				>
					<!-- 地址信息 -->
					<view class="address-info">
						<view class="address-header">
							<text class="name">{{ item.name }}</text>
							<text class="phone">{{ item.phone }}</text>
							<view class="default-tag" v-if="item.isDefault">默认</view>
						</view>
						<view class="address-detail">
							<text v-if="item.province || item.city || item.district">
								{{ [item.province, item.city, item.district].filter(Boolean).join(' ') }}
							</text>
							<text v-if="item.address">{{ item.address }}</text>
						</view>
					</view>
					
					<!-- 操作按钮 -->
					<view class="address-actions">
						<view class="action-icon" @click.stop="editAddress(item)">
							<u-icon name="edit-pen" size="20" color="#3d8af5"></u-icon>
						</view>
						<view class="action-icon" @click.stop="deleteAddress(item)">
							<u-icon name="trash" size="20" color="#ff4d4f"></u-icon>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-else>
				<u-empty mode="data" text="暂无收货地址"></u-empty>
			</view>
		</scroll-view>
		
		<!-- 底部新建按钮 -->
		<view class="footer">
			<view class="add-btn" @click="addNewAddress">
				<u-icon name="plus" size="20" color="#fff"></u-icon>
				<text class="add-btn-text">新建地址</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { getAddressList, deleteAddress, setDefaultAddress } from '@/apis/address_service.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'
	
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				addressList: [],
				refreshing: false,
				userId: '',
				fromOrder: false // 是否从订单页面跳转来的
			}
		},
		onLoad(options) {
			// 设置状态栏样式为白色背景
			// #ifdef MP-WEIXIN
			uni.setNavigationBarColor({
				frontColor: '#000000', // 状态栏文字颜色为黑色
				backgroundColor: '#ffffff' // 状态栏背景色为白色
			})
			// #endif
			
			// 获取状态栏高度
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
				}
			})
			
			// 获取用户ID
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo && userInfo.id) {
				this.userId = userInfo.id
			}
			
			// 从页面参数获取uid（如果有）
			if (options.uid) {
				this.userId = options.uid
			}
			
			// 检查是否从订单页面跳转来的
			if (options.from === 'order') {
				this.fromOrder = true
			}
			
			// 加载地址列表
			this.loadAddressList()
			
			// 监听地址列表刷新事件
			uni.$on('refreshAddressList', () => {
				console.log('收到刷新地址列表事件')
				this.loadAddressList()
			})
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			
			// 加载地址列表
			async loadAddressList(forceRefresh = true) {
				if (!this.userId) {
					uni.showToast({
						title: '用户信息异常',
						icon: 'none'
					})
					return
				}
				
				try {
					// 强制刷新时清除缓存并禁用缓存
					const res = await getAddressList(this.userId, forceRefresh)
					if (res.code === 200) {
						// 处理地址列表，标记第一个为默认地址（如果没有isDefault字段）
						this.addressList = res.data.map((item, index) => {
							return {
								...item,
								isDefault: item.isDefault !== undefined ? item.isDefault : (index === 0)
							}
						})
					}
				} catch (error) {
					console.error('加载地址列表失败:', error)
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
				}
			},
			
			// 下拉刷新
			async onRefresh() {
				this.refreshing = true
				await this.loadAddressList()
				setTimeout(() => {
					this.refreshing = false
				}, 500)
			},
			
			// 选择地址
			async selectAddress(item) {
				// 如果是从订单页面来的，返回选中的地址
				if (this.fromOrder) {
					// 通过事件总线传递选中的地址
					uni.$emit('selectAddress', item)
					// 返回上一页
					uni.navigateBack()
					return
				}
				
				// 否则设置为默认地址
				// 如果已经是默认地址，不做处理
				if (item.isDefault) {
					return
				}
				
				try {
					const res = await setDefaultAddress(item.id, this.userId)
					if (res.code === 200) {
						uni.showToast({
							title: '已设为默认地址',
							icon: 'success'
						})
						// 更新列表
						this.addressList.forEach(addr => {
							addr.isDefault = addr.id === item.id
						})
					}
				} catch (error) {
					console.error('设置默认地址失败:', error)
				}
			},
			
			// 编辑地址
			editAddress(item) {
				uni.navigateTo({
					url: `/pkg-mine/pages/addressEdit/addressEdit?id=${item.id}`
				})
			},
			
			// 删除地址
			deleteAddress(item) {
				uni.showModal({
					title: '提示',
					content: '确定要删除这个地址吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await deleteAddress(item.id)
								if (result.code === 200) {
									uni.showToast({
										title: '删除成功',
										icon: 'success'
									})
									// 重新加载列表
									this.loadAddressList()
								}
							} catch (error) {
								console.error('删除地址失败:', error)
								uni.showToast({
									title: '删除失败',
									icon: 'none'
								})
							}
						}
					}
				})
			},
			
			// 新建地址
			addNewAddress() {
				uni.navigateTo({
					url: '/pkg-mine/pages/addressEdit/addressEdit'
				})
			}
		},
		
		onShow() {
			// 页面显示时重新加载数据（从编辑页面返回时）
			console.log('地址管理页面 onShow 触发')
			// 使用 nextTick 确保页面完全显示后再刷新
			this.$nextTick(() => {
				console.log('开始刷新地址列表')
				this.loadAddressList()
			})
		},
		
		onUnload() {
			// 页面卸载时移除事件监听
			uni.$off('refreshAddressList', this.loadAddressList)
		}
	}
</script>

<style lang="scss" scoped>
	.address-page {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f5f5;
	}
	
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 44px;
		background-color: #fff;
		padding: 0 15px;
		border-bottom: 1px solid #eee;
		
		.header-left {
			width: 40px;
			display: flex;
			align-items: center;
			justify-content: flex-start;
		}
		
		.header-title {
			flex: 1;
			text-align: center;
			font-size: 18px;
			font-weight: 500;
			color: #333;
		}
		
		.header-right {
			width: 40px;
		}
	}
	
	.address-scroll {
		flex: 1;
		background-color: #f5f5f5;
	}
	
	.address-list {
		padding: 10px 0;
	}
	
	.address-item {
		display: flex;
		align-items: center;
		background-color: #ecf5ff;
		margin: 10px 15px;
		padding: 15px;
		border-radius: 8px;
		box-shadow: 0 1px 3px rgba(61, 138, 245, 0.15);
		border: 1px solid rgba(61, 138, 245, 0.1);
	}
	
	.address-info {
		flex: 1;
	}
	
	.address-header {
		display: flex;
		align-items: center;
		margin-bottom: 8px;
		
		.name {
			font-size: 16px;
			font-weight: 500;
			color: #333;
			margin-right: 15px;
		}
		
		.phone {
			font-size: 14px;
			color: #666;
		}
		
		.default-tag {
			margin-left: auto;
			padding: 2px 10px;
			background-color: #3d8af5;
			color: #fff;
			font-size: 12px;
			border-radius: 15px;
		}
	}
	
	.address-detail {
		font-size: 14px;
		color: #666;
		line-height: 1.5;
	}
	
	.address-actions {
		display: flex;
		align-items: center;
		gap: 15px;
		margin-left: 15px;
		
		.action-icon {
			padding: 5px;
		}
	}
	
	.empty-state {
		padding: 50px 0;
	}
	
	.footer {
		background-color: #fff;
		padding: 10px 15px;
		padding-bottom: calc(10px + env(safe-area-inset-bottom));
		border-top: 1px solid #eee;
	}
	
	.add-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 250px;
		height: 45px;
		background-color: #3d8af5;
		border-radius: 25px;
		margin: 0 auto;
		
		.add-btn-text {
			margin-left: 8px;
			font-size: 16px;
			color: #fff;
			font-weight: 500;
		}
	}
</style>

