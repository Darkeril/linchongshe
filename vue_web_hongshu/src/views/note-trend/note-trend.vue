<template>
  <div class="trend-page">
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
          <div v-if="!listLoaded && trendData.length === 0" class="trend-list">
            <div class="card skeleton-card" v-for="n in 3" :key="'sk-' + n">
              <div class="card-content">
                <div class="note-image">
                  <el-skeleton animated
                    ><template #template
                      ><el-skeleton-item
                        variant="image"
                        style="width: 200px; height: 200px; border-radius: 8px" /></template
                  ></el-skeleton>
                </div>
                <div class="note-info" style="flex: 1">
                  <el-skeleton animated :rows="4" />
                </div>
              </div>
            </div>
          </div>

          <!-- 动态列表 -->
          <div
            v-else-if="trendData.length > 0"
            class="trend-list"
            v-infinite-scroll="loadMoreData"
            :infinite-scroll-distance="50"
          >
            <div class="card" v-for="(item, index) in trendData" :key="index">
              <div class="card-content" @click="toMain(item.nid)">
                <!-- 左侧图片区域 -->
                <div class="note-image" v-if="item.imgUrls.length > 0 || item.noteCover">
                  <el-image
                    :src="item.noteType === '2' || item.noteType === '3' ? item.noteCover : item.imgUrls[0]"
                    fit="cover"
                  />

                  <!-- 视频角标（与发现页 idle 瀑布流 top-tag-area 一致） -->
                  <div v-if="item.noteType === '2'" class="top-tag-area">
                    <svg class="icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="25" height="25">
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
                  <div v-if="item.noteType === '3'" class="live-badge">
                    <span>Live</span>
                  </div>

                  <!-- 多图角标 -->
                  <div v-if="item.imgUrls.length > 1" class="more-count">+{{ item.imgUrls.length }}</div>
                </div>

                <!-- 右侧内容区域 -->
                <div class="note-info">
                  <!-- 笔记标题和内容 -->
                  <div class="note-content">
                    <h3 class="title">{{ item.title }}</h3>
                    <div class="content-text" v-if="item.content">
                      {{ formatContent(item.content) }}
                    </div>
                  </div>

                  <!-- 底部信息 -->
                  <div class="note-meta">
                    <div class="bottom-row">
                      <div class="left-group">
                        <div class="user-info">
                          <img class="avatar" :src="item.avatar" @click.stop="toUser(item.uid)" />
                          <span class="username">{{ item.username }}</span>
                        </div>

                        <div v-if="formatAddress(item)" class="location">
                          <el-icon><Location /></el-icon>
                          <span>{{ formatAddress(item) }}</span>
                        </div>
                      </div>

                      <div class="right-group">
                        <div class="actions">
                          <button
                            class="action-btn"
                            @click.stop="like(item.nid, item.uid, index, item.isLike ? -1 : 1)"
                          >
                            <i :class="['icon', item.isLike ? 'liked' : '']">
                              {{ item.isLike ? "❤️" : "🤍" }}
                            </i>
                            <span>{{ item.likeCount }}</span>
                          </button>

                          <button class="action-btn" @click.stop="toMain(item.nid)">
                            <i>💬</i>
                            <span>{{ item.commentCount }}</span>
                          </button>
                        </div>

                        <span class="time">{{ formateTime(item.time) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 已请求完成且确实无数据 -->
          <div v-else-if="listLoaded && !loading" class="empty-state">
            <el-empty :description="$t('trend.noTrend')">
              <el-button type="primary" @click="goToPublish">{{ $t("trend.goPublish") }}</el-button>
            </el-empty>
          </div>
        </div>
      </div>

      <!-- 浮动按钮 -->
      <FloatingBtn @click-refresh="refresh" />

      <!-- 详情页 -->
      <Main
        v-show="mainShow"
        :nid="nid"
        scene="profile"
        :nowTime="new Date()"
        class="animate__animated animate__zoomIn animate__delay-0.5s"
        @click-main="close"
      />
    </div>

    <!-- 未登录状态 -->
    <div v-else class="login-prompt">
      <el-empty :description="$t('trend.loginToView')" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, watch } from "vue";
import { Location } from "@element-plus/icons-vue";
import { getFollowTrend } from "@/api/follower";
import { formateTime, refreshTab } from "@/utils/util";
import FloatingBtn from "@/components/FloatingBtn.vue";
import Main from "@/views/main/main.vue";
import type { LikeOrCollectionDTO } from "@/type/likeOrCollection";
import { likeOrCollectionByDTO } from "@/api/likeOrCollection";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";

const router = useRouter();
const userStore = useUserStore();
const currentPage = ref(1);
const pageSize = ref(10);
const trendData = ref<Array<any>>([]);
const trendTotal = ref(0);
const loading = ref(false);
/** 当前列表是否至少完成过一次拉取（用于区分「加载中」与「真的没有数据」） */
const listLoaded = ref(false);
const topLoading = ref(false);
const mainShow = ref(false);
const nid = ref("");
const likeOrCollectionDTO = ref<LikeOrCollectionDTO>({
  likeOrCollectionId: "",
  publishUid: "",
  type: 0,
});
const isLogin = ref(false);
const pageType = ref("1");

const toUser = (uid: string) => {
  //router.push({ name: "user", state: { uid: uid } });
  router.push({ name: "user", query: { uid: uid } });
};

const getFollowTrends = () => {
  loading.value = true;
  getFollowTrend(currentPage.value, pageSize.value)
    .then((res) => {
      const { records, total } = res.data;
      trendData.value.push(...records);
      trendTotal.value = total;
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false;
      listLoaded.value = true;
    });
};

const loadMoreData = () => {
  // 没有更多数据或正在加载时不再请求
  if (loading.value || trendData.value.length >= trendTotal.value) return;
  currentPage.value += 1;
  getFollowTrends();
};

const toMain = (noteId: string) => {
  nid.value = noteId;
  mainShow.value = true;
};

const close = (nid: string, val: any) => {
  const index = trendData.value.findIndex((item) => item.nid === nid);
  console.log("---val", val, index);
  const _data = trendData.value[index];
  if (_data.isLike != val.isLike) {
    _data.isLike = val.isLike;
    _data.likeCount += val.isLike ? 1 : -1;
  }
  if (val.isComment) {
    _data.commentCount += 1;
  }
  mainShow.value = false;
};

const goToPublish = () => {
  router.push({ path: "/push", query: { tab: "note" } });
};

const refresh = () => {
  refreshTab(() => {
    topLoading.value = true;
    currentPage.value = 1;
    trendData.value = [];
    listLoaded.value = false;
    getFollowTrends();
    topLoading.value = false;
  });
};

const like = (nid: string, uid: string, index: number, val: number) => {
  likeOrCollectionDTO.value.likeOrCollectionId = nid;
  likeOrCollectionDTO.value.publishUid = uid;
  likeOrCollectionDTO.value.type = 1;
  likeOrCollectionByDTO(likeOrCollectionDTO.value).then(() => {
    if (val < 0 && trendData.value[index].likeCount == 0) {
      return;
    }
    trendData.value[index].isLike = val == 1;
    trendData.value[index].likeCount += val;
  });
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  currentPage.value = 1;
  trendData.value = [];
  listLoaded.value = false;
  getFollowTrends();
};

// 将富文本转为安全纯文本展示
const formatContent = (html: string): string => {
  if (!html) return "";
  let text = html
    .replace(/<\s*br\s*\/?>/gi, "\n") // br换行
    .replace(/<\s*\/p\s*>/gi, "\n") // 段落换行
    .replace(/<[^>]*>/g, "") // 移除所有标签
    .replace(/&nbsp;/gi, " ")
    .replace(/&amp;/gi, "&")
    .replace(/&lt;/gi, "<")
    .replace(/&gt;/gi, ">")
    .replace(/&quot;/gi, '"')
    .replace(/&#39;/gi, "'");
  // 压缩多余空白
  return text.replace(/\s+$/g, "").trim();
};

// 格式化地址显示（只显示市+详细地址）
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

// 初始化时根据当前路由设置正确的 pageType
onMounted(() => {
  const currentPath = router.currentRoute.value.path;
  if (currentPath === "/idleTrend") {
    pageType.value = "2";
  } else {
    pageType.value = "1";
  }
  initData();
});

// 监听页面类型变化
watch(pageType, (newVal) => {
  // 重置数据
  currentPage.value = 1;
  trendData.value = [];
  trendTotal.value = 0;
  if (newVal === "1") {
    listLoaded.value = false;
    getFollowTrends();
  } else {
    router.push("/idleTrend");
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

.trend-page {
  /* 在 .main-content 剩余宽度内水平居中（不再叠加固定 margin-left，避免随屏宽跑偏） */
  width: 100%;
  max-width: 800px;
  margin: 50px auto;
  margin-left: 150px;
  padding: 20px;
  box-sizing: border-box;

  // 移动端为底部导航栏留出空间
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
    max-width: 650px;
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
      /* 竖排文字（笔记 / 商品 等两字一列） */
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

  .trend-main .trend-list {
    margin-top: 20px;
  }

  .trend-list {
    margin-top: 20px;
    display: flex;
    flex-direction: column;
    gap: 20px;
    width: 100%;
    margin-left: auto;
    margin-right: auto;

    .card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
      overflow: hidden;
      transition: transform 0.3s ease;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }

      .card-content {
        display: flex;
        padding: 16px;
        gap: 20px;
        cursor: pointer;
      }

      .note-image {
        position: relative;
        width: 200px;
        height: 200px;
        flex-shrink: 0;

        .el-image {
          width: 100%;
          height: 100%;
          border-radius: 8px;
        }

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
        }

        .more-count {
          position: absolute;
          top: 10px;
          right: 10px;
          background: rgba(0, 0, 0, 0.356);
          color: white;
          padding: 4px 9px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: bold;
        }
      }

      .note-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .note-content {
          margin-bottom: 12px;

          .title {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin: 0 0 8px 0;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            max-height: 2.8em; /* 2行的高度 */
          }

          .content-text {
            font-size: 14px;
            color: #666;
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 6;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            max-height: 9em; /* 6行的高度 */
          }
        }

        .user-info {
          display: flex;
          align-items: center;

          .avatar {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            margin-right: 6px;
            object-fit: cover;
            cursor: pointer;
          }

          .username {
            font-size: 12px;
            color: #666;
            font-weight: 500;
            cursor: pointer;

            &:hover {
              color: @primary-color;
            }
          }
        }

        .note-meta {
          margin-top: 16px;

          .bottom-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: nowrap;
            gap: 12px;

            .left-group {
              display: flex;
              align-items: center;
              gap: 16px;
              flex: 1;
              min-width: 0;

              .user-info {
                display: flex;
                align-items: center;
                flex-shrink: 0;

                .avatar {
                  width: 28px;
                  height: 28px;
                  border-radius: 50%;
                  margin-right: 6px;
                  object-fit: cover;
                  cursor: pointer;
                }

                .username {
                  font-size: 14px;
                  color: #666;
                  font-weight: 500;
                  cursor: pointer;
                  white-space: nowrap;
                  margin-right: 10px;
                  max-width: 72px;
                  overflow: hidden;
                  text-overflow: ellipsis;

                  &:hover {
                    color: @primary-color;
                  }
                }
              }

              .location {
                display: flex;
                align-items: center;
                gap: 3px;
                cursor: pointer;
                transition: all 0.3s;
                color: #1989fa;
                flex: 1;
                min-width: 0;
                max-width: 100%;

                .el-icon {
                  font-size: 16px;
                  flex-shrink: 0;
                }

                span {
                  font-size: 14px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }
              }
            }

            .right-group {
              display: flex;
              align-items: center;
              gap: 12px;
              flex-shrink: 0;

              .actions {
                display: flex;
                align-items: center;
                gap: 6px;
                margin-right: 0;
                flex-shrink: 0;

                .action-btn {
                  display: flex;
                  align-items: center;
                  background: none;
                  border: none;
                  padding: 4px 8px;
                  cursor: pointer;
                  color: #666;
                  font-size: 14px;
                  border-radius: 4px;
                  transition: all 0.2s ease;

                  i {
                    margin-right: 4px;
                    font-size: 16px;
                    transition: transform 0.2s ease;

                    &.liked {
                      color: @primary-color;
                    }
                  }

                  &:hover {
                    color: @primary-color;
                    background: rgba(255, 65, 108, 0.08);

                    i {
                      transform: scale(1.1);
                    }
                  }
                }
              }

              .time {
                color: #909399;
                white-space: nowrap;
                font-size: 13px;
                flex-shrink: 0;
                min-width: 66px;
                text-align: right;
              }
            }
          }
        }
      }
    }
  }

  .login-prompt {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 60vh;

    .el-button {
      margin-top: 20px;
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

<style lang="less">
/* 暗色模式：胶囊轨道与文字对比度（html.dark-mode 由 themeStore 切换） */
html.dark-mode .trend-page .tab-container {
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
</style>
