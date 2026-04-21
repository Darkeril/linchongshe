<template>
  <div>
    <ul class="message-container">
      <li
        class="message-item"
        v-for="(item, index) in dataList"
        :key="index"
        :class="{ 'system-message': item.isSystem }"
      >
        <a class="user-avatar" :class="{ 'system-avatar': item.isSystem }">
          <div v-if="item.isSystem" class="avatar-item system-notice-avatar" @click.prevent>
            <el-icon :size="26"><BellFilled /></el-icon>
          </div>
          <img v-else class="avatar-item" :src="item.avatar" @click="toUser(item.uid)" />
        </a>
        <div class="main">
          <div class="info">
            <div class="user-info">
              <a :class="{ 'system-username': item.isSystem }">{{ item.username }}</a>
              <div class="interaction-hint">
                <span>{{ item.time }}</span>
              </div>
            </div>

            <div
              class="interaction-content"
              @click="item.isSystem ? viewNoticeDetail() : toChat(item.uid, index)"
              :class="{ 'system-content': item.isSystem }"
            >
              <span>{{
                item.isSystem ? systemMessageLinePreview : formatMessageContent(item.content)
              }}</span>
              <div class="msg-count" v-show="item.count > 0">{{ item.count }}</div>
            </div>
          </div>
        </div>
      </li>
    </ul>

    <el-drawer
      v-model="chatShow"
      direction="rtl"
      size="30%"
      :with-header="false"
      class="chat-drawer"
      @closed="handleDrawerClose"
    >
      <!-- 聊天组件 -->
      <NoteChat
        v-if="chatShow"
        :key="`chat-${acceptUid}`"
        v-model="chatShow"
        :acceptUid="acceptUid"
        class="chat-component animate__animated animate__fadeIn"
        @refresh-list="fetchMessages"
      />
    </el-drawer>

    <!-- 通知公告列表抽屉 -->
    <el-drawer v-model="noticeDrawerShow" direction="rtl" size="30%" :with-header="false" class="notice-drawer">
      <SystemNotificationConfigDrawer
        v-if="noticeDrawerShow"
        v-model="noticeDrawerShow"
        class="notice-list-component animate__animated animate__fadeIn"
      />
    </el-drawer>
  </div>

  <div v-if="dataList.length === 0" class="empty-state">
    <el-empty :description="t('common.noMessage')">
      <!-- <el-button type="primary">去发现更多</el-button> -->
    </el-empty>
  </div>
</template>


<script lang="ts" setup>
import { useImStore } from "@/store/imStore";
import { useUserStore } from "@/store/userStore";
import { onMounted, ref, watchEffect, watch, onUnmounted, computed } from "vue";
import { formateTime, htmlToPlainText } from "@/utils/util";
import { getChatUserList, clearMessageCount } from "@/api/im";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import NoteChat from "@/components/NoteChat.vue";
import SystemNotificationConfigDrawer from "@/components/SystemNotificationConfigDrawer.vue";
import { BellFilled } from "@element-plus/icons-vue";
import { CUSTOMER_SERVICE } from "@/utils/constants";

const router = useRouter();
const imStore = useImStore();
const userStore = useUserStore();
const dataList = ref<Array<any>>([]);
const chatShow = ref(false);
const acceptUid = ref("");
const loading = ref(false);
const noticeDrawerShow = ref(false);
const { t } = useI18n();

// WebSocket 推送的系统通知预览（有值时优先展示；勿把 computed 放进 ref 对象里再赋值，会触发只读代理报错）
const systemPushedPreview = ref<string | null>(null);

// 系统消息对象（字段均为可写普通值，由 watchEffect 同步 i18n 与推送文案）
const systemMessage = ref({
  uid: "system",
  username: "",
  content: "",
  timestamp: Date.now(),
  count: 0,
  isSystem: true,
});

watchEffect(() => {
  systemMessage.value.username = t("message.systemNotice");
  systemMessage.value.content = systemPushedPreview.value ?? t("message.clickToViewNotice");
});

/** 列表行展示：富文本系统通知转纯文本并截断 */
const systemMessageLinePreview = computed(() => {
  const c = systemMessage.value.content;
  const fallback = t("message.clickToViewNotice");
  if (!c || c === fallback) return c || fallback;
  const plain = htmlToPlainText(c);
  if (!plain) return fallback;
  return plain.length > 40 ? `${plain.slice(0, 40)}...` : plain;
});

// 格式化消息内容（与uniapp端formatLastMessage逻辑一致，不使用msgType字段）
const formatMessageContent = (content: string) => {
  // 只通过内容判断，不使用msgType字段（与uniapp端保持一致）
  if (typeof content === "string") {
    // 语音 URL：统一显示为 [语音]
    if (content.match(/\.(mp3|m4a|aac|wav|ogg)(\?|$)/i)) {
      return t("common.voice");
    }
    // 检查是否是图片URL（匹配扩展名或前缀）
    if (
      content.match(/\.(jpg|jpeg|png|gif|webp|bmp)/i) ||
      content.startsWith("https://image.") ||
      content.startsWith("http://image.")
    ) {
      return t("common.image");
    }
    // 检查是否是视频URL（匹配扩展名或前缀）
    if (
      content.match(/\.(mp4|mov|avi|webm)/i) ||
      content.startsWith("https://video.") ||
      content.startsWith("http://video.")
    ) {
      return t("common.video");
    }
    // 检查是否包含img标签
    if (content.includes("<img")) {
      return t("common.image");
    }
    // 文本消息，如果太长则截断
    if (content.length > 20) {
      return content.slice(0, 20) + "...";
    }
  }

  return content;
};

const toUser = (uid: string) => {
  router.push({ name: "user", query: { uid: uid } });
};

watchEffect(() => {
  // 去重：使用 Map 根据 uid 去重，保留最新的记录
  const uniqueMap = new Map();
  imStore.noteChatList.forEach((item) => {
    const uid = item.uid;
    // 过滤掉客服消息（客服ID是 "0"）
    if (uid === CUSTOMER_SERVICE.USER_ID) {
      return;
    }

    if (!uniqueMap.has(uid)) {
      // 新记录，直接添加
      uniqueMap.set(uid, { ...item, time: formateTime(item.timestamp) });
    } else {
      // 已存在，比较时间戳，保留最新的
      const existing = uniqueMap.get(uid);
      if (item.timestamp > existing.timestamp) {
        uniqueMap.set(uid, { ...item, time: formateTime(item.timestamp) });
      }
    }
  });

  // 构建去重后的列表
  dataList.value = [systemMessage.value]; // 确保系统消息始终在第一位
  const uniqueList = Array.from(uniqueMap.values());

  // 按时间戳排序（最新的在前）
  uniqueList.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0));

  // 添加到 dataList
  uniqueList.forEach((item) => {
    dataList.value.push(item);
  });

  // 计算总未读数
  let totalCount = 0;
  uniqueList.forEach((item) => {
    totalCount += item.count || 0;
  });

  // 更新计数（创建新对象，避免直接修改）
  imStore.setCountMessage({ chatCount: totalCount });
});

const toChat = async (uid: string, index: number) => {
  const countMessage = imStore.countMessage;
  try {
    await clearMessageCount(uid, 3, "");
    const chatCount = dataList.value[index].count;
    countMessage.chatCount -= chatCount;
    dataList.value[index].count = 0;
    imStore.setCountMessage(countMessage);

    acceptUid.value = uid;
    chatShow.value = true; // 打开抽屉会触发子组件的数据加载
  } catch (error) {
    console.error("打开聊天失败:", error);
  }
};

// 跳转到客服聊天
const toCustomerService = () => {
  acceptUid.value = CUSTOMER_SERVICE.USER_ID;
  chatShow.value = true;
};

const handleDrawerClose = async () => {
  await fetchMessages();
};

const fetchMessages = async () => {
  try {
    loading.value = true;
    const res = await getChatUserList();
    imStore.setNoteChatList(res.data);
  } catch (error) {
    console.error("获取聊天消息失败:", error);
  } finally {
    loading.value = false;
  }
};

// 监听 imStore.message 的变化（通过 index.vue 的 WebSocket 接收）
// 直接更新消息列表中的对应项，而不是刷新整个列表
watch(
  () => imStore.message,
  (newMessage) => {
    if (!newMessage) return;

    // 处理系统通知消息
    if (newMessage.msgType === 99 && newMessage.sendUid === "system") {
      const text = typeof newMessage.content === "string" ? newMessage.content.trim() : "";
      const img = newMessage.imageUrl;
      if (text) {
        systemPushedPreview.value = text;
      } else if (img) {
        systemPushedPreview.value = t("common.image");
      } else {
        systemPushedPreview.value = null;
      }
      systemMessage.value.timestamp = newMessage.timestamp || Date.now();
      return;
    }

    // 处理普通聊天消息（msgType === 1）
    if (newMessage.msgType === 1) {
      const senderUid = newMessage.sendUid;

      // 过滤掉客服消息（客服ID是 "0"）
      if (senderUid === CUSTOMER_SERVICE.USER_ID) {
        return;
      }

      const currentUser = userStore.getUserInfo();
      const currentUserId = String(currentUser?.id || "");

      // 如果是自己发送的消息，不需要更新未读数
      const isSelfMessage = senderUid === currentUserId;

      // 查找消息列表中是否存在该发送者
      const index = dataList.value.findIndex((item: any) => item.uid === senderUid);

      if (index > 0) {
        // 已存在的会话，更新最新消息
        dataList.value[index].content = newMessage.content;
        dataList.value[index].timestamp = newMessage.timestamp || Date.now();
        dataList.value[index].time = formateTime(newMessage.timestamp || Date.now());

        // 如果是接收到的消息（不是自己发送的），增加未读数
        if (!isSelfMessage) {
          dataList.value[index].count = (dataList.value[index].count || 0) + 1;
          // 更新总未读数
          const countMessage = imStore.countMessage;
          countMessage.chatCount = (countMessage.chatCount || 0) + 1;
          imStore.setCountMessage(countMessage);
        }

        // 将该会话移到第二位（系统消息后面）
        if (index > 1) {
          const item = dataList.value.splice(index, 1)[0];
          dataList.value.splice(1, 0, item);
        }
      } else if (index === -1) {
        // 新会话，需要从 imStore.noteChatList 中获取用户信息
        const chatUser = imStore.noteChatList.find((user: any) => user.uid === senderUid);

        if (chatUser) {
          // 添加到系统消息后面
          const newItem = {
            uid: senderUid,
            username: chatUser.username || t("user.unknown"),
            avatar: chatUser.avatar || "",
            content: newMessage.content,
            timestamp: newMessage.timestamp || Date.now(),
            time: formateTime(newMessage.timestamp || Date.now()),
            count: isSelfMessage ? 0 : 1,
          };

          dataList.value.splice(1, 0, newItem);

          // 更新总未读数
          if (!isSelfMessage) {
            const countMessage = imStore.countMessage;
            countMessage.chatCount = (countMessage.chatCount || 0) + 1;
            imStore.setCountMessage(countMessage);
          }
        }
        // 如果找不到用户信息，不添加也不刷新（可能是历史消息，不应该显示）
        // 移除延迟刷新逻辑，避免添加不应该显示的对话
      }
    }
  },
  { deep: true }
);

// 查看系统通知配置列表（打开侧边抽屉，与「通知公告」分离）
const viewNoticeDetail = () => {
  noticeDrawerShow.value = true;
};

onMounted(() => {
  fetchMessages();

  // 监听来自父组件的打开客服聊天事件
  const handleOpenCustomerService = () => {
    toCustomerService();
  };
  window.addEventListener("openCustomerService", handleOpenCustomerService);

  // 保存事件处理函数引用，以便在卸载时移除
  (window as any).__noteMessageCustomerServiceHandler = handleOpenCustomerService;
});

onUnmounted(() => {
  // 清理事件监听
  const handler = (window as any).__noteMessageCustomerServiceHandler;
  if (handler) {
    window.removeEventListener("openCustomerService", handler);
    delete (window as any).__noteMessageCustomerServiceHandler;
  }
});
</script>

<style lang="less" scoped>
.message-container {
  width: 40rem;

  .message-item {
    display: flex;
    flex-direction: row;
    padding-top: 24px;

    .user-avatar {
      margin-right: 24px;
      flex-shrink: 0;

      .avatar-item {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 100%;
        // border: 1px solid rgba(0, 0, 0, 0.08);
        object-fit: cover;
      }

      /* 与 UniApp 端系统消息一致：浅黄底 + 金黄铃铛 */
      .system-notice-avatar {
        cursor: default;
        flex-shrink: 0;
        color: #ffb400;
        background: #fff9e6;
        border-color: rgba(255, 180, 0, 0.35);
        box-sizing: border-box;
      }
    }

    .main {
      flex-grow: 1;
      flex-shrink: 1;
      display: flex;
      flex-direction: row;
      padding-bottom: 12px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.08);

      .info {
        flex-grow: 1;
        flex-shrink: 1;

        .user-info {
          display: flex;
          flex-direction: row;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;

          a {
            color: #333;
            font-size: 16px;
            font-weight: 600;
          }

          .interaction-hint {
            font-size: 12px;
            color: rgba(51, 51, 51, 0.6);
          }
        }

        .interaction-content {
          display: flex;
          font-size: 14px;
          color: #333;
          line-height: 140%;
          cursor: pointer;
          justify-content: space-between;
          align-items: center;

          .msg-count {
            width: 20px;
            height: 20px;
            line-height: 20px;
            font-size: 13px;
            color: #fff;
            background-color: red;
            text-align: center;
            border-radius: 100%;
          }

          span {
            // 添加样式让消息类型标识更明显
            &:has([图片], [表情], [语音], [视频], [文件]) {
              color: #666;
              font-style: italic;
            }
          }
        }
      }
    }
  }

  .system-message {
    // background-color: rgba(0, 0, 0, 0.02);

    .user-info {
      .system-username {
        color: #333;
      }
    }

    .system-content {
      cursor: default;
      color: #666;
    }
  }
}

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  text-align: center;

  :deep(.el-empty) {
    padding: 40px 0;
  }
}
</style>
