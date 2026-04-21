<template>
	<view>
		<!-- 状态栏背景（H5 需占位，微信端由系统栏展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: statusBarHeight + 'px'}"
			style="position: fixed;top: 0;width: 100%;background-color: #ffffff;z-index: 10000;"></view>
		<!-- #endif -->
		<!-- 头部导航（H5 显示标题和返回，微信端由系统栏展示） -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="header" :style="{top: navBarTop, position: 'fixed', width: '100%', zIndex: 9999}">
			<view class="header-left" @click="goBack">
				<u-icon name="arrow-left" size="20" color="#333"></u-icon>
			</view>
			<view class="header-title">账号与安全</view>
			<view class="header-right"></view>
		</view>
		<!-- #endif -->
		<!-- 头部占位（微信端不预留） -->
		<!-- #ifndef MP-WEIXIN -->
		<view :style="{height: navPlaceholderHeight}"></view>
		<!-- #endif -->
		<view style="padding: 40rpx;">
			<u-cell-group :customStyle="{'backgroundColor': '#ffffff','borderRadius': '40rpx','padding':'10rpx'}"
				:border="false">
				<u-cell title="手机号" :isLink="true" :value="userInfo.hidePhone" @click="showPhoneTips"></u-cell>
				<u-cell :border="false" title="修改密码" :isLink="true" @click="goToReset"></u-cell>
			</u-cell-group>
		</view>
		<view style="padding: 0 40rpx;">
			<u-cell-group :customStyle="{'backgroundColor': '#ffffff','borderRadius': '40rpx','padding':'10rpx'}"
				:border="false">
				<u-cell :border="false" title="微信账号" :isLink="true" :value="userInfo.isBindWechat?'已绑定':'未绑定'"
					@click="bindThird(0)"></u-cell>
				<!-- <u-cell :border="false" title="QQ账号" :isLink="true" :value="userInfo.isBindQQ?'已绑定':'未绑定'"
					@click="bindThird(1)"></u-cell> -->
				<!-- <u-cell :border="false" title="Facebook账号" :isLink="true" :value="userInfo.isBindFacebook?'已绑定':'未绑定'"
					@click="bindThird(2)"></u-cell> -->
			</u-cell-group>
		</view>
		<view style="padding: 40rpx;">
			<u-cell-group :customStyle="{'backgroundColor': '#ffffff','borderRadius': '40rpx','padding':'10rpx'}"
				:border="false">
				<u-cell :border="false" title="登陆设备信息" :isLink="true" @click="openDeviceInfo"></u-cell>
			</u-cell-group>
		</view>
		<view style="padding: 0 40rpx;">
			<u-cell-group :customStyle="{'backgroundColor': '#ffffff','borderRadius': '40rpx','padding':'10rpx'}"
				:border="false">
				<u-cell :border="false" title="注销账号" :isLink="true" @click="confirmCancelAccount"></u-cell>
			</u-cell-group>
		</view>
		<view @click="logout"
			style="margin: 40rpx;padding: 20rpx 40rpx;margin-top: 80rpx;text-align: center;width: 600rpx;background-color: #ffffff;border-radius: 40rpx;height: 60rpx;line-height: 60rpx;color: #ff4d4f;font-weight: 600;">
			退出登录
		</view>
		<view @touchmove.stop.prevent="moveHandle">
			<u-popup :show="viewSystemctlInfo" mode="center" :customStyle="{'borderRadius': '40rpx'}"
				@close="viewSystemctlInfo=false">
				<view style="padding: 40rpx;text-align: center;width: 500rpx;">
					<view style="margin-bottom: 20rpx;">设备信息</view>
					<view style="margin-bottom: 20rpx;">{{phoneInfo.systemctlInfo}}</view>
					<view>系统版本：{{userInfo.appVersion}}</view>
				</view>
			</u-popup>
		</view>
	</view>
</template>

<script>
	import {
		getUserInfo,
		getUserIsBindThird
	} from '@/apis/user_service.js'
	import {
		logout
	} from '@/apis/auth_apis.js'
	import { cancelAccount } from '@/apis/user_service.js'
	import weixinNavBar from '@/utils/weixinNavBar.js'

	export default {
		mixins: [weixinNavBar],
		data() {
			return {
				statusBarHeight: 0,
				userInfo: {},
				phoneInfo: {},
				viewSystemctlInfo: false,
			}
		},
		methods: {
			applyPhoneMask() {
				const rawPhone = this.userInfo.phoneNumber || this.userInfo.phone || ''
				if (!rawPhone) return
				this.userInfo.hidePhone = '+86' + rawPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
			},
			async refreshUserInfoFromServer() {
				const cached = uni.getStorageSync('userInfo') || {}
				this.userInfo = cached
				this.applyPhoneMask()

				const userId = this.userInfo.id
				if (!userId) return
				try {
					const res = await getUserInfo({ userId })
					// res.data 可能是 WebUser（phone 字段）
					if (res && res.code === 200 && res.data) {
						const next = { ...this.userInfo, ...res.data }
						// 兼容 phone/phoneNumber 两种字段
						if (next.phone && !next.phoneNumber) next.phoneNumber = next.phone
						this.userInfo = next
						this.applyPhoneMask()
						uni.setStorageSync('userInfo', this.userInfo)
					}
				} catch (e) {
					// 忽略，仍使用缓存
				}
			},
			goBack() {
				uni.navigateBack()
			},
			moveHandle(e) {},
			openDeviceInfo() {
				uni.getSystemInfo({
					success: (res) => {
						this.phoneInfo.systemctlInfo = `${res.deviceBrand || ''} ${res.deviceModel || ''} ${res.system || ''}`.trim()
						this.userInfo.appVersion = res.appVersion || this.userInfo.appVersion || ''
						this.viewSystemctlInfo = true
					},
					fail: () => {
						this.viewSystemctlInfo = true
					}
				})
			},
			confirmCancelAccount() {
				uni.showModal({
					title: '注销账号',
					content: '注销后账号将无法登录，相关数据可能不可恢复，是否继续？',
					confirmText: '继续注销',
					cancelText: '取消',
					success: (res) => {
						if (res.confirm) {
							this.doCancelAccount()
						}
					}
				})
			},
			doCancelAccount() {
				cancelAccount().then(() => {
					uni.showToast({ title: '已注销', icon: 'none', duration: 1200 })
					// 清除本地登录态并回到登录页
					uni.removeStorageSync('uniapp_token')
					uni.removeStorageSync('uniapp_refresh_token')
					uni.removeStorageSync('userInfo')
					setTimeout(() => {
						uni.reLaunch({ url: '/pkg-auth/pages/login/login' })
					}, 300)
				}).catch((err) => {
					uni.showToast({ title: err?.msg || err?.message || '注销失败', icon: 'none' })
				})
			},
			bindThird(type) {
				let tipContent = '';
				let isBind = false;
				if (type == 0) {
					tipContent = '微信';
					isBind = this.userInfo.isBindWechat;
				} else if (type == 1) {
					tipContent = 'QQ';
					isBind = this.userInfo.isBindQQ;
				} else {
					tipContent = 'Facebook';
					isBind = this.userInfo.isBindFacebook;
				}
				if (isBind) {
					uni.showModal({
						title: '确认解绑？',
						content: '解绑' + tipContent + '账号后，将无法继续使用它登录该账号',
						success: (res) => {
							if (res.confirm) {
								uni.showToast({
									title: '解绑成功',
									icon: 'none'
								})
							}
						}
					})
				} else {
					// 暂时没有申请第三方登录的appid，暂时不做处理
					uni.showToast({
						title: '绑定' + tipContent + '账号',
						icon: 'none'
					})
					// this.$sqliteUtil.SqlExecute('drop table if exists praise_and_collection');
				}
			},
			goToReset() {
				uni.navigateTo({
					url: '/pkg-mine/pages/setting/resetPasswordMethods'
				})
			},
			showPhoneTips() {
				const phoneText = (
					this.userInfo.hidePhone ||
					((this.userInfo.phoneNumber || this.userInfo.phone) ? ('+86' + (this.userInfo.phoneNumber || this.userInfo.phone).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')) : '')
				).replace(/^\+86/, '')
				uni.showModal({
					title: '更换绑定的手机号？',
					content: '当前绑定的手机号为' + phoneText,
					confirmText: '确定',
					cancelText: '取消',
					success: (res) => {
						if (res.confirm) {
							uni.navigateTo({
								url: '/pkg-mine/pages/setting/updateBindPhone'
							})
						}
					}
				})
			},
			logout() {
				// this.$showModal({
				// 	title: "退出登录",
				// 	content: '确定要退出登录吗？',
				// 	align: "left", // 对齐方式 left/center/right
				// 	cancelText: "取消", // 取消按钮的文字
				// 	cancelColor: "#3d8af5", // 取消按钮颜色
				// 	confirmText: "确定", // 确认按钮文字
				// 	confirmColor: "#3d8af5", // 确认按钮颜色 
				// 	showCancel: true, // 是否显示取消按钮，默认为 true
				// }).then(res => {
				logout({
					userId: this.userInfo.id
				}).then(res => {
					console.log(res);
					// 清除所有登录相关的数据
					uni.removeStorageSync('uniapp_token');
					uni.removeStorageSync('uniapp_refresh_token'); // 清除 refreshToken，防止自动刷新
					uni.removeStorageSync('userInfo');
					// this.$ws.completeClose();
					// this.$sqliteUtil.closeSqlite();
					uni.reLaunch({
						url: '/pkg-auth/pages/login/login'
					})
				}).catch(err => {
					// 即使退出登录接口失败，也要清除本地数据
					console.error('退出登录接口失败:', err);
					uni.removeStorageSync('uniapp_token');
					uni.removeStorageSync('uniapp_refresh_token');
					uni.removeStorageSync('userInfo');
					uni.reLaunch({
						url: '/pkg-auth/pages/login/login'
					})
				})
				// })
			}
		},
		onShow() {
			// 进入页面时同步最新 userInfo，并主动拉取一次服务器数据，确保手机号首次就显示
			this.refreshUserInfoFromServer()
		},
		onLoad() {
			uni.getSystemInfo({
				success: (res) => {
					this.applyWeixinNavBar(res, 44)
				}
			})
			this.userInfo = uni.getStorageSync('userInfo');
			console.log(this.userInfo);
			// this.userInfo.hidePhone = '+86' + this.userInfo.phoneNumber.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
			// 手机号脱敏展示（兼容后端未返回 hidePhone）
			this.applyPhoneMask()
			getUserIsBindThird().then(res => {
				this.userInfo.isBindWechat = !!res.data?.wechatBind;
				this.userInfo.isBindQQ = !!res.data?.qqBind;
				this.userInfo.isBindFacebook = !!res.data?.facebookBind;
			}).catch(() => {})
		}
	}
</script>

<style lang="scss">
	page {
		background-color: #f5f5f5;
	}

	/* H5 头部导航（与编辑资料等页一致） */
	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 44px;
		padding: 0 15px;
		background-color: #fff;
		border-bottom: 1px solid #eee;
	}

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
</style>