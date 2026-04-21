<template>
  <view class="pay-entry-page">
    <!-- 状态栏背景（微信端由系统栏「爱宠社」占位） -->
    <!-- #ifndef MP-WEIXIN -->
    <view class="status-bar-bg" :style="{ height: statusBarHeight + 'px' }"></view>
    <!-- #endif -->
    <!-- 导航栏（固定） -->
    <view class="navbar" :style="{ top: navBarTop }">
      <!-- #ifndef MP-WEIXIN -->
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="20" color="#333"></u-icon>
      </view>
      <!-- #endif -->
      <view class="navbar-title">订单支付</view>
      <view class="placeholder"></view>
    </view>

    <!-- 主内容区 -->
    <scroll-view
      scroll-y
      :style="{ height: scrollHeight + 'px', paddingTop: contentPaddingTop + 'px' }"
      class="content-scroll"
    >
      <!-- 订单信息 -->
      <view class="order-info-section">
        <view class="section-title">订单信息</view>
        <view class="order-item" v-if="paymentOrderInfo">
          <view class="order-row">
            <text class="label">订单号</text>
            <text class="value">{{ paymentOrderInfo.orderNumber || '-' }}</text>
          </view>
          <view class="order-row">
            <text class="label">商品名称</text>
            <text class="value product-name">{{ paymentOrderInfo.productTitle || '-' }}</text>
          </view>
          <view class="order-row">
            <text class="label">支付金额</text>
            <text class="value price">¥{{ paymentOrderInfo.payPrice || '0.00' }}</text>
          </view>
          <view class="order-row pay-type-row">
            <text class="label">支付方式</text>
            <view
              class="pay-type-badge"
              :class="paymentOrderInfo.payType === '1' ? 'wechat-badge' : 'alipay-badge'"
            >
              <image
                v-if="paymentOrderInfo.payType === '2'"
                src="/static/zfb.png"
                class="pay-type-icon"
                mode="aspectFit"
              />
              <image
                v-else-if="paymentOrderInfo.payType === '1'"
                src="/static/wx.png"
                class="pay-type-icon wechat-icon"
                mode="aspectFit"
              />
              <text class="pay-type-text">
                {{ paymentOrderInfo.payType === '1' ? '微信支付' : '支付宝' }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 支付二维码（微信支付） -->
      <view class="qr-code-section" v-if="showWeChatQRCode && wechatQRCodeUrl">
        <view class="section-title wechat-title">
          <image src="/static/wx.png" class="title-icon" mode="aspectFit"></image>
          <text>请使用微信扫码支付</text>
        </view>
        <view class="qr-code-container">
          <image :src="qrCodeImageUrl" mode="aspectFit" class="qr-code-image"></image>
        </view>
        <view class="qr-code-tip">
          <text>请使用微信扫描上方二维码完成支付</text>
        </view>
        <view class="countdown-tip" v-if="countdown > 0">
          <text>订单将在 {{ formatTime(countdown) }} 后过期</text>
        </view>
      </view>

      <!-- 支付二维码（支付宝扫码支付，仅H5环境） -->
      <!-- #ifdef H5 -->
      <view class="qr-code-section" v-if="showAlipayQRCode && alipayQRCodeUrl">
        <view class="section-title alipay-title">
          <image src="/static/zfb.png" class="title-icon" mode="aspectFit"></image>
          <text>请使用支付宝扫码支付</text>
        </view>
        <view class="qr-code-container">
          <image :src="alipayQrCodeImageUrl" mode="aspectFit" class="qr-code-image"></image>
        </view>
        <view class="qr-code-tip">
          <text>请使用支付宝扫描上方二维码完成支付</text>
        </view>
        <view class="countdown-tip" v-if="countdown > 0">
          <text>订单将在 {{ formatTime(countdown) }} 后过期</text>
        </view>
      </view>
      <!-- #endif -->

      <!-- 支付方式选择器（支付宝支付，仅H5环境） -->
      <!-- #ifdef H5 -->
      <view
        class="payment-method-selector"
        v-if="
          paymentOrderInfo &&
          paymentOrderInfo.payType === '2' &&
          !showWeChatQRCode &&
          !showAlipayQRCode &&
          paymentConfig &&
          paymentConfig.alipaySandboxEnabled &&
          paymentConfig.alipayQrcodeEnabled
        "
      >
        <view class="selector-title">选择支付方式</view>
        <radio-group @change="onPaymentMethodChange" :value="alipayPaymentMethod">
          <label class="payment-method-item" v-if="paymentConfig.alipaySandboxEnabled">
            <radio value="form" :checked="alipayPaymentMethod === 'form'" />
            <text>跳转支付（沙箱）</text>
          </label>
          <label class="payment-method-item" v-if="paymentConfig.alipayQrcodeEnabled">
            <radio value="qrCode" :checked="alipayPaymentMethod === 'qrCode'" />
            <text>扫码支付</text>
          </label>
        </radio-group>
      </view>
      <!-- #endif -->

      <!-- 支付提示 -->
      <view class="payment-tip-section" v-if="!showWeChatQRCode && !showAlipayQRCode">
        <view class="tip-content">
          <u-icon name="info-circle" size="40" color="#3d8af5"></u-icon>
          <text class="tip-text">点击下方按钮完成支付</text>
        </view>
        <!-- 倒计时提示（支付宝支付时显示，或未显示二维码时显示） -->
        <view class="countdown-tip" v-if="countdown > 0">
          <text class="countdown-text">订单将在 {{ formatTime(countdown) }} 后过期</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="total-info">
        <text class="total-label">需支付：</text>
        <text class="total-price">
          ¥{{ paymentOrderInfo && paymentOrderInfo.payPrice ? paymentOrderInfo.payPrice : '0.00' }}
        </text>
      </view>
      <view class="action-btn" @click="handlePay" :class="{ disabled: isPaying || isPaid }">
        {{ isPaid ? '已支付' : isPaying ? '支付中...' : '立即支付' }}
      </view>
    </view>

    <!-- 底部占位 -->
    <view style="height: 100rpx"></view>
  </view>
</template>

<script>
import {
  getPaymentOrder,
  createPaymentPay,
  finishPay,
  getPayStatus,
  updateOrderStatus,
  getPaymentGlobalConfig
} from '@/apis/order_service.js';
import weixinNavBar from '@/utils/weixinNavBar.js';

export default {
  mixins: [weixinNavBar],
  data() {
    return {
      statusBarHeight: 0,
      scrollHeight: 0,
      paymentOrderId: '',
      paymentOrderInfo: null,
      paymentPayId: '',
      wechatQRCodeUrl: '',
      showWeChatQRCode: false,
      // #ifdef H5
      alipayQRCodeUrl: '', // 支付宝支付二维码URL
      showAlipayQRCode: false, // 是否显示支付宝二维码
      alipayPaymentMethod: 'form', // 支付宝支付方式：form=沙箱跳转，qrCode=扫码支付
      // #endif
      isPaying: false,
      isPaid: false,
      countdown: 900,
      paymentTimeoutMinutes: 15, // 与后端 payment.timeout.minutes / 支付全局配置 对齐
      countdownTimer: null,
      statusPollTimer: null,
      isExpired: false, // 标记订单是否已过期
      isUpdatingOrderStatus: false, // 标记是否正在更新订单状态
      paymentConfig: null // 支付全局配置
    };
  },
  computed: {
    qrCodeImageUrl() {
      if (!this.wechatQRCodeUrl) return '';
      // 使用在线二维码生成服务
      return `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
        this.wechatQRCodeUrl
      )}`;
    },
    // #ifdef H5
    alipayQrCodeImageUrl() {
      if (!this.alipayQRCodeUrl) return '';
      // 使用在线二维码生成服务
      return `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
        this.alipayQRCodeUrl
      )}`;
    },
    // #endif
    // 计算内容区域的padding-top（状态栏高度 + navbar高度44px + 额外间距20rpx转px）
    contentPaddingTop() {
      const navbarHeightPx = 44;
      const extraSpacingPx = uni.upx2px(20);
      return (this.statusBarHeight || 0) + navbarHeightPx + extraSpacingPx;
    },
    paymentWindowSeconds() {
      const m = Number(this.paymentTimeoutMinutes);
      const min = Number.isFinite(m) && m > 0 ? m : 15;
      return Math.min(Math.max(min, 1), 1440) * 60;
    }
  },
  async onLoad(options) {
    const systemInfo = uni.getSystemInfoSync();
    this.applyWeixinNavBar(systemInfo, 44);
    this.scrollHeight = systemInfo.windowHeight;

    // 获取支付订单ID
    this.paymentOrderId = options.paymentOrderId || '';

    if (!this.paymentOrderId) {
      uni.showToast({
        title: '订单信息错误',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
      return;
    }

    await this.loadPaymentConfig();

    this.loadPaymentOrder();
  },
  onUnload() {
    // 清除定时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
    if (this.statusPollTimer) {
      clearInterval(this.statusPollTimer);
    }
  },
  methods: {
    // 加载支付配置
    async loadPaymentConfig() {
      try {
        const res = await getPaymentGlobalConfig();
        console.log('支付全局配置:', res);
        
        if (res.code === 200 && res.data) {
          this.paymentConfig = res.data;
          if (typeof res.data.paymentTimeoutMinutes === 'number' && res.data.paymentTimeoutMinutes > 0) {
            this.paymentTimeoutMinutes = res.data.paymentTimeoutMinutes;
          }
          console.log('小程序支付开关状态:', this.paymentConfig.wechatMiniprogramEnabled);
          
          // 检查全局支付开关
          if (!this.paymentConfig.paymentEnabled) {
            uni.showModal({
              title: '温馨提示',
              content: '支付功能暂时关闭，无法完成支付',
              showCancel: false,
              success: () => {
                uni.navigateBack();
              }
            });
            return;
          }
          
          // #ifdef H5
          // 根据配置设置默认的支付宝支付方式
          if (this.paymentConfig.alipaySandboxEnabled && !this.paymentConfig.alipayQrcodeEnabled) {
            // 只有沙箱支付可用
            this.alipayPaymentMethod = 'form';
          } else if (!this.paymentConfig.alipaySandboxEnabled && this.paymentConfig.alipayQrcodeEnabled) {
            // 只有扫码支付可用
            this.alipayPaymentMethod = 'qrCode';
          } else if (this.paymentConfig.alipaySandboxEnabled && this.paymentConfig.alipayQrcodeEnabled) {
            // 两者都可用，默认使用跳转支付
            this.alipayPaymentMethod = 'form';
          } else if (!this.paymentConfig.alipaySandboxEnabled && !this.paymentConfig.alipayQrcodeEnabled) {
            // 两者都不可用（注意：这种情况应该在 checkPaymentMethodAvailable 中被拦截）
            console.warn('支付宝支付方式都不可用');
          }
          // #endif
        }
      } catch (error) {
        console.error('加载支付配置失败:', error);
        // 加载失败时不影响页面使用，使用默认配置
      }
    },
    
    // 检查当前支付方式是否可用
    checkPaymentMethodAvailable() {
      if (!this.paymentConfig || !this.paymentOrderInfo) {
        return true; // 配置未加载时，默认允许
      }
      
      // 检查全局支付开关
      if (!this.paymentConfig.paymentEnabled) {
        return false;
      }
      
      const payType = this.paymentOrderInfo.payType;
      
      // 检查微信支付是否可用
      if (payType === '1' || payType === 'wechat') {
        if (!this.paymentConfig.wechatQrcodeEnabled) {
          uni.showModal({
            title: '支付方式已关闭',
            content: '微信支付功能已关闭，无法完成支付。请返回重新选择其他支付方式。',
            showCancel: false,
            success: () => {
              uni.navigateBack();
            }
          });
          return false;
        }
      }
      
      // 检查支付宝支付是否可用（沙箱或扫码，只要有一个可用就可以）
      if (payType === '2' || payType === 'alipay') {
        const alipayAvailable = this.paymentConfig.alipaySandboxEnabled || this.paymentConfig.alipayQrcodeEnabled;
        if (!alipayAvailable) {
          uni.showModal({
            title: '支付方式已关闭',
            content: '支付宝支付功能已关闭，无法完成支付。请返回重新选择其他支付方式。',
            showCancel: false,
            success: () => {
              uni.navigateBack();
            }
          });
          return false;
        }
      }
      
      return true;
    },
    
    // 加载支付订单信息
    async loadPaymentOrder() {
      try {
        uni.showLoading({
          title: '加载中...',
          mask: true
        });

        const res = await getPaymentOrder(this.paymentOrderId);

        if (res.code === 200 && res.data) {
          this.paymentOrderInfo = res.data;
          
          // 检查当前支付方式是否可用
          if (!this.checkPaymentMethodAvailable()) {
            return; // 如果支付方式不可用，已经在检查方法中显示提示并返回
          }

          // 如果订单状态是0（未支付）且还没有支付记录ID，创建支付记录
          if ((res.data.orderStatus === '0' || res.data.orderStatus === 0) && !this.paymentPayId) {
            try {
              await this.createPaymentPayRecord();
            } catch (error) {
              // 错误对象可能有 msg 或 message 属性
              const errorMsg = error.msg || error.message || '';
              // 如果创建支付记录失败，且是订单超时错误，调用过期处理
              if (errorMsg.includes('超时') || errorMsg.includes('过期')) {
                console.log(
                  '创建支付记录时发现订单已过期，调用过期处理, paymentOrderId:',
                  this.paymentOrderId
                );
                // 注意：不要在这里设置 isExpired，让 handleOrderExpired 自己设置
                await this.handleOrderExpired();
                return; // 过期处理后直接返回，不再继续
              }
              // 其他错误继续抛出
              throw error;
            }
          } else if (res.data.paymentPayId) {
            // 如果订单信息中包含支付记录ID，直接使用
            this.paymentPayId = res.data.paymentPayId;
          }

          // 如果订单状态为待支付或支付中，且订单未过期，启动倒计时（基于订单创建时间）
          // 参考 vue_hongshu_web 的实现，在加载订单信息后立即启动倒计时
          if ((res.data.orderStatus === '0' || res.data.orderStatus === '1') && !this.isExpired) {
            this.startCountdown();
          }

          // 如果是微信支付且订单未支付，自动获取二维码并显示
          const payType = res.data.payType;
          if (
            (payType === '1' || payType === 'wechat') &&
            (res.data.orderStatus === '0' ||
              res.data.orderStatus === '1' ||
              res.data.orderStatus === 0 ||
              res.data.orderStatus === 1) &&
            !this.isExpired &&
            !this.showWeChatQRCode
          ) {
            // 自动调用支付接口获取二维码
            this.autoLoadWeChatQRCode();
          }
        } else {
          throw new Error(res.msg || '获取订单信息失败');
        }
      } catch (error) {
        console.error('加载支付订单失败:', error);
        // 错误对象可能有 msg 或 message 属性
        const errorMsg = error.msg || error.message || '';
        // 如果是订单超时错误，不显示错误提示，因为已经处理过了
        if (!(errorMsg.includes('超时') || errorMsg.includes('过期'))) {
          uni.showToast({
            title: errorMsg || '加载失败',
            icon: 'none'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        }
      } finally {
        uni.hideLoading();
      }
    },

    // 创建支付记录
    async createPaymentPayRecord() {
      // 如果已经有支付记录ID，直接返回
      if (this.paymentPayId) {
        return;
      }

      try {
        const res = await createPaymentPay(this.paymentOrderId);
        if (res.code === 200) {
          this.paymentPayId = res.data;
          console.log('支付记录创建成功:', this.paymentPayId);
        }
      } catch (error) {
        console.error('创建支付记录失败:', error);
        // 错误对象可能有 msg 或 message 属性
        const errorMsg = error.msg || error.message || '';
        // 如果是订单过期错误，抛出包含 message 的错误（不要在这里设置 isExpired）
        if (errorMsg.includes('超时') || errorMsg.includes('过期')) {
          const err = new Error(errorMsg);
          err.code = error.code;
          throw err;
        }
        throw error;
      }
    },

    // 支付
    async handlePay() {
      if (this.isPaying || this.isPaid) {
        return;
      }
      
      // 再次检查当前支付方式是否可用（双重保险）
      if (!this.checkPaymentMethodAvailable()) {
        return; // 如果支付方式不可用，已经在检查方法中显示提示并返回
      }

      // 先计算剩余时间（基于订单创建时间），检查订单是否过期
      if (this.paymentOrderInfo && this.paymentOrderInfo.createTime) {
        const createTime = new Date(this.paymentOrderInfo.createTime).getTime();
        const now = new Date().getTime();
        const elapsedSeconds = Math.floor((now - createTime) / 1000);
        const totalSeconds = this.paymentWindowSeconds;
        const remainingSeconds = Math.max(totalSeconds - elapsedSeconds, 0);

        if (remainingSeconds <= 0) {
          uni.showToast({
            title: '订单已过期，请重新下单',
            icon: 'none'
          });
          return;
        }
      }

      // 如果没有支付记录ID，先创建
      if (!this.paymentPayId) {
        try {
          await this.createPaymentPayRecord();
        } catch (error) {
          // 如果创建失败（可能是订单过期），直接返回
          if (error.message && error.message.includes('超时')) {
            this.isExpired = true;
            this.handleOrderExpired();
          }
          return;
        }
      }

      if (!this.paymentPayId) {
        uni.showToast({
          title: '创建支付记录失败',
          icon: 'none'
        });
        return;
      }

      // 如果已经显示二维码，直接返回（避免重复调用）
      if (
        (this.showWeChatQRCode && this.wechatQRCodeUrl) ||
        (this.showAlipayQRCode && this.alipayQRCodeUrl)
      ) {
        return;
      }

      this.isPaying = true;

      try {
        uni.showLoading({
          title: '处理中...',
          mask: true
        });

        // 根据支付类型和支付方式传递参数
        let paymentMethod = undefined;
        let openid = undefined;
        
        // #ifdef MP-WEIXIN
        // 小程序环境下，如果是微信支付，检查是否启用小程序支付
        if (this.paymentOrderInfo?.payType === '1' || this.paymentOrderInfo?.payType === 'wechat') {
          console.log('小程序环境，微信支付，检查配置:', {
            paymentConfig: this.paymentConfig,
            wechatMiniprogramEnabled: this.paymentConfig?.wechatMiniprogramEnabled
          });
          
          // 检查小程序支付开关（如果配置未加载，默认尝试使用小程序支付）
          if (this.paymentConfig && this.paymentConfig.wechatMiniprogramEnabled !== false) {
            // 小程序支付已启用（或配置未加载），尝试使用小程序支付
            console.log('尝试使用小程序支付');
            
            // 先检查用户信息中是否有openid
            const userInfo = uni.getStorageSync('userInfo');
            if (userInfo && userInfo.openid) {
              // 用户信息中有openid，直接使用小程序支付
              paymentMethod = 'miniprogram';
              openid = userInfo.openid;
              console.log('从用户信息中获取到openid，使用小程序支付');
            } else {
              // 用户信息中没有openid（可能是手机号登录），尝试获取
              console.log('用户信息中没有openid，尝试获取');
              openid = await this.getOpenid();
              console.log('获取到的openid:', openid);
              
              if (openid) {
                // 成功获取到openid，使用小程序支付
                paymentMethod = 'miniprogram';
                console.log('成功获取到openid，使用小程序支付');
              } else {
                // 无法获取openid（用户可能是通过手机号登录的），降级为二维码支付
                paymentMethod = undefined;
                console.log('无法获取openid，降级为二维码支付');
                uni.showToast({
                  title: '将使用扫码支付',
                  icon: 'none',
                  duration: 2000
                });
              }
            }
          } else {
            // 小程序支付未启用，使用二维码支付
            paymentMethod = undefined;
            console.log('小程序支付未启用，使用二维码支付');
          }
        }
        // #endif
        
        // #ifdef H5
        // H5环境下支付宝支持扫码支付
        if (this.paymentOrderInfo?.payType === '2' && this.alipayPaymentMethod === 'qrCode') {
          paymentMethod = 'qrCode';
        }
        // #endif

        const res = await finishPay(this.paymentPayId, paymentMethod, openid);

        uni.hideLoading();

        if (res.code === 200) {
          const payType = res.data?.payType || this.paymentOrderInfo?.payType;

          if (payType === '1' || payType === 'wechat') {
            // 微信支付
            // #ifdef MP-WEIXIN
            // 小程序环境下，如果是小程序支付，调起微信支付
            if (res.data.paymentMethod === 'miniprogram' && res.data.timeStamp && res.data.paySign) {
              await this.requestWeChatPay(res.data);
            } else {
              // 否则显示二维码
              this.wechatQRCodeUrl = res.data.codeUrl || res.data.qrCodeUrl;
              if (this.wechatQRCodeUrl) {
                this.showWeChatQRCode = true;
                this.startCountdown();
                this.startStatusPolling();
              } else {
                uni.showToast({
                  title: '获取支付二维码失败',
                  icon: 'none'
                });
              }
            }
            // #endif
            
            // #ifndef MP-WEIXIN
            // 非小程序环境，显示二维码
            this.wechatQRCodeUrl = res.data.codeUrl || res.data.qrCodeUrl;
            if (this.wechatQRCodeUrl) {
              this.showWeChatQRCode = true;
              this.startCountdown();
              this.startStatusPolling();
            } else {
              uni.showToast({
                title: '获取支付二维码失败',
                icon: 'none'
              });
            }
            // #endif
          } else {
            // 支付宝支付
            // #ifdef H5
            if (this.alipayPaymentMethod === 'qrCode' && (res.data.codeUrl || res.data.qrCodeUrl)) {
              // 支付宝扫码支付：显示二维码
              this.alipayQRCodeUrl = res.data.codeUrl || res.data.qrCodeUrl;
              if (this.alipayQRCodeUrl) {
                this.showAlipayQRCode = true;
                // 显示二维码时启动倒计时（基于订单创建时间）
                this.startCountdown();
                // 开始轮询支付状态
                this.startStatusPolling();
              } else {
                uni.showToast({
                  title: '获取支付二维码失败',
                  icon: 'none'
                });
              }
            } else if (res.data.paymentFormHtml) {
              // 支付宝沙箱跳转支付：跳转到支付页面
              const container = document.createElement('div');
              container.style.display = 'none';
              container.innerHTML = res.data.paymentFormHtml;
              document.body.appendChild(container);
              const innerForm = container.querySelector('form');
              if (innerForm) {
                // 如果后端未设置action，则使用默认网关
                if (!innerForm.getAttribute('action')) {
                  innerForm.setAttribute(
                    'action',
                    res.data.action || 'https://openapi.alipay.com/gateway.do'
                  );
                }
                innerForm.submit();
              } else {
                uni.showToast({
                  title: '支付表单异常',
                  icon: 'none'
                });
              }
            }
            // #endif

            // #ifndef H5
            if (res.data.paymentFormHtml) {
              uni.showToast({
                title: '支付宝支付功能开发中',
                icon: 'none'
              });
            }
            // #endif
          }
        } else {
          throw new Error(res.msg || '支付失败');
        }
      } catch (error) {
        console.error('支付失败:', error);
        uni.showToast({
          title: error.message || '支付失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.isPaying = false;
      }
    },

    // 开始倒计时
    startCountdown() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
      }

      // 计算剩余时间（基于订单创建时间与配置的支付窗口）
      if (this.paymentOrderInfo && this.paymentOrderInfo.createTime) {
        const createTime = new Date(this.paymentOrderInfo.createTime).getTime();
        const now = new Date().getTime();
        const elapsedSeconds = Math.floor((now - createTime) / 1000);
        const totalSeconds = this.paymentWindowSeconds;
        this.countdown = Math.max(totalSeconds - elapsedSeconds, 0);
      } else {
        this.countdown = this.paymentWindowSeconds;
      }

      // 如果已经过期，直接处理过期逻辑
      if (this.countdown <= 0) {
        this.handleOrderExpired();
        return;
      }

      this.countdownTimer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
        } else {
          clearInterval(this.countdownTimer);
          this.handleOrderExpired();
        }
      }, 1000);
    },

    // 处理订单过期
    async handleOrderExpired() {
      // 防止重复调用
      if (this.isExpired || this.isUpdatingOrderStatus) {
        console.log('订单过期处理已执行，跳过重复调用', {
          isExpired: this.isExpired,
          isUpdatingOrderStatus: this.isUpdatingOrderStatus
        });
        return;
      }

      this.isExpired = true;

      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
        this.countdownTimer = null;
      }

      console.log('处理订单过期', {
        paymentOrderId: this.paymentOrderId,
        orderStatus: this.paymentOrderInfo?.orderStatus,
        paymentOrderInfo: this.paymentOrderInfo
      });

      // 只有在订单状态为未支付或待支付时才更新状态
      if (
        this.paymentOrderInfo &&
        (this.paymentOrderInfo.orderStatus === '0' ||
          this.paymentOrderInfo.orderStatus === '1' ||
          this.paymentOrderInfo.orderStatus === 0 ||
          this.paymentOrderInfo.orderStatus === 1)
      ) {
        this.isUpdatingOrderStatus = true;
        try {
          console.log('开始调用 updateOrderStatus，paymentOrderId:', this.paymentOrderId);
          // 调用后端接口释放订单和商品
          await updateOrderStatus(this.paymentOrderId);
          console.log('updateOrderStatus 调用成功');
          uni.showToast({
            title: '订单已超时，请重新下单',
            icon: 'none',
            duration: 2000
          });
          // 发送刷新事件，通知订单列表页面刷新
          uni.$emit('refreshOrderList', { tab: 'unpaid' });
          // 延迟返回订单列表
          setTimeout(() => {
            uni.navigateBack();
          }, 2000);
        } catch (error) {
          console.error('更新订单状态失败:', error);
          uni.showToast({
            title: error.message || '订单已过期，请重新下单',
            icon: 'none'
          });
        } finally {
          this.isUpdatingOrderStatus = false;
        }
      } else {
        console.log('订单状态不符合更新条件', {
          hasPaymentOrderInfo: !!this.paymentOrderInfo,
          orderStatus: this.paymentOrderInfo?.orderStatus
        });
        uni.showToast({
          title: '订单已过期',
          icon: 'none'
        });
      }
    },

    // 开始轮询支付状态
    startStatusPolling() {
      if (this.statusPollTimer) {
        clearInterval(this.statusPollTimer);
      }

      this.statusPollTimer = setInterval(async () => {
        try {
          const res = await getPayStatus(this.paymentPayId);
          if (res.code === 200) {
            const status = res.data;
            // 支付状态：0-未支付，1-待支付，2-支付中，3-已支付，4-支付完成
            if (status === '3' || status === '4') {
              // 支付成功
              clearInterval(this.statusPollTimer);
              this.isPaid = true;
              uni.showToast({
                title: '支付成功',
                icon: 'success'
              });

              // 跳转到订单列表
              setTimeout(() => {
                uni.redirectTo({
                  url: '/pkg-mine/pages/orders/orders'
                });
              }, 1500);
            }
          }
        } catch (error) {
          console.error('查询支付状态失败:', error);
        }
      }, 3000); // 每3秒轮询一次
    },

    // 格式化时间
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60);
      const secs = seconds % 60;
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },

    // 自动加载微信支付二维码
    async autoLoadWeChatQRCode() {
      // 如果没有支付记录ID，先创建
      if (!this.paymentPayId) {
        try {
          await this.createPaymentPayRecord();
        } catch (error) {
          console.error('自动加载二维码时创建支付记录失败:', error);
          return;
        }
      }

      if (!this.paymentPayId) {
        console.error('支付记录ID不存在，无法获取二维码');
        return;
      }

      // 如果已经显示二维码，直接返回
      if (
        (this.showWeChatQRCode && this.wechatQRCodeUrl) ||
        (this.showAlipayQRCode && this.alipayQRCodeUrl)
      ) {
        return;
      }

      try {
        const res = await finishPay(this.paymentPayId);

        if (res.code === 200) {
          const payType = res.data?.payType || this.paymentOrderInfo?.payType;

          if (payType === '1' || payType === 'wechat') {
            // 微信支付：显示二维码
            this.wechatQRCodeUrl = res.data.codeUrl || res.data.qrCodeUrl;
            if (this.wechatQRCodeUrl) {
              this.showWeChatQRCode = true;
              // 开始轮询支付状态
              this.startStatusPolling();
            }
          }
        }
      } catch (error) {
        console.error('自动加载微信支付二维码失败:', error);
        // 不显示错误提示，让用户手动点击按钮
      }
    },

    // #ifdef H5
    // 支付宝支付方式改变
    onPaymentMethodChange(e) {
      this.alipayPaymentMethod = e.detail.value;
    },
    // #endif

    // 返回
    goBack() {
      uni.navigateBack();
    },

    // #ifdef MP-WEIXIN
    /**
     * 获取openid（小程序环境）
     */
    async getOpenid() {
      try {
        // 先从用户信息中获取openid（如果后端返回并存储了）
        const userInfo = uni.getStorageSync('userInfo');
        console.log('用户信息:', userInfo);
        if (userInfo && userInfo.openid) {
          console.log('从用户信息中获取到openid:', userInfo.openid);
          return userInfo.openid;
        }

        // 如果没有，通过wx.login获取code，然后调用后端接口获取openid
        console.log('用户信息中没有openid，尝试通过code获取');
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          });
        });

        if (!loginRes.code) {
          console.error('获取微信登录code失败');
          return null;
        }

        console.log('获取到微信登录code，尝试通过后端接口获取openid');
        // 注意：如果用户是通过手机号登录的，调用微信登录接口可能会创建新账号或返回错误
        // 这里只是尝试获取openid，如果失败则返回null，支付页面会降级为二维码支付
        try {
          const { wechatMiniProgramLogin } = await import('@/apis/auth_apis.js');
          const res = await wechatMiniProgramLogin({
            code: loginRes.code,
            userInfo: null,
            phoneCode: null
          });
          
          if (res.code === 200 && res.data && res.data.userInfo) {
            // 从返回的用户信息中获取openid（可能是openId或openid）
            const openid = res.data.userInfo.openId || res.data.userInfo.openid;
            if (openid) {
              console.log('通过后端接口获取到openid:', openid);
              // 更新本地用户信息（如果用户已登录）
              const currentUserInfo = uni.getStorageSync('userInfo');
              if (currentUserInfo) {
                currentUserInfo.openid = openid;
                uni.setStorageSync('userInfo', currentUserInfo);
                console.log('已更新本地用户信息的openid');
              }
              return openid;
            }
          }
        } catch (apiError) {
          console.warn('调用后端接口获取openid失败（可能是手机号登录用户）:', apiError);
          // 如果是手机号登录的用户，这里会失败，返回null让支付页面降级为二维码支付
        }

        console.warn('无法获取openid（用户可能是通过手机号登录的），将使用二维码支付');
        return null;
      } catch (error) {
        console.error('获取openid失败:', error);
        return null;
      }
    },

    /**
     * 调起微信支付（小程序环境）
     */
    async requestWeChatPay(paymentData) {
      try {
        const payParams = {
          provider: 'wxpay',
          timeStamp: paymentData.timeStamp,
          nonceStr: paymentData.nonceStr,
          package: paymentData.package,
          signType: paymentData.signType,
          paySign: paymentData.paySign
        };

        console.log('调起微信支付，参数:', payParams);

        const payRes = await new Promise((resolve, reject) => {
          uni.requestPayment({
            ...payParams,
            success: resolve,
            fail: reject
          });
        });

        console.log('微信支付成功:', payRes);
        
        // 支付成功，开始轮询支付状态
        this.startStatusPolling();
        
        uni.showToast({
          title: '支付成功',
          icon: 'success'
        });

        // 延迟跳转到订单列表
        setTimeout(() => {
          uni.redirectTo({
            url: '/pkg-mine/pages/orders/orders'
          });
        }, 1500);
      } catch (error) {
        console.error('微信支付失败:', error);
        
        // 如果用户取消支付，不显示错误提示
        if (error.errMsg && error.errMsg.includes('cancel')) {
          console.log('用户取消支付');
          return;
        }

        uni.showToast({
          title: error.errMsg || '支付失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.isPaying = false;
      }
    },
    // #endif
  }
};
</script>

<style scoped lang="scss">
.pay-entry-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.status-bar-bg {
  position: fixed;
  top: 0;
  width: 100%;
  background-color: #ffffff;
  z-index: 1001; /* 确保在navbar之上 */
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
  z-index: 1000;

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

.content-scroll {
  padding: 20rpx;
  box-sizing: border-box;
}

.order-info-section {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  .section-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 30rpx;
    padding-bottom: 20rpx;
    border-bottom: 1px solid #f0f0f0;
  }

  .order-item {
    .order-row {
      display: flex;
      align-items: center;
      margin-bottom: 32rpx;

      &:last-child {
        margin-bottom: 0;
      }

      &.pay-type-row {
        align-items: flex-start;
      }

      .label {
        font-size: 28rpx;
        color: #999;
        width: 160rpx;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        font-size: 28rpx;
        color: #333;
        word-break: break-all;

        &.product-name {
          font-weight: 500;
          color: #333;
        }

        &.price {
          font-size: 40rpx;
          font-weight: 700;
          color: #ff2442;
        }
      }

      .pay-type-badge {
        display: inline-flex;
        align-items: center;
        padding: 0;
        border-radius: 0;
        font-size: 26rpx;
        font-weight: 500;
        background: transparent;

        &.wechat-badge {
          background: transparent;
          color: #333;
        }

        &.alipay-badge {
          background: transparent;
          color: #333;
        }

        .pay-type-icon {
          width: 44rpx;
          height: 44rpx;
          margin-right: 12rpx;
          flex-shrink: 0;

          &.wechat-icon {
            width: 44rpx;
            height: 44rpx;
          }
        }

        .pay-type-text {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
        }
      }
    }
  }
}

/* 支付方式选择器样式（仅H5） */
/* #ifdef H5 */
.payment-method-selector {
  margin: 30rpx 30rpx 20rpx;
  padding: 30rpx;
  background: #f5f7fa;
  border-radius: 12rpx;

  .selector-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    text-align: center;
  }

  .payment-method-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #e5e7eb;

    &:last-child {
      border-bottom: none;
    }

    radio {
      margin-right: 20rpx;
      transform: scale(1.2);
    }

    text {
      font-size: 28rpx;
      color: #606266;
    }
  }
}
/* #endif */

.qr-code-section {
  // height: 50vh;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 40rpx 40rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  .section-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;

    .title-icon {
      width: 40rpx;
      height: 40rpx;
      flex-shrink: 0;
    }
  }

  .qr-code-container {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 40rpx;
    padding: 30rpx;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 20rpx;

    .qr-code-image {
      width: 400rpx;
      height: 400rpx;
      border: 2px solid #e8e8e8;
      border-radius: 16rpx;
      background-color: #fff;
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    }
  }

  .qr-code-tip {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 44rpx;
    line-height: 1.6;
    font-weight: 500;
  }

  .countdown-tip {
    font-size: 26rpx;
    color: #ff2442;
    font-weight: 500;
    padding: 12rpx 24rpx;
    background-color: #fff5f5;
    border-radius: 50rpx;
    display: inline-block;
  }
}

.payment-tip-section {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  .tip-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24rpx;
    margin-bottom: 30rpx;

    .tip-text {
      font-size: 30rpx;
      color: #666;
      font-weight: 500;
    }
  }

  .countdown-tip {
    font-size: 26rpx;
    color: #ff2442;
    font-weight: 500;
    padding: 12rpx 24rpx;
    background-color: #fff5f5;
    border-radius: 50rpx;
    display: inline-block;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);

  .total-info {
    display: flex;
    align-items: baseline;
    gap: 8rpx;

    .total-label {
      font-size: 28rpx;
      color: #666;
    }

    .total-price {
      font-size: 40rpx;
      font-weight: 700;
      color: #ff2442;
    }
  }

  .action-btn {
    width: 260rpx;
    height: 80rpx;
    background: linear-gradient(135deg, #3d8af5 0%, #2d6ed8 100%);
    border-radius: 50rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 32rpx;
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba(61, 138, 245, 0.3);

    &.disabled {
      background: #ccc;
      color: #999;
      box-shadow: none;
    }
  }
}
</style>


