<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb
      :items="[$t('menu.im.message'), $t('menu.web.customerService')]"
    ></Breadcrumb>
    <a-card
      class="general-card r-nav-card"
      :body-style="{ padding: '15px' }"
      :header-style="{ padding: '15px' }"
    >
      <a-row :gutter="16" style="height: calc(100vh - 200px)">
        <!-- 左侧：用户列表 -->
        <a-col :span="6" style="border-right: 1px solid var(--color-border-2)">
          <div
            style="
              padding: 16px;
              border-bottom: 1px solid var(--color-border-2);
            "
          >
            <a-input
              v-model="searchKeyword"
              :placeholder="$t('biz.searchUserPlaceholder')"
              allow-clear
              @input="handleSearch"
            >
              <template #prefix>
                <icon-search />
              </template>
            </a-input>
          </div>
          <div style="overflow-y: auto; height: calc(100% - 60px)">
            <div
              v-for="user in filteredUserList"
              :key="user.uid"
              :class="['user-item', { active: selectedUserId === user.uid }]"
              @click="selectUser(user)"
            >
              <a-avatar :size="40" style="margin-right: 12px">
                <img
                  v-if="user.avatar"
                  :src="user.avatar"
                  :alt="$t('biz.avatar')"
                />
                <icon-user v-else />
              </a-avatar>
              <div style="flex: 1; min-width: 0">
                <div style="font-weight: 500; margin-bottom: 4px">
                  {{ user.username || $t('biz.unknownUser') }}
                  <a-badge
                    v-if="(user.count ?? 0) > 0"
                    :count="user.count ?? 0"
                    :max-count="99"
                    style="margin-left: 8px"
                  />
                </div>
                <div
                  style="
                    font-size: 12px;
                    color: var(--color-text-3);
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  "
                >
                  {{ formatLastMessage(user) }}
                </div>
              </div>
              <div
                style="
                  font-size: 12px;
                  color: var(--color-text-3);
                  margin-left: 8px;
                "
              >
                {{ formatTime(user.timestamp ?? 0) }}
              </div>
            </div>
            <a-empty
              v-if="filteredUserList.length === 0"
              :description="$t('biz.noUsersYet')"
            />
          </div>
        </a-col>

        <!-- 右侧：聊天界面 -->
        <a-col
          :span="18"
          style="
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
          "
        >
          <div v-if="selectedUser" class="chat-wrapper">
            <!-- 聊天头部 -->
            <div class="chat-header">
              <a-avatar :size="44" class="header-avatar">
                <img
                  v-if="selectedUser.avatar"
                  :src="selectedUser.avatar"
                  :alt="$t('biz.avatar')"
                />
                <icon-user v-else />
              </a-avatar>
              <div class="header-info">
                <div class="header-name">
                  {{ selectedUser.username || $t('biz.unknownUser') }}
                  <a-badge
                    v-if="(selectedUser.count ?? 0) > 0"
                    :count="selectedUser.count ?? 0"
                    :max-count="99"
                    style="margin-left: 8px"
                  />
                </div>
              </div>
              <a-space class="header-actions">
                <!-- AI客服开关 -->
                <a-space style="margin-right: 16px">
                  <span style="font-size: 13px; color: var(--color-text-2)">{{
                    $t('biz.aiAutoReplyLabel')
                  }}</span>
                  <a-switch
                    v-model="aiCustomerServiceEnabled"
                    :loading="aiSwitchLoading"
                    size="small"
                    @change="handleAISwitchChange"
                  />
                  <span style="font-size: 12px; color: var(--color-text-3)">
                    {{
                      aiCustomerServiceEnabled
                        ? $t('biz.enabled')
                        : $t('biz.disabled')
                    }}
                  </span>
                </a-space>
                <a-button type="text" size="small" @click="refreshMessages">
                  <template #icon><icon-refresh /></template>
                </a-button>
              </a-space>
            </div>

            <!-- 消息列表 -->
            <div ref="messageListRef" class="message-list-container">
              <a-spin :loading="loading" style="width: 100%; min-height: 100%">
                <div
                  v-if="messageList.length === 0 && !loading"
                  class="empty-message"
                >
                  <a-empty :description="$t('biz.noMessagesYet')" />
                </div>
                <template v-else>
                  <div
                    v-for="(message, index) in messageList"
                    :key="message.id || index"
                    :class="[
                      'message-item',
                      {
                        'message-right': String(message.sendUid) === '0',
                      },
                    ]"
                  >
                    <a-avatar :size="36" class="message-avatar">
                      <img
                        v-if="String(message.sendUid) === '0'"
                        src="@/assets/customer-service-avatar.png"
                        :alt="$t('biz.altCustomerService')"
                      />
                      <img
                        v-else-if="selectedUser.avatar"
                        :src="selectedUser.avatar"
                        :alt="$t('biz.altUser')"
                      />
                      <icon-user v-else />
                    </a-avatar>
                    <div class="message-content">
                      <div
                        class="message-bubble"
                        :class="{
                          'bubble-right': String(message.sendUid) === '0',
                        }"
                        @contextmenu.prevent="
                          handleMessageContextMenu($event, message)
                        "
                      >
                        <div
                          v-if="
                            message.msgType === 1 &&
                            !isImageUrl(message.content)
                          "
                          class="message-text"
                        >
                          {{ message.content }}
                        </div>
                        <div
                          v-else-if="
                            message.msgType === 2 ||
                            (message.msgType === 1 &&
                              isImageUrl(message.content))
                          "
                          class="message-image"
                        >
                          <img
                            :src="message.content"
                            class="message-img"
                            :alt="$t('biz.altImageMessage')"
                            @click="previewImage(message.content)"
                            @error="handleImageError"
                          />
                        </div>
                        <button
                          v-else-if="message.msgType === 3"
                          type="button"
                          class="message-audio"
                          @click="handlePlayAudio(message)"
                        >
                          <icon-sound
                            v-if="playingAudioId !== message.id"
                            style="margin-right: 6px"
                          />
                          <icon-pause v-else style="margin-right: 6px" />
                          <span>
                            {{
                              message.audioTime
                                ? `${message.audioTime}”`
                                : $t('biz.voiceShort')
                            }}
                          </span>
                        </button>
                        <div class="message-time">{{
                          formatMessageTime(message.timestamp)
                        }}</div>
                      </div>
                    </div>
                  </div>
                </template>
              </a-spin>
            </div>

            <!-- 输入框 -->
            <div class="input-container">
              <div class="input-toolbar">
                <a-space>
                  <a-button size="small" @click="handleSendImage">
                    <template #icon><icon-image /></template>
                    {{ $t('biz.imageShort') }}
                  </a-button>
                  <a-button size="small" @click="handleSendFile">
                    <template #icon><icon-file /></template>
                    {{ $t('biz.fileShort') }}
                  </a-button>
                </a-space>
                <a-button
                  type="primary"
                  :loading="sending"
                  :disabled="!inputMessage.trim()"
                  @click="handleSendMessage"
                >
                  <template #icon><icon-send /></template>
                  {{ $t('biz.send') }}
                </a-button>
              </div>
              <a-textarea
                v-model="inputMessage"
                :placeholder="$t('biz.csMessageInputPlaceholder')"
                class="message-input"
                :auto-size="{ minRows: 2, maxRows: 5 }"
                @keydown.enter.prevent="handleEnterKey"
              />
            </div>
          </div>
          <a-empty
            v-else
            :description="$t('biz.selectUserToStartChat')"
            style="
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: center;
            "
          />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, watch, h } from 'vue';
import { useI18n } from 'vue-i18n';
import { currentLocale } from '@/utils/common';
import {
  getCustomerServiceUserList,
  getCustomerServiceChatRecord,
  sendCustomerServiceMessage,
  clearCustomerServiceMessageCount,
  getAICustomerServiceStatus,
  updateAICustomerServiceStatus,
  type CustomerServiceRecord,
  type CustomerServiceMessage,
} from '@/api/web/customerService';
import { Message, Modal } from '@arco-design/web-vue';
import {
  IconSearch,
  IconUser,
  IconSend,
  IconImage,
  IconFile,
  IconRefresh,
  IconSound,
  IconPause,
} from '@arco-design/web-vue/es/icon';
import { useWebSocket } from '@/utils/websocket';
import { useUserStore } from '@/store';

const { t } = useI18n();
const searchKeyword = ref('');
const userList = ref<CustomerServiceRecord[]>([]);
const filteredUserList = ref<CustomerServiceRecord[]>([]);
const selectedUserId = ref<string>('');
const selectedUser = ref<CustomerServiceRecord | null>(null);
const messageList = ref<CustomerServiceMessage[]>([]);
const inputMessage = ref('');
const loading = ref(false);
const sending = ref(false);
const messageListRef = ref<HTMLElement>();
const aiCustomerServiceEnabled = ref(false);
const aiSwitchLoading = ref(false);

// 判断是否为图片URL
const isImageUrl = (content?: string): boolean => {
  if (!content || typeof content !== 'string') return false;
  // 检查是否是图片URL（匹配扩展名或前缀）
  const imagePattern = /\.(jpg|jpeg|png|gif|webp|bmp)/i;
  const imagePrefix = /^https?:\/\/image\.|^https?:\/\/.*\/image\//i;
  // 检查是否包含img标签
  const hasImgTag = content.includes('<img') || content.includes('<IMG');
  return imagePattern.test(content) || imagePrefix.test(content) || hasImgTag;
};

// 格式化用户列表中的最后一条消息
const formatLastMessage = (record: CustomerServiceRecord | any) => {
  const content = record?.content ?? record?.lastMessage ?? '';
  if (!content) return t('biz.lastMessageEmpty');

  // 如果有类型字段，先按类型判断
  const msgType = record?.msgType ?? record?.lastMsgType;
  if (msgType === 2) {
    return t('biz.bracketImage');
  }
  if (msgType === 3) {
    return t('biz.bracketVoice');
  }

  // 没有类型时，通过内容简单判断
  if (isImageUrl(content)) {
    return t('biz.bracketImage');
  }
  const audioPattern = /\.(mp3|wav|m4a|aac|ogg)$/i;
  if (audioPattern.test(content)) {
    return t('biz.bracketVoice');
  }

  if (content.length > 30) {
    return `${content.substring(0, 30)}...`;
  }
  return content;
};

// 格式化时间
const formatTime = (timestamp: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  if (days === 1) {
    return t('biz.yesterday');
  }
  if (days < 7) {
    return t('biz.daysAgo', { n: days });
  }
  return date.toLocaleDateString(currentLocale());
};

// 格式化消息时间
const formatMessageTime = (timestamp?: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// 搜索用户
const handleSearch = () => {
  if (!searchKeyword.value) {
    filteredUserList.value = userList.value;
    return;
  }
  const keyword = searchKeyword.value.toLowerCase();
  filteredUserList.value = userList.value.filter(
    (user) =>
      user.username?.toLowerCase().includes(keyword) ||
      user.uid?.toLowerCase().includes(keyword)
  );
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
};

// 语音播放控制
const currentAudio = ref<HTMLAudioElement | null>(null);
const playingAudioId = ref<string | number | null>(null);

const handlePlayAudio = (message: CustomerServiceRecord) => {
  const url = message.content;
  if (!url) return;

  // 再次点击同一条，切换播放/暂停
  if (playingAudioId.value === message.id && currentAudio.value) {
    if (currentAudio.value.paused) {
      currentAudio.value.play();
    } else {
      currentAudio.value.pause();
    }
    return;
  }

  // 停止上一条
  if (currentAudio.value) {
    currentAudio.value.pause();
    currentAudio.value = null;
    playingAudioId.value = null;
  }

  const audio = new Audio(url);
  currentAudio.value = audio;
  playingAudioId.value = message.id ?? null;

  audio.onended = () => {
    playingAudioId.value = null;
    currentAudio.value = null;
  };
  audio.onerror = () => {
    playingAudioId.value = null;
    currentAudio.value = null;
  };

  audio.play();
};

// 加载AI客服开关状态
const loadAICustomerServiceStatus = async () => {
  try {
    const res = await getAICustomerServiceStatus();
    if (res.data !== undefined) {
      aiCustomerServiceEnabled.value = res.data;
    }
  } catch (error) {
    console.error('加载AI客服开关状态失败', error);
  }
};

// 处理AI客服开关切换
const handleAISwitchChange = async (value: string | number | boolean) => {
  const enabled = Boolean(value);
  aiSwitchLoading.value = true;
  try {
    await updateAICustomerServiceStatus(enabled);
    Message.success(enabled ? t('biz.aiCsEnabled') : t('biz.aiCsDisabled'));
    aiCustomerServiceEnabled.value = enabled;
  } catch (error) {
    Message.error(t('biz.aiCsSwitchUpdateFailed'));
    // 恢复原状态
    aiCustomerServiceEnabled.value = !enabled;
  } finally {
    aiSwitchLoading.value = false;
  }
};

// 加载消息列表
const loadMessages = async () => {
  if (!selectedUserId.value) return;

  loading.value = true;
  try {
    const res = await getCustomerServiceChatRecord(
      1,
      100,
      selectedUserId.value
    );
    if (res.data?.records) {
      messageList.value = res.data.records;
    }
  } catch (error) {
    Message.error(t('biz.loadMessagesFailed'));
  } finally {
    loading.value = false;
  }
};

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedUserId.value) {
    return;
  }

  sending.value = true;
  try {
    const message = {
      sendUid: '0', // 客服ID使用0
      acceptUid: selectedUserId.value,
      content: inputMessage.value.trim(),
      msgType: 1, // 文本消息
      chatType: 0, // 私聊
    };

    await sendCustomerServiceMessage(message);

    // 添加到消息列表
    const newMessage: CustomerServiceMessage = {
      id: Date.now().toString(),
      sendUid: '0', // 客服ID使用0
      acceptUid: selectedUserId.value,
      content: inputMessage.value.trim(),
      msgType: 1,
      chatType: 0,
      timestamp: Date.now(),
    };
    messageList.value.push(newMessage);

    inputMessage.value = '';
    await nextTick();
    scrollToBottom();
  } catch (error) {
    Message.error(t('biz.sendMessageFailed'));
  } finally {
    sending.value = false;
  }
};

// 处理回车键
const handleEnterKey = (e: KeyboardEvent) => {
  if (e.shiftKey) {
    return; // Shift+Enter 换行
  }
  handleSendMessage();
};

// 发送图片
const handleSendImage = () => {
  Message.info(t('biz.imageFeatureTodo'));
};

// 发送文件
const handleSendFile = () => {
  Message.info(t('biz.fileFeatureTodo'));
};

// 刷新消息
const refreshMessages = async () => {
  if (!selectedUserId.value) return;
  await loadMessages();
  scrollToBottom();
  Message.success(t('biz.messagesRefreshed'));
};

// 预览图片
const previewImage = (src?: string) => {
  if (!src) return;
  Modal.info({
    title: t('biz.imagePreviewTitle'),
    content: () => {
      return h('img', {
        src,
        style: {
          width: '100%',
          maxHeight: '70vh',
          objectFit: 'contain',
        },
      });
    },
    footer: false,
    width: '80%',
  });
};

// 图片加载错误处理
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  if (img) {
    img.style.display = 'none';
    console.error('图片加载失败:', img.src);
  }
};

// 消息右键菜单
const handleMessageContextMenu = (
  e: MouseEvent,
  message: CustomerServiceMessage
) => {
  // 可以在这里实现复制等功能
  if (
    message.msgType === 1 &&
    message.content &&
    !isImageUrl(message.content)
  ) {
    navigator.clipboard.writeText(message.content).then(() => {
      Message.success(t('biz.copiedToClipboard'));
    });
  }
};

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await getCustomerServiceUserList({});
    if (res.data) {
      // 调试：打印数据，确认count字段是否存在
      console.log('客服用户列表数据:', res.data);
      res.data.forEach((user: CustomerServiceRecord) => {
        console.log(
          `用户 ${user.username} (ID: ${user.uid}) 未读数:`,
          user.count
        );
      });
      userList.value = res.data;
      filteredUserList.value = res.data;
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
    Message.error(t('biz.loadUserListFailed'));
  }
};

// 监听消息列表变化，自动滚动
watch(
  messageList,
  () => {
    scrollToBottom();
  },
  { deep: true }
);

// 处理新消息
const handleNewMessage = (message: any) => {
  console.log('处理新消息:', message);

  // 检查是否是客服相关的消息（acceptUid === '0' 表示发送给客服）
  const isCustomerServiceMessage =
    message.acceptUid === '0' || message.sendUid === '0';

  if (!isCustomerServiceMessage) {
    // 不是客服消息，忽略
    return;
  }

  // 获取用户ID（发送方或接收方）
  const userId = message.sendUid === '0' ? message.acceptUid : message.sendUid;

  // 如果当前正在查看该用户的消息，则添加到消息列表
  if (selectedUserId.value === userId) {
    const newMessage: CustomerServiceMessage = {
      id: message.id || Date.now().toString(),
      sendUid: message.sendUid,
      acceptUid: message.acceptUid,
      content: message.content,
      msgType: message.msgType || 1,
      chatType: message.chatType || 0,
      timestamp: message.timestamp || Date.now(),
    };
    messageList.value.push(newMessage);
    nextTick(() => {
      scrollToBottom();
    });
  }

  // 更新用户列表中的未读数
  if (userId && userId !== '0') {
    const userIndex = userList.value.findIndex((u) => u.uid === userId);
    if (userIndex !== -1) {
      // 如果是用户发送给客服的消息，增加未读数
      if (message.sendUid !== '0' && message.acceptUid === '0') {
        userList.value[userIndex].count =
          (userList.value[userIndex].count || 0) + 1;
      }
      // 更新最后一条消息
      userList.value[userIndex].content = message.content;
      userList.value[userIndex].timestamp = message.timestamp || Date.now();
      // 更新过滤后的列表
      handleSearch();
    } else {
      // 新用户，需要重新加载用户列表
      loadUserList();
    }
  }
};

// 从 WebSocket 更新用户列表
const updateUserListFromWebSocket = (newUserList: CustomerServiceRecord[]) => {
  // 合并新用户列表和现有列表
  const existingUserMap = new Map(userList.value.map((u) => [u.uid, u]));
  newUserList.forEach((newUser) => {
    const existingUser = existingUserMap.get(newUser.uid);
    if (existingUser) {
      // 更新现有用户的信息
      Object.assign(existingUser, {
        content: newUser.content || existingUser.content,
        timestamp: newUser.timestamp || existingUser.timestamp,
        count: newUser.count ?? existingUser.count ?? 0,
      });
    } else {
      // 添加新用户
      userList.value.push(newUser);
    }
  });
  // 更新过滤后的列表
  handleSearch();
};

// WebSocket 连接
const userStore = useUserStore();
const adminUserId = String(
  (userStore.user as any)?.userId ||
    (userStore.user as any)?.user?.userId ||
    '0'
);
const { connect, disconnect, onMessage, offMessage, isConnected } =
  useWebSocket(adminUserId);

// 初始化 WebSocket
const initWebSocket = async () => {
  try {
    await connect();
    console.log('WebSocket 连接成功');

    // 监听新消息（msgType === 1）
    onMessage(1, (message: any) => {
      console.log('收到新消息:', message);
      handleNewMessage(message);
    });

    // 监听用户列表更新（msgType === 5）
    onMessage(5, (message: any) => {
      console.log('收到用户列表更新:', message);
      if (message.content && Array.isArray(message.content)) {
        // 更新用户列表
        updateUserListFromWebSocket(message.content);
      }
    });

    // 监听未读数更新（msgType === 0）
    onMessage(0, (message: any) => {
      console.log('收到未读数更新:', message);
      // 重新加载用户列表以获取最新的未读数
      loadUserList();
    });
  } catch (error) {
    console.error('WebSocket 连接失败:', error);
  }
};

// 选择用户时，清除该用户的未读数（标记为已读）
const selectUser = async (user: CustomerServiceRecord) => {
  selectedUserId.value = user.uid || '';
  selectedUser.value = user;
  messageList.value = [];
  inputMessage.value = '';

  // 清除该用户的未读数（调用后端接口）
  if (user.count && user.count > 0 && user.uid) {
    try {
      await clearCustomerServiceMessageCount(user.uid);
      // 更新本地数据
      const userIndex = userList.value.findIndex((u) => u.uid === user.uid);
      if (userIndex !== -1) {
        userList.value[userIndex].count = 0;
      }
      const filteredIndex = filteredUserList.value.findIndex(
        (u) => u.uid === user.uid
      );
      if (filteredIndex !== -1) {
        filteredUserList.value[filteredIndex].count = 0;
      }
    } catch (error) {
      console.error('清除未读消息数失败:', error);
    }
  }

  await loadMessages();
  scrollToBottom();
};

onMounted(() => {
  loadUserList();
  loadAICustomerServiceStatus(); // 加载AI客服开关状态
  initWebSocket();
});

onUnmounted(() => {
  // 移除消息监听
  offMessage(1);
  offMessage(5);
  offMessage(0);
  disconnect();
  if (currentAudio.value) {
    currentAudio.value.pause();
    currentAudio.value = null;
  }
});
</script>

<style lang="less" scoped>
.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 4px;
  margin: 2px 8px;

  &:hover {
    background-color: var(--color-fill-2);
  }

  &.active {
    background-color: var(--color-primary-light-1);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-2);
  display: flex;
  align-items: center;
  background: var(--color-bg-1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
  flex-shrink: 0;

  .header-avatar {
    margin-right: 12px;
    flex-shrink: 0;
  }

  .header-info {
    flex: 1;
    min-width: 0;

    .header-name {
      font-weight: 500;
      font-size: 16px;
      margin-bottom: 4px;
    }

    .header-meta {
      font-size: 12px;
      color: var(--color-text-3);
      display: flex;
      align-items: center;
    }
  }

  .header-actions {
    flex-shrink: 0;
  }
}

.message-list-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  background: var(--color-fill-1);
  position: relative;
  min-height: 0;
  /* 关键：flex: 1 让这个容器占据剩余空间，min-height: 0 确保可以正确计算 */

  .empty-message {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 300px;
    height: 100%;
  }
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
  animation: fadeIn 0.3s ease-in;

  &.message-right {
    flex-direction: row-reverse;

    .message-avatar {
      margin-left: 12px;
      margin-right: 0;
    }
  }
}

.message-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.message-content {
  max-width: 65%;
  min-width: 100px;
}

.message-bubble {
  padding: 10px 14px;
  background: var(--color-bg-2);
  border-radius: 12px;
  word-wrap: break-word;
  position: relative;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  &.bubble-right {
    background: #3d8af5;
    color: #fff;

    .message-text {
      color: #fff;
    }

    .message-time {
      color: rgba(255, 255, 255, 0.85);
    }
  }
}

.message-text {
  margin-bottom: 4px;
  line-height: 1.5;
  word-break: break-word;
  color: var(--color-text-1);
}

.message-time {
  font-size: 11px;
  color: var(--color-text-3);
  margin-top: 6px;
  opacity: 0.7;
}

.message-image {
  margin-bottom: 4px;
  display: inline-block;
  max-width: 100%;

  .message-img {
    max-width: 200px;
    max-height: 200px;
    width: auto;
    height: auto;
    border-radius: 8px;
    cursor: pointer;
    object-fit: cover;
    display: block;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.02);
    }

    &:active {
      transform: scale(0.98);
    }
  }
}

.message-audio {
  border: none;
  outline: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding: 8px 14px;
  border-radius: 999px;
  background-color: #5094ee;
  color: #fff;
  font-size: 13px;
  line-height: 1;
  transition: background-color 0.2s, transform 0.1s;

  &:hover {
    background-color: #3c7dff;
  }

  &:active {
    transform: scale(0.97);
  }
}

.input-container {
  padding: 16px 20px;
  border-top: 1px solid var(--color-border-2);
  background: var(--color-bg-1);
  flex-shrink: 0; /* 关键：输入框不参与flex缩放，始终固定在底部 */

  .input-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .message-input {
    :deep(.arco-textarea) {
      border-radius: 8px;
      font-size: 14px;
    }
  }
}

.chat-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-height: 100%;
  overflow: hidden;
  position: relative;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

