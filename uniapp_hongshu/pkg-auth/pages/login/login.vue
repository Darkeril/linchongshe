<template>
  <view class="login">
    <view class="content">
      <!-- 头部logo和标题 -->
      <view class="header">
        <image :src="logoImage" class="logo-img"></image>
      </view>
      <view class="title">
        <view class="subtitle">爱宠社</view>
      </view>
      <!-- 登录方式切换 -->
      <view class="login-type-switch">
        <text
          :class="['switch-btn', loginType === 'password' ? 'active' : '']"
          @click="switchType('password')"
        >
          密码登录
        </text>
        <text
          :class="['switch-btn', loginType === 'code' ? 'active' : '']"
          @click="switchType('code')"
        >
          验证码登录
        </text>
      </view>
      <!-- 主体表单 -->
      <view class="main">
        <wInput
          v-model="phoneData"
          type="text"
          maxlength="11"
          placeholder="请输入手机号"
          :focus="isFocus"
          prefixIcon="phone"
        ></wInput>
        <wInput
          v-if="loginType === 'password'"
          v-model="passData"
          type="password"
          maxlength="20"
          placeholder="请输入密码"
          :isShowPass="true"
          prefixIcon="lock"
        ></wInput>
        <wInput
          v-else
          v-model="codeData"
          type="number"
          maxlength="6"
          placeholder="请输入验证码"
          prefixIcon="email"
          :isShowCode="true"
          :codeText="codeBtnText"
          :setTime="60"
          @setCode="getCode"
        ></wInput>
      </view>
      <wButton
        class="wbutton"
        :text="loginType === 'password' ? '登 录' : '登 录'"
        :rotate="isRotate"
        @click="startLogin"
      ></wButton>
      <!-- 底部信息 -->
      <view class="footer">
        <text>— 新用户直接登录（自动注册) —</text>
      </view>
      <!-- 演示账号登录 - 根据配置动态显示 -->
      <view v-if="demoAccountConfig.enabled" class="demo-login">
        <button class="demo-btn" @click="handleDemoLogin">
          <!-- #ifdef H5 -->
          <svg
            class="reds-icon"
            width="18"
            height="18"
            style="margin-right: 4px"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm0 10c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4zm0-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
            />
          </svg>
          <!-- #endif -->
          <!-- #ifndef H5 -->
          <text class="demo-icon">🎯</text>
          <!-- #endif -->
          <text class="demo-text">演示账号</text>
        </button>
      </view>
    </view>
    <!-- AJ-Captcha滑块弹窗 -->
    <Verify
      v-show="showVerifyDialog"
      ref="verify"
      id="verify"
      @success="onVerifySuccess"
      :mode="'pop'"
      :captchaType="'blockPuzzle'"
      :imgSize="{ width: '330px', height: '155px' }"
    />
  </view>
</template>

<script>
import wInput from '../../components/watch-login/watch-input.vue'; //input
import wButton from '../../components/watch-login/watch-button.vue'; //button
import Verify from '../../components/verify/Verify.vue'; // 使用子包内的 Verify 组件
import {
  generalLogin,
  loginByCode,
  sendSmsCode,
  demoLogin,
  getDemoAccountConfig
} from '@/apis/auth_apis.js';

export default {
  data() {
    return {
      logoImage: '/static/logoImage.png',
      phoneData: '', //手机号
      passData: '', //密码
      codeData: '', //验证码
      isRotate: false, //加载旋转
      isFocus: false, // 不自动聚焦，让用户手动点击输入框
      userInfo: {},
      loginType: 'password', // 'password' 或 'code'
      codeBtnText: '获取验证码',
      codeBtnDisabled: false,
      codeTimer: null,
      codeTime: 60,
      verifyAction: '', // 标记当前滑块用途：'login' 或 'code'
      captchaVerification: '', // 滑块校验串
      showVerifyDialog: false,
      // 演示账号配置
      demoAccountConfig: {
        enabled: false,
        username: '',
        password: '',
        description: '',
        permissions: [],
        expireTime: ''
      }
    };
  },
  components: {
    wInput,
    wButton,
    Verify
  },
  methods: {
    // 获取演示账号配置
    async fetchDemoAccountConfig() {
      try {
        const response = await getDemoAccountConfig();
        if (response.code === 200 && response.data) {
          this.demoAccountConfig = {
            enabled: response.data.enabled || false,
            username: response.data.username || '',
            password: response.data.password || '',
            description: response.data.description || '',
            permissions: response.data.permissions || [],
            expireTime: response.data.expireTime || ''
          };
        }
      } catch (error) {
        console.error('获取演示账号配置失败:', error);
        // 如果获取失败，保持默认配置（enabled: false）
      }
    },

    switchType(type) {
      this.loginType = type;
      this.passData = '';
      this.codeData = '';
      // 保存登录方式到本地，防止页面切换后丢失
      uni.setStorageSync('login_type_preference', type);
    },

    showVerify() {
      this.showVerifyDialog = true;
      let verifyComponent = this.$refs.verify;
      // #ifdef MP-WEIXIN
      if (!verifyComponent || !verifyComponent.show) {
        verifyComponent = this.selectComponent('#verify');
      }
      // #endif
      if (verifyComponent && verifyComponent.show) {
        verifyComponent.show();
      }
    },

    // 滑块校验成功回调
    onVerifySuccess(res) {
      this.showVerifyDialog = false;
      const captchaVerification = typeof res === 'string' ? res : res.captchaVerification;
      if (this.verifyAction === 'code') {
        this.sendSmsCode(captchaVerification);
      } else if (this.verifyAction === 'login') {
        if (this.loginType === 'password') {
          this.doPasswordLogin(captchaVerification);
        } else {
          this.doCodeLogin(captchaVerification);
        }
      }
      this.verifyAction = '';
    },

    // 密码登录实际逻辑
    doPasswordLogin(captchaVerification) {
      // 确保 captchaVerification 是字符串
      if (typeof captchaVerification !== 'string') {
        captchaVerification =
          captchaVerification && captchaVerification.captchaVerification
            ? captchaVerification.captchaVerification
            : '';
      }
      const phoneReg = /^(?=.*[0-9])(?=.*[a-zA-Z])(.{6,16})$/;
      if (
        !this.passData ||
        this.passData.length < 6 ||
        this.passData.length > 16 ||
        !phoneReg.test(this.passData)
      ) {
        uni.showToast({
          icon: 'none',
          title: '密码格式不正确'
        });
        return;
      }
      this.isRotate = true;
      generalLogin({
        phone: this.phoneData,
        password: this.passData,
        captchaVerification
      })
        .then(res => {
          this.isRotate = false;
          if (res.code == 200) {
            this.handleLoginSuccess(res.data);
          } else {
            uni.showToast({
              icon: 'none',
              title: res.msg
            });
          }
        })
        .catch(() => {
          this.isRotate = false;
        });
    },

    // 验证码登录
    doCodeLogin() {
      if (!this.phoneData) {
        uni.showToast({
          icon: 'none',
          title: '请输入手机号'
        });
        return;
      }
      if (!this.codeData || this.codeData.length !== 6) {
        uni.showToast({
          icon: 'none',
          title: '请输入6位验证码'
        });
        return;
      }
      this.isRotate = true;
      loginByCode({
        phone: this.phoneData,
        code: this.codeData,
        terminal: 'web'
        // 不再传 captchaVerification
      })
        .then(res => {
          this.isRotate = false;
          if (res.code == 200) {
            this.handleLoginSuccess(res.data);
          } else {
            uni.showToast({
              icon: 'none',
              title: res.msg
            });
          }
        })
        .catch(() => {
          this.isRotate = false;
        });
    },

    // 获取验证码
    getCode() {
      const reg = /^1[3456789]\d{9}$/;
      if (!reg.test(this.phoneData)) {
        uni.showToast({
          icon: 'none',
          title: '手机号格式不正确'
        });
        return;
      }
      this.verifyAction = 'code';
      this.showVerify(); // 弹出滑块
    },

    // 滑块弹窗回调
    onCaptchaMessage(e) {
      const verification = e.detail.data.verification;
      if (verification) {
        this.captchaVerification = verification;
        this.$refs.captchaPopup.close();
        this.sendSmsCode(verification);
      }
    },

    // 发送验证码
    async sendSmsCode(captchaVerification) {
      if (!this.phoneData) {
        uni.showToast({
          icon: 'none',
          title: '请输入手机号'
        });
        return;
      }
      try {
        const res = await sendSmsCode({
          phone: this.phoneData,
          captchaVerification
        });
        if (res.code === 200) {
          uni.showToast({
            title: '验证码已发送',
            icon: 'success'
          });
          this.startCodeTimer();
        } else {
          uni.showToast({
            title: res.msg,
            icon: 'none'
          });
        }
      } catch (e) {
        uni.showToast({
          title: '发送失败',
          icon: 'none'
        });
      }
    },

    startCodeTimer() {
      this.codeBtnDisabled = true;
      this.codeBtnText = `${this.codeTime}s`;

      // 保存倒计时开始时间，用于页面切换后恢复
      const startTime = Date.now();
      uni.setStorageSync('code_timer_start', startTime);

      this.codeTimer = setInterval(() => {
        this.codeTime--;
        this.codeBtnText = `${this.codeTime}s`;
        if (this.codeTime <= 0) {
          clearInterval(this.codeTimer);
          this.codeBtnText = '获取验证码';
          this.codeBtnDisabled = false;
          this.codeTime = 60;
          // 倒计时结束，清除保存的开始时间
          uni.removeStorageSync('code_timer_start');
        }
      }, 1000);
    },

    // 恢复验证码倒计时
    restoreCodeTimer() {
      const startTime = uni.getStorageSync('code_timer_start');
      if (!startTime) return;

      // 计算已经过去的时间（秒）
      const elapsedSeconds = Math.floor((Date.now() - startTime) / 1000);
      const remainingTime = 60 - elapsedSeconds;

      if (remainingTime > 0) {
        // 还有剩余时间，继续倒计时
        this.codeTime = remainingTime;
        this.codeBtnDisabled = true;
        this.codeBtnText = `${this.codeTime}s`;

        // 清除之前的定时器（如果存在）
        if (this.codeTimer) {
          clearInterval(this.codeTimer);
        }

        this.codeTimer = setInterval(() => {
          this.codeTime--;
          this.codeBtnText = `${this.codeTime}s`;
          if (this.codeTime <= 0) {
            clearInterval(this.codeTimer);
            this.codeBtnText = '获取验证码';
            this.codeBtnDisabled = false;
            this.codeTime = 60;
            uni.removeStorageSync('code_timer_start');
          }
        }, 1000);

        console.log('恢复验证码倒计时，剩余时间:', remainingTime, '秒');
      } else {
        // 倒计时已结束
        this.codeBtnText = '获取验证码';
        this.codeBtnDisabled = false;
        this.codeTime = 60;
        uni.removeStorageSync('code_timer_start');
        console.log('验证码倒计时已过期');
      }
    },

    // 登录方法
    startLogin() {
      if (this.isRotate) return false;
      const reg = /^1[3456789]\d{9}$/;
      if (!reg.test(this.phoneData)) {
        uni.showToast({
          icon: 'none',
          title: '手机号格式不正确'
        });
        return;
      }
      if (this.loginType === 'password') {
        // 密码登录需要滑块
        this.verifyAction = 'login';
        this.showVerify();
      } else {
        // 验证码登录不需要滑块，直接登录
        this.doCodeLogin();
      }
    },

    handleLoginSuccess(loginData) {
      console.log('处理登录成功，返回数据:', loginData);

      // 从登录返回数据中提取 accessToken、refreshToken 和 userInfo
      const accessToken = loginData.accessToken;
      const refreshToken = loginData.refreshToken;
      const data = loginData.userInfo;

      // 验证 token 是否有效
      if (
        !accessToken ||
        accessToken === '' ||
        accessToken === 'undefined' ||
        accessToken === 'null'
      ) {
        console.error('登录返回的AccessToken无效:', accessToken);
        uni.showToast({
          icon: 'none',
          title: '登录失败：Token无效'
        });
        return;
      }

      const userInfo = {
        avatarUrl: data.avatar,
        nickname: data.username,
        id: parseInt(data.id, 10) || 0,
        hsId: data.hsId,
        ipAddr: data.address,
        selfIntroduction: data.description,
        age: data.age || null,
        sex: data.gender !== undefined ? parseInt(data.gender, 10) : null,
        attentionNum: parseInt(data.followerCount) || 0,
        fansNum: parseInt(data.fanCount) || 0,
        homePageBackground: data.userCover,
        area: data.address,
        phoneNumber: data.phone,
        tags: data.tags ? JSON.parse(data.tags) : []
      };

      try {
        // 持久化存储用户信息、accessToken 和 refreshToken
        uni.setStorageSync('userInfo', userInfo);
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

        // 记住手机号，下次登录自动回填
        if (this.phoneData) {
          uni.setStorageSync('hongshu_saved_phone', this.phoneData);
        }

        // 清除登录方式偏好和倒计时（登录成功后重置）
        uni.removeStorageSync('login_type_preference');
        uni.removeStorageSync('code_timer_start');

        // 清除定时器
        if (this.codeTimer) {
          clearInterval(this.codeTimer);
        }

        // 显示登录成功提示
        uni.$u.toast('登录成功');

        setTimeout(() => {
          this.isRotate = false;
          uni.reLaunch({
            url: '/pages/index/index'
          });
        }, 1000);
      } catch (e) {
        console.error('存储登录信息失败:', e);
        uni.showToast({
          icon: 'none',
          title: '登录失败：无法保存登录状态'
        });
      }
    },

    // 获取演示账号配置
    async fetchDemoAccountConfig() {
      console.log('开始获取演示账号配置...');
      try {
        const response = await getDemoAccountConfig();
        console.log('演示账号配置响应:', response);
        if (response.code === 200 && response.data) {
          this.demoAccountConfig = {
            enabled: response.data.enabled || false,
            username: response.data.username || '',
            password: response.data.password || '',
            description: response.data.description || '',
            permissions: response.data.permissions || [],
            expireTime: response.data.expireTime || ''
          };
          console.log('演示账号配置已更新:', this.demoAccountConfig);
        }
      } catch (error) {
        console.error('获取演示账号配置失败:', error);
        // 如果获取配置失败，保持默认值（不显示演示账号）
        this.demoAccountConfig.enabled = false;
      }
    },

    // 演示账号登录
    handleDemoLogin() {
      if (this.isRotate) return false;

      // 检查演示账号配置
      if (!this.demoAccountConfig.enabled) {
        uni.showToast({
          icon: 'none',
          title: '演示账号功能未开启'
        });
        return;
      }

      // 使用配置中的演示账号信息
      if (this.demoAccountConfig.username && this.demoAccountConfig.password) {
        this.phoneData = this.demoAccountConfig.username;
        this.passData = this.demoAccountConfig.password;
      } else {
        // 如果配置中没有账号信息，使用默认值
        this.phoneData = '17611776901';
        this.passData = '123456';
      }
      this.loginType = 'password';

      // 显示加载状态
      this.isRotate = true;

      // 调用演示账号登录接口
      demoLogin({
        phone: this.phoneData,
        password: this.passData
      })
        .then(res => {
          this.isRotate = false;
          if (res.code == 200) {
            uni.$u.toast('演示账号登录成功');
            this.handleLoginSuccess(res.data);
          } else {
            uni.showToast({
              icon: 'none',
              title: res.msg || '演示账号登录失败'
            });
          }
        })
        .catch(err => {
          this.isRotate = false;
          console.error('演示账号登录失败:', err);
          const msg = err?.response?.data?.message || err?.data?.message || err?.message;
          if (msg) {
            uni.showToast({ icon: 'none', title: msg });
          }
        });
    }
  },
  async onLoad() {
    console.log('登录页面加载，开始获取演示账号配置...');
    // 获取演示账号配置
    await this.fetchDemoAccountConfig();
    console.log('演示账号配置获取完成，当前配置:', this.demoAccountConfig);
    console.log('演示账号是否启用:', this.demoAccountConfig.enabled);

    // 回填上次登录的手机号
    const savedPhone = uni.getStorageSync('hongshu_saved_phone');
    if (savedPhone) {
      this.phoneData = savedPhone;
    }

    // 恢复上次选择的登录方式
    const savedLoginType = uni.getStorageSync('login_type_preference');
    if (savedLoginType) {
      this.loginType = savedLoginType;
      console.log('恢复登录方式:', savedLoginType);
    }
  },

  onShow() {
    const t = uni.getStorageSync('uniapp_token');
    if (t && t !== '' && t !== 'undefined' && t !== 'null') {
      uni.reLaunch({ url: '/pages/index/index' });
      return;
    }
    // 页面显示时恢复登录方式（处理从其他应用返回的情况）
    const savedLoginType = uni.getStorageSync('login_type_preference');
    if (savedLoginType) {
      this.loginType = savedLoginType;
      console.log('页面显示，恢复登录方式:', savedLoginType);
    }

    // 恢复验证码倒计时
    this.restoreCodeTimer();
  },

  onUnload() {
    // 页面卸载时清除定时器
    if (this.codeTimer) {
      clearInterval(this.codeTimer);
    }
  }
};
</script>

<style>
@import url('../../components/watch-login/css/icon.css');
@import url('./css/main.css');

.login {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
}

.content {
  padding: 0 60rpx;
}

.header {
  width: 100%;
  height: auto;
  border-radius: 0;
  margin-top: 0;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.header image,
.header .logo-img {
  width: 144rpx;
  height: 144rpx;
  margin-left: 0;
  border-radius: 50%;
}

.subtitle {
  color: #231710;
  margin-top: 8rpx;
  text-align: center;
  font-size: 44rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
  opacity: 1;
}

.title {
  text-align: center;
}

.login-type-switch {
  margin: 100rpx 0 30rpx 0;
  display: flex;
  justify-content: center;
}

.switch-btn {
  margin: 0 15rpx;
  font-size: 30rpx;
  color: #8F7260;
  font-weight: 500;
  padding-bottom: 6rpx;
  transition: color 0.2s;
}

.switch-btn.active {
  color: #C97B4A;
  border-bottom: 4rpx solid #C97B4A;
  font-weight: bold;
}

.main {
  padding-left: 0;
  padding-right: 0;
}

.main .main-list {
  margin: 12rpx 0;
}

.get-code-btn {
  /* margin-left: 10rpx; */
  height: 80rpx;
  line-height: 80rpx;
  background: #C97B4A;
  color: #fff;
  border-radius: 55rpx;
  padding: 0 30rpx;
  font-size: 22rpx;
  box-shadow: 0 2rpx 8rpx rgba(201, 123, 74, 0.1);
}

.wbutton {
  border-radius: 40rpx !important;
  margin-top: 50rpx;
  /* box-shadow: 0 8rpx 32rpx rgba(201, 123, 74, 0.15); */
}

.footer {
  margin-top: 70rpx;
  color: #C4AC95;
  font-size: 26rpx;
  text-align: center;
  letter-spacing: 2rpx;
}

.demo-login {
  margin-top: 30rpx;
  text-align: center;
}

.demo-btn {
  background: transparent;
  border: 1rpx solid #C97B4A;
  border-radius: 50rpx;
  padding: 1rpx 32rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.demo-btn:active {
  background: #C97B4A;
  transform: scale(0.95);
}

.reds-icon {
  fill: #C97B4A;
}

.demo-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
  color: #C97B4A;
  font-weight: bold;
}

.demo-text {
  color: #C97B4A;
  font-size: 28rpx;
  font-weight: bold;
}

.demo-btn:active .demo-text {
  color: #fff;
}

page {
  background-color: #F4EDE2 !important;
}
uni-page-body {
  background-color: #F4EDE2 !important;
}
</style>