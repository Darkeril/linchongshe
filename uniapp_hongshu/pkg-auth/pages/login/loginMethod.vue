<template>
  <view class="login-method-container" style="background-color: #F4EDE2;">
    <!-- 顶部径向渐变装饰 -->
    <view class="bg-radial"></view>

    <!-- 浮动宠物头像群（装饰） -->
    <view class="pet-avatars-float">
      <image
        class="pet-float pet-dog"
        src="/static/pet-avatars/dog.svg"
        mode="aspectFit"
      ></image>
      <image
        class="pet-float pet-cat"
        src="/static/pet-avatars/cat.svg"
        mode="aspectFit"
      ></image>
      <image
        class="pet-float pet-mochi"
        src="/static/pet-avatars/mochi.svg"
        mode="aspectFit"
      ></image>
      <image
        class="pet-float pet-puppy"
        src="/static/pet-avatars/puppy.svg"
        mode="aspectFit"
      ></image>
      <!-- 装饰小爪印 -->
      <image
        class="paw-deco-login"
        src="/static/icons/paw-primary.svg"
        mode="aspectFit"
      ></image>
    </view>

    <!-- Logo + 品牌名 -->
    <view class="brand-area">
      <view class="logo-circle">
        <image
          src="/static/icons/paw-white.svg"
          class="logo-paw"
          mode="aspectFit"
        ></image>
      </view>
      <text class="brand-name">爱宠社</text>
    </view>

    <!-- 欢迎文案 -->
    <view class="welcome-area">
      <text class="welcome-title">欢迎回来</text>
      <view class="welcome-desc">
        <text class="welcome-line">宠友都在这里，等你一起分享</text>
        <text class="welcome-line">萌瞬间、闲置好物和日常治愈</text>
      </view>
    </view>

    <!-- 登录按钮组 -->
    <view class="login-buttons">
      <!-- 微信一键登录：仅微信小程序端显示 -->
      <!-- #ifdef MP-WEIXIN -->
      <button
        class="btn-primary"
        open-type="getPhoneNumber"
        @getphonenumber="handleGetPhoneNumber"
      >
        <text class="btn-primary-text">微信一键登录</text>
      </button>
      <!-- #endif -->

      <!-- #ifndef MP-WEIXIN -->
      <!-- 非小程序端：手机号登录作为主按钮 -->
      <button class="btn-primary" @click="handlePhoneLogin">
        <text class="btn-primary-text">手机号登录 / 注册</text>
      </button>
      <!-- #endif -->

      <!-- #ifdef MP-WEIXIN -->
      <!-- 小程序端：手机号作为次按钮 -->
      <button class="btn-secondary" @click="handlePhoneLogin">
        <text class="btn-secondary-text">手机号登录 / 注册</text>
      </button>
      <!-- #endif -->
    </view>

    <!-- 底部用户协议 -->
    <view class="agreement-area">
      <view class="checkbox-wrapper" @click="toggleAgreement">
        <view :class="['agreement-checkbox', agreementChecked ? 'checked' : '']">
          <text v-if="agreementChecked" class="check-icon">✓</text>
        </view>
      </view>
      <text class="agreement-text">
        已阅读并同意
        <text class="link-text" @click.stop="handleUserManual"> 用户协议 </text>
        和
        <text class="link-text" @click.stop="handlePrivacyPolicy"> 隐私政策</text>
      </text>
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
      isLogging: false,
      phoneCode: null
    };
  },
  methods: {
    fixPageBackground() {
      // #ifdef H5
      this.$nextTick(() => {
        // UniApp H5 的页面容器元素，强制设置背景色防止刷新丢失
        const selectors = ['uni-page-body', 'uni-page-wrapper', 'uni-page'];
        selectors.forEach(tag => {
          const el = document.querySelector(tag);
          if (el) {
            el.style.backgroundColor = '#F4EDE2';
          }
        });
        // 也设置 body 兜底
        document.body.style.backgroundColor = '#F4EDE2';
      });
      // #endif
    },

    toggleAgreement() {
      this.agreementChecked = !this.agreementChecked;
    },

    async handleGetPhoneNumber(e) {
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户协议和隐私政策',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      if (this.isLogging) {
        return;
      }

      if (e.detail.errMsg && e.detail.errMsg !== 'getPhoneNumber:ok') {
        console.warn('用户拒绝授权手机号:', e.detail.errMsg);
        this.phoneCode = null;
        await this.wechatMiniProgramLogin();
        return;
      }

      this.phoneCode = e.detail.code;
      await this.wechatMiniProgramLogin();
    },

    async handleWechatLogin() {
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户协议和隐私政策',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      if (this.isLogging) {
        return;
      }

      this.isLogging = true;

      try {
        // #ifdef MP-WEIXIN
        await this.wechatMiniProgramLogin();
        // #endif

        // #ifdef H5
        uni.showToast({
          title: 'H5环境暂不支持微信登录',
          icon: 'none'
        });
        this.isLogging = false;
        // #endif

        // #ifdef APP-PLUS
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

    async wechatMiniProgramLogin() {
      try {
        let userInfo = null;
        try {
          const userProfileRes = await new Promise((resolve, reject) => {
            uni.getUserProfile({
              desc: '用于完善用户资料',
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
        }

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

        uni.showLoading({
          title: '登录中...',
          mask: true
        });

        const res = await wechatMiniProgramLogin({
          code: loginRes.code,
          userInfo: userInfo,
          phoneCode: this.phoneCode
        });

        this.phoneCode = null;

        uni.hideLoading();

        if (res.code === 200) {
          const { userInfo: userData, accessToken, refreshToken } = res.data;

          if (
            !accessToken ||
            accessToken === '' ||
            accessToken === 'undefined' ||
            accessToken === 'null'
          ) {
            console.error('微信登录返回的Token无效:', accessToken);
            throw new Error('登录失败：Token无效');
          }

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
            openid: userData.openId || userData.openid || null
          };

          try {
            uni.setStorageSync('userInfo', userInfoData);
            uni.setStorageSync('uniapp_token', accessToken);

            if (
              refreshToken &&
              refreshToken !== '' &&
              refreshToken !== 'undefined' &&
              refreshToken !== 'null'
            ) {
              uni.setStorageSync('uniapp_refresh_token', refreshToken);
            }

            const savedToken = uni.getStorageSync('uniapp_token');
            const savedUserInfo = uni.getStorageSync('userInfo');

            if (savedToken !== accessToken || !savedUserInfo) {
              throw new Error('Token或用户信息存储失败');
            }

            uni.showToast({
              title: '登录成功',
              icon: 'success',
              duration: 1500
            });

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

    handlePhoneLogin() {
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户协议和隐私政策',
          icon: 'none',
          duration: 2000
        });
        return;
      }

      uni.navigateTo({
        url: '/pkg-auth/pages/login/login'
      });
    },

    handleUserManual() {
      uni.showToast({
        title: '用户手册',
        icon: 'none'
      });
    },

    handlePrivacyPolicy() {
      uni.showToast({
        title: '隐私政策',
        icon: 'none'
      });
    }
  },

  onLoad() {
    this.fixPageBackground();
  },

  onShow() {
    this.fixPageBackground();
    const t = uni.getStorageSync('uniapp_token');
    if (t && t !== '' && t !== 'undefined' && t !== 'null') {
      uni.reLaunch({ url: '/pages/index/index' });
    }
  },

  mounted() {
    this.fixPageBackground();
  }
};
</script>

<!-- 非 scoped：设置 UniApp page 元素背景，防止刷新后闪白 -->
<style>
page {
  background-color: #F4EDE2 !important;
}
uni-page-body {
  background-color: #F4EDE2 !important;
}
</style>

<style scoped>
/* ─── 整体容器 ─── */
.login-method-container {
  width: 100%;
  min-height: 100vh;
  background: var(--acs-bg, #F4EDE2);
  position: relative;
  overflow: hidden;
}

/* ─── 顶部径向渐变装饰（设计稿 radial-gradient） ─── */
.bg-radial {
  position: absolute;
  top: -120rpx;
  left: -80rpx;
  right: -80rpx;
  height: 680rpx;
  background: radial-gradient(ellipse at 50% 60%, #EFCFB0 0%, #F4EDE2 70%);
  pointer-events: none;
}

/* ─── 浮动宠物头像群（设计稿 LoginScreen） ─── */
.pet-avatars-float {
  position: absolute;
  top: 140rpx;
  left: 0;
  right: 0;
  height: 560rpx;
  pointer-events: none;
}

.pet-float {
  position: absolute;
  border-radius: 50%;
}

/* 设计稿: top: 30, left: 40, rotate(-10deg), size 56 */
.pet-dog {
  width: 112rpx;
  height: 112rpx;
  top: 60rpx;
  left: 80rpx;
  transform: rotate(-10deg);
}

/* 设计稿: top: 10, right: 50, rotate(12deg), size 64 */
.pet-cat {
  width: 128rpx;
  height: 128rpx;
  top: 20rpx;
  right: 100rpx;
  transform: rotate(12deg);
}

/* 设计稿: top: 140, left: 20, rotate(-5deg), size 44 */
.pet-mochi {
  width: 88rpx;
  height: 88rpx;
  top: 280rpx;
  left: 40rpx;
  transform: rotate(-5deg);
}

/* 设计稿: top: 150, right: 30, rotate(8deg), size 48 */
.pet-puppy {
  width: 96rpx;
  height: 96rpx;
  top: 300rpx;
  right: 60rpx;
  transform: rotate(8deg);
}

/* 设计稿: top: 110, left: 40%, opacity 0.35, rotate(-20deg), size 22 */
.paw-deco-login {
  position: absolute;
  width: 44rpx;
  height: 44rpx;
  top: 220rpx;
  left: 40%;
  opacity: 0.35;
  transform: rotate(-20deg);
}

/* ─── Logo + 品牌名（设计稿: top: 210） ─── */
.brand-area {
  position: absolute;
  top: 420rpx;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  z-index: 2;
}

/* 设计稿: 72x72 圆形，gradient 135deg primary→primaryInk */
.logo-circle {
  width: 144rpx;
  height: 144rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #C97B4A, #8A4A1F);
  box-shadow: 0 20rpx 56rpx rgba(201, 123, 74, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-paw {
  width: 68rpx;
  height: 68rpx;
}

/* 设计稿: fontSize 28, fontWeight 700, letterSpacing 1.5 */
.brand-name {
  font-size: 56rpx;
  font-weight: 700;
  color: var(--acs-ink, #231710);
  letter-spacing: 3rpx;
}

/* ─── 欢迎文案（设计稿: top: 360） ─── */
.welcome-area {
  position: absolute;
  top: 720rpx;
  left: 64rpx;
  right: 64rpx;
  text-align: center;
}

/* 设计稿: fontSize 22, fontWeight 700 */
.welcome-title {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--acs-ink, #231710);
  letter-spacing: 1rpx;
  margin-bottom: 16rpx;
  display: block;
}

.welcome-desc {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

/* 设计稿: fontSize 14, color inkSoft, lineHeight 1.6 */
.welcome-line {
  font-size: 28rpx;
  color: var(--acs-ink-soft, #63463A);
  line-height: 1.6;
}

/* ─── 按钮组（设计稿: top: 470, left/right: 28） ─── */
.login-buttons {
  position: absolute;
  top: 940rpx;
  left: 56rpx;
  right: 56rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 设计稿: height 52, borderRadius radius*1.6, gradient primary→primaryInk */
.btn-primary {
  width: 100%;
  height: 104rpx;
  line-height: 104rpx;
  border-radius: 35rpx;
  background: linear-gradient(135deg, #C97B4A, #8A4A1F);
  box-shadow: 0 16rpx 40rpx rgba(201, 123, 74, 0.35),
              inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.btn-primary::after {
  border: none;
}

.btn-primary-text {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
}

/* 设计稿: height 52, bg surface, color primary, border 1.5px solid primarySoft */
.btn-secondary {
  width: 100%;
  height: 104rpx;
  line-height: 104rpx;
  border-radius: 35rpx;
  background: var(--acs-surface, #FFFFFF);
  border: 3rpx solid var(--acs-primary-soft, #EFCFB0);
  box-shadow: 0 8rpx 24rpx rgba(201, 123, 74, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-secondary::after {
  border: none;
}

.btn-secondary-text {
  color: var(--acs-primary, #C97B4A);
  font-size: 32rpx;
  font-weight: 600;
}

/* ─── 底部协议（设计稿: bottom: 40） ─── */
.agreement-area {
  position: absolute;
  bottom: 80rpx;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

/* 设计稿: 14x14 圆形 checkbox */
.checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.agreement-checkbox {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  border: 3rpx solid var(--acs-ink-faint, #C4AC95);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.agreement-checkbox.checked {
  background: var(--acs-primary, #C97B4A);
  border-color: var(--acs-primary, #C97B4A);
}

.check-icon {
  color: #ffffff;
  font-size: 18rpx;
  font-weight: bold;
}

/* 设计稿: fontSize 11, color inkMuted */
.agreement-text {
  font-size: 22rpx;
  color: var(--acs-ink-muted, #8F7260);
}

.link-text {
  color: var(--acs-primary, #C97B4A);
}
</style>
