<template>
	<view class="confirm-order-page">
		<!-- 状态栏（微信端由系统栏「爱宠社」占位） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}" style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 导航栏 -->
		<view class="navbar" :style="{top: navBarTop}">
			<!-- #ifndef MP-WEIXIN -->
			<view class="back-btn" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<!-- #endif -->
			<view class="navbar-title">确认订单</view>
			<view class="placeholder"></view>
		</view>
		
		<!-- 头部占位 -->
		<view :style="{height: navPlaceholderHeight}"></view>
		
		<!-- 主内容区 -->
		<scroll-view scroll-y :style="{height: scrollHeight + 'px'}">
			<!-- 收货地址 -->
			<view class="address-section" :class="{ 'address-disabled': hasExistingOrder }" @click="selectAddress">
				<view v-if="selectedAddress" class="address-info">
					<view class="address-header">
						<text class="receiver-name">{{ selectedAddress.receiverName }}</text>
						<text class="receiver-phone">{{ selectedAddress.phone }}</text>
					</view>
					<view class="address-detail">
						{{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detailAddress }}
					</view>
				</view>
				<view v-else class="no-address">
					<u-icon name="map" size="24" color="#999"></u-icon>
					<text class="no-address-text">请选择收货地址</text>
				</view>
				<u-icon v-if="!hasExistingOrder" name="arrow-right" size="16" color="#999"></u-icon>
			</view>
			
			<!-- 商品信息 -->
			<view class="product-section">
				<view class="section-title">商品信息</view>
				<view class="product-item">
					<image class="product-image" :src="productInfo.coverPicture" mode="aspectFill"></image>
					<view class="product-info">
						<view class="product-title">{{ productInfo.title }}</view>
						<view class="product-price-row">
							<text class="product-price">¥{{ productInfo.price }}</text>
							<text class="product-quantity">x{{ quantity }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 配送方式 -->
			<view class="delivery-section">
				<view class="section-title">配送方式</view>
				<view class="delivery-item">
					<text class="delivery-label">{{ productInfo.postType === 0 ? '快递配送' : '到店自提' }}</text>
					<text v-if="productInfo.postType === 0" class="delivery-tag">包邮</text>
				</view>
			</view>
			
			<!-- 订单备注 -->
			<view class="remark-section">
				<view class="section-title">订单备注</view>
				<textarea 
					v-model="remark" 
					class="remark-input" 
					:disabled="hasExistingOrder"
					:placeholder="hasExistingOrder ? '订单已创建，不可修改' : '选填，请先和卖家协商一致'"
					maxlength="200"
				></textarea>
			</view>
			
			<!-- 支付方式 -->
			<view class="payment-section">
				<view class="section-title">支付方式</view>
				<view class="payment-list">
					<view 
						class="payment-item" 
						:class="{ 'payment-item-disabled': item.disabled || paymentMethodLocked }"
						v-for="item in visiblePaymentMethods" 
						:key="item.value"
						@click="!item.disabled && !paymentMethodLocked && selectPayment(item.value)"
					>
						<view class="payment-left">
							<view class="payment-icon" :class="['icon-' + item.value, { 'icon-disabled': item.disabled }]">
								<image 
									v-if="item.value === 'wechat'" 
									class="icon-image" 
									src="/static/wx.png" 
									mode="aspectFit"
								></image>
								<image 
									v-else-if="item.value === 'alipay'" 
									class="icon-image" 
									src="/static/zfb.png" 
									mode="aspectFit"
								></image>
								<text v-else class="icon-text">{{ item.iconText }}</text>
							</view>
							<text class="payment-name" :class="{ 'payment-name-disabled': item.disabled }">{{ item.name }}</text>
						</view>
						<view class="payment-radio">
							<view class="radio-check" :class="{ 'radio-active': selectedPayment === item.value, 'radio-disabled': item.disabled }">
								<text v-if="selectedPayment === item.value && !item.disabled" class="check-icon">✓</text>
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 费用明细 -->
			<view class="cost-section">
				<view class="cost-item">
					<text class="cost-label">商品金额</text>
					<text class="cost-value">¥{{ (productInfo.price * quantity).toFixed(2) }}</text>
				</view>
				<view class="cost-item">
					<text class="cost-label">运费</text>
					<text class="cost-value">{{ productInfo.postType === 0 ? '包邮' : '¥0.00' }}</text>
				</view>
			</view>
		</scroll-view>
		
		<!-- 底部提交栏 -->
		<view class="submit-bar">
			<view class="total-section">
				<text class="total-label">合计：</text>
				<text class="total-price">¥{{ (productInfo.price * quantity).toFixed(2) }}</text>
			</view>
			<view 
				class="submit-btn" 
				:class="{ 'submit-btn-disabled': !canSubmit }"
				@click="submitOrder"
			>
				{{ submitButtonText }}
			</view>
		</view>
		
		<!-- 底部占位 -->
		<view style="height: 60px;"></view>
	</view>
</template>

<script>
import { getProductById } from '@/apis/idles_service.js';
import { createProductOrder, createPaymentOrder, getOrdersByUser, getOrderDetail, getPaymentGlobalConfig } from '@/apis/order_service.js';
import weixinNavBar from '@/utils/weixinNavBar.js';

export default {
	mixins: [weixinNavBar],
	data() {
		return {
			statusBarHeight: 0,
			scrollHeight: 0,
			productId: '',
			quantity: 1,
			productInfo: {},
			selectedAddress: null,
			remark: '',
			selectedPayment: 'alipay', // 默认选中支付宝（非微信小程序环境）
			paymentMethods: [
				{
					name: '支付宝',
					value: 'alipay',
					iconText: '支',
					disabled: false // 将在onLoad中根据平台设置
				},
				{
					name: '微信',
					value: 'wechat',
					iconText: '✓',
					disabled: false
				}
			],
			existingPaymentOrderId: null, // 已存在的支付订单ID
			existingOrderId: null, // 已存在的商品订单ID
			hasExistingOrder: false, // 是否有已存在的订单
			existingOrderDetail: null, // 已存在订单的详情
			paymentConfig: null, // 支付全局配置
			isFirstLoad: true // 是否是首次加载（用于控制onShow时是否重新加载配置）
		};
	},
	computed: {
		// 可见的支付方式列表（根据平台过滤）
		visiblePaymentMethods() {
			// #ifdef MP-WEIXIN
			// 微信小程序环境：只显示微信支付，隐藏支付宝
			return this.paymentMethods.filter(item => item.value !== 'alipay');
			// #endif
			
			// #ifdef MP-ALIPAY
			// 支付宝小程序环境：只显示支付宝，隐藏微信支付
			return this.paymentMethods.filter(item => item.value !== 'wechat');
			// #endif
			
			// #ifndef MP-WEIXIN || MP-ALIPAY
			// 其他环境（H5、APP等）：显示所有支付方式
			return this.paymentMethods;
			// #endif
		},
		
		// 已建支付单后锁定支付方式；仅有商品订单时仍可改支付方式再建支付单
		paymentMethodLocked() {
			return this.hasExistingOrder && !!this.existingPaymentOrderId
		},
		
		// 是否可以提交订单
		canSubmit() {
			// 已有待支付流程：允许点击「去支付」（跳转或补建支付单）
			if (this.hasExistingOrder) {
				if (!this.existingOrderId) return false
				if (!this.paymentConfig || !this.paymentConfig.paymentEnabled) return false
				if (this.productInfo.postType !== 0 && !this.selectedAddress) return false
				if (this.existingPaymentOrderId) return true
				const allDisabled = this.paymentMethods.every(item => item.disabled)
				if (allDisabled) return false
				const selectedMethod = this.paymentMethods.find(item => item.value === this.selectedPayment)
				if (selectedMethod && selectedMethod.disabled) return false
				return true
			}
			
			// 如果没有选择收货地址（邮寄商品需要地址）
			if (this.productInfo.postType !== 0 && !this.selectedAddress) return false;
			
			// 如果支付功能关闭，不能提交
			if (!this.paymentConfig || !this.paymentConfig.paymentEnabled) return false;
			
			// 如果所有支付方式都被禁用，不能提交
			const allDisabled = this.paymentMethods.every(item => item.disabled);
			if (allDisabled) return false;
			
			// 如果选中的支付方式被禁用，不能提交
			const selectedMethod = this.paymentMethods.find(item => item.value === this.selectedPayment);
			if (selectedMethod && selectedMethod.disabled) return false;
			
			return true;
		},
		// 提交按钮文本
		submitButtonText() {
			if (this.hasExistingOrder) return '去支付';
			if (this.productInfo.postType !== 0 && !this.selectedAddress) return '请选择收货地址';
			
			// 统一提示：全局支付关闭 或 所有支付方式都不可用
			if (!this.paymentConfig || !this.paymentConfig.paymentEnabled) return '支付功能已关闭';
			
			const allDisabled = this.paymentMethods.every(item => item.disabled);
			if (allDisabled) return '支付功能已关闭';
			
			const selectedMethod = this.paymentMethods.find(item => item.value === this.selectedPayment);
			if (selectedMethod && selectedMethod.disabled) return '当前支付方式不可用';
			
			return '提交订单';
		}
	},
	onLoad(options) {
		const systemInfo = uni.getSystemInfoSync();
		this.applyWeixinNavBar(systemInfo, 44);
		this.scrollHeight = systemInfo.windowHeight - 60;
		
		// 获取参数
		this.productId = options.productId || '';
		this.quantity = parseInt(options.quantity) || 1;
		
		// 在微信小程序环境下，默认选中微信支付
		// #ifdef MP-WEIXIN
		this.selectedPayment = 'wechat';
		// #endif
		
		// 在支付宝小程序环境下，默认选中支付宝
		// #ifdef MP-ALIPAY
		this.selectedPayment = 'alipay';
		// #endif
		
		// 加载支付配置（首次加载）
		this.loadPaymentConfig();
		
		// 加载商品信息
		this.loadProductInfo();
		
		// 加载默认收货地址
		this.loadDefaultAddress();
		
		// 检查是否有已存在的未支付订单
		this.checkExistingOrder();
		
		// 监听地址选择事件
		uni.$on('selectAddress', this.handleAddressSelect);
	},
	
	onShow() {
		// 如果不是首次加载（即从其他页面返回），重新加载配置
		// 这样当管理端修改配置后，用户返回页面时会自动刷新
		if (!this.isFirstLoad) {
			console.log('从其他页面返回，重新加载支付配置');
			this.loadPaymentConfig();
		}
		// 标记首次加载已完成
		this.isFirstLoad = false;
	},
	
	onUnload() {
		// 页面卸载时移除事件监听
		uni.$off('selectAddress', this.handleAddressSelect);
	},
	methods: {
		// 加载支付配置
		async loadPaymentConfig() {
			try {
				const res = await getPaymentGlobalConfig();
				console.log('支付全局配置:', res);
				
				if (res.code === 200 && res.data) {
					this.paymentConfig = res.data;
					
					// 检查全局支付开关
					if (!this.paymentConfig.paymentEnabled) {
						// 全局支付关闭，禁用所有支付方式
						this.paymentMethods.forEach(item => {
							item.disabled = true;
						});
						// 不在这里弹窗，让用户可以正常浏览页面，只在提交订单时提示
						return;
					}
					
					// 根据配置更新支付方式的可用状态
					this.paymentMethods.forEach(item => {
						if (item.value === 'wechat') {
							// 微信支付
							item.disabled = !this.paymentConfig.wechatQrcodeEnabled;
						} else if (item.value === 'alipay') {
							// 支付宝支付（沙箱或扫码，只要有一个可用就可以选择）
							const alipayAvailable = this.paymentConfig.alipaySandboxEnabled || this.paymentConfig.alipayQrcodeEnabled;
							item.disabled = !alipayAvailable;
						}
					});
					
					// 检查是否所有可见的支付方式都被禁用
					const allDisabled = this.visiblePaymentMethods.every(item => item.disabled);
					if (!allDisabled) {
						// 如果当前选中的支付方式被禁用或不在可见列表中，自动选择第一个可用的支付方式
						const selectedMethod = this.visiblePaymentMethods.find(item => item.value === this.selectedPayment);
						
						if (!selectedMethod || selectedMethod.disabled) {
							// 选择第一个可用的支付方式
							const firstAvailable = this.visiblePaymentMethods.find(item => !item.disabled);
							if (firstAvailable) {
								this.selectedPayment = firstAvailable.value;
							}
						}
					}
				}
			} catch (error) {
				console.error('加载支付配置失败:', error);
				// 加载失败时不影响页面使用，使用默认配置
			}
		},
		
		// 加载商品信息
		async loadProductInfo() {
			try {
				const res = await getProductById({
					productId: this.productId
				});
				
				console.log('商品详情:', res);
				
				if (res.code === 200 && res.data) {
					this.productInfo = {
						...res.data,
						coverPicture: res.data.cover || res.data.coverPicture,
						price: res.data.price || 0,
						title: res.data.title || '商品',
						postType: res.data.postType || 0
					};
				}
			} catch (error) {
				console.error('加载商品信息失败:', error);
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			}
		},
		
		// 加载默认收货地址
		loadDefaultAddress() {
			// TODO: 调用API获取默认收货地址
			// 暂时使用模拟数据
			const addressList = uni.getStorageSync('addressList') || [];
			if (addressList.length > 0) {
				this.selectedAddress = addressList.find(addr => addr.isDefault) || addressList[0];
			}
		},
		
		// 选择收货地址
		selectAddress() {
			if (this.hasExistingOrder) {
				return; // 已有订单时不允许修改
			}
			uni.navigateTo({
				url: '/pkg-mine/pages/addressManagement/addressManagement?from=order'
			});
		},
		
		// 处理地址选择
		handleAddressSelect(address) {
			console.log('选中的地址:', address);
			// 更新选中的地址
			this.selectedAddress = {
				...address,
				receiverName: address.name || address.receiverName,
				detailAddress: address.address || address.detailAddress
			};
		},
		
		// 选择支付方式
		selectPayment(value) {
			if (this.paymentMethodLocked) {
				return;
			}
			// 检查该支付方式是否被禁用
			const paymentMethod = this.paymentMethods.find(item => item.value === value);
			if (paymentMethod && !paymentMethod.disabled) {
				this.selectedPayment = value;
			}
		},
		
		// 检查是否有已存在的未支付订单
		async checkExistingOrder() {
			try {
				const orderInfo = await this.findExistingUnpaidOrder();
				if (orderInfo && orderInfo.id) {
					this.existingOrderId = orderInfo.id;
					this.existingPaymentOrderId = orderInfo.paymentOrderId || null;
					this.hasExistingOrder = true;
					console.log('检测到已存在的未支付订单，订单ID:', orderInfo.id, '支付订单ID:', orderInfo.paymentOrderId || '(未创建)');
					await this.loadExistingOrderDetail(orderInfo.id);
				}
			} catch (error) {
				console.error('检查已存在订单失败:', error);
			}
		},
		
		// 加载已存在订单的详情并填充数据
		async loadExistingOrderDetail(orderId) {
			try {
				const res = await getOrderDetail(orderId);
				if (res.code === 200 && res.data) {
					this.existingOrderDetail = res.data;
					
					// 填充收货地址信息
					if (res.data.postAddress) {
						// 解析地址字符串（格式：省 市 区 详细地址）
						const addressParts = res.data.postAddress.split(' ');
						this.selectedAddress = {
							receiverName: res.data.postUsername || '',
							phone: res.data.postPhone || '',
							province: addressParts[0] || '',
							city: addressParts[1] || '',
							district: addressParts[2] || '',
							detailAddress: addressParts.slice(3).join(' ') || ''
						};
					}
					
					// 填充支付方式
					if (res.data.payType) {
						this.selectedPayment = res.data.payType === '1' ? 'wechat' : 'alipay';
					}
					
					// 填充商品信息（如果订单中有）
					if (res.data.productTitle) {
						this.productInfo = {
							...this.productInfo,
							title: res.data.productTitle,
							coverPicture: res.data.productCover || this.productInfo.coverPicture,
							price: res.data.productMoney ? (res.data.productMoney / 100) : this.productInfo.price
						};
					}
					
					console.log('订单详情加载完成，已填充数据');
				}
			} catch (error) {
				console.error('加载订单详情失败:', error);
			}
		},
		
		// 查询该商品的未支付订单
		async findExistingUnpaidOrder() {
			try {
				// 查询未支付订单列表
				const res = await getOrdersByUser({
					page: 1,
					limit: 50,
					status: 'unpaid' // 未支付状态
				});
				
				if (res.code === 200 && res.data && res.data.records) {
					const unpaidOrder = res.data.records.find(order => {
						const match = order.productId === this.productId ||
							(order.product && order.product.id === this.productId)
						if (!match) return false
						const st = String(order.orderStatus)
						return st === '0' || st === '1' || st === '2'
					})
					if (unpaidOrder) {
						console.log('找到已存在的未支付订单:', unpaidOrder)
						return unpaidOrder
					}
				}
			} catch (error) {
				console.error('查询未支付订单失败:', error);
			}
			return null;
		},
		
		// 提交订单
		async submitOrder() {
			// 再次检查支付配置（双重保险）
			// 统一提示：不管是全局支付关闭，还是所有支付方式都不可用，对用户来说结果一样
			if (!this.paymentConfig || !this.paymentConfig.paymentEnabled) {
				uni.showToast({
					title: '支付功能暂时关闭，暂时无法购买商品',
					icon: 'none',
					duration: 2500
				});
				return;
			}
			
			// 检查是否所有支付方式都被禁用
			const allDisabled = this.paymentMethods.every(item => item.disabled);
			if (allDisabled) {
				uni.showToast({
					title: '支付功能暂时关闭，暂时无法购买商品',
					icon: 'none',
					duration: 2500
				});
				return;
			}
			
			// 验证收货地址
			if (!this.selectedAddress) {
				uni.showToast({
					title: '请选择收货地址',
					icon: 'none'
				});
				return;
			}
			
			// 如果已有支付订单ID，直接跳转到支付页面
			if (this.existingPaymentOrderId) {
				console.log('使用已存在的支付订单:', this.existingPaymentOrderId);
				uni.navigateTo({
					url: `/pkg-others/pages/payEntry/payEntry?paymentOrderId=${this.existingPaymentOrderId}`
				});
				return;
			}
			
			// 仅有商品订单、尚未创建支付单：按当前支付方式补建支付单后跳转
			if (this.hasExistingOrder && this.existingOrderId) {
				uni.showLoading({ title: '准备支付...', mask: true })
				try {
					const payType = this.selectedPayment === 'wechat' ? '1' : '2'
					const paymentOrderRes = await createPaymentOrder(this.existingOrderId, payType)
					if (paymentOrderRes.code !== 200 || !paymentOrderRes.data) {
						throw new Error(paymentOrderRes.msg || paymentOrderRes.message || '创建支付订单失败')
					}
					this.existingPaymentOrderId = paymentOrderRes.data
					uni.hideLoading()
					uni.navigateTo({
						url: `/pkg-others/pages/payEntry/payEntry?paymentOrderId=${this.existingPaymentOrderId}`
					})
				} catch (error) {
					console.error('补建支付单失败:', error)
					uni.hideLoading()
					uni.showToast({
						title: (error && error.message) || '创建支付订单失败',
						icon: 'none'
					})
				}
				return
			}
			
			// 显示加载提示
			uni.showLoading({
				title: '提交订单中...',
				mask: true
			});
			
			try {
				// 1. 创建商品订单
				const province = this.selectedAddress.province || '';
				const city = this.selectedAddress.city || '';
				const district = this.selectedAddress.district || '';
				const detailAddress = this.selectedAddress.detailAddress || '';
				const addressStr = `${province} ${city} ${district} ${detailAddress}`.trim();
				
				const productOrderData = {
					productId: this.productId,
					username: this.selectedAddress.receiverName || this.selectedAddress.name,
					phone: this.selectedAddress.phone,
					address: addressStr
				};
				
				const productOrderRes = await createProductOrder(productOrderData);
				
				if (productOrderRes.code !== 200) {
					// 如果错误是"商品已售出"，尝试查找已存在的未支付订单
					if (productOrderRes.msg && (productOrderRes.msg.includes('已售出') || productOrderRes.msg.includes('售出'))) {
						console.log('商品已售出，尝试查找已存在的未支付订单...');
						const existingOrder = await this.findExistingUnpaidOrder();
						
						if (existingOrder && existingOrder.id) {
							this.existingOrderId = existingOrder.id;
							this.existingPaymentOrderId = existingOrder.paymentOrderId || null;
							this.hasExistingOrder = true;
							await this.loadExistingOrderDetail(existingOrder.id);
							
							uni.hideLoading();
							if (this.existingPaymentOrderId) {
								uni.showToast({ title: '订单已存在，跳转支付页面', icon: 'none', duration: 1500 });
								setTimeout(() => {
									uni.navigateTo({
										url: `/pkg-others/pages/payEntry/payEntry?paymentOrderId=${this.existingPaymentOrderId}`
									});
								}, 1500);
							} else {
								uni.showToast({ title: '订单已存在，请点击去支付', icon: 'none' });
							}
							return;
						} else {
							// 如果是其他用户的订单，显示友好提示
							uni.hideLoading();
							uni.showModal({
								title: '商品暂时不可用',
								content: '该商品有人正在购买中，订单有效期为15分钟。如果对方未完成支付，商品将自动释放。\n\n建议您稍后再试，或关注该商品等待释放。',
								showCancel: true,
								cancelText: '返回',
								confirmText: '稍后再试',
								success: (res) => {
									if (res.confirm) {
										// 稍后再试：刷新商品信息，延迟后重新尝试
										this.loadProductInfo();
										setTimeout(() => {
											uni.showToast({
												title: '请稍后再试',
												icon: 'none'
											});
										}, 1000);
									} else {
										// 返回：跳转回商品详情页
										uni.navigateBack();
									}
								}
							});
							return;
						}
					}
					throw new Error(productOrderRes.msg || '创建订单失败');
				}
				
				const productOrderId = productOrderRes.data;
				console.log('商品订单创建成功:', productOrderId);
				
				// 2. 创建支付订单
				// 支付类型：alipay -> 2, wechat -> 1
				const payType = this.selectedPayment === 'wechat' ? '1' : '2';
				const paymentOrderRes = await createPaymentOrder(productOrderId, payType);
				
				if (paymentOrderRes.code !== 200) {
					throw new Error(paymentOrderRes.msg || '创建支付订单失败');
				}
				
				const paymentOrderId = paymentOrderRes.data;
				console.log('支付订单创建成功:', paymentOrderId);
				
				// 保存支付订单ID，以便下次直接跳转
				this.existingPaymentOrderId = paymentOrderId;
				
				// 3. 跳转到支付页面
				uni.hideLoading();
				uni.navigateTo({
					url: `/pkg-others/pages/payEntry/payEntry?paymentOrderId=${paymentOrderId}`
				});
				
			} catch (error) {
				console.error('提交订单失败:', error);
				uni.hideLoading();
				uni.showToast({
					title: error.message || '提交订单失败，请稍后重试',
					icon: 'none',
					duration: 2000
				});
			}
		},
		
		// 返回
		goBack() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped lang="scss">
.confirm-order-page {
	width: 100vw;
	min-height: 100vh;
	background-color: #f5f5f5;
}

.navbar {
	position: fixed;
	width: 100%;
	height: 44px;
	background-color: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 15px;
	border-bottom: 1px solid #eee;
	z-index: 9999;
	
	.back-btn {
		width: 40px;
		height: 40px;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.navbar-title {
		flex: 1;
		text-align: center;
		font-size: 17px;
		font-weight: 500;
		color: #333;
	}
	
	.placeholder {
		width: 40px;
	}
}

.address-section {
	margin: 20rpx;
	padding: 30rpx;
	background-color: #fff;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	
	&.address-disabled {
		opacity: 0.7;
		cursor: not-allowed;
	}
	
	.address-info {
		flex: 1;
		
		.address-header {
			display: flex;
			align-items: center;
			margin-bottom: 20rpx;
			
			.receiver-name {
				font-size: 32rpx;
				font-weight: 500;
				color: #333;
				margin-right: 20rpx;
			}
			
			.receiver-phone {
				font-size: 28rpx;
				color: #666;
			}
		}
		
		.address-detail {
			font-size: 28rpx;
			color: #666;
			line-height: 1.6;
		}
	}
	
	.no-address {
		flex: 1;
		display: flex;
		align-items: center;
		gap: 10rpx;
		
		.no-address-text {
			font-size: 28rpx;
			color: #999;
		}
	}
}

.product-section,
.delivery-section,
.cost-section {
	margin: 20rpx;
	padding: 30rpx;
	background-color: #fff;
	border-radius: 16rpx;
	
	.section-title {
		font-size: 32rpx;
		font-weight: 500;
		color: #333;
		margin-bottom: 20rpx;
	}
}

.remark-section {
	margin: 20rpx;
	padding: 25rpx 30rpx;
	background-color: #fff;
	border-radius: 16rpx;
	
	.section-title {
		font-size: 32rpx;
		font-weight: 500;
		color: #333;
		margin-bottom: 15rpx;
	}
}

.product-item {
	display: flex;
	gap: 20rpx;
	
	.product-image {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		flex-shrink: 0;
	}
	
	.product-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		
		.product-title {
			font-size: 28rpx;
			color: #333;
			line-height: 1.4;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			overflow: hidden;
		}
		
		.product-price-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			
			.product-price {
				font-size: 32rpx;
				font-weight: 600;
				color: #ff2442;
			}
			
			.product-quantity {
				font-size: 28rpx;
				color: #999;
			}
		}
	}
}

.delivery-item {
	display: flex;
	align-items: center;
	gap: 20rpx;
	
	.delivery-label {
		font-size: 28rpx;
		color: #333;
	}
	
	.delivery-tag {
		padding: 6rpx 16rpx;
		background-color: #e8f5e8;
		color: #52c41a;
		font-size: 24rpx;
		border-radius: 8rpx;
	}
}

.payment-section {
	margin: 20rpx;
	padding: 30rpx;
	background-color: #fff;
	border-radius: 16rpx;
	
	.section-title {
		font-size: 32rpx;
		font-weight: 500;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.payment-list {
		.payment-item {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 15rpx 0;
			border-bottom: 1px solid #f5f5f5;
			
			&:last-child {
				border-bottom: none;
			}
			
			&.payment-item-disabled {
				opacity: 0.5;
				cursor: not-allowed;
				
				&:active {
					opacity: 0.5;
				}
			}
			
			.payment-left {
				display: flex;
				align-items: center;
				gap: 20rpx;
				
				.payment-icon {
					width: 50rpx;
					height: 50rpx;
					border-radius: 8rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					flex-shrink: 0;
					
					.icon-text {
						font-size: 28rpx;
						font-weight: bold;
						color: #fff;
					}
					
					.icon-image {
						width: 100%;
						height: 100%;
						border-radius: inherit;
					}
					
					&.icon-alipay {
						background: transparent;
						border: none;
					}
					
					&.icon-wechat {
						background: transparent;
						border: none;
					}
					
					&.icon-disabled {
						opacity: 0.5;
						background: #d9d9d9 !important;
					}
				}
				
				.payment-name {
					font-size: 30rpx;
					color: #333;
					font-weight: 500;
					
					&.payment-name-disabled {
						color: #999;
					}
				}
			}
			
			.payment-radio {
				.radio-check {
					width: 40rpx;
					height: 40rpx;
					border: 2px solid #ddd;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					transition: all 0.3s;
					
					&.radio-active {
						background-color: #3d8af5;
						border-color: #3d8af5;
						
						.check-icon {
							color: #fff;
							font-size: 24rpx;
							font-weight: bold;
						}
					}
					
					&.radio-disabled {
						border-color: #d9d9d9;
						background-color: #f5f5f5;
					}
				}
			}
		}
	}
}

.remark-input {
	width: 100%;
	min-height: 80rpx;
	max-height: 200rpx;
	padding: 15rpx 20rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
	box-sizing: border-box;
	
	&:disabled {
		opacity: 0.6;
		background-color: #f0f0f0;
	}
}

.cost-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
	
	.cost-label {
		font-size: 28rpx;
		color: #666;
	}
	
	.cost-value {
		font-size: 28rpx;
		color: #333;
	}
}

.submit-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 60px;
	background-color: #fff;
	border-top: 1px solid #eee;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
	
	.total-section {
		.total-label {
			font-size: 28rpx;
			color: #666;
		}
		
		.total-price {
			font-size: 36rpx;
			font-weight: 600;
			color: #ff2442;
		}
	}
	
	.submit-btn {
		width: 240rpx;
		height: 70rpx;
		background-color: #3d8af5;
		border-radius: 50rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 30rpx;
		font-weight: 500;
	}
	
	.submit-btn-disabled {
		background-color: #cccccc !important;
		opacity: 0.6;
	}
}
</style>
