<template>
  <div class="container idle-trend-page" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
    <!-- 登录状态判断 -->
    <div v-if="isLogin">
      <div class="trend-layout">
        <aside class="trend-sidebar">
          <div class="tab-container">
            <div class="tab-track">
              <div
                class="tab-slider"
                :style="{ transform: pageType === '2' ? 'translateY(100%)' : 'translateY(0)' }"
              ></div>
              <div class="tab-item" :class="{ active: pageType === '1' }" @click="pageType = '1'">
                {{ $t("note.title") }}
              </div>
              <div class="tab-item" :class="{ active: pageType === '2' }" @click="pageType = '2'">
                {{ $t("idle.title") }}
              </div>
            </div>
          </div>
        </aside>

        <div class="trend-main">
          <!-- 首屏加载中：骨架屏 -->
          <div v-if="!listLoaded && trendList.length === 0" class="product-list">
            <div class="product-card skeleton-card" v-for="n in 3" :key="'sk-' + n">
              <div class="product-content">
                <div class="product-image">
                  <el-skeleton animated
                    ><template #template
                      ><el-skeleton-item
                        variant="image"
                        style="width: 200px; height: 200px; border-radius: 8px" /></template
                  ></el-skeleton>
                </div>
                <div class="product-info" style="flex: 1">
                  <el-skeleton animated :rows="4" />
                </div>
              </div>
            </div>
          </div>

          <!-- 单列商品列表 -->
          <div v-else-if="trendList.length > 0" class="product-list">
            <div class="product-card" v-for="(trend, index) in trendList" :key="index" @click="toMain(trend.id)">
              <div class="product-content">
                <div class="product-image">
                  <!-- 视频角标（与发现页 idle 瀑布流 top-tag-area 一致） -->
                  <div v-if="trend.productType === 2" class="top-tag-area">
                    <svg
                      class="icon"
                      viewBox="0 0 1024 1024"
                      xmlns="http://www.w3.org/2000/svg"
                      width="25"
                      height="25"
                    >
                      <path
                        d="M512 0C230.4 0 0 230.4 0 512s230.4 512 512 512 512-230.4 512-512S793.6 0 512 0z m0 981.333333C253.866667 981.333333 42.666667 770.133333 42.666667 512S253.866667 42.666667 512 42.666667s469.333333 211.2 469.333333 469.333333-211.2 469.333333-469.333333 469.333333z"
                        fill="#ffffff"
                      />
                      <path
                        d="M672 441.6l-170.666667-113.066667c-57.6-38.4-106.666667-12.8-106.666666 57.6v256c0 70.4 46.933333 96 106.666666 57.6l170.666667-113.066666c57.6-42.666667 57.6-106.666667 0-145.066667z"
                        fill="#ffffff"
                      />
                    </svg>
                  </div>
                  <!-- Live图角标 -->
                  <div v-if="trend.productType === 3" class="live-badge">
                    <span>Live</span>
                  </div>
                  <el-image :src="trend.image" fit="cover"></el-image>
                  <!-- 多图角标 -->
                  <div v-if="trend.imgUrls && trend.imgUrls.length > 1" class="more-count">
                    +{{ trend.imgUrls.length }}
                  </div>
                  <div class="status-tag" :class="getStatusClass(trend.type)">
                    {{ getStatusText(trend.type) }}
                  </div>
                </div>
                <div class="product-info">
                  <h3 class="title">{{ trend.title }}</h3>
                  <div class="subtitle">{{ formatContent(trend.description) }}</div>
                  <div class="price-info">
                    <div class="price">
                      <span class="current">¥{{ convert.to_price(trend.price) }}</span>
                      <span class="original">¥{{ convert.to_price(trend.originalPrice) }}</span>
                      <span class="post-type" :class="trend.postType === 0 ? 'delivery' : 'pickup'">
                        {{ trend.postType === 0 ? $t("common.mail") : $t("common.selfPickup") }}
                      </span>
                    </div>
                    <div class="meta">
                      <div class="user-info">
                        <el-avatar :src="trend.avatar" :size="28" />
                        <span class="username">{{ trend.username }}</span>
                      </div>
                      <span class="location" v-if="formatAddress(trend)">
                        <el-icon><Location /></el-icon>
                        {{ formatAddress(trend) }}
                      </span>
                      <span class="time">{{ formateTime(trend.time) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="listLoaded && !loading" class="empty-state">
            <el-empty :description="$t('trend.noTrend')">
              <el-button type="primary" @click="goToPublish">{{ $t("trend.goPublishIdle") }}</el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="load-more" v-if="!finished && !mainShow">
      <el-loading v-if="loading" />
      <span v-else>{{ $t("trend.pullLoadMore") }}</span>
    </div>
    <!-- <div class="no-more" v-if="finished && !mainShow">
      没有更多数据了
    </div> -->
    <FloatingBtn @click-refresh="refresh"></FloatingBtn>
    <IdleMain
      v-show="mainShow"
      :pid="productId"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="closeMain"
    >
    </IdleMain>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from "vue";
import { useI18n } from "vue-i18n";
import { Location } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import convert from "@/utils/convert";
import IdleMain from "@/views/idle-main/main.vue";
import { getProductByUser } from "@/api/user";
import { formateTime, refreshTab } from "@/utils/util";
import FloatingBtn from "@/components/FloatingBtn.vue";

interface Trend {
  id: string;
  // type: 'publish' | 'sell' | 'buy'
  type: 0 | 1 | 2;
  image: string;
  title: string;
  description: string;
  originalPrice: number;
  price: number;
  color: string;
  time: number;
  createTime: string;
  productType: number;
  postType: number;
  province: string;
  city: string;
  district?: string;
  address?: string;
  buyUid?: string;
  avatar: string;
  username: string;
  imgUrls?: string[];
}

const { t } = useI18n();

const isLogin = ref(false);
const mainShow = ref(false);
const productId = ref("");
const trendList = ref<Trend[]>([]);
const router = useRouter();
const userStore = useUserStore();
const uid = userStore.getUserInfo().id;
const currentPage = ref(1);
const pageSize = 10;
const type = 0;
const total = ref(0);
const loading = ref(false);
const finished = ref(false); // 是否加载完成
const pageType = ref("2");
const listLoaded = ref(false);

const getStatusClass = (type: number) => {
  switch (type) {
    case 0: // 上新
      return "new";
    case 1: // 卖出
      return "sold";
    case 2: // 买到
      return "bought";
    default:
      return "";
  }
};

// 获取状态文本
const getStatusText = (type: number) => {
  switch (type) {
    case 0:
      return t("trend.statusNew");
    case 1:
      return t("trend.statusSold");
    case 2:
      return t("trend.statusBought");
    default:
      return "";
  }
};

const goToPublish = () => {
  router.push({ path: "/push", query: { tab: "idle" } });
};

const refresh = () => {
  refreshTab(() => {
    finished.value = false;
    currentPage.value = 1;
    trendList.value = [];
    listLoaded.value = false;
    getTrend();
  });
};

const loadMoreData = () => {
  if (finished.value || loading.value) return;
  // 如果是第一页，则不执行加载
  if (currentPage.value === 1 && trendList.value.length === 0) return;

  currentPage.value += 1;
  getTrend();
};

const getTrend = async () => {
  if (finished.value || loading.value) return;
  const queryParams = {
    currentPage: currentPage.value,
    pageSize: pageSize,
    uid: uid,
    type: type,
    status: "trend",
  };
  try {
    loading.value = true;
    const res = await getProductByUser(queryParams);
    const { records, total: totalCount } = res.data;
    total.value = totalCount;

    // 处理每条记录，判断类型
    const newItems = records.map((item: any) => {
      const trendItem: any = {
        ...item,
        image: item.cover,
        imgUrls: [],
        // image: item.cover ? JSON.parse(item.urls)[0] : '',
        type: 0, // 默认为上新
      };
      // 解析多图
      try {
        trendItem.imgUrls = item && item.urls ? JSON.parse(item.urls) : [];
      } catch (e) {
        trendItem.imgUrls = [];
      }
      // 如果有买家ID，判断是买入还是卖出
      if (item.buyUid) {
        if (item.buyUid === uid) {
          // 当前用户是买家
          trendItem.type = 2; // 买到了一件宝贝
          trendItem.color = "#67C23A";
        } else {
          // 当前用户是卖家
          trendItem.type = 1; // 卖出了一件宝贝
          trendItem.color = "#E6A23C";
        }
      } else {
        // 没有买家，说明是上新
        trendItem.type = 0;
        trendItem.color = "#409EFF";
      }
      return trendItem;
    });

    trendList.value = [...trendList.value, ...newItems];

    if (trendList.value.length >= total.value) {
      finished.value = true;
    }
  } catch (error) {
    console.error("获取动态失败:", error);
  } finally {
    loading.value = false;
    listLoaded.value = true;
  }
};

const toMain = (val: string) => {
  productId.value = val;
  mainShow.value = true;
};

const closeMain = () => {
  mainShow.value = false;
};

// 将富文本转为安全纯文本展示
const formatContent = (html: string): string => {
  if (!html) return "";
  let text = html
    .replace(/<\s*br\s*\/?>/gi, "\n")
    .replace(/<\s*\/p\s*>/gi, "\n")
    .replace(/<[^>]*>/g, "")
    .replace(/&nbsp;/gi, " ")
    .replace(/&amp;/gi, "&")
    .replace(/&lt;/gi, "<")
    .replace(/&gt;/gi, ">")
    .replace(/&quot;/gi, '"')
    .replace(/&#39;/gi, "'");
  return text.replace(/\s+$/g, "").trim();
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

onMounted(() => {
  isLogin.value = userStore.isLogin();
  getTrend();
});

// 监听页面类型变化
watch(pageType, (newVal) => {
  // 重置数据
  currentPage.value = 1;
  trendList.value = [];
  finished.value = false;

  if (newVal === "1") {
    router.push("/noteTrend");
  } else {
    listLoaded.value = false;
    getTrend();
  }
});
</script>

<style lang="less" scoped>
// 主题变量
@primary-color: #3d8af5;
@text-primary: rgba(51, 51, 51, 0.8);
@text-secondary: rgba(51, 51, 51, 0.6);
@border-color: rgba(0, 0, 0, 0.08);
@white: #fff;

.container {
  width: 100%;
  max-width: 800px;
  margin: 50px auto;
  margin-left: 150px;
  padding: 20px;
  box-sizing: border-box;

  @media screen and (max-width: 695px) {
    padding-bottom: 70px;
  }

  .trend-layout {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    gap: 20px;
    width: 100%;
  }

  .trend-sidebar {
    flex: 0 0 auto;
    position: sticky;
    top: 72px;
    align-self: flex-start;
    z-index: 2;
  }

  .trend-main {
    flex: 1;
    min-width: 0;
    max-width: 660px;
  }

  @media screen and (max-width: 695px) {
    .trend-layout {
      flex-direction: column;
      gap: 16px;
    }

    .trend-sidebar {
      position: static;
      width: 100%;
      display: flex;
      justify-content: center;
    }

    .trend-main {
      max-width: none;
      width: 100%;
    }
  }

  .tab-container {
    display: flex;
    margin: 0;

    .tab-track {
      margin-top: 20px;
      margin-left: 0;
      position: relative;
      display: inline-flex;
      flex-direction: column;
      align-items: stretch;
      // min-width: 44px;
      border: 1px solid rgba(0, 0, 0, 0.06);
      border-radius: 999px;
      padding: 0;
      overflow: hidden;
    }

    .tab-slider {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 50%;
      background: @primary-color;
      border-radius: 999px;
      box-shadow: 0 2px 6px rgba(61, 138, 245, 0.32);
      transition: transform 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
    }

    .tab-item {
      position: relative;
      z-index: 1;
      flex: 1 1 0;
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 0;
      min-height: 44px;
      padding: 8px 8px;
      cursor: pointer;
      color: #86909c;
      font-size: 13px;
      font-weight: 600;
      line-height: 1.35;
      border-radius: 999px;
      transition: color 0.3s ease;
      user-select: none;
      text-align: center;
      writing-mode: vertical-rl;
      text-orientation: mixed;
      letter-spacing: 0.06em;

      &:hover:not(.active) {
        color: #4e5969;
      }

      &.active {
        color: #fff;
      }
    }
  }

  .skeleton-card {
    pointer-events: none;
  }
}

.trend-main .product-list {
  margin-top: 20px;
}

.product-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }

  .product-content {
    display: flex;
    padding: 16px;
    gap: 20px;
  }

  .product-image {
    position: relative;
    width: 200px;
    height: 200px;
    flex-shrink: 0;

    .top-tag-area {
      position: absolute;
      right: 12px;
      top: 12px;
      z-index: 4;
      line-height: 0;
    }

    .live-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      padding: 4px 8px;
      border-radius: 12px;
      color: #fff;
      font-size: 12px;
      background-color: @primary-color;
      z-index: 2;
    }

    .more-count {
      position: absolute;
      top: 10px;
      right: 10px;
      background: rgba(0, 0, 0, 0.356);
      color: #fff;
      padding: 4px 9px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: bold;
      z-index: 2;
    }

    .el-image {
      width: 100%;
      height: 100%;
      border-radius: 8px;
    }

    .status-tag {
      position: absolute;
      top: 10px;
      left: 10px;
      padding: 4px 8px;
      border-radius: 4px;
      color: #fff;
      font-size: 12px;

      &.new {
        background-color: #409eff; // 蓝色，表示新上架
      }

      &.sold {
        background-color: #e6a23c; // 橙色，表示已卖出
      }

      &.bought {
        background-color: #67c23a; // 绿色，表示已购买
      }
    }
  }

  .product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .title {
      font-size: 18px;
      font-weight: 500;
      color: #333;
      margin: 0 0 8px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
      max-height: 2.8em; /* 2行的高度 */
    }

    .subtitle {
      font-size: 14px;
      color: #666;
      margin-bottom: auto;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
      max-height: 3em; /* 2行的高度 */
    }

    .price-info {
      margin-top: 16px;

      .price {
        margin-bottom: 10px;

        .current {
          color: #ff2442;
          font-size: 20px;
          font-weight: bold;
        }

        .original {
          margin-left: 8px;
          color: #999;
          font-size: 14px;
          text-decoration: line-through;
          margin-right: 20px;
        }

        .post-type {
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 15px;
          margin-right: 15px;
          border: none;

          &.delivery {
            background: #67c23a;
            color: #ffffff;
          }

          &.pickup {
            background: #e6a23c;
            color: #ffffff;
          }
        }
      }

      .meta {
        display: flex;
        align-items: center;
        color: #999;
        font-size: 13px;
        position: relative;
        width: 100%;

        .user-info {
          display: flex;
          align-items: center;

          .el-avatar {
            margin-right: 6px;
          }

          .username {
            font-size: 14px;
            color: #666;
            margin-right: 30px;
          }
        }

        .location {
          margin-top: 2px;
          display: flex;
          align-items: center;
          gap: 5px;
          cursor: pointer;
          transition: all 0.3s;
          color: #1989fa;
          max-width: 200px;
          flex-shrink: 1;

          .el-icon {
            flex-shrink: 0;
          }

          .location-text {
            font-size: 14px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .time {
          color: #909399;
          margin-left: auto; // 让时间始终在最右侧
        }
      }
    }
  }
}

.load-more,
.no-more {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 10px 0;
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

<style lang="less">
html.dark-mode .idle-trend-page .tab-container {
  .tab-track {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.14);
  }

  .tab-slider {
    background: #3d8af5;
    box-shadow: 0 2px 12px rgba(61, 138, 245, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.12);
  }

  .tab-item {
    color: rgba(255, 255, 255, 0.5);

    &:hover:not(.active) {
      color: rgba(255, 255, 255, 0.88);
    }

    &.active {
      color: #fff !important;
      -webkit-text-fill-color: #fff;
    }
  }
}

/* 暗色模式：商品动态卡片正文与元数据（避免被全局闲置「价格红」规则牵连后继承异常） */
html.dark-mode .idle-trend-page .product-card {
  background: #2a2a2a !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.35) !important;

  .product-info .title {
    color: #fff !important;
  }

  .product-info .subtitle {
    color: rgba(255, 255, 255, 0.65) !important;
  }

  .price-info .price .current {
    color: #ff2442 !important;
  }

  .price-info .price .original {
    color: rgba(255, 255, 255, 0.42) !important;
  }

  .price-info .price .post-type.delivery,
  .price-info .price .post-type.pickup {
    color: #fff !important;
  }

  .price-info .meta .username {
    color: #fff !important;
  }

  .price-info .meta .location {
    color: #3d8af5 !important;
  }

  .price-info .meta .time {
    color: rgba(255, 255, 255, 0.55) !important;
  }

  .price-info .meta .location .el-icon {
    color: #3d8af5 !important;
  }
}
</style>
