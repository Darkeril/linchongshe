<template>
  <!-- 保持模板部分基本不变 -->
  <div class="container">
    <div class="push-container">
      <div class="header">
        <span class="header-icon"></span>
        <span class="header-title">{{ $t("order.payTitle") }}</span>
      </div>

      <div class="img-list">
        <div
          v-if="isAwaitingPaymentUi"
          style="color: white; font-size: 20px; font-weight: bold"
        >
          {{ $t("order.payAmount") }}：{{ convert.to_price(paymentOrderInfo.payPrice) }} {{ $t("order.yuan") }}
        </div>
        <div v-if="String(paymentOrderInfo.orderStatus) === '3'" style="color: white; font-size: 20px; font-weight: bold">
          {{ $t("order.paySuccess") }}
        </div>
        <!-- 添加倒计时显示 -->
        <div
          v-if="isAwaitingPaymentUi"
          style="color: white; font-size: 14px; margin-top: 10px"
        >
          {{ $t("order.payRemaining") }}：{{ formatCountdown }}
        </div>
      </div>

      <div style="margin-left: 20px">
        <el-divider style="width: 576px" />
      </div>

      <div class="header-title" style="margin-top: 10px; margin-bottom: 15px; font-size: 18px; padding-left: 20px">
        {{ $t("order.orderInfo") }}
      </div>

      <div style="margin-left: 20px">
        <el-descriptions class="margin-top" :column="1" border>
          <el-descriptions-item>
            <template #label>
              <i class="el-icon-s-order"></i>
              {{ $t("order.orderNumber") }}
            </template>
            {{ paymentOrderInfo.orderNumber }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <i class="el-icon-s-release"></i>
              {{ $t("order.orderType") }}
            </template>
            {{ $t("order.productBuy") }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <i class="el-icon-s-release"></i>
              {{ $t("order.payType") }}
            </template>
            <span :class="getPayTypeClass" class="pay-type-badge">
              <img v-if="paymentOrderInfo.payType === '2'" src="@/assets/zfb.png" :alt="$t('order.alipay')" class="pay-type-icon" />
              <img
                v-else-if="paymentOrderInfo.payType === '1'"
                src="@/assets/wx.png"
                :alt="$t('order.wechatPay')"
                class="pay-type-icon wechat-icon"
              />
              <span class="pay-type-text">{{ getPayTypeText }}</span>
            </span>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <i class="el-icon-s-order"></i>
              {{ $t("order.orderStatusLabel") }}
            </template>
            <span :class="getStatusClass">{{ getPaymentStatusText }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="submit">
        <div v-if="isAwaitingPaymentUi">
          <!-- 支付宝支付方式选择 -->
          <div v-if="paymentOrderInfo.payType === '2' && showPaymentMethodSelector" class="payment-method-selector">
            <el-radio-group v-model="alipayPaymentMethod" size="small">
              <el-radio label="form" v-if="paymentConfig && paymentConfig.alipaySandboxEnabled"
                >{{ $t("order.redirectPay") }}</el-radio
              >
              <el-radio label="qrCode" v-if="paymentConfig && paymentConfig.alipayQrcodeEnabled">{{ $t("order.scanPay") }}</el-radio>
            </el-radio-group>
          </div>
          <button class="publishBtn" type="submit" @click.prevent="pay" :disabled="isExpired">
            <span class="btn-content">{{ isExpired ? $t("order.orderExpired") : $t("order.confirmPay") }}</span>
          </button>
          <button class="clearBtn" @click="cancel">
            <span class="btn-content">{{ isExpired ? $t("common.back") : $t("common.cancel") }}</span>
          </button>
        </div>
        <div v-else>
          <button class="publishBtn" type="submit" @click="router.push('/idle')">
            <span class="btn-content">{{ $t("common.back") }}</span>
          </button>
        </div>
      </div>

      <!-- 用于存放支付表单的隐藏 div -->
      <div id="payment-form-container" style="display: none"></div>
    </div>

    <!-- 支付跳转提示弹窗 - 支付宝 -->
    <el-dialog
      v-model="paymentDialogVisible"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      width="400px"
      class="payment-dialog"
      v-if="paymentOrderInfo.payType === '2'"
    >
      <div class="payment-dialog-content">
        <div class="dialog-header">
          <div class="alipay-logo"></div>
        </div>

        <div class="loading-spinner">
          <svg viewBox="25 25 50 50" class="circular">
            <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
          </svg>
        </div>

        <h2 class="dialog-title">{{ $t("order.redirectingPayTitle") }}</h2>
        <div class="dialog-tips">
          <p>{{ $t("order.alipayRedirectWait") }}</p>
          <p>{{ $t("order.doNotCloseAfterPay") }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 支付宝扫码支付二维码弹窗 -->
    <el-dialog
      v-model="alipayPayDialogVisible"
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      width="400px"
      class="alipay-payment-dialog"
      v-if="paymentOrderInfo.payType === '2'"
    >
      <div class="alipay-payment-dialog-content">
        <div class="dialog-header">
          <div class="alipay-logo-dialog">
            <img src="@/assets/zfb.png" :alt="$t('order.alipay')" class="alipay-logo-icon" />
            <span class="alipay-logo-text">{{ $t("order.alipayCheckoutTitle") }}</span>
          </div>
        </div>

        <div class="qr-code-container" v-if="alipayQRCodeUrl">
          <el-image
            :src="alipayQrCodeImageUrl"
            fit="contain"
            style="width: 250px; height: 250px; margin: 20px auto; display: block"
          />
          <p class="qr-code-tips">{{ $t("order.scanAlipayToPay") }}</p>
          <p class="qr-code-tips-small">{{ $t("order.afterPayClickIHavePaid") }}</p>
        </div>
        <div class="qr-code-loading" v-else>
          <div class="loading-spinner">
            <svg viewBox="25 25 50 50" class="circular">
              <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
            </svg>
          </div>
          <p>{{ $t("order.generatingQR") }}</p>
        </div>

        <div class="dialog-actions">
          <el-button @click="alipayPayDialogVisible = false">{{ $t("common.cancel") }}</el-button>
          <el-button type="primary" @click="checkPaymentStatus">{{ $t("order.iHavePaid") }}</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 微信支付二维码弹窗 -->
    <el-dialog
      v-model="wechatPayDialogVisible"
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      width="400px"
      class="wechat-payment-dialog"
      v-if="paymentOrderInfo.payType === '1'"
    >
      <div class="wechat-payment-dialog-content">
        <div class="dialog-header">
          <div class="wechat-logo">
            <img src="@/assets/wx.png" :alt="$t('order.wechatPay')" class="wechat-logo-icon" />
            <span class="wechat-logo-text">{{ $t("order.wechatPay") }}</span>
          </div>
        </div>

        <div class="qr-code-container" v-if="wechatQRCodeUrl">
          <el-image
            :src="qrCodeImageUrl"
            fit="contain"
            style="width: 250px; height: 250px; margin: 20px auto; display: block"
          />
          <p class="qr-code-tips">{{ $t("order.scanWechatToPay") }}</p>
          <p class="qr-code-tips-small">{{ $t("order.afterPayClickIHavePaid") }}</p>
        </div>
        <div class="qr-code-loading" v-else>
          <div class="loading-spinner">
            <svg viewBox="25 25 50 50" class="circular">
              <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
            </svg>
          </div>
          <p>{{ $t("order.generatingQR") }}</p>
        </div>

        <div class="dialog-actions">
          <el-button @click="wechatPayDialogVisible = false">{{ $t("common.cancel") }}</el-button>
          <el-button type="primary" @click="checkPaymentStatus">{{ $t("order.iHavePaid") }}</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  getPaymentOrder,
  createPayOrder,
  payOrder,
  cancelPay,
  updateOrderStatus,
  getPaymentGlobalConfig,
} from "@/api/idle";
import convert from "@/utils/convert";
import { ElMessage, ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";

// 路由
const router = useRouter();
const route = useRoute();
const { t } = useI18n();

// 类型定义
interface PaymentOrderInfo {
  orderStatus: string;
  payPrice: number;
  orderNumber: string;
  payType: string;
  payTypeName: string;
  paymentPayId: string;
  createTime?: string;
  expireTime?: string; // 添加过期时间
}

// 响应式状态
const paymentOrderInfo = ref<PaymentOrderInfo>({
  orderStatus: "0",
  payPrice: 0,
  orderNumber: "",
  payType: "",
  payTypeName: "",
  paymentPayId: "",
});
const paymentPayId = ref("");
const paymentDialogVisible = ref(false); // 支付宝沙箱跳转支付弹窗
const wechatPayDialogVisible = ref(false); // 微信支付弹窗
const alipayPayDialogVisible = ref(false); // 支付宝扫码支付弹窗
const wechatQRCodeUrl = ref(""); // 微信支付二维码URL
const alipayQRCodeUrl = ref(""); // 支付宝支付二维码URL
const qrCodeImageUrl = ref(""); // 二维码图片URL（用于显示）
const alipayQrCodeImageUrl = ref(""); // 支付宝二维码图片URL（用于显示）
const alipayPaymentMethod = ref<"form" | "qrCode">("form"); // 支付宝支付方式：form=沙箱跳转，qrCode=扫码支付
const paymentConfig = ref<any>(null); // 支付全局配置
const showPaymentMethodSelector = ref(true); // 是否显示支付方式选择器
/** 与后端 payment.timeout.minutes 对齐；若接口未返回则默认 15 */
const paymentTimeoutMinutes = ref(15);

const paymentOrderStatusStr = computed(() => String(paymentOrderInfo.value.orderStatus ?? ""));
/** 未支付 / 待支付 / 支付中：展示金额、倒计时与支付按钮 */
const isAwaitingPaymentUi = computed(() => ["0", "1", "2"].includes(paymentOrderStatusStr.value));

// 添加订单状态文字显示的计算属性
const getPaymentStatusText = computed(() => {
  switch (paymentOrderStatusStr.value) {
    case "0":
      return t("order.unpaid");
    case "1":
      return t("order.pendingPay");
    case "2":
      return t("order.paying");
    case "3":
      return t("order.paidStatus");
    case "4":
      return t("order.transactionSuccess");
    default:
      return t("order.unknownStatus");
  }
});

const getPayTypeText = computed(() => {
  switch (paymentOrderInfo.value.payType) {
    case "1":
      return t("order.wechatPay");
    case "2":
      return t("order.alipay");
    default:
      return t("order.alipay");
  }
});

// 支付类型样式类
const getPayTypeClass = computed(() => {
  switch (paymentOrderInfo.value.payType) {
    case "1":
      return "pay-type-wechat";
    case "2":
      return "pay-type-alipay";
    default:
      return "pay-type-alipay";
  }
});

// 添加状态样式的计算属性
const getStatusClass = computed(() => {
  switch (paymentOrderStatusStr.value) {
    case "0":
      return "status-unpaid";
    case "1":
      return "status-pending";
    case "2":
      return "status-pending";
    case "3":
      return "status-paid";
    default:
      return "";
  }
});

// 倒计时相关
const countdown = ref(900); // 默认 15 分钟，单位秒；加载配置后会按 paymentTimeoutMinutes 重算
const countdownTimer = ref<ReturnType<typeof setInterval> | null>(null);
const isExpired = computed(() => countdown.value <= 0);

// 格式化倒计时显示
const formatCountdown = computed(() => {
  if (countdown.value <= 0) return "00:00";
  const minutes = Math.floor(countdown.value / 60);
  const seconds = countdown.value % 60;
  return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
});

// 开始倒计时
const startCountdown = () => {
  // 清除可能存在的旧定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
  }

  // 计算剩余时间
  const windowSec = paymentTimeoutMinutes.value * 60;
  let remainingTime = windowSec;

  if (paymentOrderInfo.value.expireTime) {
    // 如果有明确的过期时间，直接计算剩余秒数
    const expireTime = new Date(paymentOrderInfo.value.expireTime).getTime();
    const now = new Date().getTime();
    remainingTime = Math.max(Math.floor((expireTime - now) / 1000), 0);
  } else if (paymentOrderInfo.value.createTime) {
    // 根据创建时间计算
    const createTime = new Date(paymentOrderInfo.value.createTime).getTime();
    const now = new Date().getTime();
    const elapsedSeconds = Math.floor((now - createTime) / 1000);
    remainingTime = Math.max(windowSec - elapsedSeconds, 0);
  }

  countdown.value = remainingTime;

  // 只有在订单未过期的情况下才设置定时器
  if (countdown.value > 0) {
    countdownTimer.value = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        handleOrderExpired();
      }
    }, 1000);
  } else {
    handleOrderExpired();
  }
};

// 修改倒计时相关逻辑
const handleOrderExpired = async () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
    countdownTimer.value = null;
  }

  // 只有在订单状态为未支付或待支付时才更新状态
  if (["0", "1", "2"].includes(paymentOrderStatusStr.value)) {
    try {
      await updateOrder(route.query.paymentOrderId as string);
      await refreshOrderStatus();
      // 只在页面可见时才显示提示
      if (document.visibilityState === "visible") {
        ElMessage.warning(t("order.orderTimeoutReorder"));
      }
    } catch (error) {
      console.error("更新订单状态失败:", error);
    }
  }
};

const updateOrder = async (paymentOrderId: string) => {
  try {
    await updateOrderStatus(paymentOrderId);
  } catch (error) {
    console.error("更新订单状态失败:", error);
    throw error;
  }
};

// 刷新订单状态
const refreshOrderStatus = async () => {
  try {
    const res = await getPaymentOrder(route.query.paymentOrderId as string);
    paymentOrderInfo.value = res.data;
  } catch (error) {
    console.error("刷新订单状态失败:", error);
  }
};

// 加载支付配置
const loadPaymentConfig = async () => {
  try {
    const res = (await getPaymentGlobalConfig()) as any;
    console.log("支付全局配置:", res);

    if (res.code === 200 && res.data) {
      paymentConfig.value = res.data;
      const tm = res.data.paymentTimeoutMinutes;
      if (typeof tm === "number" && tm > 0 && tm <= 1440) {
        paymentTimeoutMinutes.value = tm;
      }

      // 检查全局支付开关
      if (!paymentConfig.value.paymentEnabled) {
        ElMessage.warning(t("order.paymentClosedCannotPay"));
        setTimeout(() => {
          router.back();
        }, 2000);
        return;
      }

      // 根据配置设置支付宝支付方式选择器的显示逻辑
      const alipaySandboxEnabled = paymentConfig.value.alipaySandboxEnabled;
      const alipayQrcodeEnabled = paymentConfig.value.alipayQrcodeEnabled;

      // 只有当两种支付方式都可用时才显示选择器
      showPaymentMethodSelector.value = alipaySandboxEnabled && alipayQrcodeEnabled;

      // 根据配置设置默认的支付宝支付方式
      if (alipaySandboxEnabled && !alipayQrcodeEnabled) {
        // 只有沙箱支付可用
        alipayPaymentMethod.value = "form";
      } else if (!alipaySandboxEnabled && alipayQrcodeEnabled) {
        // 只有扫码支付可用
        alipayPaymentMethod.value = "qrCode";
      }
    }
  } catch (error) {
    console.error("加载支付配置失败:", error);
    // 加载失败时不影响页面使用，使用默认配置
  }
};

// 获取支付订单信息
const getPaymentOrderInfo = async () => {
  try {
    const res = await getPaymentOrder(route.query.paymentOrderId as string);
    console.log("获取订单信息响应:", res);
    console.log("订单支付类型:", res.data?.payType);
    paymentOrderInfo.value = res.data;
    paymentPayId.value = res.data.paymentPayId;

    // 如果是从商品页面跳转过来且订单状态为待支付，则自动调用支付
    // if (route.query.from === 'product' && paymentOrderInfo.value.orderStatus === '1') {
    //   await pay();
    // }

    // 如果订单状态为待支付或支付中，则开始倒计时
    if (["0", "1", "2"].includes(paymentOrderStatusStr.value)) {
      if (paymentOrderStatusStr.value === "0") {
        await createPaymentPay();
      }
      startCountdown();
    }
  } catch (error) {
    console.error("获取支付订单失败:", error);
    ElMessage.error(t("order.getPayOrderFailedRetry"));
  }
};

// 创建支付订单
const createPaymentPay = async () => {
  try {
    const res = await createPayOrder(route.query.paymentOrderId as string);
    paymentPayId.value = res.data;
  } catch (error) {
    console.error("创建支付订单失败:", error);
    ElMessage.error(t("order.createPayOrderFailedRetry"));
  }
};

// 支付
const pay = async () => {
  if (!["0", "1", "2"].includes(paymentOrderStatusStr.value)) {
    ElMessage.warning(t("order.statusNotAllowPay"));
    return;
  }
  if (isExpired.value) {
    ElMessage.warning(t("order.orderExpiredReorder"));
    return;
  }

  try {
    // 根据支付类型和支付方式传递参数
    const paymentMethod =
      paymentOrderInfo.value.payType === "2" && alipayPaymentMethod.value === "qrCode" ? "qrCode" : undefined;

    const res = (await payOrder(paymentPayId.value, paymentMethod)) as any;

    console.log("支付响应:", res);
    console.log("订单支付类型:", paymentOrderInfo.value.payType);
    console.log("支付宝支付方式:", alipayPaymentMethod.value);
    console.log("响应数据:", res.data);

    if (res.code === 200) {
      // 根据支付类型处理
      // 注意：优先检查响应中的 payType，如果没有则使用订单信息中的 payType
      const payType = res.data?.payType || paymentOrderInfo.value.payType;
      console.log("实际使用的支付类型:", payType);

      if (payType === "1" || payType === "wechat") {
        // 微信支付：显示二维码
        wechatQRCodeUrl.value = res.data.codeUrl || res.data.qrCodeUrl;
        console.log("微信支付二维码URL:", wechatQRCodeUrl.value);
        if (wechatQRCodeUrl.value) {
          // 使用二维码生成库或API生成二维码图片
          qrCodeImageUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(
            wechatQRCodeUrl.value
          )}`;
          wechatPayDialogVisible.value = true;
          // 开始轮询支付状态
          startPaymentStatusPolling();
        } else {
          console.error("微信支付二维码URL为空，响应数据:", res.data);
          ElMessage.error(t("order.getQrcodeFailedContact"));
        }
      } else {
        // 支付宝支付
        if (alipayPaymentMethod.value === "qrCode" && (res.data.codeUrl || res.data.qrCodeUrl)) {
          // 支付宝扫码支付：显示二维码
          alipayQRCodeUrl.value = res.data.codeUrl || res.data.qrCodeUrl;
          console.log("支付宝支付二维码URL:", alipayQRCodeUrl.value);
          if (alipayQRCodeUrl.value) {
            alipayQrCodeImageUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(
              alipayQRCodeUrl.value
            )}`;
            alipayPayDialogVisible.value = true;
            // 开始轮询支付状态
            startPaymentStatusPolling();
          } else {
            console.error("支付宝支付二维码URL为空，响应数据:", res.data);
            ElMessage.error(t("order.getQrcodeFailedContact"));
          }
        } else {
          // 支付宝沙箱跳转支付：显示跳转提示并提交表单
          paymentDialogVisible.value = true;
          setTimeout(() => {
            const div = document.createElement("div");
            div.innerHTML = res.data.paymentFormHtml;
            document.body.appendChild(div);
            const form = div.querySelector("form");
            if (form) {
              console.log("生成的支付表单:", form);
              form.submit();
            } else {
              paymentDialogVisible.value = false;
              console.error("未找到支付表单");
              ElMessage.error(t("order.payFormNotFound"));
            }
          }, 1500);
        }
      }
    } else {
      paymentDialogVisible.value = false;
      wechatPayDialogVisible.value = false;
      alipayPayDialogVisible.value = false;
      const errorMsg = res.data?.message || res.message || t("order.payFailedRetry");
      console.error("支付失败:", res);
      ElMessage.error(errorMsg);
    }
  } catch (error: any) {
    paymentDialogVisible.value = false;
    wechatPayDialogVisible.value = false;
    alipayPayDialogVisible.value = false;
    console.error("支付失败:", error);
    const errorMsg = error?.response?.data?.message || error?.message || t("order.payFailedRetry");
    ElMessage.error(errorMsg);
  }
};

// 轮询支付状态（微信支付使用）
let paymentStatusPollingTimer: ReturnType<typeof setInterval> | null = null;

const startPaymentStatusPolling = () => {
  // 清除之前的定时器
  if (paymentStatusPollingTimer) {
    clearInterval(paymentStatusPollingTimer);
  }

  // 每3秒查询一次支付状态
  paymentStatusPollingTimer = setInterval(async () => {
    try {
      await refreshOrderStatus();
      // 如果支付成功，停止轮询并关闭弹窗
      if (paymentOrderStatusStr.value === "3") {
        stopPaymentStatusPolling();
        wechatPayDialogVisible.value = false;
        alipayPayDialogVisible.value = false;
        ElMessage.success(t("order.paySuccessExclaim"));
        // 刷新页面或跳转
        setTimeout(() => {
          router.push("/idle");
        }, 1500);
      }
    } catch (error) {
      console.error("查询支付状态失败:", error);
    }
  }, 3000);
};

const stopPaymentStatusPolling = () => {
  if (paymentStatusPollingTimer) {
    clearInterval(paymentStatusPollingTimer);
    paymentStatusPollingTimer = null;
  }
};

// 检查支付状态（用户点击"我已支付"按钮）
const checkPaymentStatus = async () => {
  try {
    await refreshOrderStatus();
    if (paymentOrderStatusStr.value === "3") {
      wechatPayDialogVisible.value = false;
      alipayPayDialogVisible.value = false;
      ElMessage.success(t("order.paySuccessExclaim"));
      setTimeout(() => {
        router.push("/idle");
      }, 1500);
    } else {
      ElMessage.warning(t("order.payNotFinishedRetry"));
    }
  } catch (error) {
    console.error("查询支付状态失败:", error);
    ElMessage.error(t("order.queryPayStatusFailedRetry"));
  }
};

// 取消支付
const cancel = async () => {
  // 如果订单已过期，直接返回
  if (isExpired.value) {
    returnToPreviousPage();
    return;
  }
  try {
    await ElMessageBox.confirm(t("order.confirmCancelPay"), t("common.tip"), {
      confirmButtonText: t("common.confirm"),
      cancelButtonText: t("common.cancel"),
      type: "warning",
    });
    // 用户点击确定后执行取消操作
    await cancelPay(paymentPayId.value);
    returnToPreviousPage();
  } catch (error) {
    // 用户点击取消按钮会触发 catch，不需要处理
    if (error !== "cancel") {
      console.error("取消支付失败:", error);
      ElMessage.error(t("order.cancelPayFailedRetry"));
    }
  }
};

// 提取返回逻辑到单独方法
const returnToPreviousPage = () => {
  const { from } = route.query;
  if (from === "detail") {
    router.back(); // 返回详情页
  } else {
    router.push("/idle"); // 返回列表页
  }
};

// 提取visibilitychange处理函数
const handleVisibilityChange = async () => {
  if (document.visibilityState === "visible") {
    // 获取最新订单状态
    const prevStatus = String(paymentOrderInfo.value.orderStatus ?? "");
    await refreshOrderStatus();

    if (["0", "1", "2"].includes(prevStatus) && countdown.value <= 0) {
      ElMessage.warning(t("order.orderTimeoutReorder"));
    }
  }
};

onMounted(async () => {
  await loadPaymentConfig();
  getPaymentOrderInfo();
  document.addEventListener("visibilitychange", handleVisibilityChange);
});

// 组件卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
    countdownTimer.value = null;
  }
  stopPaymentStatusPolling();
  document.removeEventListener("visibilitychange", handleVisibilityChange);
});
</script>

<style lang="less" scoped>
.payment-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;

    .el-dialog__header {
      display: none;
    }

    .el-dialog__body {
      padding: 0;
    }
  }
}

.payment-dialog-content {
  padding: 0;
  text-align: center;
  background: linear-gradient(to bottom, #f8f8f8, #ffffff);

  .dialog-header {
    background: #fff;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .alipay-logo {
      width: 114px;
      height: 40px;
      margin: 0 auto;
      background: url(https://t.alipayobjects.com/images/T1HHFgXXVeXXXXXXXX.png) no-repeat center;
      background-size: contain;
    }
  }

  .loading-spinner {
    margin: 30px auto 20px;

    .circular {
      width: 42px;
      height: 42px;
      animation: loading-rotate 2s linear infinite;
    }

    .path {
      stroke-dasharray: 90, 150;
      stroke-dashoffset: 0;
      stroke-width: 2;
      stroke: #1677ff;
      stroke-linecap: round;
      animation: loading-dash 1.5s ease-in-out infinite;
    }
  }

  .dialog-title {
    font-size: 20px;
    color: #333;
    margin: 0 0 20px;
    font-weight: 500;
  }

  .dialog-tips {
    padding: 0 20px 30px;

    p {
      margin: 8px 0;
      color: #666;
      font-size: 14px;
      line-height: 1.5;

      &:last-child {
        color: #999;
        font-size: 13px;
      }
    }
  }
}

// 添加状态样式
.status-unpaid {
  color: #f56c6c;
}

.status-pending {
  color: #e6a23c;
}

.status-paid {
  color: #67c23a;
}

/* 支付类型样式 */
.pay-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;

  .pay-type-icon {
    width: 25px;
    height: 25px;
    flex-shrink: 0;

    &.wechat-icon {
      width: 20px;
      height: 20px;
    }
  }

  .pay-type-text {
    font-weight: 600;
  }
}

.pay-type-wechat {
  background-color: #f0f9ff;
  color: #07c160;
  border: 1px solid #07c160;

  .pay-type-text {
    color: #07c160;
  }
}

.pay-type-alipay {
  background-color: #fff7e6;
  color: #1677ff;
  border: 1px solid #1677ff;

  .pay-type-text {
    color: #1677ff;
  }
}

/* 支付宝扫码支付弹窗样式 */
.alipay-payment-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;

    .el-dialog__header {
      display: none;
    }

    .el-dialog__body {
      padding: 0;
    }
  }
}

.alipay-payment-dialog-content {
  padding: 0;
  text-align: center;
  background: linear-gradient(to bottom, #f8f8f8, #ffffff);

  .dialog-header {
    background: #fff;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .alipay-logo-dialog {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 10px;

      .alipay-logo-icon {
        width: 32px;
        height: 32px;
        object-fit: contain;
        flex-shrink: 0;
      }

      .alipay-logo-text {
        font-size: 18px;
        font-weight: bold;
        color: #1677ff;
      }
    }
  }

  .qr-code-container {
    padding: 20px;

    .qr-code-tips {
      margin: 15px 0 5px;
      color: #333;
      font-size: 16px;
      font-weight: 500;
    }

    .qr-code-tips-small {
      margin: 5px 0;
      color: #999;
      font-size: 13px;
    }
  }

  .qr-code-loading {
    padding: 40px 20px;

    .loading-spinner {
      margin: 20px auto;

      .circular {
        width: 42px;
        height: 42px;
        animation: loading-rotate 2s linear infinite;
      }

      .path {
        stroke-dasharray: 90, 150;
        stroke-dashoffset: 0;
        stroke-width: 2;
        stroke: #1677ff;
        stroke-linecap: round;
        animation: loading-dash 1.5s ease-in-out infinite;
      }
    }

    p {
      margin-top: 20px;
      color: #666;
      font-size: 14px;
    }
  }

  .dialog-actions {
    padding: 20px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: center;
    gap: 12px;

    .el-button {
      min-width: 100px;
    }
  }
}

/* 支付方式选择器样式 */
.payment-method-selector {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  text-align: center;

  :deep(.el-radio-group) {
    .el-radio {
      margin-right: 24px;

      &:last-child {
        margin-right: 0;
      }

      .el-radio__label {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

/* 微信支付弹窗样式 */
.wechat-payment-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;

    .el-dialog__header {
      display: none;
    }

    .el-dialog__body {
      padding: 0;
    }
  }
}

.wechat-payment-dialog-content {
  padding: 0;
  text-align: center;
  background: linear-gradient(to bottom, #f8f8f8, #ffffff);

  .dialog-header {
    background: #fff;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .wechat-logo {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 10px;

      .wechat-logo-icon {
        width: 32px;
        height: 32px;
        object-fit: contain;
        flex-shrink: 0;
      }

      .wechat-logo-text {
        font-size: 18px;
        font-weight: bold;
        color: #07c160;
      }
    }
  }

  .qr-code-container {
    padding: 20px;

    .qr-code-tips {
      margin: 15px 0 5px;
      color: #333;
      font-size: 16px;
      font-weight: 500;
    }

    .qr-code-tips-small {
      margin: 5px 0;
      color: #999;
      font-size: 13px;
    }
  }

  .qr-code-loading {
    padding: 40px 20px;

    .loading-spinner {
      margin: 20px auto;

      .circular {
        width: 42px;
        height: 42px;
        animation: loading-rotate 2s linear infinite;
      }

      .path {
        stroke-dasharray: 90, 150;
        stroke-dashoffset: 0;
        stroke-width: 2;
        stroke: #07c160;
        stroke-linecap: round;
        animation: loading-dash 1.5s ease-in-out infinite;
      }
    }

    p {
      margin-top: 20px;
      color: #666;
      font-size: 14px;
    }
  }

  .dialog-actions {
    padding: 20px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: center;
    gap: 15px;
  }
}

.container {
  flex: 1;
  padding-top: 72px;

  .push-container {
    margin-left: 12vw;
    width: 600px;
    border-radius: 8px;
    box-sizing: border-box;
    box-shadow: var(--el-box-shadow-lighter);

    .header {
      padding: 15px 5px;
      line-height: 16px;
      font-size: 16px;
      font-weight: 400;

      .header-icon {
        position: relative;
        top: 2px;
        display: inline-block;
        width: 6px;
        height: 19px;
        background: #3a64ff;
        border-radius: 3px;
        margin-right: 8px;
      }

      .header-title {
        line-height: 20px;
        font-size: 20px;
      }
    }

    .img-list {
      width: 576px;
      margin-left: 20px;
      padding: 0;
      height: 200px;
      background-color: #3a64ff;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
    }

    .push-content {
      position: relative;

      .scroll-tag-container {
        position: absolute;
        width: 98%;
        background-color: #fff;
        z-index: 99999;
        border: 1px solid #f4f4f4;
        height: 300px;
        overflow: auto;

        .scrollbar-tag-item {
          display: flex;
          align-items: center;
          height: 30px;
          margin: 10px;
          text-align: center;
          border-radius: 4px;
          padding-left: 2px;
          color: #484848;
          font-size: 14px;
        }

        .scrollbar-tag-item:hover {
          background-color: #f8f8f8;
        }
      }

      .input-title {
        margin-bottom: 5px;
        font-size: 12px;
      }

      .input-content {
        font-size: 12px;
      }
    }

    .btns {
      padding: 0 12px 0px 20px;

      button {
        min-width: 62px;
        width: 62px;
        margin: 0 6px 0 0;
        height: 18px;
      }

      .css-fm44j {
        -webkit-font-smoothing: antialiased;
        appearance: none;
        font-family: RedNum, RedZh, RedEn, -apple-system;
        vertical-align: middle;
        text-decoration: none;
        border: 1px solid rgb(217, 217, 217);
        outline: none;
        user-select: none;
        cursor: pointer;
        display: inline-flex;
        -webkit-box-pack: center;
        justify-content: center;
        -webkit-box-align: center;
        align-items: center;
        margin-right: 16px;
        border-radius: 4px;
        background-color: white;
        color: rgb(38, 38, 38);
        height: 24px;
        font-size: 12px;
      }
    }

    .categorys {
      padding: 0 12px 10px 12px;
    }

    .trend-container {
      padding-left: 15px;

      .trend-item {
        display: flex;
        flex-direction: row;
        padding-top: 10px;
        max-width: 100vw;

        .main {
          flex-grow: 1;
          flex-shrink: 1;
          display: flex;
          flex-direction: row;
          padding-bottom: 12px;
          border-bottom: 1px solid rgba(0, 0, 0, 0.08);

          .details-box {
            width: 25%;
          }

          .info {
            flex-grow: 1;
            flex-shrink: 1;
            margin-left: 10px;

            .user-info {
              display: flex;
              flex-direction: row;
              align-items: center;
              font-size: 16px;
              font-weight: 600;
              margin-bottom: 4px;

              a {
                color: #333;
              }
            }

            .interaction-hint {
              font-size: 14px;
              color: rgba(51, 51, 51, 0.6);
              margin-bottom: 8px;
            }

            .interaction-content {
              display: flex;
              font-size: 14px;
              color: #333;
              margin-bottom: 12px;
              line-height: 140%;

              display: -webkit-box;
              -webkit-box-orient: vertical;
              -webkit-line-clamp: 3;
              word-break: break-all;
              overflow: hidden;
            }

            .interaction-price {
              font-weight: bolder;
              font-size: 16px;
              color: red;
            }

            .interaction-footer {
              margin: 8px 12px 0 0;
              padding: 0 12px;
              display: flex;
              justify-content: space-between;
              align-items: center;

              .icon-item {
                display: flex;
                justify-content: left;
                align-items: center;
                color: rgba(51, 51, 51, 0.929);

                .count {
                  margin-left: 3px;
                }
              }
            }
          }
        }
      }
    }

    .submit {
      padding: 0 12px 10px 20px;
      margin-top: 30px;

      button {
        width: 80px;
        height: 32px;
        font-size: 14px;
      }

      .publishBtn {
        background-color: #ff2442;
        color: #fff;
        border-radius: 4px;
        cursor: pointer;
      }

      .clearBtn {
        cursor: pointer;
        border-radius: 4px;
        margin-left: 10px;
        border: 1px solid rgb(217, 217, 217);
      }
    }
  }
}
</style>
