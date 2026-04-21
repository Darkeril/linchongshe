<template>
  <div
    class="container"
    style="transition: background-color 0.4s ease 0s; background-color: hsla(0,0%,100%,0.98)"
  >
    <div class="chat-container">
      <header class="chat-header">
        <div class="header-left"></div>
        <div class="header-user">
          <el-avatar :src="acceptUser.avatar" />
          <span style="margin-left: 0.3125rem">{{ acceptUser.username }}</span>
        </div>
        <div class="header-tool">
          <More class="icon-item"></More>
        </div>
      </header>
      <hr color="#f4f4f4" />
      <main class="chat-main">
        <div class="chat-record" ref="ChatRef" @scroll="showScroll()">
          <div v-for="(item, index) in dataList" :key="index">
            <div class="message-my-item" v-if="item.acceptUid === acceptUser.id">
              <Loading v-show="item.isLoading" style="width: 0.8em; height: 0.8em; margin-right: 0.5rem" />
              <div class="message-my-conent" v-if="item.msgType == 1">{{ item.content }}</div>
              <img :src="item.content" class="message-img" v-else-if="item.msgType == 2" />
              <!-- 语音消息（自己发送） -->
              <div
                class="message-my-voice"
                v-else-if="item.msgType == 3"
                @click="playAudio(item)"
              >
                <Microphone class="voice-icon" />
                <span class="voice-duration">{{ (item.audioTime || 1) + '\"' }}</span>
              </div>
              <div class="user-avatar">
                <el-avatar :src="currentUser.avatar" />
              </div>
            </div>

            <div class="message-item" v-else>
              <div class="user-avatar">
                <el-avatar :src="acceptUser.avatar" />
              </div>
              <div class="message-conent" v-if="item.msgType == 1">{{ item.content }}</div>
              <img :src="item.content" class="message-img" v-else-if="item.msgType == 2" />
              <!-- 语音消息（对方发送） -->
              <div
                class="message-voice"
                v-else-if="item.msgType == 3"
                @click="playAudio(item)"
              >
                <Microphone class="voice-icon" />
                <span class="voice-duration">{{ (item.audioTime || 1) + '\"' }}</span>
              </div>
            </div>
          </div>
        </div>
        <hr color="#f4f4f4" />
        <div class="chat-input">
          <div class="input-tool">
            <div class="tool-left">
              <PieChart class="icon-item"></PieChart>
              <el-upload :auto-upload="false" :show-file-list="false" :on-change="handleChange">
                <Picture class="icon-item"></Picture>
              </el-upload>
            </div>
            <div class="tool-history">
              <Clock class="icon-item"></Clock>
            </div>
          </div>

          <!-- <textarea type="textarea" v-model="content" class="input-content" rows="15" @keyup.enter="submit" /> -->
          <div class="input-content">
            <p
              id="post-textarea"
              ref="postContent"
              class="post-content"
              contenteditable="true"
              data-tribute="true"
              :placeholder="t('message.chatInputPlaceholder')"
              @keyup.enter="submit"
            ></p>
            <div class="input-btn">
              <div></div>
              <el-button type="primary" round style="width: 5.55rem" @click="submit">{{ $t("common.send") }}</el-button>
            </div>
          </div>
        </div>
      </main>
    </div>
    <div class="close-cricle" @click="close">
      <div class="close close-mask-white">
        <Close style="width: 1.2em; height: 1.2em; color: rgba(51, 51, 51, 0.8)" />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { More, PieChart, Picture, Clock, Close, Loading, Microphone } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
import { getUserById } from "@/api/user";
import { getAllChatRecord, sendMsg } from "@/api/im";
import { useUserStore } from "@/store/userStore";
import { useImStore } from "@/store/imStore";
import type { UploadProps } from "element-plus";
import { convertImgToBase64 } from "@/utils/util";
import { getRandomString } from "@/utils/util";
import { CUSTOMER_SERVICE } from "@/utils/constants";

const imStore = useImStore();
const userStore = useUserStore();
const props = defineProps({
  acceptUid: {
    type: String,
    default: "",
  },
});

const ChatRef = ref();
const currentUser = ref<any>({});
const acceptUser = ref<any>({});
const dataList = ref<any>();
const currentPlayId = ref<string | null>(null);
const audio = new Audio();
const currentPage = ref(1);
const pageSize = 15;
const messageTotal = ref(0);
const postContent = ref(null);

watch(
  () => imStore.message,
  (newVal) => {
    if (!newVal || !newVal.sendUid || !newVal.acceptUid) return;
    
    // 🔧 修复：处理两种情况
    // 1. 来自对方的消息（客服发送给用户）：sendUid = 客服ID, acceptUid = 用户ID
    // 2. 用户自己发送的消息被推送回来：sendUid = 用户ID, acceptUid = 客服ID
    
    if (newVal.sendUid === acceptUser.value.id && newVal.acceptUid === currentUser.value.id) {
      // 情况1：来自对方的消息，直接添加
      console.log('✅ 收到来自对方的消息，添加到列表');
      insertMessage(newVal);
    } else if (newVal.sendUid === currentUser.value.id && newVal.acceptUid === acceptUser.value.id) {
      // 情况2：用户自己发送的消息被推送回来了，检查是否是pending消息
      console.log('⚠️ 用户自己发送的消息被推送回来了');
      
      // 检查是否是pending消息（通过内容匹配）
      const matchingPendingKey = Array.from(pendingMessages).find(key => {
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
      
      if (matchingPendingKey) {
        console.log('✅ 这是pending消息，更新现有消息而不是添加新消息');
        // 这是用户自己发送的消息被推送回来了，更新现有消息而不是添加新消息
        const index = dataList.value.findIndex((item: any) => {
          return item._isLocalMessage && 
                 item.content === newVal.content && 
                 item.sendUid === newVal.sendUid && 
                 item.acceptUid === newVal.acceptUid &&
                 Math.abs((item.timestamp || 0) - (newVal.timestamp || 0)) < 5000; // 5秒内的消息认为是同一条
        });
        
        if (index !== -1) {
          console.log('✅ 找到匹配的消息，更新ID和时间戳');
          // 更新现有消息，使用后端返回的ID和时间戳
          dataList.value[index] = {
            ...dataList.value[index],
            id: newVal.id || dataList.value[index].id,
            timestamp: newVal.timestamp || dataList.value[index].timestamp,
            _isLocalMessage: false, // 标记为已同步
          };
          // 移除pending标记
          pendingMessages.delete(matchingPendingKey);
          if (dataList.value[index]._messageKey) {
            pendingMessages.delete(dataList.value[index]._messageKey);
          }
          return; // 不添加新消息
        } else {
          console.log('⚠️ 未找到匹配的消息，可能已经被添加过了，跳过');
        }
      } else {
        console.log('⚠️ 这不是pending消息，可能是重复推送，跳过');
      }
    } else {
      console.log('⚠️ 消息不匹配当前聊天对象，跳过');
    }
  },
  {
    deep: true,
  }
);

const insertMessage = async (message: any) => {
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
    // 2. 通过messageKey判断（用于跟踪用户自己发送的消息）
    if (item._messageKey && message._messageKey && item._messageKey === message._messageKey) {
      return true;
    }
    // 3. 通过时间戳、内容、发送者和接收者判断（处理ID不一致的情况）
    // 对于用户自己发送的消息，时间戳容差可以更大（5秒）
    // 对于接收到的消息，时间戳容差较小（1秒）
    const timeTolerance = message._isLocalMessage ? 5000 : 1000;
    const timeMatch = item.timestamp === message.timestamp || 
                     (item.timestamp && message.timestamp && 
                      Math.abs(Number(item.timestamp) - Number(message.timestamp)) < timeTolerance);
    const contentMatch = item.content === message.content;
    const sendUidMatch = item.sendUid === message.sendUid;
    const acceptUidMatch = item.acceptUid === message.acceptUid;
    
    if (timeMatch && contentMatch && sendUidMatch && acceptUidMatch) {
      return true;
    }
    return false;
  });

  if (!exists) {
    dataList.value.push(message);

    // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
    dataList.value.sort((a: any, b: any) => {
      const timeA = Number(a.timestamp) || 0;
      const timeB = Number(b.timestamp) || 0;
      return timeA - timeB;
    });
  } else {
    // 如果消息已存在，记录日志用于调试
    console.log('⚠️ 消息已存在，跳过添加:', {
      id: message.id,
      timestamp: message.timestamp,
      content: message.content?.substring(0, 20),
      sendUid: message.sendUid,
      acceptUid: message.acceptUid
    });
  }

  await nextTick();
  // 滚动到最底部
  if (ChatRef.value?.lastElementChild) {
    ChatRef.value.lastElementChild.scrollIntoView({
      block: "start",
      behavior: "smooth",
    });
  }
};

// 播放语音消息（msgType=3）
const playAudio = (item: any) => {
  if (!item || !item.content) {
    ElMessage.warning(t("message.voiceEmpty"));
    return;
  }
  const url = decodeURIComponent(String(item.content));
  try {
    // 再次点击同一条，则暂停播放
    if (currentPlayId.value === item.id && !audio.paused) {
      audio.pause();
      currentPlayId.value = null;
      return;
    }
    // 切换到新语音
    audio.pause();
    audio.src = url;
    audio.play()
      .then(() => {
        currentPlayId.value = item.id;
      })
      .catch((err) => {
        console.error("语音播放失败:", err);
        currentPlayId.value = null;
      });
    audio.onended = () => {
      currentPlayId.value = null;
    };
    audio.onerror = (err) => {
      console.error("语音播放出错:", err);
      currentPlayId.value = null;
    };
  } catch (e) {
    console.error("语音播放异常:", e);
    currentPlayId.value = null;
  }
};

const emit = defineEmits(["clickChat"]);

const close = () => {
  // 关闭对话框时，停止当前语音播放并重置状态
  try {
    audio.pause();
    audio.currentTime = 0;
  } catch (e) {
    console.warn("关闭对话时停止语音播放异常:", e);
  }
  currentPlayId.value = null;
  emit("clickChat", props.acceptUid);
};

// 选择图片
const handleChange: UploadProps["onChange"] = (uploadFile) => {
  const imgSrc = URL.createObjectURL(uploadFile.raw!);
  convertImgToBase64(
    uploadFile.raw!,
    (data: any) => {
      document.getElementById(
        "post-textarea"
      )!.innerHTML += `<img src='${imgSrc}' text='${data}' style='width:3.75rem;height:3.75rem;object-fit: cover;'></img>`;
    },
    (error: any) => {
      console.log("error", error);
    }
  );
};

// 🔧 标记：用于跟踪用户自己发送的消息，避免WebSocket推送回来时重复添加
const pendingMessages = new Set<string>();

const sendMessage = (message: any) => {
  // 🔧 修复：先添加消息到本地列表（显示loading状态），然后发送
  // 生成一个唯一标识，用于跟踪这条消息
  const messageKey = `${message.sendUid}-${message.acceptUid}-${message.content}-${Date.now()}`;
  pendingMessages.add(messageKey);
  
  // 给消息添加一个标记，表示这是用户自己发送的消息
  message._isLocalMessage = true;
  message._messageKey = messageKey;
  
  insertMessage(message);
  
  sendMsg(message).then(() => {
    // 发送成功后，更新消息状态（移除loading）
    const index = dataList.value.findIndex((item: any) => {
      return item._messageKey === messageKey || 
             (item.id === message.id) || 
             (item.timestamp === message.timestamp && 
              item.content === message.content && 
              item.sendUid === message.sendUid);
    });
    if (index !== -1) {
      dataList.value[index].isLoading = false;
      // 移除标记，表示消息已发送成功
      if (dataList.value[index]._messageKey) {
        pendingMessages.delete(dataList.value[index]._messageKey);
      }
    }
  }).catch((error) => {
    console.error('发送消息失败:', error);
    // 发送失败时，标记消息为错误状态
    const index = dataList.value.findIndex((item: any) => {
      return item._messageKey === messageKey || 
             (item.id === message.id) || 
             (item.timestamp === message.timestamp && 
              item.content === message.content && 
              item.sendUid === message.sendUid);
    });
    if (index !== -1) {
      dataList.value[index].isLoading = false;
      dataList.value[index].error = true;
      // 移除标记
      if (dataList.value[index]._messageKey) {
        pendingMessages.delete(dataList.value[index]._messageKey);
      }
    }
  });
};

const submit = () => {
  let htmlContent = document.getElementById("post-textarea")!.innerHTML;

  if (htmlContent === "") {
    return;
  }

  const imgReg = /<img.*?(?:>|\/>)/gi;
  const srcReg = /text=['"]?([^'"]*)['"]?/i;
  // let params = new FormData();
  // //注意此处对文件数组进行了参数循环添加
  const _contentImg = htmlContent.match(imgReg);

  const replaceContent = htmlContent.replaceAll(imgReg, "#").replace(/<[^>]*>[^<]*(<[^>]*>)?/gi, "");
  // 内容分割
  const _splitContent = replaceContent.split("#");

  _splitContent.forEach((item: string) => {
    if (item === null || item === "") {
      return;
    }
    //发送文字消息
    const message = {} as any;
    message.id = getRandomString(12);
    message.sendUid = currentUser.value.id;
    message.acceptUid = acceptUser.value.id;
    message.content = item;
    message.msgType = 1;
    message.chatType = 0;
    message.isLoading = true;
    sendMessage(message);
  });

  // 图片分割
  _contentImg?.forEach((item: any) => {
    const src = item.match(srcReg);
    const message = {} as any;
    message.id = getRandomString(12);
    message.sendUid = currentUser.value.id;
    message.acceptUid = acceptUser.value.id;
    message.content = src[1];
    message.msgType = 2;
    message.chatType = 0;
    message.isLoading = true;
    sendMessage(message);
  });
  // const content = htmlContent.replace(/<[^>]*>[^<]*(<[^>]*>)?/gi, "");
  document.getElementById("post-textarea")!.innerHTML = "";
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

onMounted(async () => {
  currentUser.value = userStore.getUserInfo();
  
  // 🔧 修复：如果是客服，使用固定的客服信息，否则获取用户信息
  if (props.acceptUid === CUSTOMER_SERVICE.USER_ID) {
    console.log('Chat组件 - 客服头像路径:', CUSTOMER_SERVICE.AVATAR);
    acceptUser.value = {
      id: CUSTOMER_SERVICE.USER_ID,
      username: CUSTOMER_SERVICE.USER_NAME,
      avatar: CUSTOMER_SERVICE.AVATAR
    };
    console.log('Chat组件 - 客服用户信息:', acceptUser.value);
  } else {
    getUserById(props.acceptUid).then((res) => {
      acceptUser.value = res.data;
    });
  }
  dataList.value = [];

  // 先获取第一页，确定总页数
  getAllChatRecord(1, pageSize, props.acceptUid).then(async (res) => {
    const { records, total } = res.data;
    messageTotal.value = total;

    // 计算总页数
    const totalPages = Math.ceil(total / pageSize);

    // 如果总页数大于1，从最后一页开始加载（最新的消息）
    if (totalPages > 1) {
      currentPage.value = totalPages;
      getAllChatRecord(currentPage.value, pageSize, props.acceptUid).then(async (res) => {
        const { records } = res.data;
        if (records && records.length > 0) {
          // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
          records.sort((a: any, b: any) => {
            const timeA = Number(a.timestamp) || 0;
            const timeB = Number(b.timestamp) || 0;
            return timeA - timeB;
          });

          dataList.value = records;
          currentPage.value = Math.max(totalPages - 1, 1); // 准备加载上一页

          await nextTick();
          // 滚动到最底部
          if (ChatRef.value?.lastElementChild) {
            ChatRef.value.lastElementChild.scrollIntoView({
              block: "start",
              behavior: "smooth",
            });
          }
        }
      });
    } else {
      // 只有一页，直接加载
      if (records && records.length > 0) {
        // 按时间戳排序，确保顺序正确（最旧的在前面，最新的在后面）
        records.sort((a: any, b: any) => {
          const timeA = Number(a.timestamp) || 0;
          const timeB = Number(b.timestamp) || 0;
          return timeA - timeB;
        });

        dataList.value = records;
        await nextTick();
        // 滚动到最底部
        if (ChatRef.value?.lastElementChild) {
          ChatRef.value.lastElementChild.scrollIntoView({
            block: "start",
            behavior: "smooth",
          });
        }
      }
    }
  });
});

// 组件卸载时，确保清理音频播放状态，避免页面上残留声音
onUnmounted(() => {
  try {
    audio.pause();
    audio.currentTime = 0;
    audio.src = "";
  } catch (e) {
    console.warn("组件卸载时停止语音播放异常:", e);
  }
  currentPlayId.value = null;
});
</script>
<style lang="less" scoped>
.icon-item {
  width: 1.2em;
  height: 1.2em;
  margin-right: 0.3125rem;
  color: rgba(51, 51, 51, 0.8);
}

.container {
  position: fixed;
  left: 45%;
  top: 50%;
  width: 90vw;
  height: 85vh;
  z-index: 20;
  overflow: auto;
  transform: translate(-50%, -50%);
  border-radius: 10px; /* 可选：为容器添加圆角 */

  .chat-container {
    width: 65%;
    margin: 0 auto;
    height: 90%;
    min-width: 50rem;
    transition: transform 0.4s ease 0s, width 0.4s ease 0s;
    transform: translate(6.5rem, 2rem) scale(1);
    overflow: visible;
    box-shadow: 0 0.5rem 4rem 0 rgba(0, 0, 0, 0.04), 0 0.0625rem 0.25rem 0 rgba(0, 0, 0, 0.02);
    border-radius: 1.25rem;
    background-color: #fff;
    transform-origin: left top;
    z-index: 100;

    .chat-header {
      height: 3.75rem;
      width: 100%;
      background-color: #fff;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-user {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .chat-main {
      height: 100%;

      .message-img {
        width: 15rem;
        height: 18.75rem;
        object-fit: cover;
        margin: 0 0.3125rem;
        border-radius: 0.5rem;
      }

      .chat-record {
        height: 60%;
        padding: 0 1.25rem;
        overflow-y: scroll;

        .message-item {
          display: flex;
          justify-content: left;
          align-items: center;
          margin: 1.25rem 0;

          .message-conent {
            margin-left: 0.3125rem;
            padding: 0.25rem 0.625rem;
            border: 0.0625rem solid #f4f4f4;
            background-color: #fff;
            border-radius: 0.5rem;
            font-size: 1rem;
          }

          .message-voice {
            margin-left: 0.3125rem;
            padding: 0.25rem 0.75rem;
            border-radius: 999px;
            background-color: #fff;
            display: inline-flex;
            align-items: center;
            cursor: pointer;
            border: 0.0625rem solid #f4f4f4;
          }
        }

        .message-my-item {
          display: flex;
          justify-content: right;
          align-items: center;
          margin: 1.25rem 0;

          .message-my-conent {
            margin-right: 0.3125rem;
            padding: 0.25rem 0.625rem;
            color: #fff;
            background-color: rgb(0, 170, 255);
            border-radius: 0.5rem;
            font-size: 1rem;
          }

          .message-my-voice {
            margin-right: 0.3125rem;
            padding: 0.25rem 0.75rem;
            color: #fff;
            background-color: rgb(0, 170, 255);
            border-radius: 999px;
            font-size: 1rem;
            display: inline-flex;
            align-items: center;
            cursor: pointer;
          }
        }
      }

      .chat-input {
        height: 25%;

        .input-tool {
          display: flex;
          justify-content: space-between;
          height: 1.25rem;
          padding: 0 0.3125rem;

          .tool-left {
            display: flex;
            justify-content: left;
          }
        }

        .input-content {
          width: 100%;
          height: 90%;
          resize: none;
          border: 0rem;
          outline: none;
          display: flex;
          flex-direction: column;
          justify-content: space-between;

          .input-btn {
            display: flex;
            justify-content: space-between;
            padding: 0 0.625rem;
          }
        }

        .post-content:empty::before {
          content: attr(placeholder);
          color: #ccc;
          font-size: 0.875rem;
        }

        .post-content {
          cursor: text;
          width: 100%;
          min-height: 60%;
          margin-bottom: 1.25rem;
          background: #fff;
          padding: 0rem 0.75rem 1.375rem;
          outline: none;
          overflow-y: auto;
          text-rendering: optimizeLegibility;
          font-size: 0.875rem;
          line-height: 1.375rem;
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
  }

  .close-cricle {
    left: 18vw;
    top: 1.3vw;
    position: fixed;
    display: flex;
    z-index: 100;
    cursor: pointer;

    .close-mask-white {
      box-shadow: 0 0.125rem 0.5rem 0 rgba(0, 0, 0, 0.04), 0 0.0625rem 0.125rem 0 rgba(0, 0, 0, 0.02);
      border: 0.0625rem solid rgba(0, 0, 0, 0.08);
    }

    .close {
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 100%;
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 2.5rem;
      cursor: pointer;
      transition: all 0.3s;
      background-color: #fff;
    }
    :hover {
      cursor: pointer; /* 显示小手指针 */
      transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
    }
  }
}
</style>
