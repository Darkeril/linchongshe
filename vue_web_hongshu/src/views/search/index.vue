<template>
  <div class="feeds-page">
    <div class="middle">
      <div id="search-type">
        <div data-v-7f9e6aac="" class="scroll-container channel-scroll-container">
          <div class="content-container">
            <div :class="typeClass == 0 ? 'channel active' : 'channel'" @click="getNoteByType(0)">
              {{ $t("search.all") }}
            </div>
            <div :class="typeClass == 1 ? 'channel active' : 'channel'" @click="getNoteByType(1)">
              {{ $t("search.hottest") }}
            </div>
            <div :class="typeClass == 2 ? 'channel active' : 'channel'" @click="getNoteByType(2)">
              {{ $t("search.latest") }}
            </div>
            <div style="line-height: 40px">|</div>
            <div :class="typeClass == 3 ? 'channel active' : 'channel'" @click="getUserData">
              {{ $t("search.users") }}
            </div>
            <div :class="typeClass == 4 ? 'channel active' : 'channel'" @click="getNoteByType(4)">
              {{ $t("search.idleItems") }}
            </div>
          </div>
        </div>
      </div>
      <div class="filter-box">
        <div>
          <div class="filter">
            <span>{{ $t("search.comprehensive") }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="divider rec-filter"></div>
    <div class="note-container" v-if="!isShowUser">
      <div class="channel-container">
        <div class="scroll-container channel-scroll-container">
          <div class="content-container">
            <!-- 笔记分类 -->
            <template v-if="typeClass !== 4">
              <div :class="categoryClass == '0' ? 'channel active' : 'channel'" @click="getNoteList">
                {{ $t("search.recommendNote") }}
              </div>
              <div
                :class="categoryClass == item.id ? 'channel active' : 'channel'"
                v-for="item in categoryList"
                :key="item.id"
                @click="getNoteListByCategory(item.id)"
              >
                {{ item.title }}
              </div>
            </template>
            <!-- 商品分类 -->
            <template v-else>
              <div :class="categoryClass == '0' ? 'channel active' : 'channel'" @click="getProductList">
                {{ $t("search.recommendProduct") }}
              </div>
              <div
                :class="categoryClass == item.id ? 'channel active' : 'channel'"
                v-for="item in productCategoryList"
                :key="item.id"
                @click="getProductListByCategory(item.id)"
              >
                {{ item.title }}
              </div>
            </template>
          </div>
        </div>
      </div>
      <div class="loading-container"></div>
      <!-- 笔记瀑布流 -->
      <div
        class="feeds-container"
        v-if="typeClass !== 4"
        v-infinite-scroll="loadMoreData"
        :infinite-scroll-distance="50"
        :infinite-scroll-disabled="mainShow || loadingMore"
      >
        <div class="feeds-loading-top animate__animated animate__zoomIn animate__delay-0.5s" v-show="topLoading">
          <Loading style="width: 1.2em; height: 1.2em"></Loading>
        </div>
        <Waterfall
          :list="noteList"
          :width="options.width"
          :gutter="options.gutter"
          :hasAroundGutter="hasAroundGutter"
          :animation-effect="options.animationEffect"
          :animation-duration="options.animationDuration"
          :animation-delay="options.animationDelay"
          :breakpoints="options.breakpoints"
          :style="{ minWidth: isMobile ? '100%' : '740px' }"
        >
          <template #item="{ item }">
            <el-skeleton style="width: 15rem" :loading="!item.isLoading" animated>
              <template #template>
                <el-image
                  :src="item.noteCover"
                  :style="{
                    width: '15rem',
                    maxHeight: '18.75rem',
                    height: item.noteCoverHeight + 'px',
                    borderRadius: '.5rem',
                  }"
                  @load="handleLoad(item)"
                ></el-image>
                <div style="padding: 0.875rem">
                  <el-skeleton-item variant="h3" style="width: 100%" />
                  <div style="display: flex; align-items: center; margin-top: 0.125rem; height: 1rem">
                    <el-skeleton style="--el-skeleton-circle-size: 1.25rem">
                      <template #template>
                        <el-skeleton-item variant="circle" />
                      </template>
                    </el-skeleton>
                    <el-skeleton-item variant="text" style="margin-left: 0.625rem" />
                  </div>
                </div>
              </template>

              <template #default>
                <div class="card">
                  <el-image
                    :src="item.noteCover"
                    fit="cover"
                    class="note-cover-img"
                    @click="toMain(item.id)"
                  ></el-image>
                  <div class="footer">
                    <a class="title">
                      <span>{{ item.title }}</span>
                    </a>
                    <div class="author-wrapper">
                      <a class="author">
                        <img class="author-avatar" :src="item.avatar" />
                        <span class="name">{{ item.username }}</span>
                      </a>
                      <span class="like-wrapper like-active">
                        <i class="iconfont icon-follow" style="width: 1em; height: 1em"></i>
                        <span class="count">{{ item.likeCount }}</span>
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </el-skeleton>
          </template>
        </Waterfall>

        <div class="feeds-loading">
          <Loading style="width: 1.2em; height: 1.2em" v-show="botLoading"></Loading>
        </div>
      </div>
      <!-- 闲置商品瀑布流 -->
      <div
        class="feeds-container"
        v-else
        v-infinite-scroll="loadMoreProduct"
        :infinite-scroll-distance="50"
        :infinite-scroll-disabled="mainShow || loadingMore"
      >
        <div class="feeds-loading-top animate__animated animate__zoomIn animate__delay-0.5s" v-show="topLoading">
          <Loading style="width: 1.2em; height: 1.2em"></Loading>
        </div>
        <Waterfall
          :list="productDataList"
          :width="options.width"
          :gutter="options.gutter"
          :hasAroundGutter="hasAroundGutter"
          :animation-effect="options.animationEffect"
          :animation-duration="options.animationDuration"
          :animation-delay="options.animationDelay"
          :breakpoints="options.breakpoints"
          :style="{ minWidth: isMobile ? '100%' : '740px' }"
        >
          <template #item="{ item }">
            <el-skeleton style="width: 15rem" :loading="!item.isLoading" animated>
              <template #template>
                <el-image
                  :src="item.cover"
                  :style="{
                    width: '15rem',
                    maxHeight: '18.75rem',
                    height: item.coverHeight + 'px',
                    borderRadius: '.5rem',
                  }"
                  @load="handleLoad(item)"
                ></el-image>
                <div style="padding: 0.875rem">
                  <el-skeleton-item variant="h3" style="width: 100%" />
                  <div style="display: flex; align-items: center; margin-top: 0.125rem; height: 1rem">
                    <el-skeleton style="--el-skeleton-circle-size: 1.25rem">
                      <template #template>
                        <el-skeleton-item variant="circle" />
                      </template>
                    </el-skeleton>
                    <el-skeleton-item variant="text" style="margin-left: 0.625rem" />
                  </div>
                </div>
              </template>

              <template #default>
                <div class="card">
                  <el-image
                    :src="item.cover"
                    fit="cover"
                    class="product-cover-img"
                    @click="toProduct(item.id)"
                  ></el-image>
                  <div class="footer">
                    <a class="title">
                      <span>{{ item.title }}</span>
                    </a>
                    <div class="author-wrapper">
                      <a class="author">
                        <img class="author-avatar" :src="item.avatar" />
                        <span class="name">{{ item.username }}</span>
                      </a>
                      <span class="like-wrapper like-active">
                        <svg
                          t="1739470033309"
                          class="icon"
                          viewBox="0 0 1024 1024"
                          version="1.1"
                          xmlns="http://www.w3.org/2000/svg"
                          p-id="19111"
                          width="30"
                          height="30"
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
                        <span class="count">{{ item.likeCount }}</span>
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </el-skeleton>
          </template>
        </Waterfall>
        <div class="feeds-loading">
          <Loading style="width: 1.2em; height: 1.2em" v-show="botLoading"></Loading>
        </div>
      </div>
    </div>
    <!-- 用户 -->
    <div class="user-container" v-else>
      <ul class="agree-container" v-infinite-scroll="loadMoreUser" :infinite-scroll-distance="20">
        <li class="agree-item" v-for="(item, index) in userDataList" :key="index">
          <a class="user-avatar">
            <img class="avatar-item" :src="item.avatar" @click="toUser(item.id)" />
          </a>
          <div class="main">
            <div class="info">
              <div class="user-info">
                <a class>{{ item.username }}</a>
              </div>
              <div class="interaction-hint">
                <span>{{ $t("search.fansCount") }}:</span><span>{{ item.fanCount }}</span
                >&nbsp;|&nbsp;<span>{{ $t("search.followCount") }}:&nbsp;</span><span>{{ item.followerCount }}</span
                >&nbsp;|&nbsp;<span>{{ $t("search.notesCount") }}:&nbsp;</span><span>{{ item.noteCount }}</span>
              </div>
            </div>
            <div class="extra">
              <el-button type="info" round size="large" v-if="item.isFollow" @click="follow(item.uid, index, 1)">{{
                $t("common.followed")
              }}</el-button>
              <el-button type="primary" round size="large" v-else @click="follow(item.uid, index, -1)">{{
                $t("common.followBack")
              }}</el-button>
            </div>
          </div>
        </li>
      </ul>
    </div>

    <FloatingBtn @click-refresh="refresh"></FloatingBtn>
    <Main
      v-show="mainShow && typeClass !== 4"
      :nid="nid"
      scene="search"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="close"
    >
    </Main>
    <IdleMain
      v-show="mainShow && typeClass === 4"
      :pid="pid"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="close"
    >
    </IdleMain>
  </div>
</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { getNoteByDTO, getCategoryAgg } from "@/api/search";
import type { NoteDTO, NoteSearch } from "@/type/note";
import type { EsProductDTO } from "@/type/product";
import type { Category } from "@/type/category";
import Main from "@/views/main/main.vue";
import IdleMain from "@/views/idle-main/main.vue";
import FloatingBtn from "@/components/FloatingBtn.vue";
import { options } from "@/constant/constant";
import Loading from "@/components/Loading.vue";
import { refreshTab } from "@/utils/util";
import { useRoute, useRouter } from "vue-router";
import { getUserByKeyword } from "@/api/user";
import { getProductByKeyword } from "@/api/idle";
import { getProductCategoryAgg } from "@/api/idleCategory";

import { followById } from "@/api/follower";

const { t } = useI18n();
void t("common.confirm");
const route = useRoute();
const router = useRouter();
const topLoading = ref(false);
const botLoading = ref(false);
const noteList = ref<Array<NoteSearch>>([]);
const categoryList = ref<Array<Category>>([]);
const productCategoryList = ref<Array<Category>>([]);
const currentPage = ref(1);
const pageSize = 20;
const noteTotal = ref(0);
const currentUserPage = ref(1);
const userPageSize = 15;
const categoryClass = ref("0");
const typeClass = ref(0);
const mainShow = ref(false);
const nid = ref("");
const pid = ref("");

// 移动端检测
const isMobile = computed(() => window.innerWidth <= 695);
// 移动端关闭周围gutter，减少左右留白
const hasAroundGutter = computed(() => !isMobile.value);

const noteDTO = ref<NoteDTO>({
  keyword: "",
  type: 0,
  cid: "",
  cpid: "",
});
const productDTO = ref<EsProductDTO>({
  keyword: "",
  type: 0,
  cid: "",
  cpid: "",
});
const loadingMore = ref(false);
const isShowUser = ref(false);
const userDataList = ref<Array<any>>([]);
const productDataList = ref<Array<any>>([]);
const userTotal = ref(0);

const getUserData = () => {
  typeClass.value = 3;
  isShowUser.value = true;
  currentUserPage.value = 1;
  userDataList.value = [];
  getUserByKeyword(currentUserPage.value, userPageSize, noteDTO.value.keyword).then((res: any) => {
    const { records, total } = res.data;
    userDataList.value.push(...records);
    userTotal.value = total;
  });
};

const getProductData = () => {
  loadingMore.value = true;
  botLoading.value = true;

  getProductByKeyword(currentUserPage.value, pageSize, noteDTO.value.keyword) // 添加分类参数
    .then((res: any) => {
      const { records, total } = res.data;
      records.forEach((item: any) => {
        item.isLoading = false;
        item.coverHeight = 240;
      });
      productDataList.value = records;
      userTotal.value = total;

      setTimeout(() => {
        records.forEach((item: any) => {
          item.isLoading = true;
        });
      }, 500);
    })
    .finally(() => {
      loadingMore.value = false;
      setTimeout(() => {
        botLoading.value = false;
      }, 500);
    });
};

const follow = (fid: string, index: number, type: number) => {
  followById(fid).then(() => {
    userDataList.value[index].isFollow = type == -1;
  });
};

const loadMoreUser = () => {
  botLoading.value = true;
  loadingMore.value = true;
  currentUserPage.value += 1;
  new Promise((resolve) => {
    getUserByKeyword(currentUserPage.value, userPageSize, noteDTO.value.keyword).then((res: any) => {
      const { records, total } = res.data;
      userDataList.value.push(...records);
      userTotal.value = total;
      resolve(false);
    });
  }).then((data) => {
    loadingMore.value = data as boolean;
    setTimeout(() => {
      botLoading.value = false;
    }, 500);
  });
};

watch(
  () => [route.query.keyword],
  (newVal) => {
    // 重置所有状态
    noteDTO.value.keyword = newVal[0] as string;
    noteDTO.value.cid = "";
    noteDTO.value.type = 0;
    categoryClass.value = "0";
    isShowUser.value = false;
    typeClass.value = 0;
    currentPage.value = 1;
    currentUserPage.value = 1;
    noteList.value = [];
    productDataList.value = [];
    userDataList.value = [];

    // 重新获取数据
    getNoteListByKeyword();
    getCategoryData();
    if (typeClass.value === 4) {
      getProductCategoryData();
    }
  }
);

const getNoteByType = (type: number) => {
  // 重置页面状态
  currentPage.value = 1;
  currentUserPage.value = 1;
  noteList.value = [];
  productDataList.value = [];
  isShowUser.value = false;
  noteDTO.value.type = type;
  typeClass.value = type;
  categoryClass.value = "0"; // 重置分类选择
  noteDTO.value.cid = ""; // 重置分类ID

  // 根据类型加载不同数据
  switch (type) {
    case 4: // 闲置商品
      getProductCategoryData(); // 获取商品分类
      getProductData();
      break;
    case 3: // 用户
      getUserData();
      break;
    default: // 笔记相关（全部/最热/最新）
      getCategoryData();
      getNoteListByKeyword();
      break;
  }
};

const toMain = (noteId: string) => {
  // router.push({ name: "main", state: { nid: nid } });
  nid.value = noteId;
  mainShow.value = true;
};

const close = () => {
  mainShow.value = false;
  nid.value = "";
  pid.value = "";
};

const handleLoad = (item: any) => {
  item.isLoading = true;
};

const toUser = (uid: string) => {
  router.push({ name: "user", query: { uid: uid } });
};

const refresh = () => {
  refreshTab(() => {
    topLoading.value = true;
    isShowUser.value = false;
    typeClass.value = 0;
    currentPage.value = 1;
    currentUserPage.value = 1;
    noteList.value = [];
    userDataList.value = [];
    productDataList.value = []; // 添加这行
    setTimeout(() => {
      getNoteList();
      topLoading.value = false;
    }, 1000);
  });
};

const loadMoreData = () => {
  botLoading.value = true;
  loadingMore.value = true;
  currentPage.value += 1;
  new Promise((resolve) => {
    getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
      setData(res);
      resolve(false);
    });
  }).then((data) => {
    loadingMore.value = data as boolean;
    setTimeout(() => {
      botLoading.value = false;
    }, 500);
  });
};

const loadMoreProduct = () => {
  if (loadingMore.value) return;

  botLoading.value = true;
  loadingMore.value = true;
  currentUserPage.value += 1;

  getProductByKeyword(currentUserPage.value, pageSize, noteDTO.value.keyword)
    .then((res: any) => {
      const { records, total } = res.data;
      records.forEach((item: any) => {
        item.isLoading = false;
        item.coverHeight = 240;
      });
      productDataList.value.push(...records);
      userTotal.value = total;

      // 延迟设置 isLoading
      setTimeout(() => {
        records.forEach((item: any) => {
          item.isLoading = true;
        });
      }, 500);
    })
    .finally(() => {
      loadingMore.value = false;
      setTimeout(() => {
        botLoading.value = false;
      }, 500);
    });
};

const toProduct = (productId: string) => {
  pid.value = productId;
  mainShow.value = true;
};

const setData = (res: any) => {
  const { records, total } = res.data;
  noteTotal.value = total;
  noteList.value.push(...records);
};

const getNoteList = () => {
  noteDTO.value.type = 0;
  noteDTO.value.cid = "";
  categoryClass.value = "0";
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

const getNoteListByCategory = (id: string) => {
  categoryClass.value = id;
  noteDTO.value.cid = id;
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

// 获取商品分类数据
const getProductCategoryData = () => {
  getProductCategoryAgg(noteDTO.value).then((res: any) => {
    productCategoryList.value = res.data;
  });
};

// 获取推荐商品列表
const getProductList = () => {
  productDTO.value.cid = "";
  categoryClass.value = "0";
  productDataList.value = [];
  currentUserPage.value = 1;
  getProductData();
};

// 根据分类获取商品列表
const getProductListByCategory = (id: string) => {
  categoryClass.value = id;
  productDTO.value.cid = id;
  productDataList.value = [];
  currentUserPage.value = 1;
  getProductData();
};

const getNoteListByKeyword = () => {
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

const getCategoryData = () => {
  getCategoryAgg(noteDTO.value).then((res: any) => {
    categoryList.value = res.data;
  });
};

const initData = () => {
  const keyword = route.query.keyword as string;
  if (keyword.trim().length > 0) {
    noteDTO.value.keyword = keyword as string;
    noteDTO.value.cid = "";
    categoryClass.value = "0";
    getNoteListByKeyword();
    getCategoryData();
  }
};

initData();
</script>

<style lang="less" scoped>
.feeds-page {
  flex: 1;
  padding: 0 1.5rem;
  padding-top: 4.5rem;
  height: 100vh;
  position: relative;

  // 移动端为底部导航栏留出空间
  @media screen and (max-width: 695px) {
    padding: 0;
    padding-top: 64px;
    padding-bottom: 70px;
  }

  .middle {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.5rem;

    // 移动端优化
    @media screen and (max-width: 695px) {
      margin-top: 0.25rem;
    }
    .channel-scroll-container {
      position: relative;
      overflow: hidden;
      display: flex;
      user-select: none;
      -webkit-user-select: none;
      align-items: center;
      font-size: 1rem;
      color: rgba(51, 51, 51, 0.8);
      height: 3rem;
      white-space: nowrap;

      // 移动端优化
      @media screen and (max-width: 695px) {
        height: 2.5rem;
      }

      .content-container {
        display: flex;
        overflow-x: scroll;
        overflow-y: hidden;
        white-space: nowrap;
        color: rgba(51, 51, 51, 0.8);

        .channel {
          height: 2.5rem;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 0 1rem;
          cursor: pointer;
          -webkit-user-select: none;
          user-select: none;
        }
        .active {
          font-weight: 600;
        }

        .active,
        .channel:hover {
          background: rgba(0, 0, 0, 0.03);
          border-radius: 62.4375rem;
          color: #333;
        }
      }
    }
  }

  .filter-box {
    display: flex;
    align-items: center;
    justify-content: center;

    // 移动端隐藏
    @media screen and (max-width: 695px) {
      display: none;
    }

    .filter {
      height: 2.5rem;
      font-size: 1rem;
      line-height: 120%;
      color: rgba(51, 51, 51, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      -webkit-user-select: none;
      user-select: none;
      padding: 0 1rem;
    }
  }

  .divider.rec-filter {
    height: 0.0625rem;
    background: rgba(0, 0, 0, 0.08);
    margin: 0.5rem 0;

    // 移动端优化
    @media screen and (max-width: 695px) {
      margin: 0.25rem 0;
    }
  }

  .channel-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    user-select: none;
    -webkit-user-select: none;
    margin-top: 0.5rem;
    margin-bottom: 0.5rem;

    // 移动端优化
    @media screen and (max-width: 695px) {
      margin-top: 0.25rem;
      margin-bottom: 0.25rem;
    }

    .channel-scroll-container {
      backdrop-filter: blur(1.25rem);
      background-color: transparent;
      width: calc(100vw - 1.5rem);

      position: relative;
      overflow: hidden;
      display: flex;
      user-select: none;
      -webkit-user-select: none;
      align-items: center;
      font-size: 1rem;
      color: rgba(51, 51, 51, 0.8);
      height: 3rem;
      white-space: nowrap;

      // 移动端优化
      @media screen and (max-width: 695px) {
        height: 2.5rem;
      }

      .content-container::-webkit-scrollbar {
        display: none;
      }

      .content-container {
        display: flex;
        overflow-x: scroll;
        overflow-y: hidden;
        white-space: nowrap;
        color: rgba(51, 51, 51, 0.8);

        .active {
          font-weight: 600;
          background: rgba(9, 152, 235, 0.06);
          color: #3d8af5;
        }

        .channel {
          height: 2.5rem;
          margin-right: 0.75rem;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 0 1rem;
          cursor: pointer;
          -webkit-user-select: none;
          user-select: none;
          //background: rgba(0, 0, 0, 0.03);
          border-radius: 0.625rem;
        }
      }
    }
  }

  .feeds-container {
    position: relative;
    transition: width 0.5s;
    margin: 0 auto;

    // 移动端优化
    @media screen and (max-width: 695px) {
      padding: 0;
    }

    .feeds-loading {
      margin: 3vh;
      text-align: center;
    }

    .feeds-loading-top {
      text-align: center;
      line-height: 6vh;
      height: 6vh;
    }

    .noteImg {
      width: 15rem;
      max-height: 18.75rem;
      object-fit: cover;
      border-radius: 0.5rem;
    }

    .note-cover-img {
      width: 100%;
      display: block;
      border-radius: 8px 8px 0 0;
      cursor: pointer;

      // 移动端优化
      @media screen and (max-width: 695px) {
        border-radius: 8px 8px 0 0;
      }

      :deep(img) {
        width: 100%;
        height: auto;
        max-height: 350px;
        object-fit: cover;
        display: block;

        // 移动端优化
        @media screen and (max-width: 695px) {
          max-height: 300px;
        }
      }
    }

    .product-cover-img {
      width: 100%;
      display: block;
      border-radius: 8px 8px 0 0;
      cursor: pointer;

      // 移动端优化
      @media screen and (max-width: 695px) {
        border-radius: 8px 8px 0 0;
      }

      :deep(img) {
        width: 100%;
        height: auto;
        max-height: 350px;
        object-fit: cover;
        display: block;

        // 移动端优化
        @media screen and (max-width: 695px) {
          max-height: 300px;
        }
      }
    }

    .card {
      position: relative;
      background-color: var(--card-bg);
      border-radius: 0.5rem;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      width: 100%;

      // 移动端优化
      @media screen and (max-width: 695px) {
        border-radius: 8px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
      }
    }

    .footer {
      padding: 0.75rem;

      // 移动端优化
      @media screen and (max-width: 695px) {
        padding: 8px 10px;
      }

      .title {
        margin-bottom: 0.5rem;
        word-break: break-all;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
        font-weight: 500;
        font-size: 0.875rem;
        line-height: 140%;
        color: var(--card-text);

        // 移动端优化
        @media screen and (max-width: 695px) {
          font-size: 13px;
          line-height: 1.4;
          margin-bottom: 6px;
        }
      }

      .author-wrapper {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 1.25rem;
        color: rgba(51, 51, 51, 0.8);
        font-size: 0.75rem;
        transition: color 1s;

        // 移动端优化
        @media screen and (max-width: 695px) {
          font-size: 11px;
          height: 18px;
        }

        .author {
          display: flex;
          align-items: center;
          color: inherit;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-right: 0.75rem;

          .author-avatar {
            margin-right: 0.375rem;
            width: 1.25rem;
            height: 1.25rem;
            border-radius: 1.25rem;
            border: 0.0625rem solid rgba(0, 0, 0, 0.08);
            flex-shrink: 0;
            object-fit: cover;

            // 移动端优化
            @media screen and (max-width: 695px) {
              width: 18px;
              height: 18px;
              margin-right: 4px;
            }
          }

          .name {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .like-wrapper {
          position: relative;
          cursor: pointer;
          display: flex;
          align-items: center;
          flex-shrink: 0;

          .count {
            margin-left: 0.125rem;
          }
        }
      }
    }
  }

  .floating-btn-sets {
    position: fixed;
    display: flex;
    flex-direction: column;
    width: 2.5rem;
    grid-gap: 0.5rem;
    gap: 0.5rem;
    right: 1.5rem;
    bottom: 1.5rem;

    .back-top {
      width: 2.5rem;
      height: 2.5rem;
      background: #fff;
      border: 0.0625rem solid rgba(0, 0, 0, 0.08);
      border-radius: 6.25rem;
      color: rgba(51, 51, 51, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      // transition: background 0.2s;
      cursor: pointer;
    }

    .reload {
      width: 2.5rem;
      height: 2.5rem;
      background: #fff;
      border: 0.0625rem solid rgba(0, 0, 0, 0.08);
      box-shadow: 0 0.125rem 0.5rem 0 rgba(0, 0, 0, 0.1), 0 0.0625rem 0.125rem 0 rgba(0, 0, 0, 0.02);
      border-radius: 6.25rem;
      color: rgba(51, 51, 51, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      //transition: background 0.2s;
      cursor: pointer;
    }
  }

  .agree-container {
    .agree-item {
      display: flex;
      flex-direction: row;
      padding-top: 1.5rem;

      .user-avatar {
        margin-right: 1.5rem;
        flex-shrink: 0;

        .avatar-item {
          width: 3rem;
          height: 3rem;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          border-radius: 100%;
          border: 0.0625rem solid rgba(0, 0, 0, 0.08);
          object-fit: cover;
        }
      }

      .main {
        flex-grow: 1;
        flex-shrink: 1;
        display: flex;
        flex-direction: row;
        padding-bottom: 0.75rem;
        border-bottom: 0.0625rem solid rgba(0, 0, 0, 0.08);

        .info {
          flex-grow: 1;
          flex-shrink: 1;

          .user-info {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 0.25rem;

            a {
              color: #333;
              font-size: 1rem;
              font-weight: 600;
            }
          }

          .interaction-hint {
            font-size: 0.875rem;
            color: rgba(51, 51, 51, 0.6);
            margin-bottom: 0.5rem;
          }

          .interaction-content {
            display: flex;
            font-size: 0.875rem;
            color: #333;
            line-height: 140%;
            cursor: pointer;
            margin-bottom: 0.75rem;

            .msg-count {
              width: 1.25rem;
              height: 1.25rem;
              line-height: 1.25rem;
              font-size: 0.8125rem;
              color: #fff;
              background-color: red;
              text-align: center;
              border-radius: 100%;
            }
          }

          .quote-info {
            font-size: 0.75rem;
            display: flex;
            align-items: center;
            color: rgba(51, 51, 51, 0.6);
            margin-bottom: 0.75rem;
            cursor: pointer;
          }

          .quote-info::before {
            content: "";
            display: inline-block;
            border-radius: 0.5rem;
            margin-right: 0.375rem;
            width: 0.25rem;
            height: 1.0625rem;
            background: rgba(0, 0, 0, 0.08);
          }
        }

        .extra {
          min-width: 3rem;
          flex-shrink: 0;
          margin-left: 1.5rem;

          .follow-button {
            width: 6rem;
          }

          .reds-button-new.large {
            font-size: 1rem;
            font-weight: 600;
            line-height: 1rem;
            padding: 0 1.5rem;
            height: 2.5rem;
          }

          .reds-button-new.primary {
            background-color: #ff2e4d;
            color: #fff;
          }

          .reds-button-new {
            position: relative;
            cursor: pointer;
            -webkit-user-select: none;
            user-select: none;
            white-space: nowrap;
            outline: none;
            background: none;
            border: none;
            vertical-align: middle;
            text-align: center;
            display: inline-block;
            padding: 0;
            border-radius: 6.25rem;
            font-weight: 500;
          }
        }
      }
    }
  }
}
</style>
