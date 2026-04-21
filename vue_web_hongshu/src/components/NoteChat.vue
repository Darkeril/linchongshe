<template>
  <div class="chat-container">
    <!-- 添加用户信息栏 -->
    <div class="chat-header">
      <div class="user-info">
        <el-avatar :src="acceptUser.avatar" :size="40" />
        <span class="username">{{ acceptUser.username }}</span>
      </div>
    </div>
    <main class="chat-main">
      <div class="chat-record" ref="ChatRef" @scroll="showScroll()">
        <div v-for="(item, index) in dataList" :key="item.id || item.timestamp || `msg-${index}`">
          <div class="message-my-item" v-if="item.acceptUid === acceptUser.id">
            <Loading v-show="item.isLoading" style="width: 0.8em; height: 0.8em; margin-right: 0.5rem" />
            <div class="message-content-wrapper">
              <div class="message-time">{{ item.time }}</div>
              <!-- 自己发送的文字 -->
              <div class="message-my-conent" v-if="item.msgType == 1">{{ item.content }}</div>
              <!-- 自己发送的图片 -->
              <div class="message-content" v-else-if="item.msgType === 2">
                <el-image
                  :src="item.content"
                  :preview-teleported="false"
                  fit="cover"
                  class="message-img"
                  :initial-index="0"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      {{ $t("common.imageLoadFailed") }}
                    </div>
                  </template>
                </el-image>
              </div>
              <!-- 自己发送的语音 -->
              <div
                class="message-my-voice"
                v-else-if="item.msgType === 3"
                @click="playAudio(item)"
              >
                <Microphone
                  class="voice-icon"
                  :class="{ 'voice-playing': currentPlayId === item.id }"
                />
                <span class="voice-duration">{{ (item.audioTime || 1) + '\"' }}</span>
              </div>
            </div>
            <div class="user-avatar">
              <el-avatar :src="currentUser.avatar" />
            </div>
          </div>

          <div class="message-item" v-else>
            <div class="user-avatar">
              <el-avatar :src="acceptUser.avatar" />
            </div>
            <div class="message-content-wrapper">
              <div class="message-time">{{ item.time }}</div>
              <!-- 对方发送的文字 -->
              <div class="message-conent" v-if="item.msgType == 1">{{ item.content }}</div>
              <!-- 对方发送的图片 -->
              <div class="message-content" v-else-if="item.msgType === 2">
                <el-image
                  :src="item.content"
                  :preview-teleported="false"
                  fit="cover"
                  class="message-img"
                  :initial-index="0"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      {{ $t("common.imageLoadFailed") }}
                    </div>
                  </template>
                </el-image>
              </div>
              <!-- 对方发送的语音 -->
              <div
                class="message-voice"
                v-else-if="item.msgType === 3"
                @click="playAudio(item)"
              >
                <Microphone
                  class="voice-icon"
                  :class="{ 'voice-playing': currentPlayId === item.id }"
                />
                <span class="voice-duration">{{ (item.audioTime || 1) + '\"' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="chat-input-wrapper">
        <hr color="#f4f4f4" />
        <div class="chat-input">
          <div class="input-tool">
            <div class="tool-left">
              <PieChart class="icon-item"></PieChart>
              <el-upload :auto-upload="false" :show-file-list="false" :on-change="handleChange">
                <Picture class="icon-item"></Picture>
              </el-upload>
              <!-- 表情选择器 -->
              <el-popover
                v-model:visible="emojiPickerVisible"
                placement="top-start"
                trigger="click"
                :width="300"
                :offset="10"
              >
                <template #reference>
                  <button class="emoji-btn">
                    <span class="btn-content">😀</span>
                  </button>
                </template>
                <div class="emoji-picker">
                  <span v-for="emoji in emojis" :key="emoji" @click="insertEmoji(emoji)" class="emoji-item">
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>
            </div>
            <div class="tool-history">
              <Clock class="icon-item"></Clock>
            </div>
          </div>

          <div class="input-content">
            <p
              id="post-textarea"
              ref="postContent"
              class="post-content"
              contenteditable="true"
              data-tribute="true"
              :placeholder="t('message.chatInputPlaceholder')"
              @keyup.enter="submit"
              @blur="handleBlur"
              @focus="handleFocus"
            ></p>
            <div class="input-btn">
              <div></div>
              <el-button type="primary" round style="width: 5.55rem" @click="submit">{{ $t("common.send") }}</el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script lang="ts" setup>
import { PieChart, Picture, Clock, Loading, Microphone } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { ref, onMounted, watch, nextTick, onUnmounted } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
import { getUserById } from "@/api/user";
import { getAllChatRecord, sendMsg } from "@/api/im";
import { useUserStore } from "@/store/userStore";
import { CUSTOMER_SERVICE } from "@/utils/constants";
import { useImStore } from "@/store/imStore";
import type { UploadProps } from "element-plus";
import { uploadFileMethod, formatDate, getRandomString } from "@/utils/util";
const imStore = useImStore();
const userStore = useUserStore();
const savedRange = ref<Range | null>(null);
const currentPlayId = ref<string | number | null>(null);
let audioPlayer: HTMLAudioElement | null = null;

interface UploadImage {
  id: string;
  file: File; // 存储文件对象
  preview: string; // 预览URL
}

const uploadImages = ref<UploadImage[]>([]);

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  acceptUid: {
    type: String,
    default: "",
  },
  productId: {
    type: String,
    default: "",
  },
});

const ChatRef = ref();
const currentUser = ref<any>({});
const acceptUser = ref<any>({});
const dataList = ref<any>();
const currentPage = ref(1);
const pageSize = 15;
const messageTotal = ref(0);
const postContent = ref(null);

const emojiPickerVisible = ref(false);
const emojis = [
  "😀",
  "😃",
  "😄",
  "😁",
  "😆",
  "😅",
  "😂",
  "🤣",
  "😊",
  "😇",
  "🙂",
  "🙃",
  "😉",
  "😌",
  "😍",
  "🥰",
  "😘",
  "😗",
  "😙",
  "😚",
  "😋",
  "😛",
  "😝",
  "😜",
  "🤪",
  "🤨",
  "🧐",
  "🤓",
  "😎",
  "🤩",
  "🥳",
  "😏",
  "😒",
  "😞",
  "😔",
  "😟",
  "😕",
  "🙁",
  "☹️",
  "😣",
  "😖",
  "😫",
  "😩",
  "🥺",
  "😢",
  "😭",
  "😤",
  "😠",
  "😡",
  "🤬",
  "🤯",
  "😳",
  "🥵",
  "🥶",
  "😱",
  "😨",
  "😰",
  "😥",
  "😓",
  "🤗",
  "🤔",
  "🤭",
  "🤫",
  "🤥",
  "😶",
  "😐",
  "😑",
  "😬",
  "🙄",
  "😯",
  "😦",
  "😧",
  "😮",
  "😲",
  "🥱",
  "😴",
  "🤤",
  "😪",
  "😵",
  "🤐",
  "🥴",
  "🤢",
  "🤮",
  "🤧",
  "😷",
  "🤒",
  "🤕",
  "🤑",
  "🤠",
  "😈",
  "👿",
  "👹",
  "👺",
  "🤡",
  "💩",
  "👻",
  "💀",
  "☠️",
  "👽",
  "👾",
  "🤖",
  "🎃",
  "😺",
  "😸",
  "😹",
  "😻",
  "😼",
  "😽",
  "🙀",
  "😿",
  "😾",
];

// 播放语音
const playAudio = (item: any) => {
  if (!item || !item.content) {
    ElMessage.warning(t("message.voiceEmpty"));
    return;
  }

  const url = decodeURIComponent(item.content);

  // 如果当前已经在播放这条，点击则停止
  if (currentPlayId.value === item.id && audioPlayer) {
    audioPlayer.pause();
    audioPlayer.currentTime = 0;
    currentPlayId.value = null;
    return;
  }

  // 停掉之前的
  if (audioPlayer) {
    audioPlayer.pause();
    audioPlayer.currentTime = 0;
  }

  audioPlayer = new Audio(url);
  currentPlayId.value = item.id;

  audioPlayer.onended = () => {
    currentPlayId.value = null;
  };

  audioPlayer.onerror = () => {
    currentPlayId.value = null;
  };

  audioPlayer
    .play()
    .catch((e) => {
      console.error("音频播放异常", e);
      currentPlayId.value = null;
    });
};

// 处理输入框失去焦点
const handleBlur = () => {
  savedRange.value = saveSelection();
};

// 处理输入框获得焦点
const handleFocus = () => {
  if (savedRange.value) {
    restoreSelection(savedRange.value);
  }
};

// 修改表情选择器的点击处理
const handleEmojiPickerClick = (event: Event) => {
  // 阻止冒泡，防止触发输入框的失焦事件
  event.stopPropagation();
};

const insertEmoji = (emoji: string) => {
  const postTextarea = document.getElementById("post-textarea");
  if (!postTextarea) return;

  // 获取当前选区
  const selection = window.getSelection();
  const range = selection?.getRangeAt(0);

  if (!selection || !range) {
    // 如果没有选区，则在末尾插入
    postTextarea.innerHTML += emoji;
    // 移动光标到最后
    const newRange = document.createRange();
    newRange.selectNodeContents(postTextarea);
    newRange.collapse(false);
    selection?.removeAllRanges();
    selection?.addRange(newRange);
  } else {
    // 如果有选区，在光标位置插入
    const fragment = document.createTextNode(emoji);
    range.deleteContents();
    range.insertNode(fragment);

    // 移动光标到插入的表情后面
    range.setStartAfter(fragment);
    range.setEndAfter(fragment);
    selection.removeAllRanges();
    selection.addRange(range);
  }

  // 保持焦点在输入框内
  postTextarea.focus();

  // 关闭表情选择器
  emojiPickerVisible.value = false;
};

// 添加一个方法来保存光标位置
const saveSelection = () => {
  const selection = window.getSelection();
  if (selection?.rangeCount) {
    return selection.getRangeAt(0);
  }
  return null;
};

// 添加一个方法来恢复光标位置
const restoreSelection = (range: Range | null) => {
  if (range) {
    const selection = window.getSelection();
    selection?.removeAllRanges();
    selection?.addRange(range);
  }
};

const insertMessage = async (message: any) => {
  console.log("📨 Web端新消息添加:", {
    id: message.id,
    sendUid: message.sendUid,
    acceptUid: message.acceptUid,
    timestamp: message.timestamp,
    time: message.timestamp || message.time,
    content: message.content ? message.content.substring(0, 20) : '无内容',
    currentListLength: dataList.value?.length || 0,
  });

  // 新消息应该添加到数组末尾，但需要确保按时间戳排序
  if (!dataList.value) {
    dataList.value = [];
  }

  // 🔧 优化去重逻辑：通过多个维度判断消息是否已存在
  const exists = dataList.value.some((item: any) => {
    // 1. 通过ID判断（最准确）
    if (item.id && message.id && item.id === message.id) {
      return true;
    }
    // 2. 通过时间戳、内容、发送者和接收者判断（处理ID不一致或缺失的情况）
    // 对于没有时间戳的消息，使用内容+发送者+接收者+时间容差来判断
    const timeTolerance = 5000; // 5秒容差
    const timeMatch = item.timestamp === message.timestamp || 
                     (item.timestamp && message.timestamp && 
                      Math.abs(Number(item.timestamp) - Number(message.timestamp)) < timeTolerance) ||
                     (!item.timestamp && !message.timestamp); // 都没有时间戳时，通过其他维度判断
    const contentMatch = item.content === message.content;
    const sendUidMatch = item.sendUid === message.sendUid;
    const acceptUidMatch = item.acceptUid === message.acceptUid;
    
    // 如果内容、发送者、接收者都匹配，且时间戳匹配（或都没有时间戳），则认为是同一条消息
    if (contentMatch && sendUidMatch && acceptUidMatch && timeMatch) {
      return true;
    }
    return false;
  });

  if (exists) {
    console.log('⚠️ 消息已存在，跳过添加:', message.id || message.timestamp);
    return;
  }

  // 确保消息有 timestamp 和 id
  const currentTimestamp = message.timestamp || Date.now();
  const messageId = message.id || `msg-${currentTimestamp}-${Math.random().toString(36).substr(2, 9)}`;
  
  // 添加时间格式化
  const formattedMessage = {
    ...message,
    id: messageId,
    timestamp: currentTimestamp,
    time: message.time || formatDate(currentTimestamp),
  };
  
  console.log('➕ 添加新消息到列表:', formattedMessage);
  
  // 使用 Vue 的响应式方法确保更新
  dataList.value = [...dataList.value, formattedMessage];

  // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
  dataList.value.sort((a: any, b: any) => {
    const timeA = Number(a.timestamp) || 0;
    const timeB = Number(b.timestamp) || 0;
    return timeA - timeB;
  });
  
  // 强制触发响应式更新
  dataList.value = [...dataList.value];

  console.log('✅ 消息已添加，当前列表长度:', dataList.value.length);

  await nextTick();
  // 滚动到最底部（使用 scrollTop 而不是 scrollIntoView，更可靠）
  if (ChatRef.value) {
    ChatRef.value.scrollTop = ChatRef.value.scrollHeight;
    console.log('📜 已滚动到底部');
  }
};

// 选择图片
const handleChange: UploadProps["onChange"] = async (uploadFile) => {
  // 添加文件类型和大小限制
  const isImage = uploadFile.raw?.type.startsWith("image/");
  const isLt2M = uploadFile.size! / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error(t("user.imageUploadImageOnly"));
    return;
  }
  if (!isLt2M) {
    ElMessage.error(t("user.imageUploadMax2m"));
    return;
  }

  try {
    const file = uploadFile.raw!;
    const preview = URL.createObjectURL(file);
    const imageId = getRandomString(8);

    // 只存储文件和预览URL
    uploadImages.value.push({
      id: imageId,
      file: file,
      preview,
    });

    // 插入预览图到编辑器
    document.getElementById(
      "post-textarea"
    )!.innerHTML += `<img data-id="${imageId}" src="${preview}" style="width:3.75rem;height:3.75rem;object-fit: cover;">`;
  } catch (error) {
    ElMessage.error(t("message.imageProcessFailed"));
    console.error(error);
  }
};

// const emit = defineEmits(["refresh-list"]); // 暂时不需要，消息通过 WebSocket 更新

// 🔧 标记：用于跟踪用户自己发送的消息，避免WebSocket推送回来时重复添加
const pendingSentMessages = new Set<string>();

const sendMessage = async (message: any) => {
  try {
    // 🔧 修复：给用户自己发送的消息添加标记，避免WebSocket推送回来时重复添加
    const messageKey = `${message.sendUid}-${message.acceptUid}-${message.content}-${Date.now()}`;
    message._isLocalMessage = true;
    message._messageKey = messageKey;
    pendingSentMessages.add(messageKey);
    
    await insertMessage(message);
    await sendMsg(message);

    // 更新消息状态
    const index = dataList.value.findIndex((item: any) => {
      return item._messageKey === messageKey || 
             (item.id === message.id) || 
             (item.content === message.content && 
              item.sendUid === message.sendUid && 
              item.acceptUid === message.acceptUid &&
              Math.abs((item.timestamp || 0) - (message.timestamp || Date.now())) < 5000);
    });
    if (index !== -1) {
      dataList.value[index].isLoading = false;
      // 移除标记
      if (dataList.value[index]._messageKey) {
        pendingSentMessages.delete(dataList.value[index]._messageKey);
      }
    }
    
    // 发送成功后，不再刷新整个列表，而是通过 WebSocket 消息更新
    // 消息会通过 WebSocket 推送回来，由 note-message.vue 的 watch 处理
  } catch (error) {
    // 更新消息状态为发送失败
    const index = dataList.value.findIndex((item: any) => {
      return item._messageKey === message._messageKey || 
             (item.id === message.id) || 
             (item.content === message.content && 
              item.sendUid === message.sendUid && 
              item.acceptUid === message.acceptUid);
    });
    if (index !== -1) {
      dataList.value[index].isLoading = false;
      dataList.value[index].error = true;
      // 移除标记
      if (dataList.value[index]._messageKey) {
        pendingSentMessages.delete(dataList.value[index]._messageKey);
      }
    }
    throw error;
  }
};

const submit = async () => {
  const textarea = document.getElementById("post-textarea")!;
  const htmlContent = textarea.innerHTML;

  if (!htmlContent.trim()) return;

  try {
    // 1. 处理文字内容 - 添加清理 HTML 标签的逻辑
    const tempDiv = document.createElement("div");
    tempDiv.innerHTML = htmlContent;

    // 获取所有图片元素
    const images = Array.from(tempDiv.getElementsByTagName("img"));

    // 将图片替换为占位符
    images.forEach((img) => {
      img.replaceWith("{{IMG}}");
    });

    // 清理 HTML 标签，保留纯文本
    const cleanText = tempDiv.innerText
      .replace(/\n\s*\n/g, "\n") // 删除多余的空行
      .replace(/^\s+|\s+$/g, ""); // 删除首尾空白

    // 分割文本
    const textParts = cleanText.split("{{IMG}}").filter((text) => text.trim());

    // 2. 发送文字消息
    for (const text of textParts) {
      if (!text.trim()) continue;

      const message = {
        id: getRandomString(12),
        sendUid: currentUser.value.id,
        acceptUid: acceptUser.value.id,
        content: text.trim(),
        msgType: 1,
        chatType: 0,
        timestamp: new Date().getTime(),
        time: formatDate(new Date().getTime()),
        isLoading: true,
      };

      await sendMessage(message);
    }

    // 3. 处理图片 - 先上传再发送
    for (const img of Array.from(images)) {
      const imageId = img.getAttribute("data-id");
      const uploadedImage = uploadImages.value.find((img) => img.id === imageId);

      if (uploadedImage) {
        try {
          // 显示上传加载状态
          const loading = ElMessage.warning({
            message: t("message.imageSending"),
            duration: 3000,
          });

          // 上传图片
          const res = (await uploadFileMethod(uploadedImage.file)) as any;
          loading.close();

          if (res.code === 200) {
            const imageUrl = res.data;

            // 发送图片消息
            const message = {
              id: getRandomString(12),
              sendUid: currentUser.value.id,
              acceptUid: acceptUser.value.id,
              content: imageUrl,
              msgType: 2,
              chatType: 0,
              timestamp: new Date().getTime(),
              time: formatDate(new Date().getTime()),
              isLoading: true,
            };

            await sendMessage(message);
          } else {
            throw new Error(res.data?.msg || t("message.uploadFailed"));
          }
        } catch (error) {
          let msg = t("error.unknown");
          if (error instanceof Error) {
            msg = error.message;
          } else if (typeof error === "string") {
            msg = error;
          }
          ElMessage.error(t("message.imageUploadFailedPrefix") + msg);
          console.error("图片上传失败:", error);
          continue;
        }
      }
    }

    // 4. 清理
    textarea.innerHTML = "";
    // 清理预览URL
    uploadImages.value.forEach((img) => {
      URL.revokeObjectURL(img.preview);
    });
    uploadImages.value = [];
  } catch (error) {
    ElMessage.error(t("message.sendFailed"));
    console.error(error);
  }
};

const isLoadingMore = ref(false);

const showScroll = () => {
  if (!ChatRef.value || isLoadingMore.value) {
    return;
  }
  
  const topval = ChatRef.value.scrollTop;
  // 当滚动到顶部附近（小于等于10px）时，加载更多
  if (topval <= 10 && currentPage.value > 1) {
    loadMoreData();
  }
};

const loadMoreData = () => {
  // 向上加载历史消息，页码减1
  if (currentPage.value > 1) {
    currentPage.value--;
    getAllChatRecordMethod();
  }
};

const getAllChatRecordMethod = async () => {
  if (isLoadingMore.value) {
    return;
  }
  
  isLoadingMore.value = true;
  try {
    const res = await getAllChatRecord(currentPage.value, pageSize, props.acceptUid);
    const { records, total } = res.data;
    messageTotal.value = total;

    if (records && records.length > 0) {
      // 后端返回的是按时间戳升序排列的（最旧->最新）
      // 加载更多历史消息时，将更旧的消息添加到数组开头
      const existingMessages = dataList.value || [];
      const newMessages = records.map((item: any) => ({
        ...item,
        time: formatDate(item.timestamp),
      }));
      const allMessages = [...newMessages, ...existingMessages];

      // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
      allMessages.sort((a: any, b: any) => {
        const timeA = Number(a.timestamp) || 0;
        const timeB = Number(b.timestamp) || 0;
        return timeA - timeB;
      });

      dataList.value = allMessages;

      // 保持滚动位置（加载历史消息时，保持当前可见的消息位置不变）
      const scrollHeightBefore = ChatRef.value?.scrollHeight || 0;
      await nextTick();
      // 使用 setTimeout 确保 DOM 完全更新
      setTimeout(() => {
        if (ChatRef.value) {
          const scrollHeightAfter = ChatRef.value.scrollHeight;
          const heightDiff = scrollHeightAfter - scrollHeightBefore;
          ChatRef.value.scrollTop = (ChatRef.value.scrollTop || 0) + heightDiff;
        }
      }, 50);
    }
  } catch (error) {
    console.error("加载历史消息失败:", error);
  } finally {
    isLoadingMore.value = false;
  }
};

const initChatData = async () => {
  try {
    // 先获取必要的用户信息
    currentUser.value = userStore.getUserInfo();

    if (props.acceptUid) {
      // 确保有 acceptUid 才继续
      // 🔧 客服功能：如果是客服ID（0），使用固定的客服信息
      if (props.acceptUid === '0' || String(props.acceptUid) === '0') {
        console.log('客服头像路径:', CUSTOMER_SERVICE.AVATAR);
        acceptUser.value = {
          id: '0',
          username: CUSTOMER_SERVICE.USER_NAME,
          avatar: CUSTOMER_SERVICE.AVATAR // 使用导入的客服头像
        };
        console.log('客服用户信息:', acceptUser.value);
      } else {
        const userRes = await getUserById(props.acceptUid);
        acceptUser.value = userRes.data;
      }

      // 先获取第一页，确定总页数
      const firstPageRes = await getAllChatRecord(1, pageSize, props.acceptUid);
      const { total } = firstPageRes.data;
      messageTotal.value = total;

      // 计算总页数
      const totalPages = Math.ceil(total / pageSize);

      // 收集所有消息
      const allMessages: any[] = [];
      
      if (totalPages > 0) {
        // 加载所有页面的消息（从第一页到最后一页）
        for (let page = 1; page <= totalPages; page++) {
          const pageRes = await getAllChatRecord(page, pageSize, props.acceptUid);
          const { records } = pageRes.data;
          if (records && records.length > 0) {
            allMessages.push(...records);
          }
        }
        
        // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
        allMessages.sort((a: any, b: any) => {
          const timeA = Number(a.timestamp) || 0;
          const timeB = Number(b.timestamp) || 0;
          return timeA - timeB;
        });

        // 格式化并设置数据
        dataList.value = allMessages.map((item: any) => ({
          ...item,
          time: formatDate(item.timestamp),
        }));
        
        // 设置当前页为最后一页的前一页，用于加载更多历史消息
        currentPage.value = Math.max(totalPages - 1, 1);

        // 等待 DOM 更新后滚动到底部
        await nextTick();
        // 使用 setTimeout 确保 DOM 完全渲染
        setTimeout(() => {
          if (ChatRef.value) {
            ChatRef.value.scrollTop = ChatRef.value.scrollHeight;
          }
        }, 100);
      } else {
        // 没有消息
        dataList.value = [];
      }
    }
  } catch (error) {
    console.error("初始化聊天数据失败:", error);
  }
};

watch(
  () => imStore.message,
  (newVal) => {
    if (!newVal || !newVal.msgType || newVal.msgType !== 1) {
      return; // 只处理普通聊天消息
    }
    
    // 确保 acceptUser 和 currentUser 已初始化
    if (!acceptUser.value || !acceptUser.value.id || !currentUser.value || !currentUser.value.id) {
      console.log('用户信息未初始化，跳过消息处理', {
        acceptUser: acceptUser.value,
        currentUser: currentUser.value,
      });
      return;
    }
    
    // 判断是否是当前对话的消息
    // 情况1：接收方收到消息（newVal.acceptUid === currentUser.id && newVal.sendUid === acceptUser.id）
    // 情况2：发送方发送的消息（newVal.sendUid === currentUser.id && newVal.acceptUid === acceptUser.id）
    const currentUserId = String(currentUser.value.id || '');
    const acceptUserId = String(acceptUser.value.id || '');
    const messageSendUid = String(newVal.sendUid || '');
    const messageAcceptUid = String(newVal.acceptUid || '');
    
    console.log('检查消息是否属于当前对话:', {
      currentUserId,
      acceptUserId,
      messageSendUid,
      messageAcceptUid,
      message: newVal,
    });
    
    // 🔧 修复：判断是否是当前对话的消息
    // 只处理接收到的消息（来自对方的消息），不处理用户自己发送的消息（避免重复）
    // 用户自己发送的消息已经在 sendMessage 中添加了
    const isReceivedMessage = messageSendUid === acceptUserId && messageAcceptUid === currentUserId; // 接收方收到消息
    const isSentMessage = messageSendUid === currentUserId && messageAcceptUid === acceptUserId; // 发送方发送的消息（WebSocket推送回来的）
    
    if (isReceivedMessage) {
      // 这是来自对方的消息，直接添加
      console.log('✅ 收到来自对方的消息，添加到列表:', newVal);
      insertMessage(newVal);
    } else if (isSentMessage) {
      // 🔧 这是用户自己发送的消息被推送回来了，检查是否已经存在
      console.log('⚠️ 用户自己发送的消息被推送回来了，检查是否已存在');
      
      // 检查是否是pending消息（通过内容匹配）
      const matchingPendingKey = Array.from(pendingSentMessages).find(key => {
        const parts = key.split('-');
        if (parts.length < 4) return false;
        // messageKey格式：sendUid-acceptUid-content-timestamp
        const keySendUid = parts[0];
        const keyAcceptUid = parts[1];
        const keyContent = parts.slice(2, -1).join('-'); // 内容可能在key中包含多个'-'
        return keySendUid === newVal.sendUid && 
               keyAcceptUid === newVal.acceptUid &&
               keyContent === newVal.content;
      });
      
      // 检查消息是否已存在（通过内容+发送者+接收者匹配）
      const alreadyExists = dataList.value.some((item: any) => {
        return item.content === newVal.content && 
               item.sendUid === newVal.sendUid && 
               item.acceptUid === newVal.acceptUid &&
               (!item.timestamp || !newVal.timestamp || 
                Math.abs((item.timestamp || 0) - (newVal.timestamp || 0)) < 5000);
      });
      
      if (matchingPendingKey || alreadyExists) {
        console.log('⚠️ 消息已存在，跳过添加（可能是WebSocket推送回来的重复消息）');
        // 如果已存在，更新现有消息的ID和时间戳（如果有的话）
        const index = dataList.value.findIndex((item: any) => {
          return (item._isLocalMessage || item._messageKey === matchingPendingKey) &&
                 item.content === newVal.content && 
                 item.sendUid === newVal.sendUid && 
                 item.acceptUid === newVal.acceptUid &&
                 (!item.timestamp || !newVal.timestamp || 
                  Math.abs((item.timestamp || 0) - (newVal.timestamp || 0)) < 5000);
        });
        if (index !== -1) {
          // 更新现有消息的ID和时间戳（如果有的话）
          if (newVal.id) {
            dataList.value[index].id = newVal.id;
          }
          if (newVal.timestamp) {
            dataList.value[index].timestamp = newVal.timestamp;
            dataList.value[index].time = formatDate(newVal.timestamp);
          }
          // 移除本地消息标记
          dataList.value[index]._isLocalMessage = false;
          // 移除pending标记
          if (matchingPendingKey) {
            pendingSentMessages.delete(matchingPendingKey);
          }
          if (dataList.value[index]._messageKey) {
            pendingSentMessages.delete(dataList.value[index]._messageKey);
          }
        }
      } else {
        console.log('✅ 消息不存在，添加到列表（可能是首次推送）');
        insertMessage(newVal);
      }
    } else {
      console.log('❌ 消息不属于当前对话，跳过:', {
        isReceivedMessage,
        isSentMessage,
        currentUserId,
        acceptUserId,
        messageSendUid,
        messageAcceptUid,
      });
    }
  },
  {
    deep: true,
    immediate: false,
  }
);

watch(
  () => props.modelValue,
  async (newVal) => {
    if (newVal && props.acceptUid) {
      try {
        // 重置数据
        dataList.value = [];
        currentPage.value = 1;
        messageTotal.value = 0;

        // 重新获取用户信息和聊天记录
        await initChatData();
      } catch (error) {
        console.error("初始化聊天数据失败:", error);
      }
    }
  },
  {
    immediate: true, // 添加 immediate: true
  }
);

onMounted(async () => {
  currentUser.value = userStore.getUserInfo();
  // 为表情选择器添加点击事件监听
  const emojiPicker = document.querySelector(".emoji-picker");
  if (emojiPicker) {
    emojiPicker.addEventListener("click", handleEmojiPickerClick);
  }
});

// 在组件卸载时移除事件监听，并确保停止语音播放
onUnmounted(() => {
  // 清理表情选择器事件监听
  const emojiPicker = document.querySelector(".emoji-picker");
  if (emojiPicker) {
    emojiPicker.removeEventListener("click", handleEmojiPickerClick);
  }

  // 清理所有预览URL
  uploadImages.value.forEach((img) => {
    URL.revokeObjectURL(img.preview);
  });

  // 停止当前语音播放，避免弹窗关闭后仍然播放
  if (audioPlayer) {
    try {
      audioPlayer.pause();
      audioPlayer.currentTime = 0;
      audioPlayer.src = "";
    } catch (e) {
      console.warn("NoteChat 卸载时停止音频异常", e);
    }
  }
  currentPlayId.value = null;
});
</script>

<style lang="less" scoped>
.emoji-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.6rem 0.5rem;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(51, 51, 51, 0.8);

  &:hover {
    color: #00aaff;
  }
}

.emoji-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  user-select: none; /* 防止表情文本被选中 */

  .emoji-item {
    font-size: 1.5rem;
    cursor: pointer;
    transition: all 0.2s;
    text-align: center;
    padding: 4px;
    border-radius: 4px;

    &:hover {
      transform: scale(1.2);
      background-color: #f0f0f0;
    }
  }
}

.container {
  position: fixed;
  left: 83%;
  top: 70%;
  width: 22vw;
  height: 87vh;
  overflow: auto;
  transform: translate(-50%, -50%);
  border-radius: 10px;

  .chat-container {
    flex-direction: column;
    height: 90%;
    background-color: #fff;

    .chat-header {
      height: 60px; // 固定高度
      width: auto;
      display: flex;
      justify-content: center; // 水平居中
      align-items: center; // 垂直居中
      background: #fff;
      border-bottom: 1px solid #f4f4f4;
      position: relative;
      margin-top: -10px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .username {
          font-size: 16px;
          font-weight: 500;
          color: #333;
        }
      }

      .header-user {
        display: flex;
        align-items: center;
      }
    }

    .chat-main {
      display: flex;
      flex-direction: column;
      height: 100%;
      position: relative;

      .message-img {
        width: 15rem;
        height: 18.75rem;
        object-fit: cover;
        margin: 0 0.3125rem;
        border-radius: 0.5rem;
      }

      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #909399;
        font-size: 12px;

        .el-icon {
          font-size: 24px;
          margin-bottom: 8px;
        }
      }

      .chat-record {
        flex: 1;
        padding: 16px;
        overflow-y: auto;
        overflow-x: hidden;
        min-height: 300px;
        height: 100%; // 确保占满父容器

        .message-item,
        .message-my-item {
          display: flex;
          margin: 2rem 0; // 增加每条消息的上下间距
          position: relative; // 添加相对定位

          .message-content-wrapper {
            display: flex;
            flex-direction: column;
            max-width: 70%; // 限制消息内容最大宽度

            .message-time {
              font-size: 0.7rem; // 12px
              color: #888;
              margin-bottom: 4px;
              white-space: nowrap; // 防止时间换行
              position: absolute; // 使用绝对定位
              top: -12px; // 将时间戳向上移动
            }

            .message-conent,
            .message-my-conent,
            .message-voice,
            .message-my-voice {
              padding: 8px 12px;
              border-radius: 8px;
              word-break: break-word; // 文字换行
              min-width: 40px; // 设置最小宽度
              max-width: 100%; // 确保不超过wrapper宽度
              margin-top: 5px; // 添加顶部间距，使气泡向下移动
              display: inline-flex;
              align-items: center; // 图标和秒数垂直居中
            }
          }
        }

        // 左侧消息（对方）
        .message-item {
          justify-content: flex-start;

          .message-content-wrapper {
            margin-left: 12px;

            .message-time {
              left: 60px; // 调整左侧时间位置
            }

            .message-conent,
            .message-voice {
              background-color: #fff;
              border: 1px solid #f4f4f4;
            }
          }
        }

        // 右侧消息（自己）
        .message-my-item {
          justify-content: flex-end;

          .message-content-wrapper {
            margin-right: 12px;
            align-items: flex-end; // 右对齐

            .message-time {
              right: 60px; // 调整右侧时间位置
            }

            .message-my-conent,
            .message-my-voice {
              background-color: #00aaff;
              color: #fff;
            }
          }
        }

        // 图片消息样式优化
        .message-img {
          max-width: 240px;
          max-height: 300px;
          width: auto; // 修改为自适应宽度
          height: auto; // 修改为自适应高度
          object-fit: contain; // 确保图片比例正确
          border-radius: 8px;
        }
      }

      // 添加消息间隔样式
      .message-item + .message-my-item,
      .message-my-item + .message-item {
        margin-top: 2rem; // 增加不同发送者消息之间的间距
      }

      // 优化滚动条样式
      .chat-record {
        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background-color: rgba(0, 0, 0, 0.1);
          border-radius: 3px;
        }

        &::-webkit-scrollbar-track {
          background-color: transparent;
        }
      }

      .chat-input {
        height: auto;
        min-height: 140px;
        margin-top: auto;

        .input-tool {
          display: flex;
          justify-content: space-between;
          height: 0.5rem;
          padding: 0 0.3125rem;

          .tool-left {
            display: flex;
            justify-content: left;
          }
        }

        .input-content {
          height: 120px;
          display: flex;
          flex-direction: column;

          .input-btn {
            display: flex;
            justify-content: space-between;
            padding: 0 2rem;
            margin-top: 10px;
          }
        }

        .post-content:empty::before {
          content: attr(placeholder);
          color: #a29e9e;
          font-size: 0.875rem;
        }

        .post-content {
          flex: 1;
          min-height: 80px;
          background-color: #f8f8f8;
          border-radius: 5px;
          padding: 8px;
          overflow-y: auto;
          cursor: text; /* 添加文本光标样式 */
          outline: none; /* 移除默认的轮廓 */
        }
      }
    }
  }
}

.voice-icon {
  width: 1.1em;
  height: 1.1em;
  margin-right: 0.25rem;
}

.voice-duration {
  font-size: 0.9rem;
}

.voice-playing {
  animation: voicePulse 1s infinite;
}

@keyframes voicePulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.6;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.icon-item {
  width: 1.2em;
  height: 1.2em;
  margin-right: 0.6rem;
  color: rgba(51, 51, 51, 0.8);
}
</style>
