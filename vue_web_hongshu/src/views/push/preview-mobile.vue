<template>
  <div class="preview-container">
    <div class="mobile-wrapper">
      <!-- 状态栏 -->
      <div class="status-bar">
        <span class="time">00:00</span>
        <div class="right-icons">
          <span class="signal">
            <svg
              t="1736094828268"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="12068"
              width="16"
              height="16"
            >
              <path
                d="M128 597.333333h128v213.333334H128v-213.333334z m213.333333-128h128v341.333334H341.333333v-341.333334z m213.333334-128h128v469.333334h-128V341.333333z m213.333333-128h128v597.333334h-128V213.333333z"
                fill="#000000"
                p-id="12069"
              ></path>
            </svg>
          </span>
          <span class="wifi">
            <svg
              t="1736094928620"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="13444"
              width="14"
              height="14"
            >
              <path
                d="M0 352.832l93.12 98.752c231.296-245.44 606.464-245.44 837.76 0L1024 352.832C741.44 53.056 283.008 53.056 0 352.832z m372.352 395.008L512 896l139.648-148.16c-76.8-81.92-202.048-81.92-279.296 0zM186.24 550.4l93.12 98.752c128.448-136.32 336.96-136.32 465.408 0L837.824 550.4c-179.648-190.592-471.488-190.592-651.648 0z"
                fill="#000000"
                p-id="13445"
              ></path>
            </svg>
          </span>
          <span class="battery">
            <svg
              t="1736094793598"
              class="icon"
              viewBox="0 0 1828 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="10942"
              width="16"
              height="16"
            >
              <path
                d="M60.275337 3.248868l-57.025203 57.025203C-8.154907 10.377019 10.378284-8.156172 60.275337 3.248868z"
                fill="#2c2c2c"
                p-id="10943"
              ></path>
              <path
                d="M1822.354106 673.295002V345.400086c0-28.512601 0-61.302093-22.810081-79.835284-41.343272-34.215122-48.471422-79.835284-54.173943-126.881077-7.12815-59.876463-25.661341-111.199146-88.389064-135.434857H60.275337C28.911475 10.377019 10.378284 28.91021 3.250134 60.274071v898.146946C24.634585 1014.020589 71.680377 1022.57437 128.70558 1024h1459.845194c95.517215 0 134.009227-29.938232 153.968048-122.604186 12.830671-59.876463 12.830671-122.604186 67.004613-166.798719 15.681931-14.256301 12.830671-39.917642 12.830671-61.302093z m-104.070995-52.748312c1.42563 31.363862-11.405041 44.194532-41.343272 38.492012-57.025203-11.405041-64.153353 18.533191-64.153354 67.004613 1.42563 188.18317 0 188.18317-186.757539 188.183169-420.560871 0-839.696112-1.42563-1260.256984 1.425631-62.727723 0-81.260914-15.681931-79.835284-78.409654 4.27689-222.398291 2.85126-446.222213 0-668.620504-1.42563-54.173943 15.681931-68.430243 68.430244-67.004614 235.228962 1.42563 467.606664 0 699.984365 0 232.377702 0 464.755404 1.42563 697.133105-1.42563 55.599573-1.42563 71.281504 15.681931 65.578984 68.430244-4.27689 32.789492 1.42563 67.004613-1.425631 99.794105-2.85126 54.173943-5.70252 102.645365 72.707134 88.389064 25.661341-4.27689 29.938232 14.256301 29.938232 35.640752 0 75.558394-1.42563 152.542418 0 228.100812z"
                fill="#2c2c2c"
                p-id="10944"
              ></path>
              <path
                d="M851.500027 814.432379c-199.58821 0-399.17642-1.42563-598.76463 1.42563-51.322683 1.42563-62.727723-14.256301-62.727724-64.153353 2.85126-166.798718 2.85126-332.171807 0-498.970525 0-38.492012 8.55378-52.748313 49.897053-52.748313 407.730201 1.42563 816.886031 1.42563 1224.616232 0 41.343272 0 51.322683 12.830671 51.322682 52.748313-1.42563 168.224349-2.85126 336.448697 0 506.098676 1.42563 49.897053-17.107561 57.025203-61.302093 57.025202-199.58821-2.85126-402.02768-1.42563-603.04152-1.42563z"
                fill="#2c2c2c"
                p-id="10945"
              ></path>
            </svg>
          </span>
        </div>
      </div>

      <div class="content-area">
        <!-- 详情模式 -->
        <template v-if="previewMode === 'detail'">
          <!-- 顶部用户信息 -->
          <div class="user-bar">
            <div class="left">
              <i class="el-icon-arrow-left"></i>
              <el-avatar :src="userInfo.avatar" :size="30" />
              <span class="username">{{ userInfo.username }}</span>
            </div>
            <div class="right">
              <span class="follow-btn">{{ $t("common.follow") }}</span>
              <i class="el-icon-share"></i>
            </div>
          </div>
          <!-- 预览内容区 -->
          <div class="preview-content">
            <div class="media-container">
              <!-- 根据当前模式显示图片或视频 -->
              <template v-if="contentType === 'image'">
                <div v-if="validFileList.length > 0" class="image-slider">
                  <!-- 左右切换按钮 -->
                  <div class="arrow left" @click="prevImage" v-show="currentIndex > 0"></div>
                  <div class="arrow right" @click="nextImage" v-show="currentIndex < validFileList.length - 1"></div>
                  <!-- 图片展示 -->
                  <img
                    v-if="validFileList[currentIndex] && validFileList[currentIndex].url"
                    :key="`preview-img-${currentIndex}-${
                      validFileList[currentIndex]?.uid || validFileList[currentIndex]?.url
                    }`"
                    :src="validFileList[currentIndex].url"
                    class="preview-image"
                  />
                  <!-- 底部指示点 -->
                  <div class="indicator-dots" v-if="validFileList.length > 1">
                    <span
                      v-for="(_, index) in validFileList"
                      :key="index"
                      :class="['dot', { active: currentIndex === index }]"
                      @click="currentIndex = index"
                    ></span>
                  </div>
                </div>
              </template>
              <!-- 视频类型 -->
              <template v-else-if="contentType === 'video'">
                <video v-if="videoUrl" :src="videoUrl" class="preview-video" controls></video>
              </template>
            </div>
            <!-- 底部信息 -->
            <div class="post-info">
              <p class="post-time">
                {{ $t("preview.publishedAt") }} {{ getCurrentTime() }} {{ $t("preview.beijing") }}
              </p>
              <!-- 底部操作栏 -->
              <div class="action-bar">
                <div class="action-item">
                  <i class="el-icon-chat-dot-round"></i>
                  <span>{{ $t("preview.saySomething") }}</span>
                </div>
                <div class="action-icons">
                  <i class="el-icon-star-off"></i>
                  <i class="el-icon-collection"></i>
                  <i class="el-icon-more"></i>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 双列模式 -->
        <template v-else>
          <!-- App 导航栏 -->
          <div class="app-header">
            <div class="nav-tabs">
              <span class="mobile-tab-item">{{ $t("preview.tabFollowing") }}</span>
              <span class="mobile-tab-item active">{{ $t("preview.tabNotes") }}</span>
              <span class="mobile-tab-item">{{ $t("preview.tabNearby") }}</span>
            </div>
          </div>

          <!-- 搜索栏 -->
          <div class="search-bar">
            <i class="el-icon-search"></i>
            <span class="placeholder">{{ $t("preview.searchNotesProductsUsers") }}</span>
          </div>

          <!-- 分类标签栏 -->
          <div class="category-tabs">
            <div class="tab-scroll">
              <span class="category-tab-item active">{{ $t("dashboard.recommend") }}</span>
              <span class="category-tab-item">{{ $t("preview.fashionOutfit") }}</span>
              <span class="category-tab-item">{{ $t("preview.personalCare") }}</span>
              <span class="category-tab-item">{{ $t("preview.drawing") }}</span>
              <span class="category-tab-item">{{ $t("preview.recipe") }}</span>
              <span class="category-tab-item">{{ $t("preview.digital") }}</span>
            </div>
          </div>
          <div class="grid-view">
            <div class="grid-item" v-if="contentType === 'image' && validFileList.length > 0">
              <img v-if="validFileList[0] && validFileList[0].url" :src="validFileList[0].url" class="grid-image" />
              <div class="grid-info">
                <p class="grid-title">{{ displayNote.title || $t("note.titlePlaceholder") }}</p>
                <div class="grid-user">
                  <el-avatar :src="userInfo.avatar" :size="20" />
                  <span>{{ userInfo.username }}</span>
                </div>
              </div>
            </div>
            <div class="grid-item" v-else-if="contentType === 'video' && videoUrl">
              <video :src="videoUrl" class="grid-image" controls />
              <div class="grid-info">
                <p class="grid-title">{{ displayNote.title || $t("note.titlePlaceholder") }}</p>
                <div class="grid-user">
                  <el-avatar :src="userInfo.avatar" :size="20" />
                  <span>{{ userInfo.nickname }}</span>
                </div>
              </div>
            </div>
            <!-- 添加一些示例项目 -->
            <div class="grid-item placeholder" v-for="n in 5" :key="n">
              <div class="grid-image placeholder"></div>
              <div class="grid-info">
                <p class="grid-title">{{ $t("preview.sampleNoteTitle") }}</p>
                <div class="grid-user">
                  <div class="avatar placeholder"></div>
                  <span>{{ $t("preview.sampleUsername") }}</span>
                </div>
              </div>
            </div>
            <!-- 底部导航栏 -->
            <div class="bottom-nav">
              <div class="nav-item active">
                <i class="el-icon-house"></i>
                <span>{{ $t("preview.home") }}</span>
              </div>
              <div class="nav-item">
                <i class="el-icon-shopping-bag-1"></i>
                <span>{{ $t("preview.mall") }}</span>
              </div>
              <div class="nav-item publish">
                <div class="publish-btn">
                  <i class="el-icon-plus">+</i>
                </div>
              </div>
              <div class="nav-item">
                <i class="el-icon-message"></i>
                <span>{{ $t("preview.messages") }}</span>
              </div>
              <div class="nav-item">
                <i class="el-icon-user"></i>
                <span>{{ $t("preview.me") }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
    <!-- 在 content-area 底部添加切换按钮 -->
    <div class="view-switcher">
      <div :class="['switch-btn', { active: previewMode === 'detail' }]" @click="previewMode = 'detail'">
        {{ $t("preview.detailView") }}
      </div>
      <div :class="['switch-btn', { active: previewMode === 'grid' }]" @click="previewMode = 'grid'">
        {{ $t("preview.gridView") }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue";
import type { UploadUserFile } from "element-plus";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/userStore";

const { locale } = useI18n();

const props = defineProps({
  currentTab: {
    type: String,
    required: true,
  },
  contentType: {
    type: String,
    default: "image",
  },
  fileList: {
    type: Array as () => UploadUserFile[],
    default: () => [],
  },
  videoUrl: {
    type: String,
    default: "",
  },
  coverUrl: {
    type: String,
    default: "",
  },
  note: {
    type: Object,
    default: () => ({
      title: "",
      content: "",
    }),
  },
  dynamicTags: {
    type: Array as () => string[],
    default: () => [],
  },
  locationInfo: {
    type: String,
    default: "",
  },
  product: {
    type: Object,
    default: () => ({
      price: "",
      originalPrice: "",
      postType: "0",
    }),
  },
});

// 状态管理
const previewMode = ref("detail"); // 预览模式：'detail' | 'grid'
const currentIndex = ref(0);
const userInfo = ref<any>({});

const displayNote = computed(() => props.note || {});
const userStore = useUserStore();

// 过滤出有效的文件列表（有 url 的文件）
const validFileList = computed(() => {
  return props.fileList.filter((file) => file && file.url);
});

// 图片切换方法
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

const nextImage = () => {
  if (currentIndex.value < validFileList.value.length - 1) {
    currentIndex.value++;
  }
};

// 添加获取当前时间的方法
const getCurrentTime = () => {
  const now = new Date();
  const loc = locale.value === "en-US" ? "en-US" : "zh-CN";
  return now.toLocaleString(loc, {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const initData = () => {
  userInfo.value = userStore.getUserInfo();
};

onMounted(() => {
  initData();

  document.addEventListener("keydown", (e) => {
    if (e.key === "ArrowLeft") {
      prevImage();
    } else if (e.key === "ArrowRight") {
      nextImage();
    }
  });
});

// 监听文件列表变化
watch(
  () => props.fileList,
  (newList, oldList) => {
    if (!newList || newList.length === 0) {
      currentIndex.value = 0;
      return;
    }

    // 确保所有文件都有 url
    newList.forEach((file) => {
      if (!file.url && file.raw) {
        file.url = URL.createObjectURL(file.raw);
      }
    });

    // 检查文件列表是否真的发生了变化（通过比较 uid 或 url）
    const newUids = newList.map((f) => f.uid || f.url).join(",");
    const oldUids = oldList?.map((f) => f.uid || f.url).join(",") || "";

    // 如果文件列表发生变化，重置索引
    if (newUids !== oldUids || newList.length !== (oldList?.length || 0)) {
      currentIndex.value = 0;
    }

    // 确保索引不越界
    if (currentIndex.value >= validFileList.value.length) {
      currentIndex.value = Math.max(0, validFileList.value.length - 1);
    }
  },
  { deep: true, immediate: true }
);

// 监听有效文件列表变化，确保索引不越界
watch(validFileList, (newList) => {
  if (newList.length === 0) {
    currentIndex.value = 0;
    return;
  }
  if (currentIndex.value >= newList.length) {
    currentIndex.value = Math.max(0, newList.length - 1);
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

.preview-container {
  position: fixed;
  top: 140px;
  left: calc(31vw + 700px);
  height: calc(100vh - 112px);

  .view-switcher {
    position: absolute;
    top: 620px; // ✅ mobile-wrapper 高度 650px + 间距 20px
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    background: #f5f5f5;
    border-radius: 20px;
    padding: 2px;
    z-index: 10;

    .switch-btn {
      padding: 6px 20px;
      border-radius: 16px;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;

      &.active {
        background: #fff;
        color: @primary-color;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
    }
  }

  .mobile-wrapper {
    width: 300px;
    height: 600px;
    background: #fff;
    border-radius: 40px;
    box-shadow: 0 0 24px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    position: relative;

    // 手机状态栏样式
    .status-bar {
      height: 35px;
      background: #fff;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;
      font-size: 14px;

      .right-icons {
        display: flex;
        gap: 4px;
        align-items: center;
      }
    }

    // App导航栏样式
    .app-header {
      padding: 2px 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-bottom: 1px solid #f5f5f5;

      .nav-tabs {
        display: flex;
        gap: 24px;

        .mobile-tab-item {
          font-size: 15px;
          color: #666;
          position: relative;
          padding: 4px 0;
          cursor: pointer;

          &.active {
            color: #000;
            font-weight: 500;

            &::after {
              content: "";
              position: absolute;
              bottom: -2px;
              left: 50%;
              transform: translateX(-50%);
              width: 20px;
              height: 2px;
              background: @primary-color;
              border-radius: 1px;
            }
          }
        }
      }
    }

    // 搜索栏样式
    .search-bar {
      margin: 8px 16px;
      height: 25px;
      background: #f5f5f5;
      border-radius: 18px;
      display: flex;
      align-items: center;
      justify-content: center; // 内容居中
      padding: 0 16px;
      color: #999;

      .el-icon-search {
        margin-right: 8px;
      }

      .placeholder {
        font-size: 12px;
      }
    }

    // 分类标签样式
    .category-tabs {
      padding: 0 12px;

      .tab-scroll {
        display: flex;
        overflow-x: auto;
        scrollbar-width: none;
        -ms-overflow-style: none;
        &::-webkit-scrollbar {
          display: none;
        }

        .category-tab-item {
          flex-shrink: 0;
          padding: 8px 8px;
          font-size: 12px;
          color: #666;
          position: relative;

          &.active {
            color: @primary-color;
            font-weight: 500;
          }
        }
      }
    }

    .bottom-nav {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 50px;
      background: #fff;
      display: flex;
      align-items: center;
      justify-content: space-around;
      border-top: 1px solid #f5f5f5;

      .nav-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        color: #666;
        font-size: 12px;
        gap: 4px;

        &.active {
          color: @primary-color;
        }

        &.publish {
          .publish-btn {
            width: 40px;
            height: 40px;
            background: @primary-color;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-size: 20px;
          }
        }

        i {
          font-size: 20px;
        }
      }
    }

    .grid-view {
      padding: 12px;
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
      overflow-y: auto;
      height: calc(100% + 44px);

      .grid-item {
        background: #fff;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

        &.placeholder {
          .grid-image {
            background: #f5f5f5;
          }
          .avatar {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background: #f5f5f5;
          }
        }

        .grid-image {
          width: 100%;
          aspect-ratio: 1;
          object-fit: cover;
        }

        .grid-info {
          padding: 8px;

          .grid-title {
            font-size: 12px;
            margin-top: 5px;
            margin-bottom: 5px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .grid-user {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: #999;

            .el-avatar {
              width: 20px;
              height: 20px;
            }
          }
        }
      }
    }

    // 内容区域样式
    .content-area {
      height: calc(100% - 4px);
      background: #fff;
      overflow-y: auto;

      .user-bar {
        padding: 10px 15px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid #f5f5f5;

        .left {
          display: flex;
          align-items: center;
          gap: 12px;

          .avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
          }

          .username {
            font-size: 15px;
            font-weight: 500;
          }
        }

        .right {
          display: flex;
          align-items: center;
          gap: 16px;

          .follow-btn {
            padding: 4px 12px;
            border-radius: 16px;
            background: @primary-color;
            color: #fff;
            font-size: 14px;
          }
        }
      }

      .preview-content {
        .media-container {
          background: #f8f8f8;
          position: relative;
          overflow: hidden;

          .image-slider {
            width: 100%;
            // height: 100%;
            position: relative;

            .preview-image {
              width: 100%;
              height: 400px;
              // 改用cover而不是contain
              object-fit: cover;
            }

            // 左右箭头样式
            .arrow {
              position: absolute;
              top: 50%;
              transform: translateY(-50%);
              width: 40px;
              height: 40px;
              display: flex;
              align-items: center;
              justify-content: center;
              cursor: pointer;
              z-index: 2;

              &::before {
                content: "";
                display: block;
                width: 12px;
                height: 12px;
                border-width: 2px 2px 0 0;
                border-style: solid;
                border-color: #fff;
              }

              &.left {
                left: 14px;
                &::before {
                  transform: rotate(-135deg);
                }
              }

              &.right {
                right: 5px;
                &::before {
                  transform: rotate(45deg);
                }
              }
            }

            // 底部指示点样式
            .indicator-dots {
              position: absolute;
              bottom: 10px;
              left: 50%;
              transform: translateX(-50%);
              display: flex;
              gap: 8px;
              z-index: 2;

              .dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: rgba(185, 181, 181, 0.5);
                cursor: pointer;
                transition: all 0.3s;

                &.active {
                  background: @primary-color;
                  transform: scale(1.2);
                }
              }
            }
          }
          .preview-video {
            width: 100%;
            height: 380px; // 设置固定高度，比图片低一些
            object-fit: contain;
          }
        }

        .post-info {
          padding: 16px;

          .post-time {
            font-size: 12px;
            color: #999;
            margin-bottom: 8px;
          }

          .post-title {
            font-size: 16px;
            font-weight: 500;
            margin-bottom: 8px;
          }

          .post-desc {
            font-size: 14px;
            color: #666;
            margin-bottom: 16px;
          }

          .action-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-top: 12px;
            border-top: 1px solid #f5f5f5;

            .action-item {
              display: flex;
              align-items: center;
              gap: 8px;
              color: #999;
            }

            .action-icons {
              display: flex;
              gap: 24px;
              color: #666;
            }
          }
        }
      }
    }
  }

  // 底部导航栏样式
  .bottom-nav {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 50px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: space-around;
    border-top: 1px solid #f5f5f5;

    .nav-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #666;
      font-size: 12px;
      gap: 4px;
      cursor: pointer;

      &.active {
        color: @primary-color;
      }

      &.publish {
        .publish-btn {
          width: 40px;
          height: 40px;
          background: @primary-color;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
        }
      }
    }
  }
}
</style>