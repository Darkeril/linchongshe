<template>
  <div>
    <ul class="message-container">
      <li class="message-item" v-for="(item, index) in dataList" :key="index">
        <a class="user-avatar">
          <img class="avatar-item" :src="item.avatar" @click="toUser(item.uid)" />
        </a>
        <div class="main">
          <div class="info">
            <div class="user-info">
              <a>{{ item.username }}</a>
              <div class="interaction-hint">
                <span>{{ item.time }}</span>
              </div>
            </div>

            <!-- 添加商品信息展示 -->
            <div class="product-info" @click="toProduct(item.productId)">
              <img :src="item.productImage" class="product-image" />
              <div class="product-detail">
                <span class="product-name">{{ item.productName }}</span>
                <span class="product-price">¥{{ item.price }}</span>
              </div>
            </div>

            <div class="interaction-content" @click="toChat(item.uid, item.productId, index)">
              <span>{{ formatMessageContent(item.content) }}</span>
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
      <!-- 商品信息头部 -->
      <div class="product-info-header">
        <div class="product-brief">
          <el-image :src="productInfo.imgList[0]" fit="cover" class="product-image" />
          <div class="product-detail">
            <div class="price">
              <span class="symbol">￥</span>
              <span class="amount">{{ convert.to_price(productInfo.price) }}</span>
              <span
                style="
                  margin-left: 5px;
                  font-size: 12px;
                  color: #9e9e9e;
                  font-weight: normal;
                  text-decoration: line-through;
                "
              >
                ￥{{ convert.to_price(productInfo.originalPrice) }}
              </span>
            </div>
            <div class="title">{{ productInfo.title }}</div>
            <div class="post-type">
              <span class="post" v-if="productInfo.postType === 0">
                {{ $t("message.shippingMethodPrefix") }}{{ $t("common.mail") }}
              </span>
              <span class="post" v-if="productInfo.postType === 1">
                {{ $t("message.shippingMethodPrefix") }}{{ $t("common.selfPickup") }}
              </span>
            </div>
            <span class="location">
              {{ productInfo.province }}
              {{ productInfo.city }}
            </span>
          </div>
          <div class="buy">
            <el-button
              type="primary"
              round
              @click="purchase(productInfo.id)"
              v-if="productInfo.uid != userStore.getUserInfo().id && productInfo.status === 1"
            >
              {{ $t("common.buy") }}
            </el-button>
          </div>
        </div>
        <el-divider />
      </div>
      <!-- 聊天组件 -->
      <ProductChat
        :key="`chat-${productId}`"
        :acceptUid="acceptUid"
        :productId="productId"
        class="chat-component animate__animated animate__fadeIn"
        @click-chat="chatShow = false"
        @refresh-list="fetchMessages"
      />
    </el-drawer>
    <IdleMain
      v-show="mainShow"
      :pid="pid"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="close"
    />
    <div v-if="dataList.length === 0" class="empty-state">
      <el-empty :description="$t('common.noMessage')"> </el-empty>
    </div>
  </div>
</template>


<script lang="ts" setup>
import { useImStore } from "@/store/imStore";
import { onMounted, ref, watchEffect } from "vue";
import { formateTime } from "@/utils/util";
import ProductChat from "@/components/ProductChat.vue";
import { getProductChatMessages, clearMessageCount } from "@/api/im";
import { getProductById } from "@/api/idle";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import convert from "@/utils/convert";
import type { ProductInfo } from "@/type/product";
import IdleMain from "@/views/idle-main/main.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const userStore = useUserStore();
const router = useRouter();
const imStore = useImStore();
const dataList = ref<Array<any>>([]);
const chatShow = ref(false);
const acceptUid = ref("");
const productId = ref("");
const loading = ref(false);
const mainShow = ref(false);
const pid = ref("");

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

const productInfo = ref<ProductInfo>({
  id: "",
  uid: "",
  title: "",
  description: "",
  type: 0,
  productType: 1,
  image: "",
  cover: "",
  urls: "",
  imgList: [],
  price: 0,
  originalPrice: 0,
  postType: 0,
  province: "",
  city: "",
  district: "",
  address: "",
  status: 0,
  username: "",
  avatar: "",
  time: "",
  likeCount: 0,
  collectionCount: 0,
  commentCount: 0,
  isCollection: false,
  isFollow: false,
  isLike: false,
  pinned: "",
  auditStatus: "",
});

const handleDrawerClose = async () => {
  await fetchMessages();
};

const fetchMessages = async () => {
  try {
    loading.value = true;
    const res = await getProductChatMessages();
    imStore.setProductChatList(res.data);
  } catch (error) {
    console.error("获取聊天消息失败:", error);
  } finally {
    loading.value = false;
  }
};

const close = () => {
  mainShow.value = false;
};

// 获取商品详情
const fetchProductDetail = async (pid: string) => {
  try {
    getProductById(pid).then((res: any) => {
      productInfo.value = res.data;
      productInfo.value.imgList = JSON.parse(res.data.urls);
      productInfo.value.time = formateTime(res.data.time);
    });
  } catch (error) {
    console.error("获取商品详情失败:", error);
  }
};

const purchase = (productId: string) => {
  chatShow.value = true;
  router.push({ name: "orderCreate", query: { productId: productId } });
};

const toUser = (uid: string) => {
  router.push({ name: "user", query: { uid: uid } });
};

const toProduct = (productId: string) => {
  pid.value = productId;
  mainShow.value = true;
};

watchEffect(() => {
  dataList.value = [];
  const countMessage = imStore.countMessage;
  countMessage.productChatCount = 0;
  imStore.productChatList.forEach((item) => {
    item.time = formateTime(item.timestamp);
    countMessage.productChatCount += item.count;
    dataList.value.push(item);
  });
  imStore.setCountMessage(countMessage);
});

const toChat = async (uid: string, pid: string, index: number) => {
  const countMessage = imStore.countMessage;
  try {
    await clearMessageCount(uid, 4, pid);
    const chatCount = dataList.value[index].count;
    countMessage.productChatCount -= chatCount;
    dataList.value[index].count = 0;
    imStore.setCountMessage(countMessage);
    acceptUid.value = uid;
    productId.value = pid;
    // 获取商品详情
    await fetchProductDetail(pid);
    chatShow.value = true;
  } catch (error) {
    console.error("打开聊天失败:", error);
  }
};

const fetchProductMessages = async () => {
  try {
    loading.value = true;
    const res = await getProductChatMessages();
    // 更新store中的消息列表
    imStore.setProductChatList(res.data);
  } catch (error) {
    console.error("获取商品聊天消息失败:", error);
  } finally {
    loading.value = false;
  }
};

// 使用WebSocket监听新消息
const handleNewMessage = (message: any) => {
  const countMessage = imStore.countMessage;
  const index = dataList.value.findIndex(
    (item) => item.uid === message.fromUid && item.productId === message.productId
  );

  if (index > -1) {
    dataList.value[index].content = message.content;
    dataList.value[index].timestamp = message.timestamp;
    dataList.value[index].count += 1;
    countMessage.productChatCount += 1;
  } else {
    dataList.value.unshift({
      uid: message.fromUid,
      username: message.fromUsername,
      avatar: message.fromAvatar,
      content: message.content,
      timestamp: message.timestamp,
      count: 1,
      productId: message.productId,
      productName: message.productName,
      productImage: message.productImage,
      price: message.price,
    });
    countMessage.productChatCount += 1;
  }
  imStore.setCountMessage(countMessage);
};

// 监听WebSocket消息
watchEffect(() => {
  if (imStore.socket) {
    imStore.socket.on("product_message", handleNewMessage);
  }

  return () => {
    if (imStore.socket) {
      imStore.socket.off("product_message", handleNewMessage);
    }
  };
});

onMounted(() => {
  fetchProductMessages();
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

        .product-info {
          display: flex;
          align-items: center;
          padding: 12px;
          background: #f8f8f8;
          border-radius: 8px;
          margin-bottom: 12px;
          cursor: pointer;

          .product-image {
            width: 60px;
            height: 60px;
            border-radius: 4px;
            object-fit: cover;
            margin-right: 12px;
          }

          .product-detail {
            display: flex;
            flex-direction: column;

            .product-name {
              font-size: 14px;
              color: #333;
              margin-bottom: 4px;
              // 文本超出显示省略号
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
            }

            .product-price {
              font-size: 14px;
              color: #ff4d4f;
              font-weight: 600;
            }
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

.product-info-header {
  padding: 20px;
  background: #fff;

  .product-brief {
    display: flex;
    gap: 15px;

    .product-image {
      width: 100px;
      height: 100px;
      border-radius: 8px;
      object-fit: cover;
    }

    .product-detail {
      flex: 1;

      .price {
        color: #ff5d50;
        margin-bottom: 8px;

        .symbol {
          font-size: 14px;
        }

        .amount {
          font-size: 18px;
          font-weight: bold;
        }
      }

      .title {
        font-size: 15px;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .post-type {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 10px;
        margin-bottom: 5px;

        .post {
          font-size: 13px;
          line-height: 120%;
          color: rgba(51, 51, 51, 0.6);
        }
      }

      .location {
        font-size: 13px;
        line-height: 120%;
        color: rgba(51, 51, 51, 0.6);
      }
    }

    .buy {
      margin-top: 65px;
      margin-right: 35px;
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
