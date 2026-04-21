<template>
  <div class="ai-chat-container">
    <!-- 顶部导航栏 -->
    <div class="ai-chat-header">
      <div class="header-left">
        <el-button type="text" @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          {{ $t("common.back") }}
        </el-button>
        <h2 class="title">{{ $t("aiChat.title") }}</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openFullScreen" class="fullscreen-btn">
          <el-icon><FullScreen /></el-icon>
          {{ $t("common.fullscreen") }}
        </el-button>
      </div>
    </div>

    <!-- iframe容器 -->
    <div class="iframe-container" ref="iframeContainer">
      <iframe
        ref="chatIframe"
        :src="chatUrl"
        frameborder="0"
        class="chat-iframe"
        @load="onIframeLoad"
        @error="onIframeError"
      ></iframe>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-loading :text="loadingText" background="rgba(255, 255, 255, 0.8)" />
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="error-overlay">
      <el-result icon="error" :title="$t('aiChat.loadFailed')" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="retryLoad">{{ $t("aiChat.retry") }}</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { ElMessage } from "element-plus";
import { ArrowLeft, FullScreen } from "@element-plus/icons-vue";

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const chatIframe = ref<HTMLIFrameElement>();
const iframeContainer = ref<HTMLElement>();
const loading = ref(true);
const error = ref(false);
const errorMessage = ref("");
const loadingText = computed(() => t("aiChat.loading"));

// AI聊天服务配置
const AI_CHAT_CONFIG = {
  // 开发环境
  dev: "http://localhost:1002",
  // 生产环境配置
  production: {
    "hongshu.website": "https://hongshu.website/chat",
    "www.hongshu.website": "https://hongshu.website/chat",
    // 可以添加更多域名配置
  } as { [key: string]: string },
};

// 计算属性
const chatUrl = computed(() => {
  const token = userStore.getToken();
  const userInfo = userStore.getUserInfo();
  let baseUrl = "";

  if (import.meta.env.DEV) {
    // 开发环境
    baseUrl = AI_CHAT_CONFIG.dev;
  } else {
    // 生产环境
    const currentHost = window.location.host;
    const protocol = window.location.protocol;

    // 检查是否有特定域名配置
    if (AI_CHAT_CONFIG.production[currentHost]) {
      baseUrl = AI_CHAT_CONFIG.production[currentHost];
    } else if (currentHost.includes("localhost")) {
      // 本地测试环境
      baseUrl = AI_CHAT_CONFIG.dev;
    } else {
      // 默认配置：假设项目2在同一服务器不同端口
      baseUrl = `${protocol}//${currentHost.replace(/:\d+$/, "")}:1002`;
    }
  }

  console.log("AI聊天服务地址:", baseUrl);
  console.log("用户信息:", userInfo);
  console.log("用户手机号:", userInfo?.phone);
  console.log("Token:", token);

  if (token && userInfo?.phone) {
    // 如果已登录，传递手机号和Token实现自动登录
    // 跳转到根路径，让路由守卫处理
    const url = `${baseUrl}/?phone=${encodeURIComponent(userInfo.phone)}&accessToken=${encodeURIComponent(String(token))}`;
    console.log("跳转URL:", url);
    return url;
  } else {
    // 未登录，显示登录页面
    console.log("未登录或缺少手机号，跳转到登录页");
    return `${baseUrl}/`;
  }
});

// 方法
const goBack = () => {
  router.back();
};

const openFullScreen = () => {
  if (chatIframe.value) {
    chatIframe.value.requestFullscreen?.();
  }
};

const onIframeLoad = () => {
  loading.value = false;
  error.value = false;
  console.log("AI助手加载完成");
};

const onIframeError = () => {
  loading.value = false;
  error.value = true;
  errorMessage.value = t("aiChat.connectError");
  ElMessage.error(t("aiChat.loadFailed"));
};

const retryLoad = () => {
  error.value = false;
  loading.value = true;
  if (chatIframe.value) {
    chatIframe.value.src = chatUrl.value;
  }
};

// 监听iframe消息（用于双向通信）
const handleMessage = (event: MessageEvent) => {
  // 验证消息来源
  let expectedOrigin = "";

  if (import.meta.env.DEV) {
    expectedOrigin = AI_CHAT_CONFIG.dev;
  } else {
    const currentHost = window.location.host;
    const protocol = window.location.protocol;

    if (AI_CHAT_CONFIG.production[currentHost]) {
      expectedOrigin = AI_CHAT_CONFIG.production[currentHost];
    } else if (currentHost.includes("localhost")) {
      expectedOrigin = AI_CHAT_CONFIG.dev;
    } else {
      expectedOrigin = `${protocol}//${currentHost.replace(/:\d+$/, "")}:1002`;
    }
  }

  console.log("期望的消息来源:", expectedOrigin, "实际来源:", event.origin);

  if (event.origin !== expectedOrigin) {
    return;
  }

  const { type, data } = event.data;

  switch (type) {
    case "LOGIN_REQUIRED":
      // AI助手需要登录
      ElMessage.warning(t("aiChat.pleaseLoginFirst"));
      router.push("/login");
      break;
    case "CHAT_ERROR":
      // 聊天错误
      ElMessage.error(data.message || t("aiChat.chatErrorDefault"));
      break;
    case "TOKEN_EXPIRED":
      // Token过期
      ElMessage.error(t("aiChat.loginExpired"));
      userStore.loginOut();
      router.push("/login");
      break;
    default:
      console.log("收到AI助手消息:", event.data);
  }
};

// 生命周期
onMounted(() => {
  // 监听iframe消息
  window.addEventListener("message", handleMessage);
});

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener("message", handleMessage);
});
</script>

<style lang="less" scoped>
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.ai-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .back-btn {
      padding: 8px;
      color: #606266;

      &:hover {
        color: #409eff;
        background: #f0f9ff;
      }
    }

    .title {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .header-right {
    .fullscreen-btn {
      padding: 8px 16px;
    }
  }
}

.iframe-container {
  flex: 1;
  position: relative;
  overflow: hidden;

  .chat-iframe {
    width: 100%;
    height: 100%;
    border: none;
    background: #fff;
  }
}

.loading-overlay,
.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 1001;
}

.error-overlay {
  background: #fff;
}

// 响应式设计
@media (max-width: 768px) {
  .ai-chat-header {
    padding: 12px 16px;

    .header-left {
      gap: 12px;

      .title {
        font-size: 16px;
      }
    }
  }
}

// 全屏模式样式
.ai-chat-container:fullscreen {
  .ai-chat-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 10000;
  }

  .iframe-container {
    margin-top: 60px; // 为header留出空间
  }
}
</style>
