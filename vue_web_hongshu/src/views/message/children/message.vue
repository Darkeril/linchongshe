<template>
  <div>
    <!-- 客服入口 -->
    <div class="customer-service-entry" @click="toCustomerService">
      <div class="service-avatar">
        <div class="avatar-item service-icon">💬</div>
      </div>
      <div class="service-info">
        <div class="service-title">
          <span>{{ $t("message.customerService") }}</span>
          <span class="service-tag">{{ $t("message.online") }}</span>
        </div>
        <div class="service-desc">{{ $t("message.contactServiceDesc") }}</div>
      </div>
      <div class="service-arrow">
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
    
    <ul class="message-container">
      <li class="message-item" v-for="(item, index) in dataList" :key="index">
        <a class="user-avatar">
          <!-- https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png -->
          <img class="avatar-item" :src="item.avatar" @click="toUser(item.uid)" />
        </a>
        <div class="main">
          <div class="info">
            <div class="user-info">
              <a class>{{ item.username }}</a>
              <div class="interaction-hint">
                <span>{{ item.time }}</span>
              </div>
            </div>

            <div class="interaction-content" @click="toChat(item.uid, index)">
              <span>{{ formatMessageContent(item.content) }}</span>
              <div class="msg-count" v-show="item.count > 0">{{ item.count }}</div>
            </div>
          </div>
        </div>
      </li>
    </ul>
    <Chat
      v-if="chatShow"
      :acceptUid="acceptUid"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-chat="close"
    ></Chat>
  </div>
</template>

<script lang="ts" setup>
import { useImStore } from "@/store/imStore";
import { ref, watchEffect } from "vue";
import { formateTime } from "@/utils/util";
import Chat from "@/components/Chat.vue";
import { clearMessageCount } from "@/api/im";
import { useRouter } from "vue-router";
import { ArrowRight } from "@element-plus/icons-vue";
import { CUSTOMER_SERVICE } from "@/utils/constants";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const router = useRouter();
const imStore = useImStore();
const dataList = ref<Array<any>>([]);
const chatShow = ref(false);
const acceptUid = ref("");

// 格式化消息内容（与uniapp端formatLastMessage逻辑一致，不使用msgType字段）
const formatMessageContent = (content: string) => {
  // 只通过内容判断，不使用msgType字段（与uniapp端保持一致）
  if (typeof content === 'string') {
    // 语音 URL（mp3/m4a/aac/wav/ogg），统一显示为 [语音]
    if (content.match(/\.(mp3|m4a|aac|wav|ogg)(\?|$)/i)) {
      return t("common.voice");
    }
    // 检查是否是图片URL（匹配扩展名或前缀）
    if (content.match(/\.(jpg|jpeg|png|gif|webp|bmp)/i) ||
        content.startsWith('https://image.') ||
        content.startsWith('http://image.')) {
      return t("common.image");
    }
    // 检查是否是视频URL（匹配扩展名或前缀）
    if (content.match(/\.(mp4|mov|avi|webm)/i) ||
        content.startsWith('https://video.') ||
        content.startsWith('http://video.')) {
      return t("common.video");
    }
    // 检查是否包含img标签
    if (content.includes('<img')) {
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
  dataList.value = [];
  const _countMessage = imStore.countMessage;
  _countMessage.chatCount = 0;
  imStore.userList.forEach((item) => {
    item.time = formateTime(item.timestamp);
    _countMessage.chatCount += item.count;
    dataList.value.push(item);
  });
  imStore.setCountMessage(_countMessage);
});

const toChat = (uid: string, index: number) => {
  const _countMessage = imStore.countMessage;
  clearMessageCount(uid, 3, "").then(() => {
    const chatCount = dataList.value[index].count;
    _countMessage.chatCount -= chatCount;
    dataList.value[index].count = 0;
    imStore.setCountMessage(_countMessage);
    acceptUid.value = uid;
    chatShow.value = true;
  });
};

const close = (uid: string) => {
  const index = dataList.value.findIndex((item) => item.uid === uid);
  const _countMessage = imStore.countMessage;
  clearMessageCount(uid, 3, "").then(() => {
    const chatCount = dataList.value[index].count;
    _countMessage.chatCount -= chatCount;
    dataList.value[index].count = 0;
    imStore.setCountMessage(_countMessage);
    chatShow.value = false;
  });
};

// 跳转到客服聊天
const toCustomerService = () => {
  acceptUid.value = CUSTOMER_SERVICE.USER_ID;
  chatShow.value = true;
};
</script>

<style lang="less" scoped>
// 客服入口样式
.customer-service-entry {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }
  
  .service-avatar {
    margin-right: 16px;
    flex-shrink: 0;
    
      .avatar-item {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        border: 2px solid rgba(255, 255, 255, 0.3);
        object-fit: cover;
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .service-icon {
        font-size: 28px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
  }
  
  .service-info {
    flex: 1;
    
    .service-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      
      span:first-child {
        font-size: 18px;
        font-weight: 600;
        color: #fff;
      }
      
      .service-tag {
        font-size: 12px;
        padding: 2px 8px;
        background: rgba(255, 255, 255, 0.3);
        border-radius: 12px;
        color: #fff;
      }
    }
    
    .service-desc {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
  
  .service-arrow {
    color: #fff;
    font-size: 20px;
  }
}

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
        border: 1px solid rgba(0, 0, 0, 0.08);
        object-fit: cover;
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
        }
      }
    }
  }
}
</style>
