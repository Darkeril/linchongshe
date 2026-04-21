<template>
  <div class="reds-modal login-modal" role="presentation">
    <i tabindex="-1" class="reds-mask" :aria-label="$t('login.maskLabel')"></i>
    <div class="login-container">
      <!-- 关闭按钮 -->
      <div class="icon-btn-wrapper close-button" @click="close">
        <Close style="width: 1em; height: 1em" />
      </div>
      <!-- 左侧区域 -->
      <div class="left">
        <div class="login-reason">{{ $t("login.loginReason") }}</div>
        <img v-if="logoUrl" class="logo" :src="logoUrl" alt="logo" @error="handleLogoError" />
        <img v-else class="logo" src="@/assets/logo.png" alt="logo" />
        <div class="qrcode">
          <img class="qrcode-img" :src="qrcodeUrl" :alt="$t('login.qrCodeAlt')" />
          <div class="status" v-show="qrcodeStatusDisplay && qrcodeStatusDisplay !== $t('login.qrWaiting')">
            {{ qrcodeStatusDisplay }}
          </div>
        </div>
        <div class="tip">
          {{ $t("login.scanToLogin", { app: "" }) }}<span class="hongshu">{{ $t("login.appName") }}</span
          >{{ "" }}
        </div>
        <!-- 微信登录按钮 - 根据微信登录配置显示 -->
        <div v-if="wechatLoginConfig.enabled" class="wechat-login-left">
          <button class="wechat-btn-left" @click="handleWeChatLogin">
            <img src="@/assets/wx.png" :alt="$t('login.wechatAlt')" class="wechat-icon-left" />
            {{ $t("login.wechatLogin") }}
          </button>
        </div>
      </div>
      <!-- 右侧区域 -->
      <div class="right">
        <div class="title">{{ isPasswordLogin ? $t("login.passwordLogin") : $t("login.codeLogin") }}</div>
        <!-- 登录表单 -->
        <div class="input-container">
          <!-- 登录方式切换tab -->
          <div class="login-type-switch">
            <span :class="['login-type-tab', isPasswordLogin ? 'active' : '']" @click="setLoginType(true)">{{
              $t("login.passwordLogin")
            }}</span>
            <span :class="['login-type-tab', !isPasswordLogin ? 'active' : '']" @click="setLoginType(false)">{{
              $t("login.codeLogin")
            }}</span>
          </div>
          <form @submit.prevent="handleSubmit">
            <!-- 手机号输入 -->
            <label class="phone">
              <span class="country-code">+86</span>
              <input
                v-model="userLogin.phone"
                type="text"
                :placeholder="$t('login.phonePlaceholder')"
                @blur="validatePhone"
              />
              <svg v-show="userLogin.phone" class="reds-icon clear" width="24" height="24" @click="clearPhone">
                <use xlink:href="#clear"></use>
              </svg>
            </label>
            <!-- 密码/验证码输入 -->
            <div style="height: 16px"></div>
            <label class="auth-code">
              <template v-if="isPasswordLogin">
                <input
                  v-model="userLogin.password"
                  type="password"
                  :placeholder="$t('login.passwordPlaceholder')"
                  autocomplete="current-password"
                />
              </template>
              <template v-else>
                <input
                  v-model="userLogin.code"
                  type="text"
                  :placeholder="$t('login.codePlaceholder')"
                  autocomplete="off"
                  maxlength="6"
                  pattern="[0-9]*"
                  inputmode="numeric"
                  @input="validateCodeInput"
                />
                <!-- 验证码按钮 -->
                <span class="code-button" :class="{ disabled: !canSendCode || countdown > 0 }" @click="handleSendCode">
                  {{ countdown > 0 ? $t("login.retryAfter", { n: countdown }) : $t("login.getCode") }}
                </span>
              </template>
            </label>
            <!-- 错误提示 -->
            <div class="err-msg">{{ errorMessage }}</div>
            <!-- 登录按钮 -->
            <el-tooltip v-if="needAgreementTip" :content="$t('login.agreeTip')" placement="top" effect="dark">
              <button class="submit" :disabled="true" type="submit">{{ $t("login.login") }}</button>
            </el-tooltip>
            <button v-else class="submit" :disabled="!isFormValid" type="submit">{{ $t("login.login") }}</button>
          </form>
        </div>
        <!-- 用户协议 -->
        <div class="agreements">
          <label class="agreement-label">
            <input type="checkbox" v-model="agreedToTerms" class="agreement-checkbox" />
            <span class="checkmark"></span>
          </label>
          <span class="agree-text">{{ $t("login.agreeText") }}</span>
          <a class="links" target="_blank" href=""> {{ $t("login.userAgreement") }} </a>
          <a class="links" target="_blank" href=""> {{ $t("login.privacyPolicy") }} </a>
          <br />
          <a class="links" target="_blank" href="" style="margin-left: 25px"> {{ $t("login.childProtection") }} </a>
        </div>
        <div class="oauth-tip">
          <span class="oauth-tip-line">{{ $t("login.autoRegister") }}</span>
        </div>
        <!-- 演示账号登录 - 根据配置动态显示 -->
        <div v-if="demoAccountConfig.enabled" class="demo-login">
          <button class="demo-btn" @click="handleDemoLogin">
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
            {{ $t("login.demoAccount") }}
          </button>
        </div>
        <!-- 滑块验证码弹窗 - 根据配置动态显示 -->
        <Verify
          v-if="captchaConfig.isEnabled"
          ref="verifyRef"
          @success="capctchaCheckSuccess"
          :mode="'pop'"
          :captchaType="captchaType"
          :imgSize="{ width: '330px', height: '160px' }"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from "vue";
import { Close } from "@element-plus/icons-vue";
import type { UserLogin } from "@/type/user";
import {
  login,
  demoLogin,
  loginByCode,
  sendVerifyCode,
  getCaptchaType,
  getQrcodeUrl,
  checkQrCodeStatus,
  qrCodeLogin,
  generateWeChatAuthUrl,
  getWeChatLoginInfo,
} from "@/api/user";
import { getWebsiteConfig, getCaptchaConfig, getDemoAccountConfig, getWeChatLoginConfig } from "@/api/config";
import { storage } from "@/utils/storage";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import Verify from "@/components/verifition/Verify.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

// 状态管理
const userStore = useUserStore();
const router = useRouter();

// 响应式数据
const isPasswordLogin = ref(true);
const agreedToTerms = ref(false);
const errorMessage = ref("");
const countdown = ref(0);
const qrcodeStatus = ref("");
const qrcodeUrl = ref("");
const loginToken = ref("");
const qrCodePollingTimer = ref<any>(null);
const logoUrl = ref<string>("");

// 配置数据
const demoAccountConfig = ref({
  enabled: false,
  username: "",
  password: "",
  description: "",
  permissions: [],
  expireTime: "",
});

const wechatLoginConfig = ref({
  enabled: false,
});

const captchaConfig = ref({
  isEnabled: false,
  cacheType: "",
  type: "",
  waterMark: "",
  slipOffset: 5,
  aesStatus: false,
  interferenceOptions: 2,
  jigsaw: "",
  picClick: "",
});

const verifyRef = ref();
const sliderVerification = ref("");
// 验证码类型：点选/滑块
const captchaType = ref("");
// 标记当前是登录流程还是发验证码流程
const isLoginAction = ref(false);
const sliderVerified = ref(false);

let timer: any = null;
let qrTimer: any = null;

const userLogin = ref<UserLogin>({
  phone: "",
  email: "",
  code: "",
  username: "",
  password: "",
  captchaVerification: "",
});

const qrStatusMap: Record<string, string> = {
  pending: "login.qrWaiting",
  failed: "login.qrFailed",
  confirmed: "login.qrSuccess",
  scanned: "login.qrConfirm",
  expired: "login.qrExpired",
};
const qrcodeStatusDisplay = computed(() => {
  const key = qrStatusMap[qrcodeStatus.value];
  return key ? t(key) : qrcodeStatus.value;
});

const setLoginType = (isPwd: boolean) => {
  isPasswordLogin.value = isPwd;
  userLogin.value.password = "";
  userLogin.value.code = "";
  errorMessage.value = "";
};

// 获取验证码配置
const fetchCaptchaConfig = async () => {
  try {
    const response = (await getCaptchaConfig()) as any;
    if (response.code === 200 && response.data) {
      if (response.data) {
        Object.assign(captchaConfig.value, response.data);
      }
    }
  } catch (error) {
    console.error("获取演示账号配置失败:", error);
  }
};

// 获取演示账号配置
const fetchDemoAccountConfig = async () => {
  try {
    const response = (await getDemoAccountConfig()) as any;
    if (response.code === 200 && response.data) {
      if (response.data) {
        Object.assign(demoAccountConfig.value, response.data);
      }
    }
  } catch (error) {
    console.error("获取演示账号配置失败:", error);
  }
};

// 获取微信登录配置
const fetchWeChatLoginConfig = async () => {
  try {
    const response = (await getWeChatLoginConfig()) as any;
    if (response.code === 200 && response.data) {
      if (response.data) {
        wechatLoginConfig.value.enabled = response.data.enabled || false;
      }
    }
  } catch (error) {
    console.error("获取微信登录配置失败:", error);
  }
};

// 获取logo
const fetchLogo = async () => {
  try {
    const response = (await getWebsiteConfig()) as any;
    if (response.code === 200 && response.data?.logo) {
      logoUrl.value = response.data.logo;
    }
  } catch (error) {
    console.error("获取logo失败:", error);
  }
};

// 处理logo加载错误
const handleLogoError = () => {
  logoUrl.value = ""; // 清空URL，使用默认logo
};

// 发送验证码成功后，设置本地存储
const setCooldown = (phone: string, seconds: number) => {
  const expire = Date.now() + seconds * 1000;
  localStorage.setItem(`sms_cooldown_${phone}`, expire.toString());
};

// 封装获取二维码的函数
const fetchQrCode = async () => {
  try {
    // console.log("开始获取新二维码...");
    const qrRes = await getQrcodeUrl();
    // console.log("获取二维码响应:", qrRes.data);
    qrcodeUrl.value = qrRes.data.qrcodeUrl;
    loginToken.value = qrRes.data.loginToken;
    qrcodeStatus.value = "pending";
    // console.log("新二维码loginToken:", loginToken.value);

    // 清除旧定时器
    if (qrTimer) clearTimeout(qrTimer);
    if (qrCodePollingTimer.value) clearInterval(qrCodePollingTimer.value);
    if ((window as any).scannedTimer) {
      clearTimeout((window as any).scannedTimer);
      (window as any).scannedTimer = null;
    }

    // 开始轮询二维码状态
    startQrCodePolling();

    // 4分钟后自动刷新（后端5分钟有效期，提前刷新更安全）
    qrTimer = setTimeout(() => {
      console.log("二维码即将过期，自动刷新...");
      fetchQrCode();
    }, 4 * 60 * 1000);
  } catch (error) {
    console.error("获取二维码失败:", error);
    qrcodeStatus.value = "failed";
  }
};

// 开始二维码状态轮询
const startQrCodePolling = () => {
  if (!loginToken.value) return;

  // console.log("开始轮询二维码状态，loginToken:", loginToken.value);

  qrCodePollingTimer.value = setInterval(async () => {
    try {
      // 检查用户是否已登录，如果已登录则停止轮询
      if (userStore.isLogin()) {
        console.log("用户已登录，停止二维码轮询");
        clearInterval(qrCodePollingTimer.value);
        qrCodePollingTimer.value = null;
        return;
      }

      // console.log("轮询二维码状态，当前loginToken:", loginToken.value);
      const statusRes = await checkQrCodeStatus(loginToken.value);
      // console.log("二维码状态响应:", statusRes.data);
      const { status, userId } = statusRes.data;

      if (status === "confirmed") {
        qrcodeStatus.value = "confirmed";
        clearInterval(qrCodePollingTimer.value);
        qrCodePollingTimer.value = null;

        // 获取用户信息并登录
        if (userId) {
          console.log("二维码登录成功，userId:", userId);
          // 直接调用现有的登录成功处理逻辑
          try {
            // 调用后端接口获取用户信息和token
            const loginRes = (await qrCodeLogin(userId)) as any;

            if (loginRes.code === 200 && loginRes.data) {
              // 使用与密码登录相同的处理逻辑
              const { data } = loginRes;
              storage.set("accessToken", data.accessToken);
              storage.set("refreshToken", data.refreshToken);
              userStore.setUserInfo(data.userInfo);
              router.push({ path: "/", query: { date: Date.now() } });
              emit("clickChild", false);

              ElMessage.success(t("login.scanSuccess"));
            } else {
              ElMessage.error(t("login.loginFailedRetry"));
            }
          } catch (error) {
            console.error("QR login failed:", error);
            ElMessage.error(t("login.loginFailedRetry"));
          }
        }
      } else if (status === "scanned") {
        qrcodeStatus.value = "scanned";
        // 如果等待确认超过30秒，自动刷新二维码
        if (!(window as any).scannedTimer) {
          (window as any).scannedTimer = setTimeout(() => {
            console.log("等待确认超时，自动刷新二维码...");
            fetchQrCode();
            (window as any).scannedTimer = null;
          }, 30000); // 30秒后自动刷新
        }
      } else if (status === "expired") {
        qrcodeStatus.value = "expired";
        clearInterval(qrCodePollingTimer.value);
        qrCodePollingTimer.value = null;
        // 自动刷新二维码
        setTimeout(fetchQrCode, 1000);
      } else if (status === "pending") {
        qrcodeStatus.value = "pending";
      }
    } catch (error) {
      console.error("检查二维码状态失败:", error);
    }
  }, 3000); // 每3秒轮询一次（降低服务器压力）
};

// 页面加载或手机号切换时，检查本地存储
const checkCooldown = () => {
  const phone = userLogin.value.phone;
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    countdown.value = 0;
    return;
  }
  const expire = localStorage.getItem(`sms_cooldown_${phone}`);
  if (expire) {
    const left = Math.ceil((parseInt(expire) - Date.now()) / 1000);
    if (left > 0) {
      countdown.value = left;
      startCountdown();
      return;
    }
  }
  countdown.value = 0;
};

// 启动倒计时
const startCountdown = () => {
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
};

// 计算属性
const isFormValid = computed(() => {
  const phoneValid = /^1[3-9]\d{9}$/.test(userLogin.value.phone);
  if (!phoneValid || !agreedToTerms.value) return false;
  return isPasswordLogin.value ? !!userLogin.value.password : !!userLogin.value.code;
});

const canSendCode = computed(() => {
  return countdown.value === 0 && /^1[3-9]\d{9}$/.test(userLogin.value.phone);
});

// 验证码输入验证
const validateCodeInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const value = target.value;
  // 只允许数字输入
  if (!/^\d*$/.test(value)) {
    // 如果不是纯数字，清除非数字字符
    userLogin.value.code = value.replace(/\D/g, "");
  }
};

// 发送验证码
const handleSendCode = async () => {
  // 如果不能发送验证码,直接返回
  if (!canSendCode.value) {
    if (!userLogin.value.phone) {
      ElMessage.warning(t("login.phoneRequired"));
    } else if (!/^1[3-9]\d{9}$/.test(userLogin.value.phone)) {
      ElMessage.warning(t("login.phoneCorrect"));
    }
    return;
  }
  // 如果验证码功能未启用，直接发送验证码
  if (!captchaConfig.value.isEnabled) {
    await sendCode();
    return;
  }
  // 弹出滑块
  verifyRef.value?.show();
};

// 滑块验证通过后
const capctchaCheckSuccess = async (data: any) => {
  sliderVerification.value = typeof data === "string" ? data : data.captchaVerification;
  sliderVerified.value = true;
  if (isLoginAction.value) {
    // 登录流程
    await doLogin();
    isLoginAction.value = false;
  } else {
    // 发验证码流程
    try {
      await sendCode();
    } catch (error: any) {
      ElMessage.error(error.message || t("login.codeSendFailed"));
    }
  }
};

// 在登录成功后重置验证码状态
const doLogin = async () => {
  try {
    const loginData = { ...userLogin.value };
    if (captchaConfig.value.isEnabled && sliderVerification.value) {
      loginData.captchaVerification = sliderVerification.value;
    }
    const res = await (isPasswordLogin.value ? login(loginData) : loginByCode(loginData)) as any;
    if (res.code === 517) {
      ElMessage.success(
        res.msg || t("login.registerPendingAudit")
      );
      sliderVerification.value = "";
      sliderVerified.value = false;
      return;
    }
    const { data } = res;
    storage.set("accessToken", data.accessToken);
    storage.set("refreshToken", data.refreshToken);
    userStore.setUserInfo(data.userInfo);

    // 登录成功后停止二维码轮询
    if (qrCodePollingTimer.value) {
      clearInterval(qrCodePollingTimer.value);
      qrCodePollingTimer.value = null;
    }

    router.push({ path: "/", query: { date: Date.now() } });
    emit("clickChild", false);

    // 重置验证码状态
    sliderVerification.value = "";
    sliderVerified.value = false;
  } catch (error: any) {
    let msg = t("login.loginFailed");
    if (error.msg) {
      msg = error.msg;
    } else if (error.message) {
      msg = error.message;
    }
    ElMessage.error(msg);
  }
};

// 方法
const emit = defineEmits(["clickChild"]);

const close = () => {
  emit("clickChild", false);
};

const clearPhone = () => {
  userLogin.value.phone = "";
};

const validatePhone = () => {
  const phoneNumber = userLogin.value.phone;
  if (!phoneNumber) {
    errorMessage.value = t("login.phoneRequired");
    return false;
  }
  if (!/^1[3-9]\d{9}$/.test(phoneNumber)) {
    errorMessage.value = t("login.phoneInvalid");
    return false;
  }
  errorMessage.value = "";
  return true;
};

const sendCode = async () => {
  if (!canSendCode.value) return;
  try {
    // 如果验证码功能未启用，不传递验证码参数
    const verification = captchaConfig.value.isEnabled ? sliderVerification.value : "";
    await sendVerifyCode(userLogin.value.phone, verification);
    setCooldown(userLogin.value.phone, 60); // 60秒冷却
    countdown.value = 60;
    startCountdown();
    ElMessage.success(t("login.codeSent"));
  } catch (error: any) {
    let msg = t("login.codeSendFailed");
    if (error.msg) {
      msg = error.msg;
    } else if (error.message) {
      msg = error.message;
    }
    ElMessage.error(msg);
  }
};

const handleSubmit = async () => {
  if (!agreedToTerms.value) {
    ElMessage.warning(t("login.agreeFirst"));
    return;
  }
  if (!validatePhone()) return;

  if (isPasswordLogin.value) {
    // 密码登录，始终弹滑块
    if (captchaConfig.value.isEnabled) {
      isLoginAction.value = true;
      verifyRef.value?.show();
    } else {
      await doLogin();
    }
  } else {
    // 验证码登录
    if (captchaConfig.value.isEnabled) {
      if (sliderVerified.value && sliderVerification.value) {
        // 已通过滑块校验，直接登录
        await doLogin();
      } else {
        // 未校验，弹滑块
        isLoginAction.value = true;
        verifyRef.value?.show();
      }
    } else {
      // 验证码功能未启用，直接登录
      await doLogin();
    }
  }
};

const handleDemoLogin = async () => {
  // 使用配置中的演示账号信息
  if (demoAccountConfig.value.username && demoAccountConfig.value.password) {
    userLogin.value.phone = demoAccountConfig.value.username;
    userLogin.value.password = demoAccountConfig.value.password;
  } else {
    // 默认演示账号
    userLogin.value.phone = "17611776901";
    userLogin.value.password = "123456";
  }
  agreedToTerms.value = true;
  // 延迟一下让UI更新
  await nextTick();
  // 触发登录
  try {
    const res = await demoLogin(userLogin.value);
    const { data } = res;
    storage.set("accessToken", data.accessToken);
    storage.set("refreshToken", data.refreshToken);
    userStore.setUserInfo(data.userInfo);

    // 登录成功后停止二维码轮询
    if (qrCodePollingTimer.value) {
      clearInterval(qrCodePollingTimer.value);
      qrCodePollingTimer.value = null;
    }

    router.push({ path: "/", query: { date: Date.now() } });
    emit("clickChild", false);
    // ElMessage.success(t("login.demoLoginSuccess"));
  } catch (error: any) {
    console.error(error);
    const msg = error?.response?.data?.message || error?.message;
    if (msg && !msg.includes('demoLogin')) {
      ElMessage.error(msg);
    }
  }
};

// 处理微信登录
const handleWeChatLogin = async () => {
  // 自动勾选同意用户协议和隐私政策
  agreedToTerms.value = true;

  try {
    // 构建后端回调地址（需要在微信开放平台配置此地址）
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || window.location.origin;
    // 回调地址必须是完整的URL，且与微信开放平台配置的一致
    const redirectUri = `${apiBaseUrl}/web/auth/wechat/callback`;

    // 获取微信授权URL
    const res = (await generateWeChatAuthUrl(redirectUri)) as any;
    if (res.code === 200 && res.data) {
      // 跳转到微信授权页面
      window.location.href = res.data;
    } else {
      ElMessage.error(t("login.wechatAuthFailed"));
    }
  } catch (error: any) {
    console.error("WeChat login failed:", error);
    ElMessage.error(error?.message || t("login.wechatLoginFailed"));
  }
};

// 处理微信回调（通过临时token）
const handleWeChatCallback = async (tempToken: string) => {
  try {
    ElMessage.info(t("login.loggingIn"));
    const res = (await getWeChatLoginInfo(tempToken)) as any;
    if (res.code === 200 && res.data) {
      storage.set("accessToken", res.data.accessToken);
      storage.set("refreshToken", res.data.refreshToken);
      userStore.setUserInfo(res.data.userInfo);

      // 清除URL中的参数
      const url = new URL(window.location.href);
      url.searchParams.delete("wechat_login_token");
      window.history.replaceState({}, "", url.toString());

      router.push({ path: "/", query: { date: Date.now() } });
      emit("clickChild", false);
      ElMessage.success(t("login.wechatLoginSuccess"));
    } else {
      ElMessage.error(res.message || t("login.loginFailed"));
    }
  } catch (error: any) {
    console.error("WeChat callback failed:", error);
    ElMessage.error(error?.message || t("login.wechatLoginFailed"));
  }
};

const isFormFilled = computed(() => {
  const phoneValid = /^1[3-9]\d{9}$/.test(userLogin.value.phone);
  if (!phoneValid) return false;
  if (isPasswordLogin.value) {
    return !!userLogin.value.password;
  } else {
    return !!userLogin.value.code;
  }
});

const needAgreementTip = computed(() => {
  return isFormFilled.value && !agreedToTerms.value;
});

onMounted(async () => {
  // 检查是否是微信回调（通过临时token）
  const urlParams = new URLSearchParams(window.location.search);
  const tempToken = urlParams.get("wechat_login_token");
  const wechatError = urlParams.get("wechat_login_error");

  if (tempToken) {
    // 处理微信回调
    await handleWeChatCallback(tempToken);
    return;
  }

  if (wechatError) {
    const message = urlParams.get("message") || t("login.loginFailed");
    ElMessage.error(decodeURIComponent(message));
    // 清除URL参数
    const url = new URL(window.location.href);
    url.searchParams.delete("wechat_login_error");
    url.searchParams.delete("message");
    window.history.replaceState({}, "", url.toString());
  }

  // 获取验证码配置
  await fetchCaptchaConfig();
  // 获取演示账号配置
  await fetchDemoAccountConfig();
  // 获取微信登录配置
  await fetchWeChatLoginConfig();
  // 获取验证码类型
  const typeRes = await getCaptchaType();
  captchaType.value = typeRes.data;
  // 获取logo
  await fetchLogo();
  checkCooldown();

  // 延迟获取二维码，确保页面完全加载
  setTimeout(async () => {
    // console.log("页面加载完成，开始获取二维码...");
    await fetchQrCode();
  }, 500);
});

// 页面卸载时清理定时器
onUnmounted(() => {
  if (qrTimer) clearTimeout(qrTimer);
  if (qrCodePollingTimer.value) clearInterval(qrCodePollingTimer.value);
});

watch(
  () => userLogin.value.phone,
  () => {
    checkCooldown();
  }
);
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.reds-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;

  .reds-mask {
    position: absolute;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.25);
    z-index: -1;
  }

  .login-container {
    position: relative;
    display: flex;
    width: 800px;
    height: 480px;
    background: @white;
    border-radius: 16px;
    box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.08), 0 1px 4px 0 rgba(0, 0, 0, 0.04);
    transition: all 0.2s;

    .close-button {
      position: absolute;
      right: 20px;
      top: 20px;
      padding: 8px;
      cursor: pointer;
      color: @text-primary;
      transition: all 0.2s;
      border-radius: 50%;

      &:hover {
        background-color: rgba(0, 0, 0, 0.04);
      }
    }

    // 左侧区域
    .left {
      width: 400px;
      display: flex;
      align-items: center;
      flex-direction: column;
      border-right: 1px solid @border-color;
      padding: 40px 0;

      .login-reason {
        padding: 12px 24px;
        background: rgba(61, 138, 245, 0.1);
        color: @primary-color;
        border-radius: 999px;
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 30px;
      }

      .hongshu {
        color: @primary-color;
        font-weight: bold;
      }

      .logo {
        // margin-top: 1px;
        // margin-bottom: 1px;
        width: 100px;
        height: 40px;
        object-fit: contain;
        user-select: none;
      }

      .qrcode {
        position: relative;
        margin-top: 30px;
        padding: 14px;
        border: 1.5px solid @border-color;
        border-radius: 12px;
        background: @white;
        transition: all 0.2s;

        &:hover {
          border-color: @primary-color;
        }

        .qrcode-img {
          width: 160px;
          height: 160px;
        }

        .status {
          position: absolute;
          bottom: 8px;
          left: 50%;
          transform: translateX(-50%);
          padding: 4px 12px;
          background: rgba(0, 0, 0, 0.7);
          border-radius: 16px;
          font-size: 12px;
          color: white;
          font-weight: 500;
          z-index: 10;
        }
      }

      .tip {
        margin-top: 16px;
        color: @text-primary;
        font-weight: 600;
        font-size: 14px;
      }

      .wechat-login-left {
        margin-top: 15px;
        display: flex;
        justify-content: center;

        .wechat-btn-left {
          padding: 5px 10px;
          background: #07ce67;
          border: none;
          border-radius: 20px;
          color: @white;
          font-size: 14px;
          cursor: pointer;
          transition: all 0.2s;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          font-weight: 500;

          .wechat-icon-left {
            width: 20px;
            height: 20px;
            object-fit: contain;
          }

          &:hover {
            opacity: 0.9;
            background: #06ad56;
          }

          &:active {
            transform: scale(0.95);
          }
        }
      }
    }

    // 右侧区域
    .right {
      flex: 1;
      display: flex;
      align-items: center;
      flex-direction: column;
      padding: 48px 48px 32px;

      .title {
        font-size: 24px;
        color: @text-primary;
        font-weight: 600;
        margin-bottom: 20px;
      }

      .input-container {
        width: 100%;
        max-width: 320px;

        .login-type-switch {
          display: flex;
          justify-content: center;
          align-items: center;
          margin-bottom: 22px;
          margin-top: 2px;
          .login-type-tab {
            font-size: 16px;
            color: #bbb;
            font-weight: 500;
            margin: 0 14px;
            padding-bottom: 6px;
            cursor: pointer;
            transition: color 0.2s;
            position: relative;
          }
          .login-type-tab.active {
            color: @primary-color;
            font-weight: bold;
          }
          .login-type-tab.active::after {
            content: "";
            display: block;
            width: 100%;
            height: 3px;
            background: @primary-color;
            border-radius: 2px;
            position: absolute;
            left: 0;
            bottom: -2px;
          }
        }

        form {
          display: flex;
          flex-direction: column;
          // gap: 16px;
        }

        // 手机号输入框
        .phone {
          position: relative;
          width: 100%;

          .country-code {
            position: absolute;
            left: 16px;
            top: 50%;
            transform: translateY(-50%);
            color: @text-primary;
            font-weight: 500;
            padding-right: 12px;
            border-right: 1px solid @border-color;
          }

          input {
            width: 100%;
            height: 48px;
            padding: 0 16px 0 72px;
            border: 1px solid @border-color;
            border-radius: 999px;
            font-size: 16px;
            color: @text-primary;
            transition: all 0.2s;

            &:focus {
              border-color: @primary-color;
              box-shadow: 0 0 0 2px rgba(21, 121, 235, 0.1);
            }
          }

          .clear {
            position: absolute;
            right: 16px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            color: @text-secondary;

            &:hover {
              color: @text-primary;
            }
          }
        }

        // 验证码/密码输入框
        .auth-code {
          display: flex;
          align-items: center;
          gap: 12px;

          input {
            flex: 1;
            height: 48px;
            padding: 0 16px;
            border: 1px solid @border-color;
            border-radius: 999px;
            font-size: 16px;
            color: @text-primary;
            transition: all 0.2s;

            &:focus {
              border-color: @primary-color;
              box-shadow: 0 0 0 2px rgba(21, 121, 235, 0.1);
            }
          }

          .code-button {
            white-space: nowrap;
            color: @primary-color;
            font-size: 14px;
            cursor: pointer;
            user-select: none;
            transition: all 0.2s;

            &.disabled {
              opacity: 0.5;
              cursor: not-allowed;
              color: @text-secondary;
            }

            &:not(.disabled):hover {
              opacity: 0.8;
            }
          }
        }

        // 错误提示
        .err-msg {
          min-height: 20px;
          color: @primary-color;
          font-size: 14px;
          text-align: center;
        }

        .submit[disabled] {
          cursor: not-allowed;
        }
        // 登录按钮
        .submit {
          width: 60%;
          height: 48px;
          margin: 0 auto; // 水平居中
          background: @primary-color;
          color: @white;
          border: none;
          border-radius: 999px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            opacity: 0.9;
          }

          &:active {
            transform: scale(0.98);
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
        }
      }

      // 协议区域
      .agreements {
        margin-top: 16px;
        padding-left: 24px;
        position: relative;
        font-size: 12px;
        color: @text-secondary;
        line-height: 1.5;

        .agreement-label {
          position: absolute;
          left: 0;
          top: 2px;
          cursor: pointer;

          .agreement-checkbox {
            position: absolute;
            opacity: 0;
          }

          .checkmark {
            display: block;
            width: 16px;
            height: 16px;
            border: 1px solid @border-color;
            border-radius: 4px;
            transition: all 0.2s;

            &:after {
              content: "";
              position: absolute;
              display: none;
              left: 5px;
              top: 1px;
              width: 4px;
              height: 8px;
              border: solid @white;
              border-width: 0 2px 2px 0;
              transform: rotate(45deg);
            }
          }

          .agreement-checkbox:checked ~ .checkmark {
            background: @primary-color;
            border-color: @primary-color;

            &:after {
              display: block;
            }
          }
        }

        .links {
          color: #13386c;
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }
        }
      }

      .oauth-tip:after,
      .oauth-tip:before {
        display: block;
        width: 50px;
        height: 1px;
        content: "";
        background-color: rgba(0, 0, 0, 0.08);
      }

      // 其他登录方式
      .oauth-tip {
        margin-top: 20px;
        display: flex;
        align-items: center;
        color: @text-secondary;
        font-size: 12px;

        &:before,
        &:after {
          content: "";
          flex: 1;
          height: 1px;
          background: @border-color;
        }

        span {
          padding: 0 16px;
        }
      }

      .login {
        margin-top: 24px;

        .login-common {
          padding: 8px 32px;
          border: 1px solid @border-color;
          border-radius: 999px;
          color: @text-primary;
          font-size: 14px;
          cursor: pointer;
          transition: all 0.2s;
          display: flex;
          align-items: center;
          gap: 8px;

          &:hover {
            border-color: @primary-color;
            color: @primary-color;
          }

          svg {
            fill: currentColor;
          }
        }
      }

      .demo-login {
        margin-top: 12px;
        text-align: center;

        .demo-btn {
          padding: 5px 12px;
          background: @white;
          border: 1px solid @primary-color;
          border-radius: 20px;
          color: @primary-color;
          font-size: 13px;
          cursor: pointer;
          transition: all 0.2s;
          display: inline-flex;
          align-items: center;
          font-weight: bold;

          &:hover {
            opacity: 0.8;
            background: rgba(6, 69, 204, 0.05);
          }

          &:active {
            transform: scale(0.95);
          }
        }
      }
    }
  }
}

// 深色模式覆盖 - 与全局 style.css 中的 .dark-mode 保持一致
.dark-mode {
  .login-modal {
    .login-container {
      background: #1f1f1f;
      box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.6), 0 1px 4px 0 rgba(0, 0, 0, 0.4);

      .close-button {
        color: rgba(255, 255, 255, 0.8);

        &:hover {
          background-color: rgba(255, 255, 255, 0.06);
        }
      }

      .left {
        border-right-color: rgba(255, 255, 255, 0.12);

        .login-reason {
          background: rgba(61, 138, 245, 0.12);
          color: @primary-color;
        }

        .qrcode {
          border-color: rgba(255, 255, 255, 0.12);
          background: #242424;

          .status {
            background: rgba(0, 0, 0, 0.8);
            color: white;
          }
        }

        .tip {
          color: rgba(255, 255, 255, 0.8);
        }

        .wechat-login-left {
          .wechat-btn-left {
            background: #07c160;

            &:hover {
              background: #06ad56;
            }
          }
        }
      }

      .right {
        .title {
          color: rgba(255, 255, 255, 0.9);
        }

        .login-type-switch .login-type-tab {
          color: #777;
        }

        .phone {
          .country-code {
            color: rgba(255, 255, 255, 0.9);
            border-right-color: rgba(255, 255, 255, 0.12);
          }
          input {
            border-color: rgba(255, 255, 255, 0.12);
            background: #242424;
            color: rgba(255, 255, 255, 0.9);
          }
          .clear {
            color: rgba(255, 255, 255, 0.5);
          }
        }

        .auth-code {
          input {
            border-color: rgba(255, 255, 255, 0.12);
            background: #242424;
            color: rgba(255, 255, 255, 0.9);
          }
          .code-button {
            color: @primary-color;
          }
        }

        .err-msg {
          color: @primary-color;
        }

        .agreements {
          color: rgba(255, 255, 255, 0.6);

          .checkmark {
            border-color: rgba(255, 255, 255, 0.12);
          }
        }

        .oauth-tip {
          color: rgba(255, 255, 255, 0.6);
          &:before,
          &:after {
            background: rgba(255, 255, 255, 0.12);
          }
        }

        .login .login-common {
          background: transparent;
          border-color: rgba(255, 255, 255, 0.12);
          color: rgba(255, 255, 255, 0.85);
        }

        .submit {
          background: @primary-color;
          color: #fff;
        }
      }

      .demo-login .demo-btn {
        background: transparent;
        border-color: @primary-color;
        color: @primary-color;
      }
    }
  }
}

// 响应式设计
@media screen and (max-width: 860px) {
  .reds-modal .login-container {
    width: 90%;
    max-width: 400px;
    height: auto;
    flex-direction: column;

    .left {
      display: none !important;
    }

    .right {
      padding: 24px 20px;

      .title {
        font-size: 20px;
        margin-bottom: 24px;
      }
    }
  }
}
</style>
