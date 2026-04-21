<template>
	<view class="address-edit-page">
		<!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航 -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<!-- #ifndef MP-WEIXIN -->
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<!-- #endif -->
			<view class="header-title">{{ addressId ? '编辑地址' : '新建地址' }}</view>
			<view class="header-right"></view>
		</view>
		
		<!-- 头部占位 -->
		<view :style="{height: navPlaceholderHeight}"></view>
		
		<!-- 表单 -->
		<view class="form-container">
			<view class="form-item">
				<view class="form-label">收货人</view>
				<input 
					class="form-input" 
					v-model="formData.name" 
					placeholder="请输入收货人姓名"
					maxlength="20"
				/>
			</view>
			
			<view class="form-item">
				<view class="form-label">手机号</view>
				<input 
					class="form-input" 
					v-model="formData.phone" 
					type="number"
					placeholder="请输入手机号"
					maxlength="11"
				/>
			</view>
			
			<view class="form-item" @click="showAddressPicker">
				<view class="form-label">省市区</view>
				<view class="form-input address-picker-input">
					<text v-if="regionText" class="address-text">{{ regionText }}</text>
					<text v-else class="address-placeholder">请选择省市区</text>
				</view>
				<u-icon name="arrow-right" size="16" color="#999"></u-icon>
			</view>
			
			<view class="form-item">
				<view class="form-label">详细地址</view>
				<textarea 
					class="form-textarea" 
					v-model="formData.address" 
					placeholder="请输入详细地址"
					maxlength="200"
					auto-height
				/>
			</view>
			
			<view class="form-item" v-if="addressId">
				<view class="form-label">设为默认</view>
				<switch 
					:checked="formData.isDefault" 
					@change="onDefaultChange"
					color="#3d8af5"
				/>
			</view>
		</view>
		
		<!-- 底部保存按钮 -->
		<view class="footer">
			<view class="save-btn" @click="saveAddress">
				保存
			</view>
		</view>
		
		<!-- 省市区选择器 -->
		<address-picker
			ref="addressPicker"
			:show="showPicker"
			:addressData="addressData"
			:indexs="pickerIndexs"
			closeOnClickOverlay
			@confirm="onAddressConfirm"
			@close="showPicker = false"
			@cancel="showPicker = false"
			@open="onPickerOpen"
		></address-picker>
	</view>
</template>

<script>
	import { getAddressDetail, addAddress, updateAddress, setDefaultAddress } from '@/apis/address_service.js'
	import cacheManager from '@/utils/cacheManager.js'
	
	import weixinNavBar from '@/utils/weixinNavBar.js'
	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				addressId: '',
				userId: '',
				formData: {
					name: '',
					phone: '',
					province: '',
					city: '',
					district: '',
					address: '',
					isDefault: false
				},
				showPicker: false,
				addressData: [],
				pickerIndexs: [0, 0, 0]
			}
		},
		computed: {
			regionText() {
				const parts = []
				if (this.formData.province) parts.push(this.formData.province)
				if (this.formData.city) parts.push(this.formData.city)
				if (this.formData.district) parts.push(this.formData.district)
				return parts.length > 0 ? parts.join(' ') : ''
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
			
			// 获取地址ID（编辑模式）
			if (options.id) {
				this.addressId = options.id
				this.loadAddressDetail()
			}
			
			// 获取用户ID
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo && userInfo.id) {
				this.userId = userInfo.id
			}
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			
			// 加载地址详情
			async loadAddressDetail() {
				try {
					const res = await getAddressDetail(this.addressId)
					if (res.code === 200 && res.data) {
						this.formData = {
							name: res.data.name || '',
							phone: res.data.phone || '',
							province: res.data.province || '',
							city: res.data.city || '',
							district: res.data.district || '',
							address: res.data.address || '',
							isDefault: res.data.isDefault || false
						}
						// 设置选择器的初始值
						this.setPickerInitialValue()
					}
				} catch (error) {
					console.error('加载地址详情失败:', error)
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
				}
			},
			
			// 设置选择器初始值
			setPickerInitialValue() {
				if (this.formData.province && this.formData.city && this.formData.district) {
					this.addressData = [this.formData.province, this.formData.city, this.formData.district]
					// 根据省市区名称查找对应的索引
					this.calculatePickerIndexs()
				} else {
					this.addressData = []
					this.pickerIndexs = [0, 0, 0]
				}
			},
			
			// 计算选择器索引
			calculatePickerIndexs() {
				try {
					// 导入省市区数据
					const areaData = require('@/uni_modules/address-picker/province-city-county.json')
					
					// 查找省份索引
					const pIdx = areaData.findIndex(p => p.name === this.formData.province)
					if (pIdx === -1) {
						this.pickerIndexs = [0, 0, 0]
						return
					}
					
					// 查找城市索引
					const cityList = areaData[pIdx].children || []
					const cIdx = cityList.findIndex(c => c.name === this.formData.city)
					if (cIdx === -1) {
						this.pickerIndexs = [pIdx, 0, 0]
						return
					}
					
					// 查找区县索引
					const districtList = cityList[cIdx].children || []
					const dIdx = districtList.findIndex(d => d.name === this.formData.district)
					if (dIdx === -1) {
						this.pickerIndexs = [pIdx, cIdx, 0]
						return
					}
					
					this.pickerIndexs = [pIdx, cIdx, dIdx]
				} catch (error) {
					console.error('计算选择器索引失败:', error)
					this.pickerIndexs = [0, 0, 0]
				}
			},
			
			// 显示省市区选择器
			showAddressPicker() {
				// 在打开选择器前，确保初始值已设置
				if (this.formData.province && this.formData.city && this.formData.district) {
					this.setPickerInitialValue()
				} else {
					// 如果没有数据，重置为默认值
					this.addressData = []
					this.pickerIndexs = [0, 0, 0]
				}
				this.showPicker = true
			},
			
			// 选择器打开时的回调
			onPickerOpen() {
				// 选择器打开时，确保数据已设置
				// address-picker 组件在 open() 时会调用 formatData()，此时会读取 this.addressData
				// 所以我们需要在打开前确保 addressData 已设置
				if (this.formData.province && this.formData.city && this.formData.district) {
					// 重新设置数据，确保组件能正确回显
					this.setPickerInitialValue()
				}
			},
			
			// 省市区选择确认
			onAddressConfirm(e) {
				// e.value 是当前选中项的值数组，例如：['北京市', '北京市', '东城区']
				// e.values 是所有列的所有选项数组，不是选中的值
				const values = e.value || []
				if (values.length >= 3) {
					this.formData.province = values[0] || ''
					this.formData.city = values[1] || ''
					this.formData.district = values[2] || ''
					this.addressData = values
					this.pickerIndexs = e.indexs || [0, 0, 0]
				}
				this.showPicker = false
			},
			
			// 默认地址开关
			onDefaultChange(e) {
				this.formData.isDefault = e.detail.value
			},
			
			// 验证表单
			validateForm() {
				if (!this.formData.name || this.formData.name.trim() === '') {
					uni.showToast({
						title: '请输入收货人姓名',
						icon: 'none'
					})
					return false
				}
				
				if (!this.formData.phone || this.formData.phone.trim() === '') {
					uni.showToast({
						title: '请输入手机号',
						icon: 'none'
					})
					return false
				}
				
				// 验证手机号格式
				const phoneReg = /^1[3-9]\d{9}$/
				if (!phoneReg.test(this.formData.phone)) {
					uni.showToast({
						title: '请输入正确的手机号',
						icon: 'none'
					})
					return false
				}
				
				if (!this.formData.province || !this.formData.city || !this.formData.district) {
					uni.showToast({
						title: '请选择省市区',
						icon: 'none'
					})
					return false
				}
				
				if (!this.formData.address || this.formData.address.trim() === '') {
					uni.showToast({
						title: '请输入详细地址',
						icon: 'none'
					})
					return false
				}
				
				return true
			},
			
			// 保存地址
			async saveAddress() {
				if (!this.validateForm()) {
					return
				}
				
				if (!this.userId) {
					uni.showToast({
						title: '用户信息异常',
						icon: 'none'
					})
					return
				}
				
				try {
					const addressData = {
						uid: this.userId,
						name: this.formData.name.trim(),
						phone: this.formData.phone.trim(),
						province: this.formData.province.trim(),
						city: this.formData.city.trim(),
						district: this.formData.district.trim(),
						address: this.formData.address.trim(),
						isDefault: this.formData.isDefault
					}
					
					let res
					if (this.addressId) {
						// 更新地址
						res = await updateAddress(this.addressId, addressData)
					} else {
						// 新增地址
						res = await addAddress(addressData)
					}
					
					if (res.code === 200) {
						// 如果设置为默认地址
						if (this.formData.isDefault && res.data && res.data.id) {
							await setDefaultAddress(res.data.id || this.addressId, this.userId)
						}
						
						uni.showToast({
							title: this.addressId ? '保存成功' : '添加成功',
							icon: 'success'
						})
						
						// 清除地址列表缓存
						try {
							const cacheKey = cacheManager.generateCacheKey(`/idle/address/addressList?uid=${this.userId}`, {}, 'GET')
							cacheManager.delete(cacheKey)
							console.log('已清除地址列表缓存')
						} catch (error) {
							console.error('清除缓存失败:', error)
						}
						
						// 发送刷新地址列表事件
						uni.$emit('refreshAddressList')
						
						// 直接调用上一页的刷新方法（更可靠）
						try {
							const pages = getCurrentPages()
							if (pages.length > 1) {
								const prevPage = pages[pages.length - 2]
								console.log('上一页路由:', prevPage?.route)
								if (prevPage && prevPage.route && prevPage.route.includes('addressManagement')) {
									// 如果上一页是地址管理页面，直接调用其刷新方法（强制刷新）
									if (prevPage.$vm && typeof prevPage.$vm.loadAddressList === 'function') {
										console.log('直接调用上一页的loadAddressList方法（强制刷新）')
										prevPage.$vm.loadAddressList(true) // 传入 true 强制刷新
									} else {
										console.log('上一页实例或方法不存在')
									}
								}
							}
						} catch (error) {
							console.error('调用上一页刷新方法失败:', error)
						}
						
						// 延迟返回，让用户看到成功提示
						setTimeout(() => {
							uni.navigateBack({
								success: () => {
									// 返回成功后再次发送刷新事件（确保刷新）
									setTimeout(() => {
										uni.$emit('refreshAddressList')
									}, 100)
								}
							})
						}, 1000)
					}
				} catch (error) {
					console.error('保存地址失败:', error)
					uni.showToast({
						title: '保存失败',
						icon: 'none'
					})
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.address-edit-page {
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
	
	.form-container {
		flex: 1;
		background-color: #fff;
		margin-top: 10px;
	}
	
	.form-item {
		display: flex;
		align-items: center;
		padding: 15px;
		border-bottom: 1px solid #f0f0f0;
		
		.form-label {
			width: 80px;
			font-size: 15px;
			color: #333;
			flex-shrink: 0;
		}
		
		.form-input {
			flex: 1;
			font-size: 15px;
			color: #333;
		}
		
		.form-textarea {
			flex: 1;
			font-size: 15px;
			color: #333;
			min-height: 80px;
			line-height: 1.5;
		}
		
		.address-picker-input {
			display: flex;
			align-items: center;
		}
		
		.address-text {
			font-size: 15px;
			color: #333;
		}
		
		.address-placeholder {
			font-size: 15px;
			color: #999;
		}
	}
	
	.footer {
		background-color: #fff;
		padding: 10px 15px;
		padding-bottom: calc(10px + env(safe-area-inset-bottom));
		border-top: 1px solid #eee;
	}
	
	.save-btn {
		width: 250px;
		height: 45px;
		background-color: #3d8af5;
		border-radius: 25px;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 16px;
		font-weight: 500;
		margin: 0 auto;
	}
</style>























