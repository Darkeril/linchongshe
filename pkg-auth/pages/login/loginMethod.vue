<template>
  <view class="login-method-container">
    <view class="content">
      <!-- Logo -->
      <view class="logo-wrapper">
        <image :src="logoImage" class="logo" mode="aspectFit"></image>
      </view>

      <!-- 欢迎文字 -->
      <!-- <view class="welcome-text">
				<text>欢迎来到爱宠社</text>
			</view> -->

      <!-- 登录按钮 -->
      <view class="login-buttons">
        <!-- 微信一键登录：仅微信小程序端显示 -->
        <!-- #ifdef MP-WEIXIN -->
        <button
          class="wechat-login-btn"
          open-type="getPhoneNumber"
          @getphonenumber="handleGetPhoneNumber"
        >
          <text>微信一键登录</text>
        </button>
        <!-- #endif -->

        <!-- 手机号登录 -->
        <button class="phone-login-btn" @click="handlePhoneLogin">
          <text>手机号登录</text>
        </button>
      </view>

      <!-- 用户协议 -->
      <view class="agreement-container">
        <view class="checkbox-wrapper" @click="toggleAgreement">
          <view :class="['checkbox', agreementChecked ? 'checked' : '']">
            <text v-if="agreementChecked" class="check-icon">✓</text>
          </view>
        </view>
        <text class="agreement-text">
          我已阅读
          <text class="link-text" @click.stop="handleUserManual">用户手册</text>
          和
          <text class="link-text" @click.stop="handlePrivacyPolicy">隐私政策</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script>
import { wechatMiniProgramLogin } from '@/apis/auth_apis.js';

export default {
  data() {
    return {
      logoImage: '/static/logoImage.png',
      agreementChecked: false,
      isLogging: false, // 登录中状态
      phoneCode: null // 手机号授权code
    };
  },
  methods: {
    // 切换协议勾选状态
    toggleAgreement() {
      this.agreementChecked = !this.agreementChecked;
    },

    // 处理获取手机号（微信小程序）
    async handleGetPhoneNumber(e) {
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户手册和隐私政策',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      // 防止重复点击
      if (this.isLogging) {
        return;
      }

      // 检查是否获取到手机号授权
      if (e.detail.errMsg && e.detail.errMsg !== 'getPhoneNumber:ok') {
        console.warn('用户拒绝授权手机号:', e.detail.errMsg);
        // 用户拒绝授权手机号，仍然可以登录，只是没有手机号
        this.phoneCode = null;
        await this.wechatMiniProgramLogin();
        return;
      }

      // 保存手机号授权code
      this.phoneCode = e.detail.code;

      // 继续登录流程
      await this.wechatMiniProgramLogin();
    },

    // 处理微信登录（非小程序环境或没有手机号授权时）
    async handleWechatLogin() {
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户手册和隐私政策',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      // 防止重复点击
      if (this.isLogging) {
        return;
      }

      this.isLogging = true;

      try {
        // #ifdef MP-WEIXIN
        // 微信小程序环境（这种情况不应该发生，因为小程序应该使用getPhoneNumber）
        await this.wechatMiniProgramLogin();
        // #endif

        // #ifdef H5
        // H5环境（微信H5网页授权）
        uni.showToast({
          title: 'H5环境暂不支持微信登录',
          icon: 'none'
        });
        this.isLogging = false;
        // #endif

        // #ifdef APP-PLUS
        // App环境（微信开放平台SDK）
        uni.showToast({
          title: 'App环境暂不支持微信登录',
          icon: 'none'
        });
        this.isLogging = false;
        // #endif
      } catch (error) {
        console.error('微信登录失败:', error);
        this.isLogging = false;
      }
    },

    // 微信小程序登录
    async wechatMiniProgramLogin() {
      try {
        // 1. 先获取用户信息（必须在用户点击事件的同步代码中调用）
        let userInfo = null;
        try {
          const userProfileRes = await new Promise((resolve, reject) => {
            uni.getUserProfile({
              desc: '用于完善用户资料', // 声明获取用户个人信息后的用途
              success: resolve,
              fail: reject
            });
          });

          userInfo = {
            nickName: userProfileRes.userInfo.nickName,
            avatarUrl: userProfileRes.userInfo.avatarUrl,
            gender: userProfileRes.userInfo.gender || 0,
            country: userProfileRes.userInfo.country || '',
            province: userProfileRes.userInfo.province || '',
            city: userProfileRes.userInfo.city || ''
          };
        } catch (err) {
          console.warn('用户拒绝授权或获取用户信息失败:', err);
          // 用户拒绝授权时，仍然可以使用code登录，但无法获取用户信息
        }

        // 2. 获取微信登录code
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          });
        });

        if (!loginRes.code) {
          throw new Error('获取微信登录code失败');
        }

        // 3. 调用后端登录接口
        uni.showLoading({
          title: '登录中...',
          mask: true
        });

        const res = await wechatMiniProgramLogin({
          code: loginRes.code,
          userInfo: userInfo,
          phoneCode: this.phoneCode // 传递手机号授权code
        });

        // 登录成功后清空phoneCode
        this.phoneCode = null;

        uni.hideLoading();

        if (res.code === 200) {
          // 登录成功
          const { userInfo: userData, accessToken, refreshToken } = res.data;

          console.log('微信登录成功，AccessToken:', accessToken ? '存在' : '不存在');
          console.log('微信登录成功，RefreshToken:', refreshToken ? '存在' : '不存在');

          // 验证 token 是否有效
          if (
            !accessToken ||
            accessToken === '' ||
            accessToken === 'undefined' ||
            accessToken === 'null'
          ) {
            console.error('微信登录返回的Token无效:', accessToken);
            throw new Error('登录失败：Token无效');
          }

          // 保存用户信息和token
          const userInfoData = {
            avatarUrl: userData.avatar || userInfo?.avatarUrl || '',
            nickname: userData.username || userInfo?.nickName || '微信用户',
            id: parseInt(userData.id, 10) || 0,
            hsId: userData.hsId || '',
            ipAddr: userData.address || '',
            selfIntroduction: userData.description || '',
            age: userData.age || null,
            sex: userData.gender !== undefined ? parseInt(userData.gender, 10) : null,
            attentionNum: parseInt(userData.followerCount) || 0,
            fansNum: parseInt(userData.fanCount) || 0,
            homePageBackground: userData.userCover || '',
            area: userData.address || '',
            phoneNumber: userData.phone || '',
            tags: userData.tags ? JSON.parse(userData.tags) : [],
            openid: userData.openId || userData.openid || null // 保存openid，用于小程序支付
          };

          try {
            // 持久化存储用户信息、accessToken 和 refreshToken
            uni.setStorageSync('userInfo', userInfoData);
            uni.setStorageSync('uniapp_token', accessToken);

            // 保存 refreshToken（如果存在）
            if (
              refreshToken &&
              refreshToken !== '' &&
              refreshToken !== 'undefined' &&
              refreshToken !== 'null'
            ) {
              uni.setStorageSync('uniapp_refresh_token', refreshToken);
              console.log('RefreshToken已保存');
            }

            // 验证存储是否成功
            const savedToken = uni.getStorageSync('uniapp_token');
            const savedUserInfo = uni.getStorageSync('userInfo');

            console.log('Token存储验证:', savedToken === accessToken ? '成功' : '失败');
            console.log('用户信息存储验证:', savedUserInfo ? '成功' : '失败');

            if (savedToken !== accessToken || !savedUserInfo) {
              throw new Error('Token或用户信息存储失败');
            }

            uni.showToast({
              title: '登录成功',
              icon: 'success',
              duration: 1500
            });

            // 延迟跳转，确保toast显示
            setTimeout(() => {
              this.isLogging = false;
              uni.reLaunch({
                url: '/pages/index/index'
              });
            }, 1500);
          } catch (e) {
            console.error('存储登录信息失败:', e);
            throw new Error('登录失败：无法保存登录状态');
          }
        } else {
          throw new Error(res.msg || '登录失败');
        }
      } catch (error) {
        uni.hideLoading();
        console.error('微信登录错误:', error);
        uni.showToast({
          title: error.message || '登录失败，请重试',
          icon: 'none',
          duration: 2000
        });
        this.isLogging = false;
      }
    },

    // 处理手机号登录
    handlePhoneLogin() {
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户手册和隐私政策',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      uni.navigateTo({
        url: '/pkg-auth/pages/login/login'
      });
    },

    // 查看用户手册
    handleUserManual() {
      uni.showToast({
        title: '用户手册',
        icon: 'none'
      });
      // TODO: 跳转到用户手册页面
    },

    // 查看隐私政策
    handlePrivacyPolicy() {
      uni.showToast({
        title: '隐私政策',
        icon: 'none'
      });
      // TODO: 跳转到隐私政策页面
    }
  },

  onShow() {
    const t = uni.getStorageSync('uniapp_token');
    if (t && t !== '' && t !== 'undefined' && t !== 'null') {
      uni.reLaunch({ url: '/pages/index/index' });
    }
  }
};
</script>

<style scoped>
.login-method-container {
  width: 100%;
  height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 60rpx;
  margin-top: -10vh;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 80rpx;
}

.logo {
  width: 200rpx;
  height: 200rpx;
}

.welcome-text {
  font-size: 48rpx;
  color: #333333;
  font-weight: 600;
  margin-bottom: 120rpx;
}

.login-buttons {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
  margin-bottom: 80rpx;
}

.wechat-login-btn {
  width: 100%;
  height: 100rpx;
  line-height: 100rpx;
  background: #3d8af5;
  color: #ffffff;
  border-radius: 50rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wechat-login-btn::after {
  border: none;
}

.phone-login-btn {
  width: 100%;
  height: 100rpx;
  line-height: 100rpx;
  background: #ffffff;
  color: #3d8af5;
  border: 2rpx solid #3d8af5;
  border-radius: 50rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-login-btn::after {
  border: none;
}

.agreement-container {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #999999;
}

.checkbox-wrapper {
  margin-right: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #cccccc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.checkbox.checked {
  background: #3d8af5;
  border-color: #3d8af5;
}

.check-icon {
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
}

.agreement-text {
  font-size: 24rpx;
  color: #999999;
}

.link-text {
  color: #3d8af5;
}
</style>
