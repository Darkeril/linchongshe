<template>
  <div class="container">
    <div v-if="isLogin">
      <div class="sticky-placeholder">
        <div class="reds-sticky">
          <div class="reds-tabs-list">
            <el-badge :value="chatCount" :max="99" :hidden="chatCount <= 0" class="message-badge mobile-hide">
              <div :class="getTabClass(MessageType.CHAT)">
                <div class="badge-container" @click="toPage(MessageType.CHAT)">
                  <span>{{ $t("message.chat") }}</span>
                </div>
              </div>
            </el-badge>
            <el-badge
              :value="productChatCount"
              :max="99"
              :hidden="productChatCount <= 0"
              class="message-badge mobile-hide"
            >
              <div :class="getTabClass(MessageType.PRODUCT)">
                <div class="badge-container" @click="toPage(MessageType.PRODUCT)">
                  <span>{{ $t("message.productChat") }}</span>
                </div>
              </div>
            </el-badge>
            <el-badge
              :value="countMessage.commentCount"
              :max="99"
              :hidden="countMessage.commentCount == 0"
              class="message-badge"
            >
              <div :class="getTabClass(MessageType.COMMENT)">
                <div class="badge-container" @click="toPage(MessageType.COMMENT)">
                  <span>{{ $t("message.commentAnd") }}@</span>
                </div>
              </div>
            </el-badge>
            <el-badge
              :value="countMessage.likeOrCollectionCount"
              :max="99"
              :hidden="countMessage.likeOrCollectionCount == 0"
              class="message-badge"
            >
              <div :class="getTabClass(MessageType.LIKE)">
                <div class="badge-container" @click="toPage(MessageType.LIKE)">
                  <span>{{ $t("message.like") }}</span>
                </div>
              </div>
            </el-badge>
            <el-badge
              :value="countMessage.followCount"
              :max="99"
              :hidden="countMessage.followCount == 0"
              class="message-badge"
            >
              <div :class="getTabClass(MessageType.FOLLOW)">
                <div class="badge-container" @click="toPage(MessageType.FOLLOW)">
                  <span>{{ $t("message.follow") }}</span>
                </div>
              </div>
            </el-badge>
          </div>
          <div class="divider" style="margin: 16px 32px 0px"></div>
        </div>
      </div>

      <!-- 使用计算属性动态渲染内容组件 -->
      <component :is="currentComponent" v-if="type !== null" @click-main="toMain" />

      <NoteMain
        v-show="mainShow && currentType === 'note'"
        :nid="nid"
        :now-time="new Date()"
        class="animate__animated animate__zoomIn animate__delay-0.5s"
        @click-main="close"
      />

      <ProductMain
        v-show="mainShow && currentType === 'product'"
        :pid="pid"
        :now-time="new Date()"
        class="animate__animated animate__zoomIn animate__delay-0.5s"
        @click-main="close"
      />

      <el-backtop :bottom="80" :right="24">
        <div class="back-top">
          <Top style="width: 1.2em; height: 1.2em" color="rgba(51, 51, 51, 0.8)" />
        </div>
      </el-backtop>

      <!-- 客服悬浮按钮 - 可拖拽（在所有标签页显示） -->
      <div
        ref="customerServiceBtn"
        class="customer-service-float-btn draggable"
        :class="{ dragging: isDragging }"
        :style="{
          left: customerServicePosition.x + 'px',
          top: customerServicePosition.y + 'px',
          transition: isDragging ? 'none' : isInitialized ? 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)' : 'none',
        }"
        @mousedown="handleCustomerServiceMouseDown"
        @click="handleCustomerServiceClick"
        :title="$t('message.contactService')"
      >
        <div class="service-icon-wrapper">
          <svg
            data-icon-id="1768137397561"
            class="icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            p-id="8558"
            width="40"
            height="40"
          >
            <path
              d="M884.165 451.494c-2.494-203.504-168.046-367.753-372.161-367.753s-369.666 164.249-372.165 367.753c-34.163 27.314-56.096 69.25-56.096 116.37 0 82.254 66.697 148.969 148.96 148.969 35.899 0 68.818-12.726 94.534-33.857l-70.18-261.923c-7.949-1.294-16.045-2.155-24.353-2.155-5.474 0-10.855 0.339-16.183 0.926 17.954-147.35 143.268-261.601 295.488-261.601 152.199 0 277.532 114.25 295.481 261.601-5.306-0.583-10.705-0.926-16.18-0.926-8.305 0-16.421 0.863-24.375 2.155l-70.159 261.801c19.847 16.295 43.981 27.559 70.459 31.868-35.138 37.751-79.825 66.511-130.378 82.068l-6.164 6.156c-15.77-28.99-46.122-48.892-81.442-48.892-51.427 0-93.102 41.674-93.102 93.118 0 51.431 41.673 93.104 93.102 93.104 42.211 0 77.419-28.27 88.852-66.758 102.752-25.189 188.829-92.829 238.272-183.482 38.595-26.917 63.899-71.562 63.899-122.175-0.002-47.108-21.955-89.058-56.102-116.371z"
              fill="#ffffff"
              p-id="8559"
              data-spm-anchor-id="a313x.search_index.0.i3.50643a81WDYy3v"
              class="selected"
            ></path>
          </svg>
        </div>
      </div>
    </div>
    <div v-else>
      <el-empty :description="$t('common.notLoggedIn')" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Top } from "@element-plus/icons-vue";
import { computed, onMounted, onUnmounted, ref, watch, reactive } from "vue";
import ProductMessage from "./children/product-message.vue";
import NoteMessage from "./children/note-message.vue";
import LikeCollection from "@/views/message/children/like-collection.vue";
import Follower from "@/views/message/children/follower.vue";
import Comment from "@/views/message/children/comment.vue";
import NoteMain from "@/views/main/main.vue";
import ProductMain from "@/views/idle-main/main.vue";
import { useImStore, MessageCount } from "@/store/imStore";
import { getChatUserList, getProductChatMessages, clearMessageCount } from "@/api/im";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
void t("common.confirm");

// 使用枚举定义消息类型
enum MessageType {
  LIKE = 0,
  COMMENT = 1,
  FOLLOW = 2,
  CHAT = 3,
  PRODUCT = 4,
}

const imStore = useImStore();
const userStore = useUserStore();
const router = useRouter();

// 移动端检测
const isMobile = computed(() => window.innerWidth <= 695);

// 移动端默认显示"评论和@"，PC端默认显示"我的消息"
const type = ref(isMobile.value ? MessageType.COMMENT : MessageType.CHAT);
const nid = ref("");
const pid = ref("");
const currentType = ref<"note" | "product">("note");
const currentUid = ref("");
const mainShow = ref(false);
const isLogin = ref(false);

interface CountMessage {
  chatCount: number;
  productChatCount: number;
  likeOrCollectionCount: number;
  commentCount: number;
  followCount: number;
}

const countMessage = ref<CountMessage>({
  chatCount: 0,
  productChatCount: 0,
  likeOrCollectionCount: 0,
  commentCount: 0,
  followCount: 0,
});

// 计算属性: 获取标签类名
const getTabClass = computed(() => (tabType: MessageType) => ({
  "reds-tab-item": true,
  active: type.value === tabType,
  "tab-item": true,
}));

// 计算属性: 当前显示的组件
const currentComponent = computed(() => {
  switch (type.value) {
    case MessageType.PRODUCT:
      return ProductMessage;
    case MessageType.CHAT:
      return NoteMessage;
    case MessageType.COMMENT:
      return Comment;
    case MessageType.LIKE:
      return LikeCollection;
    case MessageType.FOLLOW:
      return Follower;
    default:
      return null;
  }
});

const toPage = async (messageType: MessageType) => {
  try {
    if (messageType === MessageType.PRODUCT) {
      // 重新获取闲宝消息列表
      const productData = await getProductChatMessages();
      imStore.setProductChatList(productData.data);

      // 计算新的总未读数
      const productTotalCount = productData.data.reduce((sum: any, message: { count: any }) => {
        return sum + (message.count || 0);
      }, 0);

      // 更新未读计数
      imStore.updateProductChatCount(productTotalCount);
    }

    const currentCount = imStore.getMessageCount(messageType);
    if (currentCount > 0) {
      await clearMessageCount(currentUid.value, messageType, pid.value);
      imStore.resetMessageCount(messageType);
    }
    type.value = messageType;
  } catch (error) {
    console.error("切换消息类型失败:", error);
  }
};

const close = () => {
  mainShow.value = false;
  nid.value = "";
  pid.value = "";
};

const toMain = (data: { type: string; id: string }) => {
  if (data.type === "note") {
    nid.value = data.id;
    pid.value = "";
    currentType.value = "note";
  } else if (data.type === "product") {
    pid.value = data.id;
    nid.value = "";
    currentType.value = "product";
  }
  mainShow.value = true;
};

// 添加独立的计算属性
const chatCount = computed(() => Math.max(0, Number(countMessage.value.chatCount)));
const productChatCount = computed(() => Math.max(0, Number(countMessage.value.productChatCount)));
// const commentCount = computed(() => Number(countMessage.value.commentCount) || 0);
// const likeCount = computed(() => Number(countMessage.value.likeOrCollectionCount) || 0);
// const followCount = computed(() => Number(countMessage.value.followCount) || 0);

watch(
  () => imStore.countMessage,
  (newVal: MessageCount) => {
    countMessage.value = {
      chatCount: Math.max(0, Number(newVal.chatCount)),
      productChatCount: Math.max(0, Number(newVal.productChatCount)),
      likeOrCollectionCount: Math.max(0, Number(newVal.likeOrCollectionCount)),
      commentCount: Math.max(0, Number(newVal.commentCount)),
      followCount: Math.max(0, Number(newVal.followCount)),
    };
  },
  { deep: true }
);

// 初始化加载所有消息数据
const initializeMessageCounts = async () => {
  try {
    console.log("开始加载消息数据");
    const [chatData, productData] = await Promise.all([getChatUserList(), getProductChatMessages()]);
    // 计算笔记总未读消息数
    const chatTotalCount = chatData.data.reduce((sum: any, message: { count: any }) => {
      return sum + (message.count || 0);
    }, 0);
    console.log("笔记未读总数:", chatTotalCount);
    // 计算闲宝总未读消息数
    const productTotalCount = productData.data.reduce((sum: any, message: { count: any }) => {
      return sum + (message.count || 0);
    }, 0);
    console.log("闲宝未读总数:", productTotalCount);
    // 更新消息计数
    imStore.updateMessageCounts({
      chatCount: chatTotalCount, // 使用计算得到的笔记未读总数
      productChatCount: productTotalCount, // 使用计算得到的闲宝未读总数
    });

    console.log("更新后的消息计数:", imStore.countMessage);

    // 更新聊天列表
    imStore.setNoteChatList(chatData.data);
    imStore.setProductChatList(productData.data);
  } catch (error) {
    console.error("初始化消息数据失败:", error);
  }
};

onMounted(() => {
  if (isLogin.value) {
    initializeMessageCounts();
  }

  // 初始化客服按钮位置
  isInitialized.value = false;
  setCustomerServiceDefaultPosition();

  // 延迟启用过渡动画
  setTimeout(() => {
    isInitialized.value = true;
  }, 100);

  // 监听窗口大小变化
  window.addEventListener("resize", handleCustomerServiceResize);

  // 检查是否有客服聊天参数
  const route = router.currentRoute.value;
  if (route.query.customerService === "true") {
    // 延迟执行，确保组件已渲染
    setTimeout(() => {
      type.value = MessageType.CHAT;
      // 可以通过事件通知子组件打开客服聊天
    }, 200);
  }
});

onUnmounted(() => {
  // 清理事件监听
  document.removeEventListener("mousemove", handleCustomerServiceMouseMove);
  document.removeEventListener("mouseup", handleCustomerServiceMouseUp);
  window.removeEventListener("resize", handleCustomerServiceResize);
});

const initData = () => {
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    currentUid.value = userStore.getUserInfo().id;
  }
};

initData();

// 客服按钮拖动相关
const customerServiceBtn = ref<HTMLElement | null>(null);
const isDragging = ref(false);
const isClick = ref(true);
const isInitialized = ref(false); // 标记是否已初始化位置，用于控制过渡动画

// 客服按钮位置
const customerServicePosition = reactive({
  x: 0,
  y: 0,
});

// 拖拽相关变量
const dragStart = reactive({
  x: 0,
  y: 0,
  elementX: 0,
  elementY: 0,
});

// 设置客服按钮默认位置
const setCustomerServiceDefaultPosition = () => {
  const rightOffset = isMobile.value ? 20 : 20;
  const bottomOffset = isMobile.value ? 150 : 150;
  customerServicePosition.x = window.innerWidth - 56 - rightOffset;
  customerServicePosition.y = window.innerHeight - 56 - bottomOffset;
};

// 窗口大小变化处理
const handleCustomerServiceResize = () => {
  const btnWidth = 56;
  const btnHeight = 56;
  const maxX = window.innerWidth - btnWidth;
  const maxY = window.innerHeight - btnHeight;

  if (customerServicePosition.x > maxX) {
    customerServicePosition.x = maxX;
  }
  if (customerServicePosition.y > maxY) {
    customerServicePosition.y = maxY;
  }
};

// 客服按钮鼠标按下
const handleCustomerServiceMouseDown = (e: MouseEvent) => {
  e.preventDefault();
  isClick.value = true;
  isDragging.value = true;

  // 记录鼠标起始位置和元素位置
  dragStart.x = e.clientX;
  dragStart.y = e.clientY;
  dragStart.elementX = customerServicePosition.x;
  dragStart.elementY = customerServicePosition.y;

  // 添加全局事件监听
  document.addEventListener("mousemove", handleCustomerServiceMouseMove);
  document.addEventListener("mouseup", handleCustomerServiceMouseUp);
};

// 客服按钮鼠标移动
const handleCustomerServiceMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return;

  // 如果移动距离超过5px，则认为是拖拽而不是点击
  const moveX = Math.abs(e.clientX - dragStart.x);
  const moveY = Math.abs(e.clientY - dragStart.y);
  if (moveX > 5 || moveY > 5) {
    isClick.value = false;
  }

  // 计算新位置
  const deltaX = e.clientX - dragStart.x;
  const deltaY = e.clientY - dragStart.y;

  let newX = dragStart.elementX + deltaX;
  let newY = dragStart.elementY + deltaY;

  // 边界限制
  const btnWidth = 56;
  const btnHeight = 56;
  const maxX = window.innerWidth - btnWidth;
  const maxY = window.innerHeight - btnHeight;

  newX = Math.max(0, Math.min(newX, maxX));
  newY = Math.max(0, Math.min(newY, maxY));

  customerServicePosition.x = newX;
  customerServicePosition.y = newY;
};

// 客服按钮鼠标松开
const handleCustomerServiceMouseUp = () => {
  isDragging.value = false;

  // 移除全局事件监听
  document.removeEventListener("mousemove", handleCustomerServiceMouseMove);
  document.removeEventListener("mouseup", handleCustomerServiceMouseUp);
};

// 客服按钮点击事件
const handleCustomerServiceClick = () => {
  // 只有在没有拖拽的情况下才触发点击
  if (isClick.value) {
    toCustomerService();
  }
};

// 跳转到客服聊天
const toCustomerService = () => {
  // 切换到"我的消息"标签页
  if (type.value !== MessageType.CHAT) {
    type.value = MessageType.CHAT;
  }

  // 使用 nextTick 确保组件已切换，然后通过事件通知子组件打开客服聊天
  setTimeout(() => {
    // 通过全局事件通知 note-message 组件打开客服聊天
    // 使用 window 事件或 Vue 的事件总线
    window.dispatchEvent(new CustomEvent("openCustomerService"));
  }, 200);
};
</script>

<style lang="less" scoped>
// 客服悬浮按钮样式（使用 :deep 或全局样式）
.container {
  flex: 1;
  padding: 0 24px;
  padding-top: 52px;
  width: 67%;
  margin: 0 auto;
  min-height: 100vh;
  box-sizing: border-box;
  position: relative;

  // 移动端优化
  @media screen and (max-width: 695px) {
    width: 100vw;
    max-width: 100%;
    padding: 0 16px;
    padding-top: 64px;
    padding-bottom: 80px;
    min-height: calc(100vh - 60px);
    overflow-x: hidden;
  }

  .sticky-placeholder {
    height: 80px;

    @media screen and (max-width: 695px) {
      height: 48px;
    }
  }

  .reds-sticky {
    top: 72px;
    position: fixed;
    z-index: 1;
    border-bottom: 1px solid #eee;

    @media screen and (max-width: 695px) {
      top: 64px;
    }
    width: 40rem;
    box-sizing: border-box;
    height: 72px;
    padding-top: 16px;
    background: #fff;

    // 移动端优化
    @media screen and (max-width: 695px) {
      width: 100%;
      height: 48px;
      padding-top: 8px;
      display: flex;
      justify-content: flex-start;
      align-items: center;
    }

    .reds-tabs-list {
      justify-content: flex-start;
      display: flex;
      flex-wrap: nowrap;
      position: relative;
      font-size: 16px;
      padding: 0 32px;

      // 移动端优化
      @media screen and (max-width: 695px) {
        padding: 0 16px 0 12px !important;
        font-size: 15px;
        justify-content: flex-start !important;
        width: 100%;
        gap: 16px;
        margin: 0;
      }

      // 移动端隐藏"我的消息"和"闲宝消息"
      .mobile-hide {
        @media screen and (max-width: 695px) {
          display: none !important;
        }
      }

      .reds-tab-item {
        padding: 0px 16px;
        height: 40px;
        cursor: pointer;
        color: rgba(51, 51, 51, 0.8);
        display: flex;
        align-items: center;
        transition: transform 0.3s cubic-bezier(0.2, 0, 0.25, 1);

        // 移动端优化
        @media screen and (max-width: 695px) {
          padding: 0;
          flex: 0 0 auto;
        }

        &.active {
          font-weight: 600;
          color: #333;
          background-color: rgba(0, 0, 0, 0.03);
          border-radius: 20px;

          // 移动端：选中时添加padding
          @media screen and (max-width: 695px) {
            padding: 0 12px;
          }
        }

        .badge-container {
          position: relative;
        }
      }
    }
  }

  .back-top {
    width: 40px;
    height: 40px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 100px;
    color: rgba(51, 51, 51, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
    cursor: pointer;
  }
}

// 客服悬浮按钮样式（全局样式，不在 scoped 中）
.customer-service-float-btn {
  position: fixed;
  width: 60px;
  height: 60px;
  background: #3d8af5;
  border: none;
  border-radius: 50%;
  cursor: move;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px 0 rgba(65, 147, 255, 0.4), 0 2px 8px 0 rgba(0, 0, 0, 0.15);
  z-index: 1000;
  user-select: none;
  will-change: transform, box-shadow;

  // 移动端优化
  @media screen and (max-width: 695px) {
    width: 48px;
    height: 48px;
  }

  &:hover:not(.dragging) {
    transform: translateY(-2px) scale(1.05);
    box-shadow: 0 8px 25px 0 rgba(65, 211, 255, 0.5), 0 4px 12px 0 rgba(0, 0, 0, 0.2);
  }

  &.dragging {
    cursor: grabbing;
    transform: scale(1.08);
    box-shadow: 0 12px 30px 0 rgba(65, 195, 255, 0.6), 0 6px 16px 0 rgba(0, 0, 0, 0.25);
    opacity: 0.95;
    transition: transform 0.1s, box-shadow 0.1s, opacity 0.1s !important;
  }

  &:active:not(.dragging) {
    transform: scale(0.98);
  }

  .service-icon-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 1;
  }

  .service-icon {
    font-size: 24px;
    color: #fff;

    @media screen and (max-width: 695px) {
      font-size: 20px;
    }
  }

  .headset-icon {
    width: 24px;
    height: 24px;
    color: #fff;

    @media screen and (max-width: 695px) {
      width: 20px;
      height: 20px;
    }
  }
}
</style>
