<template>
  <div class="chat-container">
    <hr color="#f4f4f4" />
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
                  <span 
                    v-for="emoji in emojis" 
                    :key="emoji"
                    @click="insertEmoji(emoji)"
                    class="emoji-item"
                  >
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
import { getProductChatRecord, sendMsg } from "@/api/im";
import { useUserStore } from "@/store/userStore";
import { useImStore } from "@/store/imStore";
import type { UploadProps } from "element-plus";
import { uploadFileMethod, formatDate, getRandomString } from "@/utils/util";
const imStore = useImStore();
const userStore = useUserStore();
// 添加保存的光标位置变量
const savedRange = ref<Range | null>(null);
const currentPlayId = ref<string | number | null>(null);
let audioPlayer: HTMLAudioElement | null = null;

const props = defineProps({
  acceptUid: {
    type: String,
    default: "",
  },
  productId: {  
    type: String,
    default: "",
  }
});

interface UploadImage {
  id: string;
  file: File;        // 存储文件对象
  preview: string;    // 预览URL
}

const uploadImages = ref<UploadImage[]>([]);

const ChatRef = ref();
const currentUser = ref<any>({});
const acceptUser = ref<any>({});
const dataList = ref<any>();
const currentPage = ref(1);
const pageSize = 15;
const messageTotal = ref(0);
const postContent = ref(null);

watch(
  () => imStore.message,
  (newVal) => {
    if (!newVal || !newVal.msgType || newVal.msgType !== 1) {
      return; // 只处理普通聊天消息
    }
    
    // 确保 acceptUser 和 currentUser 已初始化
    if (!acceptUser.value || !acceptUser.value.id || !currentUser.value || !currentUser.value.id) {
      console.log('商品聊天：用户信息未初始化，跳过消息处理', {
        acceptUser: acceptUser.value,
        currentUser: currentUser.value,
      });
      return;
    }
    
    // 判断是否是当前商品对话的消息
    // 需要同时检查 productId 是否匹配
    const currentUserId = String(currentUser.value.id || '');
    const acceptUserId = String(acceptUser.value.id || '');
    const messageSendUid = String(newVal.sendUid || '');
    const messageAcceptUid = String(newVal.acceptUid || '');
    const messageProductId = String(newVal.productId || '');
    const currentProductId = String(props.productId || '');
    
    // 判断是否是当前商品对话的消息
    const isCurrentChatMessage = 
      messageProductId === currentProductId && // 商品ID必须匹配
      ((messageSendUid === acceptUserId && messageAcceptUid === currentUserId) || // 接收方收到消息
       (messageSendUid === currentUserId && messageAcceptUid === acceptUserId)); // 发送方发送消息
    
    if (isCurrentChatMessage) {
      console.log('✅ 商品聊天：收到当前对话的新消息，准备添加:', newVal);
      insertMessage(newVal);
    } else {
      console.log('❌ 商品聊天：消息不属于当前对话，跳过:', {
        isCurrentChatMessage,
        messageProductId,
        currentProductId,
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

const emojiPickerVisible = ref(false);
const emojis = ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗', '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯', '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈', '👿', '👹', '👺', '🤡', '💩', '👻', '💀', '☠️', '👽', '👾', '🤖', '🎃', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾'];

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
  console.log("📨 商品聊天：新消息添加:", {
    id: message.id,
    sendUid: message.sendUid,
    acceptUid: message.acceptUid,
    productId: message.productId,
    timestamp: message.timestamp,
    time: message.timestamp || message.time,
    content: message.content ? message.content.substring(0, 20) : '无内容',
    currentListLength: dataList.value?.length || 0,
  });

  // 新消息应该添加到数组末尾，但需要确保按时间戳排序
  if (!dataList.value) {
    dataList.value = [];
  }
  
  // 确保消息有 timestamp 和 id
  const currentTimestamp = message.timestamp || Date.now();
  const messageId = message.id || `msg-${currentTimestamp}-${Math.random().toString(36).substr(2, 9)}`;
  
  // 检查消息是否已存在（避免重复）
  const exists = dataList.value.some(
    (item: any) => {
      // 通过 ID 判断
      if (item.id && messageId && item.id === messageId) {
        return true;
      }
      // 通过时间戳和内容判断
      if (item.timestamp === currentTimestamp && item.content === message.content) {
        return true;
      }
      return false;
    }
  );
  
  if (exists) {
    console.log('⚠️ 商品聊天：消息已存在，跳过添加:', messageId || currentTimestamp);
    return;
  }
  
  // 添加时间格式化
  const formattedMessage = {
    ...message,
    id: messageId,
    timestamp: currentTimestamp,
    time: message.time || formatDate(currentTimestamp),
  };
  
  console.log('➕ 商品聊天：添加新消息到列表:', formattedMessage);
  
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
  
  console.log('✅ 商品聊天：消息已添加，当前列表长度:', dataList.value.length);

  await nextTick();
  // 滚动到最底部（使用 scrollTop 而不是 scrollIntoView，更可靠）
  if (ChatRef.value) {
    ChatRef.value.scrollTop = ChatRef.value.scrollHeight;
    console.log('📜 商品聊天：已滚动到底部');
  }
};

// 选择图片
const handleChange: UploadProps["onChange"] = async (uploadFile) => {
  // 添加文件类型和大小限制
  const isImage = uploadFile.raw?.type.startsWith('image/');
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
      preview
    });

    // 插入预览图到编辑器
    document.getElementById("post-textarea")!.innerHTML +=
      `<img data-id="${imageId}" src="${preview}" style="width:3.75rem;height:3.75rem;object-fit: cover;">`;
      
  } catch (error) {
    ElMessage.error(t("message.imageProcessFailed"));
    console.error(error);
  }
};


const sendMessage = (message: any) => {
  // 添加 productId 到消息对象
  message.productId = props.productId;

  new Promise((res) => {
    insertMessage(message);
    res(message);
  }).then((_message: any) => {
    sendMsg(_message).then(() => {
      const data = dataList.value.filter((item: any) => item.id === _message.id);
      data[0].isLoading = false;
    });
  });
};

const submit = async () => {
  const textarea = document.getElementById("post-textarea")!;
  const htmlContent = textarea.innerHTML;
  
  if (!htmlContent.trim()) return;

  try {
    // 1. 处理文字内容 - 添加清理 HTML 标签的逻辑
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = htmlContent;
    
    // 获取所有图片元素
    const images = Array.from(tempDiv.getElementsByTagName('img'));
    
    // 将图片替换为占位符
    images.forEach(img => {
      img.replaceWith('{{IMG}}');
    });
    
    // 清理 HTML 标签，保留纯文本
    const cleanText = tempDiv.innerText
      .replace(/\n\s*\n/g, '\n') // 删除多余的空行
      .replace(/^\s+|\s+$/g, ''); // 删除首尾空白
    
    // 分割文本
    const textParts = cleanText.split('{{IMG}}').filter(text => text.trim());
    
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
        isLoading: true
      };
      
      await sendMessage(message);
    }

    // 3. 处理图片 - 先上传再发送
    for (const img of Array.from(images)) {
      const imageId = img.getAttribute('data-id');
      const uploadedImage = uploadImages.value.find(img => img.id === imageId);
      
      if (uploadedImage) {
        try {
          // 显示上传加载状态
          const loading = ElMessage.warning({
            message: t("message.imageSending"),
            duration: 3000
          });
          
          // 上传图片
          const res = await uploadFileMethod(uploadedImage.file) as any;
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
              isLoading: true
            };
            
            await sendMessage(message);
          } else {
            throw new Error(res.data?.msg || t("message.uploadFailed"));
          }
        } catch (error) {
          let msg = t("error.unknown");
          if (error instanceof Error) {
            msg = error.message;
          } else if (typeof error === 'string') {
            msg = error;
          }
          ElMessage.error(t("message.imageUploadFailedPrefix") + msg);
          console.error('图片上传失败:', error);
          // 继续处理下一张图片
          continue;
        }
      }
    }

    // 4. 清理
    textarea.innerHTML = '';
    // 清理预览URL
    uploadImages.value.forEach(img => {
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
    const res = await getProductChatRecord(currentPage.value, pageSize, props.acceptUid, props.productId);
    const { records, total } = res.data;
    messageTotal.value = total;
    
    if (records && records.length > 0) {
      // 后端返回的是按时间戳升序排列的（最旧->最新）
      // 加载更多历史消息时，将更旧的消息添加到数组开头
      const existingMessages = dataList.value || [];
      const allMessages = [...records, ...existingMessages];
      
      // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
      allMessages.sort((a: any, b: any) => {
        const timeA = Number(a.timestamp) || 0;
        const timeB = Number(b.timestamp) || 0;
        return timeA - timeB;
      });
      
      dataList.value = allMessages;
      
      // 保持滚动位置
      const scrollHeightBefore = ChatRef.value.scrollHeight;
      await nextTick();
      const scrollHeightAfter = ChatRef.value.scrollHeight;
      const heightDiff = scrollHeightAfter - scrollHeightBefore;
      ChatRef.value.scrollTop = ChatRef.value.scrollTop + heightDiff;
    }
  } catch (error) {
    console.error("加载历史消息失败:", error);
  } finally {
    isLoadingMore.value = false;
  }
};

const initChatData = async () => {
  currentUser.value = userStore.getUserInfo();
  const userRes = await getUserById(props.acceptUid);
  acceptUser.value = userRes.data;
  
  // 先获取第一页，确定总页数
  const firstPageRes = await getProductChatRecord(1, pageSize, props.acceptUid, props.productId);
  const { total } = firstPageRes.data;
  messageTotal.value = total;
  
  // 计算总页数
  const totalPages = Math.ceil(total / pageSize);
  
  // 收集所有消息
  const allMessages: any[] = [];
  
  if (totalPages > 0) {
    // 加载所有页面的消息（从第一页到最后一页）
    for (let page = 1; page <= totalPages; page++) {
      const pageRes = await getProductChatRecord(page, pageSize, props.acceptUid, props.productId);
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
};

watch(
  // 同时监听 acceptUid 和 productId
  [() => props.acceptUid, () => props.productId],
  ([newAcceptUid, newProductId]) => {
    if (newAcceptUid && newProductId) {
      // 重置数据
      dataList.value = [];
      currentPage.value = 1;
      messageTotal.value = 0;
      
      // 重新获取用户信息和聊天记录
      initChatData();
    }
  },
  {
    immediate: true
  }
);

onMounted(async () => {
  currentUser.value = userStore.getUserInfo();
  // 为表情选择器添加点击事件监听
  const emojiPicker = document.querySelector('.emoji-picker');
  if (emojiPicker) {
    emojiPicker.addEventListener('click', handleEmojiPickerClick);
  }
});

// 在组件卸载时移除事件监听，并确保停止语音播放
onUnmounted(() => {
  const emojiPicker = document.querySelector('.emoji-picker');
  if (emojiPicker) {
    emojiPicker.removeEventListener('click', handleEmojiPickerClick);
  }

  // 停止当前语音播放，避免弹窗关闭后仍然播放
  if (audioPlayer) {
    try {
      audioPlayer.pause();
      audioPlayer.currentTime = 0;
      audioPlayer.src = '';
    } catch (e) {
      console.warn('ProductChat 卸载时停止音频异常', e);
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
    display: flex;
    flex-direction: column;
    height: 77%;
    background-color: #fff;

    .chat-header {
      padding: 12px 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #f4f4f4;

      .header-user {
        display: flex;
        align-items: center;
      }
    }

    .chat-main {
      display: flex;
      flex-direction: column;
      overflow: hidden;
      margin-top: -20px;
      height: 100%;

      .message-img {
        width: 15rem;
        height: 18.75rem;
        object-fit: cover;
        margin: 0 0.3125rem;
        border-radius: 0.5rem;
      }

      .chat-record {
        flex: 1;
        padding: 16px;
        overflow-y: auto;
        min-height: 300px;
        margin-top: -30px;

        .message-item, .message-my-item {
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
              top: -15px; // 将时间戳向上移动
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
        min-height: 170px;
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

.icon-item {
  width: 1.2em;
  height: 1.2em;
  margin-right: 0.6rem;
  color: rgba(51, 51, 51, 0.8);
}
</style>
