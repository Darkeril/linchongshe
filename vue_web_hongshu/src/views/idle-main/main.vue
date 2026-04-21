<template>
  <div
    class="note-detail-mask"
    style="transition: background-color 0.4s ease 0s; background-color: hsla(0, 0%, 100%, 0.98)"
    @click="close"
  >
    <div class="note-container" @click.stop>
      <div class="media-container" style="position: relative">
        <!-- 根据 type 判断是图片轮播还是视频播放器 -->
        <el-carousel
          v-if="productInfo.productType === 1"
          height="90vh"
          :autoplay="false"
          :arrow="productInfo.imgList && productInfo.imgList.length > 1 ? 'hover' : 'never'"
          @change="handleProdCarouselChange"
        >
          <el-carousel-item v-for="(item, index) in productInfo.imgList" :key="index">
            <el-image
              :src="getNginxProxyImageUrl(item)"
              fit="contain"
              class="image-item animate__animated animate__zoomIn"
            />
          </el-carousel-item>
        </el-carousel>
        <!-- 图片数量角标（仅多图显示） -->
        <div v-if="productInfo.imgList && productInfo.imgList.length > 1" class="img-counter-badge">
          {{ prodCarouselIndex + 1 }}/{{ productInfo.imgList.length }}
        </div>
        <!-- 视频播放部分 -->
        <div v-if="productInfo.productType === 2" class="video-player">
          <video
            v-if="productInfo.imgList.length > 0"
            ref="video"
            class="video"
            controls
            :src="productInfo.imgList[0]"
            @pause="handlePause"
            @play="handlePlay"
            style="width: 100%; height: 100%"
          >
            Your browser does not support the video tag.
          </video>
          <!-- 暂停符号 -->
          <div v-if="isPaused" class="pause-overlay" @click="handlePlayClick">
            <svg
              t="1734061569758"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="9973"
              width="100"
              height="100"
            >
              <path
                d="M780.8 475.733333L285.866667 168.533333c-27.733333-17.066667-64 4.266667-64 36.266667v614.4c0 32 36.266667 53.333333 64 36.266667l492.8-307.2c29.866667-14.933333 29.866667-57.6 2.133333-72.533334z"
                fill="#ffffff"
                p-id="9974"
              ></path>
            </svg>
          </div>
        </div>
      </div>

      <div class="interaction-container">
        <div class="author-container">
          <div class="author-me">
            <div class="info" @click="toUser(productInfo.uid)">
              <img class="avatar-item" style="width: 40px; height: 40px" :src="productInfo.avatar" />
              <span class="name">{{ productInfo.username }}</span>
            </div>
            <div class="follow-btn" v-if="currentUid !== productInfo.uid">
              <el-button
                plain
                type="primary"
                size="large"
                round
                v-if="productInfo.isFollow"
                @click="follow(productInfo.uid, 1)"
              >
                {{ $t("common.followed") }}
              </el-button>
              <el-button type="primary" size="large" round v-else @click="follow(productInfo.uid, 0)">{{
                $t("common.follow")
              }}</el-button>
            </div>
            <div class="follow-btn" v-else>
              <el-dropdown>
                <el-button plain type="primary" size="large" round>
                  {{ $t("common.edit") }}
                  <el-icon class="el-icon--right">
                    <arrow-down />
                  </el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="productInfo.pinned === '0'" @click="pinned(productInfo.id, '1')">
                      <el-icon><Top /></el-icon>
                      {{ $t("common.pinned") }}
                    </el-dropdown-item>
                    <el-dropdown-item v-else @click="pinned(productInfo.id, '0')">
                      <el-icon><Bottom /></el-icon>
                      {{ $t("common.unpin") }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="toEdit()" v-if="productInfo.id">
                      <el-icon><Edit /></el-icon>
                      {{ $t("common.edit") }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteProduct(productInfo.id)">
                      <el-icon><Delete /></el-icon>
                      {{ $t("common.delete") }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="note-scroller" ref="noteScroller" @scroll="loadMoreData">
            <div class="note-content">
              <div class="title">{{ productInfo.title }}</div>
              <div class="desc">
                <span v-html="formatContentWithHashtags(productInfo.description)"></span>
                <br />
              </div>
              <div class="post-container">
                <span class="post" v-if="productInfo.postType === 0"
                  >{{ $t("note.deliveryMethod") }}：{{ $t("common.mail") }}
                </span>
                <span class="post" v-if="productInfo.postType === 1"
                  >{{ $t("note.deliveryMethod") }}：{{ $t("common.selfPickup") }}
                </span>
              </div>
              <div class="price-container">
                <div class="post">
                  <span style="font-weight: bold; color: red; font-size: 13px">
                    ￥
                    <span style="font-size: 16px">
                      {{ convert.to_price(productInfo.price) }}
                    </span>
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
                  </span>
                </div>
              </div>
              <div class="bottom-container">
                <div class="bottom-left">
                  <span class="date">{{ productInfo.time }}</span>
                  <span class="location" v-if="formatAddress(productInfo)">
                    <el-icon><Location /></el-icon>
                    <span class="location-text">{{ formatAddress(productInfo) }}</span>
                  </span>
                </div>
                <div class="bottom-right">
                  <el-button
                    type="primary"
                    round
                    @click="chatShow = true"
                    v-if="
                      userStore.getUserInfo()?.id &&
                      productInfo.uid != userStore.getUserInfo()?.id &&
                      productInfo.status === 0
                    "
                  >
                    {{ $t("common.wantIt") }}
                  </el-button>
                </div>
              </div>
            </div>
            <div class="divider interaction-divider"></div>

            <!-- 评论 -->
            <div class="comments-el">
              <Comment
                :productId="props.pid"
                :currentPage="currentPage"
                :replyComment="replyComment"
                :seed="seed"
                @click-comment="clickComment"
              >
              </Comment>
            </div>
          </div>

          <div class="interactions-footer">
            <div class="buttons">
              <div class="left">
                <span class="like-wrapper like-active">
                  <span class="like-lottie" v-if="productInfo.isCollection" @click="collection(productInfo.id, 1, -1)">
                    <svg
                      t="1739470033309"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="19111"
                      width="20"
                      height="20"
                    >
                      <path
                        d="M428.6976 107.3152c-6.5024 72.192-36.352 207.2576-160.256 337.408 3.6864-48.0256-7.1168-83.7632-19.0464-107.6736-6.6048-13.1584-26.0608-10.5984-28.8768 3.84-5.7344 29.44-20.5824 75.0592-57.6 137.7792-71.6288 121.3952-62.5664 459.8784 340.736 459.8784s430.4384-352.8192 373.1456-496.0256c-37.376-93.44-93.952-152.5248-128.8192-182.3232-11.4176-9.7792-29.1328-1.9456-29.5936 13.056-0.9216 30.464-7.3216 73.3696-33.0752 102.144-0.6656-52.7872-38.144-208.384-202.4448-296.8576-23.296-12.544-51.7632 2.4576-54.1696 28.7744z"
                        fill="#FF5D50"
                        p-id="19112"
                      ></path>
                      <path
                        d="M702.2592 678.4c-4.1984-45.056-60.672-166.5536-212.6336-246.4256-10.5984-5.5808-23.0912 3.1232-21.504 15.0016 6.2464 46.848 12.9536 140.4928-24.064 184.7296 4.0448-40.3968-18.1248-73.8304-36.6592-94.3104-8.3968-9.216-23.552-4.6592-25.4976 7.68-3.5328 22.3232-12.8512 56.2688-36.5568 97.9456-42.0864 74.0352-86.9888 188.672 124.5696 294.656 10.9568 0.5632 22.1696 0.8704 33.7408 0.8704 11.2128 0 22.0672-0.3072 32.7168-0.8704 158.2592-59.4944 173.4656-177.9712 165.888-259.2768z"
                        fill="#FFDF99"
                        p-id="19113"
                      ></path>
                    </svg>
                  </span>
                  <span class="like-lottie" v-else @click="collection(productInfo.id, 1, 1)">
                    <svg
                      t="1739864213490"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="2122"
                      width="20"
                      height="20"
                    >
                      <path
                        d="M428.6976 107.3152c-6.5024 72.192-36.352 207.2576-160.256 337.408 3.6864-48.0256-7.1168-83.7632-19.0464-107.6736-6.6048-13.1584-26.0608-10.5984-28.8768 3.84-5.7344 29.44-20.5824 75.0592-57.6 137.7792-71.6288 121.3952-62.5664 459.8784 340.736 459.8784s430.4384-352.8192 373.1456-496.0256c-37.376-93.44-93.952-152.5248-128.8192-182.3232-11.4176-9.7792-29.1328-1.9456-29.5936 13.056-0.9216 30.464-7.3216 73.3696-33.0752 102.144-0.6656-52.7872-38.144-208.384-202.4448-296.8576-23.296-12.544-51.7632 2.4576-54.1696 28.7744z"
                        fill="#cdcdcd"
                        p-id="2123"
                        data-spm-anchor-id="a313x.search_index.0.i1.26253a81VGicUs"
                        class="selected"
                      ></path>
                      <path
                        d="M702.2592 678.4c-4.1984-45.056-60.672-166.5536-212.6336-246.4256-10.5984-5.5808-23.0912 3.1232-21.504 15.0016 6.2464 46.848 12.9536 140.4928-24.064 184.7296 4.0448-40.3968-18.1248-73.8304-36.6592-94.3104-8.3968-9.216-23.552-4.6592-25.4976 7.68-3.5328 22.3232-12.8512 56.2688-36.5568 97.9456-42.0864 74.0352-86.9888 188.672 124.5696 294.656 10.9568 0.5632 22.1696 0.8704 33.7408 0.8704 11.2128 0 22.0672-0.3072 32.7168-0.8704 158.2592-59.4944 173.4656-177.9712 165.888-259.2768z"
                        fill="#FFDF99"
                        p-id="2124"
                      ></path>
                    </svg>
                  </span>
                  <span class="count">{{ productInfo.likeCount }}</span>
                </span>

                <span class="chat-wrapper">
                  <span class="like-lottie">
                    <ChatRound style="width: 0.8em; height: 0.8em; color: #333" />
                  </span>
                  <span class="count">
                    {{ productInfo.commentCount }}
                  </span>
                </span>
              </div>
              <div class="share-wrapper"></div>
            </div>
            <div :class="showSaveBtn ? 'comment-wrapper active comment-comp ' : 'comment-wrapper comment-comp '">
              <div class="input-wrapper">
                <input
                  class="comment-input"
                  v-model="commentValue"
                  type="text"
                  :placeholder="commentPlaceVal"
                  @input="commenInput"
                  @keyup.enter="saveComment"
                />
                <div class="input-buttons" @click="clearCommeent" v-show="showSaveBtn">
                  <Close style="width: 1.2em; height: 1.2em" />
                </div>
              </div>
              <button class="submit" @click="saveComment">{{ $t("common.send") }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-drawer v-model="chatShow" direction="rtl" size="30%" :with-header="false" class="chat-drawer">
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
              <span class="post" v-if="productInfo.postType === 0"
                >{{ $t("note.deliveryMethod") }}：{{ $t("common.mail") }}
              </span>
              <span class="post" v-if="productInfo.postType === 1"
                >{{ $t("note.deliveryMethod") }}：{{ $t("common.selfPickup") }}
              </span>
            </div>
            <span class="location" v-if="formatAddress(productInfo)">
              <el-icon><Location /></el-icon>
              <span class="location-text">{{ formatAddress(productInfo) }}</span>
            </span>
          </div>
          <div class="buy">
            <el-button
              type="primary"
              round
              @click="purchase(productInfo.id)"
              v-if="currentUser?.id && productInfo.uid != currentUser?.id && productInfo.status === 0"
            >
              {{ $t("common.buy") }}
            </el-button>
          </div>
        </div>
        <el-divider />
      </div>
      <!-- 聊天组件 -->
      <ProductChat
        :key="`chat-${productInfo.id}`"
        :acceptUid="productInfo.uid"
        :productId="productInfo.id"
        class="chat-component animate__animated animate__fadeIn"
        @click-chat="chatShow = false"
      />
    </el-drawer>

    <el-drawer
      v-model="editDrawerVisible"
      :title="$t('idle.editProduct')"
      size="40%"
      :before-close="handleEditClose"
      destroy-on-close
      @close="onDrawerClose"
    >
      <EditIdleForm
        v-if="editDrawerVisible"
        :product-data="productInfo"
        :loading="saveLoading"
        @save="handleSaveProduct"
        @cancel="handleCancel"
        @change="(val) => (hasUnsavedChanges = val)"
      />
    </el-drawer>

    <div class="close-cricle" @click="close">
      <div class="close close-mask-white">
        <Close style="width: 1.2em; height: 1.2em; color: rgba(51, 51, 51, 0.8)" />
      </div>
    </div>
    <div class="back-desk"></div>
  </div>
</template>

<script lang="ts" setup>
import { Top, Bottom, Edit, Delete, Close, ChatRound, ArrowDown, Location } from "@element-plus/icons-vue";
import { ElLoading, ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import {
  getProductById,
  pinnedProduct,
  updateProductByDTO,
  deleteProductByIds,
  getPaymentGlobalConfig,
} from "@/api/idle";
import type { ProductInfo } from "@/type/product";
import { formateTime, getRandomString, getNginxProxyImageUrl } from "@/utils/util";
import { followById } from "@/api/follower";
import Comment from "@/components/Comment.vue";
import type { CommentDTO } from "@/type/comment";
import { saveCommentByDTO, syncCommentByIds } from "@/api/comment";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/userStore";
import convert from "@/utils/convert";
import type { CollectionDTO } from "@/type/collection";
import { collectionByDTO } from "@/api/collection";
import ProductChat from "@/components/ProductChat.vue";
import EditIdleForm from "@/components/EditIdleForm.vue";

// 修改编辑相关的代码
const editDrawerVisible = ref(false);
const isEditing = ref(false);
const hasUnsavedChanges = ref(false);
const saveLoading = ref(false);

const userStore = useUserStore();
const router = useRouter();
const { t } = useI18n();
const currentUser = computed(() => userStore.getUserInfo());

const emit = defineEmits(["clickMain"]);

const props = defineProps({
  pid: {
    type: String,
    default: "",
  },
  nowTime: {
    type: Date,
    default: null,
  },
  editMode: {
    type: Boolean,
    default: false,
  },
});

const currentUid = ref("");
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
  pinned: "0",
  username: "",
  avatar: "",
  time: "",
  likeCount: 0,
  collectionCount: 0,
  commentCount: 0,
  isCollection: false,
  isFollow: false,
  isLike: false,
  auditStatus: "",
});
const commentValue = ref("");
const commentPlaceVal = ref(t("common.inputContent"));
const commentObject = ref<any>({});
const replyComment = ref<any>({});
const showSaveBtn = ref(false);
const currentPage = ref(1);
const seed = ref("");
const commentIds = ref<Array<string>>([]);
const noteScroller = ref(null);
const isLogin = ref(false);
const likeOrComment = ref({
  isLike: false,
  isComment: false,
  isCollection: false,
});
const isPaused = ref(false);
const video = ref(); // 获取 video 元素的引用
const productList = ref<Array<any>>([]);
const chatShow = ref(false);
const prodCarouselIndex = ref(0);

watch(
  () => [props.nowTime, props.editMode],
  () => {
    currentPage.value = 1;
    if (props.pid !== null && props.pid !== "") {
      getProductById(props.pid).then((res: any) => {
        productInfo.value = res.data;
        try {
          productInfo.value.imgList = res.data?.urls ? JSON.parse(res.data.urls) : [];
        } catch {
          productInfo.value.imgList = [];
        }
        productInfo.value.time = formateTime(res.data.time);
        likeOrComment.value.isLike = productInfo.value.isLike;
        // 只有数据加载完再判断 editMode
        if (props.editMode) {
          toEdit();
        }
      });
    }
  }
);

// 视频暂停
const handlePause = () => {
  isPaused.value = true;
};

// 视频播放
const handlePlay = () => {
  isPaused.value = false;
};

// 点击暂停符号时继续播放
const handlePlayClick = () => {
  video.value.play(); // 播放视频
  isPaused.value = false; // 更新暂停状态
};

const noLoginNotice = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.notLoggedIn"));
    return false;
  }
  return true;
};

const purchase = async (productId: string) => {
  // 检查支付功能是否可用
  try {
    const res = (await getPaymentGlobalConfig()) as any;
    if (res.code === 200 && res.data) {
      // 检查全局支付开关
      if (!res.data.paymentEnabled) {
        ElMessage.warning(t("idle.paymentDisabledCannotBuy"));
        return; // 阻止跳转到确认订单页面
      }

      // 检查是否有可用的支付方式
      const hasAvailablePayment =
        res.data.wechatQrcodeEnabled || res.data.alipaySandboxEnabled || res.data.alipayQrcodeEnabled;

      if (!hasAvailablePayment) {
        ElMessage.warning(t("idle.noPaymentMethodCannotBuy"));
        return; // 阻止跳转到确认订单页面
      }
    }
  } catch (error) {
    console.error("check payment config failed:", error);
    // 检查失败时仍允许进入，由确认订单页面再次检查
  }

  chatShow.value = true;
  router.push({ name: "orderCreate", query: { productId: productId } });
};

const toUser = (uid: string) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  router.push({ name: "user", query: { uid: uid } });
};

// 格式化内容，处理#标签高亮和@用户高亮（只显示不跳转）
const formatContentWithHashtags = (content: string) => {
  if (!content) return content;

  let processedContent = content;

  // 处理@用户高亮（先处理@用户，避免与#标签冲突）
  const mentionRegex = /@([^\s@#<>]+)/g;
  processedContent = processedContent.replace(mentionRegex, (match, _userName, offset, string) => {
    // 检查是否在HTML标签属性内
    const beforeMatch = string.substring(0, offset);
    const lastTagStart = beforeMatch.lastIndexOf("<");
    const lastTagEnd = beforeMatch.lastIndexOf(">");

    // 如果在未闭合的标签内，检查是否在属性值中
    if (lastTagStart > lastTagEnd) {
      const tagContent = string.substring(lastTagStart, offset + match.length);
      const beforeInTag = tagContent.substring(0, offset - lastTagStart);
      const singleQuotes = (beforeInTag.match(/'/g) || []).length;
      const doubleQuotes = (beforeInTag.match(/"/g) || []).length;
      // 如果引号数量是奇数，说明在属性值内，跳过
      if (singleQuotes % 2 === 1 || doubleQuotes % 2 === 1) {
        return match;
      }
    }
    return `<span style="color: #1890ff;">${match}</span>`;
  });

  // 处理#标签高亮（只显示蓝色，不跳转）
  // 注意：需要避免匹配HTML标签内的#（如style="color: #1890ff;"）
  let lastIndex = 0;
  let result = "";
  const hashtagRegex = /#([^\s#<>]+)/g;
  let match;

  while ((match = hashtagRegex.exec(processedContent)) !== null) {
    const matchStart = match.index;
    const matchEnd = matchStart + match[0].length;

    // 添加匹配前的文本
    const beforeText = processedContent.substring(lastIndex, matchStart);
    result += beforeText;

    // 检查是否在HTML标签属性内
    const beforeMatch = processedContent.substring(0, matchStart);
    const lastTagStart = beforeMatch.lastIndexOf("<");
    const lastTagEnd = beforeMatch.lastIndexOf(">");

    let shouldSkip = false;

    // 如果在未闭合的标签内
    if (lastTagStart > lastTagEnd) {
      const tagContent = processedContent.substring(lastTagStart, matchEnd);
      // 检查是否在引号内（属性值中）
      const beforeInTag = tagContent.substring(0, matchStart - lastTagStart);
      const singleQuotes = (beforeInTag.match(/'/g) || []).length;
      const doubleQuotes = (beforeInTag.match(/"/g) || []).length;
      // 如果引号数量是奇数，说明在属性值内
      if (singleQuotes % 2 === 1 || doubleQuotes % 2 === 1) {
        shouldSkip = true;
      }
    }

    // 检查是否是十六进制颜色代码
    if (!shouldSkip && /^[0-9a-fA-F]{6}$/i.test(match[1])) {
      shouldSkip = true;
    }

    if (shouldSkip) {
      result += match[0]; // 保持原样
    } else {
      // 检查#标签前面是否有空格，如果没有就添加一个空格
      const beforeText = processedContent.substring(lastIndex, matchStart);
      const lastChar =
        beforeText.length > 0
          ? beforeText[beforeText.length - 1]
          : lastIndex > 0
          ? processedContent[lastIndex - 1]
          : " ";
      const needSpace = lastChar !== " " && lastChar !== "\n" && lastChar !== "\t" && lastIndex !== 0;
      const space = needSpace ? " " : "";
      // 只显示蓝色样式，不添加链接（只显示不跳转）
      result += `${space}<span style="color: #1890ff;">${match[0]}</span>`;
    }

    lastIndex = matchEnd;
  }

  // 添加剩余的文本
  result += processedContent.substring(lastIndex);

  return result;
};

// 格式化地址显示（只显示市+详细地址，过长时缩略）
const formatAddress = (item: any): string => {
  if (!item) return "";
  const parts = [];
  // 优先显示城市
  if (item.city && item.city.trim()) {
    parts.push(item.city);
  }
  // 然后显示详细地址
  if (item.address && item.address.trim()) {
    parts.push(item.address);
  }
  // 如果都没有数据，返回空字符串
  const fullAddress = parts.length > 0 ? parts.join(" ") : "";
  // 如果地址长度超过12个字符，则截断并添加省略号
  return fullAddress.length > 12 ? fullAddress.substring(0, 12) + "..." : fullAddress;
};

const close = () => {
  if (isLogin.value) {
    syncCommentByIds(commentIds.value).then(() => {
      commentIds.value = [];
      emit("clickMain", props.pid, likeOrComment.value);
    });
    if (video.value && productInfo.value.productType === 2) {
      video.value.pause();
    }
  } else {
    emit("clickMain");
  }
};

const follow = (fid: string, type: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  followById(fid).then(() => {
    productInfo.value.isFollow = type == 0;
  });
};

const collection = async (pid: string, type: number, val: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  try {
    // 保存当前的重要信息
    const currentImgList = [...productInfo.value.imgList];
    const currentTime = productInfo.value.time; // 保存格式化后的时间

    const res = await getProductById(pid);
    productInfo.value = res.data;

    // 恢复保存的信息
    productInfo.value.imgList = currentImgList;
    productInfo.value.time = currentTime; // 恢复格式化后的时间

    const collectionDTO = {} as CollectionDTO;
    collectionDTO.collectionId = productInfo.value.id;
    collectionDTO.publishUid = productInfo.value.uid;
    collectionDTO.type = type == 1 ? 1 : 3;

    await collectionByDTO(collectionDTO);
    productInfo.value.isCollection = val == 1;
    productInfo.value.likeCount += val;
    likeOrComment.value.isCollection = val == 1;

    // 同步更新 productList 中的对应项
    const productIndex = productList.value.findIndex((product) => product.id === pid);
    if (productIndex !== -1) {
      productList.value[productIndex].likeCount += val;
      productList.value[productIndex].isCollection = val == 1;
    }
  } catch (error) {
    console.error("collection failed:", error);
    ElMessage.error(t("common.collectFailed"));
  }
};

const pinned = (productId: string, type: string) => {
  pinnedProduct(productId)
    .then((res: any) => {
      if (res.data) {
        productInfo.value.pinned = type;
        ElMessage.success(type === "1" ? t("common.pinSuccess") : t("common.unpinSuccess"));
      } else {
        ElMessage.error(type === "1" ? t("common.pinFailed") : t("common.unpinFailed"));
      }
    })
    .catch((error) => {
      if (error.response?.data?.message) {
        ElMessage.warning(error.response.data.message);
      } else if (type === "1") {
        ElMessage.warning(t("common.maxPinProducts"));
      } else {
        ElMessage.error(type === "1" ? t("common.pinFailed") : t("common.unpinFailed"));
      }
    });
};

const deleteProduct = (productId: string) => {
  ElMessage.warning(t("common.noPermission"));
  const data = [] as Array<string>;
  data.push(productId);
  deleteProductByIds(data).then(() => {
    ElMessage.success(t("common.deleteSuccess"));
    emit("clickMain");
  });
};

// 打开编辑抽屉
const toEdit = () => {
  if (!checkEditPermission()) return;
  editDrawerVisible.value = true;
  isEditing.value = true;
};

// 编辑权限检查
const checkEditPermission = () => {
  if (!isLogin.value) {
    ElMessage.warning(t("common.pleaseLoginFirst"));
    return false;
  }
  if (productInfo.value.uid !== currentUid.value) {
    ElMessage.warning(t("common.noEditPermission"));
    return false;
  }
  return true;
};

// 处理保存
const handleSaveProduct = async (formData: FormData) => {
  const loading = ElLoading.service({
    lock: true,
    text: t("common.saving"),
    background: "rgba(0, 0, 0, 0.7)",
  });
  try {
    // 调用API上传数据和文件
    const res = (await updateProductByDTO(formData)) as any;
    if (res.code === 200) {
      // 更新本地数据
      const updatedNote = res.data;
      Object.assign(productInfo.value, updatedNote);
      editDrawerVisible.value = false;
      isEditing.value = false;
      hasUnsavedChanges.value = false;
      ElMessage.success(t("idle.productUpdateSuccess"));
      // 更新：保存成功后关闭详情页弹窗
      emit("clickMain");
      // 刷新当前页面
      setTimeout(() => {
        router.go(0);
      }, 300); // 延迟一点点，确保弹窗关闭动画能看到
    } else {
      throw new Error(res.data?.message || t("note.updateFailed"));
    }
  } catch (error: any) {
    ElMessage.error(error.message || t("note.updateFailed"));
  } finally {
    loading.close();
  }
};

const handleCancel = () => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(t("common.unsavedChanges"), {
      confirmButtonText: t("common.confirm"),
      cancelButtonText: t("common.cancel"),
      type: "warning",
    })
      .then(() => {
        editDrawerVisible.value = false;
        hasUnsavedChanges.value = false;
        // 只有未通过状态时才关闭 Main 弹窗
        if (productInfo.value.auditStatus === "2") {
          setTimeout(() => {
            emit("clickMain");
          }, 200);
        }
      })
      .catch(() => {});
  } else {
    editDrawerVisible.value = false;
    // 只有未通过状态时才关闭 Main 弹窗
    if (productInfo.value.auditStatus === "2") {
      setTimeout(() => {
        emit("clickMain");
      }, 200);
    }
  }
};

const onDrawerClose = () => {
  // 只有未通过状态时才关闭 Main 弹窗
  if (productInfo.value.auditStatus === "2") {
    setTimeout(() => {
      emit("clickMain");
    }, 200);
  }
};

// 处理抽屉关闭
const handleEditClose = async (done: () => void) => {
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm(t("common.unsavedClose"), {
        confirmButtonText: t("common.confirm"),
        cancelButtonText: t("common.cancel"),
        type: "warning",
      });
      isEditing.value = false;
      hasUnsavedChanges.value = false;
      done();
    } catch {
      // 用户取消关闭
    }
  } else {
    done();
  }
};

// 键盘快捷键（与笔记详情一致：ESC 关闭抽屉，Ctrl/Cmd+S 触发表单提交）
const handleKeyDown = (e: KeyboardEvent) => {
  if (isEditing.value) {
    if (e.key === "Escape") {
      editDrawerVisible.value = false;
    }
    if ((e.ctrlKey || e.metaKey) && e.key === "s") {
      e.preventDefault();
      const formEl = document.querySelector(".edit-note-form form");
      if (formEl) {
        formEl.dispatchEvent(new Event("submit"));
      }
    }
  }
};

const clickComment = (comment: any) => {
  commentObject.value = comment;
  commentPlaceVal.value = t("common.replyTo", { name: comment.username });
};

const commenInput = (e: any) => {
  const { value } = e.target;
  commentValue.value = value;
  showSaveBtn.value = commentValue.value.length > 0 || commentObject.value.pid !== undefined;
};

const saveComment = () => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  const comment = {} as CommentDTO;
  comment.productId = props.pid;
  comment.productUid = productInfo.value.uid;
  if (commentObject.value.pid === undefined) {
    comment.pid = "0";
    comment.replyId = "0";
    comment.replyUid = productInfo.value.uid;
    comment.level = 1;
  } else if (commentObject.value.pid == "0") {
    comment.pid = commentObject.value.id;
    comment.replyId = commentObject.value.id;
    comment.replyUid = commentObject.value.uid;
    comment.level = 2;
  } else {
    comment.pid = commentObject.value.pid;
    comment.replyId = commentObject.value.id;
    comment.replyUid = commentObject.value.uid;
    comment.level = 2;
  }

  comment.content = commentValue.value;
  saveCommentByDTO(comment).then((res: any) => {
    replyComment.value = res.data;
    replyComment.value.replyUsername = commentObject.value.username;
    commentValue.value = "";
    commentObject.value = {};
    commentPlaceVal.value = t("common.inputContent");
    showSaveBtn.value = false;
    seed.value = getRandomString(12);
    commentIds.value.push(res.data.id);
    likeOrComment.value.isComment = true;
  });
};

const clearCommeent = () => {
  commentValue.value = "";
  commentObject.value = {};
  commentPlaceVal.value = t("common.inputContent");
  showSaveBtn.value = false;
};

const loadMoreData = () => {
  const container = noteScroller.value as any;
  if (container.scrollTop + container.clientHeight >= container.scrollHeight) {
    currentPage.value += 1;
    console.log("scroll reached bottom");
  }
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    currentUid.value = userStore.getUserInfo().id;
  }
};

initData();

onMounted(() => {
  document.addEventListener("keydown", handleKeyDown);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeyDown);
});

const handleProdCarouselChange = (newIndex: number) => {
  prodCarouselIndex.value = newIndex;
};
</script>

<style lang="less" scoped>
.chat-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
    display: flex;
    flex-direction: column;
  }

  .product-info-header {
    padding: 10px;
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
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          line-height: 120%;
          color: #1890ff;
          background: none;
          padding: 0;
          border-radius: 0;
          border: none;
          font-weight: 500;
          margin-top: 8px;
          width: fit-content;

          .el-icon {
            font-size: 13px;
            color: #1890ff;
          }

          .location-text {
            color: #1890ff;
            font-weight: 500;
          }
        }
      }

      .buy {
        margin-top: 65px;
        margin-right: 10px;
      }
    }
  }

  .chat-component {
    flex: 1;
    overflow: hidden;
  }
}

// 移除原有的 feeds-tab-container 相关样式
.feeds-tab-container {
  display: none;
}

// .feeds-tab-container {
//   padding-left: 2rem;
// }

.image-item {
  width: 100%;
  height: 100%;
  object-fit: cover;
  animation-delay: 0.3s; /* 动画延迟 */
}

:deep(.el-dropdown-menu__item:not(.is-disabled):focus) {
  background-color: #f8f8f8;
  color: black;
}

.note-detail-mask {
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  width: 100vw;
  height: 100vh;
  z-index: 20;
  overflow: auto;

  .back-desk {
    position: fixed;
    background-color: #f4f4f4;
    opacity: 0.5;
    width: 100vw;
    height: 100vh;
    z-index: 30;
  }

  .close-cricle {
    left: 1.3vw;
    top: 1.3vw;
    position: fixed;
    display: flex;
    z-index: 100;
    cursor: pointer;

    .close-mask-white {
      box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.04), 0 1px 2px 0 rgba(0, 0, 0, 0.02);
      border: 1px solid rgba(0, 0, 0, 0.08);
    }

    .close {
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 100%;
      width: 40px;
      height: 40px;
      border-radius: 40px;
      cursor: pointer;
      transition: all 0.3s;
      background-color: #fff;
    }
    :hover {
      cursor: pointer; /* 显示小手指针 */
      transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
    }
  }

  .note-container {
    width: 65%;
    height: 86%;
    transition: transform 0.4s ease 0s, width 0.4s ease 0s;
    transform: translate(350px, 60px) scale(1);
    overflow: visible;

    display: flex;
    box-shadow: 0 8px 64px 0 rgba(0, 0, 0, 0.04), 0 1px 4px 0 rgba(0, 0, 0, 0.02);
    border-radius: 20px;
    background: #f8f8f8;
    transform-origin: left top;
    z-index: 100;

    .media-container {
      width: 65%;
      height: auto;

      position: relative;
      background: rgba(0, 0, 0, 0.03);
      flex-shrink: 0;
      flex-grow: 0;
      -webkit-user-select: none;
      user-select: none;
      overflow: hidden;
      border-radius: 20px 0 0 20px;
      transform: translateZ(0);
      height: 100%;
      object-fit: contain;
      min-width: 440px;

      .video-player {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%; /* 让容器宽度为 100% */
        height: 100%; /* 让容器高度占满整个视口 */
      }
      .pause-overlay {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: white;
        padding: 10px;
      }
    }

    .interaction-container {
      width: 35%;
      flex-shrink: 0;
      border-radius: 0 20px 20px 0;
      position: relative;
      display: flex;
      flex-direction: column;
      flex-grow: 1;
      height: 100%;
      background-color: #fff;
      overflow: hidden;
      border-left: 1px solid rgba(0, 0, 0, 0.08);

      .author-me {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        padding: 24px;
        border-bottom: 1px solid transparent;

        .info {
          display: flex;
          align-items: center;

          .avatar-item {
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            border-radius: 100%;
            border: 1px solid rgba(0, 0, 0, 0.08);
            object-fit: cover;
          }

          .name {
            padding-left: 12px;
            height: 40px;
            display: flex;
            align-items: center;
            font-size: 16px;
            color: rgba(51, 51, 51, 0.8);
          }
        }
      }

      .note-scroller::-webkit-scrollbar {
        display: none;
      }

      .note-scroller {
        transition: scroll 0.4s;
        overflow-y: scroll;
        flex-grow: 1;
        height: 80vh;

        .note-content {
          padding: 0 24px 24px;
          color: var(--color-primary-label);

          .title {
            margin-bottom: 8px;
            font-weight: 600;
            font-size: 18px;
            line-height: 140%;
          }

          .desc {
            margin: 0;
            font-weight: 400;
            font-size: 14px;
            line-height: 150%;
            color: #333;
            white-space: pre-wrap;
            overflow-wrap: break-word;

            .tag-search {
              cursor: pointer;
            }

            .tag {
              margin-right: 2px;
              color: #13386c;
            }
          }

          .post-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 30px;

            .post {
              font-size: 12px;
              line-height: 120%;
              color: rgba(51, 51, 51, 0.6);
            }
          }

          .price-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 10px;

            .post {
              font-size: 12px;
              line-height: 120%;
              color: rgba(51, 51, 51, 0.6);
            }
          }

          .bottom-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 24px;
            margin-bottom: -15px;

            .bottom-left {
              display: flex;
              gap: 20px;
              align-items: center;
              flex: 1;
              min-width: 0; // 允许flex收缩
            }

            .bottom-right {
              flex-shrink: 0;
              margin-left: 20px;
            }

            .date {
              font-size: 12px;
              line-height: 120%;
              color: rgba(51, 51, 51, 0.6);
            }

            .location {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 14px;
              line-height: 120%;
              color: #1890ff;
              background: none;
              padding: 0;
              border-radius: 0;
              border: none;
              font-weight: 500;
              white-space: nowrap; // 防止地址换行
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 200px; // 限制地址最大宽度

              .el-icon {
                font-size: 14px;
                color: #1890ff;
                flex-shrink: 0;
              }
              .location-text {
                color: #1890ff;
                font-weight: 500;
                overflow: hidden;
                text-overflow: ellipsis;
              }
            }
          }
        }

        .interaction-divider {
          margin: 0 24px;
        }

        .divider {
          margin: 4px 8px;
          list-style: none;
          height: 0;
          border: solid rgba(0, 0, 0, 0.08);
          border-width: 1px 0 0;
        }

        .comments-el {
          position: relative;
        }
      }

      .interactions-footer {
        position: absolute;
        bottom: 0px;
        background: #fff;
        flex-shrink: 0;
        padding: 12px 24px 24px;
        height: 130px;
        border-top: 1px solid rgba(0, 0, 0, 0.08);
        flex-basis: 130px;
        z-index: 1;

        .buttons {
          display: flex;
          justify-content: space-between;

          .count {
            margin-left: 8px;
            margin-right: 12px;
            font-weight: 500;
            font-size: 12px;
          }

          .left {
            display: flex;

            .like-wrapper {
              position: relative;
              cursor: pointer;
              display: flex;
              justify-content: left;
              color: rgba(51, 51, 51, 0.6);
              margin-right: 5px;
              align-items: center;

              .like-lottie {
                transform: scale(1.7);
              }
            }

            .collect-wrapper {
              position: relative;
              cursor: pointer;
              display: flex;
              color: rgba(51, 51, 51, 0.6);
              margin-right: 5px;
              align-items: center;

              .like-lottie {
                transform: scale(1.7);
              }
            }
            :hover {
              cursor: pointer; /* 显示小手指针 */
              transform: scale(1.05); /* 鼠标移入时按钮稍微放大 */
            }

            .chat-wrapper {
              cursor: pointer;
              color: rgba(51, 51, 51, 0.6);
              display: flex;
              align-items: center;

              .like-lottie {
                transform: scale(1.7);
              }
            }
          }
        }

        .comment-wrapper {
          &.active {
            .input-wrapper {
              flex-shrink: 1;
            }
          }
        }

        .comment-wrapper {
          display: flex;
          font-size: 16px;
          overflow: hidden;

          .input-wrapper {
            display: flex;
            position: relative;
            width: 100%;
            flex-shrink: 0;
            transition: flex 0.3s;

            .comment-input:placeholder-shown {
              background-image: none;
              padding: 12px 92px 12px 36px;
              background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAMAAABg3Am1AAAANlBMVEUAAAA0NDQyMjIzMzM2NjY2NjYyMjI0NDQ1NTU1NTUzMzM1NTU1NTUzMzM1NTUzMzM1NTU1NTVl84gVAAAAEnRSTlMAmUyGEzlgc2AmfRx9aToKQzCSoXt+AAAAhElEQVRIx+3Uuw6DMAyF4XOcBOdCafv+L9vQkQFyJBak/JOHT7K8GLM7epuHusRhHwP/mejJ77i32CpZh33aD+lDFDzgZFE8+tgUv5BB9NxEb9NPL3i46JvoUUhXPBKZFQ/rTPHI3ZXt8xr12KX055LoAVtXz9kKHprxNMMxXqRvmAn9ACQ7A/tTXYAxAAAAAElFTkSuQmCC);
              background-repeat: no-repeat;
              background-size: 16px 16px;
              background-position: 16px 12px;
              color: rgba(51, 51, 51, 0.3);
            }

            .comment-input {
              padding: 12px 92px 12px 16px;
              width: 100%;
              height: 40px;
              line-height: 16px;
              background: rgba(0, 0, 0, 0.03);
              caret-color: rgba(51, 51, 51, 0.3);
              border-radius: 22px;
              border: none;
              outline: none;
              resize: none;
              color: #333;
            }

            .input-buttons {
              position: absolute;
              right: 0;
              top: 0;
              height: 40px;
              display: flex;
              align-items: center;
              justify-content: center;
              width: 92px;
              color: rgba(51, 51, 51, 0.3);
            }
          }

          .submit {
            margin-left: 8px;
            width: 60px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-weight: 600;
            cursor: pointer;
            flex-shrink: 0;
            background: #3d8af5;
            border-radius: 44px;
            font-size: 16px;
          }
        }

        .comment-comp {
          margin-top: 20px;
        }
      }
    }
  }
}

// 暗色模式下的地点样式
@media (prefers-color-scheme: dark) {
  .note-detail-mask {
    .note-container {
      .interaction-container {
        .note-scroller {
          .note-content {
            .bottom-container {
              .location {
                // background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
                // border-color: #3d5afe;
                color: #64b5f6;

                .el-icon {
                  color: #64b5f6;
                }
                .location-text {
                  color: #64b5f6;
                }
              }
            }
          }
        }
      }
    }
  }

  .chat-drawer {
    .product-info-header {
      .product-brief {
        .product-detail {
          .location {
            background: none;
            border: none;
            color: #64b5f6;

            .el-icon {
              color: #64b5f6;
            }

            .location-text {
              color: #64b5f6;
            }
          }
        }
      }
    }
  }
}

// 手动暗色模式类
.dark {
  .note-detail-mask {
    .note-container {
      .interaction-container {
        .note-scroller {
          .note-content {
            .bottom-container {
              .location {
                background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
                border-color: #3d5afe;
                color: #64b5f6;

                .el-icon {
                  color: #64b5f6;
                }
                .location-text {
                  color: #64b5f6;
                }
              }
            }
          }
        }
      }
    }
  }

  .chat-drawer {
    .product-info-header {
      .product-brief {
        .product-detail {
          .location {
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            border-color: #3d5afe;
            color: #64b5f6;

            .el-icon {
              color: #64b5f6;
            }

            .location-text {
              color: #64b5f6;
            }
          }
        }
      }
    }
  }
}

/* 复用与笔记详情一致的角标样式 */
.img-counter-badge {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
  background: rgba(51, 51, 51, 0.75);
  color: #fff;
  border-radius: 999px;
  font-size: 16px;
  min-width: 40px;
  text-align: center;
  padding: 7px 15px;
  user-select: none;
}

// 移动端响应式样式
@media screen and (max-width: 695px) {
  .note-detail-mask {
    background-color: #fff; // 去掉 !important，让暗色模式样式可以覆盖
    z-index: 10001 !important; // 确保在底部导航栏上方
    position: fixed !important;
    left: 0 !important;
    right: 0 !important;
    top: 0 !important;
    bottom: 0 !important;

    .note-container {
      width: 100vw !important;
      max-width: 100vw !important;
      height: 100vh !important;
      max-height: 100vh !important;
      transform: translate(0, 0) scale(1) !important;
      border-radius: 0 !important;
      flex-direction: column !important;
      background: #fff; // 去掉 !important
      box-shadow: none !important;
      display: flex !important;
      overflow: hidden !important; // 容器本身不滚动，只有中间内容区域滚动
      padding: 0 !important;
      margin: 0 !important;
      position: fixed !important;
      left: 0 !important;
      right: 0 !important;
      top: 0 !important;
      bottom: 0 !important;
      -webkit-overflow-scrolling: touch !important;

      // 1. 图片/视频区域（固定在顶部，不随内容滚动）
      .media-container {
        width: 100% !important;
        min-width: 0 !important;
        max-width: 100% !important;
        height: 40vh !important;
        max-height: 400px !important;
        flex-shrink: 0 !important;
        background: #f5f5f5; // 去掉 !important
        margin: 0 !important;
        margin-top: 60px !important; // 为固定顶部栏留出空间
        padding: 0 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        overflow: visible !important;
        position: relative !important; // 固定定位，不随内容滚动
      }

      :deep(.el-carousel) {
        height: 100% !important;
        width: 100% !important;
        margin: 0 !important;
        padding: 0 !important;
      }

      :deep(.el-carousel__container) {
        height: 100% !important;
        width: 100% !important;
        left: 0 !important;
        right: 0 !important;
        transform: none !important;
        margin: 0 !important;
        padding: 0 !important;
      }

      :deep(.el-carousel__item) {
        height: 100% !important;
        width: 100% !important;
        background: #f5f5f5 !important;
        padding: 0 !important;
        margin: 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
      }

      :deep(.el-carousel__arrow) {
        display: flex !important;
        background: rgba(31, 45, 61, 0.5) !important;
        width: 36px !important;
        height: 36px !important;
      }

      :deep(.el-image) {
        width: 100% !important;
        height: 100% !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
      }

      :deep(img) {
        max-width: 100% !important;
        max-height: 100% !important;
        width: auto !important;
        height: auto !important;
        object-fit: contain !important;
        margin-left: 0 !important;
        margin-right: 0 !important;
      }

      .img-counter-badge {
        display: block !important;
        position: absolute !important;
        top: 14px !important;
        right: 14px !important;
        z-index: 10 !important;
        background: rgba(51, 51, 51, 0.75) !important;
        color: #fff !important;
        border-radius: 999px !important;
        font-size: 14px !important;
        padding: 4px 12px !important;
      }
    }

    // 2. 内容容器（包含滚动区域）
    .interaction-container {
      width: 100% !important;
      flex: 1 !important; // 使用 flex: 1 占据剩余空间
      display: flex !important;
      flex-direction: column !important;
      border: none !important;
      overflow: hidden !important; // 隐藏溢出，让子元素滚动
      padding: 0 !important;
      margin: 0 !important;
      position: static !important;
      min-height: 0 !important; // 确保 flex 子元素可以正确收缩

      .author-container {
        width: 100% !important;
        flex: 1 !important;
        display: flex !important;
        flex-direction: column !important;
        overflow: hidden !important; // 隐藏溢出，让子元素滚动
        padding: 0 !important;
        margin: 0 !important;
        min-height: 0 !important; // 确保 flex 子元素可以正确收缩
      }

      .author-me {
        width: 100% !important;
        height: 60px !important;
        flex-shrink: 0 !important;
        padding: 0 8px 0 16px !important;
        border: none !important;
        border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
        display: flex !important;
        align-items: center !important;
        justify-content: space-between !important;
        overflow: visible !important;
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        right: 0 !important;
        z-index: 100 !important;
        background: #fff; // 去掉 !important
        border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;

        .info {
          display: flex !important;
          align-items: center !important;
          flex-shrink: 1 !important;
          min-width: 0 !important;
          margin-left: 48px !important;

          .avatar-item {
            width: 36px !important;
            height: 36px !important;
            flex-shrink: 0 !important;
          }

          .name {
            font-size: 15px !important;
            font-weight: 600 !important;
            color: #333; // 去掉 !important
            margin-left: 10px !important;
            padding: 0 !important;
            height: auto !important;
            white-space: nowrap !important;
            overflow: hidden !important;
            text-overflow: ellipsis !important;
          }
        }

        .follow-btn {
          flex-shrink: 0 !important;

          :deep(.el-button) {
            padding: 6px 16px !important;
            height: 32px !important;
            font-size: 13px !important;
          }

          :deep(.el-dropdown) {
            display: none !important;
          }
        }
      }

      // 3. 内容区域（笔记内容、评论）- 只有这部分滚动
      .note-scroller {
        flex: 1 !important;
        overflow-y: auto !important; // 恢复滚动，只有中间内容区域滚动
        overflow-x: hidden !important;
        background: #fff !important;
        padding: 0 !important;
        padding-top: 60px !important; // 为固定的顶部栏留出空间
        padding-bottom: 48px !important; // 为底部状态栏留出空间，防止内容被遮挡
        margin: 0 !important;
        margin-bottom: 0 !important;
        min-height: 0 !important; // 确保 flex 子元素可以正确收缩
        -webkit-overflow-scrolling: touch !important;

        // 内容区域背景
        .note-content {
          background: #fff !important;
        }

        // 评论区域背景
        .comments-el {
          background: #fff !important;
        }

        // 笔记内容
        .note-content {
          padding: 8px 16px 16px 16px !important;
          background: #fff; // 去掉 !important
          display: block !important;
          width: 100% !important;

          .title {
            font-size: 18px !important;
            font-weight: 600 !important;
            line-height: 1.5 !important;
            margin-bottom: 12px !important;
            color: #333; // 去掉 !important
          }

          .price-info {
            display: flex !important;
            align-items: baseline !important;
            margin-bottom: 12px !important;

            .current-price {
              font-size: 24px !important;
              font-weight: 700 !important;
              color: #ff2442 !important;
            }

            .original-price {
              font-size: 14px !important;
              color: #999 !important;
              text-decoration: line-through !important;
              margin-left: 8px !important;
            }
          }

          .desc {
            font-size: 15px !important;
            line-height: 1.7 !important;
            color: #333; // 去掉 !important
            margin-bottom: 12px !important;
            word-break: break-word !important;
          }

          .bottom-container {
            display: flex !important;
            align-items: flex-start !important;
            gap: 12px !important;
            font-size: 13px !important;
            color: #999 !important;
            margin-top: 12px !important;
            padding-top: 0 !important;
            border-top: none !important;
            flex-wrap: wrap !important;

            .date {
              flex-shrink: 0 !important;
              white-space: nowrap !important;
            }

            .location {
              display: flex !important;
              align-items: center !important;
              gap: 4px !important;
              flex: 1 !important;
              min-width: 0 !important;
              background: none !important;
              border: none !important;
              padding: 0 !important;
              border-radius: 0 !important;

              .el-icon {
                flex-shrink: 0 !important;
                font-size: 13px !important;
                color: #1890ff !important;
              }

              .location-text {
                overflow: hidden !important;
                text-overflow: ellipsis !important;
                white-space: nowrap !important;
                color: #1890ff !important;
                font-weight: normal !important;
              }
            }

            .bottom-right {
              display: none !important;
            }
          }
        }

        .divider {
          height: 1px !important;
          margin: 0 16px !important;
          margin-top: 16px !important;
          margin-bottom: 16px !important;
          border-top: 1px solid #f0f0f0 !important;
          display: block !important;
        }

        .comments-el {
          padding: 0 16px !important; // 左右都有 padding
          background: #fff !important;
          display: block !important;
          visibility: visible !important;
          min-height: 100px !important;
          margin-bottom: 0 !important;
          padding-bottom: 0 !important;

          // 确保评论组件内的样式正确应用
          :deep(.comments-container) {
            padding: 0 !important; // 移除组件内部的 padding，因为外层已经有 padding

            .total {
              font-size: 14px !important;
              color: rgba(51, 51, 51, 0.6) !important;
              margin-left: 0 !important;
              margin-bottom: 12px !important;
            }

            .list-container {
              .parent-comment {
                margin-bottom: 16px !important;

                .comment-item {
                  display: flex !important;
                  padding: 8px 0 !important;

                  .comment-inner-container {
                    display: flex !important;
                    width: 100% !important;

                    .avatar {
                      flex: 0 0 auto !important;

                      .avatar-item {
                        display: block !important;
                        width: 40px !important;
                        height: 40px !important;
                        border-radius: 50% !important;
                        border: 1px solid rgba(0, 0, 0, 0.08) !important;
                        object-fit: cover !important;
                        cursor: pointer !important;
                      }
                    }

                    .right {
                      margin-left: 12px !important;
                      display: flex !important;
                      flex-direction: column !important;
                      flex-grow: 1 !important;
                      font-size: 14px !important;

                      .author-wrapper {
                        display: flex !important;
                        justify-content: space-between !important;
                        align-items: center !important;

                        .author {
                          display: flex !important;
                          align-items: center !important;

                          .name {
                            color: rgba(51, 51, 51, 0.6) !important;
                            line-height: 18px !important;
                          }
                        }
                      }

                      .content {
                        margin-top: 4px !important;
                        line-height: 140% !important;
                        color: #333 !important;
                      }

                      .info {
                        display: flex !important;
                        flex-direction: column !important;
                        font-size: 12px !important;
                        line-height: 16px !important;
                        color: rgba(51, 51, 51, 0.6) !important;

                        .date {
                          margin: 8px 0 !important;
                        }

                        .interactions {
                          display: flex !important;
                          margin-left: -2px !important;

                          .like-wrapper {
                            padding: 0 4px !important;
                            color: rgba(51, 51, 51, 0.8) !important;
                            font-weight: 500 !important;
                            cursor: pointer !important;
                            display: flex !important;
                            align-items: center !important;
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 4. 底部栏
      .interactions-footer {
        position: fixed !important;
        bottom: 0 !important;
        left: 0 !important;
        right: 0 !important;
        width: 100% !important;
        background: #fff !important;
        border-top: 1px solid rgba(0, 0, 0, 0.08) !important;
        padding: 10px 16px !important;
        padding-bottom: calc(10px + env(safe-area-inset-bottom)) !important; // 适配安全区域
        min-height: 48px !important;
        max-height: 48px !important;
        height: 48px !important;
        z-index: 10002 !important; // 确保在遮罩层和导航栏上方
        box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08) !important;
        display: flex !important;
        align-items: center !important;
        gap: 10px !important;

        // 当有输入内容时，隐藏右侧按钮
        &:has(.comment-wrapper.active) .buttons {
          display: none !important;
        }

        .comment-wrapper {
          order: 1 !important;
          flex: 1 !important;
          display: flex !important;
          align-items: center !important;
          gap: 10px !important;
          margin: 0 !important;
          margin-right: 12px !important;

          .input-wrapper {
            flex: 1 !important;
            display: flex !important;
            align-items: center !important;
            gap: 6px !important;
            background: #f5f5f5; // 去掉 !important
            border-radius: 18px !important;
            padding: 5px 12px !important;
            height: 28px !important;
            min-height: 28px !important;
            max-height: 28px !important;
            position: relative !important;

            .comment-input {
              flex: 1 !important;
              height: 20px !important;
              line-height: 20px !important;
              font-size: 13px !important;
              padding: 0 !important;
              padding-right: 24px !important;
              border: none !important;
              background: transparent !important;
              outline: none !important;
            }

            .input-buttons {
              position: absolute !important;
              right: 8px !important;
              top: 50% !important;
              transform: translateY(-50%) !important;
              display: flex !important;
              align-items: center !important;
              justify-content: center !important;
              color: #999 !important;
              cursor: pointer !important;
              font-size: 16px !important;
              width: 20px !important;
              height: 20px !important;

              &[style*="display: none"] {
                display: none !important;
              }
            }
          }

          .submit {
            display: none;
            height: 28px !important;
            padding: 0 16px !important;
            font-size: 13px !important;
            border-radius: 14px !important;
            background: #007aff !important;
            color: #fff !important;
            border: none !important;
            cursor: pointer !important;
            white-space: nowrap !important;
            flex-shrink: 0 !important;
          }
        }

        // 有输入内容时显示发送按钮
        .comment-wrapper.active {
          .submit {
            display: block !important;
          }

          .input-wrapper {
            .comment-input {
              padding-right: 8px !important;
            }

            .input-buttons {
              display: none !important;
            }
          }
        }

        .buttons {
          order: 2 !important;
          display: flex !important;
          align-items: center !important;
          gap: 20px !important;
          margin-left: 8px !important;

          .left {
            display: flex !important;
            align-items: center !important;
            gap: 20px !important;
          }

          .like-wrapper,
          .collect-wrapper,
          .chat-wrapper {
            display: flex !important;
            align-items: center !important;
            gap: 4px !important;
            cursor: pointer !important;

            .like-lottie {
              display: flex !important;
              align-items: center !important;
              justify-content: center !important;
              font-size: 20px !important;
              line-height: 1 !important;
            }

            .count {
              font-size: 14px !important;
              color: #333; // 去掉 !important
              line-height: 1 !important;
              padding-top: 1px !important;
            }
          }

          .share-wrapper {
            display: flex !important;
            align-items: center !important;
            cursor: pointer !important;
          }
        }
      }
    }
  }

  // 关闭按钮
  .close-cricle {
    position: fixed !important;
    top: 12px !important;
    left: 8px !important;
    z-index: 200 !important;

    .close {
      width: 32px !important;
      height: 32px !important;
      background: rgba(255, 255, 255, 0.9) !important;
      border-radius: 50% !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
    }
  }

  .back-desk {
    display: none !important;
  }
}
</style>

<style>
/* 商品详情页移动端暗色模式 - 使用超高优先级选择器 */
@media screen and (max-width: 695px) {
  html.dark-mode.dark-mode .note-detail-mask {
    background-color: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .note-container {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .media-container {
    background: #000 !important;
  }

  html.dark-mode.dark-mode .el-carousel__item {
    background: #000 !important;
  }

  html.dark-mode.dark-mode .author-container {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .author-me {
    background: #1a1a1a !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  html.dark-mode.dark-mode .author-me .info .name {
    color: #e0e0e0 !important;
  }

  html.dark-mode.dark-mode .note-scroller {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .note-content {
    background: #1a1a1a !important;
  }

  html.dark-mode.dark-mode .note-content .title {
    color: #e0e0e0 !important;
  }

  html.dark-mode.dark-mode .note-content .desc {
    color: #b0b0b0 !important;
  }

  html.dark-mode.dark-mode .interactions-footer {
    background: #1a1a1a !important;
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  html.dark-mode.dark-mode .interactions-footer .left .count {
    color: #e0e0e0 !important;
  }

  html.dark-mode.dark-mode .comment-wrapper .input-wrapper {
    background: #2a2a2a !important;
  }

  html.dark-mode.dark-mode .comment-input {
    color: #e0e0e0 !important;
  }
}
</style>









